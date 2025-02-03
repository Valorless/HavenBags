package valorless.havenbags;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import valorless.havenbags.commands.HBCommand;
import valorless.havenbags.commands.CommandAutopickup;
import valorless.havenbags.commands.CommandConvertEpicBackpacks;
import valorless.havenbags.commands.CommandConvertMinepacks;
import valorless.havenbags.commands.CommandCreate;
import valorless.havenbags.commands.CommandEmpty;
import valorless.havenbags.commands.CommandGUI;
import valorless.havenbags.commands.CommandGive;
import valorless.havenbags.commands.CommandInfo;
import valorless.havenbags.commands.CommandMod;
import valorless.havenbags.commands.CommandModelData;
import valorless.havenbags.commands.CommandPreview;
import valorless.havenbags.commands.CommandRawInfo;
import valorless.havenbags.commands.CommandReload;
import valorless.havenbags.commands.CommandRename;
import valorless.havenbags.commands.CommandRestore;
import valorless.havenbags.commands.CommandTexture;
import valorless.havenbags.commands.CommandToken;
import valorless.havenbags.commands.CommandTrust;
import valorless.havenbags.commands.CommandUntrust;
import valorless.havenbags.commands.CommandWeight;

import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandListener implements CommandExecutor {
	
	String Name = "§7[§aHaven§bBags§7]§r";
	
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Log.Debug(Main.plugin, "[DI-85] " + "Sender: " + sender.getName());
    	Log.Debug(Main.plugin, "[DI-86] " + "Command: " + command.toString());
    	Log.Debug(Main.plugin, "[DI-87] " + "Label: " + label);
    	for(String arg : args) {
			Log.Debug(Main.plugin, "[DI-132] " + arg);
		}
    	final HBCommand cmd = new HBCommand(sender, command, label, args);
    	
		if(args.length == 0) {
			sender.sendMessage(Name + " HavenBags by Valorless.");
			return false;
		}
		else 
		if (args.length >= 1){
			try {
				if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("havenbags.reload")) {
					return CommandReload.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("create") && sender.hasPermission("havenbags.create")) {
					return CommandCreate.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("give") && sender.hasPermission("havenbags.give")) {
					return CommandGive.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.restore")) {
					return CommandRestore.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("preview") && sender.hasPermission("havenbags.preview")) {
					return CommandPreview.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("rename") && sender.hasPermission("havenbags.rename")) {
					return CommandRename.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("info") && sender.hasPermission("havenbags.info")) {
					return CommandInfo.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("rawinfo") && sender.hasPermission("havenbags.info")) {
					return CommandRawInfo.Run(cmd);
				}
				if(args[0].equalsIgnoreCase("gui") && sender.hasPermission("havenbags.gui")) {
					return CommandGUI.Run(cmd);
				}

				if(args[0].equalsIgnoreCase("empty") && sender.hasPermission("havenbags.empty")) {
					return CommandEmpty.Run(cmd);
				}

				if(args[0].equalsIgnoreCase("autopickup") && sender.hasPermission("havenbags.autopickup")) {
					return CommandAutopickup.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("weight") && sender.hasPermission("havenbags.weight")) {
					return CommandWeight.Run(cmd);
				}

				if(args[0].equalsIgnoreCase("help")) {
					return valorless.havenbags.commands.CommandHelp.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("trust") && sender.hasPermission("havenbags.trust")) {
					return CommandTrust.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("untrust") && sender.hasPermission("havenbags.trust")) {
					return CommandUntrust.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("texture") && sender.hasPermission("havenbags.texture")) {
					return CommandTexture.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("modeldata") && sender.hasPermission("havenbags.modeldata")) {
					return CommandModelData.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("token") && sender.hasPermission("havenbags.token")) {
					return CommandToken.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("mod") && Main.plugins.GetBool("mods.HavenBagsPreview.enable-command")) {
					return CommandMod.Run(cmd);
				}
				
				if(args[0].equalsIgnoreCase("convertminepacks") && sender.isOp()) {
					return CommandConvertMinepacks.Run(cmd);
				}
				if(args[0].equalsIgnoreCase("convertepicbackpacks") && sender.isOp()) {
					return CommandConvertEpicBackpacks.Run(cmd);
				}
			} catch(Exception e) {
				sender.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
				Log.Error(Main.plugin, e.getMessage());
				//Log.Error(Main.plugin, e.printStackTrace());
				e.printStackTrace();
			}
		}
		sender.sendMessage("Unknown command.");
        return false;
    }
	
	String FixMaterialName(String string) {
    	string = string.replace('_', ' ');
        char[] charArray = string.toCharArray();
        boolean foundSpace = true;
        for(int i = 0; i < charArray.length; i++) {
        	charArray[i] = Character.toLowerCase(charArray[i]);
        	if(Character.isLetter(charArray[i])) {
        		if(foundSpace) {
        			charArray[i] = Character.toUpperCase(charArray[i]);
        			foundSpace = false;
        		}
        	}
        	else {
        		foundSpace = true;
        	}
        }
        string = String.valueOf(charArray);
    	return string;
    }
	
	public static Set<String> listFilesUsingJavaIO(String dir) {
	    return Stream.of(new File(dir).listFiles())
	      .filter(file -> !file.isDirectory())
	      .map(File::getName)
	      .collect(Collectors.toSet());
	}

}
