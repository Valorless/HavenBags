package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandRawInfo {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		if(command != null) {
			for(String arg : command.args) {
				Log.Debug(Main.plugin, arg);
			}
		}
		
		ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
		ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			
		if(HavenBags.IsBag(hand)) {
			String info = "§6## HavenBag Bag Raw Information ##";
			info = info + "\n  §f" + meta.toString();
			command.sender.sendMessage(info);
			return true;
		}
		return false;
	}
}
