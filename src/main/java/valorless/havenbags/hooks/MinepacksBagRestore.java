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
import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.utils.Utils;

public class MinepacksBagRestore implements Listener{
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-211] Registering MinepacksBagRestore");
		Bukkit.getServer().getPluginManager().registerEvents(new MinepacksBagRestore(), Main.plugin);
	}
	
	Boolean playersRemain = true;

	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		playersRemain = PlayersRemaining();
		if(!playersRemain) return;
        Player player = event.getPlayer();
        Log.Debug(Main.plugin, "[DI-212] [MinepacksBagRestore] " + player.getName());
        Config config = new Config(Main.plugin, "minepacks/players.yml");
        ConfigurationSection playersSection = config.GetConfigurationSection("players");
        
        if(playersSection == null) {
        	RemoveDirectory();
        	return;
        }
        
        if(playersSection.getKeys(false) == null || playersSection.getKeys(false).size() == 0) {
        	RemoveDirectory();
        	return;
        }else {
        	for (String playerKey : playersSection.getKeys(false)) {
        		Log.Debug(Main.plugin, "[DI-213] [MinepacksBagRestore] " + playerKey);
        		if(playerKey.equalsIgnoreCase(player.getUniqueId().toString())) {
        			String baguuid = config.GetString(String.format("players.%s", playerKey));
            		Log.Debug(Main.plugin, "[DI-214] [MinepacksBagRestore] " + baguuid);
        			ItemStack bag = GetBag(baguuid, playerKey, player);
        			if(bag != null) {
                		Log.Debug(Main.plugin, "[DI-215] [MinepacksBagRestore] " + "BagData found, giving bag.");
        				GiveItem(player, bag);
        				config.Set(String.format("players.%s", playerKey), null);
        				config.SaveConfig();
        			}
        		}
            }
        }
    }
	
	void GiveItem(Player player, ItemStack item) {
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
	
    boolean PlayersRemaining() {
        // Get the data folder of the plugin
        File dataFolder = Main.plugin.getDataFolder();

        // Now, check for a specific folder inside the data folder
        File myFolder = new File(dataFolder, "minepacks");

        if (myFolder.exists() && myFolder.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }
    
    void RemoveDirectory() {
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
    
    ItemStack GetBag(String baguuid, String playeruuid, Player target) {
    	List<ItemStack> Content  = LoadContent(playeruuid, baguuid);
		if (Content == null) return null;
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		
		String bagTexture = Main.config.GetString("bag-texture");
		ItemStack bagItem = new ItemStack(Material.AIR);
		int size = Content.size();
		
		if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
			bagItem = HeadCreator.itemFromBase64(bagTexture);
		} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
		}
		
		PDC.SetString(bagItem, "uuid", baguuid);
		PDC.SetString(bagItem, "owner", playeruuid);
		PDC.SetInteger(bagItem, "size", size);
		if(playeruuid.equalsIgnoreCase("ownerless")) {
			PDC.SetBoolean(bagItem, "binding", false);
		}else {
			PDC.SetBoolean(bagItem, "binding", true);
		}
		
		ItemMeta bagMeta = bagItem.getItemMeta();
		if(Main.config.GetInt("bag-custom-model-data") != 0) {
			bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
		}
		if(Main.config.GetBool("bag-custom-model-datas.enabled")) {
			for(int s = 9; s <= 54; s += 9) {
				if(size == s) {
					if(PDC.GetBoolean(bagItem, "binding")) {
						bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + size));
					}else {
						bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-ownerless-" + size));
					}
				}
			}
		}
		
		if(PDC.GetBoolean(bagItem, "binding")) {
			bagMeta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-bound-name"), target));
		}else {
			bagMeta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-ownerless-used"), target));
		}
		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.GetStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, target));
		}
		if(PDC.GetBoolean(bagItem, "binding")) {
			placeholders.add(new Placeholder("%owner%", target.getName()));
            lore.add(Lang.Parse(Lang.Get("bound-to"), placeholders, target));
        }
		
        placeholders.add(new Placeholder("%size%", size));
        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, target));
		
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
						items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, target));
					} else {
						items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, target));
					}
    			}else {
	    			itemph.add(new Placeholder("%item%", Main.translator.Translate(Content.get(i).getType().getTranslationKey())));
	    			itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
	    			if(Content.get(i).getAmount() != 1) {
    					items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, target));
    				} else {
    					items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, target));
    				}
    			}
    			a++;
    		}
    	}
        if(a > 0 && Lang.lang.GetBool("show-bag-content")) {
        	lore.add(Lang.Get("bag-content-title"));
        	for(int k = 0; k < items.size(); k++) {
        		if(k < Lang.lang.GetInt("bag-content-preview-size")) {
        			lore.add("  " + items.get(k));
        		}
        	}
        	if(a > Lang.lang.GetInt("bag-content-preview-size")) {
        		lore.add(Lang.Get("bag-content-and-more"));
        	}
        }
		bagMeta.setLore(lore);
		bagItem.setItemMeta(bagMeta);
		
		try {
			HavenBags.UpdateBagItem(bagItem, Bukkit.getOfflinePlayer(UUID.fromString(playeruuid)));
		}catch(Exception e) {
			HavenBags.UpdateBagItem(bagItem, null);
		}
		
		return bagItem;
    }
    
    List<ItemStack> LoadContent(String owner, String uuid) {
		String id = uuid.replace(".json", "");
		id = id.replace(".yml", "");
		return BagData.GetBag(id, null).getContent();
	}
    
    
}
