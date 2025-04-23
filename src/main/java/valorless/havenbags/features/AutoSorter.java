package valorless.havenbags.features;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.Main.ServerVersion;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.items.ItemUtils;

public class AutoSorter {
	
	// Not used, just added.
	public static void SortBag(ItemStack bag) {
		if(!HavenBags.IsBag(bag)) return;
		String uuid = HavenBags.GetBagUUID(bag);
		Data data = BagData.GetBag(uuid, null);
		
		if(!data.hasAutoSort()) return;
		
		List<ItemStack> sorted = SortInventory(data.getContent());
		OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(data.getOwner()));
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
            @Override
            public void run(){
            	HavenBags.UpdateBagItem(bag, sorted, owner);
            }
        }, 1L);
	}

	public static List<ItemStack> SortInventory(List<ItemStack> content) {
		try {
			content.sort((item1, item2) -> {
				if (item1 == null || item1.getType() == null) return 1; // Push nulls to the end
				if (item2 == null || item2.getType() == null) return -1;

				// Get best available name: DisplayName > Custom ItemName > Material Name
				String name1 = getBestItemName(item1);
				String name2 = getBestItemName(item2);

				// Compare names
				return name1.compareToIgnoreCase(name2);
			});
		}catch(Exception e) {}

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
    	if(Main.VersionCompare(Main.server, ServerVersion.v1_20_5) >= 0) {
    		if(ItemUtils.HasItemName(item)) {
    			return ItemUtils.GetItemName(item);
    		}
    	}
        return null;
    }
}
