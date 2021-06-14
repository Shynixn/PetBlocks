package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.service.EntityRegistrationService

/**
 * This class handles registering the custom Entities into Minecraft.
 */
class EntityRegistration117R1ServiceImpl : EntityRegistrationService {

    /**
     * Registers a new customEntity Clazz as the given [entityType].
     * Does nothing if the class is already registered.
     */
    override fun <C> register(customEntityClazz: C, entityType: EntityType) {
    }

    /**
     * Clears all resources this service has allocated and reverts internal
     * nms changes.
     */
    override fun clearResources() {
    }
}
