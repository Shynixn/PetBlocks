package com.github.shynixn.petblocks.bukkit.logic.persistence.configuration

import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitEngineData
import com.github.shynixn.petblocks.core.logic.persistence.configuration.EngineConfiguration
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
class BukkitEngineConfiguration : EngineConfiguration<Player>() {

    private val plugin = JavaPlugin.getPlugin(PetBlocksPlugin::class.java)

    /**
     * Reloads the content from the fileSystem.
     */
    override fun reload() {
        this.items.clear()
        this.plugin.reloadConfig()
        val data = (this.plugin.config.get("engines") as MemorySection).getValues(false)
        for (key in data.keys) {
            val content = (this.plugin.config.get("engines." + key) as MemorySection).getValues(true)
            try {
                this.items.add(BukkitEngineData(key.toLong(), content))
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to add content " + key + '.'.toString(), e)
            }
        }
    }
}