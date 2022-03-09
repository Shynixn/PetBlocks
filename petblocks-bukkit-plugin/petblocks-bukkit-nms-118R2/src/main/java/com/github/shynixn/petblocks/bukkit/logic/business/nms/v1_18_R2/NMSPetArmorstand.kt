@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_18_R2

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
import net.minecraft.nbt.MojangsonParser
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.entity.*
import net.minecraft.world.entity.decoration.EntityArmorStand
import net.minecraft.world.entity.player.EntityHuman
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.RayTrace
import net.minecraft.world.phys.MovingObjectPosition
import net.minecraft.world.phys.Vec3D
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.util.Vector
import java.lang.reflect.Field

/**
 * Created by Shynixn 2020.
 */
class NMSPetArmorstand(owner: org.bukkit.entity.Player, private val petMeta: PetMeta) :
    EntityArmorStand((owner.location.world as CraftWorld).handle, owner.location.x, owner.location.y, owner.location.z),
    NMSPetProxy {
    private var internalProxy: PetProxy? = null
    private var jumpingField: Field = EntityLiving::class.java.getDeclaredField("bn")
    private var internalHitBox: EntityInsentient? = null
    private val aiService = PetBlocksApi.resolve(AIService::class.java)

    private val hasFlyCollisionsEnabled = PetBlocksApi.resolve(ConfigurationService::class.java)
        .findValue<Boolean>("global-configuration.fly-wall-colision")
    private var flyHasTakenOffGround = false
    private var flyGravity: Boolean = false
    private var flyWallCollisionVector: Vector? = null

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

        this.b(position.x, position.y, position.z, location.yaw, location.pitch) // Set Position and Rotation.
        mcWorld.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        internalProxy = Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.proxy.PetProxyImpl")
            .getDeclaredConstructor(
                PetMeta::class.java,
                ArmorStand::class.java,
                org.bukkit.entity.Player::class.java
            )
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
            val armorstand = proxy.getHeadArmorstand<ArmorStand>()

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
            val armorstand = proxy.getHeadArmorstand<ArmorStand>()

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
    override fun a(enumitemslot: EnumItemSlot?, itemstack: ItemStack?) {
    }

    /**
     * Sets the slot securely.
     */
    fun setSecureSlot(enumitemslot: EnumItemSlot?, itemstack: ItemStack?) {
        super.a(enumitemslot, itemstack)
    }

    /**
     * Entity tick.
     */
    override fun eF() {
        super.eF() // doTick.

        try {
            proxy.run()

            if (dp()) { // isRemoved.
                return
            }

            if (this.internalHitBox != null) {
                val location = internalHitBox!!.bukkitEntity.location
                val aiGoal = petMeta.aiGoals.lastOrNull { p -> p is AIMovement } ?: return
                var y = location.y + (aiGoal as AIMovement).movementYOffSet

                if (this.n()) { // isSmall
                    y += 0.6
                }

                if (y > -100) {
                    this.b(location.x, y, location.z, location.yaw, location.pitch)
                    this.n(
                        this.internalHitBox!!.da().b, this.internalHitBox!!.da().c, this.internalHitBox!!.da().d // SetMot
                    )
                }
            }

            if (proxy.teleportTarget != null) {
                val location = proxy.teleportTarget!! as Location

                if (this.internalHitBox != null) {
                    this.internalHitBox!!.b(
                        location.x,
                        location.y,
                        location.z,
                        location.yaw,
                        location.pitch
                    )
                }

                this.b(location.x, location.y, location.z, location.yaw, location.pitch)
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
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPetArmorstand {
        if (this.entityBukkit == null) {
            entityBukkit = CraftPetArmorstand(Bukkit.getServer() as CraftServer, this)
            val field = Entity::class.java.getDeclaredField("bukkitEntity")
            field.isAccessible = true
            field.set(this, entityBukkit)
        }

        return this.entityBukkit as CraftPetArmorstand
    }

    /**
     * Riding function.
     */
    override fun h(vec3d: Vec3D) {
        val human = this.cF().firstOrNull { p -> p is EntityHuman }

        if (this.cF() == null || human == null) {
            flyHasTakenOffGround = false
            return
        }

        val aiFlyRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIFlyRiding }

        if (aiFlyRiding != null) {
            rideInAir(human as  EntityHuman, aiFlyRiding as AIFlyRiding)
            return
        }

        val aiGroundRiding = this.petMeta.aiGoals.firstOrNull { a -> a is AIGroundRiding }

        if (aiGroundRiding != null) {
            rideOnGround(human as  EntityHuman, aiGroundRiding as AIGroundRiding, vec3d.c)
            return
        }
    }

    /**
     * Handles the riding in air.
     */
    private fun rideInAir(human: EntityHuman, ai: AIFlyRiding) {
        // Show entity and player rotation.
        val sideMot: Float = human.bo * 0.5f
        var forMot: Float = human.bq

        o(human.dn())
        this.x = this.dn()
        this.p(human.dn() * 0.5f)
        this.a(dn(), dn())
        this.aX = this.dn()
        this.aZ = this.aX

        // Calculate flying direction and fix yaw in flying direction.
        var flyingVector = Vector()
        val flyingLocation = Location(this.cA().world, this.dc(), this.de(), this.di())

        if (sideMot < 0.0f) {
            flyingLocation.yaw = human.dn() - 90
            flyingVector.add(flyingLocation.direction.normalize().multiply(-0.5))
        } else if (sideMot > 0.0f) {
            flyingLocation.yaw = human.dn() + 90
            flyingVector.add(flyingLocation.direction.normalize().multiply(-0.5))
        }

        if (forMot < 0.0f) {
            flyingLocation.yaw = human.dn()
            flyingVector.add(flyingLocation.direction.normalize().multiply(0.5))
        } else if (forMot > 0.0f) {
            flyingLocation.yaw = human.dn()
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
            this.e(
                this.flyWallCollisionVector!!.x,
                this.flyWallCollisionVector!!.y,
                this.flyWallCollisionVector!!.z
            )
            return
        }

        flyingLocation.add(flyingVector.multiply(2.25).multiply(ai.ridingSpeed))
        this.e(flyingLocation.x, flyingLocation.y, flyingLocation.z)

        if (isCollidingWithWall()) {
            // Cache current position if entity is going to collide with wall.
            this.flyWallCollisionVector = flyingLocation.toVector()
        }
    }

    /**
     *  Gets if this entity is going to collide with a wall.
     */
    private fun isCollidingWithWall(): Boolean {
        val currentLocationVector = Vec3D(this.dc(), this.de(), this.di())
        val directionVector =
            Vec3D(this.dc() + this.da().b * 1.5, this.de() + this.da().c * 1.5,  this.di() + this.da().d * 1.5)
        val rayTrace = RayTrace(
            currentLocationVector,
            directionVector,
            RayTrace.BlockCollisionOption.a,
            RayTrace.FluidCollisionOption.a,
            null
        )

        val movingObjectPosition = this.cA().a(rayTrace)

        return movingObjectPosition.c() == MovingObjectPosition.EnumMovingObjectType.b && hasFlyCollisionsEnabled
    }

    /**
     * Handles the riding on ground.
     */
    private fun rideOnGround(human: EntityHuman, ai: AIGroundRiding, f2: Double) {
        val sideMot: Float = human.bo * 0.5f
        var forMot: Float = human.bq

        o(human.dn())
        this.x = this.dn()
        this.p(human.dn() * 0.5f)
        this.a(dn(), dn())
        this.aX = this.dn()
        this.aZ = this.aX

        if (forMot <= 0.0f) {
            forMot *= 0.25f
        }

        if (this.aw() && this.isPassengerJumping()) {
           this.n(this.da().b, 0.5, this.da().d)
        }

        this.P = ai.climbingHeight.toFloat()
        this.ba = this.ew() * 0.1f

        if (!this.cA().k_()) {
            this.r(0.35f)
            super.h(Vec3D(sideMot * ai.ridingSpeed, f2, forMot * ai.ridingSpeed))
        }
    }

    /**
     * Applies the entity NBT to the hitbox.
     */
    private fun applyNBTTagToHitBox(hitBox: EntityInsentient) {
        val compound = NBTTagCompound()
        hitBox.b(compound) // SaveData
        applyAIEntityNbt(
            compound,
            this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.hitBoxNbt }.toList()
        )
        hitBox.a(compound) // LoadData
        // CustomNameVisible does not working via NBT Tags.
        hitBox.n(compound.e("CustomNameVisible") && compound.h("CustomNameVisible") == 1)
    }

    /**
     * Applies the entity NBT to the armorstand.
     */
    private fun applyNBTTagForArmorstand() {
        val compound = NBTTagCompound()
        this.b(compound) // SaveData
        applyAIEntityNbt(
            compound,
            this.petMeta.aiGoals.asSequence().filterIsInstance<AIEntityNbt>().map { a -> a.armorStandNbt }.toList()
        )
        this.a(compound) // LoadData
        // CustomNameVisible does not working via NBT Tags.
        this.n(compound.e("CustomNameVisible") && compound.h("CustomNameVisible") == 1)
    }

    /**
     * Applies the raw NbtData to the given target.
     */
    private fun applyAIEntityNbt(target: NBTTagCompound, rawNbtDatas: List<String>) {
        val compoundMapField = NBTTagCompound::class.java.getDeclaredField("x")
        compoundMapField.isAccessible = true
        val rootCompoundMap = compoundMapField.get(target) as MutableMap<Any?, Any?>

        for (rawNbtData in rawNbtDatas) {
            if (rawNbtData.isEmpty()) {
                continue
            }

            val parsedCompound = try {
                MojangsonParser.a(rawNbtData)
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
        return cF() != null && this.cF().isNotEmpty() && jumpingField.getBoolean(this.cF()[0])
    }
}
