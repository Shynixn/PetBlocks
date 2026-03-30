package com.github.shynixn.petblocks.contract

interface JavaScriptService {
    /**
     * Evaluates a Javascript expression.
     */
    fun evaluate(expression: String): Any?
}