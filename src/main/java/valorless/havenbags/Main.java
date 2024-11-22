package valorless.havenbags;

import valorless.havenbags.hooks.*;
import valorless.havenbags.prevention.*;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin implements Listener {
	public enum ServerVersion { NULL, v1_17, v1_17_1, v1_18, v1_18_1, v1_18_2, v1_19, v1_19_1, v1_19_2, v1_19_3, v1_19_4, v1_20, v1_20_1, v1_20_3, v1_20_4, v1_20_5, v1_20_6, v1_21, v1_21_1 }
	public static JavaPlugin plugin;
	public static Config config;
	//public static Config timeTable;
	public static Config weight;
	public static Config blacklist;
	public static Config plugins;
	public static Config textures;
	public static List<ActiveBag> activeBags = new ArrayList<ActiveBag>();
	Boolean uptodate = true;
	int newupdate = 9999999;
	String newVersion = null;
	public static Translator translator;
	public static ServerVersion server;
	
	/**
	 * Compares two ServerVersion enums to determine their order based on their declaration.
	 *
	 * @param version    The ServerVersion to compare.
	 * @param compareTo  The ServerVersion to compare against.
	 * @return          A negative integer, zero, or a positive integer as the first argument
	 *                  is less than, equal to, or greater than the second argument.
	 */
	public static int VersionCompare(ServerVersion version, ServerVersion compareTo) {
	    if (version.ordinal() < compareTo.ordinal()) {
	        return -1;  // version is less than compareTo
	    } else if (version.ordinal() > compareTo.ordinal()) {
	        return 1;   // version is greater than compareTo
	    } else {
	        return 0;   // version is equal to compareTo
	    }
	}
	
	public String[] commands = {
    		"havenbags", "bags", "bag",
    };
	
	public void onLoad() {
		plugin = this;
		Log.Debug(plugin, Bukkit.getVersion());
		Log.Debug(plugin, Bukkit.getBukkitVersion());
		ResolveVersion();
		config = new Config(this, "config.yml");
		Lang.lang = new Config(this, "lang.yml");
		//timeTable = new Config(this, "timetable.yml");
		AutoPickup.filter = new Config(this, "filtering.yml");
		weight = new Config(this, "weight.yml");
		blacklist = new Config(this, "blacklist.yml");;
		plugins = new Config(this, "plugins.yml");
		textures = new Config(this, "textures.yml");
	}
	
	@SuppressWarnings("unused")
	boolean ValorlessUtils() {
		Log.Debug(plugin, "[DI-0] Checking ValorlessUtils");
		
		int requiresBuild = 237;
		
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
		
		PlaceholderAPIHook.Hook();
		ChestSortHook.Hook();
		PvPManagerHook.Hook();
		//OraxenHook.Hook();
		
		Log.Debug(plugin, "[DI-1] " +Long.toString(System.currentTimeMillis() / 1000L));
		
		config.AddValidationEntry("debug", false);
		config.AddValidationEntry("config-version", 2);
		config.AddValidationEntry("check-updates", true);
		config.AddValidationEntry("auto-save-interval", 1200);
		config.AddValidationEntry("auto-save-message", true);
		config.AddValidationEntry("language", "en_us");
		config.AddValidationEntry("bag-type", "HEAD");
		config.AddValidationEntry("bag-material", "ENDER_CHEST");
		config.AddValidationEntry("bag-custom-model-data", 0);
		config.AddValidationEntry("bag-custom-model-datas.enabled", false);
		config.AddValidationEntry("bag-custom-model-datas.size-9", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-18", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-27", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-36", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-45", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-54", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-ownerless-9", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-ownerless-18", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-ownerless-27", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-ownerless-36", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-ownerless-45", 0);
		config.AddValidationEntry("bag-custom-model-datas.size-ownerless-54", 0);
		config.AddValidationEntry("bag-texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=");
		config.AddValidationEntry("bag-textures.enabled", false);
		config.AddValidationEntry("bag-textures.size-9", "");
		config.AddValidationEntry("bag-textures.size-18", "");
		config.AddValidationEntry("bag-textures.size-27", "");
		config.AddValidationEntry("bag-textures.size-36", "");
		config.AddValidationEntry("bag-textures.size-45", "");
		config.AddValidationEntry("bag-textures.size-54", "");
		config.AddValidationEntry("bag-textures.size-ownerless-9", "");
		config.AddValidationEntry("bag-textures.size-ownerless-18", "");
		config.AddValidationEntry("bag-textures.size-ownerless-27", "");
		config.AddValidationEntry("bag-textures.size-ownerless-36", "");
		config.AddValidationEntry("bag-textures.size-ownerless-45", "");
		config.AddValidationEntry("bag-textures.size-ownerless-54", "");
		
		config.AddValidationEntry("open-sound", "ITEM_BUNDLE_INSERT");
		config.AddValidationEntry("open-volume", 1);
		config.AddValidationEntry("open-pitch", 1);
		config.AddValidationEntry("close-sound", "ITEM_BUNDLE_DROP_CONTENTS");
		config.AddValidationEntry("close-volume", 1);
		config.AddValidationEntry("close-pitch", 1);
		config.AddValidationEntry("inventory-full-sound", "ENTITY_VILLAGER_NO");
		config.AddValidationEntry("inventory-full-volume", 1);
		config.AddValidationEntry("inventory-full-pitch", 1);
		config.AddValidationEntry("max-bags", 0);
		config.AddValidationEntry("protect-bags", true);
		config.AddValidationEntry("protect-bags-players", false);
		config.AddValidationEntry("bags-in-bags", false);
		config.AddValidationEntry("bags-in-shulkers", true);
		config.AddValidationEntry("keep-bags", true);
		config.AddValidationEntry("old-help-menu", false);
		config.AddValidationEntry("auto-pickup", true);
		config.AddValidationEntry("auto-pickup-sound", "ENTITY_ITEM_PICKUP");
		config.AddValidationEntry("auto-pickup-volume", 0.8);
		config.AddValidationEntry("auto-pickup-pitch-min", 1.05);
		config.AddValidationEntry("auto-pickup-pitch-max", 1.25);
		config.AddValidationEntry("auto-pickup-inventory.enabled", false);
		config.AddValidationEntry("auto-pickup-inventory.events.onBlockBreak", true);
		config.AddValidationEntry("auto-pickup-inventory.events.onItemPickup", true);
		config.AddValidationEntry("trusting", true);
		config.AddValidationEntry("upgrades.enabled", false);
		config.AddValidationEntry("upgrades.keep-texture", false);
		config.AddValidationEntry("upgrades.from-9-to-18", "EMERALD:5:90000");
		config.AddValidationEntry("upgrades.from-18-to-27", "DIAMOND:10:90001");
		config.AddValidationEntry("upgrades.from-27-to-36", "NETHERITE_INGOT:1:90002");
		config.AddValidationEntry("upgrades.from-36-to-45", "EMERALD:5:NETHERITE_BLOCK:1:90003");
		config.AddValidationEntry("upgrades.from-45-to-54", "END_CRYSTAL:1");
		config.AddValidationEntry("skin-token.display-name", "&aSkin Token");
		config.AddValidationEntry("skin-token.material", "PLAYER_HEAD");
		config.AddValidationEntry("skin-token.custommodeldata", 0);
		config.AddValidationEntry("skin-token.lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
		{ add("&7Combine with a bag in an anvil to apply."); add("&7Skin: &e%skin%"); }} );
		
		
		config.AddValidationEntry("blacklist", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
		{ add("world_name"); add("world_name_nether"); add("another_world"); }} );
		config.AddValidationEntry("allowed-containers", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
		{ add("CHEST"); add("ENDER_CHEST"); add("BARREL"); add("SHULKER_BOX"); add("MERCHANT"); }} );
		Log.Debug(plugin, "[DI-2] Validating config.yml");
		config.Validate();
		
		Lang.lang.AddValidationEntry("prefix", "&7[&aHaven&bBags&7] &r");
		Lang.lang.AddValidationEntry("malformed-command", "&cUnknown command, are you missing some parameters?");
		Lang.lang.AddValidationEntry("feature-disabled", "&cSorry, this feature is disabled.");
		Lang.lang.AddValidationEntry("bag-load-error", "&cBag failed to load.\nPlease notify staff.");
		Lang.lang.AddValidationEntry("bag-rename", "&fRenamed bag to %name%.");
		Lang.lang.AddValidationEntry("bag-rename-reset", "&fReset bag's name.");
		Lang.lang.AddValidationEntry("bag-cannot-rename", "&cYou can only rename bags.");
		Lang.lang.AddValidationEntry("bag-cannot-use", "&cYou cannot use this bag.");
		Lang.lang.AddValidationEntry("bag-does-not-exist", "&cThis bag does not exist.");
		Lang.lang.AddValidationEntry("inventory-full", "&cInventory full, dropping bag on the ground!");
		Lang.lang.AddValidationEntry("bag-already-open", "&cThis bag is already open!");
		Lang.lang.AddValidationEntry("max-bags", "&cSorry, you cannot make any more bags.");
		Lang.lang.AddValidationEntry("bag-in-bag-error", "&cBags cannot be put inside other bags.");
		Lang.lang.AddValidationEntry("bag-in-shulker-error", "&cBags cannot be put inside shulker boxes.");
		Lang.lang.AddValidationEntry("item-blacklisted", "&cSorry, this item cannot go into bags.");
		Lang.lang.AddValidationEntry("player-trusted", "&aAdded %trusted% as trusted.");
		Lang.lang.AddValidationEntry("player-untrusted", "&eRemoved %trusted% as trusted.");
		
		// Admin Lang
		//Lang.lang.AddValidationEntry("bag-create", ""); //unsure wtf this was for
		Lang.lang.AddValidationEntry("player-no-exist", "&cNo bags found for this player.");
		Lang.lang.AddValidationEntry("bag-not-found", "&cNo bag found with that UUID.");
		Lang.lang.AddValidationEntry("bag-ownerless-no-size", "&cOwnerless bag must have a size.");
		Lang.lang.AddValidationEntry("bag-given", "&aYou've been given an %name%!");
		Lang.lang.AddValidationEntry("number-conversion-error", "&cCannot convert '%value%' to a number!");
		Lang.lang.AddValidationEntry("player-no-bags", "&cPlayer '%player%' has no bags.");
		Lang.lang.AddValidationEntry("bags-of", "Bags of %player%:");
		
		// Bag GUI
		Lang.lang.AddValidationEntry("bag-inventory-title", "");
		Lang.lang.AddValidationEntry("per-size-title", false);
		Lang.lang.AddValidationEntry("bag-inventory-title-9", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-18", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-27", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-36", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-45", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-54", "");
		
		// BagItem Lang
		Lang.lang.AddValidationEntry("bag-bound-name", "&a%player%'s Bag");
		Lang.lang.AddValidationEntry("bag-unbound-name", "&aUnbound Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-unused", "&aUnused Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-used", "&aBag");
		Lang.lang.AddValidationEntry("bag-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&fA well crafted bag, suited for carrying stuff."); 
				add("&8ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ");
				}
			} 
		);
		Lang.lang.AddValidationEntry("bag-lore-add", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("%bound-to%"); 
				add("%bag-size%"); 
				add("%bag-auto-pickup%"); 
				add("%bag-trusted%"); 
				add("%bag-weight%"); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("bound-to", "&7Bound to %owner%");
		Lang.lang.AddValidationEntry("bag-size", "&7Size: %size%");
		Lang.lang.AddValidationEntry("show-bag-content", true);
		Lang.lang.AddValidationEntry("bag-content-title", "&7Content:");
		Lang.lang.AddValidationEntry("bag-content-preview-size", 5);
		Lang.lang.AddValidationEntry("bag-content-item", "&7%item%");
		Lang.lang.AddValidationEntry("bag-content-item-amount", "&7%item% &7x%amount%");
		Lang.lang.AddValidationEntry("bag-content-and-more", "&7And more..");
		Lang.lang.AddValidationEntry("bag-auto-pickup", "&7Auto Loot: %filter%");
		Lang.lang.AddValidationEntry("bag-trusted", "&7Trusted: %trusted%");
		
		// Admin GUI
		Lang.lang.AddValidationEntry("too-many-bags", "&cThis player has over 53 bags.\\nPlease restore their bags through &e/bags restore&c!");
		Lang.lang.AddValidationEntry("gui-main", "&aHaven&bBags &rGUI");
		Lang.lang.AddValidationEntry("gui-create", "&aHaven&bBags &eCreation GUI");
		Lang.lang.AddValidationEntry("gui-restore", "&aHaven&bBags &bRestoration GUI");
		Lang.lang.AddValidationEntry("gui-preview", "&aHaven&bBags &dPreview GUI");
		Lang.lang.AddValidationEntry("gui-delete", "&aHaven&bBags &cDeletion GUI");
		Lang.lang.AddValidationEntry("gui-confirm", "&aHaven&bBags &4&lDELETE&r this bag?");
		Lang.lang.AddValidationEntry("main-create", "&aBag Creation");
		Lang.lang.AddValidationEntry("main-create-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Create bags easy."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-restore", "&bBag Restoration");
		Lang.lang.AddValidationEntry("main-restore-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Restore bags of online players.");
				add("");
				add("&7Only the basic bag with it's content will be restored."); 
				add("&7Things such as weight and auto-pickup filter, will not be restored."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-preview", "&dBag Preview");
		Lang.lang.AddValidationEntry("main-preview-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Preview bags of online players."); 
				add("&7You can take items from the preview,"); 
				add("&7without affecting the real bag."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-delete", "&4Bag Deletion");
		Lang.lang.AddValidationEntry("main-delete-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Delete bags of online players."); 
				add("&c&oDeleted bags cannot be restored!"); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("confirm-cancel", "&4Cancel");
		Lang.lang.AddValidationEntry("confirm-cancel-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Cancel deletion of this bag."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("confirm-confirm", "&aConfirm");
		Lang.lang.AddValidationEntry("confirm-confirm-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Confirm deletion of this bag."); 
				add("&7This cannot be undone."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-info", "&eInformation");
		Lang.lang.AddValidationEntry("main-info-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				 add("&7You can also restore bags of offline players,");
				 add("&7by using &e/bags gui restore <player-uuid>&7.");
				}
			} 
		);
		Lang.lang.AddValidationEntry("return", "&eReturn");
		Lang.lang.AddValidationEntry("return-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Go back."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("page", "&fPage: %page%");
		Lang.lang.AddValidationEntry("next-page", "&aNext Page");
		Lang.lang.AddValidationEntry("prev-page", "&cPrevious Page");

		Log.Debug(plugin, "[DI-3] Validating lang.yml");
		Lang.lang.Validate();
		
		weight.AddValidationEntry("enabled", false);
		weight.AddValidationEntry("weight-limit", 3500);
		weight.AddValidationEntry("weight-per-size", true);
		weight.AddValidationEntry("weight-size-9", 1300);
		weight.AddValidationEntry("weight-size-18", 2400);
		weight.AddValidationEntry("weight-size-27", 3500);
		weight.AddValidationEntry("weight-size-36", 4600);
		weight.AddValidationEntry("weight-size-45", 5700);
		weight.AddValidationEntry("weight-size-54", 6800);
		weight.AddValidationEntry("over-encumber.enabled", false);
		weight.AddValidationEntry("over-encumber.percent", 80);
		weight.AddValidationEntry("over-encumber.effects", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("SLOWNESS:1"); 
				}
			} 
		);
		weight.AddValidationEntry("over-encumber.message", "&cYou feel over-encumbered.");
		weight.AddValidationEntry("over-encumber.not", "&aYou feel lighter.");
		weight.AddValidationEntry("weight-lore", "&7Weight: %bar% &7%percent% (%weight%/%limit%)");
		weight.AddValidationEntry("bar-length", 10);
		weight.AddValidationEntry("bar-style", "⬜");
		weight.AddValidationEntry("fill-style", "⬛");
		weight.AddValidationEntry("bar-start", "[");
		weight.AddValidationEntry("bar-end", "]");
		weight.AddValidationEntry("bar-color", "&7");
		weight.AddValidationEntry("fill-color", "&e");
		weight.AddValidationEntry("weight-text-pickup", true);
		weight.AddValidationEntry("bag-cant-carry", "&cCannot carry any more items.\n%item% weighs %weight%, but you can only carry %remaining%.");
		weight.AddValidationEntry("enabled", false);
		Log.Debug(plugin, "[DI-4] Validating weight.yml");
		weight.Validate();
		
		blacklist.AddValidationEntry("enabled", false);
		blacklist.AddValidationEntry("use-as-whitelist", false);
		blacklist.AddValidationEntry("blacklist.materials", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("DRAGON_EGG"); 
				add("SPAWNER"); 
				}
			} 
		);
		blacklist.AddValidationEntry("blacklist.displayname", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("Domination Sword"); 
				add("Decapitation Shovel"); 
				add("Anti-air Axe"); 
				add("Cheese");
				}
			} 
		);
		blacklist.AddValidationEntry("blacklist.custommodeldata", new ArrayList<Integer>() {
			private static final long serialVersionUID = 1L; { 
				add(1234567); 
				add(70274); 
				}
			} 
		);
		blacklist.AddValidationEntry("blacklist.nbt", new ArrayList<String>());
		Log.Debug(plugin, "[DI-5] Validating blacklist.yml");
		blacklist.Validate();
		
		plugins.AddValidationEntry("plugins.PlaceholderAPI.enabled", true);
		plugins.AddValidationEntry("plugins.ChestSort.enabled", true);
		plugins.AddValidationEntry("plugins.PvPManager.enabled", true);
		plugins.AddValidationEntry("plugins.PvPManager.tagged", true);
		plugins.AddValidationEntry("plugins.PvPManager.pvp", true);
		plugins.AddValidationEntry("plugins.PvPManager.message", "&cYou cannot use this while in PvP.");
		//plugins.AddValidationEntry("plugins.Oraxen.enabled", true);
		
		plugins.AddValidationEntry("mods.HavenBagsPreview.enabled", true);
		Log.Debug(plugin, "[DI-6] Validating plugins.yml");
		plugins.Validate();
		
		translator = new Translator(config.GetString("language"));
		
		ValidateSizeTextures();
		
		// Config-Version checks
        BagConversion();
        //TimeTable();
        try {
			DataConversion();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
        
		
		BagData.Initiate();
		
		AutoPickup.Initiate();

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
        
        
    	activeBags.clear();
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void onDisable() {
    	CloseBags();
    	BagData.SaveData();
    	Crafting.RemoveRecipes();
    }
    
    public static void CloseBags() {
    	if(activeBags.size() != 0) {
    		Log.Info(plugin, "Closing all open bags.");
    		try {
    			for(ActiveBag bag : activeBags) {
    				bag.gui.Close(true);
    				activeBags.remove(bag);
    			}
    		} catch (Exception e) {
    		
    		}
    	}
    	activeBags.clear();
    }
    
    protected void RegisterCommands() {
    	for (int i = 0; i < commands.length; i++) {
    		Log.Debug(plugin, "[DI-20] Registering Command: " + commands[i]);
    		getCommand(commands[i]).setExecutor(new CommandListener());
    		getCommand(commands[i]).setTabCompleter(new TabCompletion());
    	}
    }
    
    @SuppressWarnings("deprecation")
	protected void RegisterListeners() {
    	Log.Debug(plugin, "[DI-7] Registering PlacementListener");
		getServer().getPluginManager().registerEvents(new PlacementBlocker(), this);
		Log.Debug(plugin, "[DI-8] Registering BagDamagePrevention");
		getServer().getPluginManager().registerEvents(new BagDamagePrevention(), this);
		Log.Debug(plugin, "[DI-9] Registering BagListener");
		getServer().getPluginManager().registerEvents(new BagListener(), this);
		Log.Debug(plugin, "[DI-10] Registering CloneListener");
		getServer().getPluginManager().registerEvents(new CloneListener(), this);
		Log.Debug(plugin, "[DI-11] Registering InventoryListener");
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		Log.Debug(plugin, "[DI-12] Registering PickupPrevention");
		getServer().getPluginManager().registerEvents(new PickupPrevention(), this);
		Log.Debug(plugin, "[DI-13] Registering CraftPrevention");
		getServer().getPluginManager().registerEvents(new CraftPrevention(), this);
		Log.Debug(plugin, "[DI-14] Registering EquipPrevention");
		getServer().getPluginManager().registerEvents(new EquipPrevention(), this);
		
		Log.Debug(plugin, "[DI-15] Registering Crafting");
		getServer().getPluginManager().registerEvents(new Crafting(), this);
		Crafting.PrepareRecipes();

		Log.Debug(plugin, "[DI-16] Registering AutoPickup");
		getServer().getPluginManager().registerEvents(new AutoPickup(), this);

		Log.Debug(plugin, "[DI-17] Registering Encumbering");
		getServer().getPluginManager().registerEvents(new Encumbering(), this);
		Encumbering.Reload();
		
		Log.Debug(plugin, "[DI-18] Registering BagUpgrade");
		getServer().getPluginManager().registerEvents(new BagUpgrade(), this);
		
		Log.Debug(plugin, "[DI-19] Registering BagSkin");
		getServer().getPluginManager().registerEvents(new BagSkin(), this);
		
		Log.Debug(plugin, "[DI-211] Registering MinepacksBagRestore");
		getServer().getPluginManager().registerEvents(new MinepacksBagRestore(), this);
		
		Log.Debug(plugin, "[DI-220] Registering EpicBackpacksBagRestore");
		getServer().getPluginManager().registerEvents(new EpicBackpacksBagRestore(), this);
		
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
    
    void ResolveVersion() {
    	try {
    		String v = Bukkit.getBukkitVersion().split("-")[0];
    		server = ServerVersion.valueOf("v" + v.replace(".", "_"));
    		//Log.Debug(plugin, server.toString());
    	} catch (Exception e) {
    		server = ServerVersion.NULL;
    		Log.Error(plugin, "Failed to resolve server version, some functions might not work correctly.");
    	}
    }
    
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
