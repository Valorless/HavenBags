package valorless.havenbags.prevention;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;

public class InventoryListener implements Listener {
	
	final private List<InventoryType> allowedContainers = new ArrayList<InventoryType>() {
			private static final long serialVersionUID = 1L;
		{ 
			add(InventoryType.CHEST);
			add(InventoryType.ENDER_CHEST);
			add(InventoryType.BARREL);
			add(InventoryType.CREATIVE);
			add(InventoryType.PLAYER);
			add(InventoryType.SHULKER_BOX);
			add(InventoryType.MERCHANT);
			add(InventoryType.CRAFTING);
			add(InventoryType.HOPPER);
			}
		};

	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
		Shulkers(e);
		Containers(e);
    }
	
	void Containers(final InventoryClickEvent e) {
		ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(HavenBags.IsBag(clickedItem)) {
        	// Cancel click event if the inventory type isn't allowed.
        	if(allowedContainers.contains(e.getInventory().getType()) == false) {
        		e.setCancelled(true);
        	}
        }
	}
	
	void Shulkers(final InventoryClickEvent e) {
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
