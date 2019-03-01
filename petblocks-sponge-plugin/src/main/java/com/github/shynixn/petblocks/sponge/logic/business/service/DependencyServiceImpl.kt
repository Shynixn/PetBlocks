package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.service.DependencyService
import com.github.shynixn.petblocks.sponge.logic.business.extension.sendMessage
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class DependencyServiceImpl @Inject constructor(private val plugin: PluginContainer) : DependencyService {
    private val prefix = ChatColor.AQUA.toString() + "[PetBlocks] "

    /**
     * Checks for installed dependencies and shows console output.
     */
    override fun checkForInstalledDependencies() {
        printInstallment(PluginDependency.HEADDATABASE)
    }

    /**
     * Returns if the given [pluginDependency] is installed.
     */
    override fun isInstalled(pluginDependency: PluginDependency): Boolean {
        return plugin.getDependency(pluginDependency.pluginName).isPresent
    }

    /**
     * Returns the version of the [pluginDependency]. Throws Exception if not installed.
     */
    override fun getVersion(pluginDependency: PluginDependency): String {
        val optPlugin = plugin.getDependency(pluginDependency.pluginName)

        if (!optPlugin.isPresent) {
            throw IllegalArgumentException("Plugin not " + pluginDependency.pluginName + "installed.")
        }

        return optPlugin.get().version!!
    }

    /**
     * Prints to the console if the plugin is installed.
     */
    private fun printInstallment(pluginDependency: PluginDependency) {
        if (isInstalled(pluginDependency)) {
            val plugin = plugin.getDependency(pluginDependency.pluginName).get()

            Sponge.getServer().console.sendMessage(prefix + ChatColor.DARK_GREEN + "found dependency [" + plugin.id + "].")
            Sponge.getServer().console.sendMessage(prefix + ChatColor.DARK_GREEN + "successfully loaded dependency [" + plugin.id + "] " + plugin.version + '.')
        }
    }
}