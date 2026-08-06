package valorless.havenbags.prevention;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.database.BagCache.Observer;
import valorless.valorlessutils.logging.Log;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlacementBlocker implements Listener {
	
	public static void init() {
    	Log.debug(Main.plugin, "[DI-7] Registering PlacementBlocker");
		Bukkit.getServer().getPluginManager().registerEvents(new PlacementBlocker(), Main.plugin);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlockPlaced();
		ItemStack item = event.getItemInHand();
		ItemMeta nbt = item.getItemMeta();
		//ItemStack offItem = event.getPlayer().getInventory().getItemInOffHand();
		//ItemMeta offMeta = offItem.getItemMeta();
		Log.debug(Main.plugin, "[PlacementBlocker][DI-201] " + "Block Placed: " + block.getType().toString());
		Log.debug(Main.plugin, "[PlacementBlocker][DI-202] " + "Player Holding: " + item.getType().toString());
		
		if(nbt != null) {
			Observer.checkItem(event.getItemInHand());
		}
		
		if(HavenBags.isBag(event.getItemInHand())) {
			event.setCancelled(true); 
			return;
		}
		
		if(nbt != null) {
			Log.debug(Main.plugin, "[PlacementBlocker][DI-203] " + "Block has ItemMeta.");
			if(HavenBags.isBag(item)) {
				Log.debug(Main.plugin, "[PlacementBlocker][DI-204] " + "Block was bag!");
				block.setType(Material.AIR);
				event.setCancelled(true);
			}
			if(HavenBags.isSkinToken(item)) {
				Log.debug(Main.plugin, "[PlacementBlocker][DI-205] " + "Block was skin token!");
				block.setType(Material.AIR);
				event.setCancelled(true);
			}
		}
		if(item.getType() == Material.AIR) {
			if(Main.config.getString("bag.type").equalsIgnoreCase("ITEM")) {
			if(block.getType() == Main.config.getMaterial("bag.material")) {
				block.setType(Material.AIR);
				event.setCancelled(true);
				Log.debug(Main.plugin, "[PlacementBlocker][DI-206] " + "Player was caught holding AIR, usually triggered by BagListener removing the item from the player when it's a bag.");
				Log.debug(Main.plugin, "[PlacementBlocker][DI-207] " + "Block was likely bag, removing.");
			}
			}else if(Main.config.getString("bag.type").equalsIgnoreCase("HEAD")) {
				if(block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
					block.setType(Material.AIR);
					event.setCancelled(true);
					Log.debug(Main.plugin, "[PlacementBlocker][DI-206] " + "Player was caught holding AIR, usually triggered by BagListener removing the item from the player when it's a bag.");
					Log.debug(Main.plugin, "[PlacementBlocker][DI-207] " + "Block was likely bag, removing.");
				}
			} 
		}
	}
	
	/*
	@EventHandler (priority = EventPriority.LOWEST)
	public void onRightClickDisplay(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(true);
		if (event.getRightClicked() instanceof Interaction inter) {
	        event.getPlayer().sendMessage("PlayerInteractAtEntityEvent");
	        event.getPlayer().sendMessage(inter.getType().toString());
	        if(HavenBags.IsBag(player.getInventory().getItemInMainHand())) {
		        event.getPlayer().sendMessage("Cancelled");
	        	event.setCancelled(true);
	        }
	    }
	}
	*/
}
