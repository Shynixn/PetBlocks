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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GUI {
    private final PetBlockManager manager;

    /**
     * Initializes a new gui
     *
     * @param manager manager
     */
    GUI(PetBlockManager manager) {
        super();
        this.manager = manager;
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
        System.out.println("SETING PAGE");
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
        System.out.println("FINISHED");
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
            this.setMinecraftHeadsCostumeItems(player);
        }
    }

    public void backPage(Player player, PetMeta petMeta) {
        System.out.println("BACK PAGE");
        GuiPageContainer container = this.manager.pages.get(player);
        if (container.page == GUIPage.MAIN) {
            player.closeInventory();
        } else {
            System.out.println("PREVIOUS: " + container.previousPage.page);
            if (container.previousPage != null && container.previousPage.previousPage != null)
                this.manager.pages.put(player, container.previousPage.previousPage);
            this.setPage(player, container.previousPage.page, petMeta);
        }
    }

    public void moveList(Player player, boolean forward) {
        if (forward) {
            setListAble(player, manager.pages.get(player).page, 1);
        } else {
            setListAble(player, manager.pages.get(player).page, 2);
        }
    }

    private void setOtherItems(Player player, Inventory inventory, PetMeta petMeta, GUIPage page) {
        for (final GUIItemContainer guiItemContainer : Config.getInstance().getGuiItemsController().getAll()) {
            if (guiItemContainer.getPage() == page) {
                inventory.setItem(guiItemContainer.getPosition(), (ItemStack) guiItemContainer.generate(player, guiItemContainer.getPermission()));
            }
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
    private void setMinecraftHeadsCostumeItems(Player player) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
            new ChatBuilder().text(Config.getInstance().getPrefix())
                    .text("Pets collected by ")
                    .component(">>Minecraft-Heads.com<<")
                    .setColor(ChatColor.YELLOW)
                    .setClickAction(ChatBuilder.ClickAction.OPEN_URL, "http://minecraft-heads.com")
                    .setHoverText("Goto the Minecraft-Heads website!")
                    .builder().sendMessage(player);
        });
        this.setCostumes(player, Config.getInstance().getMinecraftHeadsCostumesController().getAll(), GUIPage.MINECRAFTHEADS_COSTUMES, 0);
    }

    private void setCostumes(Player player, List<GUIItemContainer> containers, GUIPage page, int type) {
        if (this.manager.inventories.containsKey(player)) {
            final GuiPageContainer previousContainer = this.manager.pages.get(player);
            GuiPageContainer container;
            if (previousContainer.page != page) {
                container = new GuiPageContainer(page, previousContainer);
                this.manager.pages.put(player, container);
            } else {
                container = this.manager.pages.get(player);
            }
            if (type == 1) {

                while (container.next != null) {
                    final GuiPageContainer pre = container;
                    container = container.next;
                    container.pre = pre;
                }
                System.out.println("NEXT ");
            } else if (type == 2) {

                while (container.next != null) {

                    System.out.println("CONTAINER: " + container.startCount);
                    final GuiPageContainer pre = container;
                    container = container.next;
                }
                container = container.pre;
                System.out.println("PRE");
            }

            if (container == null) {
                container = this.manager.pages.get(player);
            }

            int count = container.startCount;
            final Inventory inventory = this.costumePreparation(player);
            System.out.println("STARTCOUNT: " + count);
            int i;
            for (i = 0; i < 45 && (i + container.startCount) < containers.size(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                    inventory.setItem(i, (ItemStack) containers.get((i + container.startCount)).generate(player));
                    count++;
                }
            }

            System.out.println("STARCOUNT: " + container.startCount);
            System.out.println("NEXT: " + count + ":" + containers.size());
            if ((i + container.startCount) >= containers.size()) {
                System.out.println("REACHED MAX");
            } else {
                System.out.println("NOT MAX");
                container.next = new GuiPageContainer(page, container);
                container.next.startCount = count;
            }

            final GUIItemContainer nextPage = Config.getInstance().getGuiItemsController().getGUIItemByName("next-page");
            final GUIItemContainer previousPage = Config.getInstance().getGuiItemsController().getGUIItemByName("previous-page");
            final GUIItemContainer backGuiItemContainer = Config.getInstance().getGuiItemsController().getGUIItemByName("back");
            inventory.setItem(backGuiItemContainer.getPosition(), (ItemStack) backGuiItemContainer.generate(player, backGuiItemContainer.getPermission()));
            inventory.setItem(nextPage.getPosition(), (ItemStack) nextPage.generate(player));
            inventory.setItem(previousPage.getPosition(), (ItemStack) previousPage.generate(player));
            this.fillEmptySlots(inventory);
            System.out.println("STORE INTERNAL NEW CONTAINER");
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
