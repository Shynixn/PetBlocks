@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_17_R1

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
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.util.Vector
import java.lang.reflect.Field

/**
 * Created by Shynixn 2020.
 */
class NMSPetArmorstand(owner: org.bukkit.entity.Player, private val petMeta: PetMeta) :
    ArmorStand((owner.location.world as CraftWorld).handle, owner.location.x, owner.location.y, owner.location.z),
    NMSPetProxy {
    private var internalProxy: PetProxy? = null
    private var jumpingField: Field = net.minecraft.world.entity.LivingEntity::class.java.getDeclaredField("jumping")
    private var internalHitBox: Mob? = null
    private val aiService = PetBlocksApi.resolve(AIService::class.java)

    private val hasFlyCollisionsEnabled = PetBlocksApi.resolve(ConfigurationService::class.java)
        .findValue<Boolean>("global-configuration.fly-wall-colision")
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
        val position = PositionEntity()
        position.x = location.x
        position.y = location.y
        position.z = location.z
        position.yaw = location.yaw.toDouble()
        position.pitch = location.pitch.toDouble()
        position.worldName = location.world!!.name
        position.relativeFront(3.0)

        this.moveTo(position.x, position.y, position.z, location.yaw, location.pitch)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        internalProxy = Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.proxy.PetProxyImpl")
            .getDeclaredConstructor(PetMeta::class.java, ArmorStand::class.java, Player::class.java)
            .newInstance(petMeta, this.bukkitEntity, owner) as PetProxy

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

        val player = proxy.getPlayer<org.bukkit.entity.Player>()
        this.applyNBTTagForArmorstand()
        val hasRidingAi = petMeta.aiGoals.count { a -> a is AIGroundRiding || a is AIFlyRiding } > 0

        if (hasRidingAi) {
            val armorstand = proxy.getHeadArmorstand<org.bukkit.entity.ArmorStand>()

            if (!armorstand.passengers.contains(player)) {
                armorstand.velocity = Vector(0, 1, 0)
                armorstand.addPassenger(player)
            }
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
            val armorstand = proxy.getHeadArmorstand<org.bukkit.entity.ArmorStand>()

            if (!player.passengers.contains(armorstand)) {
                player.addPassenger(armorstand)
            }

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
     * Disable setting slots.
     */
    override fun setSlot(enumitemslot: EquipmentSlot?, itemstack: ItemStack?, silent: Boolean) {
    }

    /**
     * Disable setting slots.
     */
    override fun setItemSlot(enumitemslot: EquipmentSlot?, itemstack: ItemStack?) {
    }

    /**
     * Sets the slot securely.
     */
    fun setSecureSlot(enumitemslot: EquipmentSlot?, itemstack: ItemStack?) {
        super.setItemSlot(enumitemslot, itemstack)
    }

    /**
     * Entity tick.
     */
    override fun tick() {
        super.tick()

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

                if (y > -100) {
                    this.moveTo(location.x, y, location.z, location.yaw, location.pitch)
                    this.setDeltaMovement(
                        this.internalHitBox!!.deltaMovement.x,
                        this.internalHitBox!!.deltaMovement.y,
                        this.internalHitBox!!.deltaMovement.z
                    )
                }
            }

            if (proxy.teleportTarget != null) {
                val location = proxy.teleportTarget!! as Location

                if (this.internalHitBox != null) {
                    this.internalHitBox!!.moveTo(
                        location.x,
                        location.y,
                        location.z,
                        location.yaw,
                        location.pitch
                    )
                }

                this.moveTo(location.x, location.y, location.z, location.yaw, location.pitch)
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
    override fun move(enummovetype: MoverType?, vec3d: Vec3?) {
        super.move(enummovetype, vec3d)

        if (passengers == null || this.passengers.firstOrNull { p -> p is net.minecraft.world.entity.player.Player } == null) {
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
        locField.set(
            this,
            Vec3(
                (axisBoundingBox.minX + axisBoundingBox.maxX) / 2.0,
                axisBoundingBox.minY + offSet,
                (axisBoundingBox.minZ + axisBoundingBox.maxZ) / 2.0
            )
        )
    }

    /**
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPetArmorstand {
        if (this.entityBukkit == null) {
            entityBukkit = CraftPet(this.level.craftServer, this)

            val field = Entity::class.java.getDeclaredField("bukkitEntity")
            field.isAccessible = true
            field.set(this, entityBukkit)
        }

        return this.entityBukkit as CraftPetArmorstand
    }

    /**
     * Riding function.
     */
    override fun travel(vec3d: Vec3) {
        val human = this.passengers.firstOrNull { p -> p is Player }

        if (this.passengers == null || human == null) {
            flyHasTakenOffGround = false
            return
        }

        val aiFlyRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        if (aiFlyRiding != null) {
            rideInAir(human as Player, aiFlyRiding as AIFlyRiding)
            return
        }

        val aiGroundRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }

        if (aiGroundRiding != null) {
            rideOnGround(human as Player, aiGroundRiding as AIGroundRiding, vec3d.y)
            return
        }
    }

    /**
     * Handles the riding in air.
     */
    private fun rideInAir(human: net.minecraft.world.entity.player.Player, ai: AIFlyRiding) {
        // Show entity and player rotation.
        val sideMot: Float = human.xxa * 0.5f
        var forMot: Float = human.zza

        this.yRot = human.yRot
        this.yRotO = this.yRot
        this.xRot = human.xRot * 0.5f
        this.setRot(this.yRot, this.xRot)
        this.yBodyRot = this.yRot
        this.yHeadRot = this.yBodyRot

        // Calculate flying direction and fix yaw in flying direction.
        val flyingVector = Vector()
        val flyingLocation = Location(this.level.world, this.getX(), this.getY(), this.getZ())

        if (sideMot < 0.0f) {
            flyingLocation.yaw = human.yRot - 90
            flyingVector.add(flyingLocation.direction.normalize().multiply(-0.5))
        } else if (sideMot > 0.0f) {
            flyingLocation.yaw = human.yRot + 90
            flyingVector.add(flyingLocation.direction.normalize().multiply(-0.5))
        }

        if (forMot < 0.0f) {
            flyingLocation.yaw = human.yRot
            flyingVector.add(flyingLocation.direction.normalize().multiply(0.5))
        } else if (forMot > 0.0f) {
            flyingLocation.yaw = human.yRot
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
            this.setPos(
                this.flyWallCollisionVector!!.x,
                this.flyWallCollisionVector!!.y,
                this.flyWallCollisionVector!!.z
            )
            return
        }

        flyingLocation.add(flyingVector.multiply(2.25).multiply(ai.ridingSpeed))
        this.setPos(flyingLocation.x, flyingLocation.y, flyingLocation.z)

        if (isCollidingWithWall()) {
            // Cache current position if entity is going to collide with wall.
            this.flyWallCollisionVector = flyingLocation.toVector()
        }
    }

    /**
     *  Gets if this entity is going to collide with a wall.
     */
    private fun isCollidingWithWall(): Boolean {
        val currentLocationVector = Vec3(this.getX(), this.getY(), this.getZ())
        val directionVector = Vec3(
            this.getX() + this.deltaMovement.x * 1.5,
            this.getY() + this.deltaMovement.y * 1.5,
            this.getZ() + this.deltaMovement.z * 1.5
        )

        val rayTrace = ClipContext(
            currentLocationVector,
            directionVector,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            null
        )
        val movingObjectPosition = this.level.clip(rayTrace)

        return movingObjectPosition.type == HitResult.Type.BLOCK && hasFlyCollisionsEnabled
    }

    /**
     * Handles the riding on ground.
     */
    private fun rideOnGround(human: Player, ai: AIGroundRiding, f2: Double) {
        val sideMot: Float = human.xxa * 0.5f
        var forMot: Float = human.zza

        this.yRot = human.yRot
        this.yRotO = this.yRot
        this.xRot = human.xRot * 0.5f
        this.setRot(this.yRot, this.xRot)
        this.yBodyRot = this.yRot
        this.yHeadRot = this.yBodyRot

        if (forMot <= 0.0f) {
            forMot *= 0.25f
        }

        if (this.onGround && this.isPassengerJumping()) {
            this.setDeltaMovement(this.deltaMovement.x, 0.5, this.deltaMovement.z)
        }

        this.flyingSpeed = this.speed * 0.1f

        if (this.isControlledByLocalInstance()) {
            this.speed = 0.35f
            super.travel(Vec3(sideMot * ai.ridingSpeed, f2, forMot * ai.ridingSpeed))
        }
    }

    /**
     * Applies the entity NBT to the hitbox.
     */
    private fun applyNBTTagToHitBox(hitBox: net.minecraft.world.entity.LivingEntity) {
        val compound = CompoundTag()
        hitBox.addAdditionalSaveData(compound)
        applyAIEntityNbt(
            compound,
            this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.hitBoxNbt }.toList()
        )
        hitBox.readAdditionalSaveData(compound)
        // CustomNameVisible does not working via NBT Tags.
        hitBox.isCustomNameVisible = compound.contains("CustomNameVisible") && compound.getInt("CustomNameVisible") == 1
    }

    /**
     * Applies the entity NBT to the armorstand.
     */
    private fun applyNBTTagForArmorstand() {
        val compound = CompoundTag()
        this.addAdditionalSaveData(compound)
        applyAIEntityNbt(
            compound,
            this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.armorStandNbt }.toList()
        )
        this.readAdditionalSaveData(compound)
        // CustomNameVisible does not working via NBT Tags.
        this.isCustomNameVisible = compound.contains("CustomNameVisible") && compound.getInt("CustomNameVisible") == 1
    }

    /**
     * Applies the raw NbtData to the given target.
     */
    private fun applyAIEntityNbt(target: CompoundTag, rawNbtDatas: List<String>) {
        val compoundMapField = CompoundTag::class.java.getDeclaredField("map")
        compoundMapField.isAccessible = true
        val rootCompoundMap = compoundMapField.get(target) as MutableMap<Any?, Any?>

        for (rawNbtData in rawNbtDatas) {
            if (rawNbtData.isEmpty()) {
                continue
            }

            val parsedCompound = try {
                TagParser.parseTag(rawNbtData)
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
        val hasEmptyHitBoxAi =
            petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding || a is AIFlyRiding || a is AIWearing } != null

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
