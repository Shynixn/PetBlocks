@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.nms.v1_12_R1

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.HiddenProxy
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.service.AIService
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.api.sponge.event.PetBlocksAIPreChangeEvent
import com.github.shynixn.petblocks.core.logic.business.extension.cast
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.core.logic.business.extension.relativeFront
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.*
import com.github.shynixn.petblocks.sponge.logic.business.proxy.PetProxyImpl
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.MoverType
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.entity.living.Human
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player

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
class NMSPetArmorstand(owner: Player, val petMeta: PetMeta) : EntityArmorStand(owner.location.extent as World), NMSPetProxy, EntityPetProxy, HiddenProxy {
    private var internalProxy: PetProxyImpl? = null
    private var internalHitBox: EntityLiving? = null
    private val aiService = PetBlocksApi.resolve(AIService::class.java)

    private val flyCanHitWalls = PetBlocksApi.resolve(ConfigurationService::class.java).findValue<Boolean>("global-configuration.fly-wall-colision")
    private var flyHasTakenOffGround = false
    private var flyIsOnGround: Boolean = false
    private var flyHasHitFloor: Boolean = false
    private var flyWallCollisionVector: Position? = null

    /**
     * Proxy handler.
     */
    override val proxy: PetProxyImpl get() = internalProxy!!

    /**
     * Initializes the nms design.
     */
    init {
        val location = owner.transform
        val mcWorld = location.extent as World
        val position = location.toPosition().relativeFront(3.0)

        this.setPositionAndRotation(position.x, position.y, position.z, location.yaw.toFloat(), location.pitch.toFloat())
        mcWorld.spawnEntity(this)

        internalProxy = PetProxyImpl(petMeta, this.cast(), owner)
        petMeta.propertyTracker.onPropertyChanged(PetMeta::aiGoals, true)
        this.applyNBTTagForArmorstand()
    }

    /**
     * Removes this entity.
     */
    override fun deleteFromWorld() {
        this.setDead()
    }

    /**
     * Boots marker.
     */
    override var bootsItemStack: Any?
        get() {
            return this.getItemStackFromSlot(EntityEquipmentSlot.FEET)
        }
        set(value) {
            this.setItemStackToSlot(EntityEquipmentSlot.FEET, value as net.minecraft.item.ItemStack)
        }

    /**
     * Spawns a new hitbox
     */
    private fun spawnHitBox() {
        val shouldDeleteHitBox = shouldDeleteHitBox()

        if (shouldDeleteHitBox && internalHitBox != null) {
            (internalHitBox!! as EntityPetProxy).deleteFromWorld()
            internalHitBox = null
            proxy.changeHitBox(internalHitBox)
        }

        val player = proxy.getPlayer<Player>()
        this.applyNBTTagForArmorstand()
        val hasRidingAi = petMeta.aiGoals.count { a -> a is AIGroundRiding || a is AIFlyRiding } > 0

        if (hasRidingAi) {
            val armorstand = proxy.getHeadArmorstand<ArmorStand>()

            armorstand.velocity = Vector3d(0.0, 1.0, 0.0)
            armorstand.addPassenger(player)

            return
        } else {
            for (passenger in player.passengers) {
                if (passenger == (this.cast<ArmorStand>())) {
                    player.clearPassengers()
                    break
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
                internalHitBox = NMSPetBat(this, this.cast<ArmorStand>().transform)
                proxy.changeHitBox(internalHitBox!! as Living)
            }

            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetBat).applyPathfinders(aiGoals)
            this.applyNBTTagToHitBox(internalHitBox!!)
            return
        }

        val hoppingAi = petMeta.aiGoals.firstOrNull { a -> a is AIHopping }

        if (hoppingAi != null) {
            if (internalHitBox == null) {
                internalHitBox = NMSPetRabbit(this, this.cast<ArmorStand>().transform)
                proxy.changeHitBox(internalHitBox!! as Living)
            }

            val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
            (internalHitBox as NMSPetRabbit).applyPathfinders(aiGoals)
            this.applyNBTTagToHitBox(internalHitBox!!)
            return
        }

        if (internalHitBox == null) {
            internalHitBox = NMSPetVillager(this, this.cast<ArmorStand>().transform)
            proxy.changeHitBox(internalHitBox!! as Living)
        }

        val aiGoals = aiService.convertPetAiBasesToPathfinders(proxy, petMeta.aiGoals)
        (internalHitBox as NMSPetVillager).applyPathfinders(aiGoals)
        this.applyNBTTagToHitBox(internalHitBox!!)
    }

    /**
     * Entity tick.
     */
    override fun updateEntityActionState() {
        super.updateEntityActionState()

        try {
            proxy.run()

            if (isDead) {
                return
            }

            if (this.internalHitBox != null) {
                val location = (internalHitBox!! as Living).transform
                val aiGoal = petMeta.aiGoals.lastOrNull { p -> p is AIMovement } ?: return
                var y = location.y + (aiGoal as AIMovement).movementYOffSet

                if (this.isSmall) {
                    y += 0.6
                }

                this.setPositionAndRotation(location.x, y, location.z, location.yaw.toFloat(), location.pitch.toFloat())

                this.motionX = this.internalHitBox!!.motionX
                this.motionY = this.internalHitBox!!.motionY
                this.motionZ = this.internalHitBox!!.motionZ
            }

            if (proxy.teleportTarget != null) {
                val location = proxy.teleportTarget!! as Transform<org.spongepowered.api.world.World>

                if (this.internalHitBox != null) {
                    this.internalHitBox!!.setPositionAndRotation(location.x, location.y, location.z, location.yaw.toFloat(), location.pitch.toFloat())
                }

                this.setPositionAndRotation(location.x, location.y, location.z, location.yaw.toFloat(), location.pitch.toFloat())
                proxy.teleportTarget = null
            }

            if (PetMeta::aiGoals.hasChanged(petMeta)) {
                val event = PetBlocksAIPreChangeEvent(proxy.getPlayer(), proxy)
                Sponge.getEventManager().post(event)

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
    override fun move(type: MoverType, x: Double, y: Double, z: Double) {
        super.move(type, x, y, z)

        if (passengers.isEmpty() || this.passengers.firstOrNull { p -> p is Human } == null) {
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

        val axisBoundingBox = this.entityBoundingBox
        this.posX = (axisBoundingBox.minX + axisBoundingBox.maxX) / 2.0
        this.posY = axisBoundingBox.minY + offSet
        this.posZ = (axisBoundingBox.minZ + axisBoundingBox.maxZ) / 2.0
    }

    /**
     * Riding function.
     */
    override fun travel(sidemot: Float, f2: Float, formot: Float) {
        val human = this.passengers.firstOrNull { p -> p is EntityPlayer }

        if (this.passengers.isEmpty() || human == null) {
            flyHasTakenOffGround = false
            return
        }

        val aiFlyRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        if (aiFlyRiding != null) {
            rideInAir(human as EntityPlayerMP, aiFlyRiding as AIFlyRiding)
            return
        }

        val aiGroundRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }

        if (aiGroundRiding != null) {
            rideOnGround(human as EntityPlayerMP, aiGroundRiding as AIGroundRiding, f2)
            return
        }
    }

    /**
     * Handles the riding in air.
     */
    private fun rideInAir(human: EntityPlayerMP, ai: AIFlyRiding) {
        val sideMot: Float = human.moveStrafing * 0.5f
        val forMot: Float = human.moveForward

        this.rotationYaw = human.rotationYaw
        this.prevRotationYaw = this.rotationYaw
        this.rotationPitch = human.rotationPitch * 0.5f
        this.setRotation(this.rotationYaw, this.rotationPitch)
        this.rotationYawHead = this.rotationYaw
        this.renderYawOffset = this.rotationYaw

        val flyingVector = PositionEntity()
        val flyingLocation = PositionEntity(this.posX, this.posY, this.posZ, 0.0, 0.0, (world as org.spongepowered.api.world.World).name)

        if (sideMot < 0.0f) {
            flyingLocation.yaw = human.rotationYaw - 90.0
            flyingVector.add(flyingLocation.getDirection().toVector().normalize().mul(-0.5).toPosition())
        } else if (sideMot > 0.0f) {
            flyingLocation.yaw = human.rotationYaw + 90.0
            flyingVector.add(flyingLocation.getDirection().toVector().normalize().mul(-0.5).toPosition())
        }

        if (forMot < 0.0f) {
            flyingLocation.yaw = human.rotationYaw.toDouble()
            flyingVector.add(flyingLocation.getDirection().toVector().normalize().mul(0.5).toPosition())
        } else if (forMot > 0.0f) {
            flyingLocation.yaw = human.rotationYaw.toDouble()
            flyingVector.add(flyingLocation.getDirection().toVector().normalize().mul(0.5).toPosition())
        }

        if (!flyHasTakenOffGround) {
            flyHasTakenOffGround = true
            flyingVector.y = 1.0
        }

        if (this.isPassengerJumping()) {
            flyingVector.y = 0.5
            this.flyIsOnGround = true
            this.flyHasHitFloor = false
        } else if (this.flyIsOnGround) {
            flyingVector.y = -0.2
        }

        if (this.flyHasHitFloor) {
            flyingVector.y = 0.0
            flyingLocation.add(flyingVector.toVector().mul(2.25).mul(ai.ridingSpeed).toPosition())
            this.setPosition(flyingLocation.x, flyingLocation.y, flyingLocation.z)
        } else {
            flyingLocation.add(flyingVector.toVector().mul(2.25).mul(ai.ridingSpeed).toPosition())
            this.setPosition(flyingLocation.x, flyingLocation.y, flyingLocation.z)
        }

        val vec3d = Vec3d(this.posX, this.posY, this.posZ)
        val vec3d1 = Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ)
        val movingObjectPosition = this.world.rayTraceBlocks(vec3d, vec3d1)

        if (movingObjectPosition == null) {
            this.flyWallCollisionVector = flyingLocation.toVector().toPosition()
        } else if (this.flyWallCollisionVector != null && flyCanHitWalls) {
            this.setPosition(this.flyWallCollisionVector!!.x, this.flyWallCollisionVector!!.y, this.flyWallCollisionVector!!.z)
        }
    }

    /**
     * Handles the riding on ground.
     */
    private fun rideOnGround(human: EntityPlayerMP, ai: AIGroundRiding, f2: Float) {
        val sideMot: Float = human.moveStrafing * 0.5f
        var forMot: Float = human.moveForward

        this.rotationYaw = human.rotationYaw
        this.prevRotationYaw = this.rotationYaw
        this.rotationPitch = human.rotationPitch * 0.5f
        this.setRotation(this.rotationYaw, this.rotationPitch)
        this.rotationYawHead = this.rotationYaw
        this.renderYawOffset = this.rotationYaw

        if (forMot <= 0.0f) {
            forMot *= 0.25f
        }

        if (this.onGround && this.isPassengerJumping()) {
            this.motionY = 0.5
        }

        this.stepHeight = ai.climbingHeight.toFloat()
        this.jumpMovementFactor = this.aiMoveSpeed * 0.1F

        if (!this.world.isRemote) {
            this.aiMoveSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).attributeValue.toFloat()
            super.travel(sideMot * ai.ridingSpeed.toFloat(), f2, forMot * ai.ridingSpeed.toFloat())
        }

        this.prevLimbSwingAmount = this.limbSwingAmount
        val d1 = this.posX - this.prevPosX
        val d0 = this.posZ - this.prevPosZ
        var f22 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0f

        if (f22 > 1.0f) {
            f22 = 1.0f
        }

        this.limbSwingAmount += (f22 - this.limbSwingAmount) * 0.4f
        this.limbSwing += this.limbSwingAmount
    }

    /**
     * Applies the entity NBT to the hitbox.
     */
    private fun applyNBTTagToHitBox(hitBox: EntityLiving) {
        val compound = NBTTagCompound()
        hitBox.writeEntityToNBT(compound)
        applyAIEntityNbt(compound, this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.hitBoxNbt }.toList())
        hitBox.readEntityFromNBT(compound)
        // CustomNameVisible does not working via NBT Tags.
        hitBox.alwaysRenderNameTag = compound.hasKey("CustomNameVisible") && compound.getInteger("CustomNameVisible") == 1
    }

    /**
     * Applies the entity NBT to the armorstand.
     */
    private fun applyNBTTagForArmorstand() {
        val compound = NBTTagCompound()
        this.writeEntityToNBT(compound)
        applyAIEntityNbt(compound, this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.armorStandNbt }.toList())
        this.readEntityFromNBT(compound)
        // CustomNameVisible does not working via NBT Tags.
        this.alwaysRenderNameTag = compound.hasKey("CustomNameVisible") && compound.getInteger("CustomNameVisible") == 1
    }

    /**
     * Applies the raw NbtData to the given target.
     */
    private fun applyAIEntityNbt(target: NBTTagCompound, rawNbtDatas: List<String>) {
        val compoundMapField = NBTTagCompound::class.java.getDeclaredField("field_74784_a")
        compoundMapField.isAccessible = true
        val rootCompoundMap = compoundMapField.get(target) as MutableMap<Any?, Any?>

        for (rawNbtData in rawNbtDatas) {
            if (rawNbtData.isEmpty()) {
                continue
            }

            val parsedCompound = try {
                JsonToNBT.getTagFromJson(rawNbtData)
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
        val jumpingField = EntityLivingBase::class.java.getDeclaredField("field_70703_bu")
        jumpingField.isAccessible = true

        val isJumping = jumpingField.get(this.passengers[0]) as Boolean

        return !this.passengers.isEmpty() && isJumping
    }
}