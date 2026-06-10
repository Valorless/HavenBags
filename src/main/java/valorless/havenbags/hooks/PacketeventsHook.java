package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.Unused;
import valorless.valorlessutils.logging.Log;

@Unused
public class PacketeventsHook {
	
	public static boolean Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.debug(plugin, "[DI-228] " + "Attempting to hook Packetevents.");
		if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
    		Log.info(plugin, "Packetevents integrated!");
    		return true;
		}else {
			Log.debug(plugin, "[DI-229] " + "Packetevents not detected.");
			return false;
		}
	}
	
	public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("packetevents") != null;
	}

}
