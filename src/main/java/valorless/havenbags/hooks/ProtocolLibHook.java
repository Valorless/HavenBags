package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class ProtocolLibHook {
	
	public static boolean Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "[DI-228] " + "Attempting to hook ProtocolLib.");
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
    		Log.Info(plugin, "ProtocolLib integrated!");
    		return true;
		}else {
			Log.Debug(plugin, "[DI-229] " + "ProtocolLib not detected.");
			return false;
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
    		return true;
		}else {
			return false;
		}
	}

}
