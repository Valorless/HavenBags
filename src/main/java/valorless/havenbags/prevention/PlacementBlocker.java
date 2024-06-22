package valorless.havenbags.prevention;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlacementBlocker implements Listener {
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		
		if(HavenBags.IsBag(event.getItemInHand())) {
			event.setCancelled(true); 
			return;
		}
		
		Block block = event.getBlockPlaced();
		ItemStack item = event.getItemInHand();
		ItemMeta nbt = item.getItemMeta();
		//ItemStack offItem = event.getPlayer().getInventory().getItemInOffHand();
		//ItemMeta offMeta = offItem.getItemMeta();
		Log.Debug(Main.plugin, "Block Placed: " + block.getType().toString());
		Log.Debug(Main.plugin, "Player Holding: " + item.getType().toString());
		
		if(nbt != null) {
			Log.Debug(Main.plugin, "Block has ItemMeta.");
			if(HavenBags.IsBag(item)) {
				Log.Debug(Main.plugin, "Block was bag!");
				block.setType(Material.AIR);
				event.setCancelled(true);
			}
			if(HavenBags.IsSkinToken(item)) {
				Log.Debug(Main.plugin, "Block was skin token!");
				block.setType(Material.AIR);
				event.setCancelled(true);
			}
		}
		if(item.getType() == Material.AIR) {
			block.setType(Material.AIR);
			event.setCancelled(true);
			Log.Debug(Main.plugin, "Player was caught holding AIR, usually triggered by BagListener removing the item from the player when it's a bag.");
			Log.Debug(Main.plugin, "Block was likely bag, removing.");
		}
		
		/*
		if(block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
			Log.Debug(Main.plugin, "Block was head.");
			if(nbt != null) {
				Log.Debug(Main.plugin, "Block has ItemMeta.");
				if(NBT.Has(item, "bag-uuid")) {
					Log.Debug(Main.plugin, "Block was bag!");
					block.setType(Material.AIR);
					event.setCancelled(true);
				}
			}
			
			if(item.getType() == Material.AIR) {
				block.setType(Material.AIR);
				event.setCancelled(true);
				Log.Debug(Main.plugin, "Player was caught holding AIR, usually triggered by BagListener removing the item from the player when it's a bag.");
				Log.Debug(Main.plugin, "Block was likely bag, removing.");
			}
		} else {
			if(nbt != null) {
				Log.Debug(Main.plugin, "Block has ItemMeta.");
				if(NBT.Has(item, "bag-uuid")) {
					Log.Debug(Main.plugin, "Block was bag!");
				}
			}
			
			if(item.getType() == Material.AIR) {
				block.setType(Material.AIR);
				event.setCancelled(true);
				Log.Debug(Main.plugin, "Player was caught holding AIR, usually triggered by BagListener removing the item from the player when it's a bag.");
				Log.Debug(Main.plugin, "Block was likely bag, removing.");
			}
		}
		*/
	}
}
