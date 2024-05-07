package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class PvPManagerHook {
	
	public static void Hook() {
		if(!Main.plugins.GetBool("plugins.PvPManager.enabled")) return;
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "Attempting to hook PvPManager.");
		if (Bukkit.getPluginManager().getPlugin("PvPManager") != null) {
    		Log.Info(plugin, "PvPManager integrated!");
		}else {
			Log.Debug(plugin, "PvPManager not detected.");
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("PvPManager") != null) {
    		return true;
		}else {
			return false;
		}
	}

}
