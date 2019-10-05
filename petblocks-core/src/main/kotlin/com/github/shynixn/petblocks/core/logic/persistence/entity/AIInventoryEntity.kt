package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.annotation.YamlSerialize
import com.github.shynixn.petblocks.api.persistence.entity.AIInventory

class AIInventoryEntity : AIBaseEntity(), AIInventory {
    /**
     * Name of the type.
     */
    override var type: String = "inventory"

    /**
     * Amount of slots the player has available.
     */
    @YamlSerialize(value = "slots-amount", orderNumber = 1)
    override var slotsAmount: Int = 40
}