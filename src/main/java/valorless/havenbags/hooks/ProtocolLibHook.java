package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class ProtocolLibHook {
	
	public static boolean hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.debug(plugin, "[DI-228] " + "Attempting to hook ProtocolLib.");
        //Log.Info(plugin, "ProtocolLib integrated!");
        //Log.Debug(plugin, "[DI-229] " + "ProtocolLib not detected.");
        return Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
	}
	
	public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
	}

}
