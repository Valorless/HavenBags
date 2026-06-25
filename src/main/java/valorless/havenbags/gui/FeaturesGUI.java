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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.Experimental;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.enums.GUIAction;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.GUI;
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

		public static void add(Player player, FeaturesGUI gui) {
			guis.put(player, gui); // replaces existing if already present
		}

		public static boolean contains(Player player) {
			return guis.containsKey(player);
		}

		public static boolean contains(FeaturesGUI gui) {
			return guis.containsValue(gui);
		}

		public static FeaturesGUI get(Player player) {
			return guis.get(player); // returns null if not present
		}

		public static void remove(Player player) {
			guis.remove(player);
		}

		public static void remove(FeaturesGUI gui) {
			guis.entrySet().removeIf(entry -> entry.getValue().equals(gui));
		}

		public static void closeAll() {
			for (FeaturesGUI gui : guis.values()) {
				gui.close();
			}
		}
	}


	Inventory inv;
	int invSize = 27; // 3 rows of 9 slots
	Player player;
	ItemStack bagItem;
	Bag data;

	int autoPickupSlot;
	ToggleButton autoPickup;

	int magnetSlot;
	ToggleButton magnet;

	int autoSortSlot;
	ToggleButton autoSort;

	int refillingSlot;
	ToggleButton refilling;

	String filler = "GRAY_STAINED_GLASS_PANE";

	int page = 1;

	enum ViewingType {
		MAIN,
		AUTO_PICKUP_FILTERS
	}

	ViewingType viewing = ViewingType.MAIN;

	public FeaturesGUI(Player player, ItemStack bagItem, Bag bagData) {
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

		//this.inv = Bukkit.createInventory(player, invSize, Lang.Parse(Main.config.getString("features-gui.title"), player));

		this.filler = Main.config.getString("features-gui.filler");

		updateGUI();

		//BukkitRunnable task = new BukkitRunnable() {
		//	@Override
		//	public void run() {
		//		player.openInventory(inv);
		//	}
		//};
//
		//task.runTaskLater(Main.plugin, 1); // Delay to ensure the inventory is ready before opening
//
		OpenGUIs.add(player, this);

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		Log.debug(Main.plugin, "[FeaturesGUI] Opening FeaturesGUI for " + player.getName());
	}

	void mainPage(){
		inv = Bukkit.createInventory(player, invSize, Lang.parse(Main.config.getString("features-gui.title"), player));
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

			if (autoPickupSlot != -1)
				inv.setItem(autoPickupSlot, data.hasAutoPickup() ? autoPickup.enabled : autoPickup.disabled);
			if (magnetSlot != -1) inv.setItem(magnetSlot, data.hasMagnet() ? magnet.enabled : magnet.disabled);
			if (autoSortSlot != -1)
				inv.setItem(autoSortSlot, data.hasAutoSort() ? autoSort.enabled : autoSort.disabled);
			if (refillingSlot != -1)
				inv.setItem(refillingSlot, data.hasRefill() ? refilling.enabled : refilling.disabled);
		}
	}

	public Inventory getInv() {
		return inv;
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!event.getInventory().equals(inv)) return;
		event.setCancelled(true);
	}

	public void updateGUI(){
		if(viewing == ViewingType.MAIN) {
			mainPage();
			player.openInventory(inv);
		} else if (viewing == ViewingType.AUTO_PICKUP_FILTERS) {
			inv = GUI.createPage(
					player,
					Lang.parse(Main.config.getString("features-gui.title"),player),
					page,
					autoPickupFilters(),
					(invSize/9)
			);
			player.openInventory(inv);

		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().equals(inv)) return;
		if(!(event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT)) {
			event.setCancelled(true);
			return;
		}

		if(viewing == ViewingType.MAIN) {

			if (event.getRawSlot() != autoPickupSlot &&
					event.getRawSlot() != magnetSlot &&
					event.getRawSlot() != autoSortSlot &&
					event.getRawSlot() != refillingSlot &&
					event.getRawSlot() < invSize) {
				event.setCancelled(true);
				return;
			}

			event.setCancelled(true);

			if (event.getRawSlot() == autoPickupSlot) {
				//data.setAutopickup(!data.isAutoPickup());
				viewing = ViewingType.AUTO_PICKUP_FILTERS;
			} else if (event.getRawSlot() == magnetSlot) {
				if (player.hasPermission("havenbags.features.magnet")) {
					data.setMagnet(!data.hasMagnet());
				} else {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("no-permission"), player));
					return;
				}
			} else if (event.getRawSlot() == autoSortSlot) {
				if (player.hasPermission("havenbags.features.autosort")) {
					data.setAutoSort(!data.hasAutoSort());
				} else {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("no-permission"), player));
					return;
				}
			} else if (event.getRawSlot() == refillingSlot) {
				if (player.hasPermission("havenbags.features.refill")) {
					data.setRefill(!data.hasRefill());
				} else {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("no-permission"), player));
					return;
				}
			}
		}else if (viewing == ViewingType.AUTO_PICKUP_FILTERS) {
			event.setCancelled(true);
			ItemStack item = event.getCurrentItem();
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.getString(item, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					viewing = ViewingType.MAIN;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					page--;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					page++;
				}
			}else{
				if(PDC.has(item, "filter")) {
					String filterKey = PDC.getString(item, "filter");
					data.setAutopickup(filterKey);
					viewing = ViewingType.MAIN;
				}
			}

		}
		updateGUI();

		HavenBags.updateBagLore(bagItem, player);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getInventory().equals(inv)) return;
		close();
	}

	public void close(){
		Log.debug(Main.plugin, "[FeaturesGUI] Unregistering listener for " + player.getName());
		HandlerList.unregisterAll(this);
		OpenGUIs.remove(this);
	}

	public ToggleButton createButton(String key){

		String enabledKey = String.format("features-gui.slots.%s.material.enabled", key);
		String disabledKey = String.format("features-gui.slots.%s.material.disabled", key);
		String enabled = Main.config.getString(enabledKey);
		String disabled = Main.config.getString(disabledKey);

		ItemStack enabledItem = enabled.startsWith("nexo:") ?
				NexoItems.itemFromId(enabled.replace("nexo:", "")).build() :
				new ItemStack(Material.valueOf(enabled.toUpperCase()));
		ItemStack disabledItem = disabled.startsWith("nexo:") ?
				NexoItems.itemFromId(disabled.replace("nexo:", "")).build() :
				new ItemStack(Material.valueOf(disabled.toUpperCase()));

		if(!enabled.startsWith("nexo:")){
			ItemMeta meta = enabledItem.getItemMeta();
			meta.setDisplayName(Lang.parse(Main.config.getString(String.format("features-gui.slots.%s.name", key)), player));
			List<String> lore = new ArrayList<>();
			for(String line : Main.config.getStringList(String.format("features-gui.slots.%s.lore", key))){
				lore.add(Lang.parse(line, player));
			}
			meta.setLore(lore);
			enabledItem.setItemMeta(meta);
		}

		if(!disabled.startsWith("nexo:")){
			ItemMeta meta = disabledItem.getItemMeta();
			meta.setDisplayName(Lang.parse(Main.config.getString(String.format("features-gui.slots.%s.name", key)), player));
			List<String> lore = new ArrayList<>();
			for(String line : Main.config.getStringList(String.format("features-gui.slots.%s.lore", key))){
				lore.add(Lang.parse(line, player));
			}
			meta.setLore(lore);
			disabledItem.setItemMeta(meta);
		}

		return new ToggleButton(enabledItem, disabledItem);
	}

	public List<ItemStack> autoPickupFilters(){
		List<ItemStack> filters = new ArrayList<>();

		String nullMat = AutoPickup.filter.getString("gui.reset-filter.icon");
		ItemStack nullEntry = nullMat.startsWith("nexo:") ?
				NexoItems.itemFromId(nullMat.replace("nexo:", "")).build() :
				new ItemStack(Material.valueOf(nullMat.toUpperCase()));
		ItemMeta nullMeta = nullEntry.getItemMeta();
		nullMeta.setDisplayName(Lang.parse(AutoPickup.filter.getString("gui.reset-filter.displayname"), player));

		List<String> nlore = new ArrayList<>();
		for(String line : AutoPickup.filter.getStringList("gui.reset-filter.lore")){
			nlore.add(Lang.parse(line, player));
		}
		nullMeta.setLore(nlore);
		nullEntry.setItemMeta(nullMeta);
		PDC.setString(nullEntry, "filter", "null");
		filters.add(nullEntry);

		for(AutoPickup.Filter filter : AutoPickup.getNoGenFilters()){
			if(!filter.guiShow) continue;
			ItemStack guiEntry = filter.guiIcon.startsWith("nexo:") ?
					NexoItems.itemFromId(filter.guiIcon.replace("nexo:", "")).build() :
					new ItemStack(Material.valueOf(filter.guiIcon.toUpperCase()));
			ItemMeta meta = guiEntry.getItemMeta();
			meta.setDisplayName(Lang.parse(filter.displayname, player));

			String lineFormat = filter.lineFormat;

			List<String> lore = new ArrayList<>();
			for(String line : filter.guiLore){
				lore.add(Lang.parse(line, player));
			}
			int cap = filter.loreLimit; // Cap items to not overflow tooltip ui
			int i = 0;
			for(String entry : filter.entries){
				if(i >= cap) break;

                try {
					Material mat = Material.valueOf(entry.toUpperCase());
					lore.add(
							Lang.parse(
									String.format(lineFormat, Main.translator.Translate(mat.getTranslationKey()))
									, player)
					);
				}catch(Exception e) {
					Log.error(Main.plugin, String.format("Failed to translate '%s'.", entry));
					e.printStackTrace();
					lore.add(Lang.parse(String.format(lineFormat, entry), player));
				}
				i++;
			}
			if(filter.entries.size() > cap){
				lore.add(Lang.parse(String.format(filter.andMore, filter.entries.size() - cap), player));
			}
			meta.setLore(lore);

			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE);

			guiEntry.setItemMeta(meta);

			PDC.setString(guiEntry, "filter", filter.key);

			filters.add(guiEntry);
		}

		return filters;
	}
}
