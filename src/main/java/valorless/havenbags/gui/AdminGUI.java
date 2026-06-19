package valorless.havenbags.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;

import valorless.havenbags.items.BagItemFactory;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.utils.Utils;
import valorless.havenbags.*;
import valorless.havenbags.database.SkinCache;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.enums.GUIAction;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.GUI;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.utils.TaskUtils;

/** AdminGUI class for managing Haven Bags through a graphical user interface.
 * This class allows admins to create, restore, preview, delete, and manage bags of players.
 * It implements the Listener interface to handle various inventory events.
 */
public class AdminGUI implements Listener {
	public enum GUIType { Main, Creation, Restoration, Player, Preview, PreviewPlayer, Deletion, DeletionPlayer, Confirmation, Content }

	public JavaPlugin plugin;
	private Inventory inv;
	private final Player player;
	private String target;
	private OfflinePlayer targetPlayer;
	private GUIType type;
	private List<ItemStack> content = new ArrayList<ItemStack>();
	private ItemStack selectedBag;
	private int page = 1;

	private boolean unused = false;
	private BukkitTask loading = null;

	/** Constructor for AdminGUI without a target player.
	 * This constructor initializes the GUI for an admin to manage their own bags.
	 * @param type The type of GUI to open (e.g., Main, Creation, Restoration).
	 * @param player The player who is opening the GUI.
	 */
	public AdminGUI(GUIType type, Player player) {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		this.plugin = Main.plugin;
		this.type = type;
		this.player = player;
		this.target = player.getUniqueId().toString();
		this.targetPlayer = player;

		Log.debug(plugin, "[DI-22] " + type.toString());

		try {
			content = prepareMain();
			open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Constructor for AdminGUI with a target OfflinePlayer.
	 * This constructor initializes the GUI for an admin to manage bags of a specific player.
	 * @param type The type of GUI to open (e.g., Main, Creation, Restoration).
	 * @param player The player who is opening the GUI.
	 * @param target The OfflinePlayer whose bags are being managed.
	 */
	public AdminGUI(GUIType type, Player player, OfflinePlayer target) {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		this.plugin = Main.plugin;
		this.type = type;
		this.player = player;
		this.target = target.getUniqueId().toString();
		this.targetPlayer = target;

		Log.debug(plugin, "[DI-23] " + type.toString());

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
		if(type == GUIType.Main) {
			content = prepareMain();
		}
		else if(type == GUIType.Creation) {
			content = prepareTemplates();
		}
		else if(type == GUIType.Restoration || type == GUIType.Preview || type == GUIType.Deletion) {
			content = prepareBags();

			// Prepare offline bags asynchronously to avoid blocking the main thread.
			TaskUtils.runAsyncThenSync(() -> {
				if(player.getName().equalsIgnoreCase("Alynie")) {
					player.sendMessage("§c[Debug] Requesting offline bags...");
					Log.info(plugin, "[Debug] Requesting offline bags...");
				}
				return prepareOfflineBags();
			}, (offline) -> {
				if(unused) return;
				// After preparing all offline players async, add them to the GUI's content and reload the inventory if applicable.
				if(offline != null) {
					if(type == GUIType.Restoration || type == GUIType.Preview || type == GUIType.Deletion) {
						if(player.getName().equalsIgnoreCase("Alynie")) {
							player.sendMessage("§e[Debug] Requested offline bags successfully, adding to GUI...");
							Log.info(plugin, "[Debug] Requested offline bags successfully, adding to GUI...");
						}
						for(ItemStack off : offline) {
							content.add(off);
						}
						if(player.getOpenInventory().getTopInventory() != inv) return;
						if(player.getName().equalsIgnoreCase("Alynie")) {
							player.sendMessage("§e[Debug] Updating GUI with offline bags...");
							Log.info(plugin, "[Debug] Updating GUI with offline bags...");
						}
						open();
						return;
					}
				}else {
					if(player.getName().equalsIgnoreCase("Alynie")) {
						player.sendMessage("§c[Debug] Error, offline bags is null.");
						Log.error(plugin, "[Debug] Error, offline bags is null.");
					}
				}
			});
		}
		else if(type == GUIType.Player || type == GUIType.PreviewPlayer || type == GUIType.DeletionPlayer) {
			loading = TaskUtils.runAsyncThenSync(() -> {
				try {
					content = preparePlayerBags(target);
					return content;
				} catch (Exception e) {
					player.closeInventory();
					e.printStackTrace();
					return null;
				}
			}, (_content) -> {
				if(loading == null) {
					Log.debug(Main.plugin, "[AdminGUI][DI-265] " + "Loading task was cancelled for " + player.getName());
					return;
				}
				loading = null;
				if(unused) return;
				if(_content != null) {
					open();
					return;
				}
			});
		}else if(type == GUIType.Confirmation) {
			content = prepareConfirmation();
		}else if(type == GUIType.Content) {
			Data data = Database.getBag(HavenBags.getBagUUID(selectedBag), null);
			content = data.getContent();
		}
		open();
	}

	/** Opens the GUI for the player based on the current type.
	 * This method creates the inventory and sets the items based on the content prepared for the current GUI type.
	 */
	void open() {
		//Log.debug(plugin, type.toString());

		if(type == GUIType.Main) {
			inv = Bukkit.createInventory(player, 9, Lang.get("gui-main"));
			for(int i = 0; i < content.size(); i++) {
				inv.setItem(i, content.get(i));
			}
			player.openInventory(inv);
		}
		else if(type == GUIType.Creation) {
			inv = Bukkit.createInventory(player, 18, Lang.get("gui-create"));
			for(int i = 0; i < content.size(); i++) {
				inv.setItem(i, content.get(i));
			}
			player.openInventory(inv);
		}
		else if(type == GUIType.Restoration) {
			page = 1;
			inv = GUI.createPage(player, Lang.get("gui-restore"),
					page, content, 6);
			player.openInventory(inv);
			/*inv = Bukkit.createInventory(player, 54, Lang.Get("gui-restore"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);*/
		}
		else if(type == GUIType.Preview) {
			page = 1;
			inv = GUI.createPage(player, Lang.get("gui-preview"),
					page, content, 6);
			player.openInventory(inv);
			/*inv = Bukkit.createInventory(player, 54, Lang.Get("gui-preview"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);*/
		}
		else if(type == GUIType.Deletion) {
			page = 1;
			inv = GUI.createPage(player, Lang.get("gui-delete"),
					page, content, 6);
			player.openInventory(inv);
			/*inv = Bukkit.createInventory(player, 54, Lang.Get("gui-delete"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);*/
		}
		else if(type == GUIType.Player || type == GUIType.PreviewPlayer || type == GUIType.DeletionPlayer) {
			page = 1;
			if(type == GUIType.Player) {
				if(target.equalsIgnoreCase("ownerless")) {
					inv = GUI.createPage(player, Lang.get("gui-restore"),
							page, content, 6);
				}else {
					inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of", targetPlayer), targetPlayer.getPlayer()),
							page, content, 6);
				}
			}else if(type == GUIType.PreviewPlayer) {
				if(target.equalsIgnoreCase("ownerless")) {
					inv = GUI.createPage(player, Lang.get("gui-preview"),
							page, content, 6);
				}else {
					inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of", targetPlayer), targetPlayer.getPlayer()),
							page, content, 6);
				}
			}else if(type == GUIType.DeletionPlayer) {
				if(target.equalsIgnoreCase("ownerless")) {
					inv = GUI.createPage(player, Lang.get("gui-delete", targetPlayer),
							page, content, 6);
				}else {
					inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of", targetPlayer), targetPlayer.getPlayer()),
							page, content, 6);
				}
			}
			/*inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
					page, content, 6);*/
			player.openInventory(inv);
			//inv = Bukkit.createInventory(player, 54, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()));
			//for(int i = 0; i < content.size(); i++) {
			//	inv.setItem(i, content.get(i));
			//}
		}
		else if(type == GUIType.Confirmation) {
			inv = Bukkit.createInventory(player, 9, Lang.get("gui-confirm"));
			for(int i = 0; i < content.size(); i++) {
				inv.setItem(i, content.get(i));
			}
			player.openInventory(inv);
		}
		else if(type == GUIType.Content) {
			inv = Bukkit.createInventory(player, content.size(), "");
			for(int i = 0; i < content.size(); i++) {
				inv.setItem(i, content.get(i));
			}
			player.openInventory(inv);
		}
	}

	public void openInventory(final HumanEntity ent) {
		ent.openInventory(inv);
	}

	/** Handles the inventory close event.
	 * This method checks if the closed inventory is the one managed by this GUI.
	 * If it is, it resets the type to PreviewPlayer and reloads the GUI after a short delay.
	 * @param e The InventoryCloseEvent triggered when a player closes an inventory.
	 */
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent e) {
		if (!e.getInventory().equals(inv)) return;
		if (type == GUIType.Content) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
				@Override
				public void run(){
					type = GUIType.PreviewPlayer;
					reload();
					return;
				}
			}, 2L);

		}
	}

	/* * Handles player command events to check if the player is still interacting with the GUI.
	 * If the player issues a command and the inventory is no longer open, it unregisters the listener.
	 * @param e The PlayerCommandPreprocessEvent triggered when a player issues a command.
	 */
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!e.getPlayer().equals(player)) return;

		InventoryView view = player.getOpenInventory();
		if (view == null || view.getTopInventory() == null || !view.getTopInventory().equals(inv)) {
			Log.debug(Main.plugin, "[AdminGUI][DI-264] Unregistering listener for " + player.getName());
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
			if(action != null && action.equalsIgnoreCase("create")){
				type = GUIType.Creation;
				reload(e);
			}
			else if(action != null && action.equalsIgnoreCase("restore")){
				type = GUIType.Restoration;
				reload(e);
			}
			else if(action != null && action.equalsIgnoreCase("preview")){
				type = GUIType.Preview;
				reload(e);
			}
			else if(action != null && action.equalsIgnoreCase("delete")){
				type = GUIType.Deletion;
				reload(e);
			}
			else if(action != null && action.equalsIgnoreCase("return")){
				type = GUIType.Main;
				reload(e);
				return;
			}
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Creation) {
			String action = PDC.getString(clickedItem, "bag-action");
			if(action != null && action.equalsIgnoreCase("return")){
				type = GUIType.Main;
				reload(e);
				return;
			}

			ItemStack giveItem = clickedItem.clone();
			//PDC.SetString(giveItem, "bag-uuid", UUID.randomUUID().toString());
			player.getInventory().addItem(giveItem);
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
					page--;
					inv = GUI.createPage(player, Lang.get("gui-restore", targetPlayer),
							page, content, 6);
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					page++;
					inv = GUI.createPage(player, Lang.get("gui-restore", targetPlayer),
							page, content, 6);
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
			}

			String owner = PDC.getString(clickedItem, "owner");
			Log.debug(plugin, "[DI-24] " + "Changing Admin target to " + owner);
			if(owner == null) return;
			if(owner.equalsIgnoreCase("ownerless")) {
				target = "ownerless";
			}else {
				target = owner;
				targetPlayer = Bukkit.getOfflinePlayer(UUID.fromString(target));
			}

			type = GUIType.Player;
			reload(e);
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Preview) {
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
					page--;
					inv = GUI.createPage(player, Lang.get("gui-preview", targetPlayer),
							page, content, 6);
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					page++;
					inv = GUI.createPage(player, Lang.get("gui-preview", targetPlayer),
							page, content, 6);
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
			}

			String owner = PDC.getString(clickedItem, "owner");
			Log.debug(plugin, "[DI-25] " + "Changing Admin target to " + owner);
			if(owner == null) return;
			if(owner.equalsIgnoreCase("ownerless")) {
				target = "ownerless";
				//targetPlayer = player;
			}else {
				target = owner;
				targetPlayer = Bukkit.getOfflinePlayer(UUID.fromString(target));
			}

			type = GUIType.PreviewPlayer;
			reload(e);
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
					page--;
					inv = GUI.createPage(player, Lang.get("gui-delete", targetPlayer),
							page, content, 6);
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					page++;
					inv = GUI.createPage(player, Lang.get("gui-delete", targetPlayer),
							page, content, 6);
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
			}

			String owner = PDC.getString(clickedItem, "owner");
			Log.debug(plugin, "[DI-26] " + "Changing Admin target to " + owner);
			if(owner == null) return;
			if(owner.equalsIgnoreCase("ownerless")) {
				target = "ownerless";
				//targetPlayer = player;
			}else {
				target = owner;
				targetPlayer = Bukkit.getOfflinePlayer(UUID.fromString(target));
			}
			type = GUIType.DeletionPlayer;
			reload(e);
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.Player) {
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.getString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Restoration;
					reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.createPage(player, Lang.get("gui-restore"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}

					player.openInventory(inv);
					e.setCancelled(true);
					return;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.createPage(player, Lang.get("gui-restore"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
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

		if (type == GUIType.PreviewPlayer) {
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.getString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Preview;
					reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.createPage(player, Lang.get("gui-preview"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.createPage(player, Lang.get("gui-preview"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}

				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
			}

			selectedBag = clickedItem;
			type = GUIType.Content;
			reload(e);
			e.setCancelled(true);
			return;
		}

		if (type == GUIType.DeletionPlayer) {
			GUIAction action = null;
			try {
				action = GUIAction.valueOf(PDC.getString(clickedItem, "bag-action"));
			} catch(Exception E) {}

			if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Deletion;
					reload(e);
					return;
				}

				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.createPage(player, Lang.get("gui-delete"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
					return;
				}

				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.createPage(player, Lang.get("gui-delete"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.createPage(player, Lang.parse(Lang.get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
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
				type = GUIType.DeletionPlayer;
				reload(e);
				return;
			}

			if(action != null && action.equalsIgnoreCase("confirm")){
				String uuid = PDC.getString(selectedBag, "uuid");

				Data data = Database.getBag(uuid, null).clone();
				Database.deleteBag(uuid);

				type = GUIType.DeletionPlayer;
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

		//Create
		String cresteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19";
		ItemStack createItem = HeadCreator.itemFromBase64(cresteTexture);
		ItemMeta createMeta = createItem.getItemMeta();
		createMeta.setDisplayName(Lang.get("main-create"));
		List<String> c_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("main-create-lore")) {
			c_lore.add(Lang.parse(line, targetPlayer));
		}
		//c_lore.add("§7Create bags easy.");
		createMeta.setLore(c_lore);
		createItem.setItemMeta(createMeta);
		PDC.setString(createItem, "bag-action", "create");
		buttons.add(createItem);

		buttons.add(new ItemStack(Material.AIR));

		//Restore
		String restoreTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
		ItemStack restoreItem = HeadCreator.itemFromBase64(restoreTexture);
		ItemMeta restoreMeta = restoreItem.getItemMeta();
		restoreMeta.setDisplayName(Lang.get("main-restore"));
		List<String> r_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("main-restore-lore")) {
			r_lore.add(Lang.parse(line, targetPlayer));
		}
		//r_lore.add("§7Restore bags of online players.");
		restoreMeta.setLore(r_lore);
		restoreItem.setItemMeta(restoreMeta);
		PDC.setString(restoreItem, "bag-action", "restore");
		buttons.add(restoreItem);

		buttons.add(new ItemStack(Material.AIR));

		//Preview
		String previewTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZlM2JjYmE1N2M3YjdmOGQ0NjJiMzAwNTQzZDEzMmVjZWE5YmYyZWQ1ODdjYzlkOTk0YTM5NWFjOTU5MmVhYSJ9fX0=";
		ItemStack previewItem = HeadCreator.itemFromBase64(previewTexture);
		ItemMeta previewMeta = previewItem.getItemMeta();
		previewMeta.setDisplayName(Lang.get("main-preview"));
		List<String> p_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("main-preview-lore")) {
			p_lore.add(Lang.parse(line, targetPlayer));
		}
		previewMeta.setLore(p_lore);
		previewItem.setItemMeta(previewMeta);
		PDC.setString(previewItem, "bag-action", "preview");
		buttons.add(previewItem);

		buttons.add(new ItemStack(Material.AIR));

		//Deletion
		String deleteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwZmQxMDE5OWU4ZTRmY2RhYmNhZTRmODVjODU5MTgxMjdhN2M1NTUzYWQyMzVmMDFjNTZkMThiYjk0NzBkMyJ9fX0=";
		ItemStack deleteItem = HeadCreator.itemFromBase64(deleteTexture);
		ItemMeta deleteMeta = deleteItem.getItemMeta();
		deleteMeta.setDisplayName(Lang.get("main-delete"));
		List<String> d_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("main-delete-lore")) {
			d_lore.add(Lang.parse(line, targetPlayer));
		}
		deleteMeta.setLore(d_lore);
		deleteItem.setItemMeta(deleteMeta);
		PDC.setString(deleteItem, "bag-action", "delete");
		buttons.add(deleteItem);

		/*
		buttons.add(new ItemStack(Material.AIR));

		//Info
		String infoTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjcwNWZkOTRhMGM0MzE5MjdmYjRlNjM5YjBmY2ZiNDk3MTdlNDEyMjg1YTAyYjQzOWUwMTEyZGEyMmIyZTJlYyJ9fX0=";
		ItemStack infoItem = HeadCreator.itemFromBase64(infoTexture);
		ItemMeta infoMeta = infoItem.getItemMeta();
		infoMeta.setDisplayName(Lang.Get("main-info"));
		List<String> I_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("main-info-lore")) {
			I_lore.add(Lang.Parse(line, targetPlayer));
		}
		infoMeta.setLore(I_lore);
		infoItem.setItemMeta(infoMeta);
		buttons.add(infoItem);
		 */

		return buttons;
	}

	public static void modifyMaxStack(ItemStack item, int amount) {
		if(Server.VersionHigherOrEqualTo(Version.v1_20_5)){
			ItemUtils.SetMaxStackSize(item, amount);
		}
	}

	ArrayList<ItemStack> prepareTemplates() {
		ArrayList<ItemStack> templates = new ArrayList<ItemStack>();

		//Bound
		for(int i = 1; i <= 6; i++) {
			ItemStack bagItem = BagItemFactory.createBagItem(true, i*9, player);
			templates.add(bagItem);
		}

		//3 air spaces to make a new line.
		templates.add(new ItemStack(Material.AIR));
		templates.add(new ItemStack(Material.AIR));
		templates.add(new ItemStack(Material.AIR));

		//Ownerless
		for(int i = 1; i <= 6; i++) {
			ItemStack bagItem = BagItemFactory.createBagItem(false, i*9, player);
			templates.add(bagItem);
		}

		templates.add(new ItemStack(Material.AIR));
		templates.add(new ItemStack(Material.AIR));

		String returnTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NjFhZDFmNWM3NmU5NzM1OGM0NDRmZTBlODNhMzk1NjRlNmI0ODEwOTE3MDk4NGE4NGVjYTVkY2NkNDI0In19fQ==";
		ItemStack returnItem = HeadCreator.itemFromBase64(returnTexture);
		ItemMeta returnMeta = returnItem.getItemMeta();
		returnMeta.setDisplayName(Lang.get("return"));
		List<String> r_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("return-lore")) {
			r_lore.add(Lang.parse(line, targetPlayer));
		}
		//r_lore.add("§7Go back.");
		returnMeta.setLore(r_lore);
		returnItem.setItemMeta(returnMeta);
		PDC.setString(returnItem, "bag-action", "return");
		templates.add(returnItem);


		return templates;
	}

	List<ItemStack> prepareBags() {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		//Collection<? extends Player> p = Bukkit.getOnlinePlayers();
		//List<Player> players = new ArrayList<>(p);

		if(!Database.getBags("ownerless").isEmpty()) {
			String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
			ItemStack ownerless = HeadCreator.itemFromBase64(bagTexture);
			ItemMeta ownerlessmeta = ownerless.getItemMeta();
			ownerlessmeta.setDisplayName("§e" + "Ownerless");
			ownerless.setItemMeta(ownerlessmeta);
			PDC.setString(ownerless, "owner", "ownerless");
			bags.add(ownerless);
		}

		// ONLINE PLAYERS
		List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
		onlinePlayers.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

		for (Player p : onlinePlayers) {
			String uuid = p.getUniqueId().toString();
			if (Database.getBags(uuid).isEmpty()) continue;

			//ItemStack entry = HeadCreator.itemFromUuid(p.getUniqueId()); <-- Causes HTTP 429.
			ItemStack entry = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) entry.getItemMeta();
			PlayerProfile profile = SkinCache.getProfile(p.getName());
			if(profile != null) {
				meta.setOwnerProfile(profile);
			}
			meta.setDisplayName("§a" + p.getName());
			entry.setItemMeta(meta);
			PDC.setString(entry, "owner", uuid);
			bags.add(entry);
		}

		return bags;
	}

	List<ItemStack> prepareOfflineBags(){
		List<ItemStack> bags = new ArrayList<ItemStack>();

		if(player.getName().equalsIgnoreCase("Alynie")) {
			player.sendMessage("§e[Debug] Preparing offline bags...");
			Log.info(plugin, "[Debug] Preparing offline bags...");
		}

		// OFFLINE PLAYERS (excluding those who are currently online)
		List<OfflinePlayer> offlinePlayers = Arrays.stream(Bukkit.getOfflinePlayers())
				.filter(p -> !p.isOnline())
				.filter(p -> !Database.getBags(p.getUniqueId().toString()).isEmpty())
				.sorted(Comparator.comparing(OfflinePlayer::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.toList();

		if(player.getName().equalsIgnoreCase("Alynie")) {
			player.sendMessage("§e[Debug] Found " + offlinePlayers.size() + " offline players with bags.");
			Log.info(plugin, "[Debug] Found " + offlinePlayers.size() + " offline players with bags.");
		}

		for (OfflinePlayer p : offlinePlayers) {
			String uuid = p.getUniqueId().toString();

			if(player.getName().equalsIgnoreCase("Alynie")) {
				player.sendMessage("§e[Debug] Player: " + p.getName() + " UUID: " + uuid);
				Log.info(plugin, "[Debug] Player: " + p.getName() + " UUID: " + uuid);
			}

			//ItemStack entry = HeadCreator.itemFromUuid(p.getUniqueId()); <-- Causes HTTP 429.
			ItemStack entry = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) entry.getItemMeta();
			PlayerProfile profile = SkinCache.getProfile(p.getName());
			if(profile != null) {
				meta.setOwnerProfile(profile);
			}
			meta.setDisplayName("§c" + p.getName());
			entry.setItemMeta(meta);
			PDC.setString(entry, "owner", uuid);
			bags.add(entry);
		}

		return bags;
	}

	List<ItemStack> preparePlayerBags(String playeruuid) {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		List<Data> bagdata = Database.getBagsData(playeruuid);

		for(Data data : bagdata){
			List<ItemStack> Content  = data.getContent();
			if (Content == null) continue;

			String bagTexture = Main.config.getString("bag.texture");
			ItemStack bagItem = new ItemStack(Material.AIR);

			if(data.getMaterial() != null) {
				bagItem.setType(data.getMaterial());
				if(data.getMaterial() == Material.PLAYER_HEAD) {
					if(!Utils.IsStringNullOrEmpty(data.getTexture())) {
						Database.setTextureValue(bagItem, data.getTexture());
					}else {
						Database.setTextureValue(bagItem, bagTexture);
					}
				}
			}
			else {
				if(Main.config.getString("bag-type").equalsIgnoreCase("HEAD")){
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
				meta.setDisplayName(Lang.parse(data.getName(), targetPlayer));
			}else {
				meta.setDisplayName(Lang.parse(Lang.lang.getString("bag-bound-name"), targetPlayer));
			}

			if(data.getModeldata() != null && data.getModeldata() != 0) {
				meta.setCustomModelData(data.getModeldata());
			}

			bagItem.setItemMeta(meta);

			if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
				ItemUtils.SetItemName(bagItem, Lang.parse(Lang.lang.getString("bag-bound-name"), targetPlayer));
			}

			PDC.setString(bagItem, "uuid", data.getUuid());
			// No need to set more, will be added automatically by HavenBags.UpdateBagItem(), which runs HavenBags.UpdateNBT();

			modifyMaxStack(bagItem, 1);

			if(!Utils.IsStringNullOrEmpty(data.getItemmodel())) {
				ItemUtils.SetItemModel(bagItem, data.getItemmodel());
			}

			try {
				HavenBags.updateBagItem(bagItem, targetPlayer);
			}catch(Exception e) {
				HavenBags.updateBagItem(bagItem, null);
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
		cancelMeta.setDisplayName(Lang.get("confirm-cancel"));
		List<String> c_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("confirm-cancel-lore")) {
			c_lore.add(Lang.parse(line, targetPlayer));
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
		confirmMeta.setDisplayName(Lang.get("confirm-confirm"));
		List<String> co_lore = new ArrayList<String>();
		for(String line : Lang.lang.getStringList("confirm-confirm-lore")) {
			co_lore.add(Lang.parse(line, targetPlayer));
		}
		confirmMeta.setLore(co_lore);
		confirmItem.setItemMeta(confirmMeta);
		PDC.setString(confirmItem, "bag-action", "confirm");
		buttons.add(confirmItem);

		return buttons;
	}
}
