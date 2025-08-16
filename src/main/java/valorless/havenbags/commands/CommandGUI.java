package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import valorless.havenbags.gui.AdminGUI;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandGUI {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		if (command.args.length == 1) {
			AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Main, (Player)command.sender);
			gui.OpenInventory((Player)command.sender);
			return true;
		}
		else if (command.args.length == 2){
			if(command.args[1].equalsIgnoreCase("create")) {
				AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Creation, (Player)command.sender);
				gui.OpenInventory((Player)command.sender);
				return true;
			}
			if(command.args[1].equalsIgnoreCase("restore")) {
				AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Restoration, (Player)command.sender);
				gui.OpenInventory((Player)command.sender);
				return true;
			}
		}
		else if (command.args.length >= 3) {
			if(command.args[1].equalsIgnoreCase("restore")) {
				OfflinePlayer target;
				try {
					target = Bukkit.getOfflinePlayer(UUIDFetcher.getUUID(command.args[2]));
				} catch(Exception e) {
					return true;
				}
				AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Player, (Player)command.sender, target);
				gui.OpenInventory((Player)command.sender);
				return true;
			}
		}
		return true;
	}
}
