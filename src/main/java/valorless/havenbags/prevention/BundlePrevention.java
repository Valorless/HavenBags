package valorless.havenbags.prevention;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class BundlePrevention implements Listener {
	
	public static void init() {
		Log.debug(Main.plugin, "[DI-254] Registering BundlePrevention");
		Bukkit.getServer().getPluginManager().registerEvents(new BundlePrevention(), Main.plugin);
	}
	
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
		if(Main.config.getBool("bags-in-bundles")) return;
		if(e.getRawSlot() == -1 || e.getRawSlot() == 999) return;
		Log.debug(Main.plugin, "[DI-255] " + "[BundlePrevention] Inventory Click");
		boolean isBag = e.getCursor() != null ? HavenBags.isBag(e.getCursor()) : false;
		Material held = e.getCursor() != null ? e.getCursor().getType() : Material.AIR;
		Material clicked = e.getCurrentItem() != null ? e.getCurrentItem().getType() : Material.AIR;
		boolean clickedBag = e.getCurrentItem() != null ? HavenBags.isBag(e.getCurrentItem()) : false;
		
		//Cursor, holding bag clicking bundle.
		if(isBag && clicked.toString().contains("BUNDLE")) {
	        e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.get("bag-in-bundles-error"));
			e.setCancelled(true);
			return;
		}
		
		//Clicked, holding bundle clicking bag.
		if(held.toString().contains("BUNDLE") && clickedBag){
        	e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.get("bag-in-bundles-error"));
			e.setCancelled(true);
			return;
			
		}
    }
	
}
