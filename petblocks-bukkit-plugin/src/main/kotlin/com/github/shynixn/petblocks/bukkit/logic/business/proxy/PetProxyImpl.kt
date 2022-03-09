package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.bukkit.event.PetRemoveEvent
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.ArmorstandPetProxy
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toLocation
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toVector
import com.github.shynixn.petblocks.core.logic.business.extension.hasChanged
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
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
    var hitBox: LivingEntity? = null

    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
    private val soundService = PetBlocksApi.resolve(SoundService::class.java)
    private val logger: LoggingService = PetBlocksApi.resolve(LoggingService::class.java)
    private val itemService = PetBlocksApi.resolve(ItemTypeService::class.java)
    private val dependencyService = PetBlocksApi.resolve(DependencyService::class.java)
    private val version = PetBlocksApi.resolve(Version::class.java)
    private var placeHolderApiService: DependencyPlaceholderApiService? = null

    /**
     * Init.
     */
    init {
        meta.enabled = true

        meta.propertyTracker.onPropertyChanged(PetMeta::displayName)
        meta.propertyTracker.onPropertyChanged(PetMeta::aiGoals)
        meta.propertyTracker.onPropertyChanged(Skin::typeName)

        (design as ArmorstandPetProxy).setBootsItemStack(generateMarkerItemStack())

        if (dependencyService.isInstalled(PluginDependency.PLACEHOLDERAPI)) {
            placeHolderApiService = PetBlocksApi.resolve(DependencyPlaceholderApiService::class.java)
        }
    }

    /**
     * Gets if the pet is dead or was removed.
     */
    override val isDead: Boolean
        get() = this.design.isDead || (hitBox != null && hitBox!!.isDead)

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
                    val location = getLocation<Location>()

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
        if (hitBox !is LivingEntity?) {
            return
        }

        this.hitBox = hitBox

        if (hitBox == null) {
            return
        }

        hitBox.equipment!!.boots = generateMarkerItemStack()
    }

    /**
     * Teleports the pet to the given [location].
     */
    override fun <L> teleport(location: L) {
        var target: Any = location as Any

        if (target is Position) {
            target = target.toLocation()
        }

        if (target !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        teleportTarget = target
    }

    /**
     * Triggers a manual tick for tickless ais.
     */
    override fun triggerTick() {
        val handle =
            findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")
                .invoke(getHeadArmorstand())

        try {
            val method =
                findClazz("net.minecraft.world.entity.EntityLiving")
                    .getDeclaredMethod("doTick")
            method.isAccessible = true
            method.invoke(handle)
        } catch (e: Exception) {
            try {
                val method =
                    findClazz("com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetArmorstand")
                        .getDeclaredMethod("doTick")
                method.isAccessible = true
                method.invoke(handle)
            } catch (e2: Exception) {
                if(version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R2)){
                    val method =
                        findClazz("com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetArmorstand")
                            .getDeclaredMethod("eF")
                    method.isAccessible = true
                    method.invoke(handle)
                }else{
                    val method =
                        findClazz("com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.NMSPetArmorstand")
                            .getDeclaredMethod("eE")
                    method.isAccessible = true
                    method.invoke(handle)
                }
            }
        }
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
            Bukkit.getPluginManager().callEvent(event)

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
            design.customName = meta.displayName.translateChatColors()
        }

        if (placeHolderApiService != null) {
            design.customName = placeHolderApiService!!.applyPlaceHolders(owner, meta.displayName).translateChatColors()
        }

        if (displayNameChanged || Skin::typeName.hasChanged(meta.skin)) {
            val item = ItemEntity(
                meta.skin.typeName,
                meta.skin.dataValue,
                meta.skin.nbtTag,
                meta.displayName,
                null,
                meta.skin.owner
            )

            val itemStack = itemService.toItemStack<ItemStack>(item)
            (design as ArmorstandPetProxy).setHelmetItemStack(itemStack)
        }
    }

    /**
     * Gets the location of the pet.
     */
    override fun <L> getLocation(): L {
        if (hitBox == null) {
            return this.design.location as L
        }

        val movementAi = this.meta.aiGoals.firstOrNull { a -> a is AIMovement } as AIMovement?
        val location = hitBox!!.location

        if (movementAi == null) {
            return location as L
        }

        return location.add(0.0, movementAi.movementYOffSet + 1, 0.0) as L
    }

    /**
     * Removes the pet.
     */
    override fun remove() {
        meta.enabled = false
        triggerTick()
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

        if (vector !is Vector) {
            throw IllegalArgumentException("Vector has to be a BukkitVector!")
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
        val item = ItemEntity("APPLE", 0, "", null, arrayListOf("PetBlocks"))
        return itemService.toItemStack(item)
    }
}
