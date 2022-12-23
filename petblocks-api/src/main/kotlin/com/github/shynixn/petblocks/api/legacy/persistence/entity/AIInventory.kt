package com.github.shynixn.petblocks.api.legacy.persistence.entity

interface AIInventory : AIBase {
    /**
     * Ordered itemStacks in the inventory.
     */
    var items: MutableList<Any?>
}
