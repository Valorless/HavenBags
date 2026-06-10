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

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;
		Player player = (Player)command.sender;
		
		if(command.args.length >= 2) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				Data data = BagData.getBag(HavenBags.getBagUUID(item), null);
				if(command.args[1].equalsIgnoreCase("none")) {
					//PDC.SetString(item, "bag-filter", null);
					data.setEffect("null");
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("effects-command").replace("%value%",
							"none"), player));
					HavenBags.updateBagItem(item, player);
					return true;
				}
				boolean c = false;
				for(String effect : BagEffects.getEffectNames()) {
					if(effect.equalsIgnoreCase(command.args[1])) {
						if(PDC.getString(item, "uuid").equalsIgnoreCase("null")) {
							PDC.setString(item, "effect", effect);
							List<Placeholder> ph = new ArrayList<>();
							ph.add(new Placeholder("%effect%", Lang.parse(BagEffects.getEffectDisplayname(effect), null)));
							ItemMeta meta = item.getItemMeta();
							List<String> lore = meta.getLore();
							lore.add(Lang.parse(Lang.get("bag-effect"), ph));
							meta.setLore(lore);
							item.setItemMeta(meta);
							player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("effects-command").replace("%value%",
									BagEffects.getEffectDisplayname(effect)), player));
							return true;
						}
						data.setEffect(effect);
						player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("effects-command").replace("%value%",
								BagEffects.getEffectDisplayname(effect)), player));
						//PDC.SetString(item, "bag-filter", args[1]);
						HavenBags.updateBagItem(item, player);
						c = true; // Future Valor: why this? // Future Future Valor: I still have no clue.
						return true;
					}
				}
				if(c == false) {
					player.sendMessage(Lang.get("prefix") + Lang.get("malformed-command"));
				}
			}
			return true;
		} else {
			ItemStack item = player.getInventory().getItemInMainHand();
			if(HavenBags.isBag(item)) {
				if(PDC.getString(item, "uuid") == "null") {
					PDC.setString(item, "effect", "null");
					List<Placeholder> ph = new ArrayList<>();
					ph.add(new Placeholder("%effect%", "null"));
					ItemMeta meta = item.getItemMeta();
					List<String> lore = meta.getLore();
					lore.add(Lang.parse(Lang.get("bag-effect"), ph));
					meta.setLore(lore);
					item.setItemMeta(meta);
					return true;
				}
				Data data = BagData.getBag(HavenBags.getBagUUID(item), null);
				data.setEffect("null");
				player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("effects-command").replace("%value%",
						"none"), player));
				HavenBags.updateBagItem(item, player);
				//HavenBags.UpdateBagLore(item, player);
				return true;
			}
		}
		return true;
	}
}
