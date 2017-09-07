package com.github.shynixn.petblocks.api.entities;

/**
 * Created by Shynixn
 */
@Deprecated
public enum Movement {
    CRAWLING,
    HOPPING;

    public static Movement getMovementFromName(String name) {
        for (final Movement type : Movement.values()) {
            if (type.name().equalsIgnoreCase(name))
                return type;
        }
        return null;
    }
}
