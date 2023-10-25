package com.github.shynixn.petblocks.contract

interface ScriptService {
    /**
     * Evaluates a Javascript expression.
     */
    fun evaluate(expression: String): Any?
}
