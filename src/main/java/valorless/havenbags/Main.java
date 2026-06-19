package valorless.havenbags;

import valorless.havenbags.configconversion.CV2_BagConversion;
import valorless.havenbags.configconversion.CV6_ConfigRestructure;
import valorless.havenbags.configconversion.CV7_ConfigRestructure;
import valorless.havenbags.configconversion.CV4_DataConversion;
import valorless.havenbags.configconversion.CV5_TokenConfigConversion;
import valorless.havenbags.database.BagCache;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.database.SkinCache;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.features.BagCarryLimit;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.features.BagHealth;
import valorless.havenbags.features.BagSkin;
import valorless.havenbags.features.BagUpgrade;
import valorless.havenbags.features.Crafting;
import valorless.havenbags.features.CustomBags;
import valorless.havenbags.features.CustomData;
import valorless.havenbags.features.Encumbering;
import valorless.havenbags.features.Insurance;
import valorless.havenbags.features.InventoryLock;
import valorless.havenbags.features.Magnet;
import valorless.havenbags.features.Quiver;
import valorless.havenbags.features.Refill;
import valorless.havenbags.features.Soulbound;
import valorless.havenbags.features.BackBag;
import valorless.havenbags.gui.FeaturesGUI;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.havenbags.hooks.*;
import valorless.havenbags.prevention.*;
import valorless.havenbags.utils.NoteBlockUtils;
import valorless.havenbags.utils.UpdateChecker;
import valorless.valorlessutils.Metrics;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.translate.Translator;
import valorless.valorlessutils.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings({"deprecated", "unused"})
public final class Main extends JavaPlugin implements Listener {
	public static JavaPlugin plugin;
	public static Config config;
	//public static Config timeTable;
	public static Config weight;
	public static Config blacklist;
	public static Config plugins;
	public static Config textures;
	public static Config effects;
	public static Config insurance;
	static PlaceholderAPI papi;
	//public static List<ActiveBag> activeBags = new ArrayList<ActiveBag>();
	Boolean uptodate = true;
	int newupdate = 9999999;
	String newVersion = null;
	public static Translator translator;
	
	public String[] commands = {
    		"havenbags", "bags", "bag",
    };
	
	public void onLoad() {
		plugin = this;
		Log.debug(plugin, Bukkit.getVersion());
		Log.debug(plugin, Bukkit.getBukkitVersion());
		Server.ResolveVersion();

		validateConfigs();

	}

	private void validateConfigs() {
		//config = new Config(this, "config.yml");
		config = ConfigValidation2.validateAndGetConfig("config.yml", List.of(
				"custom-data.9.example",
				"custom-data.9.example2",
				"custom-data.18.example"
		));
		//Lang.lang = new Config(this, "lang.yml");
		Lang.lang = ConfigValidation2.validateAndGetConfig("lang.yml");
		//timeTable = new Config(this, "timetable.yml");
		AutoPickup.filter = new Config(this, "filtering.yml"); // no validation
		//weight = new Config(this, "weight.yml");
		weight = ConfigValidation2.validateAndGetConfig("weight.yml", List.of(""));
		//blacklist = new Config(this, "blacklist.yml");;
		blacklist = ConfigValidation2.validateAndGetConfig("blacklist.yml", List.of(""));
		//plugins = new Config(this, "plugins.yml");
		plugins = ConfigValidation2.validateAndGetConfig("plugins.yml", List.of(""));
		textures = new Config(this, "textures.yml"); // no validation
		effects = new Config(this, "effects.yml"); // no validation
		//insurance = new Config(this, "insurance.yml");
		insurance = ConfigValidation2.validateAndGetConfig("insurance.yml", List.of(""));

		//old validation
		ConfigValidation.validate();
	}
	
	@SuppressWarnings("unused")
	boolean valorlessUtils() {
		Log.debug(plugin, "[DI-0] Checking ValorlessUtils");
		
		int requiresBuild = 374; // The build number of ValorlessUtils that is required for HavenBags to run.
		
		String ver = Bukkit.getPluginManager().getPlugin("ValorlessUtils").getDescription().getVersion();
		//Log.Debug(plugin, ver);
		String[] split = ver.split("[.]");
		int major = Integer.parseInt(split[0]);
		int minor = Integer.parseInt(split[1]);
		int hotfix = Integer.parseInt(split[2]);
		int build = Integer.parseInt(split[3]);
		
		if(build < requiresBuild) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        		public void run() {
        			Log.error(plugin, String.format("HavenBags requires ValorlessUtils build %s or newer, found %s. (%s)", requiresBuild, build, ver));
        			Log.error(plugin, "https://www.spigotmc.org/resources/valorlessutils.109586/");
        			Bukkit.getPluginManager().disablePlugin(plugin);
        		}
    		}, 10);
			return false;
		}
		else return true;
	}
	
	@SuppressWarnings("removal")
	@Override
    public void onEnable() {
		Log.debug(plugin, "HavenBags Debugging Enabled!");
		
		// Check if a correct version of ValorlessUtils is in use, otherwise don't run the rest of the code.
		if(!valorlessUtils()) return;
		
		registerSoftCrash();
		
		//ConfigValidation.Validate();

		VaultHook.hook();
		if(PlaceholderAPIHook.hook()) {
			papi = new PlaceholderAPI();
			papi.register();
		}
		new NexoHook();
		ChestSortHook.hook();
		PvPManagerHook.hook();
		if(ProtocolLibHook.hook()) {
			//WeightTooltipProtocollib.registerTooltipListener(this);
		}

		EssentialsHook.hook();
		try {
			new Insurance(); // Initialize insurance system if enabled in config. Requires Essentials to be hooked.
		} catch (Exception e) {
			Log.error(plugin, "Failed to initialize insurance system: " + e.getMessage());
		}
		
		//OraxenHook.Hook();
		
		Log.debug(plugin, "[DI-1] " + System.currentTimeMillis() / 1000L);
		
		translator = new Translator(config.getString("language"));
		
		validateSizeTextures();
		
		// Config-Version checks
		CV2_BagConversion.check(config); // Config 1 -> 2
		//TimeTableConversion.check(); Would've been Config 2 -> 3
		try {
			CV4_DataConversion.check(config);// Config 3 -> 4
		} catch (InvalidConfigurationException e) {} 
		CV5_TokenConfigConversion.check(config); // Config 4 -> 5
		CV6_ConfigRestructure.check(config); // Config 5 -> 6
		CV7_ConfigRestructure.check(config); // Config 6 -> 7
        
		CustomData.init();
		
		BagHealth.init();
		
		Database.init();
		
		AutoPickup.initiate();
		
		CustomBags.init();
		
		EtherealBags.init();

		registerListeners();
				
		registerCommands();

		if(config.getBool("check-updates") == true) {
			Log.info(plugin, "Checking for updates..");
			new UpdateChecker(this, 110420).getVersion(version -> {

				newVersion = version;
				String update = version.replace(".", "");
				newupdate = Integer.parseInt(update);
				String current = getDescription().getVersion().replace(".", "");;
				int v = Integer.parseInt(current);
				

				//if (!getDescription().getVersion().equals(version)) {
				if (v < newupdate) {
						Log.warning(plugin, String.format("An update has been found! (v%s, you are on v%s) \n", version, getDescription().getVersion()) +
							"This could be bug fixes or additional features.\n" + 
							"Please update HavenBags at https://www.spigotmc.org/resources/110420/");
					
					uptodate = false;
				}else {
					Log.info(plugin, "Up to date.");
				}
			});
		}
		
		// All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 18791; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> config.getString("language")));
        
        
    	//activeBags.clear();
    }

	@Override
    public void onDisable() {
    	closeBags(); // Close all open bags to prevent duping and other issues.
    	if(!BackBag.tracking.isEmpty()) {
    		for(Player player : BackBag.tracking.keySet()) {
    			BackBag.tracking.get(player).despawn();
    		}
    	}
    	if(BackBag.cleantask != null) BackBag.cleantask.cancel();
    	Database.saveData(true); // Save all bag data on shutdown. The "true" parameter marks this as a shutdown save.
    	Database.shutdown(); // Close all database connections.
    	Crafting.removeRecipes();
    	BagEffects.shutdown(); // Stop the bag effects tasks.
    	UpgradeGUI.OpenGUIs.closeAll(); // Close all open upgrade GUIs.
    	SkinCache.shutdown(); // Save skin cache.
    	EtherealBags.shutdown(); // Close and save ethereal bags.
    	Insurance.shutdown(); // Save insurance data.
		FeaturesGUI.OpenGUIs.closeAll(); // Close all open features GUIs.
    }
	
	public void onCrashDisable() {
    	closeBags();
    	if(!BackBag.tracking.isEmpty()) {
    		for(Player player : BackBag.tracking.keySet()) {
    			BackBag.tracking.get(player).despawn();
    		}
    	}
    	if(BackBag.cleantask != null) BackBag.cleantask.cancel();
    	Database.saveData(true);
    	Crafting.removeRecipes();
    	BagEffects.shutdown();
    	UpgradeGUI.OpenGUIs.closeAll();
    	SkinCache.shutdown();
    	EtherealBags.shutdown();
    	Insurance.shutdown();
		
	}
    
    public static void closeBags() {
    	if(!Database.getOpenBags().isEmpty()) {
    		Log.info(plugin, "Closing all open bags.");
    		try {
    			for(Bag bag : Database.getOpenBags()) {
    				bag.getGui().close(true);
    			}
    		} catch (Exception e) {
    		
    		}
    	}
    	//activeBags.clear();
    }
    
    void registerCommands() {
        for (String command : commands) {
			Log.debug(plugin, "[DI-20] Registering Command: " + command);
			getCommand(command).setExecutor(new CommandListener());
			getCommand(command).setTabCompleter(new TabCompletion());
		}
    }
    
	void registerListeners() {
		EventListener.init();
    	PlacementBlocker.init();
		BagDamagePrevention.init();
		BagListener.init();
		CloneListener.init();
		InventoryListener.init();
		PickupPrevention.init();
		CraftPrevention.init();
		EquipPrevention.init();
		Crafting.init();
		AutoPickup.init();
		Encumbering.init();
		BagUpgrade.init();
		BagSkin.init();
		MinepacksBagRestore.init();
		EpicBackpacksBagRestore.init();
		Quiver.init();
		BagCarryLimit.init();
		Soulbound.init();
		InventoryLock.init();
		//BackBag.init(); needs a lil work
		Magnet.init();
		Refill.init();
		BundlePrevention.init();
		BagEffects.init();
		BagCache.Observer.init();
		SkinCache.init();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		Bukkit.getPluginManager().registerEvents(new NoteBlockUtils(), this);
    }


	@EventHandler
	public void updateNotification(PlayerJoinEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    public void run() {
		    	if (config.getBool("check-updates") && e.getPlayer().isOp() && uptodate == false) {
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&7[&aHaven&bBags&7] " + "&fAn update has been found.\nPlease download version&a " + newVersion
						+ ", &fyou are on version&a " + getDescription().getVersion() + "!"
						));
				}
		    }
		}, 5L);
		
	}
    
    /*void TimeTable() {
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
    }*/
    
    void validateSizeTextures() {
    	if(config.getBool("bag-textures.enabled")) {
    		boolean c = false;
    		for(int s = 9; s <= 54; s += 9) {
    			if(Utils.IsStringNullOrEmpty(Main.config.getString("bag-textures.size-" + s))){
    				config.set("bag-textures.size-" + s, config.getString("bag.texture"));
    				c = true;
    			}
			}
    		for(int s = 9; s <= 54; s += 9) {
    			if(Utils.IsStringNullOrEmpty(Main.config.getString("bag-textures.size-ownerless-" + s))){
    				config.set("bag-textures.size-ownerless-" + s, config.getString("bag.texture"));
    				c = true;
    			}
			}
    		
    		if(c) config.saveConfig();
    	}
    }
    
    void registerSoftCrash() {
    	Log.debug(plugin, "Registering shutdown hook for crash detection.");
    	try {
    		Runtime.getRuntime().addShutdownHook(
    				new Thread(() -> {
    					Log.error(plugin, "Detected possible crash. Attempting to save data, close bags properly and shutting down.");
    					onCrashDisable(); // Attempt to run the onDisable method to save data and close bags properly. This won't work on hard crashes, but should work on soft crashes.
    					Bukkit.getServer().getPluginManager().disablePlugin(this); // Disable the plugin to prevent further issues. Again, this won't work on hard crashes.
    				}, "HavenBags-Shutdown-Hook")
    		);
    		Log.debug(plugin, "Registered shutdown hook for crash detection.");
        } catch (Exception e) {
        	Log.error(plugin, "Failed to register shutdown hook for crash detection. Data may not be saved properly on crashes.");
        }
    }
}
