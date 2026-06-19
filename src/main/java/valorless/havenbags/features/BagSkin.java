package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.items.ItemUtils;

public class BagSkin implements Listener{
	
	public static void init() {
		Log.debug(Main.plugin, "[DI-19] Registering BagSkin");
		Bukkit.getServer().getPluginManager().registerEvents(new BagSkin(), Main.plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPrepareAnvilMonitor(PrepareAnvilEvent event) {
		if(!Main.config.getBool("bag-event-monitor")) return;
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
				if(HavenBags.isBag(item)) bag = item;
				if(PDC.has(item, "token-skin")) skin = item;
			}
		}
		catch(Exception e) {}
		
		if(bag == null || skin == null) return;
		if(bag.getType() == Material.AIR || skin.getType() == Material.AIR) return;
		//Log.Debug(Main.plugin, bag.toString());
		//Log.Debug(Main.plugin, skin.toString());
		
		if(!HavenBags.isBag(bag)) return;
		Log.debug(Main.plugin, "[DI-66] " + "[BagSkin] Was bag.");
		if(PDC.has(bag, "skin")) {
			if(PDC.getBoolean(bag, "skin") == false) {
				Log.debug(Main.plugin, "[DI-247] [BagUpgrade] Bag cannot be skinned.");
				return;
			}
		}
		if(BagState.getState(bag) == BagState.NEW) return;
		Log.debug(Main.plugin, "[DI-67] " + "[BagSkin] BagState.Used");
		if(!PDC.has(skin, "token-skin")) return;
		Log.debug(Main.plugin, "[DI-68] " + "[BagSkin] Found skin.");
		
		ItemStack result = bag.clone();
		
		event.getInventory().setRepairCost(0);
		event.setResult(getResult(result, skin));
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
				if(PDC.has(item, "token-skin")) skin = item;
			}
		}
		catch(Exception e) {}
		if(clicked == null || skin == null) return;
		if(!PDC.has(skin, "token-skin")) return; // If the item in slot 2 isnt a skin token, return.
		Log.debug(Main.plugin, "[DI-69] " + "[BagSkin] is bag?");
		if(HavenBags.isBag(clicked)) {
			ItemMeta meta = clicked.getItemMeta();
			String value = PDC.getString(skin, "token-skin");
			try {
				int cmd = Integer.parseInt(value);
				if(value != null && meta.hasCustomModelData()) {
					Log.debug(Main.plugin, "[DI-70] " + "[BagSkin] CustomModelData Skin.");
					meta.setCustomModelData(cmd);
					clicked.setItemMeta(meta);
				}
			}catch(Exception e) {
				if((long) value.length() < 30) {
					Log.debug(Main.plugin, "[DI-75] [UpgradeGUI] Textures.yml Skin.");
					String texture = Main.textures.getString(String.format("textures.%s", value));
					if(BagState.getState(clicked) == BagState.NEW) {
						HeadCreator.setTextureValue(clicked, texture);
					}else {
						Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(texture);
					}
				}else {
					Log.debug(Main.plugin, "[DI-75] [UpgradeGUI] Texture Skin.");
					if(Base64Validator.isValidBase64(value)) {
						if(BagState.getState(clicked) == BagState.NEW) {
							HeadCreator.setTextureValue(clicked, value);
						}else {
							Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(value);
						}
					}else {
						Log.debug(Main.plugin, "[DI-76] [UpgradeGUI] Invalid Skin.");
						return;
					}
				}
				Log.debug(Main.plugin, "[DI-71] " + "[BagSkin] Texture Skin.");
				Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(value);
			}
			Log.debug(Main.plugin, "[DI-72] " + "[BagSkin] Applied skin!");
		}
		
	}
	
	ItemStack getResult(ItemStack item, ItemStack skin) {
		Log.debug(Main.plugin, "[DI-73] " + "[BagSkin] Preparing Result.");
		ItemMeta meta = item.getItemMeta();
		String value = PDC.getString(skin, "token-skin");
		String type = PDC.has(skin, "token-type") ? PDC.getString(skin, "token-type") : null;
		
		if(type != null) {
			if(type.equalsIgnoreCase("texture")) {
				if((long) value.length() < 30) {
					Log.debug(Main.plugin, "[DI-75] [UpgradeGUI] Textures.yml Skin.");
					String texture = Main.textures.getString(String.format("textures.%s", value));
					HeadCreator.setTextureValue(item, texture);
				}else {
					Log.debug(Main.plugin, "[DI-75] [UpgradeGUI] Texture Skin.");
					if(Base64Validator.isValidBase64(value)) {
						HeadCreator.setTextureValue(item, value);
					}else {
						Log.debug(Main.plugin, "[DI-76] [UpgradeGUI] Invalid Skin.");
						item = new ItemStack(Material.AIR);
					}
				}
			}
			else if(type.equalsIgnoreCase("modeldata")) {
				try {
					int cmd = Integer.parseInt(value);
					if(value != null) {
						Log.debug(Main.plugin, "[DI-74] " + "[BagSkin] CustomModelData Skin.");
						meta.setCustomModelData(cmd);
						item.setItemMeta(meta);
					}
				}catch(Exception e) {}
			}
			else if(type.equalsIgnoreCase("itemmodel")) {
				Log.debug(Main.plugin, "[DI-249] " + "[BagSkin] ItemModel Skin.");
				ItemUtils.SetItemModel(item, value);
			}
			
		}else { // Handle old tokens
			try {
				int cmd = Integer.parseInt(value);
				if(value != null) {
					Log.debug(Main.plugin, "[DI-74] " + "[BagSkin] CustomModelData Skin.");
					meta.setCustomModelData(cmd);
					item.setItemMeta(meta);
				}
			}catch(Exception e) {
				if((long) value.length() < 30) {
					Log.debug(Main.plugin, "[DI-75] [UpgradeGUI] Textures.yml Skin.");
					String texture = Main.textures.getString(String.format("textures.%s", value));
					HeadCreator.setTextureValue(item, texture);
				}else {
					Log.debug(Main.plugin, "[DI-75] [UpgradeGUI] Texture Skin.");
					if(Base64Validator.isValidBase64(value)) {
						HeadCreator.setTextureValue(item, value);
					}else {
						Log.debug(Main.plugin, "[DI-76] [UpgradeGUI] Invalid Skin.");
						item = new ItemStack(Material.AIR);
					}
				}
			}

		}
		
		return item;
	}
}
