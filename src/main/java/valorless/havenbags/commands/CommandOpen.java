package valorless.havenbags.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import valorless.havenbags.Lang;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.gui.EtherealGUI;

public class CommandOpen {
	
	public static boolean Run(HBCommand command) {

		if(command.sender instanceof Player player) {

			if (command.args.length == 2){
				String id = command.args[1];
				if(EtherealBags.isOpen(EtherealBags.formatBagId(player.getUniqueId(), id))) {
					player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("ethereal-open-admin"), player));
					return true;
				}
				EtherealGUI gui = new EtherealGUI(player, id, player);
				gui.OpenInventory(player);
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
				if(EtherealBags.isOpen(EtherealBags.formatBagId(target.getUniqueId(), id))) {
					player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("ethereal-open-admin"), player));
					return true;
				}

				EtherealGUI gui = new EtherealGUI(target.getPlayer(), id, player);
				gui.OpenInventory(player);
				return true;
			}
		}
		return true;
	}
}
