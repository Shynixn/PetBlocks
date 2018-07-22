package com.github.shynixn.petblocks.bukkit.logic;

import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.BukkitDBContext;
import com.github.shynixn.petblocks.bukkit.logic.business.controller.BukkitPetBlockRepository;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.LoggingBridge;
import com.github.shynixn.petblocks.bukkit.logic.persistence.controller.BukkitPetDataRepository;
import com.github.shynixn.petblocks.bukkit.logic.persistence.controller.BukkitPlayerDataRepository;
import com.github.shynixn.petblocks.core.logic.business.entity.DbContext;
import com.github.shynixn.petblocks.core.logic.persistence.controller.ParticleEffectDataRepository;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class Factory {

    public static DbContext connectionContext;
    private static PetBlockController petBlockControllerCache;

    public static PlayerMetaController<Player> createPlayerDataController() {
        return new BukkitPlayerDataRepository(connectionContext);
    }

    public static ParticleEffectMetaController createParticleEffectController() {
        return new ParticleEffectDataRepository(connectionContext, new LoggingBridge(PetBlocksPlugin.logger()));
    }

    public static PetBlockController<Player> createPetBlockController() {
        if (petBlockControllerCache == null) {
            petBlockControllerCache = new BukkitPetBlockRepository();
        }

        return petBlockControllerCache;
    }

    public static PetMetaController<Player> createPetDataController() {
        return new BukkitPetDataRepository(createPlayerDataController(), createParticleEffectController(), connectionContext);
    }

    public static void initialize(Plugin plugin) {
        connectionContext = new BukkitDBContext(plugin, new LoggingBridge(plugin.getLogger()));
    }

    public static void disable() {
        if (connectionContext == null)
            return;
        try {
            connectionContext.close();
        } catch (final Exception e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to close connection context.");
        }
        connectionContext = null;
    }
}
