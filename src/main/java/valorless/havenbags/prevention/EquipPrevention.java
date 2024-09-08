package valorless.havenbags.prevention;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class EquipPrevention implements Listener {

	@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked().getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Log.Debug(Main.plugin, "[DI-194] " + "[EquipPrevention] " + event.getWhoClicked().getOpenInventory().getTopInventory().getType());
        // Check if the event involves a player's own inventory
		//Log.Debug(Main.plugin, event.getWhoClicked().getOpenInventory().getTopInventory().getType().toString());
        if (event.getWhoClicked().getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        // Handle regular clicks
        if (event.getSlot() == 39) {
            //if (event.getCursor() != null && event.getCursor().getType() == Material.PLAYER_HEAD) {
            if (event.getCursor() != null && HavenBags.IsBag(event.getCursor())) {
                Log.Debug(Main.plugin, "[DI-195] " + "[EquipPrevention] Head!");
                event.setCancelled(true);
                //event.getWhoClicked().sendMessage("You cannot wear player heads!");
            }
        }
        // Handle shift-clicks
        else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            Log.Debug(Main.plugin, "[DI-196] " + "[EquipPrevention] Shift Click!");
            //if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            if (event.getCurrentItem() != null && HavenBags.IsBag(event.getCurrentItem())) {
                Log.Debug(Main.plugin, "[DI-197] " + "[EquipPrevention] Bag!");
                Inventory clickedInventory = event.getClickedInventory();
                Inventory topInventory = event.getView().getTopInventory();
                PlayerInventory playerInventory = (PlayerInventory) event.getWhoClicked().getInventory();
                
                if(event.getCurrentItem().getType() != Material.PLAYER_HEAD) return;
                if(playerInventory.getHelmet() != null) return;
                
                // Ensure the event is within the player's inventory
                if (clickedInventory == playerInventory && topInventory == playerInventory) {
                	event.setCancelled(true);
                    
                } 
                // Moving from the hotbar to the main inventory
                else if (clickedInventory == playerInventory) {
                    event.setCancelled(true);
                }
                //event.getWhoClicked().sendMessage("You cannot wear player heads!");
            }
        }
    }
}
