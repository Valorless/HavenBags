package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class ChestSortHook {
	
	public static void Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "Attempting to hook ChestSort.");
		if (Bukkit.getPluginManager().getPlugin("ChestSort") != null) {
    		Log.Info(plugin, "ChestSort integrated!");
		}else {
			Log.Debug(plugin, "ChestSort not detected.");
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("ChestSort") != null) {
    		return true;
		}else {
			return false;
		}
	}

}
