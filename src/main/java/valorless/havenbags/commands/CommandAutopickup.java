package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandAutopickup {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		Player player = (Player)command.sender;
		
		if(!Main.config.GetBool("auto-pickup.enabled")) {
			player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("feature-disabled"), player));
			return true;
		}
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsOwner(item, player)) {
					if(command.args[1].equalsIgnoreCase("none")) {
						//PDC.SetString(item, "bag-filter", null);
						BagData.SetAutoPickup(HavenBags.GetBagUUID(item), "null");
						player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
								"none"), player));
						HavenBags.UpdateBagItem(item, player);
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
							if(PDC.GetString(item, "uuid").equalsIgnoreCase("null")) {
								PDC.SetString(item, "filter", filter);
								List<Placeholder> ph = new ArrayList<>();
								ph.add(new Placeholder("%filter%", AutoPickup.GetFilterDisplayname(filter)));
								ItemMeta meta = item.getItemMeta();
								List<String> lore = meta.getLore();
								lore.add(Lang.Parse(Lang.Get("bag-auto-pickup"), ph));
								meta.setLore(lore);
								item.setItemMeta(meta);
								player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
										AutoPickup.GetFilterDisplayname(filter)), player));
								return true;
							}
							BagData.SetAutoPickup(HavenBags.GetBagUUID(item), filter);
							player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
									AutoPickup.GetFilterDisplayname(filter)), player));
							//PDC.SetString(item, "bag-filter", args[1]);
							HavenBags.UpdateBagItem(item, player);
							c = true; // Future Valor: why this? // Future Future Valor: I still have no clue.
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
					//PDC.SetString(item, "bag-filter", null);
					if(PDC.GetString(item, "uuid") == "null") {
						PDC.SetString(item, "filter", "null");
						List<Placeholder> ph = new ArrayList<>();
						ph.add(new Placeholder("%filter%", "null"));
						ItemMeta meta = item.getItemMeta();
						List<String> lore = meta.getLore();
						lore.add(Lang.Parse(Lang.Get("bag-auto-pickup"), ph));
						meta.setLore(lore);
						item.setItemMeta(meta);
						return true;
					}
					BagData.SetAutoPickup(HavenBags.GetBagUUID(item), "null");
					player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("auto-pickup-command").replace("%value%", 
							"none"), player));
					HavenBags.UpdateBagItem(item, player);
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
