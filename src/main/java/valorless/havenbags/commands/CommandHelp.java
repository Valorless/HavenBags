package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Message;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandHelp {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		if(Main.config.GetBool("old-help-menu") == false) {
			HelpMessage(player);
			if(!(command.sender instanceof Player)) { 
				Log.Info(Main.plugin, "Sorry, but only players can view this menu.");
				Log.Info(Main.plugin, "Set 'old-help-menu' to true, if you want to use this command.");
			}
		}
		else {
			HelpMessageOld(player);
		}
		
		return true;
	}
	
	static void HelpMessage(Player player) {
		Message message = new Message("&a&lHaven&b&lBags &8- &fHelp Menu\n"
				+ "&7Optional: [] - Required: <>\n"
				+ "&8(Mouseover commands for information)");
		message.AddNewLine("");
		if(player.hasPermission("havenbags.rename") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags rename <name>",
					"&eRename the bag in your hand\n"
					+ "&eYou cannot rename any bags you aren't bound to.\n"
					+ "&7&o(Supports Hex. Leave value empty to reset)"
					);
		}
		if(player.hasPermission("havenbags.empty") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags empty",
					"&eEmpty the content of the bag\n"
					+ " in your hand, onto the ground.");
		}
		if(player.hasPermission("havenbags.autopickup") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags autopickup <filter>",
					"&eAutomatically put items inside the bag.");
		}
		if(player.hasPermission("havenbags.trust") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags trust <player>",
					"&eAllow trusted player to open your bound bag.");
		}
		if(player.hasPermission("havenbags.trust") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags untrust <player>",
					"&eRemove a trusted player from accessing your bound bag.");
		}
		if(player.hasPermission("havenbags.texture") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags texture <texture or base64>",
					"&e Set the texture of the bag you're holding."
					+ "You can only change textures of bags you own.");
		}
		if(player.hasPermission("havenbags.gui") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags gui",
					"&eOpens Admin GUI.");
		}
		if(player.hasPermission("havenbags.create") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags create [ownerless] <size>",
					"&eCreate a new bag.\n"
					+ "&7(Also in GUI)");
		}
		if(player.hasPermission("havenbags.give") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags give <player> [ownerless] <size>",
					"&eGive player a bag.");
		}
		if(player.hasPermission("havenbags.restore") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags restore <player>",
					"&eShows a list of bags by that player.\n"
					+ "&7(Also in GUI)");
			
			message.AddNewLine(" &e/bags restore <player> <bag-uuid>",
					"&eGives a copy of the bag stored on the server.\n"
					+ "&7(Also in GUI)");
		}
		if(player.hasPermission("havenbags.preview") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags preview <player>",
					"&eShows a list of bags by that player.\n"
					+ "&7(Also in GUI)");

			message.AddNewLine(" &e/bags preview <player> <bag-uuid>",
					"&ePreview a copy of the bag stored on the server.\n"
					+ "&7(Also in GUI)");
		}
		if(player.hasPermission("havenbags.info") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags info",
					"&eShows information about the bag"
					+ " you're currently holding.");
			
			message.AddNewLine(" &e/bags rawinfo",
					"&eShows raw metadata about the bag"
					+ " you're currently holding.");
		}
		if(player.hasPermission("havenbags.weight") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags weight <number>",
					"&eSet the weight limit of the bag"
					+ " you're currently holding.");
		}
		if(player.hasPermission("havenbags.token") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags token <type> <value>",
					"&eCreate a skin token of the chosen type");
		}
		if(player.hasPermission("havenbags.reload") || player.hasPermission("havenbags.help")) {
			message.AddNewLine(" &e/bags reload",
					"&eReloads config files.");
		}
		message.AddNewLine(" &e/bags help",
				"&eYou are here.");
		message.Send(player);
	}
	
	static void HelpMessageOld(Player player) {
		List<String> help = new ArrayList<String>();
		help.add("&a&lHaven&b&lBags &8- &fHelp Menu\n"
				+ "&7Optional: [] - Required: <>\n"
				+ "&8(Mouseover commands for information)");
		help.add("");
		if(player.hasPermission("havenbags.rename") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags rename <name>");
			help.add("&7&o Rename the bag in your hand");
			help.add("&7&o You cannot rename any bags you aren't bound to");
			help.add("&7&o (Supports Hex. Leave value empty to reset.)");
		}
		if(player.hasPermission("havenbags.empty") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags empty");
			help.add("&7&o Empty the content of the bag in your hand, onto the ground");
		}
		if(player.hasPermission("havenbags.autopickup") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags autopickup <filter>");
			help.add("&7&o Automatically put items inside the bag.");
		}
		if(player.hasPermission("havenbags.trust") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags trust <player>");
			help.add("&7&o Allow trusted player to open your bound bag.");
		}
		if(player.hasPermission("havenbags.trust") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags untrust <player>");
			help.add("&7&o Remove a trusted player from accessing your bound bag.");
		}
		if(player.hasPermission("havenbags.texture") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags texture <texture or base64>");
			help.add("&7&o Set the texture of the bag you're holding.");
			help.add("&7&o You can only change textures of bags you own.");
		}
		if(player.hasPermission("havenbags.gui") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags gui");
			help.add("&7&o Opens Admin GUI");
		}
		if(player.hasPermission("havenbags.create") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags create [ownerless] <size>");
			help.add("&7&o Create a new bag");
			help.add("&8 (Also in GUI)");
		}
		if(player.hasPermission("havenbags.give") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags give <player> [ownerless] <size>");
			help.add("&7&o Give player a bag");
		}
		if(player.hasPermission("havenbags.restore") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags restore <player>");
			help.add("&7&o Shows a list of bags by that player");
			help.add("&8 (Also in GUI)");
						
			help.add("&e/bags restore <player> <bag-uuid>");
			help.add("&7&o Gives a copy of the bag stored on the server");
			help.add("&8 (Also in GUI)");
		}
		if(player.hasPermission("havenbags.preview") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags preview <player>");
			help.add("&7&o Shows a list of bags by that player");
			help.add("&8 (Also in GUI)");

			help.add("&e/bags preview <player> <bag-uuid>");
			help.add("&7&o Preview a copy of the bag stored on the server");
			help.add("&8 (Also in GUI)");
		}
		if(player.hasPermission("havenbags.info") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags info");
			help.add("&7&o Shows information about the bag you're currently holding");
			
			help.add("&e/bags rawinfo");
			help.add("&7&o Shows raw metadata about the bag you're currently holding");
		}
		if(player.hasPermission("havenbags.weight") || player.hasPermission("havenbags.help")) {
			help.add(" &e/bags weight <number>");
			help.add("&eSet the weight limit of the bag you're currently holding.");
		}
		if(player.hasPermission("havenbags.token") || player.hasPermission("havenbags.help")) {
			help.add(" &e/bags token <type> <value>");
			help.add("&eCreate a skin token of the chosen type.");
		}
		if(player.hasPermission("havenbags.reload") || player.hasPermission("havenbags.help")) {
			help.add("&e/bags reload");
			help.add("&7&o Reloads config files");
		}
		help.add("&e/bags help");
		help.add("&7&o You are here");
		
		String helpString = "";
		for(String i : help) {
			helpString = helpString + Lang.Parse(i, player) + "\n ";
		}
		player.sendMessage(helpString);
	}
}
