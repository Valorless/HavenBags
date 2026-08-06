package valorless.havenbags.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import valorless.havenbags.Database;
import valorless.havenbags.Lang;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.gui.EtherealGUI;

public class CommandOpen {
	
	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;

		if(command.sender instanceof Player player) {
			
			for(Bag dat : Database.getOpenBags()) {
				if(dat.getViewer().getUniqueId().equals(player.getUniqueId())) {
					return true;
				}
			}

			if (command.args.length == 2){
				String id = command.args[1];
				if(EtherealBags.isOpen(EtherealBags.formatBagId(player.getUniqueId(), id))) {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("ethereal-open-admin"), player));
					return true;
				}
				if(!EtherealBags.hasBag(player.getUniqueId(), id)) {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-does-not-exist"), player));
					return true;
				}
				EtherealGUI gui = new EtherealGUI(player, id, player);
				gui.openInventory(player);
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
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("ethereal-open-admin"), player));
					return true;
				}

				EtherealGUI gui = new EtherealGUI(target.getPlayer(), id, player);
				gui.openInventory(player);
				return true;
			}
		}
		return true;
	}
}
