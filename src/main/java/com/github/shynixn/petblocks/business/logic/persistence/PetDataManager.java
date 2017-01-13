package com.github.shynixn.petblocks.business.logic.persistence;

import java.util.HashMap;
import java.util.Map;

import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.events.PetMetaEvent;
import com.github.shynixn.petblocks.business.Language;

public final class PetDataManager {
    private final PetDataFileManager fileManager;
    private final Map<Player, PetData> petContainer = new HashMap<>();

    Map<Player, Inventory> inventories = new HashMap<>();
    Map<Player, GUI.GuiPageContainer> pages = new HashMap<>();
    GUI gui;

    public PetDataManager(JavaPlugin plugin) {
        super();
        this.gui = new GUI(this);
        new PetDataCommandExecutor(this);
        new PetDataListener(this, plugin);
        this.fileManager = new PetDataFileManager(plugin);
        this.fileManager.connectToDataBase();
    }

    public PetMeta createPetMeta(Player player, PetType petType) {
        final ItemStack itemStack = ConfigGUI.getInstance().getGeneral_defaultAppearanceContainer().generate();
        final PetData petData = new PetData(player, petType, Language.getDefaultPetName(player), itemStack, ConfigGUI.getInstance().getGeneral_defaultAppearanceContainer().getSkullName());
        petData.setMoveType(Config.getInstance().getMovingType(petType));
        petData.setMovementType(ConfigGUI.getInstance().getContainer(petType).getMovement());
        petData.setIsBuild(true);
        return petData;
    }

    public void addPetMeta(PetMeta meta) {
        if (meta.getOwner() != null && meta instanceof PetData) {
            if (this.petContainer.containsKey(meta.getOwner())) {
                Long id = this.petContainer.get(meta.getOwner()).getId();
                ((PetData) meta).setId(id);
            }
            this.petContainer.put(meta.getOwner(), (PetData) meta);
            Bukkit.getServer().getPluginManager().callEvent(new PetMetaEvent(this.petContainer.get(meta.getOwner())));
        }
    }

    public PetMeta getPetMeta(Player player) {
        if (this.petContainer.containsKey(player))
            return this.petContainer.get(player);
        return null;
    }

    public void persist(PetMeta petMeta) {
        if (petMeta == null)
            return;
        if (PetBlocksApi.hasPetBlock(petMeta.getOwner()))
            ((PetData) petMeta).enabled = true;
        this.fileManager.save(petMeta.getOwner(), this.petContainer.get(petMeta.getOwner()), false);
    }

    void saveUnsecured(Player player, PetMeta meta) {
        this.fileManager.save(player, (PetData) meta, false);
    }

    public void dispose() {
        for (final Player player : this.petContainer.keySet()) {
            this.fileManager.save(player, this.petContainer.get(player), true);
        }
        this.petContainer.clear();
    }

    public void remove(PetMeta petMeta) {
        this.petContainer.remove(petMeta.getOwner());
    }

    public boolean hasPetMeta(Player player) {
        if (!this.petContainer.containsKey(player)) {
            final PetData petData;
            if ((petData = this.fileManager.load(player)) != null) {
                if (petData.isEnabled()) {
                    PetBlocksApi.setPetBlock(player, petData);
                }
                this.petContainer.put(player, petData);
                return true;
            }
            return false;
        }
        return true;
    }
}
