package com.github.shynixn.petblocks.business.bukkit.dependencies.supervanish;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.lib.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
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
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
            if (!VisibilityManager.this.hiddenValues.containsKey(petBlock))
                VisibilityManager.this.hiddenValues.put(petBlock, new ArrayList<>());
            for (final Player player : players) {
                try {
                    VisibilityManager.this.sendDestroyPacket(player, (Entity) petBlock.getArmorStand());
                    VisibilityManager.this.sendDestroyPacket(player, (Entity) petBlock.getEngineEntity());
                    VisibilityManager.this.hiddenValues.get(petBlock).add(player);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to send visibilty packet.", e);
                }
            }
        });
    }

    void showPetBlock(final PetBlock petBlock, final Player... players) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PetBlocksPlugin.class), () -> {
            if (VisibilityManager.this.hiddenValues.containsKey(petBlock)) {
                for (final Player player : players) {
                    VisibilityManager.this.sendSpawnPacket(player, (Entity) petBlock.getArmorStand());
                    VisibilityManager.this.sendSpawnPacket(player, (Entity) petBlock.getEngineEntity());
                    VisibilityManager.this.hiddenValues.get(petBlock).add(player);
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

    private void sendDestroyPacket(Player player, Entity entity) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        final Class<?> clazz = NMSRegistry.findClassFromVersion("net.minecraft.server.VERSION.PacketPlayOutEntityDestroy");
        final Object packet = ReflectionUtils.invokeConstructor(clazz, new Class[]{Integer[].class}, new Object[]{new int[]{entity.getEntityId()}});
        this.sendPacket(player, entity, packet);
    }

    private Object getEntityEntity(Entity entity) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Object craftPlayer = NMSRegistry.findClassFromVersion("org.bukkit.craftbukkit.VERSION.entity.CraftEntity").cast(entity);
        return ReflectionUtils.invokeMethodByObject(craftPlayer, "getHandle", new Class[0], new Object[0]);
    }

    private void sendPacket(Player player, Entity entity, Object packet) {
        try {
            final Object craftPlayer = NMSRegistry.findClassFromVersion("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer").cast(player);
            final Object entityPlayer = ReflectionUtils.invokeMethodByObject(craftPlayer, "getHandle", new Class[0], new Object[0]);
            final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            ReflectionUtils.invokeMethodByObject(playerConnection, "sendPacket", new Class[]{packet.getClass()}, new Object[]{packet});
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot send packet " + packet.getClass().getSimpleName() + '.', e);
        }
    }
}
