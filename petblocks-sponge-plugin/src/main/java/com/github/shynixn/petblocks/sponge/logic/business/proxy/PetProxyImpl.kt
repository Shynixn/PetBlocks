package com.github.shynixn.petblocks.sponge.logic.business.proxy

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.ItemService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.Skin
import com.github.shynixn.petblocks.api.sponge.event.PetRemoveEvent
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.sponge.logic.business.extension.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.world.World
import java.util.*

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
class PetProxyImpl(override val meta: PetMeta, private val design: ArmorStand, private val owner: Player) :
    PetProxy {

    override var teleportTarget: Any? = null
    override var aiGoals: List<Any>? = null
    var hitBox: Living? = null

    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
    private val soundService = PetBlocksApi.resolve(SoundService::class.java)
    private val logger: LoggingService = PetBlocksApi.resolve(LoggingService::class.java)
    private val itemService = PetBlocksApi.resolve(ItemService::class.java)

    /**
     * Init.
     */
    init {
        design.bodyPartRotationalData.bodyRotation().set(Vector3d(0.0, 0.0, 2878.0))
        design.bodyPartRotationalData.bodyRotation().set(Vector3d(2878.0, 0.0, 0.0))
        design.offer(Keys.CUSTOM_NAME_VISIBLE, true);

        meta.enabled = true

        meta.propertyTracker.onPropertyChanged(PetMeta::displayName)
        meta.propertyTracker.onPropertyChanged(PetMeta::aiGoals)
        meta.propertyTracker.onPropertyChanged(Skin::typeName)

        design.setBoots(generateMarkerItemStack())
    }

    /**
     * Gets if the pet is dead or was removed.
     */
    override val isDead: Boolean
        get() = this.design.isRemoved || (hitBox != null && hitBox!!.isRemoved)

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
        if (!design.helmet.isPresent) {
            return null as I
        }

        return design.helmet.get().copy() as I
    }

    /**
     * Gets a living hitbox entity.
     */
    override fun <L> getHitBoxLivingEntity(): Optional<L> {
        return Optional.ofNullable(hitBox as L)
    }

    /**
     * Gets called from any Movement AI to play movement effects.
     */
    override fun playMovementEffects() {
        try {
            for (aiBase in meta.aiGoals) {
                if (aiBase is AIMovement) {
                    val location = getLocation<Transform<World>>()

                    if (meta.particleEnabled) {
                        particleService.playParticle(location, aiBase.movementParticle, owner)
                    }

                    if (meta.soundEnabled) {
                        soundService.playSound(location, aiBase.movementSound, owner)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to play moving sound and particle.", e)
        }
    }

    /**
     * Gets called when the hitbox changes.
     */
    override fun changeHitBox(hitBox: Any?) {
        if (hitBox !is Living?) {
            return
        }

        this.hitBox = hitBox

        if (hitBox == null) {
            return
        }

        val effect = PotionEffect.builder()
            .potionType(PotionEffectTypes.INVISIBILITY)
            .duration(9999999).amplifier(1).build()
        val effects = hitBox.getOrCreate(PotionEffectData::class.java).get()
        effects.addElement(effect)
        hitBox.offer(effects)

        hitBox.offer(Keys.CUSTOM_NAME_VISIBLE, false)

        (this.hitBox as EntityPetProxy).bootsItemStack = generateMarkerItemStack()
    }

    /**
     * Teleports the pet to the given [location].
     */
    override fun <L> teleport(location: L) {
        var target: Any = location as Any

        if (target is Position) {
            target = target.toTransform()
        }

        if (target !is Transform<*>) {
            throw IllegalArgumentException("Location has to be a SpongeLocation!")
        }

        teleportTarget = target
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
            val event = PetRemoveEvent(getPlayer(), this)
            Sponge.getEventManager().post(event)

            if (event.isCancelled) {
                meta.enabled = true
                return
            }

            (this.design as EntityPetProxy).deleteFromWorld()

            if (this.hitBox != null) {
                (this.hitBox as EntityPetProxy).deleteFromWorld()
            }

            return
        }

        val displayNameChanged = PetMeta::displayName.hasChanged(meta)

        if (displayNameChanged) {
            design.offer(Keys.DISPLAY_NAME, meta.displayName.toText())
        }

        if (displayNameChanged || Skin::typeName.hasChanged(meta.skin)) {
            var itemStack = itemService.createItemStack(meta.skin.typeName, meta.skin.dataValue).build<ItemStack>()

            itemStack.displayName = meta.displayName
            itemStack.skin = meta.skin.owner

            if (meta.skin.unbreakable) {
                itemStack = itemStack.createUnbreakableCopy()
            }

            design.setHelmet(itemStack)
        }
    }

    /**
     * Gets the location of the pet.
     */
    override fun <L> getLocation(): L {
        if (hitBox == null) {
            return this.design.transform as L
        }

        return hitBox!!.transform as L
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
            if (hitBox != null) {
                hitBox!!.velocity = vector.toVector()
            } else {
                design.velocity = vector.toVector()
            }

            return
        }

        if (vector !is Vector3d) {
            throw IllegalArgumentException("Vector has to be a SpongeVector!")
        }

        if (hitBox != null) {
            hitBox!!.velocity = vector
        } else {
            design.velocity = vector
        }
    }

    /**
     * Gets the velocity of the pet.
     */
    override fun <V> getVelocity(): V {
        if (hitBox == null) {
            return this.design.velocity as V
        }

        return hitBox!!.velocity as V
    }

    /**
     * Gets a new marker itemstack.
     */
    private fun generateMarkerItemStack(): ItemStack {
        val item = ItemStack.builder().itemType(ItemTypes.APPLE).build()
        item.lore = arrayListOf("PetBlocks")
        return item
    }
}