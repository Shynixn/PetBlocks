package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.service.DependencyService
import com.github.shynixn.petblocks.bukkit.nms.VersionSupport
import com.google.inject.Inject
import org.bukkit.ChatColor
import org.bukkit.plugin.Plugin
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
class DependencyServiceImpl @Inject constructor(private val plugin: Plugin) : DependencyService {
    private val prefix = ChatColor.AQUA.toString() + "[PetBlocks] "
    private var printedWorldGuardError = false

    /**
     * Checks for installed dependencies and shows console output.
     */
    override fun checkForInstalledDependencies() {
        printInstallment(PluginDependency.WORLDGUARD)
        printInstallment(PluginDependency.CLEARLAG)
    }

    /**
     * Returns if the given [pluginDependency] is installed.
     */
    override fun isInstalled(pluginDependency: PluginDependency): Boolean {
        val plugin = this.plugin.server.pluginManager.getPlugin(pluginDependency.pluginName)

        if (plugin != null && plugin.description.version != "1.0" && pluginDependency == PluginDependency.WORLDGUARD && VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_13_R1)) {
            if (!printedWorldGuardError) {
                this.plugin.logger.log(Level.WARNING, "WorldGuard dependency cannot be established in 1.13 yet.")
                printedWorldGuardError = true
            }

            return false
        }

        return plugin != null
    }

    /**
     * Returns the version of the [pluginDependency]. Throws Exception if not installed.
     */
    override fun getVersion(pluginDependency: PluginDependency): String {
        val plugin = this.plugin.server.pluginManager.getPlugin(pluginDependency.pluginName)
                ?: throw IllegalArgumentException("Plugin not " + pluginDependency.pluginName + "installed.")

        return plugin.description.version
    }

    /**
     * Prints to the console if the plugin is installed.
     */
    private fun printInstallment(pluginDependency: PluginDependency) {
        val plugin = this.plugin.server.pluginManager.getPlugin(pluginDependency.pluginName)

        if (plugin != null) {
            plugin.server.consoleSender.sendMessage(prefix + ChatColor.DARK_GREEN + "found dependency [" + plugin.name + "].")

            if (isInstalled(pluginDependency)) {
                plugin.server.consoleSender.sendMessage(prefix + ChatColor.DARK_GREEN + "successfully loaded dependency [" + plugin.name + "] " + plugin.description.version + '.')
            }
        }
    }
}