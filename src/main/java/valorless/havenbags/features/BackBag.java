package valorless.havenbags.features;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import valorless.havenbags.Main;
import valorless.havenbags.datamodels.BagArmorStand;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.ValorlessUtils.Tags;

@SuppressWarnings("deprecation")
public class BackBag implements Listener {
	
	public static HashMap<Player, BagArmorStand> tracking = new HashMap<Player, BagArmorStand>();
	public static BukkitTask cleantask;
	
	public static void init() {
		if(!Main.config.GetBool("back-bag.enabled")) return;
		Log.Debug(Main.plugin, "[DI-250] Registering BackBag");
		Bukkit.getServer().getPluginManager().registerEvents(new BackBag(), Main.plugin);
		
		Restore();
		CleanupTask();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!Main.config.GetBool("back-bag.enabled")) return;
		
		Player player = event.getPlayer();

		BagArmorStand tracker = new BagArmorStand(player, Main.plugin);
		tracking.put(player, tracker);
	}
	
	private static void Restore() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			BagArmorStand tracker = new BagArmorStand(player, Main.plugin);
			tracking.put(player, tracker);
		}
	}

	private static void CleanupTask() {
		cleantask = Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
	        Bukkit.getWorlds().forEach(world -> {
	            world.getEntitiesByClass(ArmorStand.class).forEach(stand -> {
	                // Not tagged with HavenBags
	                if (Tags.Has(
	                        Main.plugin, 
	                        stand.getPersistentDataContainer(), 
	                        "HavenBags", 
	                        PersistentDataType.STRING)) {

	                    boolean tracked = tracking.values().stream()
	                        .anyMatch(tracker -> tracker.getStand() != null && tracker.getStand().equals(stand));

	                    if (!tracked) {
	                        stand.remove();
	                        Log.Debug(Main.plugin, "Removed rogue armor stand: " + stand.getUniqueId());
	                    }
	                }
	            });
	        });
	    }, 100L, 100L); // 5-second delay and repeat
	}
}
