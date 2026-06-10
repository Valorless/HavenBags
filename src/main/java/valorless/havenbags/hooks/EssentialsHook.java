package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.ess3.api.IEssentials;
import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class EssentialsHook {
	
	static IEssentials instance = null;

	public static void init() {		
		try {
			Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
			if (ess instanceof IEssentials) {
				instance = (IEssentials) ess;
			}
			Eco.init();
			Log.info(Main.plugin, "Essentials integrated!");
		}catch(Exception e) {
			Log.debug(Main.plugin, "Essentials not detected.");
		}
		
	}

	public static IEssentials getInstance() {
		return instance;
	}
	
	public static void hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.debug(plugin, "Attempting to hook Essentials.");
		
		if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
			Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
			if (ess instanceof IEssentials) {
				instance = (IEssentials) ess;
			}
			Log.info(Main.plugin, "Essentials integrated!");
		}else {
			Log.debug(Main.plugin, "Essentials not detected.");
		}
	}
	
	public static boolean isHooked() {
        return instance != null;
	}
}
