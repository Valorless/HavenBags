package valorless.havenbags;

import valorless.valorlessutils.ValorlessUtils.*;
import valorless.valorlessutils.nbt.NBT;

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
		Block block = event.getBlockPlaced();
		ItemStack item = event.getItemInHand();
		ItemMeta nbt = item.getItemMeta();
		ItemStack offItem = event.getPlayer().getInventory().getItemInOffHand();
		ItemMeta offMeta = offItem.getItemMeta();
		//Log.Debug(HavenBags.plugin, "Block Placed: " + block.getType().toString());
		//Log.Debug(HavenBags.plugin, "Player Holding: " + item.getType().toString());
	 
		if(block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
			Log.Debug(HavenBags.plugin, "Block was head.");
			if(nbt != null) {
				Log.Debug(HavenBags.plugin, "Block has ItemMeta.");
				if(NBT.Has(item, "bag-uuid")) {
					Log.Debug(HavenBags.plugin, "Block was bag!");
					block.setType(Material.AIR);
					event.setCancelled(true);
					//Log.Warning(HavenBags.plugin, "Oops.. This shouldnt happen... Please tell the developer 'PlacementBlocker:onBlockPlace()' :)");
					//Log.Warning(HavenBags.plugin, "This warning was tested quite a lot and was never triggered.");
					//Log.Warning(HavenBags.plugin, "I left this in on purpose, should it work as intended.");
					//Log.Warning(HavenBags.plugin, "No additional code is run, so you're good!");
					/*Player player = event.getPlayer();
					block.setType(Material.AIR);
					
					if(player.getGameMode() != GameMode.CREATIVE) { //Dont give Creative a replacement.
						Integer amount = player.getInventory().getItemInMainHand().getAmount();
						item.setAmount(amount -1);
			
						ItemStack replacement = new ItemStack(Material.PLAYER_HEAD, 1);
						replacement.setItemMeta(nbt);
			
						player.getInventory().addItem(replacement);
					}
					
					player.closeInventory();
					event.setCancelled(true);*/
				}
			}else {
				Log.Debug(HavenBags.plugin, "Block has no ItemMeta.");
				Log.Debug(HavenBags.plugin, "Block was likely bag, removing.");
				block.setType(Material.AIR);
				event.setCancelled(true);
			}
		}
	}
}
