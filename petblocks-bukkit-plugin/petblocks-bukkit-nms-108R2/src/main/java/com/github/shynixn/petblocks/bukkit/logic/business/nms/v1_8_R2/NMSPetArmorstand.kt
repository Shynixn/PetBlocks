package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_8_R2

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.bukkit.event.PetBlocksAIPreChangeEvent
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.AISerializationService
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.core.logic.business.extension.relativeFront
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import net.minecraft.server.v1_8_R2.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.util.Vector
import java.lang.reflect.Field

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
class NMSPetArmorstand(owner: Player, val petMeta: PetMeta) : EntityArmorStand((owner.location.world as CraftWorld).handle), NMSPetProxy {
    private var internalProxy: PetProxy? = null
    private var jumpingField: Field = EntityLiving::class.java.getDeclaredField("aY")
    private var internalHitBox: EntityInsentient? = null
    private val aiService = PetBlocksApi.resolve(AISerializationService::class.java)

    private val flyCanHitWalls = PetBlocksApi.resolve(ConfigurationService::class.java).findValue<Boolean>("global-configuration.fly-wall-colision")
    private var flyHasTakenOffGround = false
    private var flyIsOnGround: Boolean = false
    private var flyHasHitFloor: Boolean = false
    private var flyWallCollisionVector: Vector? = null

    /**
     * Proxy handler.
     */
    override val proxy: PetProxy get() = internalProxy!!

    /**
     * Initializes the nms design.
     */
    init {
        jumpingField.isAccessible = true

        val location = owner.location
        val mcWorld = (location.world as CraftWorld).handle
        val position = PositionEntity(location.x, location.y, location.z, location.yaw.toDouble(), location.pitch.toDouble(), location.world.name).relativeFront(3.0)

        this.setPositionRotation(position.x, position.y, position.z, location.yaw, location.pitch)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        internalProxy = Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.proxy.PetProxyImpl")
            .getDeclaredConstructor(PetMeta::class.java, ArmorStand::class.java, Player::class.java).newInstance(petMeta, this.bukkitEntity, owner) as PetProxy

        petMeta.propertyTracker.onPropertyChanged(PetMeta::aiGoals, true)

        val compound = NBTTagCompound()
        compound.setBoolean("invulnerable", true)
        compound.setBoolean("Invisible", true)
        compound.setBoolean("PersistenceRequired", true)
        compound.setBoolean("ShowArms", true)
        compound.setBoolean("NoBasePlate", true)
        this.a(compound)
    }

    /**
     * Spawns a new hitbox
     */
    private fun spawnHitBox() {
        if (internalHitBox != null) {
            (internalHitBox!!.bukkitEntity as EntityPetProxy).deleteFromWorld()
            internalHitBox = null
            proxy.changeHitBox(internalHitBox)
        }

        val player = proxy.getPlayer<Player>()

        val compound = NBTTagCompound()
        this.b(compound)
        compound.setBoolean("Marker", false)
        this.a(compound)
        this.customNameVisible = true

        val hasRidingAi = petMeta.aiGoals.count { a -> a is AIGroundRiding || a is AIFlyRiding } > 0

        if (hasRidingAi) {
            val armorstand = proxy.getHeadArmorstand<ArmorStand>()

            armorstand.velocity = Vector(0, 1, 0)

            if (armorstand.passenger != null) {
                armorstand.passenger.eject()
            }

            armorstand.passenger = player

            return
        } else {
            if (player.passenger != null) {
                player.eject()
            }
        }

        val aiWearing = this.petMeta.aiGoals.firstOrNull { a -> a is AIWearing }

        if (aiWearing != null) {
            val internalCompound = NBTTagCompound()
            this.b(internalCompound)
            internalCompound.setBoolean("Marker", true)
            this.a(internalCompound)
            this.customNameVisible = false

            val armorstand = proxy.getHeadArmorstand<ArmorStand>()

            if (player.passenger != null) {
                player.eject()
            }

            player.passenger = armorstand

            return
        }

        val flyingAi = petMeta.aiGoals.firstOrNull { a -> a is AIFlying }

        if (flyingAi != null) {
            internalHitBox = NMSPetBat(this, getBukkitEntity().location)
            proxy.changeHitBox(internalHitBox!!.bukkitEntity as LivingEntity)
            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetBat).applyPathfinders(aiGoals)
            return
        }

        val hoppingAi = petMeta.aiGoals.firstOrNull { a -> a is AIHopping }

        if (hoppingAi != null) {
            internalHitBox = NMSPetRabbit(this, getBukkitEntity().location)
            proxy.changeHitBox(internalHitBox!!.bukkitEntity as LivingEntity)
            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetRabbit).applyPathfinders(aiGoals)
            return
        }

        val walkingAi = petMeta.aiGoals.firstOrNull { a -> a is AIWalking }

        if (walkingAi != null) {
            internalHitBox = NMSPetVillager(this, getBukkitEntity().location)
            proxy.changeHitBox(internalHitBox!!.bukkitEntity as LivingEntity)
            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetVillager).applyPathfinders(aiGoals)
            return
        }
    }

    /**
     * Entity tick.
     */
    override fun doTick() {
        super.doTick()

        try {
            proxy.run()

            if (this.internalHitBox != null) {
                val location = internalHitBox!!.bukkitEntity.location
                val aiGoal = petMeta.aiGoals.lastOrNull { p -> p is AIMovement } ?: return
                val y = location.y + (aiGoal as AIMovement).movementYOffSet

                this.setPositionRotation(location.x, y, location.z, location.yaw, location.pitch)

                this.motX = this.internalHitBox!!.motX
                this.motY = this.internalHitBox!!.motY
                this.motZ = this.internalHitBox!!.motZ
            }

            if (proxy.teleportTarget != null) {
                val location = proxy.teleportTarget!! as Location

                if (this.internalHitBox != null) {
                    this.internalHitBox!!.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
                }

                this.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
                proxy.teleportTarget = null
            }

            if (PetMeta::aiGoals.hasChanged(petMeta)) {
                val event = PetBlocksAIPreChangeEvent(proxy.getPlayer(), proxy)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    return
                }

                spawnHitBox()
                proxy.aiGoals = null
            }
        } catch (e: Exception) {
            PetBlocksApi.resolve(LoggingService::class.java).error("Failed to execute tick.", e)
        }
    }

    /**
     * Overrides the moving of the pet design.
     */
    override fun move(d0: Double, d1: Double, d2: Double) {
        super.move(d0, d1, d2)

        if (passenger == null || passenger !is EntityHuman) {
            return
        }

        val groundAi = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }
        val airAi = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        val offSet = when {
            groundAi != null -> (groundAi as AIGroundRiding).ridingYOffSet
            airAi != null -> (airAi as AIFlyRiding).ridingYOffSet
            else -> 0.0
        }

        val axisBoundingBox = this.boundingBox
        this.locX = (axisBoundingBox.a + axisBoundingBox.d) / 2.0
        this.locY = axisBoundingBox.b + offSet
        this.locZ = (axisBoundingBox.c + axisBoundingBox.f) / 2.0
    }

    /**
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPetArmorstand {
        if (this.bukkitEntity == null) {
            this.bukkitEntity = CraftPetArmorstand(this.world.server, this)
        }

        return this.bukkitEntity as CraftPetArmorstand
    }

    /**
     * Riding function.
     */
    override fun g(sidemot: Float, formot: Float) {
        if (passenger == null || passenger !is EntityHuman) {
            flyHasTakenOffGround = false
            return
        }

        val human = passenger as EntityHuman

        val aiFlyRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        if (aiFlyRiding != null) {
            rideInAir(human, aiFlyRiding as AIFlyRiding)
            return
        }

        val aiGroundRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }

        if (aiGroundRiding != null) {
            rideOnGround(human, aiGroundRiding as AIGroundRiding)
            return
        }
    }

    /**
     * Handles the riding in air.
     */
    private fun rideInAir(human: EntityHuman, ai: AIFlyRiding) {
        val sideMot: Float = human.aZ * 0.5f
        val forMot: Float = human.ba

        this.yaw = human.yaw
        this.lastYaw = this.yaw
        this.pitch = human.pitch * 0.5f
        this.setYawPitch(this.yaw, this.pitch)
        this.aK = this.yaw
        this.aI = this.aK

        val flyingVector = Vector()
        val flyingLocation = Location(this.world.world, this.locX, this.locY, this.locZ)

        if (sideMot < 0.0f) {
            flyingLocation.yaw = human.yaw - 90
            flyingVector.add(flyingLocation.direction.normalize().multiply(-0.5))
        } else if (sideMot > 0.0f) {
            flyingLocation.yaw = human.yaw + 90
            flyingVector.add(flyingLocation.direction.normalize().multiply(-0.5))
        }

        if (forMot < 0.0f) {
            flyingLocation.yaw = human.yaw
            flyingVector.add(flyingLocation.direction.normalize().multiply(0.5))
        } else if (forMot > 0.0f) {
            flyingLocation.yaw = human.yaw
            flyingVector.add(flyingLocation.direction.normalize().multiply(0.5))
        }

        if (!flyHasTakenOffGround) {
            flyHasTakenOffGround = true
            flyingVector.setY(1f)
        }

        if (this.isPassengerJumping()) {
            flyingVector.setY(0.5f)
            this.flyIsOnGround = true
            this.flyHasHitFloor = false
        } else if (this.flyIsOnGround) {
            flyingVector.setY(-0.2f)
        }

        if (this.flyHasHitFloor) {
            flyingVector.setY(0)
            flyingLocation.add(flyingVector.multiply(2.25).multiply(ai.ridingSpeed))
            this.setPosition(flyingLocation.x, flyingLocation.y, flyingLocation.z)
        } else {
            flyingLocation.add(flyingVector.multiply(2.25).multiply(ai.ridingSpeed))
            this.setPosition(flyingLocation.x, flyingLocation.y, flyingLocation.z)
        }

        val vec3d = Vec3D(this.locX, this.locY, this.locZ)
        val vec3d1 = Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ)
        val movingObjectPosition = this.world.rayTrace(vec3d, vec3d1)

        if (movingObjectPosition == null) {
            this.flyWallCollisionVector = flyingLocation.toVector()
        } else if (this.flyWallCollisionVector != null && flyCanHitWalls) {
            this.setPosition(this.flyWallCollisionVector!!.x, this.flyWallCollisionVector!!.y, this.flyWallCollisionVector!!.z)
        }
    }

    /**
     * Handles the riding on ground.
     */
    private fun rideOnGround(human: EntityHuman, ai: AIGroundRiding) {
        val sideMot: Float = human.aZ * 0.5f
        var forMot: Float = human.ba

        this.yaw = human.yaw
        this.lastYaw = this.yaw
        this.pitch = human.pitch * 0.5f
        this.setYawPitch(this.yaw, this.pitch)
        this.aK = this.yaw
        this.aI = this.aK

        if (forMot <= 0.0f) {
            forMot *= 0.25f
        }

        if (this.onGround && this.isPassengerJumping()) {
            this.motY = 0.5
        }

        this.S = ai.climbingHeight.toFloat()
        this.aM = this.bI() * 0.1f

        if (!this.world.isClientSide) {
            this.k(0.35f)
            super.g(sideMot * ai.ridingSpeed.toFloat(), forMot * ai.ridingSpeed.toFloat())
        }

        this.aA = this.aB
        val d0 = this.locX - this.lastX
        val d1 = this.locZ - this.lastZ
        var f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0f

        if (f4 > 1.0f) {
            f4 = 1.0f
        }

        this.aB += (f4 - this.aB) * 0.4f
        this.aC += this.aB
    }

    /**
     * Gets if a passenger of the pet is jumping.
     */
    private fun isPassengerJumping(): Boolean {
        if (passenger == null) {
            return false
        }

        return jumpingField.getBoolean(this.passenger)
    }
}