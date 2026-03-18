package valorless.havenbags.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.ess3.api.IEssentials;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class EssentialsHook {
	
	static IEssentials instance = null;

	public static void init() {		
		try {
			Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
			if (ess instanceof IEssentials) {
				instance = (IEssentials) ess;
			}
			Eco.init();
			Log.Info(Main.plugin, "Essentials integrated!");
		}catch(Exception e) {
			Log.Debug(Main.plugin, "Essentials not detected.");
		}
		
	}

	public static IEssentials getInstance() {
		return instance;
	}
	
	public static void Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.Debug(plugin, "Attempting to hook Essentials.");
		
		if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
			Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
			if (ess instanceof IEssentials) {
				instance = (IEssentials) ess;
			}
			Log.Info(Main.plugin, "Essentials integrated!");
		}else {
			Log.Debug(Main.plugin, "Essentials not detected.");
		}
	}
	
	public static boolean isHooked() {
		if (instance != null) {
    		return true;
		}else {
			return false;
		}
	}
}
