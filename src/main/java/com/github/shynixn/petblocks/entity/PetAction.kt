package com.github.shynixn.petblocks.entity

import com.github.shynixn.petblocks.enumeration.PetActionCommandLevelType
import com.github.shynixn.petblocks.enumeration.PetActionType

class PetAction {
    /**
     * Name for easier debugging.
     */
    var name: String = ""

    /**
     * Action to use for execution.
     * Defaults to command.
     */
    var actionType: PetActionType = PetActionType.COMMAND

    /**
     * Command level.
     */
    var level: PetActionCommandLevelType = PetActionCommandLevelType.PLAYER

    /**
     * Optional Debug string.
     */
    var debug: String? = null

    /**
     * Optional javascript condition.
     */
    var condition: String? = null

    /**
     * Commands.
     */
    var run: List<String> = emptyList()

    /**
     * Amount of ticks to wait.
     */
    var ticks: Int = 0
}
