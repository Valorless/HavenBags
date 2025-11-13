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
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		if(!Main.config.GetBool("trusting")) {
			player.sendMessage(Lang.Get("prefix") + Lang.Get("feature-disabled"));
			return true;
		}
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player)) {
					try {
						//List<String> list = PDC.GetStringList(item, "bag-trust");
						//for(int i = 0; i < list.size(); i++) {
							//if(list.get(i).equalsIgnoreCase(args[1])) {
								//list.remove(i);
							//}
						//}
						BagData.RemoveTrusted(HavenBags.GetBagUUID(item), command.args[1]);						
						HavenBags.UpdateBagItem(item, player);
						
						List<Placeholder> ph = new ArrayList<Placeholder>();
						ph.add(new Placeholder("%trusted%", command.args[1]));
						player.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("player-untrusted"), ph));
						return true;
					}catch(Exception e) {
						e.printStackTrace();
					}
					
				}else {
					player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
				}
			}
			return true;
		}
		return true;
	}
}
