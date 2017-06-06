package com.github.shynixn.petblocks.business.bukkit;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.PluginLoader;
import com.github.shynixn.petblocks.lib.ReflectionLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Shynixn!
 *
 */
public final class PetBlocksPlugin extends JavaPlugin {
	public static final String PREFIX_CONSOLE = ChatColor.AQUA + "[PetBlocks] ";
	private boolean disabled;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		if(!BukkitUtilities.isVersionSupported()) {
			BukkitUtilities.sendColorMessage("================================================", ChatColor.RED, PREFIX_CONSOLE);
			BukkitUtilities.sendColorMessage("Petblocks does not support your server version", ChatColor.RED, PREFIX_CONSOLE);
			BukkitUtilities.sendColorMessage("Install v1.8.0 - v1.11.0", ChatColor.RED, PREFIX_CONSOLE);
			BukkitUtilities.sendColorMessage("Plugin gets now disabled!", ChatColor.RED, PREFIX_CONSOLE);
			BukkitUtilities.sendColorMessage("================================================", ChatColor.RED, PREFIX_CONSOLE);
			this.disabled = true;
			Bukkit.getPluginManager().disablePlugin(this);
		}
		else {
			BukkitUtilities.sendColorMessage("Loading PetBlocks ...", ChatColor.GREEN, PREFIX_CONSOLE);
			Language.reload(this);
			Config.initiliaze(this);
			NMSRegistry.registerAll();
			ReflectionLib.invokeMethodByClazz(PetBlocksApi.class, "init", this);
			BukkitUtilities.startFiltering();
			BukkitUtilities.sendColorMessage("Enabled PetBlocks " + this.getDescription().getVersion() + " by Shynixn", ChatColor.GREEN, PREFIX_CONSOLE);
		}
	}

	@Override
	public void onDisable() {
		if(!this.disabled) {
			NMSRegistry.unregisterAll();
			ReflectionLib.invokeMethodByClazz(PetBlocksApi.class, "dispose", this);
		}
	}

	@Override
	public List<Class<?>> getDatabaseClasses() {
		final List<Class<?>> list = new ArrayList<>();
		this.saveDefaultConfig();
		if(this.getConfig().getBoolean("sql-enabled")) {
			if(BukkitUtilities.isVersionSupported()) {
				list.add(ReflectionLib.getClassFromName("com.github.shynixn.petblocks.business.logic.persistence.entity.PetData"));
			}
		}
		return list;
	}

	public void setupDatabase() {
		this.installDDL();
	}
}
