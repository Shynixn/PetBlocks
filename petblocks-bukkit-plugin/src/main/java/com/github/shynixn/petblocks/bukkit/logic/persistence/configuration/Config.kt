package com.github.shynixn.petblocks.bukkit.logic.persistence.configuration

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.service.DependencyService
import com.github.shynixn.petblocks.api.business.service.DependencyWorldGuardService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.business.helper.toParticleType
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundEntity
import com.google.inject.Singleton
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
@Singleton
object Config : Config<Player>() {
    internal var plugin: Plugin? = null

    override fun fixJoinDefaultPet(petData: PetMeta) {
        val petMeta = petData as PetData
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
            petData.setParticleEffectMeta(generateParticleEffectCompatibility("join.settings.effect"))
        }
    }

    fun generateSuggestHeadMessage(): ChatMessage {
        return chatMessage {
            text {
                prefix + "Click here: "
            }
            component {
                color(com.github.shynixn.petblocks.api.business.enumeration.ChatColor.YELLOW) {
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
                color(com.github.shynixn.petblocks.api.business.enumeration.ChatColor.YELLOW) {
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
    }

    override fun allowPetSpawningByUUID(uuid: UUID): Boolean {
        val player = Bukkit.getPlayer(uuid)

        if (player != null && player.isOnline) {
            return allowPetSpawning(player.location)
        }

        return false
    }

    fun generateHeadDatabasemessage(): ChatMessage {
        return chatMessage {
            text {
                prefix + "Download the plugin "
            }
            component {
                color(com.github.shynixn.petblocks.api.business.enumeration.ChatColor.YELLOW) {
                    text {
                        ">>Head Database<<"
                    }
                }
                clickAction {
                    ChatClickAction.OPEN_URL to "https://www.spigotmc.org/resources/14280/"
                }
                hover {
                    text {
                        "A valid spigot account is required!"
                    }
                }
            }
        }
    }

    override fun allowPetSpawning(location2: Any?): Boolean {
        val location = location2 as Location
        val includedWorlds = this.includedWorlds
        val excludedWorlds = this.excludedWorlds

        when {
            includedWorlds.contains("all") -> return !excludedWorlds.contains(location.world.name) && this.handleRegionSpawn(location)
            excludedWorlds.contains("all") -> return includedWorlds.contains(location.world.name) && this.handleRegionSpawn(location)
            else -> Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included worlds inside of the config.yml")
        }

        return true
    }

    private fun handleRegionSpawn(location: Location): Boolean {
        val dependencyService = PetBlocksApi.INSTANCE.resolve(DependencyService::class.java)

        if (!dependencyService.isInstalled(PluginDependency.WORLDGUARD)) {
            return true
        }

        val worldGuardService = PetBlocksApi.INSTANCE.resolve(DependencyWorldGuardService::class.java)

        val includedRegions = this.includedRegions
        val excludedRegions = this.excludedRegion

        try {
            when {
                includedRegions.contains("all") -> return worldGuardService.getRegionNames(location).none { excludedRegions.contains(it) }
                excludedRegions.contains("all") -> return worldGuardService.getRegionNames(location).any { includedRegions.contains(it) }
                else -> Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included regions inside of the config.yml")
            }
        } catch (e: Exception) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to handle region spawning.", e)
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
                val data = (this.getData<Any>("pet.feeding.click-sound") as MemorySection).getValues(false)
                this.feedingClickSoundCache = SoundEntity(data["name"] as String)
                this.feedingClickSoundCache.volume = data["volume"] as Double
                this.feedingClickSoundCache.pitch = data["pitch"] as Double
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load feeding click-sound.", e)
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
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load feeding click-sound.", e)
            }

        }
        return this.feedingClickParticleCache
    }

    private fun generateParticleEffectCompatibility(path: String): ParticleEntity {
        val entityParticle = ParticleEntity(ParticleType.NONE)
        val values = (this.getData<Any>(path) as MemorySection).getValues(false)

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
                entityParticle.materialName = MaterialCompatibility13.getMaterialFromId(values["id"] as Int).name
            }
        }

        return entityParticle
    }

    /**
     * Reloads the config.
     */
    override fun reload() {
        this.plugin = JavaPlugin.getPlugin(PetBlocksPlugin::class.java)
        this.plugin!!.reloadConfig()
        super.reload()
    }

    /**
     * Returns data.
     *
     * @param path path
     * @return data
     */
    override fun <T> getData(path: String): T? {
        if (this.plugin == null)
            return null
        var data = this.plugin!!.config.get(path)
        if (data is String) {
            data = ChatColor.translateAlternateColorCodes('&', data)
        }
        return data as T
    }
}
