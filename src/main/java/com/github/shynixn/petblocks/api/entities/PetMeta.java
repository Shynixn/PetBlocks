package com.github.shynixn.petblocks.api.entities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.Serializable;

public interface PetMeta extends Serializable {
    Movement getMovementType();

    void setMovementType(Movement movementType);

    PetType getType();

    Player getOwner();

    boolean isUnbreakable();

    void setUnbreakable(boolean unbreakable);

    Particle getParticleEffect();

    void setSoundsEnabled(boolean enabled);

    boolean isSoundsEnabled();

    void setParticleEffect(Particle particle);

    void setHidden(boolean enabled);

    boolean isHidden();

    String getSkin();

    String getDisplayName();

    String getHeadDisplayName();

    void setHeadDisplayName(String headDisplayName);

    String[] getHeadLore();

    void setHeadLore(String[] headLore);

    void setPetType(PetType petType);

    void setDisplayName(String name);

    MoveType getMoveType();

    void setMoveType(MoveType moveType);

    Material getSkinMaterial();

    short getSkinDurability();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    void setSkin(Material material, short durability, String skin);

    void setAge(Age age);

    void setAgeInTicks(int ticks);

    int getAgeInTicks();

    Age getAge();
}
