package valorless.havenbags;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import valorless.havenbags.BagData.Bag;

// Unused, still brainstorming use.
// Is not registered in Main.
public class EventListener implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(HavenBags.InventoryContainsBag(player)) {
			for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
				HavenBags.UpdateBagLore(bag.item, player);
			}
		}
		
	}

}
