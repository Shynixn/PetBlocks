@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.core.logic.business.extension.stripChatColors
import com.github.shynixn.petblocks.core.logic.business.proxy.AICreationProxyImpl
import com.github.shynixn.petblocks.core.logic.business.proxy.PathfinderProxyImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.*
import com.google.inject.Inject
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.ai.EntityAISwimming
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.gamemode.GameModes
import org.spongepowered.api.world.World

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class EntityServiceImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val proxyService: ProxyService,
    private val entityRegistrationService: EntityRegistrationService,
    private val yamlSerializationService: YamlSerializationService,
    private val loggingService: LoggingService,
    private val aiService: AISerializationService,
    private val petService: PetService,
    private val afraidOfWaterService: AfraidOfWaterService,
    private val navigationService: NavigationService,
    private val soundService: SoundService,
    private val version: Version
) : EntityService {

    private var registered = false

    init {
        this.register<AIAfraidOfWater>(AIType.AFRAID_OF_WATER) { pet, aiBase ->
            val pathfinder = PathfinderProxyImpl(loggingService, aiBase)
            val hitBox = pet.getHitBoxLivingEntity<Living>().get()
            val owner = pet.getPlayer<Player>()
            var milliseconds = 0L

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isRemoved && owner.gameMode != GameModes.SPECTATOR && afraidOfWaterService.isPetInWater(pet)
            }

            pathfinder.onExecute = {
                afraidOfWaterService.escapeWater(pet, aiBase)
            }

            pathfinder.shouldGoalContinueExecuting = {
                val current = System.currentTimeMillis()
                val difference = current - milliseconds

                milliseconds = current
                difference < 2000
            }

            pathfinder
        }

        this.register<AIAmbientSound>(AIType.AMBIENT_SOUND) { pet, aiBase ->
            val pathfinder = PathfinderProxyImpl(loggingService, aiBase)
            val hitBox = pet.getHitBoxLivingEntity<Living>().get()
            val owner = pet.getPlayer<Player>()

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isRemoved && owner.gameMode != GameModes.SPECTATOR
            }

            pathfinder.onExecute = {
                if (pet.meta.soundEnabled) {
                    val value = Math.random()

                    if (value > 0.98) {
                        soundService.playSound(hitBox.transform, aiBase.sound, owner)
                    }
                }
            }

            pathfinder
        }

        this.register<AICarry>(AIType.CARRY)
        this.register<AIFeeding>(AIType.FEEDING)

        this.register<AIFloatInWater>(AIType.FLOAT_IN_WATER) { pet, _ ->
            EntityAISwimming(pet.getHitBoxLivingEntity<Living>().get() as EntityLiving)
        }

        this.register<AIFlying>(AIType.FLYING)
        this.register<AIFlyRiding>(AIType.FLY_RIDING)

        this.register<AIFollowBack>(AIType.FOLLOW_BACK) { pet, aiBase ->
            val pathfinder = PathfinderProxyImpl(loggingService, aiBase)
            val owner = pet.getPlayer<Player>()

            pathfinder.shouldGoalBeExecuted = {
                !pet.isDead && owner.gameMode != GameModes.SPECTATOR
            }

            pathfinder.onExecute = {
                val location = owner.transform
                val targetLocation = PositionEntity(
                    (location.x + (-1 * Math.cos(Math.toRadians(location.yaw + 90.0)))),
                    location.y,
                    location.z + (-1 * Math.sin(Math.toRadians(location.yaw + 90.0))),
                    location.yaw,
                    location.pitch, location.extent.name)

                pet.teleport(targetLocation)
            }

            pathfinder
        }

        this.register<AIFollowOwner>(AIType.FOLLOW_OWNER) { pet, aiBase ->
            var lastLocation: Transform<World>? = null
            val pathfinder = PathfinderProxyImpl(loggingService, aiBase)
            val owner = pet.getPlayer<Player>()
            val hitBox = pet.getHitBoxLivingEntity<Living>().get()

            pathfinder.shouldGoalContinueExecuting = {
                when {
                    owner.transform.distance(hitBox.transform) > aiBase.maxRange -> {
                        pet.teleport(owner.transform)
                        false
                    }

                    owner.transform.distance(hitBox.transform) < aiBase.distanceToOwner -> false
                    else -> !(lastLocation != null && lastLocation!!.distance(owner.transform) > 2)
                }
            }

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isRemoved && owner.gameMode != GameModes.SPECTATOR && owner.transform.distance(hitBox.transform) >= aiBase.distanceToOwner
            }

            pathfinder.onStopExecuting = {
                navigationService.clearNavigation(pet)
            }

            pathfinder.onStartExecuting = {
                lastLocation = owner.transform.toPosition().toTransform()

                val speed = if (pet.meta.aiGoals.firstOrNull { p -> p is AIHopping } != null) {
                    aiBase.speed + 1.0
                } else {
                    aiBase.speed
                }

                navigationService.navigateToLocation(pet, owner.transform, speed)
            }

            pathfinder
        }

        this.register<AIGroundRiding>(AIType.GROUND_RIDING)
        this.register<AIHopping>(AIType.HOPPING)
        this.register<AIWalking>(AIType.WALKING)
        this.register<AIWearing>(AIType.WEARING)
    }

    /**
     * Checks the entity collection for invalid pet entities and removes them.
     */
    override fun <E> cleanUpInvalidEntities(entities: Collection<E>) {
        for (entity in entities) {
            if (entity !is Living) {
                continue
            }

            if (petService.findPetByEntity(entity) != null) {
                continue
            }

            // Pets of PetBlocks hide a marker in the boots of every entity. This marker is persistent even on server crashes.
            if (entity is EntityLiving && entity.getItemStackFromSlot(EntityEquipmentSlot.FEET) != ItemStack.EMPTY) {
                val boots = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET) as org.spongepowered.api.item.inventory.ItemStack

                if (boots.lore != null) {
                    val lore = boots.lore!![0]

                    if (lore.stripChatColors() == "PetBlocks") {
                        try {
                            (entity as Any).javaClass.getDeclaredMethod("deleteFromWorld").invoke(entity)
                        } catch (e: Exception) {
                            entity.remove()
                        }

                        loggingService.info("Removed invalid pet in chunk. Fixed Wrong 'Wrong location'.")
                    }
                }
            }
        }
    }

    /**
     * Spawns a new unManaged petProxy.
     */
    override fun <L> spawnPetProxy(location: L, petMeta: PetMeta): PetProxy {
        this.registerEntitiesOnServer()

        val playerProxy = proxyService.findPlayerProxyObjectFromUUID(petMeta.playerMeta.uuid)
        val designClazz = Class.forName("com.github.shynixn.petblocks.sponge.logic.business.nms.VERSION.NMSPetArmorstand".replace("VERSION", version.bukkitId))

        return (designClazz.getDeclaredConstructor(Player::class.java, PetMeta::class.java)
            .newInstance(playerProxy.handle, petMeta) as NMSPetProxy).proxy
    }

    /**
     * Registers entities on the server when not already registered.
     * Returns true if registered. Returns false when not registered.
     */
    override fun registerEntitiesOnServer(): Boolean {
        if (registered) {
            return true
        }

        val rabbitClazz = Class.forName("com.github.shynixn.petblocks.sponge.logic.business.nms.VERSION.NMSPetRabbit".replace("VERSION", version.bukkitId))
        entityRegistrationService.register(rabbitClazz, EntityType.RABBIT)

        val villagerClazz = Class.forName("com.github.shynixn.petblocks.sponge.logic.business.nms.VERSION.NMSPetVillager".replace("VERSION", version.bukkitId))
        entityRegistrationService.register(villagerClazz, EntityType.RABBIT)

        val batClazz = Class.forName("com.github.shynixn.petblocks.sponge.logic.business.nms.VERSION.NMSPetBat".replace("VERSION", version.bukkitId))
        entityRegistrationService.register(batClazz, EntityType.RABBIT)

        val armorStandClazz = Class.forName("com.github.shynixn.petblocks.sponge.logic.business.nms.VERSION.NMSPetArmorstand".replace("VERSION", version.bukkitId))
        entityRegistrationService.register(armorStandClazz, EntityType.ARMORSTAND)

        registered = true

        return true
    }

    /**
     * Kills the nearest entity of the [player].
     */
    override fun <P> killNearestEntity(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        var distance = 100.0
        var nearest: Entity? = null

        for (entity in player.location.extent.getChunk(player.position.toVector3i()).get().entities) {
            if (entity !is Player && player.location.position.distance(entity.location.position) < distance) {
                distance = player.location.position.distance(entity.location.position)
                nearest = entity
            }
        }

        if (nearest != null) {
            nearest.remove()

            val prefix = configurationService.findValue<String>("messages.prefix")
            player.sendMessage(prefix + "" + ChatColor.GREEN + "You removed entity " + nearest.type + '.'.toString())
        }
    }

    /**
     * Registers a default ai type.
     */
    private fun <A : AIBase> register(aiType: AIType, function: ((PetProxy, A) -> Any)? = null) {
        val clazz = Class.forName("com.github.shynixn.petblocks.core.logic.persistence.entity.CUSTOMEntity".replace("CUSTOM", aiType.aiClazz.java.simpleName))
        aiService.registerSerializationProxy(aiType.type, AICreationProxyImpl(yamlSerializationService, clazz.kotlin, function as ((PetProxy, AIBase) -> Any)?))
    }
}