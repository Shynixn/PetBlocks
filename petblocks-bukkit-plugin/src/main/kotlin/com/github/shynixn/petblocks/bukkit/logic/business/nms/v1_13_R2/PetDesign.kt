package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_13_R2

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIHopping
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.PetProxyImpl
import net.minecraft.server.v1_13_R2.*
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
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
class PetDesign(owner: Player, val petMeta: PetMeta, entityType: EntityType) : EntityArmorStand((owner.location.world as CraftWorld).handle), NMSPetProxy {
    private var internalProxy: PetProxyImpl? = null
    private var jumpingField: Field = EntityLiving::class.java.getDeclaredField("bg")
    private var hitBox: PetRabbitHitBox

    companion object {
        private val axisAlignmentFields = arrayOfNulls<Field?>(5)

        /**
         * The name of the axis alignment fields changed from 1.13.1 to 1.13.2 but the
         * NMS layer is still the same.
         */
        init {
            try {
                axisAlignmentFields[0] = AxisAlignedBB::class.java.getDeclaredField("minX")
                axisAlignmentFields[1] = AxisAlignedBB::class.java.getDeclaredField("minY")
                axisAlignmentFields[2] = AxisAlignedBB::class.java.getDeclaredField("minZ")
                axisAlignmentFields[3] = AxisAlignedBB::class.java.getDeclaredField("maxX")
                axisAlignmentFields[4] = AxisAlignedBB::class.java.getDeclaredField("maxZ")
            } catch (ex: NoSuchFieldException) {
                try {
                    axisAlignmentFields[0] = AxisAlignedBB::class.java.getDeclaredField("a")
                    axisAlignmentFields[1] = AxisAlignedBB::class.java.getDeclaredField("b")
                    axisAlignmentFields[2] = AxisAlignedBB::class.java.getDeclaredField("c")
                    axisAlignmentFields[3] = AxisAlignedBB::class.java.getDeclaredField("d")
                    axisAlignmentFields[4] = AxisAlignedBB::class.java.getDeclaredField("f")
                } catch (e: NoSuchFieldException) {
                    throw RuntimeException("Fields could not get located.", e)
                }
            }
        }
    }

    /**
     * Proxy handler.
     */
    override val proxy: PetProxyImpl get() = internalProxy!!

    /**
     * Initializes the nms design.
     */
    init {
        jumpingField.isAccessible = true

        val location = owner.location
        val mcWorld = (location.world as CraftWorld).handle
        this.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        hitBox = if (entityType == EntityType.RABBIT) {
            PetRabbitHitBox(owner, this, location)
        } else {
            PetRabbitHitBox(owner, this, location)
        }

        internalProxy = PetProxyImpl(petMeta, this.bukkitEntity as ArmorStand, this, hitBox.bukkitEntity as LivingEntity, owner)

        val compound = NBTTagCompound()
        compound.setBoolean("invulnerable", true)
        compound.setBoolean("Invisible", true)
        compound.setBoolean("PersistenceRequired", true)
        compound.setBoolean("ShowArms", true)
        compound.setBoolean("NoBasePlate", true)
        this.a(compound)

        hitBox.passengers.add(this)
    }

    /**
     * Applies pathfinder to the entity.
     */
    override fun applyPathfinder(pathfinderProxies: PathfinderProxy) {
        hitBox.applyPathfinder(pathfinderProxies)
    }

    /**
     * Entity tick.
     */
    override fun doTick() {
        super.doTick()

        if (proxy.teleportTarget != null) {
            val location = proxy.teleportTarget!!
            this.hitBox.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
            this.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
            proxy.teleportTarget = null
        }

        proxy.run()
    }

    /**
     * Overrides the moving of the pet design.
     */
    override fun move(enummovetype: EnumMoveType?, d0: Double, d1: Double, d2: Double) {
        super.move(enummovetype, d0, d1, d2)

        if (this.passengers != null && this.passengers.firstOrNull { p -> p is EntityHuman } != null) {
            val axisBoundingBox = this.boundingBox

            val minXA = axisAlignmentFields[0]!!.getDouble(axisBoundingBox)
            val minXB = axisAlignmentFields[1]!!.getDouble(axisBoundingBox)
            val minXC = axisAlignmentFields[2]!!.getDouble(axisBoundingBox)
            val maxXD = axisAlignmentFields[3]!!.getDouble(axisBoundingBox)
            val maxXF = axisAlignmentFields[4]!!.getDouble(axisBoundingBox)

            this.locX = (minXA + maxXD) / 2.0
            this.locY = minXB + 0
            this.locZ = (minXC + maxXF) / 2.0
        }
    }

    /**
     * Gets if a passenger of the pet is jumping.
     */
    private fun isPassengerJumping(): Boolean {
        return passengers != null && !this.passengers.isEmpty() && jumpingField.getBoolean(this.passengers[0])
    }

    /**
     * Riding function.
     */
    override fun a(sidemot: Float, f2: Float, formot: Float) {
        val sideMot: Float
        var forMot: Float

        if (this.passengers == null || this.passengers.firstOrNull { p -> p is EntityHuman } == null) {
            return
        }

        val human = this.passengers.first { p -> p is EntityHuman } as EntityHuman

        this.yaw = human.yaw
        this.lastYaw = this.yaw
        this.pitch = human.pitch * 0.5f
        this.setYawPitch(this.yaw, this.pitch)
        this.aQ = this.yaw
        this.aS = this.aQ

        sideMot = human.bh * 0.5f
        forMot = human.bj

        if (forMot <= 0.0f) {
            forMot *= 0.25f
        }

        if (this.onGround && this.isPassengerJumping()) {
            this.motY = 0.5
        }

        this.Q = 2.0F // TODO: Climing height
        this.aU = this.cK() * 0.1f

        if (!this.world.isClientSide) {
            this.o(0.35f)

            // TODO: Pet Riding speed modifier
            super.a(sideMot, f2, forMot)
        }

        this.aI = this.aJ
        val d0 = this.locX - this.lastX
        val d1 = this.locZ - this.lastZ
        var f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0f

        if (f4 > 1.0f) {
            f4 = 1.0f
        }

        this.aJ += (f4 - this.aJ) * 0.4f
        this.aK += this.aJ
    }
}