package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;

public class Soulbound implements Listener {
    private final Map<UUID, List<ItemStack>> savedItems = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
    	if(!Main.config.GetBool("soulbound")) return;
        Player player = event.getEntity();
        if(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) return;
        UUID playerId = player.getUniqueId();
        List<ItemStack> toKeep = new ArrayList<>();

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();

            if (HavenBags.IsBag(item)) {
                toKeep.add(item.clone()); // Save item for respawn
                iterator.remove(); // Prevent dropping
            }
        }

        if (!toKeep.isEmpty()) {
            savedItems.put(playerId, toKeep);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
    	if(!Main.config.GetBool("soulbound")) return;
        Player player = event.getPlayer();
        if(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) return;
        UUID playerId = player.getUniqueId();

        if (savedItems.containsKey(playerId)) {
            List<ItemStack> items = savedItems.remove(playerId);
            for (ItemStack item : items) {
                player.getInventory().addItem(item); // Give item back on respawn
            }
        }
    }
}
