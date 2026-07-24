package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;

public class CommandRawInfo {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
		ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			
		if(HavenBags.isBag(hand)) {
			String info = "§6## HavenBag Bag Raw Information ##";
			info = info + "\n  §f" + Database.getBag(HavenBags.getBagUUID(hand)).toString();
			command.sender.sendMessage(info);
			return true;
		}
		return true;
	}
}
