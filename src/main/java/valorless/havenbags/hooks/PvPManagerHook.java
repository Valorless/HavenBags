package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class PvPManagerHook {
	
	public static void hook() {
		if(!Main.plugins.getBool("plugins.PvPManager.enabled")) return;
		JavaPlugin plugin = Main.plugin;
		
		Log.debug(plugin, "[DI-189] " + "Attempting to hook PvPManager.");
		if (Bukkit.getPluginManager().getPlugin("PvPManager") != null) {
    		Log.info(plugin, "PvPManager integrated!");
		}else {
			Log.debug(plugin, "[DI-190] " + "PvPManager not detected.");
		}
	}
	
	public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("PvPManager") != null;
	}

}
