package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;

public class CommandMod {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		command.sender.sendMessage(Lang.Parse(Main.plugins.GetString("mods.HavenBagsPreview.command-message"), (Player)command.sender));
			
		return true;
	}
}
