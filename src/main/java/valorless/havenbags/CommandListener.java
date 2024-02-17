package valorless.havenbags;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;
import valorless.valorlessutils.translate.Translator;
import valorless.valorlessutils.utils.Utils;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandListener implements CommandExecutor {
	
	String Name = "§7[§aHaven§bBags§7]§r";
	String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Log.Debug(Main.plugin, "Sender: " + sender.getName());
    	Log.Debug(Main.plugin, "Command: " + command.toString());
    	Log.Debug(Main.plugin, "Label: " + label);
    	for(String a : args) {
    		Log.Debug(Main.plugin, "Argument: " + a);
    	}
    	
    	bagTexture = Main.config.GetString("bag-texture");
		if(args.length == 0) {
			sender.sendMessage(Name + " HavenBags by Valorless.");
			return false;
		}
		else 
		if (args.length >= 1){
			try {
				if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("havenbags.reload")) {
					try {
						Main.config.Reload();
						Lang.lang.Reload();
						Crafting.config.Reload();
						Crafting.RemoveRecipes();
						Crafting.PrepareRecipes();
						Main.translator = new Translator(Main.config.GetString("language"));
						if(!(sender instanceof Player)) { 
							Log.Info(Main.plugin, "Reloaded!");
						}else {
							sender.sendMessage(Name +" §aReloaded.");
						}
						Log.Warning(Main.plugin, "It is possible that not everything was reloaded, to ensure everything has reloaded, it is recommended to restart or reload the server.");
						return true;
					}catch(Exception e) {
						if(!(sender instanceof Player)) { 
							Log.Info(Main.plugin, "Reload Failed!");
						}else {
							sender.sendMessage(Name +" §4Reload Failed!\nCheck Console for more info.");
						}
						e.printStackTrace();
						return false;
					}
				}
				ItemStack bagItem = new ItemStack(Material.AIR);
				bagTexture = Main.config.GetString("bag-texture");
				if(args[0].equalsIgnoreCase("create") && sender.hasPermission("havenbags.create")) {
					if (args.length >= 2){
						if(args[1].equalsIgnoreCase("ownerless")) {
							if (args.length >= 3){
								int size = Utils.Clamp(Integer.parseInt(args[2]), 1, 6);
								if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
									bagItem = SkullCreator.itemFromBase64(bagTexture);
								} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
									bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
								} else {
									sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
									return false;
								}
								ItemMeta bagMeta = bagItem.getItemMeta();
								if(Main.config.GetInt("bag-custom-model-data") != 0) {
									bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
								}
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
								List<String> lore = new ArrayList<String>();
								for (String l : Lang.lang.GetStringList("bag-lore")) {
									if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, (Player)sender));
								}
								for (String l : Lang.lang.GetStringList("bag-size")) {
									if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), (Player)sender));
								}
								bagMeta.setLore(lore);
								bagItem.setItemMeta(bagMeta);
								NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
								NBT.SetString(bagItem, "bag-owner", "null");
								NBT.SetInt(bagItem, "bag-size", size*9);
								NBT.SetBool(bagItem, "bag-canBind", false);
								Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
								Log.Debug(Main.plugin, String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size*9, "false"));
								//sender.sendMessage(JsonUtils.toJson(bagItem));
							}else {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
							}
						}
						else {
							try{
								int size = Utils.Clamp(Integer.parseInt(args[1]), 1, 6);
								if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
									bagItem = SkullCreator.itemFromBase64(bagTexture);
								} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
									bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
								} else {
									sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
									return false;
								}
								ItemMeta bagMeta = bagItem.getItemMeta();
								if(Main.config.GetInt("bag-custom-model-data") != 0) {
									bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
								}
								bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
								List<String> lore = new ArrayList<String>();
								for (String l : Lang.lang.GetStringList("bag-lore")) {
									if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, (Player)sender));
								}
								for (String l : Lang.lang.GetStringList("bag-size")) {
									if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), (Player)sender));
								}
								bagMeta.setLore(lore);
								bagItem.setItemMeta(bagMeta);
								NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
								NBT.SetString(bagItem, "bag-owner", "null");
								NBT.SetInt(bagItem, "bag-size", size*9);
								NBT.SetBool(bagItem, "bag-canBind", true);
								Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
								Log.Debug(Main.plugin, String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
							}
							catch (NumberFormatException ex){
								ex.printStackTrace();
								sender.sendMessage(Lang.Get("prefix") + String.format(Lang.Get("number-conversion-error"), args[1]));
							}
						}
					}else {
						sender.sendMessage(Name + "§c /havenbags create <size>\n/havenbags create ownerless <size>");
					}
					return false;
				}
				
				if(args[0].equalsIgnoreCase("give") && sender.hasPermission("havenbags.give")) {
					if (args.length >= 3){
						Player receiver = Bukkit.getPlayer(args[1]);
						if(args[2].equalsIgnoreCase("ownerless")) {
							if (args.length >= 3){
								int size = Utils.Clamp(Integer.parseInt(args[3]), 1, 6);

									//String uuid = UUID.randomUUID().toString();
									//final Bag bag = new Bag(uuid, null, number*9, true);
									if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
										bagItem = SkullCreator.itemFromBase64(bagTexture);
									} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
										bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
									} else {
										sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
										return false;
									}
									ItemMeta bagMeta = bagItem.getItemMeta();
									if(Main.config.GetInt("bag-custom-model-data") != 0) {
										bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
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
									for (String l : Lang.lang.GetStringList("bag-size")) {
										if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), receiver));
									}
									bagMeta.setLore(lore);
									bagItem.setItemMeta(bagMeta);
									NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
									NBT.SetString(bagItem, "bag-owner", "null");
									NBT.SetInt(bagItem, "bag-size", size*9);
									NBT.SetBool(bagItem, "bag-canBind", false);
									receiver.getInventory().addItem(bagItem);
									receiver.sendMessage(String.format(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-ownerless-unused"))));
									Log.Debug(Main.plugin, String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size*9, "false"));
									//sender.sendMessage(JsonUtils.toJson(bagItem));
							}else {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
							}
						}
						else {
							try{
								int size = Utils.Clamp(Integer.parseInt(args[2]), 1, 6);
									//String uuid = UUID.randomUUID().toString();
									//final Bag bag = new Bag(uuid, null, number*9, true); //<-- Remove this & Bag.java
									if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
										bagItem = SkullCreator.itemFromBase64(bagTexture);
									} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
										bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
									} else {
										sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
										return false;
									}
									ItemMeta bagMeta = bagItem.getItemMeta();
									if(Main.config.GetInt("bag-custom-model-data") != 0) {
										bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
									}
									//bagMeta.setDisplayName("§aUnbound Bag");
									bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
									//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
									//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
									//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
									//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
									List<String> lore = new ArrayList<String>();
									for (String l : Lang.lang.GetStringList("bag-lore")) {
										if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l));
									}
									//lore.add(Lang.Get("bag-size", size*9));
									for (String l : Lang.lang.GetStringList("bag-size")) {
										if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9)));
									}
									bagMeta.setLore(lore);
									bagItem.setItemMeta(bagMeta);
									NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
									NBT.SetString(bagItem, "bag-owner", "null");
									NBT.SetInt(bagItem, "bag-size", size*9);
									NBT.SetBool(bagItem, "bag-canBind", true);
									//Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
									receiver.getInventory().addItem(bagItem);
									receiver.sendMessage(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-unbound-name")));
									//sender.sendMessage(JsonUtils.toJson(bagItem));
									Log.Debug(Main.plugin, String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
							}
							catch (NumberFormatException ex){
								ex.printStackTrace();
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("number-conversion-error", args[2]));
							}
						}
					}else {
						sender.sendMessage(Name + "§c /havenbags give <player> <size>\n/havenbags give <player> ownerless <size>");
					}
					return false;
				}
				
				if(args[0].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.restore")) {
					if (args.length >= 2){ // Player Name
						String owner = args[1];
						/*String puuid = args[1]; //Player username as fallback
						try {
							puuid = UUIDFetcher.getUUID(args[1]).toString();
						}catch(Exception e) {
							sender.sendMessage(Lang.Get("player-no-exist", args[1]));
						}*/
						String dirPath = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
						File dir = new File(dirPath);
						if(!dir.exists()) {
							//sender.sendMessage(Name + "§c Player '" + args[2] + "' has no bags.");
							sender.sendMessage(Lang.Get("player-no-bags", args[1]));
							return false;
						}
						if (args.length >= 3){ // Bag UUID
							String uuid = args[2];
							//String owner = args[1];
							String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, uuid);
							//plugin.getDataFolder() + "/bags/", args[2] + "/" + args[3] + ".json"
							File bagData;
							try {
								bagData = new File(path);
							} catch(Exception e) {
								sender.sendMessage(e.toString());
								e.printStackTrace();
								return false;
								//sender.sendMessage(Name + "§c Something went wrong! §fPlayer tell the owner this: '§eHavenBags:CommandListener:ProcessCommand():Restore§f'. Thank you! §4❤§r");
							}
							if(!bagData.exists()) {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-not-found"));
								return false;
							}
							String content = "";
							try {
								Path filePath = Path.of(path);
								content = Files.readString(filePath);
							} catch (IOException e) {
								sender.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
								e.printStackTrace();
							}
							List<ItemStack> contSize = new ArrayList<ItemStack>();
							contSize = JsonUtils.fromJson(content);
							//final Bag bag = new Bag(uuid, null, number, true);
							if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
								bagItem = SkullCreator.itemFromBase64(bagTexture);
							} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
								bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
							} else {
								sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
								return false;
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
							//bagMeta.setDisplayName("§aUnbound Bag");
							//Player owner = Bukkit.getPlayer(args[2]);
							//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
							
							
							//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", contSize.size(), PersistentDataType.INTEGER);
							if(owner.equalsIgnoreCase("ownerless")) {
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", owner, PersistentDataType.STRING);
								//bagMeta.setDisplayName("§aBag");
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-used"));
							}else {
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", Bukkit.getPlayer(owner).getUniqueId().toString(), PersistentDataType.STRING);
								//bagMeta.setDisplayName("§a" + owner + "'s Bag");
								try {
									bagMeta.setDisplayName(Lang.Get("bag-bound-name", Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()));
								} catch (Exception e) {
									bagMeta.setDisplayName(Lang.Get("bag-bound-name", UUIDFetcher.getName(UUID.fromString(owner))));
								}
							}
							//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "content", content, PersistentDataType.STRING);
	
							int a = 0;
								List<String> items = new ArrayList<String>();
								List<ItemStack> cont = new ArrayList<ItemStack>();
								for(int i = 0; i < contSize.size(); i++) {
									cont.add(contSize.get(i));
									if(contSize.get(i) != null) {
										if(contSize.get(i).getItemMeta().hasDisplayName()) {
											if(contSize.get(i).getAmount() != 1) {
												//items.add("§7" + contSize.get(i).getItemMeta().getDisplayName() + " §7x" + contSize.get(i).getAmount());
												items.add(Lang.Get("bag-content-item-amount", contSize.get(i).getItemMeta().getDisplayName(), contSize.get(i).getAmount()));
											} else {
												//items.add("§7" + contSize.get(i).getItemMeta().getDisplayName());
												items.add(Lang.Get("bag-content-item", contSize.get(i).getItemMeta().getDisplayName()));
											}
										}else {
											if(contSize.get(i).getAmount() != 1) {
												//items.add("§7" + FixMaterialName(contSize.get(i).getType().name()) + " §7x" + contSize.get(i).getAmount());
												items.add(Lang.Get("bag-content-item-amount", FixMaterialName(contSize.get(i).getType().name()), contSize.get(i).getAmount()));
											} else {
												//items.add("§7" + FixMaterialName(contSize.get(i).getType().name()));
												items.add(Lang.Get("bag-content-item", FixMaterialName(contSize.get(i).getType().name())));
											}
										}
										a++;
									}
								}
								List<String> lore = new ArrayList<String>();
								for (String l : Lang.lang.GetStringList("bag-lore")) {
									if(!Utils.IsStringNullOrEmpty(l)) {
										try {
											lore.add(Lang.Parse(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getPlayer()));
										} catch(Exception e) {
											lore.add(Lang.Parse(l));
										}
									}
								}
								if(NBT.GetBool(bagItem, "bag-canBind")) {
									//lore.add(Lang.Get("bound-to", owner));
									for (String l : Lang.lang.GetStringList("bound-to")) {
										if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getPlayer().getName())));
									}
								}
								//lore.add("§7Size: " + contSize.size());
								//lore.add(Lang.Get("bag-size", contSize.size()));
								for (String l : Lang.lang.GetStringList("bag-size")) {
									if(!Utils.IsStringNullOrEmpty(l)) {
										try {
											lore.add(Lang.Parse(String.format(l, contSize.size()), Bukkit.getOfflinePlayer(UUID.fromString(owner)).getPlayer()));
										} catch(Exception e) {
											lore.add(Lang.Parse(String.format(l, contSize.size())));
										}
									}
								}
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
							
							if(owner.equalsIgnoreCase("ownerless")) {
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-used"));
							}else {
								bagMeta.setDisplayName(Lang.Get("bag-bound-name", Bukkit.getOfflinePlayer(UUID.fromString(owner)).getPlayer().getName()));
							}
	
							bagItem.setItemMeta(bagMeta);
							Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
							Log.Debug(Main.plugin, String.format("%s restored bag: %s/%s size: %s", sender.getName(), owner, uuid, contSize.size()));
							//content = 
						}else {
							// No uuid
							//String owner = args[1];
							String path = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
							Set<String> files = listFilesUsingJavaIO(path);
							String fileString = Lang.Get("prefix") + Lang.Get("bags-of", owner);
							List<String> fileNames = new ArrayList<String>();
							fileNames.addAll(files);
							for(int i = 0; i < files.size(); i++) {
								String f = fileNames.get(i).replace(".json", "");
								fileString = fileString + "\n" + f;
							}
							sender.sendMessage(fileString);
						}
					}else {
						sender.sendMessage(Name + "§c /havenbags restore <player> <bag-uuid>");
					}
					return false;
				}
				
				if(args[0].equalsIgnoreCase("preview") && sender.hasPermission("havenbags.preview")) {
					if (args.length >= 2){ // Player Name
						String owner = args[1];
						/*String player;
						//String puuid = args[1]; //Player username as fallback
						try {
							player = UUIDFetcher.getName(UUID.fromString(owner)).toString();
						}catch(Exception e) {
							sender.sendMessage(Lang.Get("player-no-exist", args[1]));
						}*/
						String dirPath = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
						File dir = new File(dirPath);
						if(!dir.exists()) {
							//sender.sendMessage(Name + "§c Player '" + args[2] + "' has no bags.");
							sender.sendMessage(Lang.Get("player-no-bags", args[1]));
							return false;
						}
						if (args.length >= 3){ // Bag UUID
							String uuid = args[2];
							String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, uuid);
							//plugin.getDataFolder() + "/bags/", args[2] + "/" + args[3] + ".json"
							File bagData;
							try {
								bagData = new File(path);
							} catch(Exception e) {
								sender.sendMessage(e.toString());
								e.printStackTrace();
								return false;
								//sender.sendMessage(Name + "§c Something went wrong! §fPlayer tell the owner this: '§eHavenBags:CommandListener:ProcessCommand():Restore§f'. Thank you! §4❤§r");
							}
							if(!bagData.exists()) {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-not-found"));
								return false;
							}
							String content = "";
							try {
								Path filePath = Path.of(path);
								content = Files.readString(filePath);
							} catch (IOException e) {
								sender.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
								e.printStackTrace();
							}
							List<ItemStack> contSize = new ArrayList<ItemStack>();
							contSize = JsonUtils.fromJson(content);
							//final Bag bag = new Bag(uuid, null, number, true);
							if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
								bagItem = SkullCreator.itemFromBase64(bagTexture);
							} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
								bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
							} else {
								sender.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
								return false;
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
							//bagMeta.setDisplayName("§aUnbound Bag");
							//Player owner = Bukkit.getPlayer(args[2]);
							//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
							
							
							//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", contSize.size(), PersistentDataType.INTEGER);
							if(owner.equalsIgnoreCase("ownerless")) {
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", owner, PersistentDataType.STRING);
								//bagMeta.setDisplayName("§aBag");
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-used"));
							}else {
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", Bukkit.getPlayer(owner).getUniqueId().toString(), PersistentDataType.STRING);
								//bagMeta.setDisplayName("§a" + owner + "'s Bag");
								bagMeta.setDisplayName(Lang.Get("bag-bound-name", owner));
							}
							//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "content", content, PersistentDataType.STRING);
	
								List<String> items = new ArrayList<String>();
								List<ItemStack> cont = new ArrayList<ItemStack>();
								for(int i = 0; i < contSize.size(); i++) {
									cont.add(contSize.get(i));
									if(contSize.get(i) != null) {
										if(contSize.get(i).getItemMeta().hasDisplayName()) {
											if(contSize.get(i).getAmount() != 1) {
												items.add("§7" + contSize.get(i).getItemMeta().getDisplayName() + " §7x" + contSize.get(i).getAmount());
												items.add(Lang.Get("bag-content-item-amount", contSize.get(i).getItemMeta().getDisplayName(), contSize.get(i).getAmount()));
											} else {
												items.add("§7" + contSize.get(i).getItemMeta().getDisplayName());
												items.add(Lang.Get("bag-content-item", contSize.get(i).getItemMeta().getDisplayName()));
											}
										}else {
											if(contSize.get(i).getAmount() != 1) {
												items.add("§7" + FixMaterialName(contSize.get(i).getType().name()) + " §7x" + contSize.get(i).getAmount());
												items.add(Lang.Get("bag-content-item-amount", FixMaterialName(contSize.get(i).getType().name()), contSize.get(i).getAmount()));
											} else {
												//items.add("§7" + FixMaterialName(contSize.get(i).getType().name()));
												items.add(Lang.Get("bag-content-item", FixMaterialName(contSize.get(i).getType().name())));
											}
										}
									}
								}
							
							if(owner.equalsIgnoreCase("ownerless")) {
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-used"));
							}else {
								bagMeta.setDisplayName(Lang.Get("bag-bound-name", owner));
							}
							bagItem.setItemMeta(bagMeta);
	
							BagGUI gui = new BagGUI(Main.plugin, NBT.GetInt(bagItem, "bag-size"), Bukkit.getPlayer(sender.getName()), bagItem, bagItem.getItemMeta(), true);
							//Bukkit.getServer().getPluginManager().registerEvents(gui, plugin);
							gui.OpenInventory(Bukkit.getPlayer(sender.getName()));
							//HavenBags.activeBags.remove(gui);
							
							Log.Debug(Main.plugin, "Attempting to preview bag");
							Log.Debug(Main.plugin, String.format("%s previwing bag: %s/%s size: %s", sender.getName(), owner, uuid, contSize.size()));
							//content = 
						}else {
							// No uuid
							//String owner = args[1];
							String path = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
							Set<String> files = listFilesUsingJavaIO(path);
							String fileString = Lang.Get("prefix") + Lang.Get("bags-of", owner);
							List<String> fileNames = new ArrayList<String>();
							fileNames.addAll(files);
							for(int i = 0; i < files.size(); i++) {
								String f = fileNames.get(i).replace(".json", "");
								fileString = fileString + "\n" + f;
							}
							sender.sendMessage(fileString);
						}
					}else {
						sender.sendMessage(Name + "§c /havenbags restore <player> <bag-uuid>");
					}
					return false;
				}
				
				if(args[0].equalsIgnoreCase("rename") && sender.hasPermission("havenbags.rename")) {
					if (args.length >= 2){ // New Name
						ItemStack hand = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand();
						ItemMeta meta = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand().getItemMeta();
						//player.sendMessage("has meta: " + hand.hasItemMeta());
						if(meta == null) return false;
						
						if(NBT.Has(hand, "bag-uuid")) {
							String owner = NBT.GetString(hand, "bag-owner");
							if (sender.hasPermission("havenbags.bypass")) {
								//Continue.
							} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(sender.getName()).getUniqueId().toString())) {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
								return false;
							}
							String rename = "";
							for(int i = 1; i < args.length; i++) { rename = rename + " " + args[i]; }
							rename = rename.substring(1);
							meta.setDisplayName(Lang.Parse(rename));
							sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-rename", Lang.Parse(rename)));
							hand.setItemMeta(meta);
						} else {
							sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-rename"));
						}
					}else {
						ItemStack hand = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand();
						ItemMeta meta = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand().getItemMeta();
						//player.sendMessage("has meta: " + hand.hasItemMeta());
						if(meta == null) return false;
						
						if(NBT.Has(hand, "bag-uuid")) {
							String owner = NBT.GetString(hand, "bag-owner");
							if (sender.hasPermission("havenbags.bypass")) {
								//Continue.
							} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(sender.getName()).getUniqueId().toString())) {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
								return false;
							}
							
							UUID uuid = UUID.fromString(owner);
							try {
								meta.setDisplayName(Lang.Parse(Lang.Get("bag-bound-name"), Bukkit.getPlayer(uuid).getName()));
							} catch (Exception e) {
								meta.setDisplayName(Lang.Parse(Lang.Get("bag-bound-name"), UUIDFetcher.getName(uuid)));
							}
							hand.setItemMeta(meta);
							sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-rename-reset"));
						} else {
							sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-rename"));
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("info") && sender.hasPermission("havenbags.info")) {
					ItemStack hand = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand();
					ItemMeta meta = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand().getItemMeta();
						
					if(HavenBags.IsBag(hand)) {
						String uuid = NBT.GetString(hand, "bag-uuid");
						String owner = NBT.GetString(hand, "bag-owner");
						String creator = NBT.GetString(hand, "bag-creator");
						Boolean canBind = NBT.GetBool(hand, "bag-canBind");
						Integer size = NBT.GetInt(hand, "bag-size");
						List<String> lore = meta.getLore();
						
						String _lore = "";
						for(int i = 0; i < lore.size(); i++) { _lore = _lore + "\n    " + lore.get(i); }
						
						String info = "§6## HavenBag Bag Information ##";
						
						if(!Utils.IsStringNullOrEmpty(uuid)) { info = info + "\n  §fUUID: §e" + uuid; }
						if(!Utils.IsStringNullOrEmpty(owner)) { 
							if (!owner.equalsIgnoreCase("ownerless") && !owner.equalsIgnoreCase("null")) {
								info = info + String.format("\n  §fOwner: §e%s (%s)", owner, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()); 
							}else {
								info = info + "\n  §fOwner: §e" + owner;
							}
						}
						if(!Utils.IsStringNullOrEmpty(creator)) {
							info = info + String.format("\n  §fCreator: §e%s (%s)", creator, Bukkit.getOfflinePlayer(UUID.fromString(creator)).getName());
						}else {
							info = info + "\n  §fCreator: §enull";
						}
						if(canBind != null) { info = info + "\n  §fCanBind: §e" + canBind.toString(); }
						if(size != null) { info = info + "\n  §fSize: §e" + size.toString(); }
						if(lore != null) { info = info + "\n  §fLore:§r" + _lore; }
						
						sender.sendMessage(info);
						return true;
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("rawinfo") && sender.hasPermission("havenbags.info")) {
					ItemStack hand = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand();
					ItemMeta meta = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand().getItemMeta();
						
					if(HavenBags.IsBag(hand)) {
						String info = "§6## HavenBag Bag Raw Information ##";
						info = info + "\n  §f" + meta.toString();
						sender.sendMessage(info);
						return true;
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("gui") && sender.hasPermission("havenbags.gui")) {
					if (args.length == 1) {
						AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Main, (Player)sender);
						gui.OpenInventory((Player)sender);
						return true;
					}
					else if (args.length == 2){
						if(args[1].equalsIgnoreCase("create")) {
							AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Creation, (Player)sender);
							gui.OpenInventory((Player)sender);
							return true;
						}
						if(args[1].equalsIgnoreCase("restore")) {
							AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Restoration, (Player)sender);
							gui.OpenInventory((Player)sender);
							return true;
						}
					}
					else if (args.length >= 3) {
						if(args[1].equalsIgnoreCase("restore")) {
							OfflinePlayer target;
							try {
								target = Bukkit.getOfflinePlayer(UUIDFetcher.getUUID(args[2]));
							} catch(Exception e) {
								return true;
							}
							AdminGUI gui = new AdminGUI(AdminGUI.GUIType.Player, (Player)sender, target);
							gui.OpenInventory((Player)sender);
							return true;
						}
					}
					return false;
					
				}

				if(args[0].equalsIgnoreCase("empty") && sender.hasPermission("havenbags.empty")) {
					Player player = (Player)sender;
					ItemStack item = player.getInventory().getItemInMainHand();
					if(HavenBags.IsBag(item)) {
						if(HavenBags.IsOwner(item, player)) {
							HavenBags.EmptyBag(item, player);
						}else {
							player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
						}
					}
					return true;
				}

				if(args[0].equalsIgnoreCase("help")) {
					Player player = (Player)sender;
					List<String> help = new ArrayList<String>();
					help.add("&a&lHaven&b&lBags &8- &fHelp\n&7Optional: [] - Required: <>");
					help.add("");
					if(sender.hasPermission("havenbags.rename") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags rename");
						help.add("&7&o Rename the bag in your hand");
						help.add("&7&o You cannot rename any bags you aren't bound to");
						help.add("&7&o (Supports Hex. Leave value empty to reset.)");
					}
					if(sender.hasPermission("havenbags.empty") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags empty");
						help.add("&7&o Empty the content of the bag in your hand, onto the ground");
					}
					if(sender.hasPermission("havenbags.gui") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags gui");
						help.add("&7&o Opens Admin GUI");
					}
					if(sender.hasPermission("havenbags.create") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags create [ownerless] <size>");
						help.add("&7&o Create a new bag");
						help.add("&8 (Also in GUI)");
					}
					if(sender.hasPermission("havenbags.give") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags give <player> [ownerless] <size>");
						help.add("&7&o Give player a bag");
					}
					if(sender.hasPermission("havenbags.restore") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags restore <player>");
						help.add("&7&o Shows a list of bags by that player");
						help.add("&8 (Also in GUI)");
						help.add("&e/bags restore <player> <bag-uuid>");
						help.add("&7&o Gives a copy of the bag stored on the server");
						help.add("&8 (Also in GUI)");
					}
					if(sender.hasPermission("havenbags.preview") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags preview <player>");
						help.add("&7&o Shows a list of bags by that player");
						help.add("&8 (Also in GUI)");
						help.add("&e/bags preview <player> <bag-uuid>");
						help.add("&7&o Preview a copy of the bag stored on the server");
						help.add("&8 (Also in GUI)");
					}
					if(sender.hasPermission("havenbags.info") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags info");
						help.add("&7&o Shows information about the bag you're currently holding");
						help.add("&e/bags rawinfo");
						help.add("&7&o Shows raw metadata about the bag you're currently holding");
					}
					if(sender.hasPermission("havenbags.reload") || sender.hasPermission("havenbags.help")) {
						help.add("&e/bags reload");
						help.add("&7&o Reloads config files");
					}
					help.add("&e/bags help");
					help.add("&7&o You are here");
					
					String helpString = "";
					for(String i : help) {
						helpString = helpString + Lang.Parse(i, player) + "\n ";
					}
					sender.sendMessage(helpString);
					return true;
				}
			} catch(Exception e) {
				sender.sendMessage(Lang.Get("prefix") + Lang.Get("malformed-command"));
				Log.Error(Main.plugin, e.getMessage());
				//Log.Error(Main.plugin, e.printStackTrace());
				e.printStackTrace();
			}
		}
		sender.sendMessage("Unknown command.");
        return false;
    }
	
	String FixMaterialName(String string) {
    	string = string.replace('_', ' ');
        char[] charArray = string.toCharArray();
        boolean foundSpace = true;
        for(int i = 0; i < charArray.length; i++) {
        	charArray[i] = Character.toLowerCase(charArray[i]);
        	if(Character.isLetter(charArray[i])) {
        		if(foundSpace) {
        			charArray[i] = Character.toUpperCase(charArray[i]);
        			foundSpace = false;
        		}
        	}
        	else {
        		foundSpace = true;
        	}
        }
        string = String.valueOf(charArray);
    	return string;
    }
	
	public Set<String> listFilesUsingJavaIO(String dir) {
	    return Stream.of(new File(dir).listFiles())
	      .filter(file -> !file.isDirectory())
	      .map(File::getName)
	      .collect(Collectors.toSet());
	}

}
