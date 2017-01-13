package com.github.shynixn.petblocks.api.entities;

public enum MoveType {
    FLYING,
    WALKING;

    public static MoveType getMoveTypeFromName(String name) {
        for (final MoveType type : MoveType.values()) {
            if (type.name().equalsIgnoreCase(name))
                return type;
        }
        return null;
    }
}
