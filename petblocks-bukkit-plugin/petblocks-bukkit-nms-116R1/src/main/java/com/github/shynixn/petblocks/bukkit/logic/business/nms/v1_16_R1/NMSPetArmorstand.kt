@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_16_R1

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.bukkit.event.PetBlocksAIPreChangeEvent
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.AIService
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.core.logic.business.extension.relativeFront
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import net.minecraft.server.v1_16_R1.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.util.Vector
import java.lang.reflect.Field

/**
 * Created by Shynixn 2020.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2020 by Shynixn
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
class NMSPetArmorstand(owner: Player, private val petMeta: PetMeta) :
    EntityArmorStand((owner.location.world as CraftWorld).handle, owner.location.x, owner.location.y, owner.location.z), NMSPetProxy {
    private var internalProxy: PetProxy? = null
    private var jumpingField: Field = EntityLiving::class.java.getDeclaredField("jumping")
    private var internalHitBox: EntityInsentient? = null
    private val aiService = PetBlocksApi.resolve(AIService::class.java)

    private val hasFlyCollisionsEnabled = PetBlocksApi.resolve(ConfigurationService::class.java).findValue<Boolean>("global-configuration.fly-wall-colision")
    private var flyHasTakenOffGround = false
    private var flyGravity: Boolean = false
    private var flyWallCollisionVector: Vector? = null

    private val locField = Entity::class.java.getDeclaredField("loc")

    // BukkitEntity has to be self cached since 1.14.
    private var entityBukkit: Any? = null

    /**
     * Proxy handler.
     */
    override val proxy: PetProxy get() = internalProxy!!

    /**
     * Initializes the nms design.
     */
    init {
        jumpingField.isAccessible = true
        locField.isAccessible = true

        val location = owner.location
        val mcWorld = (location.world as CraftWorld).handle
        val position =
            PositionEntity(location.x, location.y, location.z, location.yaw.toDouble(), location.pitch.toDouble(), location.world!!.name).relativeFront(3.0)

        this.setPositionRotation(position.x, position.y, position.z, location.yaw, location.pitch)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        internalProxy = Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.proxy.PetProxyImpl")
            .getDeclaredConstructor(PetMeta::class.java, ArmorStand::class.java, Player::class.java).newInstance(petMeta, this.bukkitEntity, owner) as PetProxy

        petMeta.propertyTracker.onPropertyChanged(PetMeta::aiGoals, true)
        applyNBTTagForArmorstand()
    }

    /**
     * Spawns a new hitbox
     */
    private fun spawnHitBox() {
        val shouldDeleteHitBox = shouldDeleteHitBox()

        if (shouldDeleteHitBox && internalHitBox != null) {
            (internalHitBox!!.bukkitEntity as EntityPetProxy).deleteFromWorld()
            internalHitBox = null
            proxy.changeHitBox(internalHitBox)
        }

        val player = proxy.getPlayer<Player>()
        this.applyNBTTagForArmorstand()
        val hasRidingAi = petMeta.aiGoals.count { a -> a is AIGroundRiding || a is AIFlyRiding } > 0

        if (hasRidingAi) {
            val armorstand = proxy.getHeadArmorstand<ArmorStand>()

            armorstand.velocity = Vector(0, 1, 0)
            armorstand.addPassenger(player)

            return
        } else {
            for (passenger in player.passengers) {
                if (passenger == this.bukkitEntity) {
                    player.removePassenger(passenger)
                }
            }
        }

        val aiWearing = this.petMeta.aiGoals.firstOrNull { a -> a is AIWearing }

        if (aiWearing != null) {
            this.applyNBTTagForArmorstand()
            val armorstand = proxy.getHeadArmorstand<ArmorStand>()
            player.addPassenger(armorstand)

            return
        }

        val flyingAi = petMeta.aiGoals.firstOrNull { a -> a is AIFlying }

        if (flyingAi != null) {
            if (internalHitBox == null) {
                internalHitBox = NMSPetBat(this, bukkitEntity.location)
                proxy.changeHitBox(internalHitBox!!.bukkitEntity as LivingEntity)
            }

            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetBat).applyPathfinders(aiGoals)
            applyNBTTagToHitBox(internalHitBox!!)
            return
        }

        val hoppingAi = petMeta.aiGoals.firstOrNull { a -> a is AIHopping }

        if (hoppingAi != null) {
            if (internalHitBox == null) {
                internalHitBox = NMSPetRabbit(this, bukkitEntity.location)
                proxy.changeHitBox(internalHitBox!!.bukkitEntity as LivingEntity)
            }

            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetRabbit).applyPathfinders(aiGoals)
            applyNBTTagToHitBox(internalHitBox!!)
            return
        }

        if (internalHitBox == null) {
            internalHitBox = NMSPetVillager(this, bukkitEntity.location)
            proxy.changeHitBox(internalHitBox!!.bukkitEntity as LivingEntity)
        }

        val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
        (internalHitBox as NMSPetVillager).applyPathfinders(aiGoals)
        applyNBTTagToHitBox(internalHitBox!!)
    }

    /**
     * Entity tick.
     */
    override fun doTick() {
        super.doTick()

        try {
            proxy.run()

            if (dead) {
                return
            }

            if (this.internalHitBox != null) {
                val location = internalHitBox!!.bukkitEntity.location
                val aiGoal = petMeta.aiGoals.lastOrNull { p -> p is AIMovement } ?: return
                var y = location.y + (aiGoal as AIMovement).movementYOffSet

                if (this.isSmall) {
                    y += 0.6
                }

                this.setPositionRotation(location.x, y, location.z, location.yaw, location.pitch)
                this.setMot(this.internalHitBox!!.mot.x, this.internalHitBox!!.mot.y, this.internalHitBox!!.mot.z)
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
    override fun move(enummovetype: EnumMoveType?, vec3d: Vec3D?) {
        super.move(enummovetype, vec3d)

        if (passengers == null || this.passengers.firstOrNull { p -> p is EntityHuman } == null) {
            return
        }

        val groundAi = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }
        val airAi = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        var offSet = when {
            groundAi != null -> (groundAi as AIGroundRiding).ridingYOffSet
            airAi != null -> (airAi as AIFlyRiding).ridingYOffSet
            else -> 0.0
        }

        if (this.isSmall) {
            offSet += 0.6
        }

        val axisBoundingBox = this.boundingBox

        // This way of setting the locations fields ensures compatibility with PaperSpigot.
        locField.set(this, Vec3D((axisBoundingBox.minX + axisBoundingBox.maxX) / 2.0, axisBoundingBox.minY + offSet, (axisBoundingBox.minZ + axisBoundingBox.maxZ) / 2.0))
    }

    /**
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPetArmorstand {
        if (this.entityBukkit == null) {
            entityBukkit = CraftPetArmorstand(this.world.server, this)

            val field = Entity::class.java.getDeclaredField("bukkitEntity")
            field.isAccessible = true
            field.set(this, entityBukkit)
        }

        return this.entityBukkit as CraftPetArmorstand
    }

    /**
     * Riding function.
     */
    override fun f(vec3d: Vec3D) {
        val human = this.passengers.firstOrNull { p -> p is EntityHuman }

        if (this.passengers == null || human == null) {
            flyHasTakenOffGround = false
            return
        }

        val aiFlyRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        if (aiFlyRiding != null) {
            rideInAir(human as EntityHuman, aiFlyRiding as AIFlyRiding)
            return
        }

        val aiGroundRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }

        if (aiGroundRiding != null) {
            rideOnGround(human as EntityHuman, aiGroundRiding as AIGroundRiding, vec3d.y)
            return
        }
    }

    /**
     * Handles the riding in air.
     */
    private fun rideInAir(human: EntityHuman, ai: AIFlyRiding) {
        // Show entity and player rotation.
        val sideMot: Float = human.aY * 0.5f
        val forMot: Float = human.ba
        this.yaw = human.yaw
        this.lastYaw = this.yaw
        this.pitch = human.pitch * 0.5f
        this.setYawPitch(this.yaw, this.pitch)
        this.aH = this.yaw
        this.aJ = this.aH

        // Calculate flying direction and fix yaw in flying direction.
        val flyingVector = Vector()
        val flyingLocation = Location(this.world.world, this.locX(), this.locY(), this.locZ())

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

        // If the player has just started riding move it up.
        if (!flyHasTakenOffGround) {
            flyHasTakenOffGround = true
            flyingVector.setY(1f)
        }

        if (this.isPassengerJumping()) {
            flyingVector.setY(0.5f)
            this.flyGravity = true
            this.flyWallCollisionVector = null
        } else if (this.flyGravity) {
            flyingVector.setY(-0.2f)
        }

        // If wall collision has happened.
        if (flyWallCollisionVector != null) {
            this.setPosition(this.flyWallCollisionVector!!.x, this.flyWallCollisionVector!!.y, this.flyWallCollisionVector!!.z)
            return
        }

        flyingLocation.add(flyingVector.multiply(2.25).multiply(ai.ridingSpeed))
        this.setPosition(flyingLocation.x, flyingLocation.y, flyingLocation.z)

        if (isCollidingWithWall()) {
            // Cache current position if entity is going to collide with wall.
            this.flyWallCollisionVector = flyingLocation.toVector()
        }
    }

    /**
     *  Gets if this entity is going to collide with a wall.
     */
    private fun isCollidingWithWall(): Boolean {
        val currentLocationVector = Vec3D(this.locX(), this.locY(), this.locZ())
        val directionVector = Vec3D(this.locX() + this.mot.x * 1.5, this.locY() + this.mot.y * 1.5, this.locZ() + this.mot.z * 1.5)
        val rayTrace = RayTrace(currentLocationVector, directionVector, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, null)
        val movingObjectPosition = this.world.rayTrace(rayTrace)

        return movingObjectPosition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK && hasFlyCollisionsEnabled
    }

    /**
     * Handles the riding on ground.
     */
    private fun rideOnGround(human: EntityHuman, ai: AIGroundRiding, f2: Double) {
        val sideMot: Float = human.aY * 0.5f
        var forMot: Float = human.ba

        this.yaw = human.yaw
        this.lastYaw = this.yaw
        this.pitch = human.pitch * 0.5f
        this.setYawPitch(this.yaw, this.pitch)
        this.aH = this.yaw
        this.aJ = this.aH

        if (forMot <= 0.0f) {
            forMot *= 0.25f
        }

        if (this.onGround && this.isPassengerJumping()) {
            this.setMot(this.mot.x, 0.5, this.mot.z)
        }

        this.G = ai.climbingHeight.toFloat()
        this.aL = this.dM() * 0.1f

        if (!this.world.isClientSide) {
            this.n(0.35f)
            super.f(Vec3D(sideMot * ai.ridingSpeed, f2, forMot * ai.ridingSpeed))
        }

        this.aB = this.aC
        val d0 = this.locX() - this.lastX
        val d1 = this.locZ() - this.lastZ
        var f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0f

        if (f4 > 1.0f) {
            f4 = 1.0f
        }

        this.aC += (f4 - this.aC) * 0.4f
        this.aD += this.aC
    }

    /**
     * Applies the entity NBT to the hitbox.
     */
    private fun applyNBTTagToHitBox(hitBox: EntityInsentient) {
        val compound = NBTTagCompound()
        hitBox.saveData(compound)
        applyAIEntityNbt(compound, this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.hitBoxNbt }.toList())
        hitBox.loadData(compound)
        // CustomNameVisible does not working via NBT Tags.
        hitBox.customNameVisible = compound.hasKey("CustomNameVisible") && compound.getInt("CustomNameVisible") == 1
    }

    /**
     * Applies the entity NBT to the armorstand.
     */
    private fun applyNBTTagForArmorstand() {
        val compound = NBTTagCompound()
        this.saveData(compound)
        applyAIEntityNbt(compound, this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.armorStandNbt }.toList())
        this.loadData(compound)
        // CustomNameVisible does not working via NBT Tags.
        this.customNameVisible = compound.hasKey("CustomNameVisible") && compound.getInt("CustomNameVisible") == 1
    }

    /**
     * Applies the raw NbtData to the given target.
     */
    private fun applyAIEntityNbt(target: NBTTagCompound, rawNbtDatas: List<String>) {
        val compoundMapField = NBTTagCompound::class.java.getDeclaredField("map")
        compoundMapField.isAccessible = true
        val rootCompoundMap = compoundMapField.get(target) as MutableMap<Any?, Any?>

        for (rawNbtData in rawNbtDatas) {
            if (rawNbtData.isEmpty()) {
                continue
            }

            val parsedCompound = try {
                MojangsonParser.parse(rawNbtData)
            } catch (e: Exception) {
                throw RuntimeException("NBT Tag '$rawNbtData' cannot be parsed.", e)
            }

            val parsedCompoundMap = compoundMapField.get(parsedCompound) as Map<*, *>

            for (key in parsedCompoundMap.keys) {
                rootCompoundMap[key] = parsedCompoundMap[key]
            }
        }
    }

    /**
     * Should the hitbox of the armorstand be deleted.
     */
    private fun shouldDeleteHitBox(): Boolean {
        val hasEmptyHitBoxAi = petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding || a is AIFlyRiding || a is AIWearing } != null

        if (hasEmptyHitBoxAi) {
            return true
        }

        if (internalHitBox != null) {
            if (internalHitBox is NMSPetVillager && petMeta.aiGoals.firstOrNull { a -> a is AIWalking } == null) {
                return true
            } else if (internalHitBox is NMSPetRabbit && petMeta.aiGoals.firstOrNull { a -> a is AIHopping } == null) {
                return true
            } else if (internalHitBox is NMSPetBat && petMeta.aiGoals.firstOrNull { a -> a is AIFlying } == null) {
                return true
            }
        }

        return false
    }

    /**
     * Gets if a passenger of the pet is jumping.
     */
    private fun isPassengerJumping(): Boolean {
        return passengers != null && !this.passengers.isEmpty() && jumpingField.getBoolean(this.passengers[0])
    }
}