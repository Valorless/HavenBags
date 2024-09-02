package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandTexture {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		if(command != null) {
			for(String arg : command.args) {
				Log.Debug(Main.plugin, arg);
			}
		}
		
		Player player = (Player)command.sender;
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player) || player.hasPermission("havenbags.bypass")) {
					if(command.args[1].chars().count() > 30) {
						BagData.GetBag(HavenBags.GetBagUUID(item), item).setTexture(command.args[1]);
						BagData.setTextureValue(item, command.args[1]);
					}else {
						BagData.GetBag(HavenBags.GetBagUUID(item), item).setTexture(Main.textures.GetString(String.format("textures.%s", command.args[1])));
						BagData.setTextureValue(item, Main.textures.GetString(String.format("textures.%s", command.args[1])));
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
