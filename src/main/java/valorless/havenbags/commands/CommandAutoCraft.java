package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Bag;

public class CommandAutoCraft {
	
	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.isBag(item)) {
			if(HavenBags.isOwner(item, player)) {
				String uuid = HavenBags.getBagUUID(item);
				if(command.args.length >= 2) {
					if(command.args[1].equalsIgnoreCase("on") || command.args[1].equalsIgnoreCase("off")) {
						Boolean value = false;
						if(command.args[1].equalsIgnoreCase("on")) value = true;
						if(command.args[1].equalsIgnoreCase("off")) value = false;
						Bag data = Database.getBag(uuid);
						data.setAutoCraft(value);
						HavenBags.updateBagItem(item, player);
						player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("auto-craft-command").replace("%value%", command.args[1]), player));
						return true;
					}
				}else {
					Bag data = Database.getBag(uuid);
					data.setAutoCraft(false);
					HavenBags.updateBagItem(item, player);
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("auto-craft-command").replace("%value%", "off"), player));
					return true;

				}
			}else {
				player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-cannot-use"), player));
			}
		}
		return true;
	}
}
