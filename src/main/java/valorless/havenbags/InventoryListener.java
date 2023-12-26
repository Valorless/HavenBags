package valorless.havenbags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (Main.config.GetBool("bags-in-shulkers") == true) return;

        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(e.getInventory().getType() != InventoryType.SHULKER_BOX) return;
        
        if(Main.IsBag(clickedItem)) {
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-shulker-error"));
        	e.setCancelled(true);
        }
    }
	
}
