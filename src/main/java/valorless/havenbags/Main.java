package valorless.havenbags;

import valorless.havenbags.configconversion.BagConversion;
import valorless.havenbags.configconversion.ConfigRestructure;
import valorless.havenbags.configconversion.DataConversion;
import valorless.havenbags.configconversion.TokenConfigConversion;
import valorless.havenbags.database.BagCache;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.database.SkinCache;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.features.BagCarryLimit;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.features.BagSkin;
import valorless.havenbags.features.BagUpgrade;
import valorless.havenbags.features.Crafting;
import valorless.havenbags.features.CustomBags;
import valorless.havenbags.features.Encumbering;
import valorless.havenbags.features.InventoryLock;
import valorless.havenbags.features.Magnet;
import valorless.havenbags.features.Quiver;
import valorless.havenbags.features.Refill;
import valorless.havenbags.features.Soulbound;
import valorless.havenbags.features.BackBag;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.havenbags.hooks.*;
import valorless.havenbags.prevention.*;
import valorless.havenbags.utils.NoteBlockUtils;
import valorless.havenbags.utils.UpdateChecker;
import valorless.valorlessutils.Metrics;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.ValorlessUtils.Log;
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

@SuppressWarnings("deprecation")
public final class Main extends JavaPlugin implements Listener {	
	public static JavaPlugin plugin;
	public static Config config;
	//public static Config timeTable;
	public static Config weight;
	public static Config blacklist;
	public static Config plugins;
	public static Config textures;
	public static Config effects;
	protected static PlaceholderAPI papi;
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
		Log.Debug(plugin, Bukkit.getVersion());
		Log.Debug(plugin, Bukkit.getBukkitVersion());
		Server.ResolveVersion();
		
		config = new Config(this, "config.yml");
		Lang.lang = new Config(this, "lang.yml");
		//timeTable = new Config(this, "timetable.yml");
		AutoPickup.filter = new Config(this, "filtering.yml");
		weight = new Config(this, "weight.yml");
		blacklist = new Config(this, "blacklist.yml");;
		plugins = new Config(this, "plugins.yml");
		textures = new Config(this, "textures.yml");
		effects = new Config(this, "effects.yml");
	}
	
	@SuppressWarnings("unused")
	boolean ValorlessUtils() {
		Log.Debug(plugin, "[DI-0] Checking ValorlessUtils");
		
		int requiresBuild = 313; // The build number of ValorlessUtils that is required for HavenBags to run.
		
		String ver = Bukkit.getPluginManager().getPlugin("ValorlessUtils").getDescription().getVersion();
		//Log.Debug(plugin, ver);
		String[] split = ver.split("[.]");
		int major = Integer.valueOf(split[0]);
		int minor = Integer.valueOf(split[1]);
		int hotfix = Integer.valueOf(split[2]);
		int build = Integer.valueOf(split[3]);
		
		if(build < requiresBuild) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        		public void run() {
        			Log.Error(plugin, String.format("HavenBags requires ValorlessUtils build %s or newer, found %s. (%s)", requiresBuild, build, ver));
        			Log.Error(plugin, "https://www.spigotmc.org/resources/valorlessutils.109586/");
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
		Log.Debug(plugin, "HavenBags Debugging Enabled!");
		
		// Check if a correct version of ValorlessUtils is in use, otherwise don't run the rest of the code.
		if(!ValorlessUtils()) return;
		
		ConfigValidation.Validate();
		
		if(PlaceholderAPIHook.Hook()) {
			papi = new PlaceholderAPI();
			papi.register();
		}
		ChestSortHook.Hook();
		PvPManagerHook.Hook();
		if(ProtocolLibHook.Hook()) {
			//WeightTooltipProtocollib.registerTooltipListener(this);
		}
		
		EssentialsHook.Hook();
		
		//OraxenHook.Hook();
		
		Log.Debug(plugin, "[DI-1] " +Long.toString(System.currentTimeMillis() / 1000L));
		
		translator = new Translator(config.GetString("language"));
		
		ValidateSizeTextures();
		
		// Config-Version checks
		BagConversion.check(config); // Config 1 -> 2
		//TimeTableConversion.check(); Would've been Config 2 -> 3
		try {
			DataConversion.check(config);// Config 3 -> 4
		} catch (InvalidConfigurationException e) {} 
		TokenConfigConversion.check(config); // Config 4 -> 5
		ConfigRestructure.check(config); // Config 5 -> 6
        
		
		BagData.Initiate();
		
		AutoPickup.Initiate();
		
		CustomBags.Initiate();
		
		EtherealBags.init();

		RegisterListeners();
				
		RegisterCommands();

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
        
        
    	//activeBags.clear();
    }

	@Override
    public void onDisable() {		
    	CloseBags();
    	if(!BackBag.tracking.isEmpty()) {
    		for(Player player : BackBag.tracking.keySet()) {
    			BackBag.tracking.get(player).despawn();
    		}
    	}
    	if(BackBag.cleantask != null) BackBag.cleantask.cancel();
    	BagData.SaveData(true);
    	BagData.Shutdown();
    	Crafting.RemoveRecipes();
    	BagEffects.shutdown();
    	UpgradeGUI.OpenGUIs.CloseAll();
    	SkinCache.shutdown();
    	EtherealBags.shutdown();
    }
    
    public static void CloseBags() {
    	if(BagData.GetOpenBags().size() != 0) {
    		Log.Info(plugin, "Closing all open bags.");
    		try {
    			for(Data bag : BagData.GetOpenBags()) {
    				bag.getGui().Close(true);
    			}
    		} catch (Exception e) {
    		
    		}
    	}
    	//activeBags.clear();
    }
    
    protected void RegisterCommands() {
    	for (int i = 0; i < commands.length; i++) {
    		Log.Debug(plugin, "[DI-20] Registering Command: " + commands[i]);
    		getCommand(commands[i]).setExecutor(new CommandListener());
    		getCommand(commands[i]).setTabCompleter(new TabCompletion());
    	}
    }
    
	protected void RegisterListeners() {
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
    
    void ValidateSizeTextures() {
    	if(config.GetBool("bag-textures.enabled")) {
    		boolean c = false;
    		for(int s = 9; s <= 54; s += 9) {
    			if(Utils.IsStringNullOrEmpty(Main.config.GetString("bag-textures.size-" + s))){
    				config.Set("bag-textures.size-" + s, config.GetString("bag.texture"));
    				c = true;
    			}
			}
    		for(int s = 9; s <= 54; s += 9) {
    			if(Utils.IsStringNullOrEmpty(Main.config.GetString("bag-textures.size-ownerless-" + s))){
    				config.Set("bag-textures.size-ownerless-" + s, config.GetString("bag.texture"));
    				c = true;
    			}
			}
    		
    		if(c) config.SaveConfig();
    	}
    }
}
