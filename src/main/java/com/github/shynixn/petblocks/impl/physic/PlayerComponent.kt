package com.github.shynixn.petblocks.impl.physic

import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.common.toLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class PlayerComponent(
    private val physicsComponent: MathComponent,
    /**
     * Render visibility updates.
     */
    var renderVisibilityUpdateMs: Int = 5000,

    /**
     * Render distance blocks.
     */
    renderDistanceBlocks: Int = 70
) : PhysicComponent {
    private var lastTimeRenderUpdate = 0L

    // Multiplied to save performance later.
    private val renderVisibilityDistance = renderDistanceBlocks * renderDistanceBlocks
    var visiblePlayers = HashSet<Player>()

    /**
     * Functions being called when a player comes in range.
     */
    val onSpawnMinecraft: MutableList<(Player, Location) -> Unit> = arrayListOf()

    /**
     * Functions being called when a player moves out of range.
     */
    val onRemoveMinecraft: MutableList<(Player, Location) -> Unit> = arrayListOf()

    override fun tickMinecraft() {
        val currentTime = Date().time

        if (currentTime - lastTimeRenderUpdate >= renderVisibilityUpdateMs) {
            lastTimeRenderUpdate = currentTime

            val players = HashSet<Player>()
            val location = physicsComponent.position.toLocation()
            for (player in location.world!!.players) {
                if (player.location.distanceSquared(location) <= renderVisibilityDistance) {
                    if (!visiblePlayers.contains(player)) {
                        onSpawnMinecraft.forEach { e -> e.invoke(player, location) }
                        visiblePlayers.add(player)
                    }

                    players.add(player)
                }
            }

            for (player in visiblePlayers.toTypedArray()) {
                if (!players.contains(player)) {
                    onRemoveMinecraft.forEach { e -> e.invoke(player, location) }
                    visiblePlayers.remove(player)
                }
            }
        }

        if (visiblePlayers.size <= 0) {
            return
        }
    }

    /**
     * Closes the component.
     */
    override fun close() {
        val location = physicsComponent.position.toLocation()
        for (player in visiblePlayers) {
            onRemoveMinecraft.forEach { e -> e.invoke(player, location) }
        }
        visiblePlayers.clear()
        onRemoveMinecraft.clear()
    }
}
