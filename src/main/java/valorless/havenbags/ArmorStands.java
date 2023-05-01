package valorless.havenbags;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ArmorStands implements Listener {

	@EventHandler
	public void onstandplace(CreatureSpawnEvent e) {
		if (e.getEntity().getType() != EntityType.ARMOR_STAND) return;
		LivingEntity s = e.getEntity();
		ArmorStand stand = (ArmorStand)s;
		Player player = getNearestPlayer(stand.getLocation());
		if(player.hasPermission("haventweaks.armorstands")) {
			stand.setArms(true);
			if(player.isSneaking()) {
				stand.setCustomNameVisible(true);
			}
		}
	}
	
	public Player getNearestPlayer(Location location) {
		double closest = Double.MAX_VALUE;
		Player closestp = null;
		for(Player i : Bukkit.getOnlinePlayers()){
			double dist = i.getLocation().distance(location);
			if (closest == Double.MAX_VALUE || dist < closest){
				closest = dist;
				closestp = i;
			}
		}
		if (closestp == null){
		  return null;
		}
		else{
		  return closestp;
		}
    }
}
