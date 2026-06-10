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
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.utils.Utils;
import valorless.havenbags.*;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.enums.GUIAction;
import valorless.havenbags.features.Insurance;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Extra;
import valorless.havenbags.utils.GUI;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.utils.TaskUtils;

public class PlayerGUI implements Listener {	
	public enum GUIType { Main, Restoration, Deletion, Confirmation }

	public JavaPlugin plugin;
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
		
		Log.debug(Main.plugin, "[PlayerGUI][DI-287] " + player.getName());

		try {
			content = prepareMain();
			open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Reloads the GUI content based on the current type.
	 * This method is called when the GUI needs to be refreshed or reloaded.
	 * It prepares the content based on the current GUI type and opens the inventory.
	 */
	void prepareContent() {
		if(GUIType.Main.equals(type)) {
			content = prepareMain();
		}
		else if(type == GUIType.Restoration ||  type == GUIType.Deletion) {
			loading = TaskUtils.runAsyncThenSync(() -> {
			try {
				content = preparePlayerBags(player.getUniqueId().toString());
				return content;
			} catch (Exception e) {
				player.closeInventory();
				e.printStackTrace();
				return null;
			}
			}, (_content) -> {
				if(loading == null) {
					Log.debug(Main.plugin, "[PlayerGUI][DI-288] " + "Loading task was cancelled for " + player.getName());
					return;
				}
				loading = null;
				if(unused) return;
				if(_content != null) {
					open();
					return;
				}
			});
		}
		else if(type == GUIType.Confirmation) {
			content = prepareConfirmation();
		}
		open();
	}
	
	public void openInventory(final HumanEntity ent) {
		ent.openInventory(inv);
	}

	/** Opens the GUI for the player based on the current type.
	 * This method creates the inventory and sets the items based on the content prepared for the current GUI type.
	 */
	void open() {

		if(type == GUIType.Main) {
			inv = Bukkit.createInventory(player, 9, Lang.get("playergui-title-main"));
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
				inv = GUI.createPage(player, Lang.parse(Lang.get("playergui-bags-of"), placeholders, player),
						page, content, 6);
				
			}else if(type == GUIType.Deletion) {
				placeholders.add(new Placeholder("%player%", player.getName()));
				inv = GUI.createPage(player, Lang.parse(Lang.get("playergui-bags-of"), placeholders, player),
						page, content, 6);
			}

			player.openInventory(inv);
		}
		else if(type == GUIType.Confirmation) {
			inv = Bukkit.createInventory(player, 9, Lang.get("playergui-title-confirm"));
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
			Log.debug(Main.plugin, "[PlayerGUI][DI-289] Unregistering listener for " + player.getName());
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
			String action = PDC.getString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("return")){
				loading.cancel();
				loading = null;
				type = GUIType.Main;
				reload(e);
				return;
			}
			return;
		}


		if (type == GUIType.Main) {
			String action = PDC.getString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("restore")){
				type = GUIType.Restoration;
				reload(e);
			}
			else if(action != null && action.equalsIgnoreCase("delete")){
				type = GUIType.Deletion;
				reload(e);
			}
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Restoration) {
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.getString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.createPage(player, Lang.parse(Lang.get("playergui-bags-of"), placeholders, player),
							page, content, 6);

					player.openInventory(inv);
					e.setCancelled(true);
					return;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.createPage(player, Lang.parse(Lang.get("playergui-bags-of"), placeholders, player),
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
			
			Insurance insurance = Insurance.getInstance();	
			if(insurance != null) {
				if(insurance.canClaim(player)) {
					if(insurance.claimInsurance(player)) {
						//player.sendMessage(Lang.Get("insurance.claimed"));
					}else {
						player.sendMessage(Lang.get("prefix") + Lang.get("insurance.fail"));
						e.setCancelled(true);
						return;
					}
				}else {
					player.sendMessage(Lang.get("prefix") + Lang.get("insurance.cooldown"));
					e.setCancelled(true);
					return;
				}
				
				ItemMeta m = giveItem.getItemMeta();
				List<String> lore = m.getLore();
				lore.remove(lore.size() - 1); // Remove insurance lore from bag item in restoration menu
				m.setLore(lore);
				giveItem.setItemMeta(m);
			}
			player.getInventory().addItem(giveItem);
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Deletion) {        	
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.getString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;

					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.createPage(player, Lang.parse(Lang.get("playergui-bags-of"), placeholders, player),
							page, content, 6);
					
					player.openInventory(inv);
					return;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;

					placeholders.add(new Placeholder("%player%", player.getName()));
					inv = GUI.createPage(player, Lang.parse(Lang.get("playergui-bags-of"), placeholders, player),
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
			reload(e);
			e.setCancelled(true);
			return;
		}
		if (type == GUIType.Confirmation) {
			String action = PDC.getString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("cancel")){
				type = GUIType.Deletion;
				reload(e);
				return;
			}

			if(action != null && action.equalsIgnoreCase("confirm")){
				Insurance insurance = Insurance.getInstance();	
				if(insurance != null) {
					if(insurance.canClaim(player)) {
						if(insurance.claimInsurance(player)) {
							//player.sendMessage(Lang.Get("insurance.claimed"));
						}else {
							player.sendMessage(Lang.get("prefix") + Lang.get("insurance.fail"));
							e.setCancelled(true);
							return;
						}
					}else {
						player.sendMessage(Lang.get("prefix") + Lang.get("insurance.cooldown"));
						e.setCancelled(true);
						return;
					}
				}
				String uuid = PDC.getString(selectedBag, "uuid");

				Data data = BagData.getBag(uuid, null).clone();
				BagData.deleteBag(uuid);

				type = GUIType.Deletion;
				reload(e);
				return;
			}

			e.setCancelled(true);
			return;
		}
	}

	/** Reloads the GUI content based on the current type and resets the cursor and current item.
	 * This method is called when the GUI needs to be refreshed or reloaded.
	 * @param event The InventoryClickEvent that triggered the reload, can be null.
	 */
	@SuppressWarnings("deprecation")
	void reload(InventoryClickEvent... event) {
		try {
			if(event != null && event.length != 0) {
				event[0].setCursor(new ItemStack(Material.AIR));
				event[0].setCurrentItem(new ItemStack(Material.AIR));
			}
			prepareContent();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Utils

	ArrayList<ItemStack> prepareMain() {
		ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();

		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));

		if(Main.config.getBool("player-gui.self-restore")) {
			//Restore
			String restoreTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
			ItemStack restoreItem = HeadCreator.itemFromBase64(restoreTexture);
			ItemMeta restoreMeta = restoreItem.getItemMeta();
			restoreMeta.setDisplayName(Lang.get("playergui-restore"));
			List<String> r_lore = new ArrayList<String>();
			for(String line : Lang.lang.getStringList("playergui-restore-lore")) {
				r_lore.add(Lang.parse(line, player));
			}
			//r_lore.add("§7Restore bags of online players.");
			restoreMeta.setLore(r_lore);
			restoreItem.setItemMeta(restoreMeta);
			PDC.setString(restoreItem, "bag-action", "restore");
			buttons.add(restoreItem);

			buttons.add(new ItemStack(Material.AIR));
		}

		if(Main.config.getBool("player-gui.self-delete")) {
			//Deletion
			String deleteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwZmQxMDE5OWU4ZTRmY2RhYmNhZTRmODVjODU5MTgxMjdhN2M1NTUzYWQyMzVmMDFjNTZkMThiYjk0NzBkMyJ9fX0=";
			ItemStack deleteItem = HeadCreator.itemFromBase64(deleteTexture);
			ItemMeta deleteMeta = deleteItem.getItemMeta();
			deleteMeta.setDisplayName(Lang.get("playergui-delete"));
			List<String> d_lore = new ArrayList<String>();
			for(String line : Lang.lang.getStringList("playergui-delete-lore")) {
				d_lore.add(Lang.parse(line, player));
			}
			deleteMeta.setLore(d_lore);
			deleteItem.setItemMeta(deleteMeta);
			PDC.setString(deleteItem, "bag-action", "delete");
			buttons.add(deleteItem);
		}

		return buttons;
	}

	public void modifyMaxStack(ItemStack item, int amount) {
		if(Server.VersionHigherOrEqualTo(Version.v1_20_5)){
			ItemUtils.SetMaxStackSize(item, amount);
		}
	}

	List<ItemStack> preparePlayerBags(String playeruuid) {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		List<Data> bagdata = BagData.getBagsData(playeruuid);

		for(Data data : bagdata){
			List<ItemStack> Content  = data.getContent();
			if (Content == null) continue;

			String bagTexture = Main.config.getString("bag.texture");
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
				if(Main.config.getString("bag.type").equalsIgnoreCase("HEAD")){
					if(!Utils.IsStringNullOrEmpty(data.getTexture())) {
						bagItem = HeadCreator.itemFromBase64(data.getTexture());
					}else {
						bagItem = HeadCreator.itemFromBase64(bagTexture);
					}
				} else if(Main.config.getString("bag.type").equalsIgnoreCase("ITEM")) {
					bagItem = new ItemStack(Main.config.getMaterial("bag.material"));
				}
			}

			ItemMeta meta = bagItem.getItemMeta();
			if(!Utils.IsStringNullOrEmpty(data.getName()) && !data.getName().equalsIgnoreCase("null")) {
				meta.setDisplayName(Lang.parse(data.getName(), player));
			}else {
				meta.setDisplayName(Lang.parse(Lang.lang.getString("bag-bound-name"), player));
			}
			
			if(data.getModeldata() != null && data.getModeldata() != 0) {
				meta.setCustomModelData(data.getModeldata());
			}
			
			bagItem.setItemMeta(meta);

			if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
				ItemUtils.SetItemName(bagItem, Lang.parse(Lang.lang.getString("bag-bound-name"), player));
			}

			PDC.setString(bagItem, "uuid", data.getUuid());
			// No need to set more, will be added automatically by HavenBags.UpdateBagItem(), which runs HavenBags.UpdateNBT();

			modifyMaxStack(bagItem, 1);

			if(!Utils.IsStringNullOrEmpty(data.getItemmodel())) {
				ItemUtils.SetItemModel(bagItem, data.getItemmodel());
			}

			try {
				HavenBags.updateBagItem(bagItem, player);
			}catch(Exception e) {
				HavenBags.updateBagItem(bagItem, null);
			}
			
			Insurance insurance = Insurance.getInstance();	
			if(insurance != null) {
				double cost = insurance.getCurrentInsuranceCost(player);
				String line = Main.config.getString("insurance.lore").replace("%cost%", Extra.formatDouble(cost));
				ItemMeta m = bagItem.getItemMeta();
				List<String> lore = m.getLore();
				if(lore == null) lore = new ArrayList<String>();
				lore.add(Lang.parse(line, player));
				m.setLore(lore);
				bagItem.setItemMeta(m);
			}

			bags.add(bagItem);
		}

		return bags;
	}

	ArrayList<ItemStack> prepareConfirmation() {
		ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();

		//Cancel
		String cancelTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=";
		ItemStack cancelItem = HeadCreator.itemFromBase64(cancelTexture);
		ItemMeta cancelMeta = cancelItem.getItemMeta();
		cancelMeta.setDisplayName(Lang.get("playergui-cancel"));
		List<String> c_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("cancel-lore")) {
			c_lore.add(Lang.parse(line, player));
		}
		cancelMeta.setLore(c_lore);
		cancelItem.setItemMeta(cancelMeta);
		PDC.setString(cancelItem, "bag-action", "cancel");
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
		confirmMeta.setDisplayName(Lang.get("playergui-confirm"));
		List<String> co_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("confirm-lore")) {
			co_lore.add(Lang.parse(line, player));
		}
		confirmMeta.setLore(co_lore);
		confirmItem.setItemMeta(confirmMeta);
		PDC.setString(confirmItem, "bag-action", "confirm");
		buttons.add(confirmItem);

		return buttons;
	}
}
