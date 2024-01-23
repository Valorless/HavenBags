package valorless.havenbags;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.sound.SFX;

public class HavenBags {

	public static class BagHashes {
		
		// Static list of HashCodes from bags.
		private static ArrayList<Integer> hashes = new ArrayList<Integer>();
		
		public static void Add(Integer hash) {
			if(!hashes.contains(hash)) {
				hashes.add(hash);
			}
		}
		
		public static Boolean Contains(Integer hash) {
			if(hashes.contains(hash)) return true;
			return false;
		}
		
	}
	
	public static Boolean IsBag(ItemStack item) {
		if(item == null) return false;
		if(item.hasItemMeta()) {
			if(NBT.Has(item, "bag-uuid")) {
				return true;
			}
		}
		return false;
	}

	public static void ReturnBag(ItemStack bag, Player player) {
    	if(player.getInventory().getItemInMainHand() != null) {
    		if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
    			player.getInventory().setItemInMainHand(bag);
    			return;
    		}
    	}
    	if(player.getInventory().firstEmpty() != -1) {
    		player.getInventory().addItem(bag);
    	} else {
    		player.sendMessage(Lang.Get("prefix") + Lang.Get("inventory-full"));
			SFX.Play(Main.config.GetString("inventory-full-sound"), 
					Main.config.GetFloat("inventory-full-volume").floatValue(), 
					Main.config.GetFloat("inventory-full-pitch").floatValue(), player);
    		player.getWorld().dropItem(player.getLocation(), bag);
    	}
	}
}
