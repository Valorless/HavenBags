package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;

public class CommandModelData {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player) || player.hasPermission("havenbags.bypass")) {
					ItemMeta meta = item.getItemMeta();
					try {
						meta.setCustomModelData(Integer.valueOf(command.args[1]));
					}catch(Exception e) { player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command")); }
					item.setItemMeta(meta);
				}else {
					player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
				}
			}
			return true;
		}
		return true;
	}
}
