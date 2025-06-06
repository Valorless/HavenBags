package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.havenbags.BagData.Bag;

public class Quiver implements Listener {
	
	List<Bag> bags = new ArrayList<Bag>();
	
	public static final List<Material> Projectiles = List.of(Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW);
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-221] Registering Quiver");
		Bukkit.getServer().getPluginManager().registerEvents(new Quiver(), Main.plugin);
	}
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
		if(Main.config.GetBool("quiver-bags") == false) return;
		if(BagData.isReady() == false) {
			return;
		}
		if(event.getAction() != Action.RIGHT_CLICK_AIR  && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.CREATIVE) return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if(hand == null) return;
        if(hand.getType() != Material.BOW && hand.getType() != Material.CROSSBOW) return;
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if(hand.getItemMeta() instanceof CrossbowMeta cross) {
    		if(cross.hasChargedProjectiles()) return;
    	}
    	
		Log.Debug(Main.plugin, "[DI-235] Fetching bags.");
		Log.Debug(Main.plugin, "[DI-236] " + player.getName());
    	bags = HavenBags.GetBagsDataInInventory(player);
    	if(bags.size() == 0) return;

    	if(Main.config.GetInt("quiver-shield-fix") == 1){ // Semi-fix
    		if(offhand != null && offhand.getType() != Material.AIR && !hasProjectile(player)) {
    			if (offhand.getType() == Material.SHIELD && hand.getType() == Material.BOW ||
    					offhand.getType() == Material.SHIELD && hand.getType() == Material.CROSSBOW) {
    				event.setCancelled(true);
    			}
    		}
    	}
    	else if(Main.config.GetInt("quiver-shield-fix") == 2){ // Disable quiver
    		if(offhand != null && offhand.getType() != Material.AIR) {
    			if (offhand.getType() == Material.SHIELD) {
    				return;
    			}
    		}
    	}
    	
    	if(offhand != null && offhand.getType() != Material.AIR) {
    		if(HavenBags.IsBag(offhand) && !hasProjectile(player)) {
				Bag quiver = new Bag(offhand, BagData.GetBag(HavenBags.GetBagUUID(offhand), offhand).getContent());
				if(quiver != null) {
					if(hasFreeSpace(player)) {
						//Log.Info(Main.plugin, "Free space");
						ItemStack arrow = drawArrow(hand, quiver, player);
						//Log.Info(Main.plugin, arrow.toString());
						if(arrow == null) return;
						player.getInventory().addItem(arrow);
						return;
					}else {
						player.sendMessage(Lang.Get("prefix") + Lang.Get("quiver-no-space"));
						return;
					}
				}
			}
    	}
    	if(!hasProjectile(player)) {
			if(hasFreeSpace(player)) {
				for(Bag bag : bags) {
					//Log.Info(Main.plugin, "Free space");
					ItemStack arrow = drawArrow(hand, bag, player);
					//Log.Info(Main.plugin, (arrow == null) ? "null" : arrow.toString());
					if(arrow == null) continue;
					player.getInventory().addItem(arrow);
					return;
				}
			}else {
				player.sendMessage(Lang.Get("prefix") + Lang.Get("quiver-no-space"));
				return;
			}
		}
    }

    @SuppressWarnings("unused")
	private boolean consumeArrowFromSpecificQuiver(Bag bag, Player player) {
    	for(ItemStack item : bag.content) {
    		if(item == null) continue;
    		if(item.getType() == Material.ARROW) {
    			int amount = item.getAmount() -1;
    			item.setAmount(amount);
    			BagData.UpdateBag(bag.item, bag.content);
    			HavenBags.UpdateBagLore(bag.item, player);
    			return true;
    		}
    	}
    	return false;
    }

    @SuppressWarnings("unused")
	private boolean hasArrowInSpecificQuiver(Bag bag) {
    	for(ItemStack item : bag.content) {
    		if(item == null) continue;
    		if(item.getType() == Material.ARROW) return true;
    	}
    	return false;
    }

    @SuppressWarnings("unused")
	private boolean consumeArrowFromQuiver(Player player) {
    	for(Bag bag : bags) {
    		for(ItemStack item : bag.content) {
        		if(item == null) continue;
    			if(item.getType() == Material.ARROW) {
    				int amount = item.getAmount() -1;
    				item.setAmount(amount);
        			BagData.UpdateBag(bag.item, bag.content);
        			HavenBags.UpdateBagLore(bag.item, player);
    				return true;
    			}
    		}
    	}
    	return false;
    }

    @SuppressWarnings("unused")
	private boolean hasArrowInQuiver(Player player) {
    	for(Bag bag : bags) {
    		for(ItemStack item : bag.content) {
        		if(item == null) continue;
    			if(item.getType() == Material.ARROW) return true;
    		}
    	}
    	return false;
    }
    
    private boolean hasProjectile(Player player) {
    	for(Material valid : Projectiles) {
    		if(player.getInventory().contains(valid)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean hasFreeSpace(Player player) {
    	for(ItemStack item : player.getInventory().getContents()) {
    		if(item == null) return true;
    		if(item.getType() == Material.AIR) return true;
    		//if(item.getType() == Material.ARROW) return true;
    	}
    	
    	return false;
    }
    
    private ItemStack drawArrow(ItemStack hand, Bag bag, Player player) {
    	for(ItemStack item : bag.content) {
    		if(item == null) continue;
    		for(Material valid : Projectiles) {
    			if(item.getType() == valid) {
    				ItemStack clone = item.clone();
    				int amount = item.getAmount() -1;
    				item.setAmount(amount);
    				BagData.UpdateBag(bag.item, bag.content);
    				HavenBags.UpdateBagLore(bag.item, player);
    				//Log.Info(Main.plugin, item.toString());

    				clone.setAmount(1);
    				return clone;
    			}
    		}

    	}
    	
    	return null;
    }
}
