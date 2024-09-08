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
import valorless.havenbags.Placeholder;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.utils.Utils;

public class CommandGive {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";
	static String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

	public static boolean Run(HBCommand command) {
		
		ItemStack bagItem = new ItemStack(Material.DIRT);
		bagTexture = Main.config.GetString("bag-texture");
		
		if (command.args.length >= 3){
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
			Player receiver = Bukkit.getPlayer(command.args[1]);
			if(command.args[2].equalsIgnoreCase("ownerless")) {
				if (command.args.length >= 3){
					int size = Utils.Clamp(Integer.parseInt(command.args[3]), 1, 6);

						//String uuid = UUID.randomUUID().toString();
						//final Bag bag = new Bag(uuid, null, number*9, true);
						if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
							if(Main.config.GetBool("bag-textures.enabled")) {
								for(int s = 9; s <= 54; s += 9) {
									if(size*9 == s) {
										bagItem = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-ownerless-" + s));
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
							bagMeta = Bukkit.getServer().getItemFactory().getItemMeta(bagItem.getType());
				        }
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
						//bagMeta.setDisplayName("§aUnused Bag");
						
						bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
						List<String> lore = new ArrayList<String>();
						for (String l : Lang.lang.GetStringList("bag-lore")) {
							if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, receiver));
						}
						//lore.add(Lang.Get("bag-size", size*9));
				        placeholders.add(new Placeholder("%size%", size*9));
				        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, receiver));
				        	//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
						//for (String l : Lang.lang.GetStringList("bag-size")) {
						//	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), receiver));
						//}
						bagMeta.setLore(lore);
						bagItem.setItemMeta(bagMeta);
						NBT.SetString(bagItem, "bag-uuid", "null");
						NBT.SetString(bagItem, "bag-owner", "null");
						NBT.SetInt(bagItem, "bag-size", size*9);
						NBT.SetBool(bagItem, "bag-canBind", false);
						receiver.getInventory().addItem(bagItem);
				        placeholders.add(new Placeholder("%name%", Lang.Get("bag-ownerless-unused")));
						receiver.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-given"), placeholders));
						Log.Debug(Main.plugin, "[DI-140] " + String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size*9, "false"));
						//sender.sendMessage(JsonUtils.toJson(bagItem));
				}else {
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
				}
			}
			else {
				try{
					int size = Utils.Clamp(Integer.parseInt(command.args[2]), 1, 6);
						//String uuid = UUID.randomUUID().toString();
						//final Bag bag = new Bag(uuid, null, number*9, true); //<-- Remove this & Bag.java
						if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
							if(Main.config.GetBool("bag-textures.enabled")) {
								for(int s = 9; s <= 54; s += 9) {
									if(size*9 == s) {
										bagItem = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-" + s));
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
									bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + s));
								}
							}
						}
						//bagMeta.setDisplayName("§aUnbound Bag");
						bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
						//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
						List<String> lore = new ArrayList<String>();
						for (String l : Lang.lang.GetStringList("bag-lore")) {
							if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, receiver));
						}
						//lore.add(Lang.Get("bag-size", size*9));

				        placeholders.add(new Placeholder("%size%", size*9));
				        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, receiver));
				        
						bagMeta.setLore(lore);
						bagItem.setItemMeta(bagMeta);
						
						NBT.SetString(bagItem, "bag-uuid", "null");
						NBT.SetString(bagItem, "bag-owner", "null");
						NBT.SetInt(bagItem, "bag-size", size*9);
						NBT.SetBool(bagItem, "bag-canBind", true);
						//Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
						receiver.getInventory().addItem(bagItem);
				        placeholders.add(new Placeholder("%name%", Lang.Get("bag-unbound-name")));
						receiver.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-given"), placeholders));
						//receiver.sendMessage(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-unbound-name")));
						//sender.sendMessage(JsonUtils.toJson(bagItem));
						Log.Debug(Main.plugin, "[DI-141] " + String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
				}
				catch (NumberFormatException ex){
					ex.printStackTrace();
			        placeholders.add(new Placeholder("%value%", command.args[2]));
			        command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("number-conversion-error"), placeholders));
				}
			}
		}else {
			command.sender.sendMessage(Name + "§c /havenbags give <player> <size>\n/havenbags give <player> ownerless <size>");
		}
		return true;
	}
}
