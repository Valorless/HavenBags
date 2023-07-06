package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class PlaceholderAPIHook {
	
	public static void Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "Attempting to hook PlaceholderAPI.");
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    		Log.Info(plugin, "PlaceholderAPI integrated!");
		}else {
			Log.Debug(plugin, "PlaceholderAPI not detected.");
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    		return true;
		}else {
			return false;
		}
	}

}
