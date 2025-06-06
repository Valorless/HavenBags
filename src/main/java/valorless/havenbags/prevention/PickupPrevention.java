package valorless.havenbags.prevention;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.*;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class PickupPrevention implements Listener {
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-12] Registering PickupPrevention");
		Bukkit.getServer().getPluginManager().registerEvents(new PickupPrevention(), Main.plugin);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(!Main.config.GetBool("protect-bags-players")) return;
		ItemStack item = event.getItemDrop().getItemStack();
		if(!HavenBags.IsBag(item)) return;
		String owner = NBT.GetString(item, "bag-owner");
		if(owner.equalsIgnoreCase("ownerless") && owner.equalsIgnoreCase("null")) return;
		try {
			event.getItemDrop().setOwner(UUID.fromString(owner));
		}catch(Exception e) {}
	}
	
	@EventHandler
	public void onHopperPickup(InventoryMoveItemEvent e) {		
		if(HavenBags.IsBag(e.getItem())){
			if(e.getDestination().getType() == InventoryType.HOPPER) {
				e.setCancelled(true);
			}
	
			if(e.getSource().getType() == InventoryType.HOPPER) {
				e.setCancelled(true);
			}
		}
	}
}
