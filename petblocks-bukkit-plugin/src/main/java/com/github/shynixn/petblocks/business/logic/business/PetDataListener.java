package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ChatBuilder;
import com.github.shynixn.petblocks.lib.SimpleListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

class PetDataListener extends SimpleListener {
    private final PetDataManager manager;
    private final Set<Player> namingPlayers = new HashSet<>();
    private final Set<Player> namingSkull = new HashSet<>();
    private final Set<Player> changingPlayers = new HashSet<>();
    private String headDatabaseTitle;
    private String headDatabaseSearch;

    /**
     * Initializes a new PetDataListener
     *
     * @param manager manager
     * @param plugin  plugin
     */
    PetDataListener(PetDataManager manager, Plugin plugin) {
        super(plugin);
        this.manager = manager;
    }

    /**
     * Removes the petblock from the player when he leaves the server
     *
     * @param event playerQuitEvent
     */
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (this.manager.headDatabasePlayers.contains(event.getPlayer())) {
            this.manager.headDatabasePlayers.remove(event.getPlayer());
        }
        PetBlocksApi.removePetBlock(event.getPlayer());
    }

    @EventHandler
    public void playerClickEvent(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getTitle().equals(Language.GUI_TITLE) && this.manager.inventories.containsKey(player) && this.manager.inventories.get(player).equals(event.getInventory())) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
            if (PetBlocksApi.hasPetBlock(player)) {
                final PetBlock petBlock = PetBlocksApi.getPetBlock(player);
                this.handleClick(event, player, (com.github.shynixn.petblocks.api.persistence.entity.PetMeta) petBlock.getPetMeta(), petBlock);
            } else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMeta(player);
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.handleClick(event, player, petMeta, null));
                });
            }
        } else if (this.manager.headDatabasePlayers.contains(player)) {
            if (this.headDatabaseTitle == null) {
                final Plugin plugin = Bukkit.getPluginManager().getPlugin("HeadDatabase");
                this.headDatabaseTitle = this.constructPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.database")));
                this.headDatabaseSearch = this.constructPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.search")));
            }
            final String currentTitle = ChatColor.stripColor(event.getView().getTopInventory().getTitle());
            if (!currentTitle.startsWith(this.headDatabaseTitle) && !currentTitle.startsWith(this.headDatabaseSearch))
                return;
            this.linkHeadDatabaseItemToPetBlocks(event.getCurrentItem(), player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerJoinEvent(final PlayerJoinEvent event) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final PetMeta petMeta;
            if (Config.getInstance().isJoin_enabled()) {
                if (!this.manager.hasPetMeta(event.getPlayer()) || Config.getInstance().isJoin_overwriteExistingPet()) {
                    final PetMeta meta = this.manager.createPetMeta(event.getPlayer(), PetType.CAT);
                    Config.getInstance().fixJoinDefaultPet(meta);
                    this.manager.persist(meta);
                }
            } else {
                this.manager.hasPetMeta(event.getPlayer());
            }
            if ((petMeta = PetBlocksApi.getPetMeta(event.getPlayer())) != null && ((PetData)petMeta).isEnabled()) {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    PetBlocksApi.removePetBlock(event.getPlayer());
                    PetBlocksApi.setPetBlock(event.getPlayer(), petMeta);
                }, 2L);
            }
        });
    }

    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        if (this.manager.inventories.containsKey(player)) {
            this.manager.inventories.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerChatEvent(PlayerChatEvent event) {
        if (!Config.getInstance().isChat_async() && Config.getInstance().isChatHighestPriority()) {
            this.handleChatMessage(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerChatEvent2(PlayerChatEvent event) {
        if (!Config.getInstance().isChat_async() && !Config.getInstance().isChatHighestPriority()) {
            this.handleChatMessage(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerChatEvent3(final AsyncPlayerChatEvent event) {
        if (Config.getInstance().isChat_async() && Config.getInstance().isChatHighestPriority()) {
            if (this.namingPlayers.contains(event.getPlayer()) || this.namingSkull.contains(event.getPlayer()))
                event.setCancelled(true);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> PetDataListener.this.handleChatMessage(new PlayerChatEvent(event.getPlayer(), event.getMessage())), 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerChatEvent4(final AsyncPlayerChatEvent event) {
        if (Config.getInstance().isChat_async() && !Config.getInstance().isChatHighestPriority()) {
            if (this.namingPlayers.contains(event.getPlayer()) || this.namingSkull.contains(event.getPlayer()))
                event.setCancelled(true);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> PetDataListener.this.handleChatMessage(new PlayerChatEvent(event.getPlayer(), event.getMessage())), 1L);
        }
    }

    private void handleClick(InventoryClickEvent event, Player player, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (this.changingPlayers.contains(player))
            return;
        if (this.manager.gui.getPetType(event.getCurrentItem()) != null) {
            final PetType type = this.manager.gui.getPetType(event.getCurrentItem());
            if (player.hasPermission(Permission.ALLPETTYPES.get()) || player.hasPermission(Permission.SINGLEPETTYPE.get() + type.name().toLowerCase())) {
                if (PetBlocksApi.hasPetBlock(player))
                    PetBlocksApi.removePetBlock(player);
                this.changingPlayers.add(player);
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final com.github.shynixn.petblocks.api.persistence.entity.PetMeta meta = this.manager.getPetMeta(player);
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final com.github.shynixn.petblocks.api.persistence.entity.PetMeta currentPetMeta = PetDataListener.this.manager.createPetMeta(player, type);
                        if (meta != null) {
                            currentPetMeta.setParticleId(meta.getParticleId());
                            currentPetMeta.setPlayerId(meta.getPlayerId());
                            currentPetMeta.setPlayerMeta(meta.getPlayerMeta());
                            currentPetMeta.setParticleEffectMeta(meta.getParticleEffectMeta());
                            ((PetData) currentPetMeta).setId(meta.getId());
                        }
                        if (ConfigGUI.getInstance().isCopySkinEnabled()) {
                            ((PetData)currentPetMeta).setSkin(Material.SKULL_ITEM, (short) 3, ConfigGUI.getInstance().getContainer(((PetData)currentPetMeta).getType()).getSkullName());
                        }
                        PetDataListener.this.manager.gui.setItems(GUIPage.MAIN, player, ((PetData)currentPetMeta).getType(), false, true, currentPetMeta);
                        PetDataListener.this.plugin.getServer().getScheduler().runTaskAsynchronously(PetDataListener.this.plugin, () -> {
                            PetDataListener.this.manager.persist(currentPetMeta);
                            PetDataListener.this.plugin.getServer().getScheduler().runTask(PetDataListener.this.plugin, () -> PetDataListener.this.changingPlayers.remove(player));
                        });
                    });
                });
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.MY_PET)) {
            if (!PetBlocksApi.hasPetBlock(player) && ConfigGUI.getInstance().isOnlyDisableItemEnabled()) {
                ((PetData)petMeta).setEnabled(true);
                PetBlocksApi.setPetBlock(player, petMeta);
                PetDataListener.this.manager.gui.setItems(GUIPage.MAIN, player, ((PetData)petMeta).getType(), true, false, petMeta);
                this.persistAsynchronously(petMeta);
            } else {
                if (ConfigGUI.getInstance().isCopySkinEnabled()) {
                    ((PetData)petMeta).setSkin(Material.SKULL_ITEM, (short) 3, ConfigGUI.getInstance().getContainer(((PetData)petMeta).getType()).getSkullName());
                } else {
                    ((PetData)petMeta).setSkin(Material.getMaterial(ConfigGUI.getInstance().getItems_defaultcostumeContainer().getId()), (short) ConfigGUI.getInstance().getItems_defaultcostumeContainer().getDamage(), ConfigGUI.getInstance().getItems_defaultcostumeContainer().getSkullName());
                }
                ((PetData)petMeta).setParticleEffect(null);
                if (petBlock != null) {
                    petBlock.respawn();
                }
                this.persistAsynchronously(petMeta);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.ENABLE_PET) && petMeta != null) {
            ((PetData)petMeta).setEnabled(true);
            PetBlocksApi.setPetBlock(player, petMeta);
            PetDataListener.this.manager.gui.setItems(GUIPage.MAIN, player, ((PetData)petMeta).getType(), true, false, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.DISABLE_PET) && petMeta != null) {
            ((PetData)petMeta).setEnabled(false);
            PetBlocksApi.removePetBlock(player);
            this.manager.gui.setItems(GUIPage.MAIN, player, ((PetData)petMeta).getType(), false, false, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.MUTE) && petMeta != null) {
            petMeta.setSoundEnabled(false);
            this.manager.gui.setItems(null, player, ((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), false, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.UNMUTE)) {
            petMeta.setSoundEnabled(true);
            this.manager.gui.setItems(null, player, ((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), false, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.PREVIOUS)) {
            this.manager.gui.setCostumes(player, this.manager.gui.getItemstackFromPage(this.manager.pages.get(player).page), this.manager.pages.get(player).page, 2);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.NEXT)) {
            this.manager.gui.setCostumes(player, this.manager.gui.getItemstackFromPage(this.manager.pages.get(player).page), this.manager.pages.get(player).page, 1);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.COSTUME)) {
            this.manager.gui.setDefaultCostumeItems(player);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.COLOR_COSTUME)) {
            this.manager.gui.setColorCostumeItems(player);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CUSTOM_COSTUME)) {
            this.manager.gui.setCustomCostumeItems(player);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.PARTICLE)) {
            this.manager.gui.setParticleItems(player);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.MINECRAFT_HEADS_COSTUME)) {
            this.manager.gui.setMinecraftHeadsCostumeItems(player);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.SUGGEST_HEADS)) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
                new ChatBuilder().text(Language.PREFIX)
                        .text("Click here: ")
                        .component(">>Submit skin<<")
                        .setColor(ChatColor.YELLOW)
                        .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com/custom/heads-generator")
                        .setHoverText("Goto the Minecraft-Heads website!")
                        .builder()
                        .text(" ")
                        .component(">>Suggest new pet<<")
                        .setColor(ChatColor.YELLOW)
                        .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com/forum/suggesthead")
                        .setHoverText("Goto the Minecraft-Heads website!")
                        .builder()
                        .sendMessage(player);
            });
            player.closeInventory();
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.HEAD_DATABASE_COSTUME) && player.hasPermission(Permission.ALLHEADATABASECOSTUMES.get())) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                final Plugin plugin = Bukkit.getPluginManager().getPlugin("HeadDatabase");
                if (plugin == null) {
                    Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
                        new ChatBuilder().text(Language.PREFIX)
                                .text("Download the plugin ")
                                .component(">>Head Database<<")
                                .setColor(ChatColor.YELLOW)
                                .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "https://www.spigotmc.org/resources/14280/")
                                .setHoverText("A valid spigot account is required!")
                                .builder().sendMessage(player);
                        player.sendMessage(Language.PREFIX + ChatColor.GRAY + "Please consider that PetBlocks is not responsible for any legal agreements between the author of Head Database and yourself.");
                    });
                } else {
                    this.manager.headDatabasePlayers.add(player);
                    player.performCommand("hdb");
                }
            }, 10L);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.WARDROBE)) {
            this.manager.gui.setItems(GUIPage.WARDROBE, player,((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), true, petMeta);
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CALL) && petBlock != null) {
            petBlock.teleport(player);
            player.closeInventory();
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.HAT) && petBlock != null) {
            if (player.hasPermission(Permission.WEARPET.get())) {
                petBlock.wear(player);
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.RIDING) && petBlock != null) {
            if (player.hasPermission(Permission.RIDEPET.get())) {
                petBlock.ride(player);
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.NAMING)) {
            if (player.hasPermission(Permission.RENAMEPET.get())) {
                this.namingPlayers.add(player);
                player.closeInventory();
                player.sendMessage(Language.PREFIX + Language.NAME_MESSAGE);
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.SKULL_NAMING)) {
            if (player.hasPermission(Permission.RENAMESKULL.get())) {
                this.namingSkull.add(player);
                player.closeInventory();
                player.sendMessage(Language.PREFIX + Language.SNAME_MESSAGE);
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CANNON) && petBlock != null) {
            if (player.hasPermission(Permission.CANNON.get())) {
                player.closeInventory();
                petBlock.launch(this.getDirection(player));
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CANCEL)) {
            player.closeInventory();
        } else if (this.manager.pages.get(player).page == GUIPage.PARTICLES) {
            if (!player.hasPermission(Permission.ALLPARTICLES.get()) && !player.hasPermission(Permission.SINGLEPARTICLE.get() + "" + event.getSlot())) {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            } else {
                ((PetData)petMeta).setParticleEffect(ConfigParticle.getInstance().getParticle(event.getSlot()));
            }
            player.closeInventory();
            this.manager.gui.open(player);
            this.manager.gui.setItems(GUIPage.MAIN, player, ((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), false, petMeta);
            this.persistAsynchronously(petMeta);
            if (petBlock != null)
                petBlock.respawn();
        } else if (!BukkitUtilities.compareItemName(event.getCurrentItem(), Language.EMPTY)
                && (this.manager.pages.get(player).page == GUIPage.DEFAULT_COSTUMES
                || this.manager.pages.get(player).page == GUIPage.CUSTOM_COSTUMES
                || this.manager.pages.get(player).page == GUIPage.COLOR_COSTUMES
                || this.manager.pages.get(player).page == GUIPage.MINECRAFTHEADS_COSTUMES)
                && event.getCurrentItem() != null) {
            final int number;
            try {
                String s = event.getCurrentItem().getItemMeta().getDisplayName();
                s = ChatColor.stripColor(s);
                number = Integer.parseInt(s);
            } catch (final Exception ex) {
                BukkitUtilities.sendColorMessage("Number prefix (lang.yml) only allows color codes!", ChatColor.RED, "[PetBlocks]");
                player.sendMessage(ChatColor.RED + "Number-prefix (lang.yml) only allows color codes! Contact an admin in order to resolve this issue.");
                player.closeInventory();
                return;
            }
            if (this.manager.pages.get(player).page == GUIPage.DEFAULT_COSTUMES && (!player.hasPermission(Permission.ALLDEFAULTCOSTUMES.get()) && !player.hasPermission(Permission.SINGLEDEFAULTCOSTUME.get() + "" + number))) {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            } else if (this.manager.pages.get(player).page == GUIPage.COLOR_COSTUMES && (!player.hasPermission(Permission.ALLCOLORCOSTUMES.get()) && !player.hasPermission(Permission.SINGLECOLORCOSTUME.get() + "" + number))) {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            } else if (this.manager.pages.get(player).page == GUIPage.CUSTOM_COSTUMES && (!player.hasPermission(Permission.ALLCUSTOMCOSTUMES.get()) && !player.hasPermission(Permission.SINGLECUSTOMCOSTUME.get() + "" + number))) {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            } else if (this.manager.pages.get(player).page == GUIPage.MINECRAFTHEADS_COSTUMES && (!player.hasPermission(Permission.ALLMINECRAFTHEADSCOSTUMES.get()) && !player.hasPermission(Permission.SINGLEMINECRAFTHEADSCOSTUME.get() + "" + number))) {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            } else if (event.getClickedInventory().equals(event.getView().getTopInventory()) || player.hasPermission(Permission.OWNINGAMECOSTUMES.get())) {
                if (petMeta == null)
                    return;
                if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
                    ((PetData)petMeta).setSkin(event.getCurrentItem().getType(), event.getCurrentItem().getDurability(), null);
                else {
                    final SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
                    if (meta.getOwner() == null) {
                        ((PetData)petMeta).setSkin(event.getCurrentItem().getType(), event.getCurrentItem().getDurability(), NMSRegistry.getSkinUrl(event.getCurrentItem()));
                    } else {
                        ((PetData)petMeta).setSkin(event.getCurrentItem().getType(), event.getCurrentItem().getDurability(), ((SkullMeta) event.getCurrentItem().getItemMeta()).getOwner());
                    }
                }
                ((PetData)petMeta).setUnbreakable(NMSRegistry.isUnbreakable(event.getCurrentItem()));
                player.closeInventory();
                this.manager.gui.open(player);
                this.manager.gui.setItems(GUIPage.MAIN, player,((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), false, petMeta);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
            } else {
                player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
            }
        } else if (ConfigGUI.getInstance().isEmptyClickBackEnabled() && BukkitUtilities.compareItemName(event.getCurrentItem(), Language.EMPTY)) {
            if (this.manager.pages.containsKey(player) && this.manager.pages.get(player).page != GUIPage.MAIN) {
                if (this.manager.pages.get(player).page == GUIPage.WARDROBE) {
                    this.manager.gui.setItems(GUIPage.MAIN, player,((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), true, petMeta);
                }
                if (petMeta != null) {
                    this.manager.gui.setItems(this.manager.pages.get(player).previousPage, player, ((PetData)petMeta).getType(), PetBlocksApi.hasPetBlock(player), true, petMeta);
                }
            }
        }
    }

    private void persistAsynchronously(com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.persist(petMeta));
    }

    private void renameName(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > ConfigPet.getInstance().getDesign_maxPetNameLength()) {
            player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
        } else {
            try {
                this.namingPlayers.remove(player);
                petMeta.setDisplayName(message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Language.PREFIX + Language.NAME_SUCCES_MESSAGE);
            } catch (final Exception e) {
                player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
            }
        }
    }

    private void renameSkull(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > 20) {
            player.sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
        } else {
            try {
                this.namingSkull.remove(player);
                ((PetData)petMeta).setSkin(Material.SKULL_ITEM, (short) 3, message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Language.PREFIX + Language.SNAME_SUCCES_MESSAGE);
            } catch (final Exception e) {
                player.sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
            }
        }
    }

    /**
     * Sets the given itemStack as petMeta skin if it full fills the skull requirements
     *
     * @param itemStack itemStack
     * @param player    player
     * @return success
     */
    private boolean linkHeadDatabaseItemToPetBlocks(ItemStack itemStack, Player player) {
        if (itemStack != null
                && itemStack.getType() == Material.SKULL_ITEM
                && itemStack.getItemMeta() != null
                && itemStack.getItemMeta().getDisplayName() != null
                && itemStack.getItemMeta().getDisplayName().startsWith(ChatColor.BLUE.toString())) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMeta(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                            if (meta.getOwner() == null) {
                                ((PetData)petMeta).setSkin(itemStack.getType(), itemStack.getDurability(), NMSRegistry.getSkinUrl(itemStack));
                            } else {
                                ((PetData)petMeta).setSkin(itemStack.getType(), itemStack.getDurability(), ((SkullMeta) itemStack.getItemMeta()).getOwner());
                            }
                            this.persistAsynchronously(petMeta);
                            if (PetBlocksApi.hasPetBlock(player)) {
                                PetBlocksApi.getPetBlock(player).respawn();
                            }
                            player.performCommand("petblock");
                        }
                );
            });
            return true;
        }
        return false;
    }

    private void handleChatMessage(PlayerChatEvent event) {
        if (this.namingPlayers.contains(event.getPlayer()) || this.namingSkull.contains(event.getPlayer()))
            event.setCancelled(true);
        if (PetBlocksApi.hasPetBlock(event.getPlayer())) {
            final PetBlock petBlock = PetBlocksApi.getPetBlock(event.getPlayer());
            if (this.namingSkull.contains(event.getPlayer())) {
                this.renameSkull(event.getPlayer(), event.getMessage(), (com.github.shynixn.petblocks.api.persistence.entity.PetMeta) petBlock.getPetMeta(), petBlock);
            } else if (this.namingPlayers.contains(event.getPlayer())) {
                this.renameName(event.getPlayer(), event.getMessage(), (com.github.shynixn.petblocks.api.persistence.entity.PetMeta) petBlock.getPetMeta(), petBlock);
            }
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMeta(event.getPlayer());
                if (this.namingSkull.contains(event.getPlayer())) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.renameSkull(event.getPlayer(), event.getMessage(), petMeta, null));
                } else if (this.namingPlayers.contains(event.getPlayer())) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.renameName(event.getPlayer(), event.getMessage(), petMeta, null));
                }
            });
        }
    }

    /**
     * Constructs a prefix from the given source
     *
     * @param source source
     * @return prefix
     */
    private String constructPrefix(String source) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            final char t = source.charAt(i);
            if (t != '%') {
                b.append(t);
            } else {
                break;
            }
        }
        return ChatColor.stripColor(b.toString());
    }

    private Vector getDirection(Player player) {
        final Vector vector = new Vector();
        final double rotX = player.getLocation().getYaw();
        final double rotY = player.getLocation().getPitch();
        vector.setY(-Math.sin(Math.toRadians(rotY)));
        final double h = Math.cos(Math.toRadians(rotY));
        vector.setX(-h * Math.sin(Math.toRadians(rotX)));
        vector.setZ(h * Math.cos(Math.toRadians(rotX)));
        vector.setY(0.5);
        return vector.multiply(3);
    }
}
