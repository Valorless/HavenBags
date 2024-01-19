package valorless.havenbags;

import valorless.havenbags.hooks.*;
import valorless.havenbags.prevention.*;
import valorless.valorlessutils.ValorlessUtils.*;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.translate.Translator;
import valorless.valorlessutils.uuid.UUIDFetcher;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
	public static JavaPlugin plugin;
	public static Config config;
	public static Config timeTable;
	public static List<ActiveBag> activeBags = new ArrayList<ActiveBag>();
	Boolean uptodate = true;
	int newupdate = 9999999;
	String newVersion = null;
	public static Translator translator;
	
	public String[] commands = {
    		"havenbags", "bags", "bag",
    };
	
	public void onLoad() {
		plugin = this;
		config = new Config(this, "config.yml");
		CommandListener.plugin = this;
		BagListener.plugin = this;
		
		Lang.lang = new Config(this, "lang.yml");

		timeTable = new Config(this, "timetable.yml");
	}
	
	@Override
    public void onEnable() {
		Log.Debug(plugin, "HavenBags Debugging Enabled!");
		PlaceholderAPIHook.Hook();
		ChestSortHook.Hook();
		
		Log.Debug(plugin, Long.toString(System.currentTimeMillis() / 1000L));
		
		config.AddValidationEntry("debug", false);
		config.AddValidationEntry("config-version", 2);
		config.AddValidationEntry("check-updates", true);
		config.AddValidationEntry("language", "en_us");
		config.AddValidationEntry("bag-type", "HEAD");
		config.AddValidationEntry("bag-material", "ENDER_CHEST");
		config.AddValidationEntry("bag-custom-model-data", 0);
		config.AddValidationEntry("bag-texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=");
		config.AddValidationEntry("open-sound", "ITEM_BUNDLE_INSERT");
		config.AddValidationEntry("open-volume", 1);
		config.AddValidationEntry("open-pitch", 1);
		config.AddValidationEntry("close-sound", "ITEM_BUNDLE_DROP_CONTENTS");
		config.AddValidationEntry("close-volume", 1);
		config.AddValidationEntry("close-pitch", 1);
		config.AddValidationEntry("inventory-full-sound", "ENTITY_VILLAGER_NO");
		config.AddValidationEntry("inventory-full-volume", 1);
		config.AddValidationEntry("inventory-full-pitch", 1);
		config.AddValidationEntry("protect-bags", true);
		config.AddValidationEntry("bags-in-bags", true);
		config.AddValidationEntry("bags-in-shulkers", true);
		config.AddValidationEntry("blacklist", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

		{ add("world_name"); add("world_name_nether"); add("another_world"); }} );
		Log.Debug(plugin, "Validating config.yml");
		config.Validate();
		
		Lang.lang.AddValidationEntry("prefix", "&7[&aHaven&bBags&7] &r");
		Lang.lang.AddValidationEntry("malformed-command", "&cUnknown command, are you missing some parameters?");
		Lang.lang.AddValidationEntry("bag-load-error", "&cBag failed to load.\nPlease notify staff.");
		Lang.lang.AddValidationEntry("bag-rename", "&fRenamed bag to %s.");
		Lang.lang.AddValidationEntry("bag-rename-reset", "&fReset bag''s name.");
		Lang.lang.AddValidationEntry("bag-cannot-rename", "&cYou can only rename bags.");
		Lang.lang.AddValidationEntry("bag-cannot-use", "&cYou cannot use this bag.");
		Lang.lang.AddValidationEntry("bag-does-not-exist", "&cThis bag does not exist.");
		Lang.lang.AddValidationEntry("inventory-full", "&cInventory full, dropping bag on the ground!");
		Lang.lang.AddValidationEntry("bag-in-bag-error", "&cBags cannot be put inside other bags.");
		Lang.lang.AddValidationEntry("bag-in-shulker-error", "&cBags cannot be put inside shulker boxes.");
		
		// Admin Lang
		//Lang.lang.AddValidationEntry("bag-create", ""); //unsure wtf this was for
		Lang.lang.AddValidationEntry("player-no-exist", "&cNo bags found for this player.");
		Lang.lang.AddValidationEntry("bag-not-found", "&cNo bag found with that UUID.");
		Lang.lang.AddValidationEntry("bag-size-error", "&cSize cannot be over 6 rows.");
		Lang.lang.AddValidationEntry("bag-ownerless-no-size", "&cOwnerless bag must have a size.");
		Lang.lang.AddValidationEntry("bag-given", "&aYou''ve been given an %s!");
		Lang.lang.AddValidationEntry("number-conversion-error", "&cCannot convert ''%s'' to a number!");
		Lang.lang.AddValidationEntry("player-no-bags", "&cPlayer ''%s'' has no bags.");
		Lang.lang.AddValidationEntry("bags-of", "Bags of %s:");
		
		// Bag GUI
		Lang.lang.AddValidationEntry("bag-inventory-title", "");
		
		// BagItem Lang
		Lang.lang.AddValidationEntry("bag-bound-name", "&a%player%''s Bag");
		Lang.lang.AddValidationEntry("bag-unbound-name", "&aUnbound Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-unused", "&aUnused Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-used", "&aBag");
		Lang.lang.AddValidationEntry("bag-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&fA well crafted bag, suited for carrying stuff."); 
				}
			} 
		);
		//Lang.lang.AddValidationEntry("bound-to", "&7Bound to %s");
		Lang.lang.AddValidationEntry("bound-to", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Bound to %s"); 
				}
			} 
		);
		//Lang.lang.AddValidationEntry("bag-size", "&7Size: %s");
		Lang.lang.AddValidationEntry("bag-size", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Size: %s"); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("show-bag-content", true);
		Lang.lang.AddValidationEntry("bag-content-title", "&7Content:");
		Lang.lang.AddValidationEntry("bag-content-preview-size", 5);
		Lang.lang.AddValidationEntry("bag-content-item", "&7%s");
		Lang.lang.AddValidationEntry("bag-content-item-amount", "&7%s &7x%s");
		Lang.lang.AddValidationEntry("bag-content-and-more", "&7And more..");
		
		Log.Debug(plugin, "Validating lang.yml");
		Lang.lang.Validate();
		
		translator = new Translator(config.GetString("language"));

		RegisterListeners();
				
		RegisterCommands();

		getServer().getPluginManager().registerEvents(new CustomRecipe(), this);
		CustomRecipe.PrepareRecipes();
		
		if(config.GetBool("check-updates") == true) {
			Log.Info(plugin, "Checking for updates..");
			new UpdateChecker(this, 110420).getVersion(version -> {

				newVersion = version;
				String update = version.replace(".", "");
				newupdate = Integer.parseInt(update);
				String current = getDescription().getVersion().replace(".", "");;
				int v = Integer.parseInt(current);
				

				//if (!getDescription().getVersion().equals(version)) {
				if (v < newupdate) {
						Log.Warning(plugin, String.format("An update has been found! (v%s, you are on v%s) \n", version, getDescription().getVersion()) + 
							"This could be bug fixes or additional features.\n" + 
							"Please update HavenBags at https://www.spigotmc.org/resources/110420/");
					
					uptodate = false;
				}else {
					Log.Info(plugin, "Up to date.");
				}
			});
		}
		
		// All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 18791; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> config.GetString("language")));
        
        // Config-Version checks
        BagConversion();
        TimeTable();
    }
    
    @Override
    public void onDisable() {
    	if(activeBags.size() != 0) {
    		Log.Info(plugin, "Closing all open bags.");
    		try {
    			for(ActiveBag bag : activeBags) {
    				bag.gui.Close(true);
    			}
    		} catch (Exception e) {
    		
    		}
    	}
    	
    	CustomRecipe.RemoveRecipes();
    }
    
    public void RegisterCommands() {
    	for (int i = 0; i < commands.length; i++) {
    		Log.Debug(plugin, "Registering Command: " + commands[i]);
    		getCommand(commands[i]).setExecutor(new CommandListener());
    		getCommand(commands[i]).setTabCompleter(new TabCompletion());
    	}
    }
    
    public void RegisterListeners() {
    	Log.Debug(plugin, "Registering PlacementListener");
		getServer().getPluginManager().registerEvents(new PlacementBlocker(), this);
		Log.Debug(plugin, "Registering BagDamagePrevention");
		getServer().getPluginManager().registerEvents(new BagDamagePrevention(), this);
		Log.Debug(plugin, "Registering BagListener");
		getServer().getPluginManager().registerEvents(new BagListener(), this);
		Log.Debug(plugin, "Registering CloneListener");
		getServer().getPluginManager().registerEvents(new CloneListener(), this);
		Log.Debug(plugin, "Registering InventoryListener");
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		Log.Debug(plugin, "Registering PickupPrevention");
		getServer().getPluginManager().registerEvents(new PickupPrevention(), this);
		Log.Debug(plugin, "Registering CraftPrevention");
		getServer().getPluginManager().registerEvents(new CraftPrevention(), this);
		
		Bukkit.getPluginManager().registerEvents(this, this);
    }


	@EventHandler
	public void UpdateNotification(PlayerJoinEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    public void run() {
		    	if (config.GetBool("check-updates") && e.getPlayer().isOp() && uptodate == false) {
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&7[&aHaven&bBags&7] " + "&fAn update has been found.\nPlease download version&a " + newVersion
						+ ", &fyou are on version&a " + getDescription().getVersion() + "!"
						));
				}
		    }
		}, 5L);
		
	}
    
    void BagConversion() {
    	if(config.GetInt("config-version") < 2) {
    		Log.Warning(plugin, "Old configuration found, updating bag data!");
    		config.Set("config-version", 2);
    		config.SaveConfig();
    		
    		File file = new File(String.format("%s/bags", plugin.getDataFolder()));
    		String[] directories = file.list(new FilenameFilter() {
    		  @Override
    		  public boolean accept(File current, String name) {
    		    return new File(current, name).isDirectory();
    		  }
    		});
			
			for(String folder : directories) {
				if(folder.equalsIgnoreCase("ownerless")) continue;
				try {
					File f = new File(String.format("%s/bags/%s", plugin.getDataFolder(), folder));
					File to = new File(String.format("%s/bags/%s", plugin.getDataFolder(), UUIDFetcher.getUUID(folder)));
					f.renameTo(to);
					Log.Warning(plugin, String.format("%s => %s", 
						String.format("/bags/%s", folder), 
						String.format("/bags/%s", UUIDFetcher.getUUID(folder))
					));
				} catch(Exception e) {
					Log.Error(plugin, String.format("Failed to convert %s, may require manual update.", String.format("/bags/%s", folder)));
				}
			}
    	}
    }
    
    void TimeTable() {
    	if(config.GetInt("config-version") < 3) {
    		Log.Warning(plugin, "Old configuration found, updating time table!");
    		config.Set("config-version", 3);
    		config.SaveConfig();
    		
    		File directory = new File(String.format("%s/bags", plugin.getDataFolder()));
    		String[] directories = directory.list(new FilenameFilter() {
    		  @Override
    		  public boolean accept(File current, String name) {
    		    return new File(current, name).isDirectory();
    		  }
    		});
			
			for(String folder : directories) {
				try {
					File file = new File(String.format("%s/bags/%s", plugin.getDataFolder(), folder));
					String[] files = file.list();
					for(String f : files) {
						try {
							f = f.replace(".json", "");
							timeTable.Set(folder + "/" + f, System.currentTimeMillis() / 1000L);
						} catch(Exception e) {
							Log.Error(plugin, String.format("Failed to add %s", String.format("/bags/%s/%s", folder, f)));
						}
					}
				} catch(Exception e) {
					Log.Error(plugin, String.format("Failed to add %s", String.format("/bags/%s", folder)));
					e.printStackTrace();
				}
			}
			timeTable.SaveConfig();
    	}
    }
}
