package com.github.shynixn.petblocks.bukkit.logic.business.listener;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.ChatBuilderExtensionKt;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.SkinHelper;
import com.github.shynixn.petblocks.bukkit.nms.v1_12_R1.MaterialCompatibility12;
import com.github.shynixn.petblocks.core.logic.business.helper.ChatBuilder;
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Listens to events for configuring petblocks and UI.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class PetDataListener extends SimpleListener {
    private final PetBlockManager manager;
    private final Set<Player> spamProtection = new HashSet<>();

    private String headDatabaseTitle;
    private String headDatabaseSearch;
    private final ChatBuilder suggestHeadMessage = new ChatBuilder().text(Config.getInstance().getPrefix())
            .text("Click here: ")
            .component(">>Submit skin<<")
            .setColor(com.github.shynixn.petblocks.core.logic.business.helper.ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com/custom/heads-generator")
            .setHoverText("Goto the Minecraft-Heads website!")
            .builder()
            .text(" ")
            .component(">>Suggest new pet<<")
            .setColor(com.github.shynixn.petblocks.core.logic.business.helper.ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com/forum/suggesthead")
            .setHoverText("Goto the Minecraft-Heads website!")
            .builder();
    private final ChatBuilder headDatabaseMessage = new ChatBuilder().text(Config.getInstance().getPrefix())
            .text("Download the plugin ")
            .component(">>Head Database<<")
            .setColor(com.github.shynixn.petblocks.core.logic.business.helper.ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "https://www.spigotmc.org/resources/14280/")
            .setHoverText("A valid spigot account is required!")
            .builder();
    private final ChatBuilder collectedMinecraftHeads = new ChatBuilder().text(Config.getInstance().getPrefix())
            .text("Pets collected by ")
            .component(">>Minecraft-Heads.com<<")
            .setColor(com.github.shynixn.petblocks.core.logic.business.helper.ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com")
            .setHoverText("Goto the Minecraft-Heads website!")
            .builder();

    /**
     * Initializes a new PetDataListener.
     *
     * @param manager manager
     * @param plugin  plugin
     */
    public PetDataListener(PetBlockManager manager, Plugin plugin) {
        super(plugin);
        this.manager = manager;
    }

    /**
     * Removes the petblock from the player when he leaves the server.
     *
     * @param event playerQuitEvent
     */
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (this.manager.headDatabasePlayers.contains(event.getPlayer())) {
            this.manager.headDatabasePlayers.remove(event.getPlayer());
        }
        if (this.spamProtection.contains(event.getPlayer())) {
            this.spamProtection.remove(event.getPlayer());
        }
        this.manager.getPetBlockController().removeByPlayer(event.getPlayer());
    }

    /**
     * Handles clicking inside of the GUI.
     *
     * @param event event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerClickEvent(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getTitle().equals(Config.getInstance().getGUITitle())
                && this.manager.inventories.containsKey(player)
                && this.manager.inventories.get(player).equals(event.getInventory())) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
            final Optional<PetBlock> optPetblock;
            if ((optPetblock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
                this.handleClick(event, player, optPetblock.get().getMeta(), optPetblock.get());
            } else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final Optional<PetMeta> optPetMeta = this.manager.getPetMetaController().getFromPlayer(player);
                    optPetMeta.ifPresent(petMeta -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.handleClick(event, player, petMeta, null)));
                });
            }
        } else if (this.manager.headDatabasePlayers.contains(player)) {
            if (this.headDatabaseTitle == null) {
                final Plugin plugin = Bukkit.getPluginManager().getPlugin("HeadDatabase");
                this.headDatabaseTitle = this.constructPrefix(ChatColor.translateAlternateColorCodes('&', "&4&r" + plugin.getConfig().getString("messages.database").split("%count%")[0]));
                this.headDatabaseSearch = this.constructPrefix(ChatColor.translateAlternateColorCodes('&', "&4&r" + plugin.getConfig().getString("messages.search").split("%count%")[0]));
            }
            final String currentTitle = ChatColor.stripColor(event.getView().getTopInventory().getTitle());
            if (!currentTitle.startsWith(this.headDatabaseTitle) && !currentTitle.startsWith(this.headDatabaseSearch))
                return;
            event.setCancelled(true);
            this.linkHeadDatabaseItemToPetBlocks(event.getCurrentItem(), player);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                for (final ItemStack itemStack : event.getWhoClicked().getInventory().getContents()) {
                    if (itemStack != null
                            && itemStack.getItemMeta() != null
                            && itemStack.getItemMeta().getDisplayName() != null) {
                        if (itemStack.getItemMeta().getDisplayName().equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
                            final Player player1 = (Player) event.getWhoClicked();
                            player1.getInventory().remove(itemStack);
                            player1.updateInventory();
                            return;
                        }
                    }
                }
            }, 5L);
        }
    }

    /**
     * Gets called when a player joins a server. Overrides existing pets if enabled in the config.yml and
     * spawns the petblock of the player when his pet was enabled when he left the server the last time.
     *
     * @param event event
     */
    @EventHandler
    public void playerJoinEvent(final PlayerJoinEvent event) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final Optional<PetMeta> petMetaOpt;
            if (Config.getInstance().isJoin_enabled()) {
                if (!this.manager.getPetMetaController().getFromPlayer(event.getPlayer()).isPresent() || Config.getInstance().isJoin_overwriteExistingPet()) {
                    if (event.getPlayer().getWorld() != null) {
                        final PetMeta meta = this.manager.getPetMetaController().create(event.getPlayer());
                        meta.getPlayerMeta().setName(event.getPlayer().getName());
                        Config.getInstance().fixJoinDefaultPet(meta);
                        this.manager.getPetMetaController().store(meta);
                    }
                }
            }
            if ((petMetaOpt = this.manager.getPetMetaController().getFromPlayer(event.getPlayer())).isPresent()) {
                if (petMetaOpt.get().isEnabled()) {
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        if (event.getPlayer().getWorld() != null) {
                            final PetBlock petBlock = this.manager.getPetBlockController().create(event.getPlayer(), petMetaOpt.get());
                            this.manager.getPetBlockController().store(petBlock);
                        }
                    }, 2L);
                }
            }
        });
    }

    /**
     * Removes the handler from the cache on inventory close.
     *
     * @param event event
     */
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        if (this.manager.inventories.containsKey(player)) {
            this.manager.inventories.remove(player);
        }
    }

    private void handleClick(InventoryClickEvent event, Player player, PetMeta petMeta, PetBlock petBlock) {
        final ItemStack currentItem = event.getCurrentItem();
        final int itemSlot = event.getSlot() + this.manager.pages.get(player).currentCount + 1;
        if (this.manager.pages.get(player).page == GUIPage.MAIN && this.getGUIItem("my-pet").getPosition() == event.getSlot()) {
            this.handleClickOnMyPetItem(player, petMeta);
        } else if (this.isGUIItem(currentItem, "empty-slot")) {
            return;
        } else if (this.isGUIItem(currentItem, "enable-pet")) {
            if (!this.spamProtection.contains(player)) {
                this.setPetBlock(player, petMeta);
                this.refreshGUI(player, petMeta);
            }
            this.handleSpamProtection(player);
        } else if (this.isGUIItem(currentItem, "disable-pet")) {
            if (!this.spamProtection.contains(player)) {
                this.removePetBlock(player, petMeta);
                this.refreshGUI(player, petMeta);
            }
            this.handleSpamProtection(player);
        } else if (this.isGUIItem(currentItem, "sounds-enabled-pet")) {
            petMeta.setSoundEnabled(false);
            this.refreshGUI(player, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (this.isGUIItem(currentItem, "sounds-disabled-pet")) {
            petMeta.setSoundEnabled(true);
            this.refreshGUI(player, petMeta);
            this.persistAsynchronously(petMeta);
        } else if (this.isGUIItem(currentItem, "next-page")) {
            this.manager.gui.moveList(player, true);
        } else if (this.isGUIItem(currentItem, "previous-page")) {
            this.manager.gui.moveList(player, false);
        } else if (this.isGUIItem(currentItem, "ordinary-costume")) {
            this.manager.gui.setPage(player, GUIPage.DEFAULT_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "color-costume")) {
            this.manager.gui.setPage(player, GUIPage.COLOR_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "rare-costume")) {
            this.manager.gui.setPage(player, GUIPage.CUSTOM_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "particle-pet")) {
            this.manager.gui.setPage(player, GUIPage.PARTICLES, petMeta);
        } else if (this.isGUIItem(currentItem, "wardrobe")) {
            this.manager.gui.setPage(player, GUIPage.WARDROBE, petMeta);
        } else if (this.isGUIItem(currentItem, "engine-settings")) {
            this.manager.gui.setPage(player, GUIPage.ENGINES, petMeta);
        } else if (this.isGUIItem(currentItem, "call-pet") && petBlock != null) {
            petBlock.teleport(player.getLocation());
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "hat-pet") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_WEAR) && petBlock != null) {
            petBlock.wear(player);
        } else if (this.isGUIItem(currentItem, "riding-pet") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_RIDE) && petBlock != null) {
            petBlock.ride(player);
        } else if (this.isGUIItem(currentItem, "suggest-heads")) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> ChatBuilderExtensionKt.sendMessage(this.suggestHeadMessage, player));
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "head-database-costume")) {
            if (PetBlockModifyHelper.hasPermission(player, Permission.ALL_HEADDATABASECOSTUMES)) {
                this.handleClickItemHeadDatabaseCostumes(player);
            } else {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNoPermission());
            }
        } else if (this.isGUIItem(currentItem, "naming-pet") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_RENAME)) {
            ChatBuilderExtensionKt.sendMessage(((ChatBuilder) Config.getInstance().getPetNamingMessage()), player);
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "skullnaming-pet") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_CUSTOMSKULL)) {
            ChatBuilderExtensionKt.sendMessage(((ChatBuilder) Config.getInstance().getPetSkinNamingMessage()), player);
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "cannon-pet") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_CANNON) && petBlock != null) {
            petBlock.setVelocity(this.getDirection(player));
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "back")) {
            this.manager.gui.backPage(player, petMeta);
        } else if (this.manager.pages.get(player).page == GUIPage.ENGINES && this.hasPermission(player, Permission.ALL_ENGINES, Permission.SINGLE_ENGINE, itemSlot)) {
            final Optional<EngineContainer<GUIItemContainer<Player>>> optEngineContainer = Config.<Player>getInstance().getEngineController().getContainerFromPosition(itemSlot);
            if (!optEngineContainer.isPresent()) {
                throw new IllegalArgumentException("Engine " + itemSlot + " could not be loaded correctly!");
            }
            PetBlockModifyHelper.setEngine(petMeta, petBlock, optEngineContainer.get());
            this.persistAsynchronously(petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else if (this.manager.pages.get(player).page == GUIPage.PARTICLES && this.hasPermission(player, Permission.ALL_PARTICLES, Permission.SINGLE_PARTICLE, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getParticleController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Particle " + itemSlot + " could not be loaded correctly.");
            PetBlockModifyHelper.setParticleEffect(petMeta, petBlock, container.get());
            this.persistAsynchronously(petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.DEFAULT_COSTUMES && this.hasPermission(player, Permission.ALL_SIMPLEBLOCKCOSTUMES, Permission.SINGLE_SIMPLEBLOCKCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getOrdinaryCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.COLOR_COSTUMES && this.hasPermission(player, Permission.ALL_COLOREDBLOCKCOSTUMES, Permission.SINGLE_COLOREDBLOCKCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getColorCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.CUSTOM_COSTUMES && this.hasPermission(player, Permission.ALL_PLAYERHEADCOSTUMES, Permission.SINGLE_PLAYERHEADCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getRareCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        }
    }

    /**
     * Sets the skin for the given petMeta and petblock of the gui container.
     *
     * @param petMeta   petMeta
     * @param petBlock  petBlock
     * @param container container
     */
    private void setCostumeSkin(Player player, PetMeta petMeta, PetBlock petBlock, GUIItemContainer container) {
        PetBlockModifyHelper.setCostume(petMeta, petBlock, container);
        this.persistAsynchronously(petMeta);
        this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
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
                    ChatBuilderExtensionKt.sendMessage(this.headDatabaseMessage, player);
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
     * If Only disable Item is enabled, the petblock spawns otherwise the petblock meta gets reset.
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void handleClickOnMyPetItem(Player player, PetMeta petMeta) {
        final Optional<PetBlock> optPetBlock;
        if (!(optPetBlock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent() && Config.getInstance().isOnlyDisableItemEnabled()) {
            this.setPetBlock(player, petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else {
            if (Config.getInstance().isCopySkinEnabled()) {
                petMeta.setSkin(MaterialCompatibility12.getIdFromMaterial(Material.SKULL_ITEM), 3, this.getGUIItem("my-pet").getSkin(), this.getGUIItem("my-pet").isItemUnbreakable());
            } else {
                final GUIItemContainer c = this.getGUIItem("default-appearance");
                petMeta.setSkin(c.getItemId(), c.getItemDamage(), c.getSkin(), c.isItemUnbreakable());
            }
            petMeta.getParticleEffectMeta().setEffectType(ParticleEffectMeta.ParticleEffectType.NONE);
            optPetBlock.ifPresent(PetBlock::respawn);
            this.persistAsynchronously(petMeta);
        }
    }

    /**
     * Sets the given itemStack as petMeta skin if it full fills the skull requirements.
     *
     * @param itemStack itemStack
     * @param player    player
     */
    private void linkHeadDatabaseItemToPetBlocks(ItemStack itemStack, Player player) {
        if (itemStack != null
                && itemStack.getType() == Material.SKULL_ITEM
                && itemStack.getItemMeta() != null
                && itemStack.getItemMeta().getDisplayName() != null
                && itemStack.getItemMeta().getDisplayName().startsWith(ChatColor.BLUE.toString())) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final Optional<PetMeta> petMeta = this.manager.getPetMetaController().getFromPlayer(player);
                if (!petMeta.isPresent())
                    return;
                final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                if (meta.getOwner() == null) {
                    petMeta.get().setSkin(MaterialCompatibility12.getIdFromMaterial(itemStack.getType()), itemStack.getDurability(), SkinHelper.getItemStackSkin(itemStack).get(), false);
                } else {
                    petMeta.get().setSkin(MaterialCompatibility12.getIdFromMaterial(itemStack.getType()), itemStack.getDurability(), ((SkullMeta) itemStack.getItemMeta()).getOwner(), false);
                }
                this.manager.getPetMetaController().store(petMeta.get());
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            final Optional<PetBlock> customPetBlock;
                            if ((customPetBlock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
                                customPetBlock.get().respawn();
                            }
                            player.performCommand("petblock");
                        }
                );
            });
        }
    }

    /**
     * Handles spamming protection.
     *
     * @param player player
     */
    private void handleSpamProtection(Player player) {
        if (!this.spamProtection.contains(player)) {
            this.spamProtection.add(player);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.spamProtection.remove(player), 30L);
        }
    }

    /**
     * Constructs a prefix from the given source.
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
                return ChatColor.stripColor(b.toString());
            }
        }
        return ChatColor.stripColor(b.toString());
    }

    /**
     * Returns the launch Direction for the cannon.
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
     * Sets the petblock for the given player.
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void setPetBlock(Player player, PetMeta petMeta) {
        petMeta.setEnabled(true);
        final PetBlock petBlock = this.manager.getPetBlockController().create(player, petMeta);
        this.manager.getPetBlockController().store(petBlock);
        this.persistAsynchronously(petMeta);
    }

    /**
     * Removes the petblock from the given player.
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void removePetBlock(Player player, PetMeta petMeta) {
        petMeta.setEnabled(false);
        this.manager.getPetBlockController().removeByPlayer(player);
        this.persistAsynchronously(petMeta);
    }

    private boolean hasPermission(Player player, Permission groupPermission, Permission singlePermission, int slot) {
        if (!PetBlockModifyHelper.hasPermission(player, groupPermission)) {
            if (!PetBlockModifyHelper.hasPermission(player, singlePermission, String.valueOf(slot))) {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNoPermission());
                return false;
            }
        }
        return true;
    }

    private GUIItemContainer<Player> getGUIItem(String name) {
        final Optional<GUIItemContainer<Player>> guiItemContainer = Config.<Player>getInstance().getGuiItemsController().getGUIItemFromName(name);
        if (!guiItemContainer.isPresent())
            throw new IllegalArgumentException("Guiitem " + name + " could not be loaded correctly!");
        return guiItemContainer.get();
    }

    /**
     * Returns if the given itemStack is the gui Item with the given name.
     *
     * @param itemStack itemStack
     * @param name      name
     * @return item
     */
    private boolean isGUIItem(ItemStack itemStack, String name) {
        return Config.getInstance().getGuiItemsController().isGUIItem(itemStack, name);
    }

    /**
     * Persists the current petMeta asynchronly.
     *
     * @param petMeta petMeta
     */
    private void persistAsynchronously(PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.getPetMetaController().store(petMeta));
    }
}
