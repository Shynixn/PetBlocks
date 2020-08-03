package com.github.shynixn.petblocks.api.business.proxy

interface ArmorstandPetProxy : EntityPetProxy {
    /**
     * Sets the helmet item stack securely if
     * blocked by the NMS call.
     */
    fun <I> setHelmetItemStack(item: I)
}