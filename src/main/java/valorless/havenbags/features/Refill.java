package valorless.havenbags.features;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.havenbags.BagData.Bag;

public class Refill implements Listener {
		
	public static void init() {
		Log.Debug(Main.plugin, "[DI-253] Registering Refill");
		Bukkit.getServer().getPluginManager().registerEvents(new Refill(), Main.plugin);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
	    Player player = event.getPlayer();
	    ItemStack item = event.getItemInHand();

	    Log.Debug(Main.plugin, "refill cancel? " + event.isCancelled());
	    
	    if (item.getAmount() == 1) {
	        List<Bag> bags = HavenBags.GetBagsDataInInventory(player);
	        Log.Debug(Main.plugin, "Bags?");
	    	if(bags.size() == 0) return;
	    	Log.Debug(Main.plugin, "Bags! " + bags.size());
	    	for(Bag bag : bags) {
	    		Data data = BagData.GetBag(HavenBags.GetBagUUID(bag.item), null);
	    		if(!data.hasRefill()) continue;
				ItemStack block = refill(item, bag, player);
		        Log.Debug(Main.plugin, "block?");
				if(block == null) continue;
		        Log.Debug(Main.plugin, "block!");
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
					boolean hasSpace = player.getInventory().firstEmpty() != -1;
					
					if(hasSpace) player.getInventory().addItem(block);
					else player.getWorld().dropItem(player.getLocation(), block);
				}, 1L);
				
				return;
			}
	    }
	}
    
    private ItemStack refill(ItemStack hand, Bag bag, Player player) {
    	for(ItemStack item : bag.content) {
    		if(item == null) continue;
    		if(item.isSimilar(hand)) {
    			ItemStack clone = item.clone();
				item.setAmount(0);
				BagData.UpdateBag(bag.item, bag.content);
				HavenBags.UpdateBagLore(bag.item, player);

				return clone;
    		}
    	}
    	
    	return null;
    }
}
