package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.gui.AdminGUI;
import valorless.havenbags.gui.PlayerGUI;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandGUI {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) {
			if(Main.config.getBool("player-gui.enabled")) {
				Player player = (Player) command.sender;
				PlayerGUI gui = new PlayerGUI(player); // Player GUI
				gui.openInventory(player);
				return true;
			}
			else return false;
		}

		//Admin GUI
		
		for(Data dat : BagData.getOpenBags()) {
			if(dat.getViewer().getUniqueId().equals(((Player)command.sender).getUniqueId())) {
				return true;
			}
		}
		
		if (command.args.length == 1) {
			AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Main, (Player)command.sender);
			gui.openInventory((Player)command.sender);
			return true;
		}
		else if (command.args.length == 2){
			if(command.args[1].equalsIgnoreCase("create")) {
				AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Creation, (Player)command.sender);
				gui.openInventory((Player)command.sender);
				return true;
			}
			if(command.args[1].equalsIgnoreCase("restore")) {
				AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Restoration, (Player)command.sender);
				gui.openInventory((Player)command.sender);
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
				gui.openInventory((Player)command.sender);
				return true;
			}
		}
		return true;
	}
}
