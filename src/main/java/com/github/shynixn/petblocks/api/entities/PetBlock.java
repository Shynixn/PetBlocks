package com.github.shynixn.petblocks.api.entities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public interface PetBlock {
    void remove();

    void ride(Player player);

    void wear(Player player);

    void setDisplayName(String name);

    String getDisplayName();

    void respawn();

    boolean isDead();

    ArmorStand getArmorStand();

    void setSkin(String skin);

    void setSkin(Material material, byte data);

    LivingEntity getMovementEntity();

    void eject(Player player);

    void damage(double amount);

    void teleport(Location location);

    void teleport(Entity entity);

    void setDieing();

    Player getOwner();

    void jump();

    void launch(Vector vector);

    Location getLocation();

    PetMeta getPetMeta();

    void teleportWithOwner(Location location);

    boolean isDieing();

    void refreshHeadMeta();
}
