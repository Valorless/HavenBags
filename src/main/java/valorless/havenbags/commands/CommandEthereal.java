package valorless.havenbags.commands;

import org.bukkit.entity.Player;

import valorless.havenbags.Lang;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.features.AutoPickup;

public class CommandEthereal {
	
	public static boolean Run(HBCommand command) {
		// -1  0         1    2      3
		// bag ethereal <id> <what> <value>
		Player player;
		String bagId;
		String type;
		String value;
		
		try {
			player = (Player) command.sender;
			bagId = command.args[1];
			type = command.args[2];
			value = command.args[3];
		}catch(Exception e) {
			command.sender.sendMessage("§cUsage: /bag ethereal <id> <autosort|autopickup|magnet> <value>");
			return true;
		}
		
		if(type.equalsIgnoreCase("autosort") && player.hasPermission("havenbags.autosort")) {
			if(command.args.length < 4) {
				command.sender.sendMessage("§cUsage: /bag ethereal <id> autosort <on|off>");
				return true;
			}
			Boolean autoSort;
			if(value.equalsIgnoreCase("on")) {
				autoSort = true;
			}
			else if(value.equalsIgnoreCase("off")) {
				autoSort = false;
			}
			else {
				command.sender.sendMessage("§cUsage: /bag ethereal <id> autosort <on|off>");
				return true;
			}
			EtherealBags.getBagSettings(player.getUniqueId(), bagId).autoSort = autoSort;
			player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-sort-command").replace("%value%", value), player));
			return true;
		}
		else if(type.equalsIgnoreCase("autopickup") && player.hasPermission("havenbags.autopickup")) {
			if(command.args.length < 4) {
				//command.sender.sendMessage("§cUsage: /bag ethereal <id> autopickup <filter|none>");
				EtherealBags.getBagSettings(player.getUniqueId(), bagId).autoPickup = "null";
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
						"None"), player));
				return true;
			}
			String filter = value.equalsIgnoreCase("none") ? "null" : value;

			EtherealBags.getBagSettings(player.getUniqueId(), bagId).autoPickup = filter;
			if(filter.equals("null")) {
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
						"None"), player));
				
			}else {
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
						AutoPickup.GetFilterDisplayname(filter)), player));
			}
			return true;
		}
		else if(type.equalsIgnoreCase("magnet") && player.hasPermission("havenbags.magnet")) {
			if(command.args.length < 4) {
				command.sender.sendMessage("§cUsage: /bag ethereal <id> magnet <on|off>");
				return true;
			}
			Boolean magnet;
			if(value.equalsIgnoreCase("on")) {
				magnet = true;
			}
			else if(value.equalsIgnoreCase("off")) {
				magnet = false;
			}
			else {
				command.sender.sendMessage("§cUsage: /bag ethereal <id> magnet <on|off>");
				return true;
			}
			EtherealBags.getBagSettings(player.getUniqueId(), bagId).magnet = magnet;
			player.sendMessage(Lang.Get("prefix") + Lang.Get("magnet-command").replace("%value%", value));
		}else {
			command.sender.sendMessage("§cUsage: /bag ethereal <id> <autosort|autopickup|magnet> <value>");
		}
		return true;
	}
}
