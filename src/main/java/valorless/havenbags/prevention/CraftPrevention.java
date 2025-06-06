package valorless.havenbags.prevention;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CraftPrevention implements Listener {

	public static void init() {
		Log.Debug(Main.plugin, "[DI-13] Registering CraftPrevention");
		Bukkit.getServer().getPluginManager().registerEvents(new CraftPrevention(), Main.plugin);
	}
	
	@EventHandler
	void onPrepareItemCraft(PrepareItemCraftEvent e) {
		for(ItemStack item : e.getInventory().getContents()) {
			if(item.isSimilar(e.getInventory().getResult())) return;
			if(HavenBags.IsBag(item)) {
				Log.Debug(Main.plugin, "[DI-209] [CraftPrevention] Trying to craft with bag.");
				e.getInventory().setResult(null);
			}
		}
	}
	
	@EventHandler
	public void onCraftItem (CraftItemEvent e) {
		for(ItemStack item : e.getInventory().getContents()) {
			if(item.isSimilar(e.getInventory().getResult())) return;
			if(HavenBags.IsBag(item)) {
				Log.Debug(Main.plugin, "[DI-210] [CraftPrevention] Trying to craft with bag.");
				e.setCancelled(true);
			}
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
