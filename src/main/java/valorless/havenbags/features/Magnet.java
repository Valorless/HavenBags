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
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.Bag;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.utils.Utils;

public class Magnet {

	public static void init() {
		Log.debug(Main.plugin, "[DI-251] Registering Magnet");
		
		Integer ticks = Utils.Clamp(Main.config.getInt("magnet.tick-rate"), 1, 999);
		
		new BukkitRunnable() {
		    @Override
		    public void run() {
		    	if(!Main.config.getBool("magnet.enabled")) return; // Keep the runnable, should it be enabled, and '/bags reload' is run.
		        for (Player player : Bukkit.getOnlinePlayers()) {
		        	for(Bag data : BagCache.getPlayerBagsFromInventory(player)) {
			            if(HavenBags.isBagFull(data.getUuid())) continue; // Ignore full bags
			            if(!data.hasMagnet()) continue;
			            if(Main.config.getBool("magnet.require-autopickup") && data.getAutopickup().equalsIgnoreCase("null")) continue;

			            Location playerLoc = player.getLocation();
			            
			            double range = Main.config.getDouble("magnet.range");
			            for (Entity entity : player.getNearbyEntities(range, range, range)) {
			                if (!(entity instanceof Item item)) continue;
                            if (item.isDead() || !item.isValid()) continue;
			                if (!item.isOnGround() && Main.config.getBool("magnet.vertical") == false) continue;
			                if (item.getOwner() != null && item.getOwner() != player.getUniqueId()) continue;
			                
			                if(Main.config.getBool("magnet.require-autopickup") && !data.getAutopickup().equalsIgnoreCase("null")) {
			                	if(Main.config.getBool("magnet.only-autopickup-items")) {
			                		if(!AutoPickup.isItemInFilter(data.getAutopickup(), item.getItemStack())) {
			            				continue;
			            			}
			                	}
			                }
			                
			                if(Main.config.getBool("magnet.instant")) {
			                    // Instant magnet effect
			                    item.teleport(playerLoc);
			                    continue;
			                }
			                
			                Vector direction = playerLoc.toVector().subtract(item.getLocation().toVector());
			                double distanceSquared = direction.lengthSquared();
			                if (distanceSquared < 0.25) continue; // 0.5 blocks squared

			                if(Main.config.getBool("magnet.vertical") == false) {
			                	if (!item.getLocation().getBlock().isLiquid()) {
			                    	direction.setY(0);
			                	}
			                }
			                
			                direction.normalize().multiply(Main.config.getDouble("magnet.speed"));

			                item.setVelocity(direction);
			            }
		        	}
		        	
		        	for(String ebag : EtherealBags.getPlayerBags(player.getUniqueId())) {
		        		if(HavenBags.isBagFull(EtherealBags.formatBagId(player.getUniqueId(), ebag))) continue; // Ignore full bags
		        		if(!EtherealBags.getBagMagnet(player.getUniqueId(), ebag)) continue;

		        		if(Main.config.getBool("magnet.require-autopickup") && EtherealBags.getBagAutoPickup(player.getUniqueId(), ebag).equalsIgnoreCase("null")) continue;
		        		
			            Location playerLoc = player.getLocation();
			            double range = Main.config.getDouble("magnet.range");

			            for (Entity entity : player.getNearbyEntities(range, range, range)) {
			                if (!(entity instanceof Item item)) continue;
                            if (item.isDead() || !item.isValid()) continue;
			                if (!item.isOnGround() && Main.config.getBool("magnet.vertical") == false) continue;
			                if (item.getOwner() != null && item.getOwner() != player.getUniqueId()) continue;

			                if(Main.config.getBool("magnet.require-autopickup") && !EtherealBags.getBagAutoPickup(player.getUniqueId(), ebag).equalsIgnoreCase("null")) {
			                	if(Main.config.getBool("magnet.only-autopickup-items")) {
			                		if(!AutoPickup.isItemInFilter(EtherealBags.getBagAutoPickup(player.getUniqueId(), ebag), item.getItemStack())) {
			            				continue;
			            			}
			                	}
			                }

			                if(Main.config.getBool("magnet.instant")) {
			                    // Instant magnet effect
			                    item.teleport(playerLoc);
			                    continue;
			                }

			                Vector direction = playerLoc.toVector().subtract(item.getLocation().toVector());
			                double distanceSquared = direction.lengthSquared();
			                if (distanceSquared < 0.25) continue; // 0.5 blocks squared

			                if(Main.config.getBool("magnet.vertical") == false) {
			                	if (!item.getLocation().getBlock().isLiquid()) {
			                    	direction.setY(0);
			                	}
			                }
			                
			                direction.normalize().multiply(Main.config.getDouble("magnet.speed"));

			                item.setVelocity(direction);
			            }
		        	}
		        }
		    }
		}.runTaskTimer(Main.plugin, ticks, ticks);
	}
}
