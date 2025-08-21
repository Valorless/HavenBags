package valorless.havenbags.commands;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.reloader.PluginReloader;

public class CommandReloadPlugin {
	
	public static boolean Run(HBCommand command) {
		command.sender.sendMessage(Lang.Parse("Reloading the HavenBags plugin, please wait around 10 seconds", null));
		PluginReloader.reloadPlugin(Main.plugin, 200, false);
		return true;
	}
}
