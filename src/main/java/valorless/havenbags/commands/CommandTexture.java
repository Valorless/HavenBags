package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.utils.Utils;

public class CommandTexture {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player) || player.hasPermission("havenbags.bypass")) {
					if(command.args[1].chars().count() > 30) {
						
						try {
							BagData.GetBag(HavenBags.GetBagUUID(item), item).setTexture(command.args[1]);
						}catch(Exception e) {} // No data found, just change the texture of the item only.
						BagData.setTextureValue(item, command.args[1]);
					}else {
						String texture = Main.textures.GetString(String.format("textures.%s", command.args[1]));
				        if(Utils.IsStringNullOrEmpty(texture)) {
				        	player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-texture-not-found").replace("%texture%", command.args[1]));
				        	return true;
				        }
						try {
							BagData.GetBag(HavenBags.GetBagUUID(item), item).setTexture(texture);
						}catch(Exception e) {} // No data found, just change the texture of the item only.
						BagData.setTextureValue(item, texture);
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
