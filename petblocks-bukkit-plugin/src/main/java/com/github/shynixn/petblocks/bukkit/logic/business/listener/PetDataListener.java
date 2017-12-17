package com.github.shynixn.petblocks.bukkit.logic.business.listener;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.lib.ChatBuilder;
import com.github.shynixn.petblocks.bukkit.lib.SimpleListener;
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.SkinHelper;
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
    private final Set<Player> namingPlayers = new HashSet<>();
    private final Set<Player> namingSkull = new HashSet<>();
    private final Set<Player> spamProtection = new HashSet<>();

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
    private final ChatBuilder collectedMinecraftHeads = new ChatBuilder().text(Config.getInstance().getPrefix())
            .text("Pets collected by ")
            .component(">>Minecraft-Heads.com<<")
            .setColor(ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com")
            .setHoverText("Goto the Minecraft-Heads website!")
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
        if (this.spamProtection.contains(event.getPlayer())) {
            this.spamProtection.remove(event.getPlayer());
        }
        PetBlocksApi.getDefaultPetBlockController().removeByPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerClickEvent(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getTitle().equals(Config.getInstance().getGUITitle())
                && this.manager.inventories.containsKey(player)
                && this.manager.inventories.get(player).equals(event.getInventory())) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
            final Optional<PetBlock> optPetblock;
            if ((optPetblock = PetBlocksApi.getDefaultPetBlockController().getFromPlayer(player)).isPresent()) {
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
                this.headDatabaseTitle = this.constructPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.database")));
                this.headDatabaseSearch = this.constructPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.search")));
            }
            final String currentTitle = ChatColor.stripColor(event.getView().getTopInventory().getTitle());
            if (!currentTitle.startsWith(this.headDatabaseTitle) && !currentTitle.startsWith(this.headDatabaseSearch))
                return;
            event.setCancelled(true);
            this.linkHeadDatabaseItemToPetBlocks(event.getCurrentItem(), player);
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
            final PetMeta petMeta;
            if (Config.getInstance().isJoin_enabled()) {
                if (!this.manager.getPetMetaController().getFromPlayer(event.getPlayer()).isPresent() || Config.getInstance().isJoin_overwriteExistingPet()) {
                    if (event.getPlayer().getWorld() != null) {
                        final PetMeta meta = this.manager.getPetMetaController().create(event.getPlayer());
                        Config.getInstance().fixJoinDefaultPet(meta);
                        this.manager.getPetMetaController().store(meta);
                    }
                }
            }

            if ((petMeta = PetBlocksApi.getDefaultPetMetaController().getByPlayer(event.getPlayer())) != null) {
                if (petMeta.isEnabled()) {
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        if (event.getPlayer().getWorld() != null) {
                            final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(event.getPlayer(), petMeta);
                            PetBlocksApi.getDefaultPetBlockController().store(petBlock);
                        }
                    }, 2L);
                }
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
        final ItemStack currentItem = event.getCurrentItem();
        final int itemSlot = event.getSlot() + this.manager.pages.get(player).currentCount + 1;
        if (this.manager.pages.get(player).page == GUIPage.MAIN && this.getGUIItem("my-pet").getPosition() == event.getSlot()) {
            this.handleClickOnMyPetItem(player, petMeta);
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
        } else if (this.isGUIItem(currentItem, "minecraft-heads-costume")) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> this.collectedMinecraftHeads.sendMessage(player));
            this.manager.gui.setPage(player, GUIPage.MINECRAFTHEADS_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "particle-pet")) {
            this.manager.gui.setPage(player, GUIPage.PARTICLES, petMeta);
        } else if (this.isGUIItem(currentItem, "wardrobe")) {
            this.manager.gui.setPage(player, GUIPage.WARDROBE, petMeta);
        } else if (this.isGUIItem(currentItem, "engine-settings")) {
            this.manager.gui.setPage(player, GUIPage.ENGINES, petMeta);
        } else if (this.isGUIItem(currentItem, "call-pet") && petBlock != null) {
            petBlock.teleport(player.getLocation());
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "hat-pet") && this.hasPermission(player, Permission.WEARPET) && petBlock != null) {
            petBlock.wear(player);
        } else if (this.isGUIItem(currentItem, "riding-pet") && this.hasPermission(player, Permission.RIDEPET) && petBlock != null) {
            petBlock.ride(player);
        } else if (this.isGUIItem(currentItem, "suggest-heads")) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> this.suggestHeadMessage.sendMessage(player));
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
        } else if (this.isGUIItem(currentItem, "cannon-pet") && this.hasPermission(player, Permission.CANNON) && petBlock != null) {
            petBlock.setVelocity(this.getDirection(player));
            player.closeInventory();
        } else if (this.isGUIItem(currentItem, "back")) {
            this.manager.gui.backPage(player, petMeta);
        } else if (this.manager.pages.get(player).page == GUIPage.ENGINES && this.hasPermission(player, Permission.ALLPETTYPES.get(), Permission.SINGLEPETTYPE.get() + "" + itemSlot)) {
            final EngineContainer engineContainer = Config.getInstance().getEngineController().getById(itemSlot);
            PetBlockModifyHelper.setEngine(petMeta, petBlock, engineContainer);
            this.persistAsynchronously(petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else if (this.manager.pages.get(player).page == GUIPage.PARTICLES && this.hasPermission(player, Permission.ALLPARTICLES.get(), Permission.SINGLEPARTICLE.get() + "" + itemSlot)) {
            final GUIItemContainer container = Config.getInstance().getParticleController().getContainerByPosition(itemSlot);
            PetBlockModifyHelper.setParticleEffect(petMeta, petBlock, container);
            this.persistAsynchronously(petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.DEFAULT_COSTUMES && this.hasPermission(player, Permission.ALLDEFAULTCOSTUMES.get(), Permission.SINGLEDEFAULTCOSTUME.get() + "" + itemSlot)) {
            final GUIItemContainer container = Config.getInstance().getOrdinaryCostumesController().getContainerByPosition(itemSlot);
            this.setCostumeSkin(player, petMeta, petBlock, container);
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.COLOR_COSTUMES && this.hasPermission(player, Permission.ALLCOLORCOSTUMES.get(), Permission.SINGLECOLORCOSTUME.get() + "" + itemSlot)) {
            final GUIItemContainer container = Config.getInstance().getColorCostumesController().getContainerByPosition(itemSlot);
            this.setCostumeSkin(player, petMeta, petBlock, container);
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.CUSTOM_COSTUMES && this.hasPermission(player, Permission.ALLCUSTOMCOSTUMES.get(), Permission.SINGLECUSTOMCOSTUME.get() + "" + itemSlot)) {
            final GUIItemContainer container = Config.getInstance().getRareCostumesController().getContainerByPosition(itemSlot);
            this.setCostumeSkin(player, petMeta, petBlock, container);
        } else if (event.getSlot() < 45 && this.manager.pages.get(player).page == GUIPage.MINECRAFTHEADS_COSTUMES && this.hasPermission(player, Permission.ALLHEADATABASECOSTUMES.get(), Permission.SINGLEMINECRAFTHEADSCOSTUME.get() + "" + itemSlot)) {
            final GUIItemContainer container = Config.getInstance().getMinecraftHeadsCostumesController().getContainerByPosition(itemSlot);
            this.setCostumeSkin(player, petMeta, petBlock, container);
        }
    }

    /**
     * Sets the skin for the given petMeta and petblock of the gui container
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
            petMeta.getParticleEffectMeta().setEffectType(ParticleEffectMeta.ParticleEffectType.NONE);
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
     */
    private void linkHeadDatabaseItemToPetBlocks(ItemStack itemStack, Player player) {
        if (itemStack != null
                && itemStack.getType() == Material.SKULL_ITEM
                && itemStack.getItemMeta() != null
                && itemStack.getItemMeta().getDisplayName() != null
                && itemStack.getItemMeta().getDisplayName().startsWith(ChatColor.BLUE.toString())) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMetaController().getByPlayer(player);
                final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                if (meta.getOwner() == null) {
                    petMeta.setSkin(itemStack.getType().getId(), itemStack.getDurability(), SkinHelper.getItemStackSkin(itemStack).get(), false);
                } else {
                    petMeta.setSkin(itemStack.getType().getId(), itemStack.getDurability(), ((SkullMeta) itemStack.getItemMeta()).getOwner(), false);
                }
                this.manager.getPetMetaController().store(petMeta);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            final PetBlock petBlock;
                            if ((petBlock = this.getPetBlock(player)) != null) {
                                petBlock.respawn();
                            }
                            player.performCommand("petblock");
                        }
                );
            });
        }
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
                return ChatColor.stripColor(b.toString());
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
     */
    private void setPetBlock(Player player, PetMeta petMeta) {
        petMeta.setEnabled(true);
        final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(player, petMeta);
        PetBlocksApi.getDefaultPetBlockController().store(petBlock);
        this.persistAsynchronously(petMeta);
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
