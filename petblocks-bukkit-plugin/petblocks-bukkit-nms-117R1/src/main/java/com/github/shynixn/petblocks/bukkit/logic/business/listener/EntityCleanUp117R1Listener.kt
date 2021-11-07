package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.EntityService
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent

class EntityCleanUp117R1Listener @Inject constructor(private val entityService: EntityService) : Listener {
    /**
     * Gets called when entities are requested to load.
     */
    @EventHandler
    fun onEntityLoad(event: EntitiesLoadEvent) {
        entityService.cleanUpInvalidEntities(event.chunk.entities.toList())
    }

    /**
     * Gets called when entities are requested to unload.
     */
    @EventHandler
    fun onEntityUnload(event: EntitiesUnloadEvent) {
        entityService.cleanUpInvalidEntities(event.chunk.entities.toList())
    }
}
