package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.Unused;
import valorless.valorlessutils.ValorlessUtils.Log;

@Unused
public class PacketeventsHook {
	
	public static boolean Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "[DI-228] " + "Attempting to hook Packetevents.");
		if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
    		Log.Info(plugin, "Packetevents integrated!");
    		return true;
		}else {
			Log.Debug(plugin, "[DI-229] " + "Packetevents not detected.");
			return false;
		}
	}
	
	public static boolean isHooked() {
		if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
    		return true;
		}else {
			return false;
		}
	}

}
