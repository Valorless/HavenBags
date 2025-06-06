package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Data;

public class CommandAutoSort {
	
	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.IsBag(item)) {
			if(HavenBags.IsOwner(item, player)) {
				String uuid = HavenBags.GetBagUUID(item);
				if(command.args.length >= 2) {
					if(command.args[1].equalsIgnoreCase("on") || command.args[1].equalsIgnoreCase("off")) {
						Boolean value = false;
						if(command.args[1].equalsIgnoreCase("on")) value = true;
						if(command.args[1].equalsIgnoreCase("off")) value = false;
						Data data = BagData.GetBag(uuid, null);
						data.setAutoSort(value);
						HavenBags.UpdateBagItem(item, null, player);
						player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-sort-command").replace("%value%", command.args[1]), player));
						return true;
					}
				}else {
					Data data = BagData.GetBag(uuid, null);
					data.setAutoSort(false);
					HavenBags.UpdateBagItem(item, null, player);
					player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-sort-command").replace("%value%", "off"), player));
					return true;

				}
			}else {
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-cannot-use"), player));
			}
		}
		return true;
	}
}
