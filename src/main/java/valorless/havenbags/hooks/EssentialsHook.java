package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class EssentialsHook {
	
	public static void Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "Attempting to hook Essentials.");
		
		if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
			Essentials.init();
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
    		return true;
		}else {
			return false;
		}
	}
}
