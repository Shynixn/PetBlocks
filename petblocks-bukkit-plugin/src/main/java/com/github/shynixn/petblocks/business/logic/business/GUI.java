package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.logic.business.configuration.Config;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.logic.business.entity.GuiPageContainer;
import com.github.shynixn.petblocks.business.logic.business.entity.ItemContainer;
import com.github.shynixn.petblocks.lib.ChatBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GUI {
    private final PetBlockManager manager;
    private final Plugin plugin;

    /**
     * Initializes a new gui
     *
     * @param manager manager
     */
    GUI(PetBlockManager manager) {
        super();
        this.manager = manager;
        this.plugin = JavaPlugin.getPlugin(PetBlocksPlugin.class);
    }

    /**
     * Opens the gui for a player
     *
     * @param player player
     */
    public void open(Player player) {
        if (!this.manager.inventories.containsKey(player)) {
            if (player.getOpenInventory() != null) {
                player.closeInventory();
            }
            if (this.manager.headDatabasePlayers.contains(player)) {
                this.manager.headDatabasePlayers.remove(player);
            }
            final Inventory inventory = Bukkit.getServer().createInventory(player, 54, Config.getInstance().getGUITitle());
            this.manager.inventories.put(player, inventory);
            player.openInventory(inventory);
        }
    }

    public void setPage(Player player, GUIPage page, PetMeta petMeta) {
        if (!this.manager.inventories.containsKey(player)) {
            return;
        }
        final Inventory inventory = this.manager.inventories.get(player);
        inventory.clear();
        if (page == GUIPage.MAIN) {
            this.setOtherItems(player, inventory, petMeta, GUIPage.MAIN);
            this.manager.pages.put(player, new GuiPageContainer(GUIPage.MAIN, null));
        } else if (page == GUIPage.WARDROBE) {
            this.setOtherItems(player, inventory, petMeta, page);
            this.manager.pages.put(player, new GuiPageContainer(page, this.manager.pages.get(player)));
        } else {
            this.setListAble(player, page, 0);
        }
        final GUIItemContainer backGuiItemContainer = Config.getInstance().getGuiItemsController().getGUIItemByName("back");
        inventory.setItem(backGuiItemContainer.getPosition(), (ItemStack) backGuiItemContainer.generate(player, backGuiItemContainer.getPermission()));
        this.fillEmptySlots(inventory);
        player.updateInventory();
    }

    private void setListAble(Player player, GUIPage page, int type) {
        if (page == GUIPage.ENGINES) {
            this.setEngineItems(player, type);
        } else if (page == GUIPage.PARTICLES) {
            this.setParticleItems(player, type);
        } else if (page == GUIPage.DEFAULT_COSTUMES) {
            this.setDefaultCostumeItems(player, type);
        } else if (page == GUIPage.COLOR_COSTUMES) {
            this.setColorCostumeItems(player, type);
        } else if (page == GUIPage.CUSTOM_COSTUMES) {
            this.setCustomCostumeItems(player, type);
        } else if (page == GUIPage.MINECRAFTHEADS_COSTUMES) {
            this.setMinecraftHeadsCostumeItems(player, type);
        }
    }

    public void backPage(Player player, PetMeta petMeta) {
        GuiPageContainer container = this.manager.pages.get(player);
        if (container.page == GUIPage.MAIN) {
            player.closeInventory();
        } else {
            if (container.previousPage != null && container.previousPage.previousPage != null)
                this.manager.pages.put(player, container.previousPage.previousPage);
            this.setPage(player, container.previousPage.page, petMeta);
        }
    }

    public void moveList(Player player, boolean forward) {
        if (forward) {
            this.setListAble(player, this.manager.pages.get(player).page, 1);
        } else {
            this.setListAble(player, this.manager.pages.get(player).page, 2);
        }
    }

    private void setOtherItems(Player player, Inventory inventory, PetMeta petMeta, GUIPage page) {
        for (final GUIItemContainer guiItemContainer : Config.getInstance().getGuiItemsController().getAll()) {
            if (guiItemContainer.getPage() == page) {
                inventory.setItem(guiItemContainer.getPosition(), (ItemStack) guiItemContainer.generate(player, guiItemContainer.getPermission()));
            }
        }
        if (page == GUIPage.MAIN) {
            final GUIItemContainer myPetContainer = Config.getInstance().getGuiItemsController().getGUIItemByName("my-pet");
            inventory.setItem(myPetContainer.getPosition(), (ItemStack) petMeta.getHeadItemStack());
        }
        if (petMeta.isSoundEnabled()) {
            final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("sounds-enabled-pet");
            if (page == container.getPage()) {
                inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, container.getPermission()));
            }
        } else {
            final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("sounds-disabled-pet");
            if (page == container.getPage()) {
                inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, container.getPermission()));
            }
        }
        if (!petMeta.isEnabled()) {
            final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("enable-pet");
            if (page == container.getPage()) {
                inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, container.getPermission()));
            }
        } else {
            final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("disable-pet");
            if (page == container.getPage()) {
                inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, container.getPermission()));
            }
        }
        final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("minecraft-heads-costume");
        if (page == container.getPage()) {
            inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, "minecraft-heads"));
        }
    }

    private void setEngineItems(Player player, int type) {
        this.setCostumes(player, Config.getInstance().getEngineController().getAllGUIItems(), GUIPage.ENGINES, type);
    }

    private void setDefaultCostumeItems(Player player, int type) {
        this.setCostumes(player, Config.getInstance().getOrdinaryCostumesController().getAll(), GUIPage.DEFAULT_COSTUMES, type);
    }

    private void setColorCostumeItems(Player player, int type) {
        this.setCostumes(player, Config.getInstance().getColorCostumesController().getAll(), GUIPage.COLOR_COSTUMES, type);
    }

    private void setCustomCostumeItems(Player player, int type) {
        this.setCostumes(player, Config.getInstance().getRareCostumesController().getAll(), GUIPage.CUSTOM_COSTUMES, type);
    }

    private void setParticleItems(Player player, int type) {
        this.setCostumes(player, Config.getInstance().getParticleController().getAll(), GUIPage.PARTICLES, type);
    }

    /**
     * Sets all minecraft-heads costumes
     *
     * @param player player
     */
    private void setMinecraftHeadsCostumeItems(Player player, int type) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
            new ChatBuilder().text(Config.getInstance().getPrefix())
                    .text("Pets collected by ")
                    .component(">>Minecraft-Heads.com<<")
                    .setColor(ChatColor.YELLOW)
                    .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com")
                    .setHoverText("Goto the Minecraft-Heads website!")
                    .builder().sendMessage(player);
        });
        this.setCostumes(player, Config.getInstance().getMinecraftHeadsCostumesController().getAll(), GUIPage.MINECRAFTHEADS_COSTUMES, type);
    }

    private void setCostumes(Player player, List<GUIItemContainer> containers, GUIPage page, int type) {
        if (this.manager.inventories.containsKey(player)) {
            final GuiPageContainer previousContainer = this.manager.pages.get(player);
            final GuiPageContainer container;
            if (previousContainer.page != page) {
                container = new GuiPageContainer(page, previousContainer);
                this.manager.pages.put(player, container);
            } else {
                container = this.manager.pages.get(player);
            }
            if (type == 1 && (container.startCount % 45 != 0 || containers.size() == container.startCount)) {
                return;
            }
            if (type == 2) {
                if (container.currentCount == 0) {
                    return;
                }
                container.startCount = container.currentCount - 45;
            }

            int count = container.startCount;
            if (count < 0)
                count = 0;
            container.currentCount = container.startCount;
            final Inventory inventory = this.costumePreparation(player);
            int i;
            int scheduleCounter = 0;
            for (i = 0; i < 45 && (i + container.startCount) < containers.size(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {

                    final int slot = i;
                    final int containerSlot = (i + container.startCount);
                    final int mountBlock = container.currentCount;
                    final GUIPage currentPage = container.page;
                    count++;
                    if (i % 2 == 0) {
                        scheduleCounter++;
                    }
                    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                        if (container.currentCount == mountBlock && currentPage == manager.pages.get(player).page) {
                            inventory.setItem(slot, (ItemStack) containers.get(containerSlot).generate(player));
                        }
                    }, scheduleCounter);
                }
            }
            container.startCount = count;
            final GUIItemContainer nextPage = Config.getInstance().getGuiItemsController().getGUIItemByName("next-page");
            final GUIItemContainer previousPage = Config.getInstance().getGuiItemsController().getGUIItemByName("previous-page");
            final GUIItemContainer backGuiItemContainer = Config.getInstance().getGuiItemsController().getGUIItemByName("back");
            inventory.setItem(backGuiItemContainer.getPosition(), (ItemStack) backGuiItemContainer.generate(player, backGuiItemContainer.getPermission()));
            inventory.setItem(nextPage.getPosition(), (ItemStack) nextPage.generate(player));
            inventory.setItem(previousPage.getPosition(), (ItemStack) previousPage.generate(player));
            this.fillEmptySlots(inventory);
        }
    }

    private Inventory costumePreparation(Player player) {
        this.clearInventory(this.manager.inventories.get(player));
        return this.manager.inventories.get(player);
    }

    private void fillEmptySlots(Inventory inventory) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, (ItemStack) Config.getInstance().getGuiItemsController().getGUIItemByName("empty-slot").generate(inventory.getHolder()));
            }
        }
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
}
