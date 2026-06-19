package valorless.havenbags.features;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Bag;
import valorless.valorlessutils.logging.Log;
import valorless.havenbags.Database.BagSimple;

public class Refill implements Listener {
		
	public static void init() {
		Log.debug(Main.plugin, "[DI-253] Registering Refill");
		Bukkit.getServer().getPluginManager().registerEvents(new Refill(), Main.plugin);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
	    Player player = event.getPlayer();
	    ItemStack item = event.getItemInHand();

	    Log.debug(Main.plugin, "refill cancel? " + event.isCancelled());
	    
	    if (item.getAmount() == 1) {
	        List<Database.BagSimple> bags = HavenBags.getBagsDataInInventory(player);
	        Log.debug(Main.plugin, "Bags?");
	    	if(bags.isEmpty()) return;
	    	Log.debug(Main.plugin, "Bags! " + bags.size());
	    	for(BagSimple bag : bags) {
	    		Bag data = Database.getBag(HavenBags.getBagUUID(bag.item), null);
	    		if(!data.hasRefill()) continue;
				ItemStack block = refill(item, bag, player);
		        Log.debug(Main.plugin, "block?");
				if(block == null) continue;
		        Log.debug(Main.plugin, "block!");
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
					boolean hasSpace = player.getInventory().firstEmpty() != -1;
					
					if(hasSpace) player.getInventory().addItem(block);
					else player.getWorld().dropItem(player.getLocation(), block);
				}, 1L);
				
				return;
			}
	    }
	}
    
    private ItemStack refill(ItemStack hand, Database.BagSimple bag, Player player) {
    	for(ItemStack item : bag.content) {
    		if(item == null) continue;
    		if(item.isSimilar(hand)) {
    			ItemStack clone = item.clone();
				item.setAmount(0);
				Database.updateBag(bag.item, bag.content);
				HavenBags.updateBagLore(bag.item, player);

				return clone;
    		}
    	}
    	
    	return null;
    }
}
