package com.github.shynixn.petblocks.entity

import com.github.shynixn.petblocks.enumeration.PetActionConditionType

class PetActionCondition {
    /**
     * Type of condition.
     */
    var type: PetActionConditionType = PetActionConditionType.NONE

    /**
     * Left Parameter.
     */
    var left: String? = null

    /**
     * Right parameter.
     */
    var right: String? = null

    /**
     * JavaScript to execute.
     */
    var js: String? = null
}
