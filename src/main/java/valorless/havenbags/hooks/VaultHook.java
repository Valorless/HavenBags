package valorless.havenbags.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class VaultHook {
	private static Economy economy;
	
	public static void Hook() {
		JavaPlugin plugin = Main.plugin;
		
		Log.debug(plugin, "Attempting to hook Vault.");
		if (setupEconomy()) {
    		Log.info(plugin, "Vault integrated!");
		}else {
			Log.debug(plugin, "Vault not detected.");
		}
	}

    public static boolean isHooked() {
        return economy != null;
    }
	
	
	private static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }
	
	public static Economy getEconomy() {
        return economy;
    }
	
}
