package com.github.shynixn.petblocks.business.bukkit;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.bukkit.nms.VersionSupport;
import com.github.shynixn.petblocks.business.metrics.Metrics;
import com.github.shynixn.petblocks.lib.ReflectionUtils;
import com.github.shynixn.petblocks.lib.UpdateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public final class PetBlocksPlugin extends JavaPlugin {
    public static final String PREFIX_CONSOLE = ChatColor.AQUA + "[PetBlocks] ";
    private static final long SPIGOT_RESOURCEID = 12056;
    private static final String PLUGIN_NAME = "PetBlocks";
    private boolean disabled;

    /**
     * Enables the plugin PetBlocks
     */
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        if (!VersionSupport.isServerVersionSupported(PLUGIN_NAME, PREFIX_CONSOLE)) {
            this.disabled = true;
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Loading PetBlocks ...");
            Language.reload(this);
            Config.initialize(this);
            if (Config.getInstance().isMetricsEnabled()) {
                new Metrics(this);
            }
            this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    UpdateUtils.checkPluginUpToDateAndPrintMessage(SPIGOT_RESOURCEID, PREFIX_CONSOLE, PLUGIN_NAME, PetBlocksPlugin.this);
                } catch (final IOException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to check for updates.");
                }
            });
            NMSRegistry.registerAll();
            try {
                ReflectionUtils.invokeMethodByClass(PetBlocksApi.class, "init", new Class[]{Plugin.class}, new Object[]{this});
                Bukkit.getServer().getConsoleSender().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Enabled PetBlocks " + this.getDescription().getVersion() + " by Shynixn");
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to enable plugin.", e);
            }
        }
    }

    /**
     * Disables the plugin PetBlocks
     */
    @Override
    public void onDisable() {
        if (!this.disabled) {
            NMSRegistry.unregisterAll();
            try {
                ReflectionUtils.invokeMethodByClass(PetBlocksApi.class, "dispose", new Class[]{}, new Object[]{});
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to disable plugin.", e);
            }
        }
    }
}
