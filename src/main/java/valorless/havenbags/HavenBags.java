package valorless.havenbags;

import valorless.valorlessutils.ValorlessUtils.*;
import valorless.valorlessutils.config.Config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HavenBags extends JavaPlugin implements Listener {
	public static JavaPlugin plugin;
	public static Config config;
	public static List<ActiveBag> activeBags = new ArrayList<ActiveBag>();
	Boolean uptodate = true;
	String newupdate = null;
	
	public String[] commands = {
    		"havenbags", "bags", "bag",
    };
	
	public void onLoad() {
		plugin = this;
		config = new Config(this, "config.yml");
		CommandListener.plugin = this;
		BagListener.plugin = this;
		
		Lang.lang = new Config(this, "lang.yml");
	}
	
	@Override
    public void onEnable() {
		
        // All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 18791; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        //metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
		
		Log.Debug(plugin, "HavenBags Debugging Enabled!");
		
		config.AddValidationEntry("debug", false);
		config.AddValidationEntry("check-updates", true);
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
		config.AddValidationEntry("blacklist", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

		{ add("world_name"); add("world_name_nether"); add("another_world"); }} );
		Log.Debug(plugin, "Validating config.yml");
		config.Validate();
		
		Lang.lang.AddValidationEntry("prefix", "&7[&aHaven&bBags&7] &r");
		Lang.lang.AddValidationEntry("bag-load-error", "&cBag failed to load.\nPlease notify staff.");
		Lang.lang.AddValidationEntry("bag-rename", "&fRenamed bag to %s.");
		Lang.lang.AddValidationEntry("bag-rename-reset", "&fReset bag''s name.");
		Lang.lang.AddValidationEntry("bag-cannot-rename", "&cYou can only rename bags.");
		Lang.lang.AddValidationEntry("bag-cannot-use", "&cYou cannot use this bag.");
		Lang.lang.AddValidationEntry("bag-does-not-exist", "&cThis bag does not exist.");
		Lang.lang.AddValidationEntry("inventory-full", "&cInventory full, dropping bag on the ground!");
		
		// Admin Lang
		//Lang.lang.AddValidationEntry("bag-create", ""); //unsure wtf this was for
		Lang.lang.AddValidationEntry("bag-not-found", "&cNo bag found with that UUID.");
		Lang.lang.AddValidationEntry("bag-size-error", "&cSize cannot be over 6 rows.");
		Lang.lang.AddValidationEntry("bag-ownerless-no-size", "&cOwnerless bag must have a size.");
		Lang.lang.AddValidationEntry("bag-given", "&aYou''ve been given an %s!");
		Lang.lang.AddValidationEntry("number-conversion-error", "&cCannot convert ''%s'' to a number!");
		Lang.lang.AddValidationEntry("player-no-bags", "&cPlayer ''%s'' has no bags.");
		Lang.lang.AddValidationEntry("bags-of", "Bags of %s:");
		
		// BagItem Lang
		Lang.lang.AddValidationEntry("bag-bound-name", "&a%s''s Bag");
		Lang.lang.AddValidationEntry("bag-unbound-name", "&aUnbound Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-unused", "&aUnused Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-used", "&aBag");
		Lang.lang.AddValidationEntry("bound-to", "&7Bound to %s");
		Lang.lang.AddValidationEntry("bag-size", "&7Size: %s");
		Lang.lang.AddValidationEntry("bag-content-title", "&7Content:");
		Lang.lang.AddValidationEntry("bag-content-preview-size", 5);
		Lang.lang.AddValidationEntry("bag-content-item", "&7%s");
		Lang.lang.AddValidationEntry("bag-content-item-amount", "&7%s &7x%s");
		Lang.lang.AddValidationEntry("bag-content-and-more", "&7And more..");
		
		Log.Debug(plugin, "Validating lang.yml");
		Lang.lang.Validate();

		Log.Debug(plugin, "Registering PlacementListener");
		getServer().getPluginManager().registerEvents(new PlacementBlocker(), this);
		Log.Debug(plugin, "Registering BagDamagePrevention");
		getServer().getPluginManager().registerEvents(new BagDamagePrevention(), this);
		Log.Debug(plugin, "Registering BagListener");
		getServer().getPluginManager().registerEvents(new BagListener(), this);
		
		Bukkit.getPluginManager().registerEvents(this, this);
				
		RegisterCommands();

		getServer().getPluginManager().registerEvents(new CustomRecipe(), this);
		CustomRecipe.PrepareRecipes();
		
		if(config.GetBool("check-updates") == true) {
			new UpdateChecker(this, 110420).getVersion(version -> {

				newupdate = version;

				if (getDescription().getVersion().equals(version)) {
					Log.Info(plugin, "Checking for updates..");
				} else {
					Log.Info(plugin, "Checking for updates..");
					Log.Warning(plugin, String.format("An update has been found! (v%s, you are on v%s) \n", version, getDescription().getVersion()) + 
							"This could be bug fixes or additional features.\n" + 
							"Please update HavenBags at https://www.spigotmc.org/resources/110420/");
					uptodate = false;

				}
			});
		}
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


	@EventHandler
	public void UpdateNotification(PlayerJoinEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    public void run() {
		    	if (config.GetBool("check-updates") && e.getPlayer().isOp() && uptodate == false) {
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&7[&aHaven&bBags&7] " + "&fAn update has been found.\nPlease download version&a " + newupdate
						+ ", &fyou are on version&a " + getDescription().getVersion() + "!"
						));
				}
		    }
		}, 5L);
		
	}
}
