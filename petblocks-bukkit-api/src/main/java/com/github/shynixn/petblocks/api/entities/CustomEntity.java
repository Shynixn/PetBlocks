package com.github.shynixn.petblocks.api.entities;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Shynixn
 */
@Deprecated
public interface CustomEntity {
    LivingEntity getSpigotEntity();

    void spawn(Location location);
}
