package valorless.havenbags.gui;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.Experimental;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.logging.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Experimental("In testing")
public class FeaturesGUI implements Listener {

	public static class ToggleButton{
		ItemStack enabled;
		ItemStack disabled;
		public ToggleButton(ItemStack enabled, ItemStack disabled) {
			this.enabled = enabled;
			this.disabled = disabled;
		}
	}

	/**
	 * Static class to keep track of all open UpgradeGUIs.
	 * Used to close all open GUIs when the server stops or the plugin is disabled.
	 */
	public static class OpenGUIs {

		// Static map of Players -> their open UpgradeGUI
		private static final HashMap<Player, FeaturesGUI> guis = new HashMap<>();

		public static void Add(Player player, FeaturesGUI gui) {
			guis.put(player, gui); // replaces existing if already present
		}

		public static boolean Contains(Player player) {
			return guis.containsKey(player);
		}

		public static boolean Contains(FeaturesGUI gui) {
			return guis.containsValue(gui);
		}

		public static FeaturesGUI Get(Player player) {
			return guis.get(player); // returns null if not present
		}

		public static void Remove(Player player) {
			guis.remove(player);
		}

		public static void Remove(FeaturesGUI gui) {
			guis.entrySet().removeIf(entry -> entry.getValue().equals(gui));
		}

		public static void CloseAll() {
			for (FeaturesGUI gui : guis.values()) {
				gui.close();
			}
		}
	}


	Inventory inv;
	int invSize = 27; // 3 rows of 9 slots
	Player player;
	ItemStack bagItem;
	Data data;

	int autoPickupSlot;
	ToggleButton autoPickup;

	int magnetSlot;
	ToggleButton magnet;

	int autoSortSlot;
	ToggleButton autoSort;

	int refillingSlot;
	ToggleButton refilling;

	String filler = "GRAY_STAINED_GLASS_PANE";

	public FeaturesGUI(Player player, ItemStack bagItem, Data bagData) {
		this.data = bagData;
		this.bagItem = bagItem;
		this.player = player;

		this.invSize = Main.config.getInt("features-gui.gui-size");
		this.autoPickupSlot = Main.config.getInt("features-gui.slots.auto-pickup.slot");
		this.autoPickup = createButton("auto-pickup");

		this.magnetSlot = Main.config.getInt("features-gui.slots.magnet.slot");
		this.magnet = createButton("magnet");

		this.autoSortSlot = Main.config.getInt("features-gui.slots.auto-sort.slot");
		this.autoSort = createButton("auto-sort");

		this.refillingSlot = Main.config.getInt("features-gui.slots.refilling.slot");
		this.refilling = createButton("refilling");

		this.inv = Bukkit.createInventory(player, invSize, Lang.Parse(Main.config.getString("features-gui.title"), player));

		this.filler = Main.config.getString("features-gui.filler");
		ItemStack fillerItem;
		if(filler.startsWith("nexo:")){
			fillerItem = NexoItems.itemFromId(filler.replace("nexo:", "")).build();
		}else{
			fillerItem = new ItemStack(Material.valueOf(filler.toUpperCase()));
			ItemMeta fillMeta = fillerItem.getItemMeta();
			fillMeta.setDisplayName(" ");
			try {
				if (Server.VersionHigherOrEqualTo(Version.v1_21)) {
					Method setHideTooltip = fillMeta.getClass().getMethod("setHideTooltip", boolean.class);
					setHideTooltip.setAccessible(true);
					setHideTooltip.invoke(fillMeta, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			fillerItem.setItemMeta(fillMeta);
		}

		for(int i = 0; i < invSize; i++) {
			inv.setItem(i, fillerItem);
		}

		Object[] f = Main.config.getSection("features-gui.custom-filler").getKeys(false).toArray();
		for(int i = 0; i < f.length; i++) {
			String slot = String.valueOf(f[i]);
			String customFiller = Main.config.getString(String.format("features-gui.custom-filler.%s", slot));
			ItemStack customFillerItem;

			if(customFiller.startsWith("nexo:")){
				customFillerItem = NexoItems.itemFromId(filler.replace("nexo:", "")).build();
			}else{
				customFillerItem = new ItemStack(Material.valueOf(customFiller.toUpperCase()));
				ItemMeta fillMeta = customFillerItem.getItemMeta();
				fillMeta.setDisplayName(" ");
				try {
					if (Server.VersionHigherOrEqualTo(Version.v1_21)) {
						Method setHideTooltip = fillMeta.getClass().getMethod("setHideTooltip", boolean.class);
						setHideTooltip.setAccessible(true);
						setHideTooltip.invoke(fillMeta, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				customFillerItem.setItemMeta(fillMeta);
			}

			inv.setItem(Integer.parseInt(slot), customFillerItem);
		}

		if(autoPickupSlot != -1) inv.setItem(autoPickupSlot, data.hasAutoPickup() ? autoPickup.enabled : autoPickup.disabled);
		if(magnetSlot != -1) inv.setItem(magnetSlot, data.hasMagnet() ? magnet.enabled : magnet.disabled);
		if(autoSortSlot != -1) inv.setItem(autoSortSlot, data.hasAutoSort() ? autoSort.enabled : autoSort.disabled);
		if(refillingSlot != -1) inv.setItem(refillingSlot, data.hasRefill() ? refilling.enabled : refilling.disabled);

		BukkitRunnable task = new BukkitRunnable() {
			@Override
			public void run() {
				player.openInventory(inv);
			}
		};

		task.runTaskLater(Main.plugin, 1); // Delay to ensure the inventory is ready before opening

		OpenGUIs.Add(player, this);

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		Log.debug(Main.plugin, "[FeaturesGUI] Opening FeaturesGUI for " + player.getName());
	}

	public Inventory GetInv() {
		return inv;
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!event.getInventory().equals(inv)) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().equals(inv)) return;
		if(!(event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT)) {
			event.setCancelled(true);
			return;
		}

		if(event.getRawSlot() != autoPickupSlot &&
				event.getRawSlot() != magnetSlot &&
				event.getRawSlot() != autoSortSlot &&
				event.getRawSlot() != refillingSlot &&
				event.getRawSlot() < invSize) {
			return;
		}

		event.setCancelled(true);

		if(event.getRawSlot() == autoPickupSlot) {
			//data.setAutopickup(!data.isAutoPickup());
		}else if(event.getRawSlot() == magnetSlot) {
			if(player.hasPermission("havenbags.features.magnet")) {
				data.setMagnet(!data.hasMagnet());
			}else{
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("no-permission"), player));
				return;
			}
		}else if(event.getRawSlot() == autoSortSlot) {
			if(player.hasPermission("havenbags.features.autosort")) {
				data.setAutoSort(!data.hasAutoSort());
			}else{
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("no-permission"), player));
				return;
			}
		}else if(event.getRawSlot() == refillingSlot) {
			if(player.hasPermission("havenbags.features.refill")) {
				data.setRefill(!data.hasRefill());
			}else{
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("no-permission"), player));
				return;
			}
		}

		if(autoPickupSlot != -1) inv.setItem(autoPickupSlot, data.hasAutoPickup() ? autoPickup.enabled : autoPickup.disabled);
		if(magnetSlot != -1) inv.setItem(magnetSlot, data.hasMagnet() ? magnet.enabled : magnet.disabled);
		if(autoSortSlot != -1) inv.setItem(autoSortSlot, data.hasAutoSort() ? autoSort.enabled : autoSort.disabled);
		if(refillingSlot != -1) inv.setItem(refillingSlot, data.hasRefill() ? refilling.enabled : refilling.disabled);

		HavenBags.UpdateBagLore(bagItem, player);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getInventory().equals(inv)) return;
		close();
	}

	public void close(){
		Log.debug(Main.plugin, "[FeaturesGUI] Unregistering listener for " + player.getName());
		HandlerList.unregisterAll(this);
		OpenGUIs.Remove(this);
	}

	public ToggleButton createButton(String key){

		String enabled = String.format("features-gui.slots.%s.material.enabled", key);
		String disabled = String.format("features-gui.slots.%s.material.disabled", key);

		ItemStack enabledItem = enabled.startsWith("nexo:") ?
				NexoItems.itemFromId(Main.config.getString(enabled).replace("nexo:", "")).build() :
				new ItemStack(Material.valueOf(Main.config.getString(enabled).toUpperCase()));
		ItemStack disabledItem = disabled.startsWith("nexo:") ?
				NexoItems.itemFromId(Main.config.getString(disabled).replace("nexo:", "")).build() :
				new ItemStack(Material.valueOf(Main.config.getString(disabled).toUpperCase()));

		if(!enabled.startsWith("nexo:")){
			ItemMeta meta = enabledItem.getItemMeta();
			meta.setDisplayName(Lang.Parse(Main.config.getString(String.format("features-gui.slots.%s.name", key)), player));
			List<String> lore = new ArrayList<>();
			for(String line : Main.config.getStringList(String.format("features-gui.slots.%s.lore", key))){
				lore.add(Lang.Parse(line, player));
			}
			meta.setLore(lore);
			enabledItem.setItemMeta(meta);
		}

		if(!disabled.startsWith("nexo:")){
			ItemMeta meta = disabledItem.getItemMeta();
			meta.setDisplayName(Lang.Parse(Main.config.getString(String.format("features-gui.slots.%s.name", key)), player));
			List<String> lore = new ArrayList<>();
			for(String line : Main.config.getStringList(String.format("features-gui.slots.%s.lore", key))){
				lore.add(Lang.Parse(line, player));
			}
			meta.setLore(lore);
			disabledItem.setItemMeta(meta);
		}

		return new ToggleButton(enabledItem, disabledItem);
	}
}
