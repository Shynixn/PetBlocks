package com.github.shynixn.petblocks.api.entities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.Serializable;

@Deprecated
public interface PetMeta extends Serializable {
    Movement getMovementType();

    void setMovementType(Movement movementType);

    Object getType();

    boolean isUnbreakable();

    void setUnbreakable(boolean unbreakable);

    void setSoundsEnabled(boolean enabled);

    boolean isSoundsEnabled();

    void setHidden(boolean enabled);

    boolean isHidden();

    String getSkin();

    String getDisplayName();

    String getHeadDisplayName();

    void setHeadDisplayName(String headDisplayName);

    String[] getHeadLore();

    void setHeadLore(String[] headLore);

    void setPetType(Object petType);

    void setDisplayName(String name);

    MoveType getMoveType();

    void setMoveType(MoveType moveType);

    Material getSkinMaterial();

    short getSkinDurability();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    void setSkin(Material material, short durability, String skin);

    void setAge(Object age);

    void setAgeInTicks(int ticks);

    int getAgeInTicks();

    Object getAge();

    @Deprecated
    Player getOwner();

    @Deprecated
    void setParticleEffect(Particle particle);

    @Deprecated
    Particle getParticleEffect();
}
