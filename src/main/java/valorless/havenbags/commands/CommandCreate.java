package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.items.BagItemFactory;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.logging.Log;

public class CommandCreate {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";
	static String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

	public static boolean Run(HBCommand command) {
		ItemStack bagItem = new ItemStack(Material.DIRT);
		bagTexture = Main.config.getString("bag.texture");
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		if (command.args.length >= 2){
			if(command.args[1].equalsIgnoreCase("ownerless")) {
				if (command.args.length >= 3){
					//int size = Utils.Clamp(Integer.parseInt(command.args[2]), 1, 6);
					int size = Integer.parseInt(command.args[2]);
					//int slots = HavenBags.findClosestNine(size);
					bagItem = BagItemFactory.createBagItem(false, size, (Player)command.sender);
					if(!HavenBags.isPowerOfNine(size)) {
						PDC.SetBoolean(bagItem, "upgrade", false);
					}
					Bukkit.getPlayer(command.sender.getName()).getInventory().addItem(bagItem);
					Log.debug(Main.plugin, "[DI-138] " + String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size, "false"));
					//sender.sendMessage(JsonUtils.toJson(bagItem));
				}else {
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
				}
			}
			else {
				try{
					//int size = Utils.Clamp(Integer.parseInt(command.args[1]), 1, 6);
					int size = Integer.parseInt(command.args[1]);
					//int slots = HavenBags.findClosestNine(size);

					bagItem = BagItemFactory.createBagItem(false, size, (Player)command.sender);
					if(!HavenBags.isPowerOfNine(size)) {
						PDC.SetBoolean(bagItem, "upgrade", false);
					}
					
					Bukkit.getPlayer(command.sender.getName()).getInventory().addItem(bagItem);
					Log.debug(Main.plugin, "[DI-139] " + String.format("Bag created: %s %s %s %s", "null", "null", size, "true"));
				}
				catch (NumberFormatException ex){
					ex.printStackTrace();
					command.sender.sendMessage(Lang.Get("prefix") + String.format(Lang.Get("number-conversion-error"), command.args[1]));
				}
			}
		}else {
			command.sender.sendMessage(Name + "§c /havenbags create <size>\n/havenbags create ownerless <size>");
		}
		return true;
	}
}
