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
import valorless.havenbags.Main;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;

public class InventoryLock implements Listener {

	public static void init() {
		Log.Debug(Main.plugin, "[DI-248] Registering InventoryLock");
		Bukkit.getServer().getPluginManager().registerEvents(new InventoryLock(), Main.plugin);
	}
	
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent e) {
		//Log.Error(Main.plugin, e.getEventName());
		if(!Main.config.GetBool("inventory-lock.enabled")) return;
		try {
			Player player = (Player) e.getSource().getViewers().get(0);
			ItemStack item = e.getItem();
			if(HavenBags.IsBag(item) && e.getSource() == player.getInventory()) {
				if(Main.config.GetBool("inventory-lock.unbound") && BagState.getState(item) == BagState.NEW &&
						PDC.GetBoolean(item, "binding") == true) {
					if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
						e.setCancelled(true);
						return;
					}
				}
				if(Main.config.GetBool("inventory-lock.bound") && BagState.getState(item) == BagState.USED &&
						PDC.GetBoolean(item, "binding") == true) {
					if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
						e.setCancelled(true);
						return;
					}
				}
				if(Main.config.GetBool("inventory-lock.unused") && BagState.getState(item) == BagState.NEW &&
						PDC.GetBoolean(item, "binding") == false) {
					if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
						e.setCancelled(true);
						return;
					}
				}
				if(Main.config.GetBool("inventory-lock.used") && BagState.getState(item) == BagState.USED &&
						PDC.GetBoolean(item, "binding") == false) {
					if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
						e.setCancelled(true);
						return;
					}
				}
			}
		}catch(Exception E) {
			E.printStackTrace();
		}
		
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) { 
		//Log.Error(Main.plugin, e.getEventName());
		if(!Main.config.GetBool("inventory-lock.enabled")) return;
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
    		ItemStack item = cursorItem;
    		if(Main.config.GetBool("inventory-lock.unbound") && BagState.getState(item) == BagState.NEW &&
					PDC.GetBoolean(item, "binding") == true) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.bound") && BagState.getState(item) == BagState.USED &&
					PDC.GetBoolean(item, "binding") == true) {
				if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.unused") && BagState.getState(item) == BagState.NEW &&
					PDC.GetBoolean(item, "binding") == false) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.used") && BagState.getState(item) == BagState.USED &&
					PDC.GetBoolean(item, "binding") == false) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
					e.setCancelled(true);
					return;
				}
			}
        }
		else if(e.getRawSlot() > inv.getSize() && clickedBag && e.isShiftClick()){
			ItemStack item = clickedItem;
			if(Main.config.GetBool("inventory-lock.unbound") && BagState.getState(item) == BagState.NEW &&
					PDC.GetBoolean(item, "binding") == true) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.bound") && BagState.getState(item) == BagState.USED &&
					PDC.GetBoolean(item, "binding") == true) {
				if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.unused") && BagState.getState(item) == BagState.NEW &&
					PDC.GetBoolean(item, "binding") == false) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.used") && BagState.getState(item) == BagState.USED &&
					PDC.GetBoolean(item, "binding") == false) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
					e.setCancelled(true);
					return;
				}
			}
        }
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		//Log.Error(Main.plugin, e.getEventName());
		if(!Main.config.GetBool("inventory-lock.enabled")) return;
		ItemStack item = e.getItemDrop().getItemStack();
		if(HavenBags.IsBag(item)) {
			if(Main.config.GetBool("inventory-lock.unbound") && BagState.getState(item) == BagState.NEW &&
					PDC.GetBoolean(item, "binding") == true) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.bound") && BagState.getState(item) == BagState.USED &&
					PDC.GetBoolean(item, "binding") == true) {
				if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.unused") && BagState.getState(item) == BagState.NEW &&
					PDC.GetBoolean(item, "binding") == false) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
					e.setCancelled(true);
					return;
				}
			}
			if(Main.config.GetBool("inventory-lock.used") && BagState.getState(item) == BagState.USED &&
					PDC.GetBoolean(item, "binding") == false) {
				if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
					e.setCancelled(true);
					return;
				}
			}
		}
		
	}
	
}
