package valorless.havenbags.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import valorless.havenbags.gui.EtherealGUI;

public class CommandOpen {
	
	public static boolean Run(HBCommand command) {
		
		if (command.args.length == 2){
			String id = command.args[1];
			EtherealGUI gui = new EtherealGUI((Player)command.sender, id);
			gui.OpenInventory((Player)command.sender);
			return true;
		}
		else if (command.args.length >= 3) {
			if(!command.sender.hasPermission("havenbags.ethereal.admin")) return true;
			String playerName = command.args[1];
			List<OfflinePlayer> offlinePlayers = Arrays.asList(Bukkit.getOfflinePlayers());
			OfflinePlayer target = offlinePlayers.stream()
					.filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
					.findFirst()
					.orElse(null);
			String id = command.args[2];
			
			EtherealGUI gui = new EtherealGUI(target.getPlayer(), id);
			gui.OpenInventory((Player)command.sender);
			return true;
		}
		return true;
	}
}
