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

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		
		Player player = (Player)command.sender;
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				if(HavenBags.isOwner(item, player) || player.hasPermission("havenbags.bypass")) {
					if(command.args[1].chars().count() > 30) {
						
						try {
							BagData.getBag(HavenBags.getBagUUID(item), item).setTexture(command.args[1]);
						}catch(Exception e) {} // No data found, just change the texture of the item only.
						BagData.setTextureValue(item, command.args[1]);
					}else {
						String texture = Main.textures.GetString(String.format("textures.%s", command.args[1]));
				        if(Utils.IsStringNullOrEmpty(texture)) {
				        	player.sendMessage(Lang.get("prefix") + Lang.get("bag-texture-not-found").replace("%texture%", command.args[1]));
				        	return true;
				        }
						try {
							BagData.getBag(HavenBags.getBagUUID(item), item).setTexture(texture);
						}catch(Exception e) {} // No data found, just change the texture of the item only.
						BagData.setTextureValue(item, texture);
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
