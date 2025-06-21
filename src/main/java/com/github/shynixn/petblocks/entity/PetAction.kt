package com.github.shynixn.petblocks.entity

import com.github.shynixn.petblocks.enumeration.PetActionCommandLevelType
import com.github.shynixn.petblocks.enumeration.PetActionType

class PetAction {
    // region Common

    /**
     * Name for easier debugging.
     */
    var name: String = ""

    /**
     * Action to use for execution.
     * Defaults to command.
     */
    var type: PetActionType = PetActionType.COMMAND

    /**
     * Optional condition.
     */
    var condition: PetActionCondition? = null

    /**
     * Flag to print all parameters.
     */
    var debug: Boolean = false

    /**
     * Optional permission check for this action.
     */
    var permission: String? = null

    // endregion

    // region Command

    /**
     * Command level.
     */
    var level: PetActionCommandLevelType = PetActionCommandLevelType.PLAYER

    /**
     * Commands.
     */
    var run: List<String> = emptyList()

    // endregion

    // region Delay

    /**
     * Amount of ticks to wait.
     */
    var ticks: Int = 1

    // endregion

    // region JavaScript

    /**
     * JavaScript to execute.
     */
    var js: String? = null

    /**
     * Name of the variable to store data.
     */
    var variable: String? = null

    /**
     * Initial value of the variable.
     */
    var initial: String? = null

    // region JavaScript
}
