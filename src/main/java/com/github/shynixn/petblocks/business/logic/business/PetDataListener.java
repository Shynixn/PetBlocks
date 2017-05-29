package com.github.shynixn.petblocks.business.logic.business;

import java.util.ArrayList;
import java.util.List;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.lib.BukkitEvents;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.github.shynixn.petblocks.lib.BukkitUtilities;

@SuppressWarnings("deprecation")
class PetDataListener extends BukkitEvents {
    private final PetDataManager manager;
    private final List<Player> namingPlayers = new ArrayList<>();
    private final List<Player> namingSkull = new ArrayList<>();

    PetDataListener(PetDataManager manager, JavaPlugin plugin) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler
    public void playerClickEvent(final InventoryClickEvent event) {
        PetMeta petMeta;
        PetBlock petBlock;
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getTitle().equals(Language.GUI_TITLE) && this.manager.inventories.containsKey(player) && this.manager.inventories.get(player).equals(event.getInventory())) {
            if (this.manager.gui.getPetType(event.getCurrentItem()) != null) {
                final PetType type = this.manager.gui.getPetType(event.getCurrentItem());
                if (player.hasPermission(Permission.ALLPETTYPES.get()) || player.hasPermission(Permission.SINGLEPETTYPE.get() + type.name().toLowerCase())) {
                    if (PetBlocksApi.hasPetBlock(player))
                        PetBlocksApi.removePetBlock(player);
                    petMeta = this.manager.createPetMeta(player, type);
                    if (ConfigGUI.getInstance().isSettings_copyskin()) {
                        petMeta.setSkin(Material.SKULL_ITEM, (short) 3, ConfigGUI.getInstance().getContainer(petMeta.getType()).getSkullName());
                    }
                    this.manager.persist((com.github.shynixn.petblocks.api.persistence.entity.PetMeta)petMeta);
                    this.manager.gui.setMainItems(player, petMeta.getType(), false, true);
                } else {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                }
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.MY_PET) && ((petMeta = this.manager.getPetMeta(player)) != null)) {
                if (ConfigGUI.getInstance().isSettings_copyskin()) {
                    petMeta.setSkin(Material.SKULL_ITEM, (short) 3, ConfigGUI.getInstance().getContainer(petMeta.getType()).getSkullName());
                } else {
                    petMeta.setSkin(Material.getMaterial(ConfigGUI.getInstance().getItems_defaultcostumeContainer().getId()), (short) ConfigGUI.getInstance().getItems_defaultcostumeContainer().getDamage(), ConfigGUI.getInstance().getItems_defaultcostumeContainer().getSkullName());
                }
                petMeta.setParticleEffect(null);
                if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                    petBlock.respawn();
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.ENABLE_PET) && ((petMeta = this.manager.getPetMeta(player)) != null)) {
                PetBlocksApi.setPetBlock(player, petMeta);
                this.manager.gui.setMainItems(player, petMeta.getType(), true, false);
                petMeta.setEnabled(true);
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.DISABLE_PET) && ((petMeta = this.manager.getPetMeta(player)) != null) && PetBlocksApi.hasPetBlock(player)) {
                PetBlocksApi.removePetBlock(player);
                this.manager.gui.setMainItems(player, petMeta.getType(), false, false);
                petMeta.setEnabled(false);
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.MUTE) && ((petMeta = this.manager.getPetMeta(player)) != null)) {
                PetBlocksApi.getPetMeta(player).setSoundsEnabled(false);
                this.manager.gui.setMainItems(player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), false);
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.UNMUTE) && ((petMeta = this.manager.getPetMeta(player)) != null)) {
                PetBlocksApi.getPetMeta(player).setSoundsEnabled(true);
                this.manager.gui.setMainItems(player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), false);
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
                this.manager.gui.setParticleCostumeItems(player);
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CALL) && ((petBlock = PetBlocksApi.getPetBlock(player)) != null)) {
                petBlock.teleport(player);
                player.closeInventory();
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.HAT) && ((petBlock = PetBlocksApi.getPetBlock(player)) != null)) {
                if (player.hasPermission(Permission.WEARPET.get())) {
                    petBlock.wear(player);
                } else {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                }
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.RIDING) && ((petBlock = PetBlocksApi.getPetBlock(player)) != null)) {
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
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CANNON) && ((petBlock = PetBlocksApi.getPetBlock(player)) != null)) {
                if (player.hasPermission(Permission.CANNON.get())) {
                    player.closeInventory();
                    petBlock.launch(this.getDirection(player));
                } else {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                }
            } else if (BukkitUtilities.compareItemName(event.getCurrentItem(), Language.CANCEL)) {
                player.closeInventory();
            } else if (this.manager.pages.get(player).page == GUI.GuiPage.PARTICLES && ((petMeta = this.manager.getPetMeta(player)) != null)) {
                if (!player.hasPermission(Permission.ALLPARTICLES.get()) && !player.hasPermission(Permission.SINGLEPARTICLE.get() + "" + event.getSlot())) {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                } else {
                    petMeta.setParticleEffect(ConfigParticle.getInstance().getParticle(event.getSlot()));
                }
                player.closeInventory();
                this.manager.gui.open(player);
                this.manager.gui.setMainItems(player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), false);
                if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                    petBlock.respawn();
            } else if (!BukkitUtilities.compareItemName(event.getCurrentItem(), Language.EMPTY) && (this.manager.pages.get(player).page == GUI.GuiPage.DEFAULTCOSTUMES || this.manager.pages.get(player).page == GUI.GuiPage.CUSTOMCOSTUMES || this.manager.pages.get(player).page == GUI.GuiPage.COLORCOSTUMES) && event.getCurrentItem() != null) {
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
                if (this.manager.pages.get(player).page == GUI.GuiPage.DEFAULTCOSTUMES && (!player.hasPermission(Permission.ALLDEFAULTCOSTUMES.get()) && !player.hasPermission(Permission.SINGLEDEFAULTCOSTUME.get() + "" + number))) {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                } else if (this.manager.pages.get(player).page == GUI.GuiPage.COLORCOSTUMES && (!player.hasPermission(Permission.ALLCOLORCOSTUMES.get()) && !player.hasPermission(Permission.SINGLECOLORCOSTUME.get() + "" + number))) {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                } else if (this.manager.pages.get(player).page == GUI.GuiPage.CUSTOMCOSTUMES && (!player.hasPermission(Permission.ALLCUSTOMCOSTUMES.get()) && !player.hasPermission(Permission.SINGLECUSTOMCOSTUME.get() + "" + number))) {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                } else if (event.getClickedInventory().equals(event.getView().getTopInventory()) || player.hasPermission(Permission.OWNINGAMECOSTUMES.get())) {
                    petMeta = this.manager.getPetMeta(player);
                    if (petMeta == null)
                        return;
                    if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
                        petMeta.setSkin(event.getCurrentItem().getType(), event.getCurrentItem().getDurability(), null);
                    else {
                        final SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
                        if (meta.getOwner() == null) {
                            petMeta.setSkin(event.getCurrentItem().getType(), event.getCurrentItem().getDurability(), NMSRegistry.getSkinUrl(event.getCurrentItem()));
                        } else {
                            petMeta.setSkin(event.getCurrentItem().getType(), event.getCurrentItem().getDurability(), ((SkullMeta) event.getCurrentItem().getItemMeta()).getOwner());
                        }
                    }
                    petMeta.setUnbreakable(NMSRegistry.isUnbreakable(event.getCurrentItem()));
                    player.closeInventory();
                    this.manager.gui.open(player);
                    this.manager.gui.setMainItems(player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), false);
                    if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                        petBlock.respawn();
                } else {
                    player.sendMessage(Language.PREFIX + Language.NO_PERMISSION);
                }
            } else if (ConfigGUI.getInstance().isSettings_clickemptyback() && BukkitUtilities.compareItemName(event.getCurrentItem(), Language.EMPTY)) {
                petMeta = PetBlocksApi.getPetMeta(player);
                if (this.manager.pages.containsKey(player) && this.manager.pages.get(player).page != GUI.GuiPage.MAIN) {
                    this.manager.gui.setMainItems(player);
                    if (petMeta != null)
                        this.manager.gui.setMainItems(player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), true);
                }
            }
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
        }
    }

    @EventHandler
    public void playerJoinEvent(final PlayerJoinEvent event) {
        final PetMeta petMeta;
        if (Config.getInstance().isJoin_enabled()) {
            if (!this.manager.hasPetMeta(event.getPlayer()) || Config.getInstance().isJoin_overwriteExistingPet()) {
                final PetMeta meta = this.manager.createPetMeta(event.getPlayer(), PetType.CAT);
                Config.getInstance().fixJoinDefaultPet(meta);
                this.manager.persist((com.github.shynixn.petblocks.api.persistence.entity.PetMeta)meta);
            }
        } else {
            this.manager.hasPetMeta(event.getPlayer());
        }
        if ((petMeta = PetBlocksApi.getPetMeta(event.getPlayer())) != null && petMeta.isEnabled()) {
            this.getPlugin().getServer().getScheduler().runTaskLater(this.getPlugin(), () -> {
                PetBlocksApi.removePetBlock(event.getPlayer());
                PetBlocksApi.setPetBlock(event.getPlayer(), PetDataListener.this.manager.getPetMeta(event.getPlayer()));
            }, 2L);
        }
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (this.manager.hasPetMeta(event.getPlayer())) {
            this.manager.persist(this.manager.getPetMeta(event.getPlayer()));
            PetBlocksApi.removePetBlock(event.getPlayer());
        }
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
        if (!Config.getInstance().isChat_async() && Config.getInstance().isChat_highestpriority()) {
            this.handleChatMessage(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerChatEvent2(PlayerChatEvent event) {
        if (!Config.getInstance().isChat_async() && !Config.getInstance().isChat_highestpriority()) {
            this.handleChatMessage(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerChatEvent3(final AsyncPlayerChatEvent event) {
        if (Config.getInstance().isChat_async() && Config.getInstance().isChat_highestpriority()) {
            if (this.namingPlayers.contains(event.getPlayer()) || this.namingSkull.contains(event.getPlayer()))
                event.setCancelled(true);
            this.getPlugin().getServer().getScheduler().runTaskLater(this.getPlugin(), () -> PetDataListener.this.handleChatMessage(new PlayerChatEvent(event.getPlayer(), event.getMessage())), 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerChatEvent4(final AsyncPlayerChatEvent event) {
        if (Config.getInstance().isChat_async() && !Config.getInstance().isChat_highestpriority()) {
            if (this.namingPlayers.contains(event.getPlayer()) || this.namingSkull.contains(event.getPlayer()))
                event.setCancelled(true);
            this.getPlugin().getServer().getScheduler().runTaskLater(this.getPlugin(), () -> PetDataListener.this.handleChatMessage(new PlayerChatEvent(event.getPlayer(), event.getMessage())), 1L);
        }
    }

    private void handleChatMessage(PlayerChatEvent event) {
        if (this.namingPlayers.contains(event.getPlayer())) {
            event.setCancelled(true);
            if (event.getMessage().length() > ConfigPet.getInstance().getDesign_maxPetNameLength())
                event.getPlayer().sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
            else {
                try {
                    this.namingPlayers.remove(event.getPlayer());
                    final PetMeta petMeta;
                    final PetBlock petBlock;
                    if ((petMeta = this.manager.getPetMeta(event.getPlayer())) != null) {
                        petMeta.setDisplayName(event.getMessage());
                    }
                    if ((petBlock = PetBlocksApi.getPetBlock(event.getPlayer())) != null)
                        petBlock.respawn();
                    event.getPlayer().sendMessage(Language.PREFIX + Language.NAME_SUCCES_MESSAGE);
                } catch (final Exception e) {
                    event.getPlayer().sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
                }
            }
        } else if (this.namingSkull.contains(event.getPlayer())) {
            event.setCancelled(true);
            if (event.getMessage().length() > 20)
                event.getPlayer().sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
            else {
                try {
                    this.namingSkull.remove(event.getPlayer());
                    final PetMeta petMeta;
                    final PetBlock petBlock;
                    if ((petMeta = this.manager.getPetMeta(event.getPlayer())) != null) {
                        petMeta.setSkin(Material.SKULL_ITEM, (short) 3, event.getMessage());
                    }
                    if ((petBlock = PetBlocksApi.getPetBlock(event.getPlayer())) != null)
                        petBlock.respawn();
                    event.getPlayer().sendMessage(Language.PREFIX + Language.SNAME_SUCCES_MESSAGE);
                } catch (final Exception e) {
                    event.getPlayer().sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
                }
            }
        }
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
