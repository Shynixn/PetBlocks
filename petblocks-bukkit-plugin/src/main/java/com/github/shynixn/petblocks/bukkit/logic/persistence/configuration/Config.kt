package com.github.shynixn.petblocks.bukkit.logic.persistence.configuration

import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.business.listener.InventoryListener
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry
import com.github.shynixn.petblocks.core.logic.business.helper.ChatBuilder
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import com.google.inject.Inject
import com.google.inject.Singleton
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
        petMeta.setSkin(this.getData<Int>("join.settings.id")!!, (this.getData<Int>("join.settings.damage") as Int), this.getData<String>("join.settings.skin"), this.getData<Boolean>("join.settings.unbreakable")!!)
        val optEngineContainer = this.engineController.getContainerFromPosition(this.getData<Int>("join.settings.engine")!!)
        if (!optEngineContainer.isPresent) {
            throw IllegalArgumentException("Join.settings.engine engine could not be loaded!")
        }
        petMeta.setEngine(optEngineContainer.get())
        petMeta.petDisplayName = this.getData<String>("join.settings.petname")!!.replace(":player", petMeta.getPlayerMeta().getName())
        petMeta.isEnabled = this.getData<Boolean>("join.settings.enabled")!!
        petMeta.age = this.getData<Int>("join.settings.age")!!.toLong()

        if (!(this.getData<Any>("join.settings.effect.name") as String).equals("none", ignoreCase = true)) {
            val meta: ParticleEffectMeta
            try {
                meta = createParticleComp((this.getData<Any>("join.settings.effect") as MemorySection).getValues(false))
                petMeta.particleEffectMeta = meta
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load particle effect for join pet.")
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
        val includedRegions = this.includedRegions
        val excludedRegions = this.excludedRegion

        when {
            includedRegions.contains("all") -> return NMSRegistry.getWorldGuardRegionsFromLocation(location).none { excludedRegions.contains(it) }
            excludedRegions.contains("all") -> return NMSRegistry.getWorldGuardRegionsFromLocation(location).any { includedRegions.contains(it) }
            else -> Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included regions inside of the config.yml")
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
                this.feedingClickSoundCache = this.createSoundComp((this.getData<Any>("pet.feeding.click-sound") as MemorySection).getValues(false))
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load feeding click-sound.", e)
            }

        }
        return this.feedingClickSoundCache
    }

    private fun createSoundComp(data: Map<String, Any>): SoundMeta {
        try {
            val clazz = Class.forName("com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitSoundBuilder")
            val constructor = clazz.getDeclaredConstructor(Map::class.java)
            return constructor.newInstance(data) as SoundMeta
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun createParticleComp(data: Map<String, Any>): ParticleEffectMeta {
        try {
            val clazz = Class.forName("com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitParticleEffect")
            val constructor = clazz.getDeclaredConstructor(Map::class.java)
            return constructor.newInstance(data) as ParticleEffectMeta
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Returns the feeding particleEffect.
     *
     * @return particleEffect
     */
    fun getFeedingClickParticleEffect(): ParticleEffectMeta {
        if (this.feedingClickParticleCache == null) {
            try {
                this.feedingClickParticleCache = this.createParticleComp((this.getData<Any>("pet.feeding.click-particle") as MemorySection).getValues(false))
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load feeding click-sound.", e)
            }

        }
        return this.feedingClickParticleCache
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
