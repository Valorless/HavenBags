package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class OraxenHook {
	
	public static void Hook() {
		if(!Main.plugins.GetBool("plugins.Oraxen.enabled")) return;
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "Attempting to hook Oraxen.");
		if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
    		Log.Info(plugin, "Oraxen integrated!");
		}else {
			Log.Debug(plugin, "Oraxen not detected.");
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
    		return true;
		}else {
			return false;
		}
	}

}
