package valorless.havenbags.features;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class BagCarryLimit implements Listener {
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-222] Registering BagCarryLimit");
		Bukkit.getServer().getPluginManager().registerEvents(new BagCarryLimit(), Main.plugin);
	}

	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
		if(e.getRawSlot() == -1 || e.getRawSlot() == 999) return;
		Player player = (Player)e.getWhoClicked();
		// Prevent hot-swapping bags.
		if (e.getHotbarButton() != -1) {
            ItemStack swapItem = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
            if (swapItem != null && HavenBags.IsBag(swapItem)) e.setCancelled(true);
        }
		
		boolean holdingBag = HavenBags.IsBag(e.getCursor());
		int limit = getBagCarryLimit(player);
		//Log.Error(Main.plugin, limit + "");
		if(limit == 0) return;
		if(e.getClickedInventory() == null) return;
		if(e.getClickedInventory().getType() == InventoryType.PLAYER && holdingBag){
			int size = NBT.GetInt(e.getCursor(), "bag-size");
			if(e.getRawSlot() >= size) return;
			if(HavenBags.GetBagsInInventory(player) >= limit) {
				//e.setCancelled(true);
				player.sendMessage(Lang.Parse(Lang.Get("carry-limit").replace("%max%", limit + ""), player));
			}
		}

		//Log.Info(Main.plugin, e.getView().getTopInventory().getType().toString());
		//Log.Info(Main.plugin, e.getView().getBottomInventory().getType().toString());
		//e.getView().getTopInventory().getType();
		//e.getView().getBottomInventory().getType();
    }
	
	@EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent e) {
		if(e.getEntityType() != EntityType.PLAYER) return;
		Player player = (Player)e.getEntity();
		int limit = getBagCarryLimit(player);
		if(limit == 0) return;
		if(HavenBags.GetBagsInInventory(player) >= limit) {
			e.setCancelled(true);
			e.getItem().setPickupDelay(100);
			player.sendMessage(Lang.Parse(Lang.Get("carry-limit").replace("%max%", limit + ""), player));
		}
		
	}
	
	@EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		int limit = getBagCarryLimit(player);
		if(limit == 0) return;
		if(HavenBags.GetBagsInInventory(player) > limit) {
			for(ItemStack item : player.getInventory().getContents()) {
				if(HavenBags.IsBag(item)) {
					ItemStack drop = item.clone();
					item.setAmount(0);
					player.getWorld().dropItem(player.getLocation(), drop);
					return;
				}
			}
		}
    }
	
	public static int getBagCarryLimit(Player player) {
	    for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
	        String permName = perm.getPermission();

	        if (permName.startsWith("havenbags.carry.")) {
	            try {
	                return Integer.parseInt(permName.substring("havenbags.carry.".length())); // Extract full number
	            } catch (NumberFormatException e) {
	                return Main.config.GetInt("carry-limit");
	            }
	        }
	    }
	    return Main.config.GetInt("carry-limit");
	}
	
	
	
}
