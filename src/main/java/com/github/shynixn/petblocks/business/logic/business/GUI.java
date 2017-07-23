package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.lib.BukkitUtilities;

class GUI {
    private final PetDataManager manager;

    GUI(PetDataManager manager) {
        super();
        this.manager = manager;
    }

    void open(Player player) {
        if (!this.manager.inventories.containsKey(player)) {
            if (player.getOpenInventory() != null)
                player.closeInventory();
            final Inventory inventory = Bukkit.getServer().createInventory(player, 54, Language.GUI_TITLE);
            this.manager.inventories.put(player, inventory);
            player.openInventory(inventory);
        }
    }

    void setMainItems(Player player, PetType petType, boolean enabled, boolean refresh, PetMeta petMeta) {
        //Thread safe
        if (this.manager.inventories.containsKey(player)) {
            if (this.manager.pages.get(player).page != GuiPage.MAIN)
                this.setMainItems(player);
            final Inventory inventory = this.manager.inventories.get(player);
            if (inventory.getItem(ConfigGUI.getInstance().getGeneral_myContainer().getPosition()) == null || refresh || inventory.getItem(ConfigGUI.getInstance().getGeneral_myContainer().getPosition()).getType() != Material.SKULL_ITEM)
                inventory.setItem(ConfigGUI.getInstance().getGeneral_myContainer().getPosition(), BukkitUtilities.nameItem(this.getItemStack(player, petType), Language.MY_PET, null));
            if (!enabled) {
                if (!ConfigGUI.getInstance().isSettings_onlyDisableItem()) {
                    Config.getInstance().setMyContainer(inventory, Language.ENABLE_PET, ConfigGUI.getInstance().getGeneral_enablePetContainer(), (Permission) null);
                } else {
                    inventory.setItem(ConfigGUI.getInstance().getGeneral_enablePetContainer().getPosition(), BukkitUtilities.nameItemDisplay(ConfigGUI.getInstance().getGeneral_emptyslotContainer().generate(), Language.EMPTY));
                }
            } else {
                Config.getInstance().setMyContainer(inventory, Language.DISABLE_PET, ConfigGUI.getInstance().getGeneral_disablePetContainer(), (Permission) null);
            }
            Config.getInstance().setMyContainer(inventory, Language.CANCEL, ConfigGUI.getInstance().getItems_cancelpetContainer(), (Permission) null);
            Config.getInstance().setMyContainer(inventory, Language.CANNON, ConfigGUI.getInstance().getItems_cannonpetContainer(), Permission.CANNON);
            Config.getInstance().setMyContainer(inventory, Language.CALL, ConfigGUI.getInstance().getItems_callpetContainer(), (Permission) null);

            Config.getInstance().setMyContainer(inventory, Language.SKULL_NAMING, ConfigGUI.getInstance().getItems_skullNamingContainer(), Permission.RENAMESKULL);
            Config.getInstance().setMyContainer(inventory, Language.NAMING, ConfigGUI.getInstance().getItems_namingContainer(), Permission.RENAMEPET);
            Config.getInstance().setMyContainer(inventory, Language.HAT, ConfigGUI.getInstance().getItems_hatpetContainer(), Permission.WEARPET);
            Config.getInstance().setMyContainer(inventory, Language.RIDING, ConfigGUI.getInstance().getItems_ridingpetContainer(), Permission.RIDEPET);

            Config.getInstance().setMyContainer(inventory, Language.COSTUME, ConfigGUI.getInstance().getItems_defaultcostumeContainer(), (Permission) null);
            Config.getInstance().setMyContainer(inventory, Language.COLOR_COSTUME, ConfigGUI.getInstance().getItems_colorcostumeContainer(), (Permission) null);
            Config.getInstance().setMyContainer(inventory, Language.CUSTOM_COSTUME, ConfigGUI.getInstance().getItems_customcostumeContainer(), (Permission) null);

            if (petMeta.isSoundsEnabled())
                Config.getInstance().setMyContainer(inventory, Language.MUTE, ConfigGUI.getInstance().getItems_soundEnabledContainer(), (Permission) null);
            else
                Config.getInstance().setMyContainer(inventory, Language.UNMUTE, ConfigGUI.getInstance().getItems_soundDisabledContainer(), (Permission) null);

            Config.getInstance().setMyContainer(inventory, Language.PARTICLE, ConfigGUI.getInstance().getItems_particlepetContainer(), (Permission) null);
            this.fillEmptySlots(inventory);
        }
    }

    void setMainItems(Player player) {
        if (this.manager.inventories.containsKey(player)) {
            this.clearInventory(this.manager.inventories.get(player));
            final Inventory inventory = this.manager.inventories.get(player);
            for (final PetType petType : PetType.values()) {
                final String name = petType.name();
                Config.getInstance().setMyContainer(inventory, Language.getDisplayName(name), ConfigGUI.getInstance().getContainer(petType), (Permission.SINGLEPETTYPE.get() + name.toLowerCase()), Permission.ALLPETTYPES.get());
            }
            this.manager.pages.put(player, new GuiPageContainer(GuiPage.MAIN));
        }
    }

    void setDefaultCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getDefaultItemStacks(), GuiPage.DEFAULTCOSTUMES, 0);
    }

    void setColorCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getColoredItemStacks(), GuiPage.COLORCOSTUMES, 0);
    }

    void setCustomCostumeItems(Player player) {
        this.setCostumes(player, ConfigGUI.getInstance().getCustomItemStacks(), GuiPage.CUSTOMCOSTUMES, 0);
    }

    void setParticleCostumeItems(Player player) {
        if (this.manager.inventories.containsKey(player)) {
            final Inventory inventory = this.costumePreparation(player);
            for (int i = 0; i < 54 && i < ConfigParticle.getInstance().getParticleItemStacks().length; i++) {
                inventory.setItem(i, ConfigParticle.getInstance().getParticleItemStacks()[i]);
            }
            this.fillEmptySlots(inventory);
            this.manager.pages.put(player, new GuiPageContainer(GuiPage.PARTICLES));
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

    void setCostumes(Player player, ItemStack[] itemStacks, GuiPage page, int type) {
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
                container = new GuiPageContainer(page);
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
            runRef.next = new GuiPageContainer(page);
            runRef.next.startCount = count;
            this.manager.pages.put(player, container);
        }
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

    private void clearInventory(Inventory inventory) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            inventory.setItem(i, null);
        }
    }

    private ItemStack getItemStack(Player player, PetType petType) {
        return Config.getInstance().getMyItemStack(player, Language.getDisplayName(petType.name()), ConfigGUI.getInstance().getContainer(petType), (Permission.SINGLEPETTYPE.get() + petType.name().toLowerCase()), Permission.ALLPETTYPES.get());
    }
}
