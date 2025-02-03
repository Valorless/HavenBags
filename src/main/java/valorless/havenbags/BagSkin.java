package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPrepareAnvilMonitor(PrepareAnvilEvent event) {
		if(!Main.config.GetBool("bag-event-monitor")) return;
		onPrepareAnvil(event);
	}
	
	@EventHandler
	public void onPrepareAnvil(PrepareAnvilEvent event) {
		if(event.getInventory().getItem(0) == null) return;
		if(event.getInventory().getItem(1) == null) return;
		ItemStack bag = null;
		ItemStack skin = null;
		try {
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(0), event.getInventory().getItem(1)))) {
				if(HavenBags.IsBag(item)) bag = item;
				if(NBT.Has(item, "bag-token-skin")) skin = item;
			}
		}
		catch(Exception e) {}
		if(bag == null || skin == null) return;
		if(bag.getType() == Material.AIR || skin.getType() == Material.AIR) return;
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
		if(event.getInventory().getItem(0) == null) return;
		if(event.getInventory().getItem(1) == null) return;
		//Log.Debug(Main.plugin, event.getRawSlot()+ "");
		if(event.getRawSlot() != 2) return;
		ItemStack clicked = event.getCurrentItem();
		ItemStack skin = null;
		try {
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(0), event.getInventory().getItem(1)))) {
				if(NBT.Has(item, "bag-token-skin")) skin = item;
			}
		}
		catch(Exception e) {}
		if(clicked == null || skin == null) return;
		if(!NBT.Has(skin, "bag-token-skin")) return; // If the item in slot 2 isnt a skin token, return.
		Log.Debug(Main.plugin, "[DI-69] " + "[BagSkin] is bag?");
		if(HavenBags.IsBag(clicked)) {
			ItemMeta meta = clicked.getItemMeta();
			String value = NBT.GetString(skin, "bag-token-skin");
			try {
				int cmd = Integer.valueOf(value);
				if(value != null && meta.hasCustomModelData()) {
					Log.Debug(Main.plugin, "[DI-70] " + "[BagSkin] CustomModelData Skin.");
					meta.setCustomModelData(cmd);
					clicked.setItemMeta(meta);
				}
			}catch(Exception e) {
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
		try {
			int cmd = Integer.valueOf(value);
			if(value != null) {
				Log.Debug(Main.plugin, "[DI-74] " + "[BagSkin] CustomModelData Skin.");
				meta.setCustomModelData(cmd);
				item.setItemMeta(meta);
			}
		}catch(Exception e) {
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
}
