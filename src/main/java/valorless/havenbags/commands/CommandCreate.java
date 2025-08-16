package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.utils.Utils;

public class CommandCreate {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";
	static String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

	public static boolean Run(HBCommand command) {
		ItemStack bagItem = new ItemStack(Material.DIRT);
		bagTexture = Main.config.GetString("bag-texture");
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		if (command.args.length >= 2){
			if(command.args[1].equalsIgnoreCase("ownerless")) {
				if (command.args.length >= 3){
					int size = Utils.Clamp(Integer.parseInt(command.args[2]), 1, 6);
					if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
						if(Main.config.GetBool("bag-textures.enabled")) {
							for(int s = 9; s <= 54; s += 9) {
								if(size*9 == s) {
									bagItem = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-ownerless-" + size*9));
								}
							}
						}else {
							bagItem = HeadCreator.itemFromBase64(bagTexture);
						}
					} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
						bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
					} else {
						command.sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
						return true;
					}
					ItemMeta bagMeta = bagItem.getItemMeta();
					if(Main.config.GetInt("bag-custom-model-data") != 0) {
						bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
					}
					if(Main.config.GetBool("bag-custom-model-datas.enabled")) {
						for(int s = 9; s <= 54; s += 9) {
							if(size*9 == s) {
								bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-ownerless-" + s));
							}
						}
					}
					bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
					List<String> lore = new ArrayList<String>();
					for (String l : Lang.lang.GetStringList("bag-lore")) {
						if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, (Player)command.sender));
					}
					placeholders.add(new Placeholder("%size%", size*9));
			        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, (Player)command.sender));
					//for (String l : Lang.lang.GetStringList("bag-size")) {
					//	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), (Player)sender));
					//}
					bagMeta.setLore(lore);
					bagItem.setItemMeta(bagMeta);
					
					if(!Utils.IsStringNullOrEmpty(Main.config.GetString("bag-item-model"))) {
						ItemUtils.SetItemModel(bagItem, Main.config.GetString("bag-item-model"));
					}
					
					//PDC.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
					PDC.SetString(bagItem, "uuid", "null");
					PDC.SetString(bagItem, "owner", "null");
					PDC.SetInteger(bagItem, "size", size*9);
					PDC.SetBoolean(bagItem, "binding", false);
					Bukkit.getPlayer(command.sender.getName()).getInventory().addItem(bagItem);
					Log.Debug(Main.plugin, "[DI-138] " + String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size*9, "false"));
					//sender.sendMessage(JsonUtils.toJson(bagItem));
				}else {
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
				}
			}
			else {
				try{
					int size = Utils.Clamp(Integer.parseInt(command.args[1]), 1, 6);
					if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
						if(Main.config.GetBool("bag-textures.enabled")) {
							for(int s = 9; s <= 54; s += 9) {
								if(size*9 == s) {
									bagItem = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-" + size*9));
								}
							}
						}else {
							bagItem = HeadCreator.itemFromBase64(bagTexture);
						}
					} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
						bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
					} else {
						command.sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
						return true;
					}
					ItemMeta bagMeta = bagItem.getItemMeta();
					if (bagMeta == null) {
						PDC.SetString(bagItem, "uuid", "null");
						bagMeta = bagItem.getItemMeta();
			        }
					if(Main.config.GetInt("bag-custom-model-data") != 0) {
						bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
					}
					if(Main.config.GetBool("bag-custom-model-datas.enabled")) {
						for(int s = 9; s <= 54; s += 9) {
							if(size*9 == s) {
								bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + s));
							}
						}
					}
					bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
					List<String> lore = new ArrayList<String>();
					for (String l : Lang.lang.GetStringList("bag-lore")) {
						if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, (Player)command.sender));
					}
					placeholders.add(new Placeholder("%size%", size*9));
			        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, (Player)command.sender));
					//for (String l : Lang.lang.GetStringList("bag-size")) {
					//	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), (Player)sender));
					//}
					bagMeta.setLore(lore);
					bagItem.setItemMeta(bagMeta);
					//PDC.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
					PDC.SetString(bagItem, "uuid", "null");
					PDC.SetString(bagItem, "owner", "null");
					PDC.SetInteger(bagItem, "size", size*9);
					PDC.SetBoolean(bagItem, "binding", true);
					Bukkit.getPlayer(command.sender.getName()).getInventory().addItem(bagItem);
					Log.Debug(Main.plugin, "[DI-139] " + String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
				}
				catch (NumberFormatException ex){
					ex.printStackTrace();
					command.sender.sendMessage(Lang.Get("prefix") + String.format(Lang.Get("number-conversion-error"), command.args[1]));
				}
			}
		}else {
			command.sender.sendMessage(Name + "§c /havenbags create <size>\n/havenbags create ownerless <size>");
		}
		return true;
	}
}
