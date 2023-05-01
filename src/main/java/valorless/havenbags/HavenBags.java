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
		CommandListener.onEnable();
		//config.AddValidationEntry("debug", false);
		config.AddValidationEntry("bag-texture", "");
		
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
		getServer().getPluginManager().registerEvents(new BagListener(), this);
		getServer().getPluginManager().registerEvents(new PlacementBlocker(), this);
		//getServer().getPluginManager().registerEvents(new PickupPrevention(), this);
		//getServer().getPluginManager().registerEvents(new BagDamagePrevention(), this);
		
		if(config.GetBool("debug")) {
			Log.Info(this, "Debugging enabled.");
		}
			
		if(config.GetBool("debug")) {
			Log.Info(this, "§aEnabled!");
		}
		
		RegisterCommands();
    }
    
    @Override
    public void onDisable() {
    	if(config.GetBool("debug")) {
    		Log.Info(this, "§cDisabled!");
    	}
    }
    
    public void RegisterCommands() {
    	for (int i = 0; i < commands.length; i++) {
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
