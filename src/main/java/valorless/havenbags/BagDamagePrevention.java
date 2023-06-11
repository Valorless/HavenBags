package valorless.havenbags;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BagDamagePrevention implements Listener{
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	
	// NOT IN USE!
	// NOTE: Causes the bag to corrupt.

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
	     if(e.getEntity().getType()==EntityType.DROPPED_ITEM) e.setCancelled(true);
	}
	
}
