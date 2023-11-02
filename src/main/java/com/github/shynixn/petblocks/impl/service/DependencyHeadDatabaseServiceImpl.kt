package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mcutils.common.toItem
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.enumeration.Permission
import com.google.inject.Inject
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.arcaniax.hdb.api.PlayerClickHeadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class DependencyHeadDatabaseServiceImpl @Inject constructor(
    private val petService: PetService,
    private val plugin: Plugin
) : DependencyHeadDatabaseService, Listener {
    private val headDatabaseApi = HeadDatabaseAPI()
    private val random = Random()

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
     * When a player clicks on a head in the HDB Gui.
     */
    @EventHandler
    fun onHeadClickEvent(event: PlayerClickHeadEvent) {
        val player = event.player

        if (!player.hasPermission(Permission.HEADDATABASE_INVENTORY_TO_PET.text)) {
            return
        }

        plugin.launch {
            try {
                val item = event.head.toItem()
                val pets = petService.getPetsFromPlayer(player)
                val nbtString = getNbtString(item.base64EncodedSkinUrl!!)
                for (pet in pets) {
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
