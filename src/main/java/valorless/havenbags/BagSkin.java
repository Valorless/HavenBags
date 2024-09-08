package valorless.havenbags;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.HavenBags.BagState;
import valorless.havenbags.utils.Base64Validator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class BagSkin implements Listener{
	
	@EventHandler
	public void onPrepareAnvil(PrepareAnvilEvent event) {
		ItemStack bag = event.getInventory().getItem(0);
		ItemStack skin = event.getInventory().getItem(1);
		if(bag == null || skin == null) return;
		//Log.Debug(Main.plugin, bag.toString());
		//Log.Debug(Main.plugin, skin.toString());
		
		if(!HavenBags.IsBag(bag)) return;
		Log.Debug(Main.plugin, "[DI-66] " + "[BagSkin] Was bag.");
		if(HavenBags.BagState(bag) == BagState.New) return;
		Log.Debug(Main.plugin, "[DI-67] " + "[BagSkin] BagState.Used");
		if(!NBT.Has(skin, "bag-token-skin")) return;
		Log.Debug(Main.plugin, "[DI-68] " + "[BagSkin] Found skin.");
		
		ItemStack result = bag.clone();
		
		event.getInventory().setRepairCost(0);
		event.setResult(GetResult(result, skin));
		
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getInventory().getType() != InventoryType.ANVIL) return;
		//Log.Debug(Main.plugin, event.getRawSlot()+ "");
		if(event.getRawSlot() != 2) return;
		ItemStack clicked = event.getCurrentItem();
		ItemStack skin = event.getInventory().getItem(1);
		if(clicked == null || skin == null) return;
		if(!NBT.Has(skin, "bag-token-skin")) return; // If the item in slot 2 isnt a skin token, return.
		Log.Debug(Main.plugin, "[DI-69] " + "[BagSkin] is bag?");
		if(HavenBags.IsBag(clicked)) {
			ItemMeta meta = clicked.getItemMeta();
			String value = NBT.GetString(skin, "bag-token-skin");
			if(isNumber(value)) {
				int cmd = Integer.valueOf(value);
				if(value != null && meta.hasCustomModelData()) {
					Log.Debug(Main.plugin, "[DI-70] " + "[BagSkin] CustomModelData Skin.");
					meta.setCustomModelData(cmd);
					clicked.setItemMeta(meta);
				}
			}else {
				Log.Debug(Main.plugin, "[DI-71] " + "[BagSkin] Texture Skin.");
				BagData.GetBag(HavenBags.GetBagUUID(clicked), clicked).setTexture(value);
			}
			Log.Debug(Main.plugin, "[DI-72] " + "[BagSkin] Applied skin!");
		}
		
	}
	
	ItemStack GetResult(ItemStack item, ItemStack skin) {
		Log.Debug(Main.plugin, "[DI-73] " + "[BagSkin] Preparing Result.");
		ItemMeta meta = item.getItemMeta();
		String value = NBT.GetString(skin, "bag-token-skin");
		if(isNumber(value)) {
			int cmd = Integer.valueOf(value);
			if(value != null) {
				Log.Debug(Main.plugin, "[DI-74] " + "[BagSkin] CustomModelData Skin.");
				meta.setCustomModelData(cmd);
				item.setItemMeta(meta);
			}
		}else {
			if(Base64Validator.isValidBase64(value)) {
				Log.Debug(Main.plugin, "[DI-75] " + "[BagSkin] Texture Skin.");
				BagData.setTextureValue(item, value);
			}else {
				Log.Debug(Main.plugin, "[DI-76] " + "[BagSkin] Invalid Skin.");
				item = new ItemStack(Material.AIR);
			}
		}
		
		return item;
	}
	
	public static boolean isNumber(Object obj) {
        return obj instanceof Integer;
    }
}
