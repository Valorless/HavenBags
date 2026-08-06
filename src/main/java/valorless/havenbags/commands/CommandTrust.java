package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.datamodels.Placeholder;

public class CommandTrust {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		Player player = (Player)command.sender;
		if(!Main.config.getBool("trusting")) {
			player.sendMessage(Lang.get("prefix") + Lang.get("feature-disabled"));
			return true;
		}
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				Bag data = Database.getBag(HavenBags.getBagUUID(item));
				if(HavenBags.isOwner(item, player)) {
					if(data.isPlayerTrusted(command.args[1])) {
						return true;
					}
					try {
						//List<String> list = new ArrayList<String>();
						//if(PDC.Has(item, "bag-trust")) {
							//list = PDC.GetStringList(item, "bag-trust");
						//}
						//list.add(args[1]);
						//PDC.SetStringList(item, "bag-trust", list);
						Database.getBag(HavenBags.getBagUUID(item), null).addTrusted(command.args[1]);
						HavenBags.updateBagItem(item, player);
						
						List<Placeholder> ph = new ArrayList<Placeholder>();
						ph.add(new Placeholder("%trusted%", command.args[1]));
						player.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("player-trusted"), ph));
						return true;
					}catch(Exception e) {
						e.printStackTrace();
					}
					
				}else {
					player.sendMessage(Lang.get("prefix") + Lang.get("bag-cannot-use"));
				}
			}
			return true;
		}
		return true;
	}
}
