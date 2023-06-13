package valorless.havenbags;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import valorless.valorlessutils.nbt.NBT;

public class BagDamagePrevention implements Listener{
	String Name = "§7[§aHaven§bBags§7]§r";

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().getType()==EntityType.DROPPED_ITEM) {
	    // Log.Debug(HavenBags.plugin, "Dropped item: " + e.getEntity().getName());
	    	if(e.getEntity() instanceof Item){
		    	//Log.Debug(HavenBags.plugin, "Dropped item can be Item.");
		    	Item item = (Item)e.getEntity();
	    		if(item.getItemStack().getType() == Material.PLAYER_HEAD) {
			    	//Log.Debug(HavenBags.plugin, "Dropped item is PLAYER_HEAD.");
	    			if(item.getItemStack().hasItemMeta()) {
				    	//Log.Debug(HavenBags.plugin, "Dropped item has ItemMeta.");
						//Log.Debug(HavenBags.plugin, "Dropped item is likely bag.");
						if(NBT.Has(item.getItemStack(), "bag-uuid")) {
							//Log.Debug(HavenBags.plugin, "Dropped item is bag!");
							if(HavenBags.config.GetBool("protect-bags")) {
								//Log.Debug(HavenBags.plugin, "Dropped item is protected.");
	    						e.setCancelled(true);
							}
						}
	    			}else {
				    	//Log.Debug(HavenBags.plugin, "Dropped item has no ItemMeta.");
	    			}
			    	
	    		 }
	    	 }
	     }
	}
	
}
