package valorless.havenbags.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.BagGUI;
import valorless.havenbags.CommandListener;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.utils.Utils;

public class CommandPreview {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";
	static String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

	public static boolean Run(HBCommand command) {
		
		ItemStack bagItem = new ItemStack(Material.DIRT);
		bagTexture = Main.config.GetString("bag-texture");
		
		if (command.args.length >= 2){ // Player Name
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
			String owner = command.args[1];
			OfflinePlayer player;
			try {
				player = Bukkit.getOfflinePlayer(UUID.fromString(owner));
				placeholders.add(new Placeholder("%player%", player.getName()));
			}catch(Exception e) {
				OfflinePlayer[] offp = Bukkit.getOfflinePlayers();
				player = offp[0];
				placeholders.add(new Placeholder("%player%", command.args[1]));
			}
			String dirPath = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
			File dir = new File(dirPath);
			if(!dir.exists()) {
				command.sender.sendMessage(Lang.Parse(Lang.Get("player-no-bags"), placeholders, player));
				return true;
			}
			if (command.args.length >= 3){ // Bag UUID
				String uuid = command.args[2];
				String path = String.format("%s/bags/%s/%s.yml", Main.plugin.getDataFolder(), owner, uuid);
				File bagData;
				Data bag = BagData.GetBag(uuid, null);
				try {
					bagData = new File(path);
				} catch(Exception e) {
					command.sender.sendMessage(e.toString());
					e.printStackTrace();
					return true;
				}
				if(!bagData.exists()) {
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-not-found"));
					return true;
				}
				List<ItemStack> contSize = new ArrayList<ItemStack>();
				//contSize = JsonUtils.fromJson(content);
				contSize = bag.getContent();
				if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
					bagItem = HeadCreator.itemFromBase64(bagTexture);
				} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
					bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
				} else {
					command.sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
					return true;
				}
				NBT.SetString(bagItem, "bag-uuid", uuid);
				NBT.SetInt(bagItem, "bag-size", contSize.size());
				if(owner.equalsIgnoreCase("ownerless")) {
					NBT.SetString(bagItem, "bag-owner", owner);
					NBT.SetBool(bagItem, "bag-canBind", false);
				}else {
					NBT.SetString(bagItem, "bag-owner", owner);
					NBT.SetBool(bagItem, "bag-canBind", true);
				}
				ItemMeta bagMeta = bagItem.getItemMeta();
				if(Main.config.GetInt("bag-custom-model-data") != 0) {
					bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
				}
				if(Main.config.GetBool("bag-custom-model-datas.enabled")) {
					for(int s = 9; s <= 54; s += 9) {
						if(contSize.size() == s) {
							if(owner.equalsIgnoreCase("ownerless")) {
								bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-ownerless-" + s));
							}else {
								bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + s));
							}
						}
					}
				}

				if(owner.equalsIgnoreCase("ownerless")) {
					bagMeta.setDisplayName(Lang.Get("bag-ownerless-used"));
				}else {
					bagMeta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-bound-name"), placeholders, player));
					
				}

				int a = 0;
					List<String> items = new ArrayList<String>();
					List<ItemStack> Content = new ArrayList<ItemStack>();
					for(int i = 0; i < Content.size(); i++) {
			    		if(Content.get(i) != null) {
			    			List<Placeholder> itemph = new ArrayList<Placeholder>();
			    			if(Content.get(i).getItemMeta().hasDisplayName()) {
		    					itemph.add(new Placeholder("%item%", Content.get(i).getItemMeta().getDisplayName()));
		    					itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
		    					if(Content.get(i).getAmount() != 1) {
		    						items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
		    					} else {
		    						items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
		    					}
			    			}else {
		    	    			itemph.add(new Placeholder("%item%", Main.translator.Translate(Content.get(i).getType().getTranslationKey())));
		    	    			itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
		    	    			if(Content.get(i).getAmount() != 1) {
		        					items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
		        				} else {
		        					items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
		        				}
			    			}
			    			a++;
			    		}
			    	}
					List<String> lore = new ArrayList<String>();
					for (String l : Lang.lang.GetStringList("bag-lore")) {
						if(!Utils.IsStringNullOrEmpty(l)) {
							lore.add(Lang.Parse(l, player));
							
						}
					}
					//if(NBT.GetBool(bagItem, "bag-canBind")) {
					//	//lore.add(Lang.Get("bound-to", owner));
					//	for (String l : Lang.lang.GetStringList("bound-to")) {
					//		if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getPlayer().getName())));
					//	}
					//}
					if(NBT.GetBool(bagItem, "bag-canBind")) {
						placeholders.add(new Placeholder("%owner%", player.getName()));
			            lore.add(Lang.Parse(Lang.Get("bound-to"), placeholders, player));
			        }
					//lore.add("§7Size: " + contSize.size());
					//lore.add(Lang.Get("bag-size", contSize.size()));
					placeholders.add(new Placeholder("%size%", contSize.size()));
		            lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
					if(a > 0) {
						//lore.add("§7Content:");
						lore.add(Lang.Get("bag-content-title"));
						for(int k = 0; k < items.size(); k++) {
							if(k < 5) {
								lore.add("  " + items.get(k));
							}
						}
						if(a > 5) {
							//lore.add("  §7And more..");
							lore.add(Lang.Get("bag-content-and-more"));
						}
					}
					bagMeta.setLore(lore);
				

				bagItem.setItemMeta(bagMeta);

				BagGUI gui = new BagGUI(Main.plugin, NBT.GetInt(bagItem, "bag-size"), Bukkit.getPlayer(command.sender.getName()), bagItem, bagItem.getItemMeta(), true);
				//Bukkit.getServer().getPluginManager().registerEvents(gui, plugin);
				gui.OpenInventory(Bukkit.getPlayer(command.sender.getName()));
				//HavenBags.activeBags.remove(gui);
				
				Log.Debug(Main.plugin, "[DI-142] " + "Attempting to preview bag");
				Log.Debug(Main.plugin, "[DI-143] " + String.format("%s previwing bag: %s/%s size: %s", command.sender.getName(), owner, uuid, contSize.size()));
				//content = 
			}else {
				// No uuid
				//String owner = args[1];
				String path = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
				Set<String> files = CommandListener.listFilesUsingJavaIO(path);
				String fileString = Lang.Get("prefix") + Lang.Parse(Lang.Get("bags-of"), placeholders, player);
				List<String> fileNames = new ArrayList<String>();
				fileNames.addAll(files);
				for(int i = 0; i < files.size(); i++) {
					String f = fileNames.get(i).replace(".json", "").replace(".yml", "");
					fileString = fileString + "\n" + f;
				}
				command.sender.sendMessage(fileString);
			}
		}else {
			command.sender.sendMessage(Name + "§c /havenbags restore <player> <bag-uuid>");
		}
		return true;
	}
}
