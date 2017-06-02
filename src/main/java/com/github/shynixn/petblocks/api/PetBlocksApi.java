package com.github.shynixn.petblocks.api;

import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.business.logic.business.PetDataManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PetBlocksApi {
    private static PetBlockManager petBlockManager;
    private static PetDataManager dataManager;

    private static void init(Plugin plugin) {
        dataManager = new PetDataManager(plugin);
        petBlockManager = new PetBlockManager(dataManager, plugin);
    }

    private static void dispose(Plugin plugin) {
        if (petBlockManager != null) {
            petBlockManager.killAll();
        }
    }

    public static void setPetBlock(Player player, PetMeta petMeta) {
        setPetBlock(player, petMeta,0);
    }

    public static void setPetBlock(Player player, PetMeta petMeta, int delay) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if (petMeta == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        if (!petMeta.getOwner().equals(player))
            throw new IllegalArgumentException("PetMeta cannot be applied to this player!");
        if (delay < 0)
            throw new IllegalArgumentException("Delay cannot be less than 0.");
        petBlockManager.setPetBlock(player, petMeta, delay);
    }

    public static void removePetBlock(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        petBlockManager.removePetBlock(player);
    }

    public static boolean hasPetBlock(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        return petBlockManager.hasPetBlock(player);
    }

    public static PetBlock getPetBlock(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if (hasPetBlock(player))
            return petBlockManager.getPetBlock(player);
        return null;
    }

    public static com.github.shynixn.petblocks.api.persistence.entity.PetMeta getPetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        return dataManager.getPetMeta(player);
    }

    public static void removePetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if (dataManager.hasPetMeta(player))
            dataManager.remove(getPetMeta(player));
    }

    public static void setPetMeta(Player player, PetType petType) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if (petType == null)
            throw new IllegalArgumentException("Type cannot be null!");
        final PetMeta meta = dataManager.createPetMeta(player, petType);
        dataManager.persist((com.github.shynixn.petblocks.api.persistence.entity.PetMeta)meta);
    }

    public static void persistPetMeta(PetMeta petMeta) {
        if (petMeta == null)
            throw new IllegalArgumentException("Meta cannot be null!");
        dataManager.persist((com.github.shynixn.petblocks.api.persistence.entity.PetMeta)petMeta);
    }

    public static boolean hasPetMeta(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        return dataManager.hasPetMeta(player);
    }
}
