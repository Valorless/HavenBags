package valorless.havenbags.hooks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
//import net.ess3.api.IEssentials;
//import net.ess3.api.IUser;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class Essentials {
	
	/*

	static IEssentials instance = null;

	public static void init() {		
		try {
			Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
			if (ess instanceof IEssentials) {
				instance = (IEssentials) ess;
			}
			Log.Info(Main.plugin, "Essentials integrated!");
		}catch(Exception e) {
			Log.Debug(Main.plugin, "Essentials not detected.");
		}
		
	}

	public static IEssentials getInstance() {
		return instance;
	}

	@SuppressWarnings("deprecation")
	public static boolean isVanished(Player player) {
		if(!EssentialsHook.isHooked()) return false;
		IUser pl = instance.getUser(player);
		return pl.isVanished();
	}

	@SuppressWarnings("deprecation")
	public static List<Player> visiblePlayers(){
		List<Player> players = new ArrayList<>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			IUser pl = instance.getUser(player);
			if(pl.isVanished()) continue;
			else players.add(pl.getBase());
		}
		return players;
	}
	*/
}
