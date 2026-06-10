package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandEmpty {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.isBag(item)) {
			if(BagData.isBagOpen(item)) {
				Log.Warning(Main.plugin, "Due to a recent bug, this player may be attempting to exploit the empty command while the bag is open: " + player.getName());
				return true;
			}
			if(HavenBags.isOwner(item, player)) {
				HavenBags.emptyBag(item, player);
			}else {
				player.sendMessage(Lang.get("prefix") + Lang.get("bag-cannot-use"));
			}
		}
		return true;
	}
}
