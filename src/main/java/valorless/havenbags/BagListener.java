package valorless.havenbags;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.ValorlessUtils.Tags;
import valorless.valorlessutils.json.JsonUtils;

public class BagListener implements Listener{
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
    		Player player = event.getPlayer();
    		//player.sendMessage("Right click");
    		if(!player.hasPermission("havenbags.use")) {
    			return;
    		}else {
    			ItemStack hand = player.getInventory().getItemInMainHand();
    			ItemMeta item = player.getInventory().getItemInMainHand().getItemMeta();
    			//player.sendMessage("has meta: " + hand.hasItemMeta());
    			if(item == null) return;
    			
    			if(Tags.Get(plugin, item.getPersistentDataContainer(), "uuid", PersistentDataType.STRING) != null) {
        			Log.Debug(plugin, player.getName() + " is attempting to open a bag");
    				//player.sendMessage("has uuid: true");
    				String owner = Tags.Get(plugin, item.getPersistentDataContainer(), "owner", PersistentDataType.STRING).toString();
    				String canbind = Tags.Get(plugin, item.getPersistentDataContainer(), "canbind", PersistentDataType.STRING).toString();
    				//player.sendMessage(owner);
    				//player.sendMessage(canbind);
    				
					// If create ownerless
    				if(owner.equalsIgnoreCase("null") && canbind.equalsIgnoreCase("false"))
    				{
    					//item.setDisplayName("§aBag");
    					item.setDisplayName(Lang.Get("bag-ownerless-used"));
    	    			List<String> lore = new ArrayList<String>() ;
						//lore.add("§7Size: " + Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER));
						lore.add(Lang.Get("bag-size", Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER)));
    					item.setLore(lore);
    					//List<ItemStack> content = new ArrayList<ItemStack>();
    					//for(int i = 0; i < (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER); i++) {
    					//	content.add(new ItemStack(Material.AIR));
    					//}
    					Tags.Set(plugin, item.getPersistentDataContainer(), "owner", "ownerless", PersistentDataType.STRING);
    					//Tags.Set(plugin, item.getPersistentDataContainer(), "content", JsonUtils.toJson(content), PersistentDataType.STRING);
    					hand.setItemMeta(item);
    					WriteToServer(player, item, (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER));
    	    			Log.Debug(plugin, "Ownerless bag created.");
    					return;
    				}
				
					// If unbound
    				if(owner.equalsIgnoreCase("null") && canbind.equalsIgnoreCase("true"))
    				{
    					//item.setDisplayName("§a" + player.getName() +"'s Bag");
    					item.setDisplayName(Lang.Get("bag-bound-name", player.getName()));
    					List<String> lore = new ArrayList<String>();
    					//lore.add("§7Bound to " + player.getName());
    					lore.add(Lang.Get("bound-to", player.getName()));
						//lore.add("§7Size: " + Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER));
						lore.add(Lang.Get("bag-size", Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER)));
    					item.setLore(lore);
    					//List<ItemStack> content = new ArrayList<ItemStack>();
    					//for(int i = 0; i < (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER); i++) {
    					//	content.add(new ItemStack(Material.AIR));
    					//}
    					Tags.Set(plugin, item.getPersistentDataContainer(), "owner", player.getUniqueId().toString(), PersistentDataType.STRING);
    					//Tags.Set(plugin, item.getPersistentDataContainer(), "content", JsonUtils.toJson(content), PersistentDataType.STRING);
    					hand.setItemMeta(item);
    					WriteToServer(player, item, (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER));
    	    			Log.Debug(plugin, "Bound new bag to: " + player.getName());
    					return;
    				}
    				

    				
				
    				if(canbind.equalsIgnoreCase("false")) {
    					//player.sendMessage("Ownerless Bag");
    					BagGUI gui = new BagGUI(plugin, (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER), player, hand, (SkullMeta)hand.getItemMeta());
    					Bukkit.getServer().getPluginManager().registerEvents(gui, plugin);
    			    	player.getInventory().remove(hand);
    					gui.OpenInventory(player);
    					SFX.Play(HavenBags.config.GetString("open-sound"), 
    							HavenBags.config.GetFloat("open-volume").floatValue(), 
    							HavenBags.config.GetFloat("open-pitch").floatValue(), player);
    	    			Log.Debug(plugin, "Attempting to open ownerless bag");
    					return;
    				}
    				if(canbind.equalsIgnoreCase("true")) {
    					//player.sendMessage("Bound Bag");
    					if(owner.equalsIgnoreCase(player.getUniqueId().toString())) {
    						BagGUI gui = new BagGUI(plugin, (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER), player, hand, (SkullMeta)hand.getItemMeta());
    						Bukkit.getServer().getPluginManager().registerEvents(gui, plugin);
        			    	player.getInventory().remove(hand);
    						gui.OpenInventory(player);
        					SFX.Play(HavenBags.config.GetString("open-sound"), 
        							HavenBags.config.GetFloat("open-volume").floatValue(), 
        							HavenBags.config.GetFloat("open-pitch").floatValue(), player);
    		    			Log.Debug(plugin, "Attempting to open bag");
    						return;
    					} else if (player.hasPermission("havenbags.bypass")) {
    						BagGUI gui = new BagGUI(plugin, (int)Tags.Get(plugin, item.getPersistentDataContainer(), "size", PersistentDataType.INTEGER), player, hand, (SkullMeta)hand.getItemMeta());
    						Bukkit.getServer().getPluginManager().registerEvents(gui, plugin);
        			    	player.getInventory().remove(hand);
    						gui.OpenInventory(player);
        					SFX.Play(HavenBags.config.GetString("open-sound"), 
        							HavenBags.config.GetFloat("open-volume").floatValue(), 
        							HavenBags.config.GetFloat("open-pitch").floatValue(), player);
    		    			Log.Debug(plugin, player + "has attempted to open a bag, bypassing the lock");
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
    
    void WriteToServer(Player player, ItemMeta bagMeta, int size) {
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
    }
}
