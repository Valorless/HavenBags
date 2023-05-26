package valorless.havenbags;

import valorless.valorlessutils.ValorlessUtils.*;
import valorless.valorlessutils.config.Config;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class HavenBags extends JavaPlugin implements Listener {
	public static JavaPlugin plugin;
	public static Config config;
	
	public String[] commands = {
    		"havenbags", "bags",
    };
	
	public void onLoad() {
		plugin = this;
		config = new Config(this, "config.yml");
		CommandListener.plugin = this;
		BagListener.plugin = this;
		//PickupPrevention.plugin = this;
		//BagDamagePrevention.plugin = this;
		
		Lang.lang = new Config(this, "lang.yml");
	}
	
	@Override
    public void onEnable() {
		Log.Debug(plugin, "HavenBags Debugging Enabled!");
		CommandListener.onEnable();
		//config.AddValidationEntry("debug", false);
		config.AddValidationEntry("bag-texture", "");
		Log.Debug(plugin, "Validating config.yml");
		config.Validate();
		
		Lang.lang.AddValidationEntry("prefix", "&7[&aHaven&bBags&7] &r");
		Lang.lang.AddValidationEntry("bag-load-error", "&cBag failed to load.\\nPlease notify staff.");
		Lang.lang.AddValidationEntry("bag-in-bag-error", "&cBags cannot be placed inside bags.");
		Lang.lang.AddValidationEntry("bag-rename", "&fRenamed bag to %s.");
		Lang.lang.AddValidationEntry("bag-given", "&aYou''ve been given an %s!");
		Lang.lang.AddValidationEntry("bag-cannot-use", "&cYou cannot use this bag.");
		Lang.lang.AddValidationEntry("bag-does-not-exist", "&cThis bag does not exist.");
		
		// Admin Lang
		//Lang.lang.AddValidationEntry("bag-create", ""); //unsure wtf this was for
		Lang.lang.AddValidationEntry("bag-not-found", "&cNo bag found with that UUID.");
		Lang.lang.AddValidationEntry("bag-size-error", "&cSize must be to the power of 9. Max size 54. (6 rows)");
		Lang.lang.AddValidationEntry("bag-ownerless-no-size", "&cOwnerless bag must have a size.");
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

		Log.Debug(plugin, "Registering CommandListener");
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
		Log.Debug(plugin, "Registering BagListener");
		getServer().getPluginManager().registerEvents(new BagListener(), this);
		Log.Debug(plugin, "Registering PlacementListener");
		getServer().getPluginManager().registerEvents(new PlacementBlocker(), this);
		//getServer().getPluginManager().registerEvents(new PickupPrevention(), this);
		//getServer().getPluginManager().registerEvents(new BagDamagePrevention(), this);
		
		RegisterCommands();
    }
    
    @Override
    public void onDisable() {
    }
    
    public void RegisterCommands() {
    	for (int i = 0; i < commands.length; i++) {
    		Log.Debug(plugin, "Registering Command: " + commands[i]);
    		getCommand(commands[i]).setExecutor(this);
    	}
    }
    
    //public static Bag NewBag(Player owner, Bag.BagSize size) {
    //	return new Bag(owner, size);
    //}
    
    //public static Bag LoadBag(String uuid) {
    //	
    //}
}
