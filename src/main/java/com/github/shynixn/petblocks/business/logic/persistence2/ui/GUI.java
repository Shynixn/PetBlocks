package com.github.shynixn.petblocks.business.logic.persistence2.ui;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import com.github.shynixn.petblocks.business.logic.persistence.PetDataManager;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class GUI {

    public Player player;
    private Inventory inventory;
    private GuiPage page;
    private GuiPageContainer container;

    public GUI(Player player) {
        this.player = player;
    }

    public void open() {
        if (this.player.getOpenInventory() != null)
            this.player.closeInventory();
        this.inventory = Bukkit.getServer().createInventory(this.player, 54, Language.GUI_TITLE);
        this.player.openInventory(this.inventory);
    }

    public boolean isClose() {
        return this.inventory == null;
    }

    void setMainItems(Player player) {
        if (this.isClose()) {
            return;
        }
        this.clearInventory(this.inventory);
        for (final PetType petType : PetType.values()) {
            final String name = petType.name();
            Config.getInstance().setMyContainer(this.inventory, Language.getDisplayName(name), ConfigGUI.getInstance().getContainer(petType), (Permission.SINGLEPETTYPE.get() + name.toLowerCase()), Permission.ALLPETTYPES.get());
        }
        this.page = GuiPage.MAIN;
    }

    public void setMainItems(Player player, PetType petType, boolean enabled, boolean refresh) {
        if (this.isClose()) {
            return;
        }
        if (this.page != GuiPage.MAIN)
            this.setMainItems(player);
        if (this.inventory.getItem(ConfigGUI.getInstance().getGeneral_myContainer().getPosition()) == null || refresh || this.inventory.getItem(ConfigGUI.getInstance().getGeneral_myContainer().getPosition()).getType() != Material.SKULL_ITEM)
            this.inventory.setItem(ConfigGUI.getInstance().getGeneral_myContainer().getPosition(), BukkitUtilities.nameItem(this.getItemStack(player, petType), Language.MY_PET, null));
        if (!enabled)
            Config.getInstance().setMyContainer(this.inventory, Language.ENABLE_PET, ConfigGUI.getInstance().getGeneral_enablePetContainer(), (Permission) null);
        else
            Config.getInstance().setMyContainer(this.inventory, Language.DISABLE_PET, ConfigGUI.getInstance().getGeneral_disablePetContainer(), (Permission) null);

        Config.getInstance().setMyContainer(this.inventory, Language.CANCEL, ConfigGUI.getInstance().getItems_cancelpetContainer(), (Permission) null);
        Config.getInstance().setMyContainer(this.inventory, Language.CANNON, ConfigGUI.getInstance().getItems_cannonpetContainer(), Permission.CANNON);
        Config.getInstance().setMyContainer(this.inventory, Language.CALL, ConfigGUI.getInstance().getItems_callpetContainer(), (Permission) null);

        Config.getInstance().setMyContainer(this.inventory, Language.SKULL_NAMING, ConfigGUI.getInstance().getItems_skullNamingContainer(), Permission.RENAMESKULL);
        Config.getInstance().setMyContainer(this.inventory, Language.NAMING, ConfigGUI.getInstance().getItems_namingContainer(), Permission.RENAMEPET);
        Config.getInstance().setMyContainer(this.inventory, Language.HAT, ConfigGUI.getInstance().getItems_hatpetContainer(), Permission.WEARPET);
        Config.getInstance().setMyContainer(this.inventory, Language.RIDING, ConfigGUI.getInstance().getItems_ridingpetContainer(), Permission.RIDEPET);

        Config.getInstance().setMyContainer(this.inventory, Language.COSTUME, ConfigGUI.getInstance().getItems_defaultcostumeContainer(), (Permission) null);
        Config.getInstance().setMyContainer(this.inventory, Language.COLOR_COSTUME, ConfigGUI.getInstance().getItems_colorcostumeContainer(), (Permission) null);
        Config.getInstance().setMyContainer(this.inventory, Language.CUSTOM_COSTUME, ConfigGUI.getInstance().getItems_customcostumeContainer(), (Permission) null);

        if (PetBlocksApi.getPetMeta(player).isSoundsEnabled())
            Config.getInstance().setMyContainer(this.inventory, Language.MUTE, ConfigGUI.getInstance().getItems_soundEnabledContainer(), (Permission) null);
        else
            Config.getInstance().setMyContainer(this.inventory, Language.UNMUTE, ConfigGUI.getInstance().getItems_soundDisabledContainer(), (Permission) null);

        Config.getInstance().setMyContainer(this.inventory, Language.PARTICLE, ConfigGUI.getInstance().getItems_particlepetContainer(), (Permission) null);
        this.fillEmptySlots(this.inventory);
    }

    public void setDefaultCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getDefaultItemStacks(), GuiPage.DEFAULTCOSTUMES, 0);
    }

    public void setColorCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getColoredItemStacks(), GuiPage.COLORCOSTUMES, 0);
    }

    public void setCustomCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getCustomItemStacks(), GuiPage.CUSTOMCOSTUMES, 0);
    }

    public void setParticleCostumeItems(Player player) {
        if (this.isClose()) {
            return;
        }
        this.clearInventory(this.inventory);
        for (int i = 0; i < 54 && i < ConfigParticle.getInstance().getParticleItemStacks().length; i++) {
            this.inventory.setItem(i, ConfigParticle.getInstance().getParticleItemStacks()[i]);
        }
        this.fillEmptySlots(this.inventory);
        this.page = GuiPage.PARTICLES;
    }

    public PetType getPetType(ItemStack itemStack) {
        for (final String name : PetType.getNames()) {
            if (BukkitUtilities.compareItemName(itemStack, Language.getDisplayName(name))) {
                return PetType.getPetTypeFromName(name);
            }
        }
        return null;
    }

    void setCostumes(Player player, ItemStack[] itemStacks, GuiPage page, int type) {
        if (this.isClose()) {
            return;
        }
        int count;
        GuiPageContainer current;
        if (this.container != null && type == 1) {
            current = this.container;
            while (current.next != null) {
                current = current.next;
            }
        } else if (this.container != null && type == 2) {
            current = this.container;
            if (current.next == null)
                return;
            if (current.next.next == null)
                return;
            while (current.next.next.next != null) {
                current = current.next;
            }
            current.next = null;
        } else {
            this.container = new GuiPageContainer(page);
            current = this.container;
        }

        count = current.startCount;
        this.clearInventory(this.inventory);
        inventory.setItem(ConfigGUI.getInstance().getGeneral_previouspageContainer().getPosition(), BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_previouspageContainer().generate(), Language.PREVIOUS));
        inventory.setItem(ConfigGUI.getInstance().getGeneral_nextpageContainer().getPosition(), BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_nextpageContainer().generate(), Language.NEXT));
        for (int i = 0; i < 45 && (i + current.startCount) < itemStacks.length; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, itemStacks[(i + current.startCount)]);
                count++;
            }
        }
        this.fillEmptySlots(inventory);

        GuiPageContainer runRef = this.container;
        while (runRef.next != null)
            runRef = runRef.next;
        runRef.next = new GuiPageContainer(page);
        runRef.next.startCount = count;
    }

    ItemStack[] getItemstackFromPage(GuiPage page) {
        if (page == GuiPage.COLORCOSTUMES)
            return ConfigGUI.getInstance().getColoredItemStacks();
        else if (page == GuiPage.DEFAULTCOSTUMES)
            return ConfigGUI.getInstance().getDefaultItemStacks();
        else if (page == GuiPage.CUSTOMCOSTUMES)
            return ConfigGUI.getInstance().getCustomItemStacks();
        return new ItemStack[0];
    }

    public Inventory getInventory() {
        return inventory;
    }

    static class GuiPageContainer {
        GuiPage page;
        int startCount;
        GuiPageContainer next;

        GuiPageContainer(GuiPage page) {
            super();
            this.page = page;
        }
    }

    enum GuiPage {
        MAIN, DEFAULTCOSTUMES, COLORCOSTUMES, CUSTOMCOSTUMES, PARTICLES
    }


    private void fillEmptySlots(Inventory inventory) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_emptyslotContainer().generate(), Language.EMPTY));
            }
        }
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
