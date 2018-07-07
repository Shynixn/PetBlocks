package com.github.shynixn.petblocks.bukkit;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.business.service.GUIService;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.bukkit.logic.business.GoogleGuiceBinder;
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.UpdateUtils;
import com.github.shynixn.petblocks.bukkit.logic.business.listener.InventoryListener;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.bukkit.nms.VersionSupport;
import com.github.shynixn.petblocks.core.logic.business.helper.ReflectionUtils;
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the plugin PetBlocks.
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
public final class PetBlocksPlugin extends JavaPlugin {
    public static final String PREFIX_CONSOLE = ChatColor.AQUA + "[PetBlocks] ";
    private static final long SPIGOT_RESOURCEID = 12056;
    private static final String PLUGIN_NAME = "PetBlocks";
    private static Logger logger;
    private boolean disabled;

    private PetBlockManager petBlockManager;

    /**
     * Listeners.
     */
    @Inject
    private InventoryListener inventoryListener;

    @Inject
    private GUIService guiService;

    /**
     * Enables the plugin PetBlocks.
     */
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        logger = this.getLogger();
        if (!VersionSupport.isServerVersionSupported(PLUGIN_NAME, PREFIX_CONSOLE)) {
            this.disabled = true;
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Loading PetBlocks ...");
            Guice.createInjector(new GoogleGuiceBinder(this));
            Config.getInstance().reload();
            if (Config.getInstance().isMetricsEnabled()) {
                final Metrics metrics = new Metrics(this);
                metrics.addCustomChart(new Metrics.SimplePie("storage", () -> {
                    if (PetBlocksPlugin.this.getConfig().getBoolean("sql.enabled")) {
                        return "MySQL";
                    }
                    return "SQLite";
                }));
            }
            this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    UpdateUtils.checkPluginUpToDateAndPrintMessage(SPIGOT_RESOURCEID, PREFIX_CONSOLE, PLUGIN_NAME, PetBlocksPlugin.this);
                } catch (final IOException e) {
                    PetBlocksPlugin.logger().log(Level.WARNING, "Failed to check for updates.");
                }
            });
            NMSRegistry.registerAll();
            try {
                this.petBlockManager = new PetBlockManager(this);
                ReflectionUtils.invokeMethodByClass(PetBlocksApi.class, "initialize", new Class[]{PetMetaController.class, PetBlockController.class, GUIService.class}, new Object[]{this.petBlockManager.getPetMetaController(), this.petBlockManager.getPetBlockController(), this.guiService});
                Bukkit.getServer().getConsoleSender().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Enabled PetBlocks " + this.getDescription().getVersion() + " by Shynixn");
            } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to enable plugin.", e);
            }
        }
    }

    /**
     * Loads the default config and saves it to the plugin folder.
     */
    @Override
    public void saveDefaultConfig() {
        try (final InputStream inputStream = this.getResource("assets/petblocks/config.yml")) {
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdir();
            }

            final File configFile = new File(this.getDataFolder(), "config.yml");
            if (configFile.exists()) {
                return;
            }

            try (final OutputStream outputStream = new FileOutputStream(configFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (final IOException e) {
            logger().log(Level.WARNING, "Failed to save default config.", e);
        }
    }

    /**
     * Disables the plugin PetBlocks.
     */
    @Override
    public void onDisable() {
        if (!this.disabled) {
            try {
                NMSRegistry.unregisterCustomEntities();
                this.petBlockManager.close();
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to disable petblocks.", e);
            }
        }
    }

    /**
     * Returns the logger of the petblocks plugin.
     *
     * @return logger
     */
    public static Logger logger() {
        return logger;
    }
}
