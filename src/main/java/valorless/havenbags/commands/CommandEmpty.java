package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;

public class CommandEmpty {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.IsBag(item)) {
			if(HavenBags.IsOwner(item, player)) {
				HavenBags.EmptyBag(item, player);
			}else {
				player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
			}
		}
		return true;
	}
}
