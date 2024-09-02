package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandWeight {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		if(command != null) {
			for(String arg : command.args) {
				Log.Debug(Main.plugin, arg);
			}
		}
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.IsBag(item)) {
			if(HavenBags.IsOwner(item, player)) {
				try {
					//Integer value = Integer.valueOf(args[1]);
					Double value = Double.valueOf(command.args[1]);
					BagData.SetWeightMax(HavenBags.GetBagUUID(item), value);
					HavenBags.UpdateBagItem(item, null, player);
					//NBT.SetDouble(item, "bag-weight-limit", value);
				} catch (Exception e) {
					//e.printStackTrace();
					player.sendMessage("§cValue must be a number.");
					return false;
				}
				HavenBags.UpdateBagLore(item, player);
			}
		}
		return true;
	}
}
