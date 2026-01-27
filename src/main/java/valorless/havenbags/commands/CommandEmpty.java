package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandEmpty {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.IsBag(item)) {
			if(BagData.IsBagOpen(item)) {
				Log.Warning(Main.plugin, "Due to a recent bug, this player may be attempting to exploit the empty command while the bag is open: " + player.getName());
				return true;
			}
			if(HavenBags.IsOwner(item, player)) {
				HavenBags.EmptyBag(item, player);
			}else {
				player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
			}
		}
		return true;
	}
}
