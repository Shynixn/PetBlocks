package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mcutils.common.toItem
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.PetService
import com.google.inject.Inject
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.arcaniax.hdb.api.PlayerClickHeadEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class DependencyHeadDatabaseServiceImpl @Inject constructor(
    private val petService: PetService,
    private val plugin: Plugin
) : DependencyHeadDatabaseService, Listener {
    private val headDatabaseApi = HeadDatabaseAPI()
    private val random = Random()
    private val applyPlayers = HashMap<Player, String>()

    /**
     * Gets an itemStack from the given headDatabaseId.
     * Returns null if not found.
     */
    override fun getItemStackFromId(headDatabaseId: String): ItemStack? {
        try {
            return headDatabaseApi.getItemHead(headDatabaseId)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Registers the next click of the given player to apply the skin.
     */
    override fun registerPlayerForNextClick(player: Player, petName: String) {
        applyPlayers[player] = petName
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val player = event.player

        if (applyPlayers.containsKey(player)) {
            applyPlayers.remove(player)
        }
    }

    /**
     * When a player clicks on a head in the HDB Gui.
     */
    @EventHandler
    fun onHeadClickEvent(event: PlayerClickHeadEvent) {
        val player = event.player

        if (event.isCancelled) {
            return
        }

        if (!applyPlayers.containsKey(player)) {
            return
        }

        val petName = applyPlayers[player]
        applyPlayers.remove(player)
        player.closeInventory()

        plugin.launch {
            try {
                val item = event.head.toItem()
                val pets = petService.getPetsFromPlayer(player)
                val nbtString = getNbtString(item.base64EncodedSkinUrl!!)
                val pet = pets.firstOrNull { e -> e.name.equals(petName, true) }

                if (pet != null) {
                    val headItem = pet.headItem
                    headItem.typeName = "minecraft:player_head,397"
                    headItem.nbt = nbtString
                    pet.headItem = headItem
                }
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    private fun getNbtString(base64EncodedUrl: String): String {
        val id1 = random.nextInt()
        val id2 = random.nextInt()
        val id3 = random.nextInt()
        val id4 = random.nextInt()
        return "{SkullOwner:{Id:[I;${id1},${id2},${id3},${id4}],Name:\"${id1}\",Properties:{textures:[{Value:\"${base64EncodedUrl}\"}]}}}"
    }
}
