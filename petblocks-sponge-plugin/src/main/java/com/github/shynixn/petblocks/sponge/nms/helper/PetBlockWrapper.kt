package com.github.shynixn.petblocks.sponge.nms.helper

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.entity.EffectPipeline
import com.github.shynixn.petblocks.api.business.entity.PetBlock
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.sponge.event.PetBlockCannonEvent
import com.github.shynixn.petblocks.api.sponge.event.PetBlockRideEvent
import com.github.shynixn.petblocks.api.sponge.event.PetBlockWearEvent
import com.github.shynixn.petblocks.core.logic.business.helper.ReflectionUtils
import com.github.shynixn.petblocks.sponge.logic.business.entity.Pipeline
import com.github.shynixn.petblocks.sponge.logic.business.helper.findServerVersion
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeSoundBuilder
import com.github.shynixn.petblocks.sponge.nms.VersionSupport
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.World

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
class PetBlockWrapper(firstSpawn: Transform<World>, private val owner: Player, private val petMeta: PetMeta) : PetBlock<Player, Transform<World>> {

    object Companion {
        var spawnMethod = Class.forName("com.github.shynixn.petblocks.sponge.nms.VERSION.CustomGroundArmorstand".replace("VERSION", VersionSupport.getServerVersion().versionText)).getDeclaredMethod("spawn", Transform::class.java)


        var engine = Class.forName("com.github.shynixn.petblocks.sponge.nms.VERSION.CustomGroundArmorstand".replace("VERSION", VersionSupport.getServerVersion().versionText))
                .getDeclaredField("rabbit")
    }

    private var engine: Living
    private var armorstandEntity: ArmorStand

    private val pipeline = Pipeline(this)

    var health = 20.0
    private var dieng = false
    var hitflor: Boolean = false

    private val explosionSound = SpongeSoundBuilder("EXPLODE", 1.0, 2.0)

    init {
        armorstandEntity = ReflectionUtils.invokeConstructor(Class.forName("com.github.shynixn.petblocks.sponge.nms.VERSION.CustomGroundArmorstand".findServerVersion()), arrayOf(player.transform.javaClass, PetBlockWrapper::class.java), arrayOf(firstSpawn, this)) as ArmorStand
        Companion.engine.isAccessible = true
        val partWrapper = Companion.engine.get(armorstandEntity) as PetBlockPartWrapper
        this.engine = partWrapper.entity as Living
    }

    override fun setSkin(skin: String?) {
        throw RuntimeException("Not implemented!")
    }

    override fun setSkin(material: Any?, data: Byte) {
        throw RuntimeException("Not implemented!")
    }

    override fun setDieing() {
        if (!isDieing) {
            jump()
            if (!armorstandEntity.isRemoved) {
                armorstandEntity.headRotation = Vector3d(0f, 1f, 0f)
                armorstandEntity.remove()
            }

        }
    }

    override fun teleportWithOwner(location: Any?) {
        teleport(location as Transform<World>)
    }

    override fun isDieing(): Boolean {
        return dieng
    }

    /**
     * Returns the pipeline for managed effect playing.
     * @return pipeline
     */
    override fun getEffectPipeline(): EffectPipeline<Transform<World>> {
        return pipeline
    }

    /**
     * Returns the meta of the petblock.
     *
     * @return meta
     */
    override fun getMeta(): PetMeta {
        return petMeta
    }

    /**
     * Returns the owner of the petblock.
     *
     * @return player
     */
    override fun getPlayer(): Player {
        return this.owner
    }

    /**
     * Removes the petblock.
     */
    override fun remove() {
        (this.engineEntity as Living).remove()

        if (!(this.armorStand as Living).isRemoved) {
            this.armorstandEntity.remove()
        }
    }

    /**
     * Lets the given player ride on the petblock.
     *
     * @param player player
     */
    override fun ride(player: Player) {
        if (armorstandEntity.passengers.isEmpty() && player.passengers.isEmpty()) {
            val event = PetBlockRideEvent(this, true)
            Sponge.getEventManager().post(event)
            if (!event.isCancelled) {
                armorstandEntity.velocity = Vector3d(0f, 1f, 0f)
                armorstandEntity.addPassenger(player)
                player.closeInventory()
            }
        }
    }

    /**
     * Lets the given player wear the petblock.
     *
     * @param player player
     */
    override fun wear(player: Player) {
        if (this.armorstandEntity.passengers.isEmpty() && player.passengers.isEmpty()) {
            val armorStand = this.armorStand as ArmorStand
            armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
            armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, false)
            engine.offer(Keys.AI_ENABLED, false)

            val event = PetBlockWearEvent(this, true)
            Sponge.getEventManager().post(event)
            if (!event.isCancelled) {
                player.addPassenger(armorstandEntity)
                player.closeInventory()
            }
        }
    }

    /**
     * Ejects the given player riding from the petblock.
     *
     * @param player player
     */
    override fun eject(player: Player) {
        val armorStand = this.armorStand as ArmorStand
        armorStand.offer(Keys.ARMOR_STAND_MARKER, false)
        armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, true)
        engine.offer(Keys.AI_ENABLED, true)

        val event = PetBlockWearEvent(this, false)
        Sponge.getEventManager().post(event)
        if (!event.isCancelled) {
            player.clearPassengers()
        }
    }

    /**
     * Sets the displayName of the petblock.
     *
     * @param name name
     */
    override fun setDisplayName(name: String?) {
        armorstandEntity.offer(Keys.DISPLAY_NAME, Text.of(name))
        armorstandEntity.offer(Keys.CUSTOM_NAME_VISIBLE, true)
    }

    /**
     * Returns the displayName of the petblock.
     *
     * @return name
     */
    override fun getDisplayName(): String {
        return (this.armorStand as ArmorStand).get<Text>(Keys.DISPLAY_NAME).get().toPlain()
    }

    /**
     * Respawns the petblock
     */
    override fun respawn() {
        val location = location
        location.add(Transform(location.extent, Vector3d(0.0, 2.2, 0.0)))
        remove()

        Companion.spawnMethod.isAccessible = true
        Companion.spawnMethod.invoke(armorstandEntity, location)
    }

    /**
     * Returns if the petblock is already removed or dead.
     *
     * @return dead
     */
    override fun isDead(): Boolean {
        return engine.isRemoved
                || armorstandEntity.isRemoved
                || (engine.world.name == armorstandEntity.world.name
                && engine.location.position.distance(armorstandEntity.location.position) > 10)
    }

    /**
     * Returns the armorstand of the petblock.
     *
     * @return armorstand
     */
    override fun getArmorStand(): Any {
        return armorstandEntity
    }

    /**
     * Returns the entity being used as engine.
     *
     * @return entity
     */
    override fun getEngineEntity(): Any {
        return engine
    }

    /**
     * Returns the location of the entity.
     *
     * @return position
     */
    override fun getLocation(): Transform<World> {
        return this.armorstandEntity.transform
    }

    /**
     * Damages the petblock the given amount of damage.
     *
     * @param amount amount
     */
    override fun damage(amount: Double) {
        if (amount < -1.0) {
            this.hitflor = true
        } else {
            if (!Config.isCombat_invincible) {
                health -= amount
                if (health <= 0) {
                    dieng = true
                }
            }
        }
    }

    /**
     * Lets the petblock perform a jump.
     */
    override fun jump() {
        engine.velocity = Vector3d(0.0, 0.5, 0.0)
    }

    /**
     * Sets the velocity of the petblock.
     *
     * @param vector vector
     */
    override fun setVelocity(vector: Any) {
        val event = PetBlockCannonEvent(this)
        Sponge.getEventManager().post(event)
        if (!event.isCancelled) {
            engine.velocity = vector as Vector3d
            effectPipeline.playSound(location, explosionSound)
        }
    }

    /**
     * Teleports the the petblock to the given location.
     *
     * @param location location
     */
    override fun teleport(location: Transform<World>) {
        (engineEntity as Living).transform = location
        (armorstandEntity).transform = location
    }
}