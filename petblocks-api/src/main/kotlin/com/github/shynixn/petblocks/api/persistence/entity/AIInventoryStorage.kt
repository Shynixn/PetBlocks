package com.github.shynixn.petblocks.api.persistence.entity

interface AIInventoryStorage {
    /**
     * Ordered itemStacks in the inventory.
     */
    val items: MutableList<Any>
}