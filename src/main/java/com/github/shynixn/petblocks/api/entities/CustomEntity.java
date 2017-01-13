package com.github.shynixn.petblocks.api.entities;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Shynixn
 */
public interface CustomEntity {
    LivingEntity getSpigotEntity();

    void spawn(Location location);
}
