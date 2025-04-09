package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
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
					if(command.args[2].chars().count() > 30) {
						if(Base64Validator.isValidBase64(command.args[2])) {
							ItemStack token = HavenBags.CreateToken(command.args[2]);
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
							player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
							return true;
						}
					}else {
						if(Base64Validator.isValidBase64(Main.textures.GetString(String.format("textures.%s", command.args[2])))) {
							ItemStack token = HavenBags.CreateToken(Main.textures.GetString(String.format("textures.%s", command.args[2])), command.args[2]);
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
							player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
							return true;
						}
					}
				}else {
					if(command.args[1].equalsIgnoreCase("custommodeldata")) {
						ItemStack token = HavenBags.CreateToken(command.args[2]);
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
