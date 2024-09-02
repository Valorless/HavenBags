package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandTrust {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		if(command != null) {
			for(String arg : command.args) {
				Log.Debug(Main.plugin, arg);
			}
		}
		
		Player player = (Player)command.sender;
		if(!Main.config.GetBool("trusting")) {
			player.sendMessage(Lang.Get("prefix") + Lang.Get("feature-disabled"));
			return false;
		}
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player)) {
					if(HavenBags.IsPlayerTrusted(item, command.args[1])) {
						return false;
					}
					try {
						//List<String> list = new ArrayList<String>();
						//if(NBT.Has(item, "bag-trust")) {
							//list = NBT.GetStringList(item, "bag-trust");
						//}
						//list.add(args[1]);
						//NBT.SetStringList(item, "bag-trust", list);
						BagData.AddTrusted(HavenBags.GetBagUUID(item), command.args[1]);
						HavenBags.UpdateBagItem(item, null, player);
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
		return false;
	}
}
