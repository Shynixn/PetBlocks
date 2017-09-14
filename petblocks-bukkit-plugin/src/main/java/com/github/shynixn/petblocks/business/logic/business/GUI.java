package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.logic.business.configuration.Config;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.logic.business.entity.GuiPageContainer;
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
            for (final GUIItemContainer guiItemContainer : Config.getInstance().getGuiItemsController().getAll()) {
                if (guiItemContainer.getPage() == GUIPage.MAIN) {
                    inventory.setItem(guiItemContainer.getPosition(), (ItemStack) guiItemContainer.generate(player, guiItemContainer.getPermission()));
                }
            }
            if (petMeta.isSoundEnabled()) {
                final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("sounds-enabled-pet");
                inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, container.getPermission()));
            } else {
                final GUIItemContainer container = Config.getInstance().getGuiItemsController().getGUIItemByName("sounds-disabled-pet");
                inventory.setItem(container.getPosition(), (ItemStack) container.generate(player, container.getPermission()));
            }
            this.manager.pages.put(player, new GuiPageContainer(GUIPage.MAIN, null));
        } else if (page == GUIPage.ENGINES) {
            this.setEngineItems(player);
        }
        else if (page == GUIPage.PARTICLES) {
            this.setParticleItems(player);
        }
        final GUIItemContainer backGuiItemContainer = Config.getInstance().getGuiItemsController().getGUIItemByName("back");
        inventory.setItem(backGuiItemContainer.getPosition(), (ItemStack) backGuiItemContainer.generate(player, backGuiItemContainer.getPermission()));
        this.fillEmptySlots(inventory);
        player.updateInventory();
        System.out.println("FINISHED");
    }

    public void moveSubPage(Player player, int page) {

    }

    public void backPage(Player player, PetMeta petMeta) {
        System.out.println("BACK PAGE");
        GuiPageContainer container = this.manager.pages.get(player);
        if (container.page == GUIPage.MAIN) {
            player.closeInventory();
        } else {
            setPage(player, container.previousPage, petMeta);
        }
    }

    private void setEngineItems(Player player) {
        this.setCostumes(player, Config.getInstance().getEngineController().getAllGUIItems(), GUIPage.ENGINES);
    }

    private void setDefaultCostumeItems(Player player) {
        this.setCostumes(player, Config.getInstance().getOrdinaryCostumesController().getAll(), GUIPage.DEFAULT_COSTUMES);
    }

    private void setColorCostumeItems(Player player) {
        this.setCostumes(player, Config.getInstance().getColorCostumesController().getAll(), GUIPage.COLOR_COSTUMES);
    }

    private void setCustomCostumeItems(Player player) {
        this.setCostumes(player, Config.getInstance().getRareCostumesController().getAll(), GUIPage.CUSTOM_COSTUMES);
    }

    private void setParticleItems(Player player) {
        this.setCostumes(player, Config.getInstance().getParticleController().getAll(), GUIPage.PARTICLES);
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
        this.setCostumes(player, Config.getInstance().getMinecraftHeadsCostumesController().getAll(), GUIPage.MINECRAFTHEADS_COSTUMES);
    }

    private void setCostumes(Player player, List<GUIItemContainer> containers, GUIPage page) {
        if (this.manager.inventories.containsKey(player)) {
            int count;
            final GuiPageContainer container = this.manager.pages.get(player);
            if (container.previousPage != page) {
                container.previousPage = container.page;
            }
            container.page = page;

            count = container.startCount;
            final Inventory inventory = this.costumePreparation(player);

            final GUIItemContainer nextPage = Config.getInstance().getGuiItemsController().getGUIItemByName("next-page");
            final GUIItemContainer previousPage = Config.getInstance().getGuiItemsController().getGUIItemByName("previous-page");
            inventory.setItem(nextPage.getPosition(), (ItemStack) nextPage.generate(player));
            inventory.setItem(previousPage.getPosition(), (ItemStack) previousPage.generate(player));
            for (int i = 0; i < 45 && (i + container.startCount) < containers.size(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                    inventory.setItem(i, (ItemStack) containers.get((i + container.startCount)).generate(player));
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
