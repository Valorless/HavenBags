package valorless.havenbags;

import valorless.valorlessutils.ValorlessUtils.*;
import valorless.valorlessutils.skulls.*;
import valorless.valorlessutils.json.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class CommandListener implements Listener {
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
	
	public static void onEnable() {
	}
	
	@EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String[] args = event.getMessage().split("\\s+");
		//Player sender = event.getPlayer();
		CommandSender sender = event.getPlayer();
		ProcessCommand(args, sender, false);
	}
	
	@EventHandler
    public void onServerCommand(ServerCommandEvent event) {
		String[] args = event.getCommand().split("\\s+");
		CommandSender console = event.getSender();
		args[0] = "/" + args[0];
		ProcessCommand(args, console, true);
		
	}
	
	public void ProcessCommand(String[] args, CommandSender sender, Boolean console) {
		if(args[0].equalsIgnoreCase("/havenbags") || args[0].equalsIgnoreCase("/bags")) {
			bagTexture = HavenBags.config.GetString("bag-texture");
			if(args.length == 1) {
				sender.sendMessage(Name + " HavenBags by Valorless.");
			}
			else 
			if (args.length >= 2){
				if(args[1].equalsIgnoreCase("reload") && sender.hasPermission("havenbags.reload")) {
					HavenBags.config.Reload();
					Lang.lang.Reload();
					sender.sendMessage(Name +" §aReloaded.");
					if(!console) { Log.Info(plugin, " §aReloaded!"); }
				}
				if(args[1].equalsIgnoreCase("create") && sender.hasPermission("havenbags.create")) {
					if (args.length >= 3){
						String[] allowedSizes = {"9","18","27","36","45","54"};
						if(args[2].equalsIgnoreCase("ownerless")) {
							if (args.length >= 4){
								boolean ok = false;
								for(String size : allowedSizes) {
									if(args[3].equalsIgnoreCase(size)) { ok = true; }
								}
								if(ok) {
									int number = Integer.parseInt(args[3]);
									String uuid = UUID.randomUUID().toString();
									final Bag bag = new Bag(uuid, null, number, true);
									ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
									SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
									//bagMeta.setDisplayName("§aUnused Bag");
									bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
									List<String> lore = new ArrayList<String>();
							        lore.add(Lang.Get("bag-size", bag.size));
									bagMeta.setLore(lore);
									bagItem.setItemMeta(bagMeta);
									Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
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
									int number = Integer.parseInt(args[2]);
									String uuid = UUID.randomUUID().toString();
									final Bag bag = new Bag(uuid, null, number, true); //<-- Remove this & Bag.java
									ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
									SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
									//bagMeta.setDisplayName("§aUnbound Bag");
									bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
									List<String> lore = new ArrayList<String>();
							        lore.add(Lang.Get("bag-size", bag.size));
									bagMeta.setLore(lore);
									bagItem.setItemMeta(bagMeta);
									Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
									//sender.sendMessage(JsonUtils.toJson(bagItem));
								}
								else {
									sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
								}
			        		}
			        		catch (NumberFormatException ex){
			        			ex.printStackTrace();
			        			sender.sendMessage(Lang.Get("prefix") + String.format(Lang.Get("number-conversion-error"), args[2]));
			        		}
						}
					}else {
						sender.sendMessage(Name + "§c /havenbags create <size>\n/havenbags create ownerless <size>");
					}
				}
				
				if(args[1].equalsIgnoreCase("give") && sender.hasPermission("havenbags.create")) {
					if (args.length >= 4){
						Player receiver = Bukkit.getPlayer(args[2]);
						String[] allowedSizes = {"9","18","27","36","45","54"};
						if(args[3].equalsIgnoreCase("ownerless")) {
							if (args.length >= 4){
								boolean ok = false;
								for(String size : allowedSizes) {
									if(args[3].equalsIgnoreCase(size)) { ok = true; }
								}
								if(ok) {
									int number = Integer.parseInt(args[3]);
									String uuid = UUID.randomUUID().toString();
									final Bag bag = new Bag(uuid, null, number, true);
									ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
									SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
									//bagMeta.setDisplayName("§aUnused Bag");
									bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
									List<String> lore = new ArrayList<String>();
							        lore.add(Lang.Get("bag-size", bag.size));
									bagMeta.setLore(lore);
									bagItem.setItemMeta(bagMeta);
									receiver.getInventory().addItem(bagItem);
									receiver.sendMessage(String.format(Lang.Get("bag-given", Lang.Get("bag-ownerless-unused"))));
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
									if(args[3].equalsIgnoreCase(size)) { ok = true; }
								}
								if(ok) {
									int number = Integer.parseInt(args[3]);
									String uuid = UUID.randomUUID().toString();
									final Bag bag = new Bag(uuid, null, number, true); //<-- Remove this & Bag.java
									ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
									SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
									//bagMeta.setDisplayName("§aUnbound Bag");
									bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", "null", PersistentDataType.STRING);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", bag.size, PersistentDataType.INTEGER);
									Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
									List<String> lore = new ArrayList<String>();
							        lore.add(Lang.Get("bag-size", bag.size));
									bagMeta.setLore(lore);
									bagItem.setItemMeta(bagMeta);
									//Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
									receiver.getInventory().addItem(bagItem);
									receiver.sendMessage(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-unbound-name")));
									//sender.sendMessage(JsonUtils.toJson(bagItem));
								}
								else {
									sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-size-error"));
								}
			        		}
			        		catch (NumberFormatException ex){
			        			ex.printStackTrace();
			        			sender.sendMessage(Lang.Get("prefix") + Lang.Get("number-conversion-error", args[3]));
			        		}
						}
					}else {
						sender.sendMessage(Name + "§c /havenbags give <player> <size>\n/havenbags give <player> ownerless <size>");
					}
				}
				
				if(args[1].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.restore")) {
					if (args.length >= 3){ // Player Name
						String dirPath = String.format("%s/bags/%s/", plugin.getDataFolder(), args[2]);
						File dir = new File(dirPath);
						if(!dir.exists()) {
				        	//sender.sendMessage(Name + "§c Player '" + args[2] + "' has no bags.");
				        	sender.sendMessage(Lang.Get("player-no-bags", args[2]));
				        	return;
				        }
						if (args.length >= 4){ // Bag UUID
							String uuid = args[3];
							String owner = args[2];
							String path = String.format("%s/bags/%s/%s.json", plugin.getDataFolder(), owner, uuid);
							//plugin.getDataFolder() + "/bags/", args[2] + "/" + args[3] + ".json"
							File bagData;
							try {
								bagData = new File(path);
							} catch(Exception e) {
								sender.sendMessage(e.toString());
								e.printStackTrace();
								return;
								//sender.sendMessage(Name + "§c Something went wrong! §fPlayer tell the owner this: '§eHavenBags:CommandListener:ProcessCommand():Restore§f'. Thank you! §4❤§r");
							}
					        if(!bagData.exists()) {
					        	sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-not-found"));
					        	return;
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
							SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
							//bagMeta.setDisplayName("§aUnbound Bag");
							//Player owner = Bukkit.getPlayer(args[2]);
							Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "uuid", uuid, PersistentDataType.STRING);
							
							
							Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "size", contSize.size(), PersistentDataType.INTEGER);
							if(owner.equalsIgnoreCase("ownerless")) {
								Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "false", PersistentDataType.STRING);
								Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", owner, PersistentDataType.STRING);
								//bagMeta.setDisplayName("§aBag");
								bagMeta.setDisplayName(Lang.Get("bag-ownerless-used"));
							}else {
								Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "canBind", "true", PersistentDataType.STRING);
								Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "owner", Bukkit.getPlayer(owner).getUniqueId().toString(), PersistentDataType.STRING);
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
						    			a++;
						    		}
						    	}
						        List<String> lore = new ArrayList<String>();
						        if(Tags.Get(HavenBags.plugin, bagMeta.getPersistentDataContainer(), "canbind", PersistentDataType.STRING).toString() != "false") {
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
							
							bagItem.setItemMeta(bagMeta);
							Bukkit.getPlayer(sender.getName()).getInventory().addItem(bagItem);
							//content = 
						}else {
							// No uuid
							String owner = args[2];
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
			}
		}
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
