package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Data;

public class CommandMagnet {
	
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
						data.setMagnet(value);
						HavenBags.UpdateBagItem(item, null, player);
						player.sendMessage(Lang.Get("prefix") + Lang.Get("magnet-command").replace("%value%", command.args[1]));
						return true;
					}
				}else {
					Data data = BagData.GetBag(uuid, null);
					data.setMagnet(false);
					HavenBags.UpdateBagItem(item, null, player);
					player.sendMessage(Lang.Get("prefix") + Lang.Get("magnet-command").replace("%value%", "off"));
					return true;

				}
			}else {
				player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
			}
		}
		return true;
	}
}
