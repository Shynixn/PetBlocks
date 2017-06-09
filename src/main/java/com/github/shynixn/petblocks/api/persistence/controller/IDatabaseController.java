package com.github.shynixn.petblocks.api.persistence.controller;

public interface IDatabaseController<T> extends IController<T> {
    /**
     * Returns the item of the given id
     *
     * @param id id
     * @return item
     */
    T getById(long id);
}