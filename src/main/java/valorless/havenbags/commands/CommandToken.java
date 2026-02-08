package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.utils.Base64Validator;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandToken {

	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {

		try {
			Player player = null;
			if(command.sender instanceof Player pl) {
				player = pl;
			}
			Player target = null;
			if(command.args.length >= 3) {
				if(command.args.length >= 4) {
					target = Bukkit.getPlayer(command.args[3]);
				}
				if(command.args[1].equalsIgnoreCase("texture")) {
					String value = command.args[2];
					ItemStack token = null;
					token = HavenBags.CreateSkinToken(value, TokenType.Texture);
					if(token != null) {
						if(target != null) {
							if(target.getInventory().firstEmpty() != -1) {
								target.getInventory().addItem(token);
							} else {
								target.getWorld().dropItem(player.getLocation(), token);
							}
							Log.Info(Main.plugin, String.format("Gave token %s to %s.", command.args[2], command.args[3]));
						}else {
							player.getInventory().addItem(token);
						}
					}else {
						player.sendMessage(Name + "§cInvalid texture value.");
					}
				}
				else if(command.args[1].equalsIgnoreCase("custommodeldata")) {
					ItemStack token = HavenBags.CreateSkinToken(command.args[2], TokenType.ModelData);
					if(target != null) {
						if(target.getInventory().firstEmpty() != -1) {
							target.getInventory().addItem(token);
						} else {
							target.getWorld().dropItem(player.getLocation(), token);
						}
						Log.Info(Main.plugin, String.format("Gave token %s to %s.", command.args[2], command.args[3]));
					}else {
						player.getInventory().addItem(token);
					}
					//player.getInventory().addItem(HavenBags.CreateToken(command.args[2]));
				}
				else if(command.args[1].equalsIgnoreCase("itemmodel")) {
					ItemStack token = HavenBags.CreateSkinToken(command.args[2], TokenType.ItemModel);
					if(target != null) {
						if(target.getInventory().firstEmpty() != -1) {
							target.getInventory().addItem(token);
						} else {
							target.getWorld().dropItem(player.getLocation(), token);
						}
						Log.Info(Main.plugin, String.format("Gave token %s to %s.", command.args[2], command.args[3]));
					}else {
						player.getInventory().addItem(token);
					}
					//player.getInventory().addItem(HavenBags.CreateToken(command.args[2]));
				}
				else if(command.args[1].equalsIgnoreCase("effect")) {
					ItemStack token = HavenBags.CreateEffectToken(command.args[2]);
					if(target != null) {
						if(target.getInventory().firstEmpty() != -1) {
							target.getInventory().addItem(token);
						} else {
							target.getWorld().dropItem(player.getLocation(), token);
						}
						Log.Info(Main.plugin, String.format("Gave token %s to %s.", command.args[2], command.args[3]));
					}else {
						player.getInventory().addItem(token);
					}
					//player.getInventory().addItem(HavenBags.CreateToken(command.args[2]));
				}
				return true;
			}
			return true;
		}catch(Exception e) {
			//e.printStackTrace();
			return false;
		}
	}
}
