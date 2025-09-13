package valorless.havenbags.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.database.BagCache;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;

public class Magnet {

	public static void init() {
		Log.Debug(Main.plugin, "[DI-251] Registering Magnet");
		
		Integer ticks = Utils.Clamp(Main.config.GetInt("magnet.tick-rate"), 1, 999);
		
		new BukkitRunnable() {
		    @Override
		    public void run() {
		    	if(!Main.config.GetBool("magnet.enabled")) return; // Keep the runnable, should it be enabled, and '/bags reload' is run.
		        for (Player player : Bukkit.getOnlinePlayers()) {
		        	for(Data data : BagCache.getPlayerBagsFromInventory(player)) {
			            if(HavenBags.IsBagFull(data.getUuid())) continue; // Ignore full bags
			            if(!data.hasMagnet()) continue;
			            if(Main.config.GetBool("magnet.require-autopickup") && data.getAutopickup().equalsIgnoreCase("null")) continue;

			            Location playerLoc = player.getLocation();
			            
			            double range = Main.config.GetDouble("magnet.range");
			            for (Entity entity : player.getNearbyEntities(range, range, range)) {
			                if (!(entity instanceof Item)) continue;
			                Item item = (Item) entity;
			                if (item.isDead() || !item.isValid()) continue;
			                if (!item.isOnGround() && Main.config.GetBool("magnet.vertical") == false) continue;
			                if (item.getOwner() != null && item.getOwner() != player.getUniqueId()) continue;
			                
			                if(Main.config.GetBool("magnet.require-autopickup") && !data.getAutopickup().equalsIgnoreCase("null")) {
			                	if(Main.config.GetBool("magnet.only-autopickup-items")) {
			                		if(!AutoPickup.IsItemInFilter(data.getAutopickup(), item.getItemStack())) {
			            				continue;
			            			}
			                	}
			                }
			                
			                if(Main.config.GetBool("magnet.instant")) {
			                    // Instant magnet effect
			                    item.teleport(playerLoc);
			                    continue;
			                }
			                
			                Vector direction = playerLoc.toVector().subtract(item.getLocation().toVector());
			                double distanceSquared = direction.lengthSquared();
			                if (distanceSquared < 0.25) continue; // 0.5 blocks squared

			                if (!item.getLocation().getBlock().isLiquid()) {
			                    direction.setY(0);
			                }
			                
			                direction.normalize().multiply(Main.config.GetDouble("magnet.speed"));

			                item.setVelocity(direction);
			            }
		        	}
		        }
		    }
		}.runTaskTimer(Main.plugin, ticks, ticks);
	}
}
