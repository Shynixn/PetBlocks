package com.github.shynixn.petblocks.impl.physic

import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.enumeration.PetVisibility
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
    renderDistanceBlocks: Int = 70,
    private val pet: Pet
) : AutoCloseable {
    var lastTimeRenderUpdate = 0L

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

    /**
     * Forces to perform rendering logic immediately.
     */
    fun forceMinecraftTick() {
        lastTimeRenderUpdate = 0L
        tickMinecraft()
    }

    fun tickMinecraft() {
        val currentTime = Date().time

        if (currentTime - lastTimeRenderUpdate >= renderVisibilityUpdateMs) {
            lastTimeRenderUpdate = currentTime

            val players = HashSet<Player>()
            val location = physicsComponent.position.toLocation()
            for (player in location.world!!.players) {
                if (pet.visibility == PetVisibility.NOBODY) {
                    continue
                }

                if (player.location.distanceSquared(location) <= renderVisibilityDistance) {
                    if (!visiblePlayers.contains(player)) {
                        if (pet.visibility == PetVisibility.ALL) {
                            onSpawnMinecraft.forEach { e -> e.invoke(player, location) }
                        } else if (pet.visibility == PetVisibility.OWNER && player == pet.player) {
                            onSpawnMinecraft.forEach { e -> e.invoke(player, location) }
                        }

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
