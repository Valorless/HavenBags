package valorless.havenbags.hooks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.utils.Utils;

public class MinepacksBagRestore implements Listener{
	
	public static void init() {
		Log.debug(Main.plugin, "[DI-211] Registering MinepacksBagRestore");
		Bukkit.getServer().getPluginManager().registerEvents(new MinepacksBagRestore(), Main.plugin);
	}
	
	Boolean playersRemain = true;

	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		playersRemain = playersRemaining();
		if(!playersRemain) return;
        Player player = event.getPlayer();
        Log.debug(Main.plugin, "[DI-212] [MinepacksBagRestore] " + player.getName());
        Config config = new Config(Main.plugin, "minepacks/players.yml");
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        
        if(playersSection == null) {
        	removeDirectory();
        	return;
        }
        
        if(playersSection.getKeys(false) == null || playersSection.getKeys(false).isEmpty()) {
        	removeDirectory();
        	return;
        }else {
        	for (String playerKey : playersSection.getKeys(false)) {
        		Log.debug(Main.plugin, "[DI-213] [MinepacksBagRestore] " + playerKey);
        		if(playerKey.equalsIgnoreCase(player.getUniqueId().toString())) {
        			String baguuid = config.getString(String.format("players.%s", playerKey));
            		Log.debug(Main.plugin, "[DI-214] [MinepacksBagRestore] " + baguuid);
        			ItemStack bag = getBag(baguuid, playerKey, player);
        			if(bag != null) {
                		Log.debug(Main.plugin, "[DI-215] [MinepacksBagRestore] " + "BagData found, giving bag.");
        				giveItem(player, bag);
        				config.set(String.format("players.%s", playerKey), null);
        				config.saveConfig();
        			}
        		}
            }
        }
    }
	
	void giveItem(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        
        // Check if the player's inventory has space for the item
        player.sendMessage("§eYou have received an item!");
        if (inventory.firstEmpty() != -1) {
            // Add the item to the player's inventory
            inventory.addItem(item);
        } else {
            // Drop the item on the ground at the player's location
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage("§cYour inventory is full, so the item has been dropped on the ground!");
        }
    }
	
    boolean playersRemaining() {
        // Get the data folder of the plugin
        File dataFolder = Main.plugin.getDataFolder();

        // Now, check for a specific folder inside the data folder
        File myFolder = new File(dataFolder, "minepacks");

        return myFolder.exists() && myFolder.isDirectory();
    }
    
    void removeDirectory() {
    	//Log.Info(Main.plugin, "All converted Minepacks have been given out.");
    	// Get the data folder of the plugin
        File dataFolder = Main.plugin.getDataFolder();

        // Now, check for a specific folder inside the data folder
        File myFolder = new File(dataFolder, "minepacks");

        if (myFolder.exists() && myFolder.isDirectory()) {
        	File[] files = myFolder.listFiles();
            if (files != null) {
                // Recursively delete files in folder
                for (File file : files) {
                	file.delete(); // Delete file
                }
            }
        	myFolder.delete();
        }
    }
    
    ItemStack getBag(String baguuid, String playeruuid, Player target) {
    	List<ItemStack> Content  = loadContent(playeruuid, baguuid);
		if (Content == null) return null;
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		
		String bagTexture = Main.config.getString("bag.texture");
		ItemStack bagItem = new ItemStack(Material.AIR);
		int size = Content.size();
		
		if(Main.config.getString("bag.type").equalsIgnoreCase("HEAD")){
			bagItem = HeadCreator.itemFromBase64(bagTexture);
		} else if(Main.config.getString("bag.type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.getMaterial("bag.material"));
		}
		
		PDC.setString(bagItem, "uuid", baguuid);
		PDC.setString(bagItem, "owner", playeruuid);
		PDC.setinteger(bagItem, "size", size);
		if(playeruuid.equalsIgnoreCase("ownerless")) {
			PDC.setBoolean(bagItem, "binding", false);
		}else {
			PDC.setBoolean(bagItem, "binding", true);
		}
		
		ItemMeta bagMeta = bagItem.getItemMeta();
		if(Main.config.getInt("bag.modeldata") != 0) {
			bagMeta.setCustomModelData(Main.config.getInt("bag.modeldata"));
		}
		if(Main.config.getBool("bag-custom-model-datas.enabled")) {
			for(int s = 9; s <= 54; s += 9) {
				if(size == s) {
					if(PDC.getBoolean(bagItem, "binding")) {
						bagMeta.setCustomModelData(Main.config.getInt("bag-custom-model-datas.size-" + size));
					}else {
						bagMeta.setCustomModelData(Main.config.getInt("bag-custom-model-datas.size-ownerless-" + size));
					}
				}
			}
		}
		
		if(PDC.getBoolean(bagItem, "binding")) {
			bagMeta.setDisplayName(Lang.parse(Lang.lang.getString("bag-bound-name"), target));
		}else {
			bagMeta.setDisplayName(Lang.parse(Lang.lang.getString("bag-ownerless-used"), target));
		}
		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.getStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, target));
		}
		if(PDC.getBoolean(bagItem, "binding")) {
			placeholders.add(new Placeholder("%owner%", target.getName()));
            lore.add(Lang.parse(Lang.get("bound-to"), placeholders, target));
        }
		
        placeholders.add(new Placeholder("%size%", size));
        lore.add(Lang.parse(Lang.get("bag-size"), placeholders, target));
		
		List<ItemStack> cont = new ArrayList<ItemStack>();
        int a = 0;
		List<String> items = new ArrayList<String>();
        for(int i = 0; i < Content.size(); i++) {
    		cont.add(Content.get(i));
    		if(Content.get(i) != null) {
    			List<Placeholder> itemph = new ArrayList<Placeholder>();
    			if(Content.get(i).getItemMeta().hasDisplayName()) {
					itemph.add(new Placeholder("%item%", Content.get(i).getItemMeta().getDisplayName()));
					itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
					if(Content.get(i).getAmount() != 1) {
						items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, target));
					} else {
						items.add(Lang.parse(Lang.get("bag-content-item"), itemph, target));
					}
    			}else {
	    			itemph.add(new Placeholder("%item%", Main.translator.Translate(Content.get(i).getType().getTranslationKey())));
	    			itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
	    			if(Content.get(i).getAmount() != 1) {
    					items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, target));
    				} else {
    					items.add(Lang.parse(Lang.get("bag-content-item"), itemph, target));
    				}
    			}
    			a++;
    		}
    	}
        if(a > 0 && Lang.lang.getBool("show-bag-content")) {
        	lore.add(Lang.get("bag-content-title"));
        	for(int k = 0; k < items.size(); k++) {
        		if(k < Lang.lang.getInt("bag-content-preview-size")) {
        			lore.add("  " + items.get(k));
        		}
        	}
        	if(a > Lang.lang.getInt("bag-content-preview-size")) {
        		lore.add(Lang.get("bag-content-and-more"));
        	}
        }
		bagMeta.setLore(lore);
		bagItem.setItemMeta(bagMeta);
		
		try {
			HavenBags.updateBagItem(bagItem, Bukkit.getOfflinePlayer(UUID.fromString(playeruuid)));
		}catch(Exception e) {
			HavenBags.updateBagItem(bagItem, null);
		}
		
		return bagItem;
    }
    
    List<ItemStack> loadContent(String owner, String uuid) {
		String id = uuid.replace(".json", "");
		id = id.replace(".yml", "");
		return Database.getBag(id, null).getContent();
	}
    
    
}
