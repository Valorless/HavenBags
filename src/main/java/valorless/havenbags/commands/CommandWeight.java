package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;

public class CommandWeight {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.isBag(item)) {
			if(HavenBags.isOwner(item, player)) {
				try {
					//Integer value = Integer.valueOf(args[1]);
					Double value = Double.valueOf(command.args[1]);
					Database.getBag(HavenBags.getBagUUID(item), null).setWeightMax(value);
					//Database.setWeightMax(HavenBags.getBagUUID(item), value);
					HavenBags.updateBagItem(item, player);
					//PDC.SetDouble(item, "bag-weight-limit", value);
				} catch (Exception e) {
					//e.printStackTrace();
					player.sendMessage("§cValue must be a number.");
					return true;
				}
				HavenBags.updateBagLore(item, player);
			}
		}
		return true;
	}
}
