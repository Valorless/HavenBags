package valorless.havenbags.commands;

import org.bukkit.entity.Player;

import valorless.havenbags.Lang;
import valorless.havenbags.features.CustomContent;

public class CommandCustomContent {

	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {

		Player player = (Player)command.sender;

		try {
			if (command.args.length == 2){
				if(command.args[1].equalsIgnoreCase("edit")) {
					CustomContent.open(player);
					return true;
				}
			}
			else if (command.args.length >= 3) {
				if(command.args[1].equalsIgnoreCase("load")) {
					String name = command.args[2];
					CustomContent.setContent(CustomContent.load(name));
					CustomContent.open(player);
					return true;
				}
				if(command.args[1].equalsIgnoreCase("save")) {
					String name = command.args[2];
					CustomContent.save(name);
					command.sender.sendMessage(Lang.Parse(
							String.format("'%s' saved to '../plugins/HavenBags/customcontent/%s.yml'", name, name)
							, null));
					return true;
				}
			}
		}catch(Exception e) {}
		
		return true;
	}
}
