package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.persistentdatacontainer.PDC;

public class CommandEffect {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		Player player = (Player)command.sender;
		
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				Data data = BagData.GetBag(HavenBags.GetBagUUID(item), null);
				if(command.args[1].equalsIgnoreCase("none")) {
					//PDC.SetString(item, "bag-filter", null);
					data.setEffect("null");
					player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("effects-command").replace("%value%", 
							"none"), player));
					HavenBags.UpdateBagItem(item, null, player);
					return true;
				}
				boolean c = false;
				for(String effect : BagEffects.getEffectNames()) {
					if(effect.equalsIgnoreCase(command.args[1])) {
						if(PDC.GetString(item, "uuid").equalsIgnoreCase("null")) {
							PDC.SetString(item, "effect", effect);
							List<Placeholder> ph = new ArrayList<>();
							ph.add(new Placeholder("%effect%", Lang.Parse(BagEffects.getEffectDisplayname(effect), null)));
							ItemMeta meta = item.getItemMeta();
							List<String> lore = meta.getLore();
							lore.add(Lang.Parse(Lang.Get("bag-effect"), ph));
							meta.setLore(lore);
							item.setItemMeta(meta);
							player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("effects-command").replace("%value%", 
									BagEffects.getEffectDisplayname(effect)), player));
							return true;
						}
						data.setEffect(effect);
						player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("effects-command").replace("%value%", 
								BagEffects.getEffectDisplayname(effect)), player));
						//PDC.SetString(item, "bag-filter", args[1]);
						HavenBags.UpdateBagItem(item, null, player);
						c = true; // Future Valor: why this? // Future Future Valor: I still have no clue.
						return true;
					}
				}
				if(c == false) {
					player.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
				}
			}
			return true;
		} else {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.IsBag(item)) {
				if(PDC.GetString(item, "uuid") == "null") {
					PDC.SetString(item, "effect", "null");
					List<Placeholder> ph = new ArrayList<>();
					ph.add(new Placeholder("%effect%", "null"));
					ItemMeta meta = item.getItemMeta();
					List<String> lore = meta.getLore();
					lore.add(Lang.Parse(Lang.Get("bag-effect"), ph));
					meta.setLore(lore);
					item.setItemMeta(meta);
					return true;
				}
				Data data = BagData.GetBag(HavenBags.GetBagUUID(item), null);
				data.setEffect("null");
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("effects-command").replace("%value%", 
						"none"), player));
				HavenBags.UpdateBagItem(item, null, player);
				//HavenBags.UpdateBagLore(item, player);
				return true;
			}
		}
		return true;
	}
}
