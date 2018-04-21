package com.github.shynixn.petblocks.sponge.logic.business.listener;

import com.flowpowered.math.vector.Vector3d;
import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.core.logic.business.helper.ChatBuilder;
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager;
import com.github.shynixn.petblocks.sponge.logic.business.helper.CompatibilityItemType;
import com.github.shynixn.petblocks.sponge.logic.business.helper.ExtensionMethodsKt;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.google.inject.Inject;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

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
public class SpongePetDataListener extends SimpleSpongeListener {
    private final PetBlocksManager manager;
    private final Set<Player> spamProtection = new HashSet<>();

    private final ChatBuilder suggestHeadMessage = new ChatBuilder().text(com.github.shynixn.petblocks.core.logic.persistence.configuration.Config.getInstance().getPrefix())
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
    private final ChatBuilder collectedMinecraftHeads = new ChatBuilder().text(com.github.shynixn.petblocks.core.logic.persistence.configuration.Config.getInstance().getPrefix())
            .text("Pets collected by ")
            .component(">>Minecraft-Heads.com<<")
            .setColor(com.github.shynixn.petblocks.core.logic.business.helper.ChatColor.YELLOW)
            .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com")
            .setHoverText("Goto the Minecraft-Heads website!")
            .builder();

    /**
     * Initializes a new PetDataListener
     *
     * @param manager manager
     * @param plugin  plugin
     */
    @Inject
    public SpongePetDataListener(PetBlocksManager manager, PluginContainer plugin) {
        super(plugin);
        this.manager = manager;
    }

    /**
     * Removes the petblock from the player when he leaves the server
     *
     * @param event playerQuitEvent
     */
    @Listener
    public void playerQuitEvent(ClientConnectionEvent.Disconnect event) {
        final Player player = event.getTargetEntity();
        if (this.spamProtection.contains(player)) {
            this.spamProtection.remove(player);
        }
        this.manager.getPetBlockController().removeByPlayer(player);
    }

    @Listener
    public void playerClickEvent(final ClickInventoryEvent.Primary event, @First(typeFilter = Player.class) Player player) {
        if (event.getTargetInventory().getName().get().equals(Config.INSTANCE.getGUITitle())
                && this.manager.getInventories().containsKey(player)) {
            event.setCancelled(true);
            ExtensionMethodsKt.updateInventory(player);
            final Optional<PetBlock> optPetblock;
            final ItemStack itemStack = event.getTransactions().get(0).getOriginal().createStack();
            final int newSlot = event.getTransactions().get(0).getSlot().getProperties(SlotIndex.class).toArray(new SlotIndex[0])[0].getValue();
            if ((optPetblock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
                Task.builder().execute(() -> this.handleClick(itemStack, newSlot, player, optPetblock.get().getMeta(), optPetblock.get())).submit(SpongePetDataListener.this.plugin);
            } else {
                Task.builder().async().execute(() -> {
                    final Optional<PetMeta> optPetMeta = SpongePetDataListener.this.manager.getPetMetaController().getFromPlayer(player);
                    Task.builder().execute(() -> optPetMeta.ifPresent(petMeta -> SpongePetDataListener.this.handleClick(itemStack, newSlot, player, petMeta, null))).submit(SpongePetDataListener.this.plugin);
                }).submit(this.plugin);
            }

        }
    }

    /**
     * Gets called when a player joins a server. Overrides existing pets if enabled in the config.yml and
     * spawns the petblock of the player when his pet was enabled when he left the server the last time.
     *
     * @param event event
     */
    @Listener
    public void playerJoinEvent(final ClientConnectionEvent.Join event) {
        final Player player = event.getTargetEntity();
        Task.builder().async().execute(() -> {
            final Optional<PetMeta> petMetaOpt;
            if (Config.INSTANCE.isJoin_enabled()) {
                if (!SpongePetDataListener.this.manager.getPetMetaController().getFromPlayer(player).isPresent() || Config.getInstance().isJoin_overwriteExistingPet()) {
                    if (player.getWorld() != null) {
                        final PetMeta meta = SpongePetDataListener.this.manager.getPetMetaController().create(player);
                        Config.INSTANCE.fixJoinDefaultPet(meta);
                        this.manager.getPetMetaController().store(meta);
                    }
                }
            }
            if ((petMetaOpt = PetBlocksApi.getDefaultPetMetaController().getFromPlayer(player)).isPresent()) {
                if (petMetaOpt.get().isEnabled()) {
                    Task.builder().execute(() -> {
                        if (player.getWorld() != null) {
                            final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(player, petMetaOpt.get());
                            PetBlocksApi.getDefaultPetBlockController().store(petBlock);
                        }
                    }).delayTicks(2L).submit(this.plugin);
                }
            }
        }).submit(this.plugin);
    }

    @Listener
    public void inventoryCloseEvent(ClickInventoryEvent.Close event, @First(typeFilter = Player.class) Player player) {
        if (this.manager.getInventories().containsKey(player)) {
            this.manager.getInventories().remove(player);
        }
    }

    private void handleClick(ItemStack currentItem, int slot, Player player, PetMeta petMeta, PetBlock petBlock) {
        final int itemSlot = slot + this.manager.getPages().get(player).currentCount + 1;
        if (this.manager.getPages().get(player).page == GUIPage.MAIN && this.getGUIItem("my-pet").getPosition() == slot) {
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
        } else if (this.isGUIItem(currentItem, "minecraft-heads-costume")) {
            ExtensionMethodsKt.sendMessage(this.collectedMinecraftHeads, player);
            this.manager.gui.setPage(player, GUIPage.MINECRAFTHEADS_COSTUMES, petMeta);
        } else if (this.isGUIItem(currentItem, "particle-pet")) {
            this.manager.gui.setPage(player, GUIPage.PARTICLES, petMeta);
        } else if (this.isGUIItem(currentItem, "wardrobe")) {
            this.manager.gui.setPage(player, GUIPage.WARDROBE, petMeta);
        } else if (this.isGUIItem(currentItem, "engine-settings")) {
            this.manager.gui.setPage(player, GUIPage.ENGINES, petMeta);
        } else if (this.isGUIItem(currentItem, "call-pet") && petBlock != null) {
            petBlock.teleport(player.getTransform());
            this.closeInventory(player);
        } else if (this.isGUIItem(currentItem, "hat-pet") && ExtensionMethodsKt.hasPermissions(player, Permission.ACTION_WEAR) && petBlock != null) {
            petBlock.wear(player);
        } else if (this.isGUIItem(currentItem, "riding-pet") && ExtensionMethodsKt.hasPermissions(player, Permission.ACTION_RIDE) && petBlock != null) {
            petBlock.ride(player);
        } else if (this.isGUIItem(currentItem, "suggest-heads")) {
            ExtensionMethodsKt.sendMessage(this.suggestHeadMessage, player);
            this.closeInventory(player);
        } else if (this.isGUIItem(currentItem, "naming-pet") && ExtensionMethodsKt.hasPermissions(player, Permission.ACTION_RENAME)) {
            ExtensionMethodsKt.sendMessage(((ChatBuilder) Config.getInstance().getPetNamingMessage()), player);
            this.closeInventory(player);
        } else if (this.isGUIItem(currentItem, "skullnaming-pet") && ExtensionMethodsKt.hasPermissions(player, Permission.ACTION_CUSTOMSKULL)) {
            ExtensionMethodsKt.sendMessage(((ChatBuilder) Config.getInstance().getPetSkinNamingMessage()), player);
            this.closeInventory(player);
        } else if (this.isGUIItem(currentItem, "cannon-pet") && ExtensionMethodsKt.hasPermissions(player, Permission.ACTION_CANNON) && petBlock != null) {
            petBlock.setVelocity(this.getDirection(player));
            this.closeInventory(player);
        } else if (this.isGUIItem(currentItem, "back")) {
            this.manager.gui.backPage(player, petMeta);
        } else if (this.manager.getPages().get(player).page == GUIPage.ENGINES && this.hasPermission(player, Permission.ALL_ENGINES, Permission.SINGLE_ENGINE, itemSlot)) {
            final Optional<EngineContainer<GUIItemContainer<Player>>> optEngineContainer = Config.<Player>getInstance().getEngineController().getContainerFromPosition(itemSlot);
            if (!optEngineContainer.isPresent()) {
                throw new IllegalArgumentException("Engine " + itemSlot + " could not be loaded correctly!");
            }
            ExtensionMethodsKt.setEngine(petMeta, petBlock, optEngineContainer.get());
            this.persistAsynchronously(petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else if (this.manager.getPages().get(player).page == GUIPage.PARTICLES && this.hasPermission(player, Permission.ALL_PARTICLES, Permission.SINGLE_PARTICLE, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = com.github.shynixn.petblocks.core.logic.persistence.configuration.Config.<Player>getInstance().getParticleController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Particle " + itemSlot + " could not be loaded correctly.");
            ExtensionMethodsKt.setParticleEffect(petMeta, petBlock, container.get());
            this.persistAsynchronously(petMeta);
            this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
        } else if (slot < 45 && this.manager.getPages().get(player).page == GUIPage.DEFAULT_COSTUMES && this.hasPermission(player, Permission.ALL_SIMPLEBLOCKCOSTUMES, Permission.SINGLE_SIMPLEBLOCKCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = com.github.shynixn.petblocks.core.logic.persistence.configuration.Config.<Player>getInstance().getOrdinaryCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        } else if (slot < 45 && this.manager.getPages().get(player).page == GUIPage.COLOR_COSTUMES && this.hasPermission(player, Permission.ALL_COLOREDBLOCKCOSTUMES, Permission.SINGLE_COLOREDBLOCKCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getColorCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        } else if (slot < 45 && this.manager.getPages().get(player).page == GUIPage.CUSTOM_COSTUMES && this.hasPermission(player, Permission.ALL_PLAYERHEADCOSTUMES, Permission.SINGLE_PLAYERHEADCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getRareCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        } else if (slot < 45 && this.manager.getPages().get(player).page == GUIPage.MINECRAFTHEADS_COSTUMES && this.hasPermission(player, Permission.ALL_MINECRAFTHEADCOSTUMES, Permission.SINGLE_MINECRAFTHEADCOSTUME, itemSlot)) {
            final Optional<GUIItemContainer<Player>> container = Config.<Player>getInstance().getMinecraftHeadsCostumesController().getContainerFromPosition(itemSlot);
            if (!container.isPresent())
                throw new IllegalArgumentException("Skin " + itemSlot + " could not be loaded correctly.");
            this.setCostumeSkin(player, petMeta, petBlock, container.get());
        }
    }

    /**
     * Sets the skin for the given petMeta and petblock of the gui container
     *
     * @param petMeta   petMeta
     * @param petBlock  petBlock
     * @param container container
     */
    private void setCostumeSkin(Player player, PetMeta petMeta, PetBlock petBlock, GUIItemContainer<Player> container) {
        ExtensionMethodsKt.setCostume(petMeta, petBlock, container);
        this.persistAsynchronously(petMeta);
        this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
    }

    /**
     * Gets called when the player clicks on the my-pet icon.
     * If Only disable Item is enabled, the petblock spawns otherwise the petblock meta gets reset
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
                petMeta.setSkin(CompatibilityItemType.SKULL_ITEM.getId(), 3, this.getGUIItem("my-pet").getSkin(), this.getGUIItem("my-pet").isItemUnbreakable());
            } else {
                final GUIItemContainer c = this.getGUIItem("default-appearance");
                petMeta.setSkin(c.getItemId(), c.getItemDamage(), c.getSkin(), c.isItemUnbreakable());
            }
            petMeta.getParticleEffectMeta().setEffectType(ParticleEffectMeta.ParticleEffectType.NONE);
            optPetBlock.ifPresent(PetBlock::respawn);
            this.persistAsynchronously(petMeta);
        }
    }

    private void closeInventory(Player player) {
        if (this.manager.getInventories().containsKey(player)) {
            this.manager.getInventories().remove(player);
        }
        player.closeInventory();
    }

    /**
     * Handles spamming protection.
     *
     * @param player player
     */
    private void handleSpamProtection(Player player) {
        if (!this.spamProtection.contains(player)) {
            this.spamProtection.add(player);
            Task.builder().execute(() -> this.spamProtection.remove(player)).delayTicks(30L).submit(this.plugin);
        }
    }

    /**
     * Returns the launch Direction for the cannon
     *
     * @param player player
     * @return launchDirection
     */
    private Vector3d getDirection(Player player) {
        final double rotX = player.getHeadRotation().getY();
        final double rotY = player.getHeadRotation().getX();
        final double h = Math.cos(Math.toRadians(rotY));
        return new Vector3d(-h * Math.sin(Math.toRadians(rotX)), 0.5, h * Math.cos(Math.toRadians(rotX)))
                .mul(3);
    }

    /**
     * Refreshes the current GUI page
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void refreshGUI(Player player, PetMeta petMeta) {
        this.manager.gui.setPage(player, this.manager.getPages().get(player).page, petMeta);
    }

    /**
     * Sets the petblock for the given player
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
     * Removes the petblock from the given player
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
        if (!ExtensionMethodsKt.hasPermissions(player, groupPermission)) {
            if (!ExtensionMethodsKt.hasPermissions(player, singlePermission, String.valueOf(slot))) {
                ExtensionMethodsKt.sendMessage(player, Config.getInstance().getPrefix() + com.github.shynixn.petblocks.core.logic.persistence.configuration.Config.getInstance().getNoPermission());
                return false;
            }
        }
        return true;
    }

    private GUIItemContainer<Player> getGUIItem(String name) {
        final Optional<GUIItemContainer<Player>> guiItemContainer = com.github.shynixn.petblocks.core.logic.persistence.configuration.Config.<Player>getInstance().getGuiItemsController().getGUIItemFromName(name);
        if (!guiItemContainer.isPresent())
            throw new IllegalArgumentException("Guiitem " + name + " could not be loaded correctly!");
        return guiItemContainer.get();
    }

    /**
     * Returns if the given itemStack is the gui Item with the given name
     *
     * @param itemStack itemStack
     * @param name      name
     * @return item
     */
    private boolean isGUIItem(ItemStack itemStack, String name) {
        return Config.INSTANCE.getGuiItemsController().isGUIItem(itemStack, name);
    }

    /**
     * Persists the current petMeta asynchronly
     *
     * @param petMeta petMeta
     */
    private void persistAsynchronously(PetMeta petMeta) {
        Task.builder().async().execute(() -> this.manager.getPetMetaController().store(petMeta)).submit(this.plugin);
    }
}
