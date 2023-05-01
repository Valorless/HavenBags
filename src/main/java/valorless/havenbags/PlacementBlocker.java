package valorless.havenbags;

import valorless.valorlessutils.ValorlessUtils.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlacementBlocker implements Listener {
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlockPlaced();
		ItemStack item = event.getItemInHand();
		ItemMeta nbt = item.getItemMeta();
		//Log.Info(String.valueOf(nbt.getCustomModelData()));
		//Log.Info(nbt.getDisplayName());
	 
		if(block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
			if(nbt != null) {
				if(Tags.Get(HavenBags.plugin, nbt.getPersistentDataContainer(), "uuid", PersistentDataType.STRING) != null) {
					Player player = event.getPlayer();
					block.setType(Material.AIR);
			
					Integer amount = player.getInventory().getItemInMainHand().getAmount();
					item.setAmount(amount -1);
			
					ItemStack replacement = new ItemStack(Material.PLAYER_HEAD, 1);
					replacement.setItemMeta(nbt);
			
					player.getInventory().addItem(replacement);
					player.closeInventory();
					event.setCancelled(true);
				}
			}
		}
	}
}
