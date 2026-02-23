package valorless.havenbags.prevention;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class BagDamagePrevention implements Listener{
	String Name = "§7[§aHaven§bBags§7]§r";
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-8] Registering BagDamagePrevention");
		Bukkit.getServer().getPluginManager().registerEvents(new BagDamagePrevention(), Main.plugin);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().getType() == EntityType.ITEM) {
	    // Log.Debug(HavenBags.plugin, "Dropped item: " + e.getEntity().getName());
	    	if(e.getEntity() instanceof Item dropped){
		    	//Log.Debug(HavenBags.plugin, "Dropped item can be Item.");
		    	//Item item = (Item)e.getEntity();
	    		ItemStack item = dropped.getItemStack();
	    		if(HavenBags.IsBag(item)) {
	    			if(Main.config.GetBool("protect-bags")) {
						//Log.Debug(HavenBags.plugin, "Dropped item is protected.");
						e.setCancelled(true);
					}
	    		}
	    	 }
	     }
	}
	
}
