package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.legacy.business.annotation.YamlSerialize
import com.github.shynixn.petblocks.api.legacy.business.enumeration.AIType
import com.github.shynixn.petblocks.api.legacy.persistence.entity.AIInventory
import com.github.shynixn.petblocks.core.logic.business.serializer.ItemStackSerializer

class AIInventoryEntity : AIBaseEntity(), AIInventory {
    /**
     * Name of the type.
     */
    override var type: String = AIType.INVENTORY.type

    /**
     * Ordered itemStacks in the inventory.
     */
    @YamlSerialize(value = "items", orderNumber = 1, customserializer = ItemStackSerializer::class)
    override var items: MutableList<Any?> = ArrayList()
}
