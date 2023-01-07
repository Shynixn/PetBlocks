package com.github.shynixn.petblocks.bukkit.service

interface ConditionService{
    /**
     * Evaluates a Javascript expression to a boolean expression.
     */
    suspend fun evaluate(expression: String): Boolean
}
