package valorless.havenbags.prevention;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
import valorless.valorlessutils.ValorlessUtils.Log;

public class InventoryListener implements Listener {
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-11] Registering InventoryListener");
		Bukkit.getServer().getPluginManager().registerEvents(new InventoryListener(), Main.plugin);
	}
		
	final private List<InventoryType> allowedContainers = PrepareAllowedContainers();
		
	List<InventoryType> PrepareAllowedContainers() {
		Log.Debug(Main.plugin, "[DI-198] " + "[InventoryListener] Preparing Allowed Containers");
		List<InventoryType> cont = new ArrayList<InventoryType>();
		cont.add(InventoryType.CRAFTING);
		cont.add(InventoryType.HOPPER);
		cont.add(InventoryType.PLAYER);
		cont.add(InventoryType.CREATIVE);
		cont.add(InventoryType.WORKBENCH);
		for(String container : Main.config.GetStringList("allowed-containers")) {
			cont.add(GetInventoryType(container));
		}
		Log.Debug(Main.plugin, "[DI-199] " + "[InventoryListener] Allowed Containers:");
		for(InventoryType type : cont) {
			Log.Debug(Main.plugin, "- " + type.toString());
		}
		Log.Debug(Main.plugin, "[DI-200] " + "[InventoryListener] To update this list, you have to restart or reload the server, not /bags reload.");
		return cont;
	}

	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
		if(e.getRawSlot() == -1 || e.getRawSlot() == 999) return;
		Log.Debug(Main.plugin, "[DI-223] " + "[InventoryListener] Inventory Click");
		if (e.getHotbarButton() != -1) {
            ItemStack swapItem = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
            if (swapItem != null && HavenBags.IsBag(swapItem)) e.setCancelled(true);
        }
        /*if(e.getHotbarButton() != -1) {
        	//Log.Debug(plugin, "" + e.getHotbarButton());
        	e.setCancelled(true);
        	return;
        }*/
		
		Shulkers(e);
		Containers(e);
    }
	
	void Containers(final InventoryClickEvent e) {
        if(e.getInventory().getType() == InventoryType.SHULKER_BOX) return;
		Log.Debug(Main.plugin, "[DI-224] " + "[InventoryListener] Container");
		boolean holdingBag = HavenBags.IsBag(e.getCursor());
		if(e.getClickedInventory() == null) return;
        
        if(e.getClickedInventory().getType() != InventoryType.PLAYER && holdingBag){
        	if(!allowedContainers.contains(e.getInventory().getType())) {
        		e.setCancelled(true);
        		return;
        	}
        }
        
        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(HavenBags.IsBag(clickedItem) && e.isShiftClick()) {
        	if(!allowedContainers.contains(e.getInventory().getType())) {
        		e.setCancelled(true);
        		return;
        	}
        }
		/*
		ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(HavenBags.IsBag(clickedItem)) {
        	// Cancel click event if the inventory type isn't allowed.
        	if(allowedContainers.contains(e.getInventory().getType()) == false) {
        		e.setCancelled(true);
        	}
        }*/
	}
	
	void Shulkers(final InventoryClickEvent e) {
        if(e.getInventory().getType() != InventoryType.SHULKER_BOX) return;
		Log.Debug(Main.plugin, "[DI-225] " + "[InventoryListener] Shulker");
        if (Main.config.GetBool("bags-in-shulkers") == true) return;

        boolean holdingBag = HavenBags.IsBag(e.getCursor());
		if(e.getClickedInventory() == null) return;
        
        if(e.getClickedInventory().getType() != InventoryType.PLAYER && holdingBag){
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-shulker-error"));
        	e.setCancelled(true);
        	return;
        }
        
        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(HavenBags.IsBag(clickedItem) && e.isShiftClick()) {
        	e.setCancelled(true);
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-shulker-error"));
        }
        /*
        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(HavenBags.IsBag(clickedItem)) {
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-shulker-error"));
        	e.setCancelled(true);
        }*/
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
        			//event.getInventory().remove(item);
        			HavenBags.ReturnBag(Return, (Player)event.getPlayer());
        			item.setAmount(0);
            	}
        	}
        }
        /*
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
        }*/
    }
	
	public InventoryType GetInventoryType(String string) {
		for(InventoryType type : InventoryType.values()) {
			if(string.equalsIgnoreCase(type.toString())) {
				return type;
			}
		}
		Log.Error(Main.plugin, String.format("InventoryType does not contain '%s'.", string));
		return null;
	}
	
}
