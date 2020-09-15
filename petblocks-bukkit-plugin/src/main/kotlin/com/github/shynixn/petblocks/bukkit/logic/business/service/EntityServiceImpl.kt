@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.bukkit.logic.business.extension.distanceSafely
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.bukkit.logic.business.pathfinder.PathfinderAfraidOfWater
import com.github.shynixn.petblocks.bukkit.logic.business.pathfinder.PathfinderAmbientSound
import com.github.shynixn.petblocks.bukkit.logic.business.pathfinder.PathfinderFollowBack
import com.github.shynixn.petblocks.bukkit.logic.business.pathfinder.PathfinderFollowOwner
import com.github.shynixn.petblocks.core.logic.business.extension.stripChatColors
import com.github.shynixn.petblocks.core.logic.business.pathfinder.PathfinderBuffEffect
import com.github.shynixn.petblocks.core.logic.business.pathfinder.PathfinderParticle
import com.github.shynixn.petblocks.core.logic.business.proxy.AICreationProxyImpl
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.logging.Level

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
    private val proxyService: ProxyService,
    private val entityRegistrationService: EntityRegistrationService,
    private val petService: PetService,
    private val yamlSerializationService: YamlSerializationService,
    private val plugin: Plugin,
    private val version: Version,
    private val aiService: AIService
) : EntityService {

    private var registered = false

    /**
     * Initializes the default ais.
     */
    init {
        this.register<AIAfraidOfWater>(AIType.AFRAID_OF_WATER) { pet, aiBase ->
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>().get()

            PathfinderAfraidOfWater(
                pet,
                aiBase,
                hitBox
            )
        }

        this.register<AIAmbientSound>(AIType.AMBIENT_SOUND) { pet, aiBase ->
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>().get()

            PathfinderAmbientSound(pet, aiBase, hitBox, pet.getPlayer())
        }

        this.register<AIBuffEffect>(AIType.BUFF_EFFECT) { pet, aiBase ->
            PathfinderBuffEffect(aiBase, pet)
        }

        this.register<AICarry>(AIType.CARRY)
        this.register<AIFeeding>(AIType.FEEDING)
        this.register<AIFleeInCombat>(AIType.FLEE_IN_COMBAT)

        this.register<AIFloatInWater>(AIType.FLOAT_IN_WATER) { pet, _ ->
            val getHandleMethod =
                findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")!!

            findClazz("net.minecraft.server.VERSION.PathfinderGoalFloat")
                .getDeclaredConstructor(findClazz("net.minecraft.server.VERSION.EntityInsentient"))
                .newInstance(getHandleMethod.invoke(pet.getHitBoxLivingEntity<LivingEntity>().get()))
        }

        this.register<AIFlying>(AIType.FLYING)
        this.register<AIFlyRiding>(AIType.FLY_RIDING)

        this.register<AIFollowBack>(AIType.FOLLOW_BACK) { pet, aiBase ->
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>().get()

            PathfinderFollowBack(pet, aiBase, hitBox, pet.getPlayer())
        }

        this.register<AIFollowOwner>(AIType.FOLLOW_OWNER) { pet, aiBase ->
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>().get()

            PathfinderFollowOwner(pet, aiBase, hitBox, pet.getPlayer())
        }

        this.register<AIGroundRiding>(AIType.GROUND_RIDING)
        this.register<AIHealth>(AIType.HEALTH)
        this.register<AIHopping>(AIType.HOPPING)
        this.register<AIWalking>(AIType.WALKING)
        this.register<AIWearing>(AIType.WEARING)
        this.register<AIInventory>(AIType.INVENTORY)
        this.register<AIEntityNbt>(AIType.ENTITY_NBT)
        this.register<AIParticle>(AIType.PARTICLE) { pet, aiBase ->
            PathfinderParticle(aiBase, pet)
        }
    }

    /**
     * Cleans up all invalid entities.
     */
    override fun cleanUpInvalidEntitiesInAllWorlds() {
        for (world in Bukkit.getWorlds()) {
            cleanUpInvalidEntities(world.entities)
        }
    }

    /**
     * Checks the entity collection for invalid pet entities and removes them.
     */
    override fun <E> cleanUpInvalidEntities(entities: Collection<E>) {
        for (entity in entities) {
            if (entity !is LivingEntity) {
                continue
            }

            if (petService.findPetByEntity(entity) != null) {
                continue
            }

            // Pets of PetBlocks hide a marker in the boots of every entity. This marker is persistent even on server crashes.
            if (entity.equipment != null && entity.equipment!!.boots != null) {
                val boots = entity.equipment!!.boots

                if (boots!!.itemMeta != null && boots.itemMeta!!.lore != null && boots.itemMeta!!.lore!!.size > 0) {
                    val lore = boots.itemMeta!!.lore!![0]

                    if (lore.stripChatColors() == "PetBlocks") {
                        try {
                            (entity as Any).javaClass.getDeclaredMethod("deleteFromWorld").invoke(entity)
                        } catch (e: Exception) {
                            entity.remove()
                        }

                        plugin.logger.log(Level.INFO, "Removed invalid pet in chunk. Fixed Wrong 'Wrong location'.")
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

        val designClazz = Class.forName(
            "com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetArmorstand".replace(
                "VERSION",
                version.bukkitId
            )
        )

        val player = proxyService.getPlayerFromUUID<Any>(petMeta.playerMeta.uuid)

        return (designClazz.getDeclaredConstructor(Player::class.java, PetMeta::class.java)
            .newInstance(player, petMeta) as NMSPetProxy).proxy
    }

    /**
     * Registers entities on the server when not already registered.
     * Returns true if registered. Returns false when not registered.
     */
    private fun registerEntitiesOnServer(): Boolean {
        if (registered) {
            return true
        }

        val rabbitClazz = Class.forName(
            "com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetRabbit".replace(
                "VERSION",
                version.bukkitId
            )
        )
        entityRegistrationService.register(rabbitClazz, EntityType.RABBIT)

        val villagerClazz = Class.forName(
            "com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetVillager".replace(
                "VERSION",
                version.bukkitId
            )
        )
        entityRegistrationService.register(villagerClazz, EntityType.RABBIT)

        val batClazz = Class.forName(
            "com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetBat".replace(
                "VERSION",
                version.bukkitId
            )
        )
        entityRegistrationService.register(batClazz, EntityType.RABBIT)

        registered = true

        return true
    }

    /**
     * Kills the nearest entity of the [player].
     */
    override fun <P> killNearestEntity(player: P) {
        require(player is Player) { "Player has to be a BukkitPlayer!" }

        var distance = 100.0
        var nearest: Entity? = null

        for (entity in player.location.chunk.entities) {
            if (entity !is Player && player.location.distanceSafely(entity.location) < distance) {
                distance = player.location.distanceSafely(entity.location)
                nearest = entity
            }
        }

        if (nearest != null) {
            if (nearest is EntityPetProxy) {
                nearest.deleteFromWorld()
            } else {
                nearest.remove()
            }

            player.sendMessage(Messages.prefix + ChatColor.GREEN + "You removed entity " + nearest.type + '.'.toString())
        }
    }

    /**
     * Registers a default ai type.
     */
    private fun <A : AIBase> register(aiType: AIType, function: ((PetProxy, A) -> Any)? = null) {
        val clazz = Class.forName(
            "com.github.shynixn.petblocks.core.logic.persistence.entity.CUSTOMEntity".replace(
                "CUSTOM",
                aiType.aiClazz.java.simpleName
            )
        )

        this.aiService.registerAI(
            aiType.type,
            AICreationProxyImpl(yamlSerializationService, clazz.kotlin, function as ((PetProxy, AIBase) -> Any)?)
        )
    }
}
