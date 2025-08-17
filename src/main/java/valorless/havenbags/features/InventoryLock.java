package valorless.havenbags.features;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.valorlessutils.ValorlessUtils.Log;

public class InventoryLock implements Listener {

	public static void init() {
		Log.Debug(Main.plugin, "[DI-248] Registering InventoryLock");
		Bukkit.getServer().getPluginManager().registerEvents(new InventoryLock(), Main.plugin);
	}
	
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent e) {
		//Log.Error(Main.plugin, e.getEventName());
		if(!Main.config.GetBool("inventory-lock")) return;
		try {
			Player player = (Player) e.getSource().getViewers().get(0);
			if(HavenBags.IsBag(e.getItem()) && e.getSource() == player.getInventory()) {
				e.setCancelled(true);
			}
		}catch(Exception E) {
			E.printStackTrace();
		}
		
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) { 
		//Log.Error(Main.plugin, e.getEventName());
		if(!Main.config.GetBool("inventory-lock")) return;
		Log.Debug(Main.plugin, "[DI-290] " + "[InventoryLock] Inventory Click");
		
		Inventory inv = e.getInventory();
		
		// Check if the inventory is the upgrade gui.
		UpgradeGUI upgrade = UpgradeGUI.OpenGUIs.Get((Player) e.getWhoClicked());
		if(upgrade != null && upgrade.GetInv() == inv) return;
		
		ItemStack clickedItem = e.getCurrentItem();
		ItemStack cursorItem = e.getCursor();
		boolean holdingBag = HavenBags.IsBag(cursorItem);	
    	boolean clickedBag = HavenBags.IsBag(clickedItem);	
    	
    	if(inv.getType() == InventoryType.ANVIL) return; //Ignore anvils, they can be used for skins and upgrades.
    	
    	if(e.getRawSlot() < inv.getSize() && holdingBag){
        	e.setCancelled(true);
        }
		else if(e.getRawSlot() > inv.getSize() && clickedBag && e.isShiftClick()){
            e.setCancelled(true);
        }
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		//Log.Error(Main.plugin, e.getEventName());
		if(!Main.config.GetBool("inventory-lock")) return;
		if(HavenBags.IsBag(e.getItemDrop().getItemStack())) {
			e.setCancelled(true);
		}
		
	}
	
}
