package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.VersionSupport;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ChatBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

class GUI {
    private final PetDataManager manager;

    /**
     * Initializes a new gui
     *
     * @param manager manager
     */
    GUI(PetDataManager manager) {
        super();
        this.manager = manager;
    }

    /**
     * Opens the gui for a player
     *
     * @param player player
     */
    void open(Player player) {
        if (!this.manager.inventories.containsKey(player)) {
            if (player.getOpenInventory() != null) {
                player.closeInventory();
            }
            if (this.manager.headDatabasePlayers.contains(player)) {
                this.manager.headDatabasePlayers.remove(player);
            }
            final Inventory inventory = Bukkit.getServer().createInventory(player, 54, Language.GUI_TITLE);
            this.manager.inventories.put(player, inventory);
            player.openInventory(inventory);
        }
    }

    /**
     * Sets the items of the given gui page
     *
     * @param guiPage guiPage
     * @param player  player
     * @param petType petType
     * @param enabled enabled
     * @param refresh refresh
     * @param petMeta petMeta
     */
    void setItems(GUIPage guiPage, Player player, PetType petType, boolean enabled, boolean refresh, PetMeta petMeta) {
        if (!this.manager.inventories.containsKey(player)) {
            return;
        }
        if (guiPage == null) {
            if (this.manager.pages.containsKey(player)) {
                guiPage = this.manager.pages.get(player).page;
            } else {
                guiPage = GUIPage.MAIN;
            }
        }
        final Inventory inventory = this.manager.inventories.get(player);
        if (guiPage != this.manager.pages.get(player).page) {
            this.clearInventory(inventory);
            player.updateInventory();
        }
        if (guiPage == GUIPage.MAIN) {
            if (this.manager.pages.get(player).page != GUIPage.MAIN) {
                this.setPetTypeItems(player);
            }
            if (inventory.getItem(ConfigGUI.Items.MYPET.getContainer().getPosition()) == null || refresh || inventory.getItem(ConfigGUI.Items.MYPET.getContainer().getPosition()).getType() != Material.SKULL_ITEM) {
                inventory.setItem(ConfigGUI.Items.MYPET.getContainer().getPosition(), BukkitUtilities.nameItem(this.getItemStack(player, petType), Language.MY_PET));
            }
            if (!enabled) {
                if (!ConfigGUI.getInstance().isOnlyDisableItemEnabled()) {
                    this.setGUIItem(guiPage, inventory, ConfigGUI.Items.ENABLEPET);
                } else {
                    System.out.println("WUUUUUUUUUT");
                   // inventory.setItem(ConfigGUI.getInstance().getGeneral_enablePetContainer().getPosition(), BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_emptyslotContainer().generate(), Language.EMPTY));
                }
            } else {
                this.setGUIItem(guiPage, inventory, ConfigGUI.Items.DISABLEPET);
            }
            this.setGUIItem(guiPage, inventory, ConfigGUI.Items.WARDROBE);
        }
        if (petMeta.isSoundEnabled()) {
            this.setGUIItem(guiPage, inventory, ConfigGUI.Items.SOUNDENABLED);
        } else {
            this.setGUIItem(guiPage, inventory, ConfigGUI.Items.SOUNDDISABLED);
        }

        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.CANCEL);
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.CALL);

        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.CANNON, Permission.CANNON.get());
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.SKULLNAMING, Permission.RENAMESKULL.get());
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.CALL, Permission.RENAMEPET.get());
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.HAT, Permission.WEARPET.get());
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.RIDING, Permission.RIDEPET.get());

        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.ORDINARYCOSTUMES);
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.COLORCOSTUMES);
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.RARECOSTUMES);
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.EXCLUSIVECOSTUMES, "minecraft-heads");
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.HEADDATABASE, "head-database");
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.SUGGESTHEADS);
        this.setGUIItem(guiPage, inventory, ConfigGUI.Items.PARTICLEEFFECTS);

        this.fillEmptySlots(inventory);
        this.manager.pages.put(player, new GuiPageContainer(guiPage, this.manager.pages.get(player).page));
        player.updateInventory();
    }

    /**
     * Initializes the pet types on the current menu page
     *
     * @param player player
     */
    void setPetTypeItems(Player player) {
        if (this.manager.inventories.containsKey(player)) {
            this.clearInventory(this.manager.inventories.get(player));
            final Inventory inventory = this.manager.inventories.get(player);
            final VersionSupport serverVersion = VersionSupport.getServerVersion();
            for (final PetType petType : PetType.values()) {
                if (serverVersion.isVersionSameOrGreaterThan(petType.getVersion())) {
                    final String name = petType.name();
                    Config.getInstance().setMyContainer(GUIPage.MAIN, inventory, Language.getDisplayName(name), ConfigGUI.getInstance().getContainer(petType), (Permission.SINGLEPETTYPE.get() + name.toLowerCase()), Permission.ALLPETTYPES.get());
                }
            }
            this.manager.pages.put(player, new GuiPageContainer(GUIPage.MAIN, null));
        }
    }

    void setDefaultCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getDefaultItemStacks(), GUIPage.DEFAULT_COSTUMES, 0);
    }

    void setColorCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getColoredItemStacks(), GUIPage.COLOR_COSTUMES, 0);
    }

    void setCustomCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getCustomItemStacks(), GUIPage.CUSTOM_COSTUMES, 0);
    }

    /**
     * Sets all minecraft-heads costumes
     *
     * @param player player
     */
    void setMinecraftHeadsCostumeItems(Player player) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
            new ChatBuilder().text(Language.PREFIX)
                    .text("Pets collected by ")
                    .component(">>Minecraft-Heads.com<<")
                    .setColor(ChatColor.YELLOW)
                    .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com")
                    .setHoverText("Goto the Minecraft-Heads website!")
                    .builder().sendMessage(player);
        });
        this.setCostumes(player, ConfigGUI.getInstance().getMinecraftHeadsItemStacks(), GUIPage.MINECRAFTHEADS_COSTUMES, 0);
    }

    void setParticleItems(Player player) {
        if (this.manager.inventories.containsKey(player)) {
            final Inventory inventory = this.costumePreparation(player);
            for (int i = 0; i < 54 && i < ConfigParticle.getInstance().getParticleItemStacks().length; i++) {
                inventory.setItem(i, ConfigParticle.getInstance().getParticleItemStacks()[i]);
            }
            this.fillEmptySlots(inventory);
            this.manager.pages.put(player, new GuiPageContainer(GUIPage.PARTICLES, this.manager.pages.get(player).page));
        }
    }

    PetType getPetType(ItemStack itemStack) {
        for (final String name : PetType.getNames()) {
            if (BukkitUtilities.compareItemName(itemStack, Language.getDisplayName(name))) {
                return PetType.getPetTypeFromName(name);
            }
        }
        return null;
    }

    void setCostumes(Player player, ItemStack[] itemStacks, GUIPage page, int type) {
        if (this.manager.inventories.containsKey(player)) {
            int count;
            final GuiPageContainer container;
            GuiPageContainer current;
            if (this.manager.pages.containsKey(player) && type == 1) {
                container = this.manager.pages.get(player);
                current = container;
                while (current.next != null) {
                    current = current.next;
                }
            } else if (this.manager.pages.containsKey(player) && type == 2) {
                container = this.manager.pages.get(player);
                current = container;
                if (current.next == null)
                    return;
                if (current.next.next == null)
                    return;
                while (current.next.next.next != null) {
                    current = current.next;
                }
                current.next = null;
            } else {
                container = new GuiPageContainer(page, this.manager.pages.get(player).page);
                current = container;
            }

            count = current.startCount;
            final Inventory inventory = this.costumePreparation(player);
            inventory.setItem(ConfigGUI.getInstance().getGeneral_previouspageContainer().getPosition(), BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_previouspageContainer().generate(), Language.PREVIOUS));
            inventory.setItem(ConfigGUI.getInstance().getGeneral_nextpageContainer().getPosition(), BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_nextpageContainer().generate(), Language.NEXT));
            for (int i = 0; i < 45 && (i + current.startCount) < itemStacks.length; i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                    inventory.setItem(i, itemStacks[(i + current.startCount)]);
                    count++;
                }
            }
            this.fillEmptySlots(inventory);

            GuiPageContainer runRef = container;
            while (runRef.next != null)
                runRef = runRef.next;
            runRef.next = new GuiPageContainer(page, this.manager.pages.get(player).page);
            runRef.next.startCount = count;
            this.manager.pages.put(player, container);
        }
    }

    ItemStack[] getItemstackFromPage(GUIPage page) {
        if (page == GUIPage.COLOR_COSTUMES)
            return ConfigGUI.getInstance().getColoredItemStacks();
        else if (page == GUIPage.DEFAULT_COSTUMES)
            return ConfigGUI.getInstance().getDefaultItemStacks();
        else if (page == GUIPage.CUSTOM_COSTUMES)
            return ConfigGUI.getInstance().getCustomItemStacks();
        else if (page == GUIPage.MINECRAFTHEADS_COSTUMES)
            return ConfigGUI.getInstance().getMinecraftHeadsItemStacks();
        return new ItemStack[0];
    }

    static class GuiPageContainer {
        GUIPage previousPage;
        GUIPage page;
        int startCount;
        GuiPageContainer next;

        GuiPageContainer(GUIPage page, GUIPage previousPage) {
            super();
            this.page = page;
            this.previousPage = previousPage;
        }
    }

    private Inventory costumePreparation(Player player) {
        this.clearInventory(this.manager.inventories.get(player));
        return this.manager.inventories.get(player);
    }

    private void fillEmptySlots(Inventory inventory) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_emptyslotContainer().generate(), Language.EMPTY));
            }
        }
    }

    private void setGUIItem(GUIPage guiPage, Inventory inventory, ConfigGUI.Items item, String... permission) {
        if (!this.canSetMyContainer(guiPage, item.getContainer()))
            return;
        inventory.setItem(item.getContainer().getPosition(), item.getContainer().generate((Player) inventory.getHolder(), permission));
    }

    /**
     * Returns if my container should be set
     *
     * @param guiPage   guiPage
     * @param container container
     * @return should be set
     */
    private boolean canSetMyContainer(GUIPage guiPage, GUIItemContainer container) {
        if (guiPage == GUIPage.MAIN && (container.getPage() == null))
            return true;
        else if ((guiPage == GUIPage.MAIN && container.getPage() == GUIPage.MAIN)
                || (guiPage == GUIPage.WARDROBE && container.getPage() == GUIPage.WARDROBE))
            return true;
        return false;
    }

    private void clearInventory(Inventory inventory) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            inventory.setItem(i, null);
        }
    }

    private ItemStack getItemStack(Player player, PetType petType) {
        return Config.getInstance().getMyItemStack(player, Language.getDisplayName(petType.name()), ConfigGUI.getInstance().getContainer(petType), (Permission.SINGLEPETTYPE.get() + petType.name().toLowerCase()), Permission.ALLPETTYPES.get());
    }
}
