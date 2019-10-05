package com.github.shynixn.petblocks.api.persistence.entity

interface AIInventory : AIBase {
    /**
     * Amount of items being accessible of the inventory storage.
     */
    val slotsAmount: Int
}