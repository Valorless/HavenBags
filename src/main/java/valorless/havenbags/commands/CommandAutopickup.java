package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandAutopickup {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		Player player = (Player)command.sender;
		
		if(!Main.config.getBool("auto-pickup.enabled")) {
			player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("feature-disabled"), player));
			return true;
		}
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				if(HavenBags.isOwner(item, player)) {
					if(command.args[1].equalsIgnoreCase("none")) {
						//PDC.SetString(item, "bag-filter", null);
						Database.getBag(HavenBags.getBagUUID(item), null).resetAutopickup();
						//Database.setAutoPickup(HavenBags.getBagUUID(item), "null");
						player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("auto-pickup-command").replace("%value%",
								"none"), player));
						HavenBags.updateBagItem(item, player);
						return true;
					}
					boolean c = false;
					for(String filter : AutoPickup.getFilterNames(null)) {
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
							if(PDC.getString(item, "uuid").equalsIgnoreCase("null")) {
								PDC.setString(item, "filter", filter);
								List<Placeholder> ph = new ArrayList<>();
								ph.add(new Placeholder("%filter%", AutoPickup.getFilterDisplayname(filter)));
								ItemMeta meta = item.getItemMeta();
								List<String> lore = meta.getLore();
								lore.add(Lang.parse(Lang.get("bag-auto-pickup"), ph));
								meta.setLore(lore);
								item.setItemMeta(meta);
								player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("auto-pickup-command").replace("%value%",
										AutoPickup.getFilterDisplayname(filter)), player));
								return true;
							}
							//Database.setAutoPickup(HavenBags.getBagUUID(item), filter);
							Database.getBag(HavenBags.getBagUUID(item), null).setAutopickup(filter);
							player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("auto-pickup-command").replace("%value%",
									AutoPickup.getFilterDisplayname(filter)), player));
							//PDC.SetString(item, "bag-filter", args[1]);
							HavenBags.updateBagItem(item, player);
							c = true; // Future Valor: why this? // Future Future Valor: I still have no clue.
							return true;
						}
					}
					if(c == false) {
						player.sendMessage(Lang.get("prefix") + Lang.get("malformed-command"));
					}
					
				}else {
					player.sendMessage(Lang.get("prefix") + Lang.get("bag-cannot-use"));
				}
			}
			return true;
		} else {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				if(HavenBags.isOwner(item, player)) {
					//PDC.SetString(item, "bag-filter", null);
					if(PDC.getString(item, "uuid") == "null") {
						PDC.setString(item, "filter", "null");
						List<Placeholder> ph = new ArrayList<>();
						ph.add(new Placeholder("%filter%", "null"));
						ItemMeta meta = item.getItemMeta();
						List<String> lore = meta.getLore();
						lore.add(Lang.parse(Lang.get("bag-auto-pickup"), ph));
						meta.setLore(lore);
						item.setItemMeta(meta);
						return true;
					}
					Database.getBag(HavenBags.getBagUUID(item), null).resetAutopickup();
					//Database.setAutoPickup(HavenBags.getBagUUID(item), "null");
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("auto-pickup-command").replace("%value%",
							"none"), player));
					HavenBags.updateBagItem(item, player);
					//HavenBags.UpdateBagLore(item, player);
					return true;
				}else {
					player.sendMessage(Lang.get("prefix") + Lang.get("bag-cannot-use"));
				}
			}
		}
		return true;
	}
}
