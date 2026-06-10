package valorless.havenbags.commands;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.reloader.PluginReloader;

public class CommandReloadPlugin {
	
	public static boolean run(HBCommand command) {
		command.sender.sendMessage(Lang.parse("Reloading the HavenBags plugin, please wait..", null));
		PluginReloader.reloadPlugin(Main.plugin, 200, false);
		return true;
	}
}
