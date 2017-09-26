package com.github.shynixn.petblocks.bukkit.dependencies.clearlag;

import com.github.shynixn.petblocks.bukkit.lib.SimpleListener;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClearLagListener extends SimpleListener {
    public ClearLagListener(JavaPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityRemoveEvent(EntityRemoveEvent event) {
        for (final Entity entity : event.getEntityList().toArray(new Entity[event.getEntityList().size()])) {
            if (this.isPet(entity)) {
                event.getEntityList().remove(entity);
            }
        }
    }

    private boolean isPet(Entity entity) {
        if (entity instanceof ArmorStand) {
            final ArmorStand stand = (ArmorStand) entity;
            final int xidentifier = (int) stand.getBodyPose().getZ();
            final int identifier = (int) stand.getRightArmPose().getX();
            if (xidentifier == 2877 && identifier == 2877) {
                return true;
            }
        } else if (entity instanceof Rabbit && entity.getCustomName() != null && entity.getCustomName().equals("PetBlockIdentifier")) {
            return true;
        }
        return false;
    }
}
