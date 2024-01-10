package valorless.havenbags;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (Main.config.GetBool("bags-in-shulkers") == true) return;

        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(e.getInventory().getType() != InventoryType.SHULKER_BOX) return;
        
        if(HavenBags.IsBag(clickedItem)) {
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-shulker-error"));
        	e.setCancelled(true);
        }
    }
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		//Log.Warning(Main.plugin, event.getInventory().getType().toString());
		
        if (event.getInventory().getType() == InventoryType.SHULKER_BOX) {
    		if (Main.config.GetBool("bags-in-shulkers") == true) return;
    		
    		if(event.getInventory().getContents().length == 0) return;
        	for (ItemStack item : event.getInventory().getContents()) {
        		if(HavenBags.IsBag(item)) {
        			ItemStack Return = item.clone();
        			event.getInventory().remove(item);
        			HavenBags.ReturnBag(Return, (Player)event.getPlayer());
            	}
        	}
        }
        
        if (event.getInventory().getType() == InventoryType.CHEST) {
    		if (Main.config.GetBool("bags-in-bags") == true) return;
        
    		// Is this Inventory on the list of bags? If so, return.
    		if(!HavenBags.BagHashes.Contains(event.getInventory().hashCode())) return;
    		
    		if(event.getInventory().getContents().length == 0) return;
    		
        	for (ItemStack item : event.getInventory().getContents()) {
        		if(HavenBags.IsBag(item)) {
        			ItemStack Return = item.clone();
        			event.getInventory().remove(item);
        			HavenBags.ReturnBag(Return, (Player)event.getPlayer());
            	}
        	}
        }
    }
	
}
