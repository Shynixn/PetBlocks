package com.github.shynixn.petblocks.api.persistence.controller;

import java.util.List;

public interface IController<T> extends AutoCloseable {
    /**
     * Stores a new a item in the repository
     *
     * @param item item
     */
    void store(T item);

    /**
     * Removes an item from the repository
     *
     * @param item item
     */
    void remove(T item);

    /**
     * Returns the amount of items in the repository
    *  @return size
     */
    int size();

    /**
     * Returns all items from the repository as unmodifiableList
     *
     * @return items
    */
    List<T> getAll();
}