package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.List;

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
import valorless.havenbags.BagData.Bag;

public class Quiver implements Listener {
	
	List<Bag> bags = new ArrayList<Bag>();
	
	/*@EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        // Check if the shooter is a player
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if(event.getConsumable().getType() != Material.ARROW) return;
        

        // Check for your virtual "quiver" (for example, stored in metadata or custom data)
        ItemStack arrowFromQuiver = getArrowFromQuiver(player);

        if (arrowFromQuiver != null) {
            // Use the arrow from the virtual container
            consumeArrowFromQuiver(player);

        } else {
            //if (!hasArrowInInventory(player)) {
            //    event.setCancelled(true); // Cancel if no arrows are available
            //}
        }
    }*/
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
		if(Main.config.GetBool("quiver-bags") == false) return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR  && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (hand == null) return;
        if (hand.getType() == Material.BOW || hand.getType() == Material.CROSSBOW) {
        	if(hand.getItemMeta() instanceof CrossbowMeta cross) {
        		if(cross.hasChargedProjectiles()) return;
        	}
        	
        	bags = HavenBags.GetBagsInInventory(player);
        	if(bags.size() == 0) return;
        	
        	if(offhand != null && offhand.getType() != Material.AIR) {
        		if(HavenBags.IsBag(offhand) && !player.getInventory().contains(Material.ARROW)) {
        			Bag quiver = new Bag(offhand, BagData.GetBag(HavenBags.GetBagUUID(offhand), offhand).getContent());
        			if(quiver != null) {
            	        if(hasArrowInSpecificQuiver(quiver)) {
            	        	if(hasFreeSpaceOrArrows(player)) {
            	        		if(!consumeArrowFromSpecificQuiver(quiver, player)) return;
            	        		player.getInventory().addItem(new ItemStack(Material.ARROW));
            	        		return;
            	        	}else {
            	        		player.sendMessage(Lang.Get("prefix") + Lang.Get("quiver-no-space"));
            	        		return;
            	        	}
            	        }
        			}
        		}
        	}
        	
            if(hasArrowInQuiver(player) && !player.getInventory().contains(Material.ARROW)) {
            	if(hasFreeSpaceOrArrows(player)) {
            		if(!consumeArrowFromQuiver(player)) return;
            		player.getInventory().addItem(new ItemStack(Material.ARROW));
            		return;
            	}else {
            		player.sendMessage(Lang.Get("prefix") + Lang.Get("quiver-no-space"));
            		return;
            	}
            }
        }
    }

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

    private boolean hasArrowInSpecificQuiver(Bag bag) {
    	for(ItemStack item : bag.content) {
    		if(item == null) continue;
    		if(item.getType() == Material.ARROW) return true;
    	}
    	return false;
    }

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

    private boolean hasArrowInQuiver(Player player) {
    	for(Bag bag : bags) {
    		for(ItemStack item : bag.content) {
        		if(item == null) continue;
    			if(item.getType() == Material.ARROW) return true;
    		}
    	}
    	return false;
    }
    
    private boolean hasFreeSpaceOrArrows(Player player) {
    	for(ItemStack item : player.getInventory().getContents()) {
    		if(item == null) return true;
    		if(item.getType() == Material.AIR) return true;
    		if(item.getType() == Material.ARROW) return true;
    	}
    	
    	return false;
    }
}
