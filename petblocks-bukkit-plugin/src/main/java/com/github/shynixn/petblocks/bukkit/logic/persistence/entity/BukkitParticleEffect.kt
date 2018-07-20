package com.github.shynixn.petblocks.bukkit.logic.persistence.entity

import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEffectData
import net.minecraft.server.v1_13_R1.Particle
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.logging.Level

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
class BukkitParticleEffect : ParticleEffectData, ConfigurationSerializable {
    constructor() : super()
    constructor(items: MutableMap<String, Any>?) : super(items)

    /**
     * Sets the name of the material
     * @param name name
     * @return builder
     */
    override fun setMaterialName(name: String?): ParticleEffectMeta {
        this.materialId = Material.getMaterial(name).id
        return this
    }

    /**
     * Returns the name of the material.
     * @return material
     */
    override fun getMaterialName(): String {
        return Material.getMaterial(materialId).name
    }

    /**
     * Plays the effect at the given location to the given players.
     *
     * @param tmpLocation location
     * @param tmpPlayers  players
     */
    override fun <Location, Player> applyTo(tmpLocation: Location?, vararg tmpPlayers: Player) {
        try {
            if (tmpLocation == null) {
                throw IllegalArgumentException("Location cannot be null!")
            }

            val location = tmpLocation as org.bukkit.Location
            val players = tmpPlayers as Array<org.bukkit.entity.Player>

            if (this.effect == "none")
                return
            val playingPlayers: Array<org.bukkit.entity.Player>

            playingPlayers = if (tmpPlayers.isEmpty()) {
                location.world.players.toTypedArray()
            } else {
                players
            }

            val speed: Float
            val amount: Int
            if (this.effect == ParticleEffectMeta.ParticleEffectType.REDSTONE.simpleName || this.effect == ParticleEffectMeta.ParticleEffectType.NOTE.simpleName) {
                amount = 0
                speed = 1.0f
            } else {
                amount = this.amount
                speed = this.speed.toFloat()
            }
            val enumParticle = getEnumParticle(this.effectType.name.toUpperCase())
            var additionalInfo: IntArray? = null
            if (this.material != null) {
                additionalInfo = if (this.effectType == ParticleEffectMeta.ParticleEffectType.ITEM_CRACK) {
                    intArrayOf(this.materialId, this.data!!.toInt())
                } else {
                    intArrayOf(this.materialId, (this.data!!.toInt() shl 12))
                }
            }
            val packet = invokeConstructor(findClass("net.minecraft.server.VERSION.PacketPlayOutWorldParticles"), arrayOf(enumParticle.javaClass, Boolean::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Float::class.javaPrimitiveType, Int::class.javaPrimitiveType, IntArray::class.java)
                    , arrayOf(enumParticle, isLongDistance(location, players), location.x.toFloat(), location.y.toFloat(), location.z.toFloat(), this.offsetX.toFloat(), this.offsetY.toFloat(), this.offsetZ.toFloat(), speed, amount, additionalInfo))
            for (player in playingPlayers) {
                sendPacket(player, packet)
            }
        } catch (e: Exception) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to send packet.", e)
        }
    }

    private fun getEnumParticle(name: String): Any {
        val clazz = findClass("net.minecraft.server.VERSION.EnumParticle")
        val method = clazz.getDeclaredMethod("valueOf", String::class.java)
        return method.invoke(null, name)
    }

    @Throws(ClassNotFoundException::class, IllegalAccessException::class, NoSuchMethodException::class, InvocationTargetException::class, NoSuchFieldException::class)
    private fun sendPacket(player: Player, packet: Any) {
        val craftPlayer = findClass("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer").cast(player)
        val methodHandle = craftPlayer.javaClass.getDeclaredMethod("getHandle")
        val entityPlayer = methodHandle.invoke(craftPlayer)
        val field = findClass("net.minecraft.server.VERSION.EntityPlayer").getDeclaredField("playerConnection")
        field.isAccessible = true
        val connection = field.get(entityPlayer)
        val sendMethod = connection.javaClass.getDeclaredMethod("sendPacket", packet.javaClass.interfaces[0])
        sendMethod.invoke(connection, packet)
    }

    /**
     * Sets the material of the particleEffect
     *
     * @param material material
     * @return builder
     */
    override fun setMaterial(material: Any?): ParticleEffectData {
        if (material != null && material is Int) {
            this.materialId = material
        } else if (material != null) {
            this.materialId = MaterialCompatibility13.getIdFromMaterial(material as Material?)
        } else {
            this.materialId = null
        }
        return this
    }

    /**
     * Returns the material of the particleEffect
     *
     * @return material
     */
    override fun getMaterial(): Any? {
        return if (this.materialId == null || MaterialCompatibility13.getMaterialFromId(materialId) == null) null else MaterialCompatibility13.getMaterialFromId(materialId)
    }

    /**
     * Invokes a constructor by the given parameters
     *
     * @param clazz      clazz
     * @param paramTypes paramTypes
     * @param params     params
     * @return instance
     * @throws NoSuchMethodException     exception
     * @throws IllegalAccessException    exception
     * @throws InvocationTargetException exception
     * @throws InstantiationException    exception
     */
    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    private fun invokeConstructor(clazz: Class<*>?, paramTypes: Array<Class<*>?>, params: Array<Any?>): Any {
        val constructor = clazz!!.getDeclaredConstructor(*paramTypes)
        constructor.isAccessible = true
        return constructor.newInstance(*params)
    }

    /**
     * Finds a class regarding of the server Version
     *
     * @param name name
     * @return clazz
     * @throws ClassNotFoundException exception
     */
    @Throws(ClassNotFoundException::class)
    private fun findClass(name: String): Class<*> {
        return Class.forName(name.replace("VERSION", Bukkit.getServer().javaClass.`package`.name.replace(".", ",").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]))
    }

    /**
     * Checks if longDistance attribute is necessary
     *
     * @param location location
     * @param players  players
     * @return isNecessary
     */
    private fun isLongDistance(location: Location, players: Array<out Player>): Boolean {
        return players.any { location.world.name == it.location.world.name && it.location.distanceSquared(location) > 65536 }
    }


    /**
     * Copies the current object.
     *
     * @return copy.
     */
    override fun copy(): ParticleEffectMeta {
        val particle = BukkitParticleEffect()
        particle.effect = this.effect
        particle.amount = this.amount
        particle.offsetX = this.offsetX
        particle.offsetY = this.offsetY
        particle.offsetZ = this.offsetZ
        particle.speed = this.speed
        particle.materialId = this.materialId
        particle.data = this.data
        return particle
    }

    /**
     * Serializes the particleEffect data to be stored to the filesystem
     *
     * @return serializedContent
     */
    override fun serialize(): Map<String, Any> {
        val map = LinkedHashMap<String, Any>()
        map["effect"] = this.effect.toUpperCase()
        map["amount"] = this.amount
        map["speed"] = this.speed
        val tmp3 = LinkedHashMap<String, Any>()
        tmp3["x"] = this.offsetX
        tmp3["y"] = this.offsetY
        tmp3["z"] = this.offsetZ
        map["size"] = tmp3
        val tmp2 = LinkedHashMap<String, Any>()
        if (this.materialId != null)
            tmp2["material"] = this.materialId
        tmp2["damage"] = this.data
        map["block"] = tmp2
        return map
    }
}