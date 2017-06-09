package com.github.shynixn.petblocks.api.persistence.entity;

@FunctionalInterface
public interface Persistenceable {

    /**
     * Returns the id of the object
     * @return id
     */
    long getId();
}
