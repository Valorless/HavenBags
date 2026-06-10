package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;

public class CommandUntrust {

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
				if(HavenBags.isOwner(item, player)) {
					try {
						//List<String> list = PDC.GetStringList(item, "bag-trust");
						//for(int i = 0; i < list.size(); i++) {
							//if(list.get(i).equalsIgnoreCase(args[1])) {
								//list.remove(i);
							//}
						//}
						BagData.removeTrusted(HavenBags.getBagUUID(item), command.args[1]);
						HavenBags.updateBagItem(item, player);
						
						List<Placeholder> ph = new ArrayList<Placeholder>();
						ph.add(new Placeholder("%trusted%", command.args[1]));
						player.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("player-untrusted"), ph));
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
