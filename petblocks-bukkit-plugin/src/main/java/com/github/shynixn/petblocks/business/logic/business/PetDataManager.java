package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.business.logic.configuration.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PetDataManager implements AutoCloseable {
    private final PetMetaController petDataController;
    private final PlayerMetaController playerMetaController;
    private final ParticleEffectMetaController particleEffectMetaController;
    private PetBlockFilter filter;

    private final long mainThreadId;

    Set<Player> headDatabasePlayers = new HashSet<>();
    Map<Player, Inventory> inventories = new HashMap<>();
    Map<Player, GUI.GuiPageContainer> pages = new HashMap<>();
    GUI gui;

    public PetDataManager(Plugin plugin) {
        super();
        this.mainThreadId = Thread.currentThread().getId();
        this.gui = new GUI(this);
        if (plugin.getPluginLoader() != null) {
            new PetDataCommandExecutor(this);
            new PetDataListener(this, plugin);
            this.filter = PetBlockFilter.create();
        }
        Factory.initialize(plugin);
        this.petDataController = Factory.createPetDataController();
        this.playerMetaController = Factory.createPlayerDataController();
        this.particleEffectMetaController = Factory.createParticleEffectController();
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
        if (Thread.currentThread().getId() == this.mainThreadId)
            throw new RuntimeException("This method has to be accessed asynchronously.");
        if (((PetData)petMeta).getOwner() != null) {
            petMeta.getPlayerMeta().setName(((Player)petMeta.getPlayerMeta().getPlayer()).getName());
            if (petMeta.getPlayerMeta().getId() == 0) {
                final PlayerMeta playerMeta;
                if ((playerMeta = this.playerMetaController.getByUUID(((Player)petMeta.getPlayerMeta().getPlayer()).getUniqueId())) != null) {
                    petMeta.setPlayerMeta(playerMeta);
                }
            }
        }
        PetDataManager.this.playerMetaController.store(petMeta.getPlayerMeta());
        PetDataManager.this.particleEffectMetaController.store(petMeta.getParticleEffectMeta());
        petMeta.setParticleId(petMeta.getParticleEffectMeta().getId());
        petMeta.setPlayerId(petMeta.getPlayerMeta().getId());
        PetDataManager.this.petDataController.store(petMeta);
    }

    public void remove(PetMeta petMeta) {
        if (petMeta == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        if (Thread.currentThread().getId() == this.mainThreadId)
            throw new RuntimeException("This method has to be accessed asynchronously.");
        this.petDataController.remove(petMeta);
        this.particleEffectMetaController.remove(petMeta.getParticleEffectMeta());
    }

    public PetMeta getPetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if (Thread.currentThread().getId() == this.mainThreadId)
            throw new RuntimeException("This method has to be accessed asynchronously.");
        if (PetBlocksApi.getPetBlock(player) != null)
            return (PetMeta) PetBlocksApi.getPetBlock(player).getPetMeta();
        final PetMeta petMeta = this.petDataController.getByPlayer(player);
        if (petMeta == null)
            return null;
        petMeta.setParticleEffectMeta(this.particleEffectMetaController.getById(petMeta.getParticleId()));
        petMeta.setPlayerMeta(this.playerMetaController.getById(petMeta.getPlayerId()));
        return petMeta;
    }

    public boolean hasPetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if (Thread.currentThread().getId() == this.mainThreadId)
            throw new RuntimeException("This method has to be accessed asynchronly.");
        return this.petDataController.hasEntry(player);
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.filter.close();
        this.petDataController.close();
        this.playerMetaController.close();
        this.particleEffectMetaController.close();
        this.pages.clear();
        this.inventories.clear();
    }
}
