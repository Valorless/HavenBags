package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;

public class CommandMod {

	public static boolean run(HBCommand command) {
		if(!Main.plugins.getBool("mods.HavenBagsPreview.enable-command")) return false;
		
		command.sender.sendMessage(Lang.parse(Main.plugins.getString("mods.HavenBagsPreview.command-message"), (Player)command.sender));
			
		return true;
	}
}
