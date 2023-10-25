package com.github.shynixn.petblocks.contract

interface ScriptService {
    /**
     * Evaluates a Javascript expression to a boolean expression.
     */
    fun evaluate(expression: String): Boolean
}
