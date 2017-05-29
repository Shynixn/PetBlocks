package com.github.shynixn.petblocks.business.logic.business;

import java.util.HashMap;
import java.util.Map;

import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.shynixn.petblocks.api.PetBlocksApi;

import com.github.shynixn.petblocks.business.Language;

public final class PetDataManager {
    private final PetMetaController petDataController;
    Map<Player, Inventory> inventories = new HashMap<>();
    Map<Player, GUI.GuiPageContainer> pages = new HashMap<>();
    GUI gui;

    public PetDataManager(JavaPlugin plugin) {
        super();
        this.gui = new GUI(this);
        new PetDataCommandExecutor(this);
        new PetDataListener(this, plugin);
        this.petDataController = Factory.createPetDataController();
    }

    public PetMeta createPetMeta(Player player, PetType petType) {
        final ItemStack itemStack = ConfigGUI.getInstance().getGeneral_defaultAppearanceContainer().generate();
        final PetData petData = new PetData(player, petType, Language.getDefaultPetName(player), itemStack, ConfigGUI.getInstance().getGeneral_defaultAppearanceContainer().getSkullName());
        petData.setMoveType(Config.getInstance().getMovingType(petType));
        petData.setMovementType(ConfigGUI.getInstance().getContainer(petType).getMovement());
        petData.setIsBuild(true);
        return petData;
    }

    public void persist(PetMeta petMeta) {
        if (petMeta == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        if (PetBlocksApi.hasPetBlock(petMeta.getOwner()))
            petMeta.setEnabled(true);





        this.petDataController.store(petMeta);
    }


    public void remove(PetMeta petMeta) {
        if (petMeta == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        this.petDataController.remove(petMeta);
    }

    public PetMeta getPetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        return this.petDataController.getByPlayer(player);
    }

    public boolean hasPetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        return this.petDataController.hasEntry(player);
    }
}
