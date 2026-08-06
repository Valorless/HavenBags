package valorless.havenbags.features;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.items.ItemUtils;

public class AutoSorter {
	
	// Not used, just added.
	public static void sortBag(ItemStack bag) {
		if(!HavenBags.isBag(bag)) return;
		String uuid = HavenBags.getBagUUID(bag);
		Bag data = Database.getBag(uuid, null);
		
		if(!data.hasAutoSort()) return;
		
		@SuppressWarnings("unused")
		List<ItemStack> sorted = sortInventory(data.getContent());
		OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(data.getOwner()));
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
            @Override
            public void run(){
            	HavenBags.updateBagItem(bag, owner);
            }
        }, 1L);
	}

	public static List<ItemStack> sortInventory(List<ItemStack> content) {
		try {
			content.sort((item1, item2) -> {
				if (item1 == null || item1.getType() == null) return 1; // Push nulls to the end
				if (item2 == null || item2.getType() == null) return -1;
				if(PDC.has(item1, "locked")) return 1;
				if(PDC.has(item2, "locked")) return -1;

				// Get best available name: DisplayName > Custom ItemName > Material Name
				String name1 = getBestItemName(item1);
				String name2 = getBestItemName(item2);

				// Compare names
				return name1.compareToIgnoreCase(name2);
			});
		}catch(Exception e) {}

		restackInventory(content);

        return content;
    }

    private static String getBestItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (meta.hasDisplayName()) return meta.getDisplayName(); // Use display name if available

            String itemName = getCustomItemName(item);
            if (itemName != null) return itemName;
        }

        return item.getType().name();
    }

    private static String getCustomItemName(ItemStack item) {
    	if(Server.VersionHigherOrEqualTo(Version.v1_20_5)) {
    		if(ItemUtils.HasItemName(item)) {
    			return ItemUtils.GetItemName(item);
    		}
    	}
        return null;
    }

	static void restackInventory(List<ItemStack> content) {
		for(int index = 0; index < content.size()-1; index++){
			ItemStack stack1 = content.get(index);
			if(stack1 == null) continue;
			if(stack1.getType() == Material.AIR) continue;
			int two = index+1;
			ItemStack stack2 = content.get(two);
			while(stack2 == null || stack2.getType() == Material.AIR || !stack1.isSimilar(stack2)){
				if(two == content.size()-1) break;
				two++;
				stack2 = content.get(two);
			}
			if(stack1.isSimilar(stack2)){
				int amount = stack1.getAmount() + stack2.getAmount();
				if(stack1.getMaxStackSize() != stack1.getType().getMaxStackSize()) continue;
				if(amount > stack1.getMaxStackSize()){
					stack1.setAmount(stack1.getMaxStackSize());
					stack2.setAmount(amount - stack1.getMaxStackSize());
				}else{
					stack1.setAmount(amount);
					stack2.setType(Material.AIR);
				}
			}
		}
	}
}
