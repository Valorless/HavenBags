package valorless.havenbags.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.utils.Utils;
import valorless.havenbags.*;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.enums.GUIAction;
import valorless.havenbags.events.BagDeleteEvent;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.GUI;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.utils.TaskUtils;

public class PlayerGUI implements Listener {	
	public enum GUIType { Main, Restoration, Deletion, Confirmation }

	public JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	private Inventory inv;
	private Player player;
	private GUIType type = GUIType.Main;
	private List<ItemStack> content = new ArrayList<ItemStack>();
	private ItemStack selectedBag;
	private int page = 1;
	
	private boolean unused = false;
	private BukkitTask loading = null;
	public PlayerGUI(Player player) {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		this.plugin = Main.plugin;
		this.player = player;
		
		Log.Debug(Main.plugin, "[PlayerGUI][DI-287] " + player.getName());

		try {
			content = PrepareMain();
			Open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Reloads the GUI content based on the current type.
	 * This method is called when the GUI needs to be refreshed or reloaded.
	 * It prepares the content based on the current GUI type and opens the inventory.
	 */
	void PrepareContent() {
		if(GUIType.Main.equals(type)) {
			content = PrepareMain();
		}
		else if(type == GUIType.Restoration ||  type == GUIType.Deletion) {
			loading = TaskUtils.runAsyncThenSync(() -> {
			try {
				content = PreparePlayerBags(player.getUniqueId().toString());
				return content;
			} catch (Exception e) {
				player.closeInventory();
				e.printStackTrace();
				return null;
			}
			}, (_content) -> {
				if(loading == null) {
					Log.Debug(Main.plugin, "[PlayerGUI][DI-288] " + "Loading task was cancelled for " + player.getName());
					return;
				}
				loading = null;
				if(unused) return;
				if(_content != null) {
					Open();
					return;
				}
			});
		}
		else if(type == GUIType.Confirmation) {
			content = PrepareConfirmation();
		}
		Open();
	}
	
	public void OpenInventory(final HumanEntity ent) {
		ent.openInventory(inv);
	}

	/** Opens the GUI for the player based on the current type.
	 * This method creates the inventory and sets the items based on the content prepared for the current GUI type.
	 */
	void Open() {

		if(type == GUIType.Main) {
			inv = Bukkit.createInventory(player, 9, Lang.Get("playergui-title-main"));
			for(int i = 0; i < content.size(); i++) {
				inv.setItem(i, content.get(i));
			}
			player.openInventory(inv);
		}
		else if(type == GUIType.Restoration || type == GUIType.Deletion) {
			page = 1;
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
			if(type == GUIType.Restoration) {
				placeholders.add(new Placeholder("%player%", player.getName()));
				inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("playergui-bags-of"), placeholders, player),
						page, content, 6);
				
			}else if(type == GUIType.Deletion) {
				placeholders.add(new Placeholder("%player%", player.getName()));
				inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("playergui-bags-of"), placeholders, player),
						page, content, 6);
			}

			player.openInventory(inv);
		}
		else if(type == GUIType.Confirmation) {
			inv = Bukkit.createInventory(player, 9, Lang.Get("playergui-title-confirm"));
			for(int i = 0; i < content.size(); i++) {
				inv.setItem(i, content.get(i));
			}
			player.openInventory(inv);
		}
	}

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent e) {
		if (!e.getInventory().equals(inv)) return;
	}
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inv)) return;
        event.setCancelled(true);
    }
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!e.getPlayer().equals(player)) return;

		InventoryView view = player.getOpenInventory();
		if(view != null && view.getTopInventory() != null && view.getTopInventory().equals(inv)) {
			player.closeInventory();
			HandlerList.unregisterAll(this);
			unused = true;
			return;
		}
		
		if (view == null || view.getTopInventory() == null || !view.getTopInventory().equals(inv)) {
			Log.Debug(Main.plugin, "[PlayerGUI][DI-289] Unregistering listener for " + player.getName());
			HandlerList.unregisterAll(this);
			unused = true;
		}
	}
	
	/* * Handles inventory click events to manage interactions with the GUI.
	 * This method processes clicks based on the current GUI type and performs actions accordingly.
	 * It also handles pagination for restoration and preview types.
	 * @param e The InventoryClickEvent triggered when a player clicks in the inventory.
	 */
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent e) {
		if (!e.getInventory().equals(inv)) return;

		if(e.getRawSlot() >= inv.getSize()) return;

		ItemStack clickedItem = e.getCurrentItem();        
		if(clickedItem == null) return;
		
		if (loading != null) {
			e.setCancelled(true);
			String action = PDC.GetString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("return")){
				loading.cancel();
				loading = null;
				type = GUIType.Main;
				Reload(e);
				return;
			}
			return;
		}


		if (type == GUIType.Main) {
			String action = PDC.GetString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("restore")){
				type = GUIType.Restoration;
				Reload(e);
			}
			else if(action != null && action.equalsIgnoreCase("delete")){
				type = GUIType.Deletion;
				Reload(e);
			}
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Restoration) {
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.GetString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					Reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("playergui-bags-of"), placeholders, player),
							page, content, 6);

					player.openInventory(inv);
					e.setCancelled(true);
					return;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("playergui-bags-of"), placeholders, player),
							page, content, 6);
					player.openInventory(inv);
					e.setCancelled(true);
					return;
				}

				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
			}

			ItemStack giveItem = clickedItem.clone();
			player.getInventory().addItem(giveItem);
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Deletion) {        	
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.GetString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					Reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;

					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("playergui-bags-of"), placeholders, player),
							page, content, 6);
					
					player.openInventory(inv);
					return;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;

					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("playergui-bags-of"), placeholders, player),
							page, content, 6);
					
					player.openInventory(inv);
					return;
				}

				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
			}

			selectedBag = clickedItem;
			type = GUIType.Confirmation;
			Reload(e);
			e.setCancelled(true);
			return;
		}
		if (type == GUIType.Confirmation) {
			String action = PDC.GetString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("cancel")){
				type = GUIType.Deletion;
				Reload(e);
				return;
			}

			if(action != null && action.equalsIgnoreCase("confirm")){
				String uuid = PDC.GetString(selectedBag, "uuid");

				Data data = BagData.GetBag(uuid, null).clone();
				if(BagData.DeleteBag(uuid)) {
					Bukkit.getPluginManager().callEvent(new BagDeleteEvent(player, data));
				}

				type = GUIType.Deletion;
				Reload(e);
				return;
			}

			e.setCancelled(true);
			return;
		}
	}

	@SuppressWarnings("deprecation")
	/** Reloads the GUI content based on the current type and resets the cursor and current item.
	 * This method is called when the GUI needs to be refreshed or reloaded.
	 * @param event The InventoryClickEvent that triggered the reload, can be null.
	 */
	void Reload(InventoryClickEvent... event) {
		try {
			if(event != null && event.length != 0) {
				event[0].setCursor(new ItemStack(Material.AIR));
				event[0].setCurrentItem(new ItemStack(Material.AIR));
			}
			PrepareContent();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Utils

	ArrayList<ItemStack> PrepareMain() {
		ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();

		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));

		if(Main.config.GetBool("player-gui.self-restore")) {
			//Restore
			String restoreTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
			ItemStack restoreItem = HeadCreator.itemFromBase64(restoreTexture);
			ItemMeta restoreMeta = restoreItem.getItemMeta();
			restoreMeta.setDisplayName(Lang.Get("playergui-restore"));
			List<String> r_lore = new ArrayList<String>();
			for(String line : Lang.lang.GetStringList("playergui-restore-lore")) {
				r_lore.add(Lang.Parse(line, player));
			}
			//r_lore.add("§7Restore bags of online players.");
			restoreMeta.setLore(r_lore);
			restoreItem.setItemMeta(restoreMeta);
			PDC.SetString(restoreItem, "bag-action", "restore");
			buttons.add(restoreItem);

			buttons.add(new ItemStack(Material.AIR));
		}

		if(Main.config.GetBool("player-gui.self-delete")) {
			//Deletion
			String deleteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwZmQxMDE5OWU4ZTRmY2RhYmNhZTRmODVjODU5MTgxMjdhN2M1NTUzYWQyMzVmMDFjNTZkMThiYjk0NzBkMyJ9fX0=";
			ItemStack deleteItem = HeadCreator.itemFromBase64(deleteTexture);
			ItemMeta deleteMeta = deleteItem.getItemMeta();
			deleteMeta.setDisplayName(Lang.Get("playergui-delete"));
			List<String> d_lore = new ArrayList<String>();
			for(String line : Lang.lang.GetStringList("playergui-delete-lore")) {
				d_lore.add(Lang.Parse(line, player));
			}
			deleteMeta.setLore(d_lore);
			deleteItem.setItemMeta(deleteMeta);
			PDC.SetString(deleteItem, "bag-action", "delete");
			buttons.add(deleteItem);
		}

		return buttons;
	}

	public void modifyMaxStack(ItemStack item, int amount) {
		if(Server.VersionHigherOrEqualTo(Version.v1_20_5)){
			ItemUtils.SetMaxStackSize(item, amount);
		}
	}

	List<ItemStack> PreparePlayerBags(String playeruuid) {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		List<Data> bagdata = BagData.GetBagsData(playeruuid);

		for(Data data : bagdata){
			List<ItemStack> Content  = data.getContent();
			if (Content == null) continue;

			String bagTexture = Main.config.GetString("bag-texture");
			ItemStack bagItem = new ItemStack(Material.AIR);

			if(data.getMaterial() != null) {
				bagItem.setType(data.getMaterial());
				if(data.getMaterial() == Material.PLAYER_HEAD) {
					if(!Utils.IsStringNullOrEmpty(data.getTexture())) {
						BagData.setTextureValue(bagItem, data.getTexture());
					}else {
						BagData.setTextureValue(bagItem, bagTexture);
					}
				}
			}
			else {
				if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
					if(!Utils.IsStringNullOrEmpty(data.getTexture())) {
						bagItem = HeadCreator.itemFromBase64(data.getTexture());
					}else {
						bagItem = HeadCreator.itemFromBase64(bagTexture);
					}
				} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
					bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
				}
			}

			ItemMeta meta = bagItem.getItemMeta();
			if(!Utils.IsStringNullOrEmpty(data.getName()) && !data.getName().equalsIgnoreCase("null")) {
				meta.setDisplayName(Lang.Parse(data.getName(), player));
			}else {
				meta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-bound-name"), player));
			}
			bagItem.setItemMeta(meta);

			if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
				ItemUtils.SetItemName(bagItem, Lang.Parse(Lang.lang.GetString("bag-bound-name"), player));
			}

			PDC.SetString(bagItem, "uuid", data.getUuid());
			// No need to set more, will be added automatically by HavenBags.UpdateBagItem(), which runs HavenBags.UpdateNBT();

			modifyMaxStack(bagItem, 1);

			if(!Utils.IsStringNullOrEmpty(data.getItemmodel())) {
				ItemUtils.SetItemModel(bagItem, data.getItemmodel());
			}

			try {
				HavenBags.UpdateBagItem(bagItem, player);
			}catch(Exception e) {
				HavenBags.UpdateBagItem(bagItem, null);
			}

			bags.add(bagItem);
		}

		return bags;
	}

	ArrayList<ItemStack> PrepareConfirmation() {
		ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();

		//Cancel
		String cancelTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=";
		ItemStack cancelItem = HeadCreator.itemFromBase64(cancelTexture);
		ItemMeta cancelMeta = cancelItem.getItemMeta();
		cancelMeta.setDisplayName(Lang.Get("playergui-cancel"));
		List<String> c_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("cancel-lore")) {
			c_lore.add(Lang.Parse(line, player));
		}
		cancelMeta.setLore(c_lore);
		cancelItem.setItemMeta(cancelMeta);
		PDC.SetString(cancelItem, "bag-action", "cancel");
		buttons.add(cancelItem);

		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));

		buttons.add(selectedBag);

		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));

		//Confirm
		String comfirmTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=";
		ItemStack confirmItem = HeadCreator.itemFromBase64(comfirmTexture);
		ItemMeta confirmMeta = confirmItem.getItemMeta();
		confirmMeta.setDisplayName(Lang.Get("playergui-confirm"));
		List<String> co_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("confirm-lore")) {
			co_lore.add(Lang.Parse(line, player));
		}
		confirmMeta.setLore(co_lore);
		confirmItem.setItemMeta(confirmMeta);
		PDC.SetString(confirmItem, "bag-action", "confirm");
		buttons.add(confirmItem);

		return buttons;
	}
}
