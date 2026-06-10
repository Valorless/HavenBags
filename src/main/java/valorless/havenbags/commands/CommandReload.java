package valorless.havenbags.commands;

import org.bukkit.entity.Player;

import valorless.havenbags.BagData;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.features.BagHealth;
import valorless.havenbags.features.Crafting;
import valorless.havenbags.features.CustomBags;
import valorless.havenbags.features.CustomData;
import valorless.havenbags.features.Encumbering;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.translate.Translator;

@SuppressWarnings("deprecation")
public class CommandReload {
	
	static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		try {
			Main.closeBags();
			Main.config.Reload();
			Lang.lang.Reload();
			Main.blacklist.Reload();
			Main.plugins.Reload();
			BagData.reload();
			if (command.args.length >= 2){
				if(command.args[1].equalsIgnoreCase("force")) {
					BagData.forceReload();
				}
			}
			Crafting.config.Reload();
			Crafting.removeRecipes();
			Crafting.PrepareRecipes();
			AutoPickup.filter.Reload();
			AutoPickup.initiate();
			Main.weight.Reload();
			Encumbering.reload();
			CustomBags.file.Reload();
			CustomBags.init();
			Main.textures.Reload();
			CustomData.reload();
			BagHealth.reload();
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
			return true;
		}
	}
}
