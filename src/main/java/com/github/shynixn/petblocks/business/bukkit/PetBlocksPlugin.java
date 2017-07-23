package com.github.shynixn.petblocks.business.bukkit;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.bukkit.nms.VersionSupport;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ReflectionLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Shynixn!
 */
public final class PetBlocksPlugin extends JavaPlugin {
	public static final String PREFIX_CONSOLE = ChatColor.AQUA + "[PetBlocks] ";
	public static final String PLUGIN_NAME = "PetBlocks";
	private boolean disabled;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		if(!VersionSupport.isServerVersionSupported(PLUGIN_NAME, PREFIX_CONSOLE)) {
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
}
