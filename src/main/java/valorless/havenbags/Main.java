package valorless.havenbags;

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
import valorless.havenbags.features.WeightTooltipProtocollib;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.havenbags.hooks.*;
import valorless.havenbags.prevention.*;
import valorless.havenbags.utils.NoteBlockUtils;
import valorless.havenbags.utils.UpdateChecker;
import valorless.valorlessutils.Metrics;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.translate.Translator;
import valorless.valorlessutils.utils.Utils;
import valorless.valorlessutils.uuid.UUIDFetcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;

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
		
		int requiresBuild = 295; // The build number of ValorlessUtils that is required for HavenBags to run.
		
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
        BagConversion(); // Config 1 -> 2
        //TimeTable(); Would've been Config 2 -> 3
        try {
			DataConversion(); // Config 3 -> 4
		} catch (InvalidConfigurationException e) {
			//e.printStackTrace();
		}
        TokenConfigConversion(); // Config 4 -> 5
		
        
		
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
    
    void DataConversion() throws InvalidConfigurationException {
    	if(config.GetInt("config-version") < 4) {
    		Log.Warning(plugin, "Old data storage found, updating bag data!");
    		//Log.Error(plugin, "Debugging. Old data files are not removed.");
    		Log.Error(plugin, "Old data files are not removed, in case of failure.");
    		config.Set("config-version", 4);
    		config.SaveConfig();
    		int converted = 0;
    		int failed = 0;
    		
    		List<String> owners	= BagData.GetBagOwners();
    		for(String owner : owners) {
    			List<String> bags = GetBags(owner);
    			for(String bag : bags) {
    				bag = bag.replace(".json", "");
    				String path = String.format("%s/bags/%s/%s.json", plugin.getDataFolder(), owner, bag);
    				Path conf = Paths.get(String.format("%s/bags/%s/%s.yml", plugin.getDataFolder(), owner, bag));
    				try {
    					List<ItemStack> cont = JsonUtils.fromJson(Files.readString(Paths.get(path)));
    	    			@SuppressWarnings("unused")
						List<String> lines = Arrays.asList(JsonUtils.toPrettyJson(cont));

    	    			try {
    	    				//Files.write(conf, lines, StandardCharsets.UTF_8);
    	    				//Files.createFile(conf);
    	    				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(conf.toString()), StandardCharsets.UTF_8);
    	    				//writer.write(String.format("uuid: %s", bag));
    	    				writer.close();
    	    				
    	    			}catch(IOException e){
    	    				//e.printStackTrace();
    	    			}finally {
    	    				try {
    	    					Config bagData = new Config(plugin, String.format("/bags/%s/%s.yml", owner, bag));
    	    					bagData.Set("uuid", bag);
    	    					bagData.Set("owner", owner);
    	    					bagData.Set("creator", "null");
    	    					bagData.Set("size", cont.size());
    	    					bagData.Set("texture", Main.config.Get("bag-texture"));
    	    					bagData.Set("custommodeldata", 0);
    	    					bagData.Set("trusted", new ArrayList<String>());
    	    					bagData.Set("auto-pickup", "null");
    	    					bagData.Set("weight-max", 0);
    	    					bagData.Set("content", JsonUtils.toJson(cont).replace("'", "◊"));
    	    					bagData.SaveConfig();
    	    					converted++;
    	    				}catch(Exception E) {
    	    					//E.printStackTrace();
    	    					// Error: Top level is not a Map.
    	    					// Unsure why this is thrown, but the file is converted successfully without issues..
    	    					//Log.Error(plugin, String.format("Something went wrong while converting %s!.", String.format("/bags/%s/%s", owner, bag)));
    	    				}
    	    			}
    				} catch(Exception e) {
    					failed++;
    		    		//config.Set("config-version", 3);
    		    		//config.SaveConfig();
    					e.printStackTrace();
    					Log.Error(plugin, String.format("Failed to convert %s, may require manual update.", String.format("/bags/%s/%s.json", owner, bag)));
    				}
    			}
    		}
    		Log.Info(plugin, String.format("Converted %s Data Files!", converted));
    		Log.Info(plugin, String.format("Failed: %s.", failed));
    	}
    }
    
	void TokenConfigConversion() {
		if(config.GetInt("config-version") < 5) {
    		Log.Warning(plugin, "Old configuration found, updating configs!");
    		config.Set("config-version", 5);
    		config.SaveConfig();
    		
    		if(config.HasKey("skin-token.material")) {
				config.Set("token.skin.material", config.Get("skin-token.material"));
				config.Set("token.skin.custommodeldata", config.Get("skin-token.custommodeldata"));
				config.Set("token.skin.displayname", config.Get("skin-token.display-name"));
				config.Set("token.skin.lore", config.Get("skin-token.lore"));
				
				config.Set("skin-token.display-name", null);
				config.Set("skin-token.material", null);
				config.Set("skin-token.custommodeldata", null);
				config.Set("skin-token.lore", null);
				config.Set("skin-token", null);
				
				config.SaveConfig();
			}
    	}
	}
    
    List<String> GetBags(@NotNull String player){
		Log.Debug(Main.plugin, "[DI-21] " + player);
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), player)).listFiles())
					.filter(file -> !file.isDirectory())
					.filter(file -> !file.getName().contains(".yml"))
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < bags.size(); i++) {
				//Log.Debug(Main.plugin, bags.get(i));
				bags.set(i, bags.get(i).replace(".yml", ""));
			}
			return bags;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
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
    				config.Set("bag-textures.size-" + s, config.GetString("bag-texture"));
    				c = true;
    			}
			}
    		for(int s = 9; s <= 54; s += 9) {
    			if(Utils.IsStringNullOrEmpty(Main.config.GetString("bag-textures.size-ownerless-" + s))){
    				config.Set("bag-textures.size-ownerless-" + s, config.GetString("bag-texture"));
    				c = true;
    			}
			}
    		
    		if(c) config.SaveConfig();
    	}
    }
}
