package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

@Deprecated(since = "1.37.2.2548", forRemoval = true)
public class ChestSortHook {
	
	public static void Hook() {
		if(!Main.plugins.GetBool("plugins.ChestSort.enabled")) return;
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "[DI-183] " + "Attempting to hook ChestSort.");
		if (Bukkit.getPluginManager().getPlugin("ChestSort") != null) {
    		Log.Info(plugin, "ChestSort integrated!");
		}else {
			Log.Debug(plugin, "[DI-184] " + "ChestSort not detected.");
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
