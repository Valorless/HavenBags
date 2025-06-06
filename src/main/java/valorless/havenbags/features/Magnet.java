package valorless.havenbags.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Bag;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class Magnet {

	public static void init() {
		Log.Debug(Main.plugin, "[DI-251] Registering Magnet");
		
		new BukkitRunnable() {
		    @Override
		    public void run() {
		    	if(!Main.config.GetBool("magnet.enabled")) return; // Keep the runnable, should it be enabled, and '/bags reload' is run.
		        for (Player player : Bukkit.getOnlinePlayers()) {
		        	for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
		        		ItemStack bagItem = bag.item;
			            if(HavenBags.IsBagFull(bag.item)) continue; // Ignore full bags
			            Data data = BagData.GetBag(HavenBags.GetBagUUID(bagItem), null);
			            if(!data.hasMagnet()) continue;
			            if(Main.config.GetBool("magnet.require-autopickup") && data.getAutopickup().equalsIgnoreCase("null")) continue;

			            Location playerLoc = player.getLocation();
			            
			            double range = Main.config.GetFloat("magnet.range");
			            for (Entity entity : player.getNearbyEntities(range, range, range)) {
			                if (!(entity instanceof Item)) continue;
			                Item item = (Item) entity;
			                if (item.isDead() || !item.isValid()) continue;
			                if (!item.isOnGround()) continue;
			                if (item.getOwner() != null && item.getOwner() != player.getUniqueId()) continue;
			                
			                if(Main.config.GetBool("magnet.require-autopickup") && !data.getAutopickup().equalsIgnoreCase("null")) {
			                	if(Main.config.GetBool("magnet.only-autopickup-items")) {
			                		if(!AutoPickup.IsItemInFilter(NBT.GetString(bag.item, "bag-filter"), item.getItemStack())) {
			            				continue;
			            			}
			                	}
			                }

			                Vector direction = playerLoc.toVector().subtract(item.getLocation().toVector());
			                double distance = direction.length();
			                if (distance < 0.5) continue;

			                direction.normalize().multiply(Main.config.GetFloat("magnet.speed"));

			                item.setVelocity(direction);
			            }
		        	}
		        }
		    }
		}.runTaskTimer(Main.plugin, 1L, 1L);
	}
}
