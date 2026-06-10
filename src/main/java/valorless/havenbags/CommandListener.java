package valorless.havenbags;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import valorless.havenbags.commands.*;
import valorless.havenbags.commands.debug.DebugHandler;
import valorless.havenbags.commands.fun.CommandExplode;
import valorless.valorlessutils.logging.Log;

public class CommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Log.debug(Main.plugin, "[DI-85] " + "Sender: " + sender.getName());
    	Log.debug(Main.plugin, "[DI-86] " + "Command: " + command.toString());
    	Log.debug(Main.plugin, "[DI-87] " + "Label: " + label);
    	for(int i = 0; i < args.length; i++) {
			Log.debug(Main.plugin, "[DI-132] " + "Arg " + i + ": " + args[i]);
		}
    	final HBCommand cmd = new HBCommand(sender, command, label, args);
    	
		if(args.length == 0) {
			//sender.sendMessage(Name + " HavenBags by Valorless.");
			return false;
		}

		final String subCommand = args[0];

		try {
			if (subCommand.equals("reload")) {
				return CommandReload.run(cmd, "havenbags.reload");
			}
			if(!BagData.isReady()) {
				return true;
			}
			switch(subCommand) {
				case "create": return CommandCreate.run(cmd, "havenbags.create");
				case "give": return CommandGive.run(cmd, "havenbags.give");
				case "rename": return CommandRename.run(cmd, "havenbags.rename");
				case "info": return CommandInfo.run(cmd, "havenbags.info");
				case "rawinfo": return CommandRawInfo.run(cmd, "havenbags.info");
				case "gui": return CommandGUI.run(cmd, "havenbags.info");
				case "empty": return CommandEmpty.run(cmd, "havenbags.empty");
				case "autopickup": return CommandAutopickup.run(cmd, "havenbags.autopickup");
				case "help": return CommandHelp.run(cmd);
				case "weight": return CommandWeight.run(cmd, "havenbags.weight");
				case "trust": return CommandTrust.run(cmd, "havenbags.trust");
				case "untrust": return CommandUntrust.run(cmd, "havenbags.trust");
				case "texture": return CommandTexture.run(cmd, "havenbags.texture");
				case "modeldata": return CommandModelData.run(cmd, "havenbags.modeldata");
				case "itemmodel": return CommandItemModel.run(cmd, "havenbags.modeldata");
				case "token": return CommandToken.run(cmd, "havenbags.token");
				case "mod": return CommandMod.run(cmd);
				case "explode": return CommandExplode.run(cmd, "havenbags.empty"); // Not visible in tabcompletion.
				case "convertdatabase": return CommandConvertDatabase.run(cmd, "havenbags.database");
				case "autosort": return CommandAutoSort.run(cmd, "havenbags.autosort");
				case "magnet": return CommandMagnet.run(cmd, "havenbags.magnet");
				case "refill": return CommandRefill.run(cmd, "havenbags.refill");
				case "effect": return CommandEffect.run(cmd, "havenbags.effects");
				case "open": return CommandOpen.run(cmd, "havenbags.ethereal");
				case "ethereal": return CommandEthereal.run(cmd, "havenbags.ethereal");
			}

			if(sender.isOp()) {
				switch (subCommand) {
					case "debug": return DebugHandler.debug(cmd);
					case "convertminepacks": return CommandConvertMinepacks.run(cmd);
					case "convertepicbackpacks": return CommandConvertEpicBackpacks.run(cmd);
					case "customcontent": return CommandCustomContent.run(cmd);
					case "pluginreload": return CommandReloadPlugin.run(cmd);
				}
			}

			if(subCommand.equalsIgnoreCase("clearcontent")) {
				if(sender instanceof ConsoleCommandSender) {
					return CommandClearContent.run(cmd);
				}else {
					sender.sendMessage(Lang.get("prefix") + "§cYou must run this command from the console.");
					return true;
				}
			}

			sender.sendMessage("Unknown command.");
			return false;


		} catch(Exception e) {
			sender.sendMessage(Lang.get("prefix") + Lang.get("malformed-command"));
			Log.error(Main.plugin, e.getMessage());
			//Log.Error(Main.plugin, e.printStackTrace());
			e.printStackTrace();
			return false;
		}
    }
	
	public static Set<String> listFilesUsingJavaIO(String dir) {
	    return Stream.of(new File(dir).listFiles())
	      .filter(file -> !file.isDirectory())
	      .map(File::getName)
	      .collect(Collectors.toSet());
	}

}
