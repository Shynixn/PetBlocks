package com.github.shynixn.petblocks.api.entities;

import com.github.shynixn.petblocks.lib.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.Serializable;

public interface Particle extends Serializable {
    void play(Location location);

    void play(Location location, Player... players);

    ParticleEffect getEffect();

    double getX();

    double getY();

    double getZ();

    double getSpeed();

    int getAmount();

    Material getMaterial();

    int getMaterialId();

    byte getData();
}
