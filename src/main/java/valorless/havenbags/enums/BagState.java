package valorless.havenbags.enums;

import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;

public enum BagState {
	NULL, NEW, USED;

	public static BagState getState(ItemStack item) {
		if(item == null) return NULL;
		if(HavenBags.IsBag(item)) {
			if(!BagData.BagExists(HavenBags.GetBagUUID(item))) {
				return NEW;
			}else {
				return USED;
			}
		}
		return NULL;
	}
}