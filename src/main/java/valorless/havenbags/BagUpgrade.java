package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.HavenBags.BagState;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class BagUpgrade implements Listener{
	
	@EventHandler
	public void onPrepareAnvil(PrepareAnvilEvent event) {
		if(Main.config.GetBool("upgrades.enabled") == false) return;
		ItemStack bag = event.getInventory().getItem(0);
		ItemStack upgrade = event.getInventory().getItem(1);
		if(bag == null || upgrade == null) return;
		Log.Debug(Main.plugin, bag.toString());
		Log.Debug(Main.plugin, upgrade.toString());
		if(!HavenBags.IsBag(bag)) return;
		int size = NBT.GetInt(bag, "bag-size");
		if(size == 54) return;
		if(HavenBags.BagState(bag) == BagState.New) return;
		if(!event.getView().getPlayer().hasPermission(String.format("havenbags.upgrade.%s", size))) return;

		String[] split = Main.config.GetString(String.format("upgrades.from-%s-to-%s", size, size+9)).split(":");
		int cmd = 0;
		Material requirement = Material.getMaterial(split[0]);
		int amount = Integer.valueOf(split[1]);
		if(split.length == 3) {
			cmd = Integer.valueOf(split[2]);
			if(upgrade.hasItemMeta()) {
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
		Log.Debug(Main.plugin, event.getRawSlot()+ "");
		if(event.getRawSlot() != 2) return;
		ItemStack clicked = event.getCurrentItem();
		if(clicked == null) return;
		Log.Debug(Main.plugin, "is bag?");
		if(HavenBags.IsBag(clicked)) {
			if(!event.getView().getPlayer().hasPermission(String.format("havenbags.upgrade.%s", NBT.GetInt(clicked, "bag-size")))) {
				event.setCancelled(true);
				return;
			}
			Log.Debug(Main.plugin, "was bag");
			String owner = NBT.GetString(clicked, "bag-owner");
			BagData.GetBag(HavenBags.GetBagUUID(clicked), clicked).getData().Set("size", NBT.GetInt(clicked, "bag-size"));
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
		List<String> lore = meta.getLore();
		List<String> newLore = new ArrayList<String>();
		String owner = NBT.GetString(bag, "bag-owner");

		String[] split = Main.config.GetString(String.format("upgrades.from-%s-to-%s", from, to)).split(":");
		Material requirement = Material.getMaterial(split[0]);
		int amount = Integer.valueOf(split[1]);
		if(upgrade.getType() == requirement && upgrade.getAmount() == amount) {
			String[] s = Lang.Get("bag-size").split("%size%"); 
			for(String line : lore) {
				String l = Lang.RemoveColorFormatting(line);
				Log.Debug(Main.plugin, l);
				s[0] = Lang.RemoveColorFormatting(s[0].replace("&", "§"));
				Log.Debug(Main.plugin, s[0]);
				
				
				if(l.contains(s[0])) {
					List<Placeholder> ph = new ArrayList<Placeholder>();
					ph.add(new Placeholder("%size%", "" + to));
					if(owner.equalsIgnoreCase("ownerless") || owner.equalsIgnoreCase("null")) {
						newLore.add(Lang.Parse(Lang.Get("bag-size"), ph));
					}else {
						newLore.add(Lang.Parse(Lang.Get("bag-size"), ph, Bukkit.getOfflinePlayer(UUID.fromString(owner))));
					}
					Log.Debug(Main.plugin, line);
					
				}else {
					newLore.add(line);
				}
			}
			meta.setLore(newLore);
			item.setItemMeta(meta);
			NBT.SetInt(item, "bag-size", to);
			if(Main.config.GetBool("bag-textures.enabled") && !Main.config.GetBool("upgrades.keep-texture")) {

				if(!owner.equalsIgnoreCase("ownerless") && !owner.equalsIgnoreCase("null")) {
					//BagData.GetBag(HavenBags.GetBagUUID(bag), bag).setTexture(Main.config.GetString(String.format("bag-textures.size-%s", to)));
					BagData.setTextureValue(item, Main.config.GetString(String.format("bag-textures.size-%s", to)));
				}else {
					if(!owner.equalsIgnoreCase("null")) {
						//BagData.GetBag(HavenBags.GetBagUUID(bag), bag).setTexture(Main.config.GetString(String.format("bag-textures.size-ownerless-%s", to)));
					}
					BagData.setTextureValue(item, Main.config.GetString(String.format("bag-textures.size-ownerless-%s", to)));
				}
			}
		}
		return item;
	}
}
