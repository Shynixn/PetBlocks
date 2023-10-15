package com.github.shynixn.petblocks.contract

interface DependencyPlaceholderApiService{
    /**
     * Registers the placeholder hook if it is not already registered.
     */
    fun registerListener()
}
