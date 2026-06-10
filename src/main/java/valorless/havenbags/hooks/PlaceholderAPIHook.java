package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class PlaceholderAPIHook {
	
	public static boolean hook() {
		if(!Main.plugins.getBool("plugins.PlaceholderAPI.enabled")) return false;
		JavaPlugin plugin = Main.plugin;
		
		Log.debug(plugin, "[DI-187] " + "Attempting to hook PlaceholderAPI.");
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    		Log.info(plugin, "PlaceholderAPI integrated!");
    		return true;
		}else {
			Log.debug(plugin, "[DI-188] " + "PlaceholderAPI not detected.");
			return false;
		}
	}
	
	public static boolean isHooked() {
		return (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
	}
	
}