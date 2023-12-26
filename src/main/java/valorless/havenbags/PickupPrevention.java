package valorless.havenbags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

public class PickupPrevention implements Listener {
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";

	// NOT IN USE!
	
	/*
    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
    	ItemStack bag = event.getItem().getItemStack();
    	SkullMeta bagMeta = (SkullMeta)bag.getItemMeta();
    	if(Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING) == null) return;
    	String owner = (String)Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING);
		if(!owner.equalsIgnoreCase(Bukkit.getPlayer(event.getEntity().getName()).getUniqueId().toString()) && Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "canbind", PersistentDataType.STRING) == "true") {
			event.setCancelled(true);
		}
    }
	*/
	
	@EventHandler
	public void onHopperPickup(InventoryMoveItemEvent e) {	
		if(Main.config.GetBool("bags-in-shulkers") == false) {		
			if(Main.IsBag(e.getItem())){
				if(e.getDestination().getType() == InventoryType.HOPPER) {
					e.setCancelled(true);
				}
		
				if(e.getSource().getType() == InventoryType.HOPPER) {
					e.setCancelled(true);
				}
			}
		}
	}
}
