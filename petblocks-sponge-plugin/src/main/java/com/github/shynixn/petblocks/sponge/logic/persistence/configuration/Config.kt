package com.github.shynixn.petblocks.sponge.logic.persistence.configuration

import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta
import com.github.shynixn.petblocks.core.logic.business.helper.ChatBuilder
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import com.github.shynixn.petblocks.sponge.PetBlocksPlugin
import com.github.shynixn.petblocks.sponge.logic.business.helper.sendMessage
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeParticleEffect
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeSoundBuilder
import com.google.inject.Singleton
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.spongepowered.api.Sponge
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
    /**
     * Returns the pet naming message.
     *
     * @return message
     */
    override fun getPetNamingMessage(): ChatBuilder {
        return ChatBuilder()
                .text(this.prefix)
                .component(this.getData<String>("messages.naming-suggest-prefix")).builder()
                .component(this.getData<String>("messages.naming-suggest-clickable"))
                .setClickAction(ChatBuilder.ClickAction.SUGGEST_COMMAND, "/" + this.getData<Any>("petblocks-gui.command") + " rename ")
                .setHoverText(this.getData<String>("messages.naming-suggest-hover")).builder()
                .component(this.getData<String>("messages.naming-suggest-suffix")).builder()
    }

    /**
     * Returns the skin naming message.
     *
     * @return message
     */
    override fun getPetSkinNamingMessage(): ChatBuilder {
        return ChatBuilder()
                .text(this.prefix)
                .component(this.getData<String>("messages.skullnaming-suggest-prefix")).builder()
                .component(this.getData<String>("messages.skullnaming-suggest-clickable"))
                .setClickAction(ChatBuilder.ClickAction.SUGGEST_COMMAND, "/" + this.getData<Any>("petblocks-gui.command") + " skin ")
                .setHoverText(this.getData<String>("messages.skullnaming-suggest-hover")).builder()
                .component(this.getData<String>("messages.skullnaming-suggest-suffix")).builder()
    }


    override fun fixJoinDefaultPet(petData: PetMeta) {
        val petMeta = petData as PetData
        petMeta.setSkin(this.getData<Int>("join.settings.id")!!, (this.getData<Int>("join.settings.damage") as Int), this.getData<String>("join.settings.skin"), this.getData<Boolean>("unbreakable")!!)
        val optEngineContainer = this.engineController.getContainerFromPosition(this.getData<Int>("join.settings.engine")!!)
        if (!optEngineContainer.isPresent) {
            throw IllegalArgumentException("Join.settings.engine engine could not be loaded!")
        }
        petMeta.setEngine(optEngineContainer.get())
        petMeta.petDisplayName = this.getData<String>("join.settings.petname")
        petMeta.isEnabled = this.getData<Boolean>("join.settings.enabled")!!
        petMeta.age = this.getData<Int>("join.settings.age")!!.toLong()

        if (!(this.getData<Any>("join.settings.particle.name") as String).equals("none", ignoreCase = true)) {
            val meta: ParticleEffectMeta
            try {
                meta = SpongeParticleEffect(this.getData<Any>("effect") as Map<String, Any>)
                petMeta.particleEffectMeta = meta
            } catch (e: Exception) {
                logger.warn("Failed to load particle effect for join pet.")
            }
        }
    }


    override fun allowPetSpawning(location2: Any?): Boolean {
        val location = location2 as Location<World>
        val includedWorlds = this.includedWorlds
        val excludedWorlds = this.excludedWorlds

        when {
            includedWorlds.contains("all") -> return !excludedWorlds.contains(location.extent.name)
            excludedWorlds.contains("all") -> return includedWorlds.contains(location.extent.name)
            else -> Sponge.getGame().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included worlds inside of the config.yml")
        }
        return true
    }


    /**
     * Returns the feeding click sound.
     *
     * @return sound
     */
    fun getFeedingClickSound(): SoundMeta {
        if (this.feedingClickSoundCache == null) {
            try {
                this.feedingClickSoundCache = SpongeSoundBuilder(this.getData<Any>("pet.feeding.click-sound") as Map<String, Any>)
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
    fun getFeedingClickParticleEffect(): ParticleEffectMeta {
        if (this.feedingClickParticleCache == null) {
            try {
                this.feedingClickParticleCache = SpongeParticleEffect(this.getData<Any>("pet.feeding.click-particle") as Map<String, Any>)
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
        return data as T
    }
}