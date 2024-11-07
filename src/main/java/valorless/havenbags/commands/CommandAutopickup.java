package valorless.havenbags.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.AutoPickup;
import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandAutopickup {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		Player player = (Player)command.sender;
		
		if(!Main.config.GetBool("auto-pickup")) {
			player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("feature-disabled"), player));
			return true;
		}
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player)) {
					if(command.args[1].equalsIgnoreCase("none")) {
						//NBT.SetString(item, "bag-filter", null);
						BagData.SetAutoPickup(HavenBags.GetBagUUID(item), "null");
						HavenBags.UpdateBagItem(item, null, player);
						return true;
					}
					boolean c = false;
					for(String filter : AutoPickup.GetFilterNames(null)) {
						if(filter.equalsIgnoreCase(command.args[1])) {
							if(AutoPickup.filter.HasKey("filters." + filter + ".permission.node")) {
								if(!player.hasPermission("havenbags.bypass")) {
									Log.Debug(Main.plugin, "[DI-133] " + "[AutoPickup] Has Permission");
									if(!AutoPickup.filter.GetString("filters." + filter + ".permission.node").equalsIgnoreCase("none")) {
										Log.Debug(Main.plugin, "[DI-134] " + "[AutoPickup] Permission " + filter);
										if(AutoPickup.filter.GetBool("filters." + filter + ".permission.apply")) {
											if(!command.sender.hasPermission(AutoPickup.filter.GetString("filters." + filter + ".permission.node"))) {
												Log.Debug(Main.plugin, "[DI-135] " + "[AutoPickup] Permission Apply true - Player false");
												return true;
											}else {
												Log.Debug(Main.plugin, "[DI-136] " + "[AutoPickup] Permission Apply true - Player true");
											}
										}else {
											Log.Debug(Main.plugin, "[DI-137] " + "[AutoPickup] Permission Apply false");
											return true;
										}
									}
								}
							}
							BagData.SetAutoPickup(HavenBags.GetBagUUID(item), command.args[1]);
							//NBT.SetString(item, "bag-filter", args[1]);
							HavenBags.UpdateBagItem(item, null, player);
							c = true;
							return true;
						}
					}
					if(c == false) {
						player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
					}
					
				}else {
					player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
				}
			}
			return true;
		} else {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player)) {
					//NBT.SetString(item, "bag-filter", null);
					BagData.SetAutoPickup(HavenBags.GetBagUUID(item), "null");
					HavenBags.UpdateBagItem(item, null, player);
					//HavenBags.UpdateBagLore(item, player);
					return true;
				}else {
					player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
				}
			}
		}
		return true;
	}
}
