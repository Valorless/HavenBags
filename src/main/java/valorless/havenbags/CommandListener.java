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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandListener implements CommandExecutor {
	
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Log.Debug(plugin, "Sender: " + sender.getName());
    	Log.Debug(plugin, "Command: " + command.toString());
    	Log.Debug(plugin, "Label: " + label);
    	for(String a : args) {
    		Log.Debug(plugin, "Argument: " + a);
    	}
    	
    	bagTexture = HavenBags.config.GetString("bag-texture");
		if(args.length == 0) {
			sender.sendMessage(Name + " HavenBags by Valorless.");
		}
		else 
		if (args.length >= 1){
			if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("havenbags.reload")) {
				HavenBags.config.Reload();
				Lang.lang.Reload();
				sender.sendMessage(Name +" §aReloaded.");
				if(!(sender instanceof Player)) { Log.Info(plugin, " §aReloaded!"); }
			}
			if(args[0].equalsIgnoreCase("create") && sender.hasPermission("havenbags.create")) {
				if (args.length >= 2){
					String[] allowedSizes = {"1","2","3","4","5","6"};
					if(args[1].equalsIgnoreCase("ownerless")) {
						if (args.length >= 3){
							boolean ok = false;
							for(String size : allowedSizes) {
								if(args[2].equalsIgnoreCase(size)) { ok = true; }
							}
							if(ok) {
								int size = Integer.parseInt(args[2]);
								//String uuid = UUID.randomUUID().toString();
								//final Bag bag = new Bag(uuid, null, number*9, true);
								ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
								SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
								//bagMeta.setDisplayName("§aUnused Bag");
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
								List<String> lore = new ArrayList<String>();
						        lore.add(Lang.Get("bag-size", size*9));
								bagMeta.setLore(lore);
								bagItem.setItemMeta(bagMeta);
								NBT.SetString(bagItem, "bag-uuid", "null");
								NBT.SetString(bagItem, "bag-owner", "null");
								NBT.SetInt(bagItem, "bag-size", size*9);
								NBT.SetBool(bagItem, "bag-canBind", false);
								Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
								Log.Debug(plugin, String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size*9, "false"));
								//sender.sendMessage(JsonUtils.toJson(bagItem));
							}
							else {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-size-error"));
							}
						}else {
							sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
						}
					}
					else {
						try{
							boolean ok = false;
							for(String size : allowedSizes) {
								if(args[1].equalsIgnoreCase(size)) { ok = true; }
							}
							if(ok) {
								int size = Integer.parseInt(args[1]);
								//String uuid = UUID.randomUUID().toString();
								//final Bag bag = new Bag(uuid, null, number*9, true); //<-- Remove this & Bag.java
								ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
								SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
								//bagMeta.setDisplayName("§aUnbound Bag");
								bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
								List<String> lore = new ArrayList<String>();
						        lore.add(Lang.Get("bag-size", size*9));
								bagMeta.setLore(lore);
								bagItem.setItemMeta(bagMeta);
								NBT.SetString(bagItem, "bag-uuid", "null");
								NBT.SetString(bagItem, "bag-owner", "null");
								NBT.SetInt(bagItem, "bag-size", size*9);
								NBT.SetBool(bagItem, "bag-canBind", true);
								Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
								//sender.sendMessage(JsonUtils.toJson(bagItem));
								Log.Debug(plugin, String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
							}
							else {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-size-error"));
							}
		        		}
		        		catch (NumberFormatException ex){
		        			ex.printStackTrace();
		        			sender.sendMessage(Lang.Get("prefix") + String.format(Lang.Get("number-conversion-error"), args[1]));
		        		}
					}
				}else {
					sender.sendMessage(Name + "§c /havenbags create <size>\n/havenbags create ownerless <size>");
				}
			}
			
			if(args[0].equalsIgnoreCase("give") && sender.hasPermission("havenbags.give")) {
				if (args.length >= 3){
					Player receiver = Bukkit.getPlayer(args[1]);
					String[] allowedSizes = {"1","2","3","4","5","6"};
					if(args[2].equalsIgnoreCase("ownerless")) {
						if (args.length >= 3){
							boolean ok = false;
							for(String size : allowedSizes) {
								if(args[3].equalsIgnoreCase(size)) { ok = true; }
							}
							if(ok) {
								int size = Integer.parseInt(args[3]);
								//String uuid = UUID.randomUUID().toString();
								//final Bag bag = new Bag(uuid, null, number*9, true);
								ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
								SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
								//bagMeta.setDisplayName("§aUnused Bag");
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
								List<String> lore = new ArrayList<String>();
						        lore.add(Lang.Get("bag-size", size*9));
								bagMeta.setLore(lore);
								bagItem.setItemMeta(bagMeta);
								NBT.SetString(bagItem, "bag-uuid", "null");
								NBT.SetString(bagItem, "bag-owner", "null");
								NBT.SetInt(bagItem, "bag-size", size*9);
								NBT.SetBool(bagItem, "bag-canBind", false);
								receiver.getInventory().addItem(bagItem);
								receiver.sendMessage(String.format(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-ownerless-unused"))));
								Log.Debug(plugin, String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size*9, "false"));
								//sender.sendMessage(JsonUtils.toJson(bagItem));
							}
							else {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-size-error"));
							}
						}else {
							sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
						}
					}
					else {
						try{
							boolean ok = false;
							for(String size : allowedSizes) {
								if(args[2].equalsIgnoreCase(size)) { ok = true; }
							}
							if(ok) {
								int size = Integer.parseInt(args[2]);
								//String uuid = UUID.randomUUID().toString();
								//final Bag bag = new Bag(uuid, null, number*9, true); //<-- Remove this & Bag.java
								ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
								SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
								//bagMeta.setDisplayName("§aUnbound Bag");
								bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
								//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
								List<String> lore = new ArrayList<String>();
						        lore.add(Lang.Get("bag-size", size*9));
								bagMeta.setLore(lore);
								bagItem.setItemMeta(bagMeta);
								NBT.SetString(bagItem, "bag-uuid", "null");
								NBT.SetString(bagItem, "bag-owner", "null");
								NBT.SetInt(bagItem, "bag-size", size*9);
								NBT.SetBool(bagItem, "bag-canBind", true);
								//Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
								receiver.getInventory().addItem(bagItem);
								receiver.sendMessage(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-unbound-name")));
								//sender.sendMessage(JsonUtils.toJson(bagItem));
								Log.Debug(plugin, String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
							}
							else {
								sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-size-error"));
							}
		        		}
		        		catch (NumberFormatException ex){
		        			ex.printStackTrace();
		        			sender.sendMessage(Lang.Get("prefix") + Lang.Get("number-conversion-error", args[2]));
		        		}
					}
				}else {
					sender.sendMessage(Name + "§c /havenbags give <player> <size>\n/havenbags give <player> ownerless <size>");
				}
			}
			
			if(args[0].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.restore")) {
				if (args.length >= 2){ // Player Name
					String dirPath = String.format("%s/bags/%s/", plugin.getDataFolder(), args[1]);
					File dir = new File(dirPath);
					if(!dir.exists()) {
			        	//sender.sendMessage(Name + "§c Player '" + args[2] + "' has no bags.");
			        	sender.sendMessage(Lang.Get("player-no-bags", args[1]));
			        	return false;
			        }
					if (args.length >= 3){ // Bag UUID
						String uuid = args[2];
						String owner = args[1];
						String path = String.format("%s/bags/%s/%s.json", plugin.getDataFolder(), owner, uuid);
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
						ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);NBT.SetString(bagItem, "bag-uuid", uuid);
						NBT.SetInt(bagItem, "bag-size", contSize.size());
						if(owner.equalsIgnoreCase("ownerless")) {
							NBT.SetString(bagItem, "bag-owner", owner);
							NBT.SetBool(bagItem, "bag-canBind", false);
						}else {
							NBT.SetString(bagItem, "bag-owner", UUIDFetcher.getUUID(owner).toString());
							NBT.SetBool(bagItem, "bag-canBind", true);
						}
						SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
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
					        if(NBT.GetBool(bagItem, "bag-canBind")) {
					        	lore.add(Lang.Get("bound-to", owner));
					        }
					        //lore.add("§7Size: " + contSize.size());
					        lore.add(Lang.Get("bag-size", contSize.size()));
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
							bagMeta.setDisplayName(Lang.Get("bag-bound-name", owner));
						}

						bagItem.setItemMeta(bagMeta);
						Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
						Log.Debug(plugin, String.format("%s restored bag: %s/%s size: %s", sender.getName(), owner, uuid, contSize.size()));
						//content = 
					}else {
						// No uuid
						String owner = args[1];
						String path = String.format("%s/bags/%s/", plugin.getDataFolder(), owner);
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
			}
			
			if(args[0].equalsIgnoreCase("preview") && sender.hasPermission("havenbags.preview")) {
				if (args.length >= 2){ // Player Name
					String dirPath = String.format("%s/bags/%s/", plugin.getDataFolder(), args[1]);
					File dir = new File(dirPath);
					if(!dir.exists()) {
			        	//sender.sendMessage(Name + "§c Player '" + args[2] + "' has no bags.");
			        	sender.sendMessage(Lang.Get("player-no-bags", args[1]));
			        	return false;
			        }
					if (args.length >= 3){ // Bag UUID
						String uuid = args[2];
						String owner = args[1];
						String path = String.format("%s/bags/%s/%s.json", plugin.getDataFolder(), owner, uuid);
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
						ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
						NBT.SetString(bagItem, "bag-uuid", uuid);
						NBT.SetInt(bagItem, "bag-size", contSize.size());
						if(owner.equalsIgnoreCase("ownerless")) {
							NBT.SetString(bagItem, "bag-owner", owner);
							NBT.SetBool(bagItem, "bag-canBind", false);
						}else {
							NBT.SetString(bagItem, "bag-owner", UUIDFetcher.getUUID(owner).toString());
							NBT.SetBool(bagItem, "bag-canBind", true);
						}
						SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
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

						BagGUI gui = new BagGUI(plugin, NBT.GetInt(bagItem, "bag-size"), Bukkit.getPlayer(sender.getName()), bagItem, (SkullMeta)bagItem.getItemMeta());
						//Bukkit.getServer().getPluginManager().registerEvents(gui, plugin);
						gui.OpenInventory(Bukkit.getPlayer(sender.getName()));
				    	//HavenBags.activeBags.remove(gui);
						
		    			Log.Debug(plugin, "Attempting to preview bag");
						Log.Debug(plugin, String.format("%s previwing bag: %s/%s size: %s", sender.getName(), owner, uuid, contSize.size()));
						//content = 
					}else {
						// No uuid
						String owner = args[1];
						String path = String.format("%s/bags/%s/", plugin.getDataFolder(), owner);
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
			}
			
			if(args[0].equalsIgnoreCase("rename") && sender.hasPermission("havenbags.rename")) {
				if (args.length >= 2){ // New Name
	    			ItemStack hand = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand();
	    			ItemMeta meta = Bukkit.getPlayer(sender.getName()).getInventory().getItemInMainHand().getItemMeta();
	    			//player.sendMessage("has meta: " + hand.hasItemMeta());
	    			if(meta == null) return false;
	    			
	    			if(NBT.Has(hand, "bag-uuid")) {
	    				String owner = NBT.GetString(hand, "bag-owner");
	    				if (!owner.equalsIgnoreCase(Bukkit.getPlayer(sender.getName()).getUniqueId().toString())) {
    						sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
	    					return false;
	    				} else if (sender.hasPermission("havenbags.bypass")) {
	    					//Continue.
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
	    				if (!owner.equalsIgnoreCase(Bukkit.getPlayer(sender.getName()).getUniqueId().toString())) {
    						sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
	    					return false;
	    				} else if (sender.hasPermission("havenbags.bypass")) {
	    					//Continue.
	    				}
	    				
	    		    	UUID uuid = UUID.fromString(owner);
	    		    	try {
	    		    		meta.setDisplayName(Lang.Get("bag-bound-name", Bukkit.getPlayer(uuid).getName()));
	    		    	} catch (Exception e) {
	    		    		meta.setDisplayName(Lang.Get("bag-bound-name", UUIDFetcher.getName(uuid)));
	    		    	}
	    				hand.setItemMeta(meta);
	    				sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-rename-reset"));
	    			} else {
	    				sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-rename"));
	    			}
				}
			}
		}
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
