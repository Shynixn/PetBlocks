package com.github.shynixn.petblocks.sponge.logic.persistence.configuration

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundEntity
import com.github.shynixn.petblocks.sponge.PetBlocksPlugin
import com.github.shynixn.petblocks.sponge.logic.business.helper.CompatibilityItemType
import com.github.shynixn.petblocks.sponge.logic.business.helper.sendConsoleMessage
import com.github.shynixn.petblocks.sponge.logic.business.helper.toParticleType
import com.google.inject.Singleton
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.io.IOException
import java.nio.file.Files
import java.util.regex.Pattern

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
@Singleton
object Config : ConfigLayer<Player>() {
    override fun fixJoinDefaultPet(petMeta: PetMeta) {
        petMeta.setSkin(this.getData<Int>("join.settings.id")!!, (this.getData<Int>("join.settings.damage") as Int), this.getData<String>("join.settings.skin"), this.getData<Boolean>("join.settings.unbreakable")!!)

        val optEngineContainer = this.engineController.getContainerFromPosition(this.getData<Int>("join.settings.engine")!!)
        if (!optEngineContainer.isPresent) {
            throw IllegalArgumentException("Join.settings.engine engine could not be loaded!")
        }

        petMeta.setEngine(optEngineContainer.get())
        petMeta.petDisplayName = this.getData<String>("join.settings.petname")!!.replace(":player", petMeta.playerMeta.name)
        petMeta.isEnabled = this.getData<Boolean>("join.settings.enabled")!!
        petMeta.age = this.getData<Int>("join.settings.age")!!.toLong()

        if (!(this.getData<Any>("join.settings.effect.name") as String).equals("none", ignoreCase = true)) {
            (petMeta as PetData).setParticleEffectMeta(generateParticleEffectCompatibility("join.settings.effect"))
        }
    }

    val suggestHeadMessage = chatMessage {
        text {
            prefix + "Click here: "
        }
        component {
            color(ChatColor.YELLOW) {
                text {
                    ">>Submit skin<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com/custom/heads-generator"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
        text { " " }
        component {
            color(ChatColor.YELLOW) {
                text {
                    ">>Suggest new pet<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com/forum/suggesthead"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
    }

    override fun allowPetSpawning(location2: Any?): Boolean {
        val location: Location<World> = if (location2 is Transform<*>) {
            location2.location as Location<World>
        } else {
            location2 as Location<World>
        }

        val includedWorlds = this.includedWorlds
        val excludedWorlds = this.excludedWorlds

        when {
            includedWorlds.contains("all") -> return !excludedWorlds.contains(location.extent.name)
            excludedWorlds.contains("all") -> return includedWorlds.contains(location.extent.name)
            else -> Sponge.getGame().sendConsoleMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included worlds inside of the config.yml")
        }
        return true
    }


    /**
     * Returns the feeding click sound.
     *
     * @return sound
     */
    fun getFeedingClickSound(): Sound {
        if (this.feedingClickSoundCache == null) {
            try {
                val data = (this.getData<Any>("pet.feeding.click-sound") as Map<String, Any>)
                this.feedingClickSoundCache = SoundEntity(data["name"] as String)
                this.feedingClickSoundCache.volume = data["volume"] as Double
                this.feedingClickSoundCache.pitch = data["pitch"] as Double
            } catch (e: Exception) {
                logger.warn("Failed to load feeding click-sound.", e)
            }

        }
        return this.feedingClickSoundCache
    }

    /**
     * Returns the feeding particleEffect.
     *
     * @return particleEffect
     */
    fun getFeedingClickParticleEffect(): Particle {
        if (this.feedingClickParticleCache == null) {
            try {
                feedingClickParticleCache = generateParticleEffectCompatibility("pet.feeding.click-particle")
            } catch (e: Exception) {
                logger.warn("Failed to load feeding click-sound.", e)
            }

        }
        return this.feedingClickParticleCache
    }

    /**
     * Reloads the config.
     */
    override fun reload() {
        if (!this.privateConfigDir.toFile().exists()) {
            this.privateConfigDir.toFile().mkdir()
        }

        val defaultConfig = this.privateConfigDir.resolve("config.yml")
        if (!Files.exists(defaultConfig)) {
            this.logger.info("Converting config....")
            try {
                this.plugin.getAsset("config.yml").get().copyToFile(defaultConfig)
            } catch (e: IOException) {
                this.logger.warn("Failed to create config.yml.", e)
            }

        }
        val loader = YAMLConfigurationLoader.builder().setPath(defaultConfig).build()
        try {
            this.node = loader.load()
        } catch (e: IOException) {
            this.logger.warn("Failed to reload config.yml.", e)
        }

        super.reload()
    }

    private fun generateParticleEffectCompatibility(path: String): ParticleEntity {
        val entityParticle = ParticleEntity(ParticleType.NONE)
        val values = this.getData<Any>(path) as Map<String, Any>

        with(entityParticle) {
            type = (values["name"] as String).toParticleType()
            amount = values["amount"] as Int
            speed = values["speed"] as Double
        }

        if (values.containsKey("offx")) {
            with(entityParticle) {
                offSetX = values["offx"] as Double
                offSetY = values["offy"] as Double
                offSetZ = values["offz"] as Double
            }
        }

        if (values.containsKey("red")) {
            with(entityParticle) {
                colorRed = values["red"] as Int
                colorGreen = values["green"] as Int
                colorBlue = values["blue"] as Int
            }
        }


        if (values.containsKey("id")) {
            if (values["id"] is String) {
                entityParticle.materialName = values["id"] as String
            } else {
                entityParticle.materialName = CompatibilityItemType.getFromId(values["id"] as Int).name
            }
        }

        return entityParticle
    }

    /**
     * Returns data.
     *
     * @param path path
     * @return data
     */
    override fun <T> getData(path: String): T? {
        if (this.node == null)
            return null

        val items = path.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val targetNode = this.node.getNode(*items as Array<Any>)
        val data = targetNode.value

        if (data is String) {
            return data.translateChatColors() as T
        }

        return data as T
    }
}
