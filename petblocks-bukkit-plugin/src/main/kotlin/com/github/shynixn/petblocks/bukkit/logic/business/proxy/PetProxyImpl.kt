package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.bukkit.event.PetRemoveEvent
import com.github.shynixn.petblocks.api.bukkit.event.PetRideEvent
import com.github.shynixn.petblocks.api.bukkit.event.PetSpawnEvent
import com.github.shynixn.petblocks.api.bukkit.event.PetWearEvent
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.Skin
import com.github.shynixn.petblocks.bukkit.logic.business.extension.setDisplayName
import com.github.shynixn.petblocks.bukkit.logic.business.extension.setSkin
import com.github.shynixn.petblocks.bukkit.logic.business.extension.setUnbreakable
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toVector
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

@Suppress("UNCHECKED_CAST")
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
class PetProxyImpl(override val meta: PetMeta, private val design: ArmorStand, private val nmsProxy: NMSPetProxy, private val hitBox: LivingEntity, private val owner: Player) :
    PetProxy, Runnable {

    var teleportTarget: Location? = null
    var aiGoals: List<Any>? = null

    private val particleService = PetBlocksApi.resolve<ParticleService>(ParticleService::class.java)
    private val soundService = PetBlocksApi.resolve<SoundService>(SoundService::class.java)
    private val logger: LoggingService = PetBlocksApi.resolve(LoggingService::class.java)
    private val itemService = PetBlocksApi.resolve<ItemService>(ItemService::class.java)
    private val aiService = PetBlocksApi.resolve<AIService>(AIService::class.java)

    /**
     * Gets if the pet is dead or was removed.
     */
    override val isDead: Boolean
        get() = this.design.isDead || hitBox.isDead

    /**
     * Init.
     */
    init {
        design.bodyPose = EulerAngle(0.0, 0.0, 2878.0)
        design.leftArmPose = EulerAngle(2878.0, 0.0, 0.0)
        design.setMetadata("keep", FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true))
        design.isCustomNameVisible = true
        design.removeWhenFarAway = false

        hitBox.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1))
        hitBox.setMetadata("keep", FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true))
        hitBox.isCustomNameVisible = false
        hitBox.customName = "PetBlockIdentifier"

        meta.enabled = true
        meta.propertyTracker.onPropertyChanged(PetMeta::displayName)
        meta.propertyTracker.onPropertyChanged(Skin::typeName)

        val event = PetSpawnEvent(this)
        Bukkit.getPluginManager().callEvent(event)
    }

    /**
     * Gets the pet owner.
     */
    override fun <P> getPlayer(): P {
        return owner as P
    }

    /**
     * Gets the head armorstand.
     */
    override fun <A> getHeadArmorstand(): A {
        return design as A
    }

    /**
     * Gets the head of the head armorstand.
     */
    override fun <I> getHeadArmorstandItemStack(): I {
        return design.helmet.clone() as I
    }

    /**
     * Gets a living hitbox entity.
     */
    override fun <L> getHitBoxLivingEntity(): L {
        return hitBox as L
    }

    /**
     * Sets the entity wearing the pet.
     */
    fun startWearing() {
        if (design.passenger != null) {
            return
        }

        val event = PetWearEvent(false, this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        design.isCustomNameVisible = false
        //designNbtChange["Marker"] = true
        //   hitBoxNbtChange["NoAI"] = true

        owner.passenger = design
        owner.closeInventory()
    }

    /**
     * Gets called from any Movement AI to play movement effects.
     */
    fun playMovementEffects() {
        try {
            for (aiBase in meta.aiGoals) {
                if (aiBase is AIMovement) {
                    val location = getLocation<Location>()

                    particleService.playParticle(location, aiBase.movementParticle, owner)
                    soundService.playSound(location, aiBase.movementSound, owner)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to play moving sound and particle.", e)
        }
    }

    /**
     * Stops the current target wearing the pet.
     */
    fun stopWearing() {
        /**
         *
        /**
         * Runnable value which represents internal nbt changes of the design armorstand.
         * Gets automatically applied next pet tick.
        */
        override val designNbtChange = HashMap<String, Any>()
        /**
         * Runnable value which represents internal nbt changes of the hitboxEntity.
         * Gets automatically applied next pet tick.
        */
        override val hitBoxNbtChange = HashMap<String, Any>()

        if (design.passenger == null) {
        return
        }

        val event = PetWearEvent(true, this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
        return
        }

        design.isCustomNameVisible = true
        designNbtChange["Marker"] = false
        hitBoxNbtChange["NoAI"] = false

        owner.eject()    */
    }

    /**
     * Starts riding the pet.zg
     */
    fun startRiding() {
        if (owner.passenger != null) {
            return
        }

        val event = PetRideEvent(false, this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        design.velocity = Vector(0, 1, 0)
        design.passenger = owner
        owner.closeInventory()
    }

    /**
     * Stops the current target riding the pet.
     */
    fun stopRiding() {
        if (owner.passenger != null) {
            return
        }

        val event = PetRideEvent(true, this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        owner.eject()
    }

    /**
     * Teleports the pet to the given [location].
     */
    override fun <L> teleport(location: L) {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        teleportTarget = location
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        if (!meta.enabled && !isDead) {
            Bukkit.getPluginManager().callEvent(PetRemoveEvent(this))
            (this.design as EntityPetProxy).deleteFromWorld()
            (this.hitBox as EntityPetProxy).deleteFromWorld()

            return
        }

        val displayNameChanged = PetMeta::displayName.hasChanged(meta)

        if (displayNameChanged) {
            design.customName = meta.displayName.translateChatColors()
        }

        if (displayNameChanged || Skin::typeName.hasChanged(meta.skin)) {
            val itemStack = itemService.createItemStack<ItemStack>(meta.skin.typeName, meta.skin.dataValue)

            itemStack.setDisplayName(meta.displayName)
            itemStack.setSkin(meta.skin.owner)
            itemStack.setUnbreakable(meta.skin.unbreakable)

            design.helmet = itemStack
        }

        // TODO: Keep this check until packet problem appears
        val hasPassenger = getHitBoxLivingEntity<LivingEntity>().passengers

        if (PetMeta::aiGoals.hasChanged(meta)) {
            aiGoals = aiService.convertPetAiBasesToPathfinders(this, meta.aiGoals)
        }
    }

    /**
     * Gets the location of the pet.
     */
    override fun <L> getLocation(): L {
        return hitBox.location as L
    }

    /**
     * Removes the pet.
     */
    override fun remove() {
        meta.enabled = false
    }

    /**
     * Sets the velocity of the pet.
     */
    override fun <V> setVelocity(vector: V) {
        if (vector is Position) {
            hitBox.velocity = vector.toVector()
            return
        }

        if (vector !is Vector) {
            throw IllegalArgumentException("Vector has to be a BukkitVector!")
        }

        hitBox.velocity = vector
    }

    /**
     * Gets the velocity of the pet.
     */
    override fun <V> getVelocity(): V {
        return hitBox.velocity as V
    }
}