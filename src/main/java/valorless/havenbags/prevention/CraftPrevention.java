package valorless.havenbags.prevention;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;

public class CraftPrevention implements Listener {
	
	@EventHandler
	void onPrepareItemCraft(PrepareItemCraftEvent e) {
		for(ItemStack item : e.getInventory().getContents()) {
			if(HavenBags.IsBag(item)) {
				e.getInventory().setResult(null);
			}
		}
	}
	
	@EventHandler
	public void onCraftItem (CraftItemEvent e) {
		for(ItemStack item : e.getInventory().getContents()) {
			if(HavenBags.IsBag(item)) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onFurnaceStartSmelt (FurnaceStartSmeltEvent e) {
		if(HavenBags.IsBag(e.getSource())){
			//e.
		}
	}
	
	@EventHandler
	public void onFurnaceBurn (FurnaceBurnEvent e) {
		if(HavenBags.IsBag(e.getFuel())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFurnaceSmelt (FurnaceSmeltEvent e) {
		if(HavenBags.IsBag(e.getSource())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPrepareAnvil (PrepareAnvilEvent e) {
		if(e.getResult() != null) return;
		for(ItemStack item : e.getInventory().getContents()) {
			if(HavenBags.IsBag(item)) {
				e.setResult(null);
			}
		}
	}
	
}
