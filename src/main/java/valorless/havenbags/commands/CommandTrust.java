package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;

public class CommandTrust {
	
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
				Data data = BagData.GetBag(HavenBags.GetBagUUID(item), item);
				if(HavenBags.IsOwner(item, player)) {
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
						BagData.AddTrusted(HavenBags.GetBagUUID(item), command.args[1]);
						HavenBags.UpdateBagItem(item, null, player);
						
						List<Placeholder> ph = new ArrayList<Placeholder>();
						ph.add(new Placeholder("%trusted%", command.args[1]));
						player.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("player-trusted"), ph));
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
