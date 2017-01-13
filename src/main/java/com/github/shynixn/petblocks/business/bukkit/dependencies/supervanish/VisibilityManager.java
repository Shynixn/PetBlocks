package com.github.shynixn.petblocks.business.bukkit.dependencies.supervanish;

import com.github.shynixn.petblocks.lib.AsyncRunnable;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.lib.ReflectionLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */
class VisibilityManager {
    private final Map<PetBlock, List<Player>> hiddenValues = new HashMap<>();

    void hidePetBlock(final PetBlock petBlock, final Player... players) {
        AsyncRunnable.toAsynchroneThread(new AsyncRunnable() {
            @Override
            public void run() {
                if (!VisibilityManager.this.hiddenValues.containsKey(petBlock))
                    VisibilityManager.this.hiddenValues.put(petBlock, new ArrayList<Player>());
                for (final Player player : players) {
                    VisibilityManager.this.sendDestroyPacket(player, petBlock.getArmorStand());
                    VisibilityManager.this.sendDestroyPacket(player, petBlock.getMovementEntity());
                    VisibilityManager.this.hiddenValues.get(petBlock).add(player);
                }
            }
        });
    }

    void showPetBlock(final PetBlock petBlock, final Player... players) {
        AsyncRunnable.toAsynchroneThread(new AsyncRunnable() {
            @Override
            public void run() {
                if (VisibilityManager.this.hiddenValues.containsKey(petBlock)) {
                    for (final Player player : players) {
                        VisibilityManager.this.sendSpawnPacket(player, petBlock.getArmorStand());
                        VisibilityManager.this.sendSpawnPacket(player, petBlock.getMovementEntity());
                        VisibilityManager.this.hiddenValues.get(petBlock).add(player);
                    }
                }
            }
        });
    }

    public void removePetBlockHidden(PetBlock petBlock) {
        if (this.hiddenValues.containsKey(petBlock)) {
            this.hiddenValues.get(petBlock).clear();
            this.hiddenValues.remove(petBlock);
        }
    }

    private void coolList() {
        for (final PetBlock petBlock1 : this.hiddenValues.keySet().toArray(new PetBlock[this.hiddenValues.size()])) {
            if (petBlock1.isDead())
                this.hiddenValues.remove(petBlock1);
        }
    }

    private void sendSpawnPacket(Player player, Entity entity) {
        // Class<?> clazz = BukkitReflection.getClassFromName("net.minecraft.server.VERSION.PacketPlayOutSpawnEntityLiving");
        //  Object packet = ReflectionLib.invokeConstructor(clazz, getEntityEntity(entity));
        //   sendPacket(player, entity, packet);
    }

    private void sendDestroyPacket(Player player, Entity entity) {
        final Class<?> clazz = ReflectionLib.getClassFromName("net.minecraft.server.VERSION.PacketPlayOutEntityDestroy");
        final Object packet = ReflectionLib.invokeConstructor(clazz, (Object) new int[]{entity.getEntityId()});
        this.sendPacket(player, entity, packet);
    }

    private Object getEntityEntity(Entity entity) {
        final Object craftPlayer = ReflectionLib.getClassFromName("org.bukkit.craftbukkit.VERSION.entity.CraftEntity").cast(entity);
        return ReflectionLib.invokeMethodByObject(craftPlayer, "getHandle");
    }

    private void sendPacket(Player player, Entity entity, Object packet) {
        try {
            final Object craftplayer = ReflectionLib.getClassFromName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer").cast(player);
            final Object entityPlayer = ReflectionLib.invokeMethodByObject(craftplayer, "getHandle");
            final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            ReflectionLib.invokeMethodByObject(playerConnection, "sendPacket", packet);
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot send packet " + packet.getClass().getSimpleName() + ".", e);
        }
    }
}
