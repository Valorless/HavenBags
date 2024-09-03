package valorless.havenbags.commands;

import org.bukkit.entity.Player;

import valorless.havenbags.AutoPickup;
import valorless.havenbags.BagData;
import valorless.havenbags.Crafting;
import valorless.havenbags.Encumbering;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.translate.Translator;

public class CommandReload {
	
	static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		if(command != null) {
			for(String arg : command.args) {
				Log.Debug(Main.plugin, arg);
			}
		}
		try {
			Main.CloseBags();
			Main.config.Reload();
			Lang.lang.Reload();
			Main.blacklist.Reload();
			Main.plugins.Reload();
			BagData.Reload();
			if (command.args.length >= 2){
				if(command.args[1].equalsIgnoreCase("force")) {
					BagData.ForceReload();
				}
			}
			Crafting.config.Reload();
			Crafting.RemoveRecipes();
			Crafting.PrepareRecipes();
			AutoPickup.filter.Reload();
			AutoPickup.Initiate();
			Main.weight.Reload();
			Encumbering.Reload();
			Main.textures.Reload();
			Main.translator = new Translator(Main.config.GetString("language"));

			if (command.args.length >= 2){
				if(!(command.sender instanceof Player)) { 
					Log.Info(Main.plugin, "Reload Forced!");
				}else {
					command.sender.sendMessage(Name +" §aReload Forced.");
				}
			}else {
				if(!(command.sender instanceof Player)) { 
					Log.Info(Main.plugin, "Reloaded!");
				}else {
					command.sender.sendMessage(Name +" §aReloaded.");
				}
			}
			Log.Warning(Main.plugin, "It is possible that not everything was reloaded, to ensure everything has reloaded, it is recommended to restart or reload the server.");
			return true;
		}catch(Exception e) {
			Log.Error(Main.plugin, "Something failed during reload.");
			return false;
		}
	}
}