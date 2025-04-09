package valorless.havenbags.features;

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

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.HavenBags.BagState;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class BagUpgrade implements Listener{
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPrepareAnvilMonitor(PrepareAnvilEvent event) {
		if(!Main.config.GetBool("bag-event-monitor")) return;
		onPrepareAnvil(event);
	}
	
	@EventHandler
	public void onPrepareAnvil(PrepareAnvilEvent event) {
		if(Main.config.GetBool("upgrades.enabled") == false) return;
		if(event.getInventory().getItem(0) == null) return;
		if(event.getInventory().getItem(1) == null) return;
		ItemStack bag = null;
		ItemStack upgrade = null;
		try {
			Log.Debug(Main.plugin, "[DI-237] [BagUpgrade] Checking items.");
			int i = 0;
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(0), event.getInventory().getItem(1)))) {
				if(HavenBags.IsBag(item)) {
					Log.Debug(Main.plugin, "[DI-237] [BagUpgrade] Bag in slot " + i + ".");
					bag = item;
				}
				else upgrade = item;
				i++;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(bag == null || upgrade == null) return;
		Log.Debug(Main.plugin, "[DI-238] [BagUpgrade] Is non-bag item a token?");
		if(NBT.Has(upgrade, "bag-token-skin")) return; // If the item in slot 2 is a skin token, return.
		//Log.Debug(Main.plugin, bag.toString());
		//Log.Debug(Main.plugin, upgrade.toString());
		if(NBT.Has(bag, "bag-upgrade")) {
			if(NBT.GetBool(bag, "bag-upgrade") == false) {
				Log.Debug(Main.plugin, "[DI-239] [BagUpgrade] Bag cannot upgrade.");
				return;
			}
		}
		int size = NBT.GetInt(bag, "bag-size");
		Log.Debug(Main.plugin, "[DI-240] [BagUpgrade] Is bag max size?");
		if(size == 54) return;
		Log.Debug(Main.plugin, "[DI-241] [BagUpgrade] Is bag used?");
		if(HavenBags.BagState(bag) == BagState.New) return;
		Log.Debug(Main.plugin, "[DI-242] [BagUpgrade] Is player allowed to upgrade this size?");
		if(!event.getView().getPlayer().hasPermission(String.format("havenbags.upgrade.%s", size))) return;

		String[] split = Main.config.GetString(String.format("upgrades.from-%s-to-%s", size, size+9)).split(":");
		int cmd = 0;
		Material requirement = Material.getMaterial(split[0]);
		int amount = Integer.valueOf(split[1]);
		if(split.length == 3) {
			cmd = Integer.valueOf(split[2]);
			if(upgrade.hasItemMeta()) {
				if(upgrade.getItemMeta().hasCustomModelData() == false) return;
				if(upgrade.getItemMeta().getCustomModelData() != cmd) return;
			}else return;
		}
		if(upgrade.getType() != requirement || upgrade.getAmount() != amount) return;
		ItemStack result = bag.clone();
		
		event.getInventory().setRepairCost(0);
		event.setResult(GetResult(result, size, size + 9, bag, upgrade));
		
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(Main.config.GetBool("upgrades.enabled") == false) return;
		if(event.getInventory().getType() != InventoryType.ANVIL) return;
		if(event.getInventory().getItem(0) == null) return;
		if(event.getInventory().getItem(1) == null) return;
		//Log.Debug(Main.plugin, event.getRawSlot()+ "");
		if(event.getRawSlot() != 2) return;
		ItemStack clicked = event.getCurrentItem();
		ItemStack bag = null;
		ItemStack upgrade = null;
		try {
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(0), event.getInventory().getItem(1)))) {
				if(HavenBags.IsBag(item)) bag = item;
				else upgrade = item;
			}
		}
		catch(Exception e) {}
		if(clicked == null || upgrade == null || bag == null) return;
		if(NBT.Has(upgrade, "bag-token-skin")) return; // If the item in slot 2 is a skin token, return.
		Log.Debug(Main.plugin, "[DI-77] " + "[BagUpgrade] is bag?");
		if(HavenBags.IsBag(clicked)) {
			Log.Debug(Main.plugin, "[DI-78] " + "[BagUpgrade] was bag");
			int size = NBT.GetInt(bag, "bag-size");

			Log.Debug(Main.plugin, "[DI-79] " + "[BagUpgrade] Upgrade item is correct?");
			String[] split = Main.config.GetString(String.format("upgrades.from-%s-to-%s", size, size+9)).split(":");
			int cmd = 0;
			Material requirement = Material.getMaterial(split[0]);
			int amount = Integer.valueOf(split[1]);


			Log.Debug(Main.plugin, "[DI-80] " + "[BagUpgrade] Checking Type and Amount");
			if(upgrade.getType() != requirement || upgrade.getAmount() != amount) return;
			
			if(split.length == 3) {
				Log.Debug(Main.plugin, "[DI-81] " + "[BagUpgrade] Checking CustomModelData");
				cmd = Integer.valueOf(split[2]);
				Log.Debug(Main.plugin, "[DI-82] " + "[BagUpgrade] " + cmd);
				if(upgrade.hasItemMeta()) {
					if(upgrade.getItemMeta().hasCustomModelData()) {
						if(upgrade.getItemMeta().getCustomModelData() != cmd) return;
					}else return;
				}else return;
			}
			
			String owner = NBT.GetString(clicked, "bag-owner");
			BagData.GetBag(HavenBags.GetBagUUID(clicked), clicked).setSize(NBT.GetInt(clicked, "bag-size"));
			//BagData.GetBag(HavenBags.GetBagUUID(clicked), clicked).getData().Set("size", NBT.GetInt(clicked, "bag-size"));
			Log.Debug(Main.plugin, "[DI-83] " + "[BagUpgrade] Size set to " + NBT.GetInt(clicked, "bag-size"));

			if(Main.weight.GetBool("weight-per-size")) {
				BagData.SetWeightMax(HavenBags.GetBagUUID(clicked), Main.weight.GetFloat(String.format("weight-size-%s", NBT.GetInt(clicked, "bag-size"))));
				Log.Debug(Main.plugin, "[DI-84] " + "[BagUpgrade] Weight Limit set to " + Main.weight.GetFloat(String.format("weight-size-%s", NBT.GetInt(clicked, "bag-size"))));
			}
			if(clicked.getType() == Material.PLAYER_HEAD) {
				if(Main.config.GetBool("bag-textures.enabled") && !Main.config.GetBool("upgrades.keep-texture")) {
					if(!owner.equalsIgnoreCase("ownerless")) {
						BagData.GetBag(HavenBags.GetBagUUID(clicked), clicked).setTexture(Main.config.GetString(String.format("bag-textures.size-%s", NBT.GetInt(clicked, "bag-size"))));
					}else {
						BagData.GetBag(HavenBags.GetBagUUID(clicked), clicked).setTexture(Main.config.GetString(String.format("bag-textures.size-ownerless-%s", NBT.GetInt(clicked, "bag-size"))));
					}
				}
			}
			HavenBags.UpdateBagLore(clicked, null);
		}
		
	}
	
	ItemStack GetResult(ItemStack item, int from, int to, ItemStack bag, ItemStack upgrade) {

		ItemMeta meta = item.getItemMeta();
		//List<String> lore = meta.getLore();
		List<String> newLore = new ArrayList<String>();
		String owner = NBT.GetString(bag, "bag-owner");

		String[] split = Main.config.GetString(String.format("upgrades.from-%s-to-%s", from, to)).split(":");
		Material requirement = Material.getMaterial(split[0]);
		int amount = Integer.valueOf(split[1]);
		if(upgrade.getType() == requirement && upgrade.getAmount() == amount) {
			/*String[] s = Lang.Get("bag-size").split("%size%"); 
			for(String line : lore) {
				String l = Lang.RemoveColorFormatting(line);
				Log.Debug(Main.plugin, "[BagUpgrade] " + l);
				s[0] = Lang.RemoveColorFormatting(s[0].replace("&", "§"));
				Log.Debug(Main.plugin, "[BagUpgrade] " + s[0]);
				
				
				if(l.contains(s[0])) {
					List<Placeholder> ph = new ArrayList<Placeholder>();
					ph.add(new Placeholder("%size%", "" + to));
					if(owner.equalsIgnoreCase("ownerless") || owner.equalsIgnoreCase("null")) {
						newLore.add(Lang.Parse(Lang.Get("bag-size"), ph));
					}else {
						newLore.add(Lang.Parse(Lang.Get("bag-size"), ph, Bukkit.getOfflinePlayer(UUID.fromString(owner))));
					}
					Log.Debug(Main.plugin, "[BagUpgrade] " + line);
					
				}else {
					newLore.add(line);
				}
			}*/
			meta.setLore(newLore);
			item.setItemMeta(meta);
			NBT.SetInt(item, "bag-size", to);
			if(Main.weight.GetBool("weight-per-size")) {
				NBT.SetDouble(item, "bag-weight-limit", Main.weight.GetFloat(String.format("weight-size-%s", to)));
			}
			HavenBags.UpdateBagLore(item, null, true);
			if(Main.config.GetBool("bag-textures.enabled") && !Main.config.GetBool("upgrades.keep-texture")) {
				if(owner.equalsIgnoreCase("ownerless")) {
					//BagData.GetBag(HavenBags.GetBagUUID(bag), bag).setTexture(Main.config.GetString(String.format("bag-textures.size-%s", to)));
					BagData.setTextureValue(item, Main.config.GetString(String.format("bag-textures.size-ownerless-%s", to)));
				}else {
					BagData.setTextureValue(item, Main.config.GetString(String.format("bag-textures.size-%s", to)));
				}
			}
		}
		return item;
	}
}
