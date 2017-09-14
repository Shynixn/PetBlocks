package com.github.shynixn.petblocks.business.logic.business.listener;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.logic.business.configuration.Config;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
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

public class PetDataListener extends SimpleListener {
    private final PetBlockManager manager;
    private final Set<Player> namingPlayers = new HashSet<>();
    private final Set<Player> namingSkull = new HashSet<>();
    private final Set<Player> changingPlayers = new HashSet<>();
    private String headDatabaseTitle;
    private String headDatabaseSearch;
    private final ChatBuilder suggestHeadMessage = new ChatBuilder().text(Config.getInstance().getPrefix())
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
            .builder();
    private final ChatBuilder headDatabaseMessage = new ChatBuilder().text(Config.getInstance().getPrefix())
            .text("Download the plugin ")
            .component(">>Head Database<<")
            .setColor(ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "https://www.spigotmc.org/resources/14280/")
            .setHoverText("A valid spigot account is required!")
            .builder();

    /**
     * Initializes a new PetDataListener
     *
     * @param manager manager
     * @param plugin  plugin
     */
    public PetDataListener(PetBlockManager manager, Plugin plugin) {
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
        PetBlocksApi.getDefaultPetBlockController().removeByPlayer(event.getPlayer());
    }

    @EventHandler
    public void playerClickEvent(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getTitle().equals(Config.getInstance().getGUITitle())
                && this.manager.inventories.containsKey(player)
                && this.manager.inventories.get(player).equals(event.getInventory())) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
            PetBlock petBlock;
            if ((petBlock = PetBlocksApi.getDefaultPetBlockController().getByPlayer(player)) != null) {
                this.handleClick(event, player, petBlock.getMeta(), petBlock);
            } else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMetaController().getByPlayer(player);
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
                if (this.manager.getPetMetaController().getByPlayer(event.getPlayer()) == null || Config.getInstance().isJoin_overwriteExistingPet()) {
                    final PetMeta meta = this.manager.getPetMetaController().create(event.getPlayer());
                    Config.getInstance().fixJoinDefaultPet(meta);
                    this.manager.getPetMetaController().store(meta);
                }
            }
            if ((petMeta = PetBlocksApi.getDefaultPetMetaController().getByPlayer(event.getPlayer())) != null && petMeta.isEnabled()) {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    PetBlocksApi.getDefaultPetBlockController().removeByPlayer(event.getPlayer());
                    final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(event.getPlayer(), petMeta);
                    PetBlocksApi.getDefaultPetBlockController().store(petBlock);
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

    private void handleClick(InventoryClickEvent event, Player player, PetMeta petMeta, PetBlock petBlock) {
        ItemStack currentItem = event.getCurrentItem();
        if (this.changingPlayers.contains(player))
            return;
       /* if (this.manager.gui.getPetType(event.getCurrentItem()) != null) {
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
        }*/
        else if (this.manager.pages.get(player).page == GUIPage.MAIN && this.getGUIItem("my-pet").getPosition() == event.getSlot()) {
            this.handleClickOnMyPetItem(player, petMeta);
        } else if (this.isGUIItem(currentItem, "enable-pet")) {
            this.setPetBlock(player, petMeta);
            this.refreshGUI(player, petMeta);
        } else if (this.isGUIItem(currentItem, "disable-pet")) {
            this.removePetBlock(player, petMeta);
            this.refreshGUI(player, petMeta);
        } else if (this.isGUIItem(currentItem, "sounds-enabled-pet")) {
            petMeta.setSoundEnabled(true);
            this.refreshGUI(player, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (this.isGUIItem(currentItem, "sounds-disabled-pet")) {
            petMeta.setSoundEnabled(false);
            this.refreshGUI(player, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (this.isGUIItem(currentItem, "next-page")) {
            this.manager.gui.moveSubPage(player, 1);
        } else if (this.isGUIItem(currentItem, "previous-page")) {
            this.manager.gui.moveSubPage(player, -1);
        } else if (this.isGUIItem(currentItem, "ordinary-costume")) {
            this.manager.gui.setPage(player, GUIPage.DEFAULT_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "color-costume")) {
            this.manager.gui.setPage(player, GUIPage.COLOR_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "rare-costume")) {
            this.manager.gui.setPage(player, GUIPage.CUSTOM_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "minecraft-heads-costume")) {
            this.manager.gui.setPage(player, GUIPage.MINECRAFTHEADS_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "particle-pet")) {
            this.manager.gui.setPage(player, GUIPage.PARTICLES, petMeta);
        } else if (this.isGUIItem(currentItem, "wardrobe")) {
            this.manager.gui.setPage(player, GUIPage.WARDROBE, petMeta);
        } else if (this.isGUIItem(currentItem, "engine-settings")) {
            this.manager.gui.setPage(player, GUIPage.ENGINES, petMeta);
        } else if (this.isGUIItem(currentItem, "call-pet")) {
            petBlock.teleport(player.getLocation());
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "hat-pet") && this.hasPermission(player, Permission.WEARPET)) {
            petBlock.wear(player);
        } else if (this.isGUIItem(currentItem, "riding-pet") && this.hasPermission(player, Permission.RIDEPET)) {
            petBlock.ride(player);
        } else if (this.isGUIItem(currentItem, "suggest-heads")) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
                this.suggestHeadMessage.sendMessage(player);
            });
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "head-database-costume") && this.hasPermission(player, Permission.ALLHEADATABASECOSTUMES)) {
            this.handleClickItemHeadDatabaseCostumes(player);
        } else if (this.isGUIItem(currentItem, "naming-pet") && this.hasPermission(player, Permission.RENAMEPET)) {
            this.namingPlayers.add(player);
            player.closeInventory();
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingMessage());
        } else if (this.isGUIItem(currentItem, "skullnaming-pet") && this.hasPermission(player, Permission.RENAMESKULL)) {
            this.namingSkull.add(player);
            player.closeInventory();
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingMessage());
        } else if (this.isGUIItem(currentItem, "cannon-pet") && this.hasPermission(player, Permission.CANNON)) {
            petBlock.setVelocity(this.getDirection(player));
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "back")) {
            this.manager.gui.backPage(player, petMeta);
        } else if (this.manager.pages.get(player).page == GUIPage.PARTICLES && this.hasPermission(player, Permission.ALLPARTICLES.get(), Permission.SINGLEPARTICLE.get() + "" + event.getSlot())) {
            final GUIItemContainer container = Config.getInstance().getParticleController().getContainerByPosition(event.getSlot()+1);
            petMeta.setParticleEffectMeta(Config.getInstance().getParticleController().getByItem(container));
            this.persistAsynchronously(petMeta);
            if (petBlock != null) {
                petBlock.respawn();
            }
        } else if (this.manager.pages.get(player).page == GUIPage.DEFAULT_COSTUMES && this.hasPermission(player, Permission.ALLDEFAULTCOSTUMES.get(), Permission.SINGLEDEFAULTCOSTUME.get() + "" + event.getSlot())) {
            final GUIItemContainer container = Config.getInstance().getOrdinaryCostumesController().getContainerByPosition(event.getSlot());
            this.setCostumeSkin(petMeta, petBlock, container);
        } else if (this.manager.pages.get(player).page == GUIPage.COLOR_COSTUMES && this.hasPermission(player, Permission.ALLCOLORCOSTUMES.get(), Permission.SINGLECOLORCOSTUME.get() + "" + event.getSlot())) {
            final GUIItemContainer container = Config.getInstance().getColorCostumesController().getContainerByPosition(event.getSlot());
            this.setCostumeSkin(petMeta, petBlock, container);
        } else if (this.manager.pages.get(player).page == GUIPage.CUSTOM_COSTUMES && this.hasPermission(player, Permission.ALLCUSTOMCOSTUMES.get(), Permission.SINGLECUSTOMCOSTUME.get() + "" + event.getSlot())) {
            final GUIItemContainer container = Config.getInstance().getRareCostumesController().getContainerByPosition(event.getSlot());
            this.setCostumeSkin(petMeta, petBlock, container);
        } else if (this.manager.pages.get(player).page == GUIPage.MINECRAFTHEADS_COSTUMES && this.hasPermission(player, Permission.ALLHEADATABASECOSTUMES.get(), Permission.SINGLEMINECRAFTHEADSCOSTUME.get() + "" + event.getSlot())) {
            final GUIItemContainer container = Config.getInstance().getMinecraftHeadsCostumesController().getContainerByPosition(event.getSlot());
            this.setCostumeSkin(petMeta, petBlock, container);
        }
    }

    /**
     * Sets the skin for the given petMeta and petblock of the gui container
     *
     * @param petMeta   petMeta
     * @param petBlock  petBlock
     * @param container container
     */
    private void setCostumeSkin(PetMeta petMeta, PetBlock petBlock, GUIItemContainer container) {
        petMeta.setSkin(container.getItemId(), container.getItemDamage(), container.getSkin(), container.isItemUnbreakable());
        this.persistAsynchronously(petMeta);
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    /**
     * Gets called when the player clicks on the headdatabase icon.
     * It shows a message if headdatabase is not installed or opens the head-database
     *
     * @param player player
     */

    private void handleClickItemHeadDatabaseCostumes(Player player) {
        player.closeInventory();
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            final Plugin plugin = Bukkit.getPluginManager().getPlugin("HeadDatabase");
            if (plugin == null) {
                Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
                    this.headDatabaseMessage.sendMessage(player);
                    player.sendMessage(Config.getInstance().getPrefix() + ChatColor.GRAY + "Please consider that PetBlocks is not responsible for any legal agreements between the author of Head Database and yourself.");
                });
            } else {
                this.manager.headDatabasePlayers.add(player);
                player.performCommand("hdb");
            }
        }, 10L);
    }

    /**
     * Gets called when the player clicks on the my-pet icon.
     * If Only disable Item is enabled, the petblock spawns otherwise the petblock meta gets reset
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void handleClickOnMyPetItem(Player player, PetMeta petMeta) {
        final PetBlock petBlock;
        if ((petBlock = this.getPetBlock(player)) == null && Config.getInstance().isOnlyDisableItemEnabled()) {
            this.setPetBlock(player, petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else {
            if (Config.getInstance().isCopySkinEnabled()) {
                petMeta.setSkin(Material.SKULL_ITEM.getId(), 3, this.getGUIItem("my-pet").getSkin(), this.getGUIItem("my-pet").isItemUnbreakable());
            } else {
                final GUIItemContainer c = this.getGUIItem("default-appearance");
                petMeta.setSkin(c.getItemId(), c.getItemDamage(), c.getSkin(), c.isItemUnbreakable());
            }
            petMeta.setParticleEffectMeta(null);
            if (petBlock != null) {
                petBlock.respawn();
            }
            this.persistAsynchronously(petMeta);
        }
    }

    private void renameName(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > ConfigPet.getInstance().getDesign_maxPetNameLength()) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
        } else {
            try {
                this.namingPlayers.remove(player);
                petMeta.setPetDisplayName(message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingSuccessMessage());
            } catch (final Exception e) {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
            }
        }
    }

    private void renameSkull(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > 20) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
        } else {
            try {
                this.namingSkull.remove(player);
                petMeta.setSkin(Material.SKULL_ITEM.getId(), 3, message, false);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingSuccessMessage());
            } catch (final Exception e) {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
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
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMetaController().getByPlayer(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                            if (meta.getOwner() == null) {
                                ((PetData) petMeta).setSkin(itemStack.getType().getId(), itemStack.getDurability(), NMSRegistry.getSkinUrl(itemStack), false);
                            } else {
                                ((PetData) petMeta).setSkin(itemStack.getType().getId(), itemStack.getDurability(), ((SkullMeta) itemStack.getItemMeta()).getOwner(), false);
                            }
                            this.persistAsynchronously(petMeta);
                            PetBlock petBlock;
                            if ((petBlock = this.getPetBlock(player)) != null) {
                                petBlock.respawn();
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
        final PetBlock petBlock;
        if ((petBlock = this.getPetBlock(event.getPlayer())) != null) {
            if (this.namingSkull.contains(event.getPlayer())) {
                this.renameSkull(event.getPlayer(), event.getMessage(), petBlock.getMeta(), petBlock);
            } else if (this.namingPlayers.contains(event.getPlayer())) {
                this.renameName(event.getPlayer(), event.getMessage(), petBlock.getMeta(), petBlock);
            }
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMetaController().getByPlayer(event.getPlayer());
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

    /**
     * Returns the launch Direction for the cannon
     *
     * @param player player
     * @return launchDirection
     */
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

    /**
     * Refreshes the current GUI page
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void refreshGUI(Player player, PetMeta petMeta) {
        this.manager.gui.setPage(player, this.manager.pages.get(player).page, petMeta);
    }

    /**
     * Sets the petblock for the given player
     *
     * @param player  player
     * @param petMeta petMeta
     * @return petblock
     */
    private PetBlock setPetBlock(Player player, PetMeta petMeta) {
        petMeta.setEnabled(true);
        final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(player, petMeta);
        PetBlocksApi.getDefaultPetBlockController().store(petBlock);
        this.persistAsynchronously(petMeta);
        return petBlock;
    }

    /**
     * Removes the petblock from the given player
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void removePetBlock(Player player, PetMeta petMeta) {
        petMeta.setEnabled(false);
        PetBlocksApi.getDefaultPetBlockController().removeByPlayer(player);
        this.persistAsynchronously(petMeta);
    }

    private boolean hasPermission(Player player, Permission permission) {
        if (!player.hasPermission(permission.get())) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNoPermission());
            return false;
        }
        return true;
    }

    private boolean hasPermission(Player player, String... permissions) {
        for (final String permission : permissions) {
            if (player.hasPermission(permission)) {
                return true;
            }
        }
        player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNoPermission());
        return false;
    }

    private GUIItemContainer getGUIItem(String name) {
        return Config.getInstance().getGuiItemsController().getGUIItemByName(name);
    }

    /**
     * Returns the petblock from the given player
     *
     * @param player player
     * @return petBlock
     */
    private PetBlock getPetBlock(Player player) {
        return PetBlocksApi.getDefaultPetBlockController().getByPlayer(player);
    }

    /**
     * Returns if the given itemStack is the gui Item with the given name
     *
     * @param itemStack itemStack
     * @param name      name
     * @return item
     */
    private boolean isGUIItem(ItemStack itemStack, String name) {
        return Config.getInstance().getGuiItemsController().isGUIItem(itemStack, name);
    }

    /**
     * Persists the current petMeta asynchronly
     *
     * @param petMeta petMeta
     */
    private void persistAsynchronously(PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.getPetMetaController().store(petMeta));
    }
}
