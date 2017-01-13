package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.lib.DynamicCommandHelper;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by Shynixn
 */
public class ConfigCommands {
    private static ConfigCommands instance;

    private DynamicCommandHelper.CommandContainer petblockGuiCommandContainer;
    private DynamicCommandHelper.CommandContainer petblocksConfigurationCommandContainer;

    private ConfigCommands() {
        super();
    }

    public static ConfigCommands getInstance() {
        if (instance == null) {
            instance = new ConfigCommands();
        }
        return instance;
    }

    public void load(FileConfiguration c) {
        this.petblockGuiCommandContainer = new DynamicCommandHelper.CommandContainer("petblocks-gui", c);
        this.petblocksConfigurationCommandContainer = new DynamicCommandHelper.CommandContainer("petblocks-configuration", c);
    }

    public DynamicCommandHelper.CommandContainer getPetblockGuiCommandContainer() {
        return this.petblockGuiCommandContainer;
    }

    public DynamicCommandHelper.CommandContainer getPetblocksConfigurationCommandContainer() {
        return this.petblocksConfigurationCommandContainer;
    }
}
