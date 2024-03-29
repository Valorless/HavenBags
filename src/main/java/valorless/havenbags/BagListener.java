package valorless.havenbags;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.sound.SFX;

public class BagListener implements Listener{
	String Name = "§7[§aHaven§bBags§7]§r";

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
    		Player player = event.getPlayer();
    		
    		// Since 1.20 players can now edit signs. Block bag interaction if a sign is clicked.
    		if(getTargetBlock(player, 5).getType().toString().contains("SIGN")) { 
    			return; 
    		}
    		
    		//player.sendMessage("Right click");
    		
    		
    		if(!player.hasPermission("havenbags.use")) {
    			return;
    		}else {
    			ItemStack hand = player.getInventory().getItemInMainHand();
    			ItemMeta item = player.getInventory().getItemInMainHand().getItemMeta();
    			//player.sendMessage("has meta: " + hand.hasItemMeta());
    			if(item == null) {
    				// This requires more testing..
    				//hand = player.getInventory().getItemInOffHand();
    				//item = player.getInventory().getItemInOffHand().getItemMeta();
    				//if(item == null) {
    					return;
    				//}
    			}
    			
    			//if(Tags.Get(plugin, item.getPersistentDataContainer(), "uuid", PersistentDataType.STRING) != null || NBT.Has(hand, "bag-uuid")) {
        		if(HavenBags.IsBag(hand)) {
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
        			List<String> blacklist = Main.config.GetStringList("blacklist");
            		if(blacklist != null) {
            			if(blacklist.size() != 0) {
        					Log.Debug(Main.plugin, "Player World: " + player.getWorld().getName());
            				for(String world : blacklist) {
            					Log.Debug(Main.plugin, "Blacklist: " + world);
            					if(player.getWorld().getName().equalsIgnoreCase(world)) return;
            				}
            			}
            		}
            		
        			Log.Debug(Main.plugin, player.getName() + " is attempting to open a bag");
    				//player.sendMessage("has uuid: true");
    				//String owner = Tags.Get(plugin, item.getPersistentDataContainer(), "owner", PersistentDataType.STRING).toString();
    				String uuid = NBT.GetString(hand, "bag-uuid");
    				String owner = NBT.GetString(hand, "bag-owner");
    				//String canbind = Tags.Get(plugin, item.getPersistentDataContainer(), "canbind", PersistentDataType.STRING).toString();
    				boolean canbind = NBT.GetBool(hand, "bag-canBind");
    				//player.sendMessage(owner);
    				//player.sendMessage(canbind);
    				
					// If create ownerless
    				if(owner.equalsIgnoreCase("null") && !canbind)
    				{
    					item.setDisplayName(Lang.Get("bag-ownerless-used"));
    	    			List<String> lore = new ArrayList<String>() ;
						//lore.add(Lang.Get("bag-size", Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER)));
						//lore.add(Lang.Get("bag-size", NBT.GetInt(hand, "bag-size")));
    	    	        for (String l : Lang.lang.GetStringList("bag-lore")) {
    	    	        	lore.add(Lang.Parse(l, player));
    	    	        }
    	    	        if(NBT.Has(hand, "bag-size")) {
			            	placeholders.add(new Placeholder("%size%", NBT.GetInt(hand, "bag-size")));
			            	lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
			            	//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
			            }
    					item.setLore(lore);
    					//Tags.Set(plugin, item.getPersistentDataContainer(), "owner", "ownerless", PersistentDataType.STRING);
    					hand.setItemMeta(item);
    					NBT.SetString(hand, "bag-owner", "ownerless");
    					NBT.SetString(hand, "bag-creator", player.getUniqueId().toString());
						//NBT.SetString(hand, "bag-uuid", UUID.randomUUID().toString());
    					//WriteToServer(player, item, (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER));
    					WriteToServer(player, hand, NBT.GetInt(hand, "bag-size"));
    	    			Log.Debug(Main.plugin, "Ownerless bag created.");
    	    			Log.Debug(Main.plugin, "Creating timestamp for " + uuid);
    	    	    	//Main.timeTable.Set(
    	    	    	//	String.format("%s/%s", "ownerless", uuid),
    	    	    	//		Long.toString(System.currentTimeMillis() / 1000L));
    	    	    	//Main.timeTable.SaveConfig();
    					return;
    				}
				
					// If unbound
    				if(owner.equalsIgnoreCase("null") && canbind)
    				{
    					//item.setDisplayName("§a" + player.getName() +"'s Bag");
    					
    					item.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-bound-name"), player));
    					List<String> lore = new ArrayList<String>();
    			        for (String l : Lang.lang.GetStringList("bag-lore")) {
    			        	lore.add(Lang.Parse(l, player));
    			        }
    					//lore.add("§7Bound to " + player.getName());
    					//lore.add(Lang.Get("bound-to", player.getName()));
    		            placeholders.add(new Placeholder("%owner%", player.getName()));
    		            lore.add(Lang.Parse(Lang.Get("bound-to"), placeholders, player));
    		            //if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()), player));
    		            
			            //for (String l : Lang.lang.GetStringList("bound-to")) {
			            //	lore.add(Lang.Parse(String.format(l, player.getName()), player));
			            //}
						//lore.add("§7Size: " + Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER));
						//lore.add(Lang.Get("bag-size", Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER)));
						//lore.add(Lang.Get("bag-size", NBT.GetInt(hand, "bag-size")));
			            if(NBT.Has(hand, "bag-size")) {
			            	placeholders.add(new Placeholder("%size%", NBT.GetInt(hand, "bag-size")));
			            	lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
			            	//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
			            }
			            //for (String l : Lang.lang.GetStringList("bag-size")) {
			            //	lore.add(Lang.Parse(String.format(l, NBT.GetInt(hand, "bag-size")), player));
			            //}
    					item.setLore(lore);
    					//List<ItemStack> content = new ArrayList<ItemStack>();
    					//for(int i = 0; i < (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER); i++) {
    					//	content.add(new ItemStack(Material.AIR));
    					//}
    					//Tags.Set(plugin, item.getPersistentDataContainer(), "owner", player.getUniqueId().toString(), PersistentDataType.STRING);
    					//Tags.Set(plugin, item.getPersistentDataContainer(), "content", JsonUtils.toJson(content), PersistentDataType.STRING);
    					hand.setItemMeta(item);
    					NBT.SetString(hand, "bag-owner", player.getUniqueId().toString());
    					NBT.SetString(hand, "bag-creator", player.getUniqueId().toString());
						//NBT.SetString(hand, "bag-uuid", UUID.randomUUID().toString());
    					WriteToServer(player, hand, NBT.GetInt(hand, "bag-size"));
    	    			Log.Debug(Main.plugin, "Bound new bag to: " + player.getName());
    	    			Log.Debug(Main.plugin, "Creating timestamp for " + uuid);
    	    	    	//Main.timeTable.Set(
    	    	    	//	String.format("%s/%s", player.getUniqueId().toString(), uuid),
    	    	    	//		System.currentTimeMillis() / 1000L);
    	    	    	//Main.timeTable.SaveConfig();
    					return;
    				}
    				

    				
				
    				if(!canbind) {
    					//player.sendMessage("Ownerless Bag");
    					BagGUI gui = new BagGUI(Main.plugin, NBT.GetInt(hand, "bag-size"), player, hand, hand.getItemMeta());
    					Bukkit.getServer().getPluginManager().registerEvents(gui, Main.plugin);
    			    	player.getInventory().remove(hand);
    					gui.OpenInventory(player);
    					SFX.Play(Main.config.GetString("open-sound"), 
    							Main.config.GetFloat("open-volume").floatValue(), 
    							Main.config.GetFloat("open-pitch").floatValue(), player);
    	    			Log.Debug(Main.plugin, "Attempting to open ownerless bag");
    					return;
    				}
    				if(canbind) {
    					//player.sendMessage("Bound Bag");
    					if(owner.equalsIgnoreCase(player.getUniqueId().toString())) {
    						BagGUI gui = new BagGUI(Main.plugin, NBT.GetInt(hand, "bag-size"), player, hand, hand.getItemMeta());
    						Bukkit.getServer().getPluginManager().registerEvents(gui, Main.plugin);
        			    	player.getInventory().remove(hand);
    						gui.OpenInventory(player);
        					SFX.Play(Main.config.GetString("open-sound"), 
        							Main.config.GetFloat("open-volume").floatValue(), 
        							Main.config.GetFloat("open-pitch").floatValue(), player);
    		    			Log.Debug(Main.plugin, "Attempting to open bag");
    						return;
    					} else if (player.hasPermission("havenbags.bypass")) {
    						BagGUI gui = new BagGUI(Main.plugin, NBT.GetInt(hand, "bag-size"), player, hand, hand.getItemMeta());
    						Bukkit.getServer().getPluginManager().registerEvents(gui, Main.plugin);
        			    	player.getInventory().remove(hand);
    						gui.OpenInventory(player);
        					SFX.Play(Main.config.GetString("open-sound"), 
        							Main.config.GetFloat("open-volume").floatValue(), 
        							Main.config.GetFloat("open-pitch").floatValue(), player);
    		    			Log.Debug(Main.plugin, player + "has attempted to open a bag, bypassing the lock");
    						return;
    					}
    					else {
    						//player.sendMessage(Name + "§c You cannot use this bag.");
    						player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
    						return;
    					}
    				}
    				//player.sendMessage(Name + "§f Something went wrong");
    				player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagListener:onPlayerInteract()§f'. \nThank you! §4❤§r");
					
				}
    		}
    	}
    }
    
    /*void WriteToServer(Player player, ItemMeta bagMeta, int size) {
    	String uuid = Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "uuid", PersistentDataType.STRING).toString();
    	String owner = Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING).toString();
    	Log.Debug(plugin, "Attempting to write bag " + owner + "/" + uuid + " onto server");
    	if(owner != "ownerless") {
    		player = Bukkit.getPlayer(UUID.fromString(owner));
    	}
    	File bagData;
    	List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < size; i++) {
    		cont.add(null);
    	}
    	if(owner != "ownerless") {
    		bagData = new File(plugin.getDataFolder() + "/bags/", player.getName() + "/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Info(plugin, String.format("Bag data for (%s) %s does not exist, creating new.", player.getName(), uuid));
            }
    	}else {
    		bagData = new File(plugin.getDataFolder() + "/bags/ownerless/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Info(plugin, String.format("Bag data for (ownerless) %s does not exist, creating new.", uuid));
            }
    	}
        
        FileWriter fw;
		try {
			fw = new FileWriter(bagData);
	        fw.write(JsonUtils.toPrettyJson(cont));
	        fw.flush();
	        fw.close();
		} catch (IOException e) {
			player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
		}
    }*/
    
    
    void WriteToServer(Player player, ItemStack bagMeta, int size) {
    	String uuid = NBT.GetString(bagMeta, "bag-uuid");
    	String owner = NBT.GetString(bagMeta, "bag-owner");
    	Log.Debug(Main.plugin, "Attempting to write bag " + owner + "/" + uuid + " onto server");
    	if(owner != "ownerless") {
    		player = Bukkit.getPlayer(UUID.fromString(owner));
    	}
    	File bagData;
    	List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < size; i++) {
    		cont.add(null);
    	}
    	if(owner != "ownerless") {
    		bagData = new File(Main.plugin.getDataFolder() + "/bags/", owner + "/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Info(Main.plugin, String.format("Bag data for (%s) %s does not exist, creating new.", owner, uuid));
            }
    	}else {
    		bagData = new File(Main.plugin.getDataFolder() + "/bags/ownerless/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Info(Main.plugin, String.format("Bag data for (ownerless) %s does not exist, creating new.", uuid));
            }
    	}
        
        FileWriter fw;
		try {
			fw = new FileWriter(bagData);
	        fw.write(JsonUtils.toPrettyJson(cont));
	        fw.flush();
	        fw.close();
		} catch (IOException e) {
			player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
		}
    }
    
    public final Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }
}
