package valorless.havenbags.commands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.valorlessutils.items.ItemUtils;

public class CommandItemModel {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		Player player = (Player)command.sender;
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				if(HavenBags.isOwner(item, player) || player.hasPermission("havenbags.bypass")) {
					if(command.args.length >= 3) {
						try {
							Material mat = Material.valueOf(command.args[2].toUpperCase());
							item.setType(mat);
						}catch(Exception e) {}
					}
					try {
						String model = command.args[1];
						ItemUtils.SetItemModel(item, model);
					}catch(Exception e) { player.sendMessage(Lang.get("prefix") + Lang.get("malformed-command")); }
				}else {
					player.sendMessage(Lang.get("prefix") + Lang.get("bag-cannot-use"));
				}
			}
			return true;
		}
		return true;
	}
}
