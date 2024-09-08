package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.utils.Base64Validator;

public class CommandToken {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		if(command.args.length >= 3) {
			if(command.args[1].equalsIgnoreCase("texture")) {
				if(command.args[2].chars().count() > 30) {
					if(Base64Validator.isValidBase64(command.args[2])) {
						player.getInventory().addItem(HavenBags.CreateToken(command.args[2]));
					}else {
						player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
						return true;
					}
				}else {
					if(Base64Validator.isValidBase64(Main.textures.GetString(String.format("textures.%s", command.args[2])))) {
						player.getInventory().addItem(HavenBags.CreateToken(Main.textures.GetString(String.format("textures.%s", command.args[2])), command.args[2]));
					}else {
						player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
						return true;
					}
				}
			}else {
				if(command.args[1].equalsIgnoreCase("custommodeldata")) {
					player.getInventory().addItem(HavenBags.CreateToken(command.args[2]));
				}
			}
			return true;
		}
		return true;
	}
}
