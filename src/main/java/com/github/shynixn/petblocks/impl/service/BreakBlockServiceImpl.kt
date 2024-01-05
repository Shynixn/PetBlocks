package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mcutils.common.CancellationToken
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.packet.PacketOutBlockBreakAnimation
import com.github.shynixn.petblocks.contract.BreakBlockService
import com.github.shynixn.petblocks.enumeration.DropType
import com.github.shynixn.petblocks.impl.PetEntityImpl
import com.google.inject.Inject
import kotlinx.coroutines.delay
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin

class BreakBlockServiceImpl @Inject constructor(private val plugin: Plugin, private val packetService: PacketService) :
    BreakBlockService {
    /**
     * Breaks the given block.
     */
    override fun breakBlock(
        petEntity: PetEntityImpl,
        timeToBreakTicks: Int,
        dropTypes: List<DropType>,
        token: CancellationToken
    ) {
        val locateBlock = petEntity.findTargetBlock(2.0) ?: return

        petEntity.isBreakingBlock = true

        plugin.launch {
            val isCancelled = showAnimation(locateBlock, petEntity, timeToBreakTicks, token)

            if (isCancelled) {
                petEntity.isBreakingBlock = false
                return@launch
            }

            petEntity.isBreakingBlock = true
            val blockType = locateBlock.type
            val blockLocation = locateBlock.location

            for (dropType in dropTypes) {
                if (dropType == DropType.VANISH) {
                    locateBlock.type = Material.AIR
                    break
                }

                if (dropType == DropType.DROP) {
                    locateBlock.breakNaturally()
                    break
                }

                if (dropType == DropType.SEND_TO_OWNER_INVENTORY) {
                    val addItemResult = petEntity.pet.player.inventory.addItem(*locateBlock.drops.toTypedArray())
                    val couldNotAddItems = addItemResult.values.toList()

                    if (couldNotAddItems.isEmpty()) {
                        locateBlock.type = Material.AIR
                        break
                    }
                }
            }

            petEntity.pet.player.playEffect(blockLocation, Effect.STEP_SOUND, blockType)
            petEntity.isBreakingBlock = false
        }
    }

    private suspend fun showAnimation(
        block: Block,
        petEntity: PetEntityImpl,
        timeToBreakTicks: Int, token: CancellationToken
    ): Boolean {
        val playerComponent = petEntity.playerComponent
        val entityComponent = petEntity.entityComponent

        for (i in 0 until 9) {
            if (token.isCancelled) {
                petEntity.isBreakingBlock = false
                for (player in playerComponent.visiblePlayers) {
                    packetService.sendPacketOutBlockBreakAnimation(player, PacketOutBlockBreakAnimation().also {
                        it.entityId = entityComponent.entityId
                        it.progress = -1
                        it.location = block.location
                    })
                }
                return true
            }

            petEntity.isBreakingBlock = true

            if (timeToBreakTicks < 9) {
                for (player in playerComponent.visiblePlayers) {
                    packetService.sendPacketOutBlockBreakAnimation(player, PacketOutBlockBreakAnimation().also {
                        it.entityId = entityComponent.entityId
                        it.progress = 9
                        it.location = block.location
                    })
                }

                break
            }

            for (player in playerComponent.visiblePlayers) {
                packetService.sendPacketOutBlockBreakAnimation(player, PacketOutBlockBreakAnimation().also {
                    it.entityId = entityComponent.entityId
                    it.progress = i
                    it.location = block.location
                })
            }

            val ticksToWait = timeToBreakTicks / 9 // 9 is the animation part
            delay(ticksToWait.ticks)
        }

        return false
    }

}
