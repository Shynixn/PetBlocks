package com.github.shynixn.petblocks.bukkit.logic.persistence.configuration

import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitItemContainer
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitParticleEffect
import com.github.shynixn.petblocks.core.logic.persistence.configuration.ParticleConfiguration
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemContainer
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
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
class BukkitParticleConfiguration : ParticleConfiguration<Player>(){

    private val plugin = JavaPlugin.getPlugin(PetBlocksPlugin::class.java)

    /**
     * Reloads the content from the fileSystem
     */
    override fun reload() {
        this.particleCache.clear()
        this.plugin.reloadConfig()
        val data = (this.plugin.getConfig().get("particles") as MemorySection).getValues(false)
        for (key in data.keys) {
            try {
                val container = BukkitItemContainer(Integer.parseInt(key), (data[key] as MemorySection).getValues(false))
                val meta = BukkitParticleEffect(((data[key] as MemorySection).getValues(false)["effect"] as MemorySection).getValues(true))
                this.particleCache.put(container, meta)
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load particle " + key + '.'.toString(), e)
            }
        }
    }
}