package com.github.shynixn.petblocks.api.legacy.business.proxy

interface ArmorstandPetProxy : EntityPetProxy {
    /**
     * Sets the helmet item stack securely if
     * blocked by the NMS call.
     */
    fun <I> setHelmetItemStack(item: I)

    /**
     * Sets the boots item stack securely if
     * blocked by the NMS call.
     */
    fun <I> setBootsItemStack(item: I)
}
