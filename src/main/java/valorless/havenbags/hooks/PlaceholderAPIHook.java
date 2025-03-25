package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class PlaceholderAPIHook {
	
	public static boolean Hook() {
		if(!Main.plugins.GetBool("plugins.PlaceholderAPI.enabled")) return false;
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "[DI-187] " + "Attempting to hook PlaceholderAPI.");
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    		Log.Info(plugin, "PlaceholderAPI integrated!");
    		return true;
		}else {
			Log.Debug(plugin, "[DI-188] " + "PlaceholderAPI not detected.");
			return false;
		}
	}
	
	public static boolean isHooked() {
		return (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
	}
	
}