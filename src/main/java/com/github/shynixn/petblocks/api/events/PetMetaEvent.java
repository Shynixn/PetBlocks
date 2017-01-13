package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.lib.SpigotEvent;
import org.bukkit.entity.Player;

public class PetMetaEvent extends SpigotEvent {
    private final PetMeta meta;

    public PetMetaEvent(PetMeta meta) {
        if (meta == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        this.meta = meta;
    }

    public PetMeta getPetMeta() {
        return this.meta;
    }

    public Player getPlayer() {
        return this.meta.getOwner();
    }
}
