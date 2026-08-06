package valorless.havenbags;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import valorless.annotations.Internal;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.havenbags.annotations.Nullable;
import valorless.havenbags.database.Files;
import valorless.havenbags.database.MySQL;
import valorless.havenbags.database.SQLite;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.enums.DatabaseType;
import valorless.havenbags.events.BagCreateEvent;
import valorless.havenbags.events.BagDeleteEvent;
import valorless.havenbags.gui.BagGUI;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.items.ItemUtils;

/**
 * Central data-access layer for HavenBags.
 *
 * <p>Manages the full lifecycle of {@link Bag} objects in memory and on disk/database:
 * loading on startup, dirty-tracking via {@link #changedBags}, periodic auto-saving,
 * and shutdown persistence. Supports four storage backends selected at runtime via
 * {@code save-type} in {@code config.yml}:
 * <ul>
 *   <li>{@link DatabaseType#FILES} – one YML file per bag</li>
 *   <li>{@link DatabaseType#SQLITE} – single SQLite database file</li>
 *   <li>{@link DatabaseType#MYSQL} – remote MySQL database (batched saves)</li>
 *   <li>{@link DatabaseType#MYSQLPLUS} – remote MySQL with real-time per-bag saves
 *       (auto-save timer is disabled)</li>
 * </ul>
 *
 * <p>Bag instances are stored in a UUID-keyed in-memory map and are considered the
 * single source of truth for runtime state. Mutating a bag through the public API
 * (e.g. {@link #updateBag}, {@link #addTrusted}) automatically registers it in
 * {@link #changedBags} so it is included in the next auto-save cycle.
 */
public class Database {

	/**
	 * Lightweight view of a bag item paired with its current content list.
	 * Used by callers that need both the {@link ItemStack} representation of the bag
	 * and its inventory contents without holding a full {@link Bag} reference.
	 */
	public static class BagSimple {
		/** The bag {@link ItemStack} as it exists in a player's inventory. */
		public final ItemStack item;
		/** The current contents of the bag's inventory slots. */
		public final List<ItemStack> content;

		/**
		 * Constructs a BagSimple pairing a bag item with its contents.
		 *
		 * @param item    the bag {@link ItemStack}
		 * @param content the bag's inventory contents
		 */
		public BagSimple(ItemStack item, List<ItemStack> content) {
			this.item = item;
			this.content = content;
		}
	}

	private static DatabaseType databaseType = DatabaseType.SQLITE;
	private static MySQL mysql;
	static SQLite sqlite;

	static BukkitRunnable autosave;

	/**
	 * Determines how a bag retrieval or update should be treated at the call site.
	 * <ul>
	 *   <li>{@code NULL} — no side-effects beyond the operation itself.</li>
	 *   <li>{@code PLAYER} — signals that a player is the initiator; the bag will
	 *       be marked open on retrieval and closed after a content update.</li>
	 * </ul>
	 */
	public enum UpdateSource { NULL, PLAYER }

	private static HashMap<UUID, Bag> data = new HashMap<>();

	/**
	 * Bags that have been mutated since the last save cycle.
	 *
	 * <p>Populated by {@link Bag#setChanged(boolean)} (called with {@code true}) and by
	 * the various Database mutator methods. Drained and cleared on each
	 * {@link #saveData} call. Marked {@link Internal} — external code should not
	 * manipulate this map directly; use the provided API methods instead.</p>
	 */
	@Internal
	public static HashMap<UUID, Bag> changedBags = new HashMap<>();

	/**
	 * Auto-save interval in server ticks, derived from {@code auto-save.interval}
	 * in {@code config.yml} (seconds × 20).
	 */
	public static long interval;

	private static boolean ready = false;

	/**
	 * Returns {@code true} once {@link #loadData()} has completed successfully
	 * and the in-memory bag map is available for use.
	 *
	 * @return {@code true} if the database layer is ready
	 */
	public static boolean isReady() {
		return ready;
	}

	/**
	 * Initialises the database layer on plugin enable.
	 *
	 * <p>Steps performed:
	 * <ol>
	 *   <li>Determines and sets the active {@link DatabaseType} from config.</li>
	 *   <li>Opens the appropriate connection (MySQL / SQLite) if required.</li>
	 *   <li>Calls {@link #loadData()} to populate the in-memory bag map.</li>
	 *   <li>Schedules the repeating auto-save task (skipped for MYSQLPLUS).</li>
	 * </ol>
	 * Disables the plugin if an invalid {@code save-type} is configured.
	 */
	@DoNotCall("Meant to be called by HavenBags only.") @Internal
	public static void init() {
		data.clear(); // Just in case
		DatabaseType type = DatabaseType.get(Main.config.getString("save-type").toUpperCase());
		if(type != null) setDatabaseType(type);
		else {
			Log.error(Main.plugin, String.format("Invalid database type \"%s\"\n"
					+ "Please choose either FILES, MYSQL, or SQLITE.", Main.config.getString("save-type")));
			Bukkit.getPluginManager().disablePlugin(Main.plugin);
		}

		if(getDatabaseType() == DatabaseType.MYSQL || getDatabaseType() == DatabaseType.MYSQLPLUS) {
			setMysql(new MySQL());
		}
		else if(getDatabaseType() == DatabaseType.SQLITE) {
			sqlite = new SQLite();
		}

		interval = Main.config.getInt("auto-save.interval")*20;
		loadData();

		if(getDatabaseType() == DatabaseType.MYSQLPLUS) return;

		autosave = new BukkitRunnable() {
			@Override
			public void run() {
				saveData(false);
			}
		};

		autosave.runTaskTimer(Main.plugin, interval, interval);
		//Log.info(Main.plugin, "Loaded bags: " + data.size());
	}

	/**
	 * Closes all active database connections cleanly.
	 * Should be called from {@code onDisable} after {@link #saveData(boolean, boolean...)}
	 * to ensure connections are released before the plugin unloads.
	 */
	@DoNotCall("Meant to be called by HavenBags only.") @Internal
	public static void shutdown() {
		if(getDatabaseType() == DatabaseType.MYSQL) {
			try {
				if(getMysql() != null) {
					getMysql().disconnect();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(getDatabaseType() == DatabaseType.SQLITE) {
			try {
				if(sqlite != null) {
					sqlite.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
			try {
				if(getMysql() != null) {
					getMysql().disconnect();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Switches the active storage backend to {@code type} at runtime.
	 *
	 * <p>Disconnects/closes the current backend, cancels the existing auto-save
	 * task, then starts a new auto-save timer for the new backend. For
	 * {@link DatabaseType#MYSQL} and {@link DatabaseType#MYSQLPLUS} the timer is
	 * immediately cancelled since those backends use real-time or per-call saving.</p>
	 *
	 * @param type the new {@link DatabaseType} to switch to
	 */
	public static void changeDatabase(DatabaseType type) {
		if(getDatabaseType() == DatabaseType.MYSQL || getDatabaseType() == DatabaseType.MYSQLPLUS) {
			try {
				getMysql().disconnect();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(getDatabaseType() == DatabaseType.SQLITE) {
			try {
				sqlite.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		setDatabaseType(type);
		autosave.cancel();
		autosave = new BukkitRunnable() {
			@Override
			public void run() {
				saveData(false);
			}
		};

		autosave.runTaskTimer(Main.plugin, interval, interval);

		if(type == DatabaseType.MYSQL || type == DatabaseType.MYSQLPLUS) {
			setMysql(new MySQL());
			autosave.cancel();
		}
		else if(type == DatabaseType.SQLITE) {
			sqlite = new SQLite();
		}

		if(type == DatabaseType.MYSQLPLUS) {
			autosave.cancel();
		}

	}

	/**
	 * Performs a lightweight config reload (interval only).
	 *
	 * <p><b>Note:</b> bag data is <em>not</em> reloaded. Use
	 * {@link #forceReload()} to fully reload bag data from the storage backend,
	 * at the cost of losing any unsaved in-memory changes.</p>
	 */
	public static void reload() {
		interval = Main.config.getInt("auto-save.interval");
		Log.info(Main.plugin, "Bag data was not reloaded. You can force bag data reload with /havenbags reload force");
		Log.warning(Main.plugin, "Any unsaved bag data will be lost!");
	}

	/**
	 * Discards the current in-memory bag map and reloads all bags from the
	 * storage backend via {@link #loadData()}.
	 *
	 * <p><b>Warning:</b> any unsaved changes will be lost. Call
	 * {@link #saveData(boolean, boolean...)} first if persistence is required.</p>
	 */
	public static void forceReload() {
		loadData();
	}

	/**
	 * Returns {@code true} if a bag with the given UUID is present in the
	 * in-memory map.
	 *
	 * @param uuid the bag UUID string to look up
	 * @return {@code true} if the bag is loaded
	 */
	public static boolean contains(@NotNull String uuid) {
		return getBag(uuid) != null;
	}

	/**
	 * Returns {@code true} if a bag with the given UUID exists in the
	 * in-memory map.
	 *
	 * <p>Unlike {@link #contains(String)}, this method validates the UUID format
	 * and returns {@code false} (rather than throwing) for {@code "null"} or
	 * malformed strings.</p>
	 *
	 * @param uuid the bag UUID string to check
	 * @return {@code true} if the bag exists, {@code false} for invalid/missing UUIDs
	 */
	public static Boolean bagExists(@NotNull String uuid) {
		if("null".equalsIgnoreCase(uuid)) {
			return false;
		}

		try {
			UUID.fromString(uuid);
		}catch(IllegalArgumentException e) {
			return false;
		}
		return data.containsKey(UUID.fromString(uuid));
	}

	private static Bag getbag(String uuid) {
		if("null".equalsIgnoreCase(uuid)) {
			return null;
		}
		try {
			UUID.fromString(uuid);
		}catch(IllegalArgumentException e) {
			Log.error(Main.plugin, String.format("Invalid UUID format: '%s'.", uuid));
			return null;
		}
		return data.get(UUID.fromString(uuid));
	}

	/**
	 * Retrieves a loaded {@link Bag} by UUID.
	 *
	 * <p>If {@code source} is {@link UpdateSource#PLAYER}, the bag is also marked as
	 * open ({@link Bag#setOpen(boolean)}) to reflect that a player is actively using it.
	 * Returns {@code null} and logs a debug message if the bag is not found.</p>
	 *
	 * @param uuid    the bag UUID string
	 * @param bagItem the {@link ItemStack} representing the bag (used for context; may be {@code null})
	 * @param source  optional — pass {@link UpdateSource#PLAYER} to mark the bag open
	 * @return the {@link Bag} instance, or {@code null} if not found
	 */
	public static Bag getBag(@NotNull String uuid, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}

		Bag bag = getbag(uuid);
		if(bag != null) {
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(true);
			}
			return bag;
		}
		Log.debug(Main.plugin, String.format("Failed to get bag '%s', this bag was not found.", uuid));
		Log.debug(Main.plugin, "If you keep seeing this error, please replace the bag causing it.");
		//if(bagItem != null) bagItem.setAmount(0);
		return null;
	}

	/**
	 * Updates a bag's content by UUID and marks it dirty for the next save cycle.
	 *
	 * <p>If {@code source} is {@link UpdateSource#PLAYER}, the bag is also marked
	 * as closed after the content update.</p>
	 *
	 * @param uuid    the bag UUID string
	 * @param content the new content list to store
	 * @param source  optional — pass {@link UpdateSource#PLAYER} to also mark the bag closed
	 */
	public static void updateBag(@NotNull String uuid, @NotNull List<ItemStack> content, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}

		Bag bag = getBag(uuid);

		if(bag == null) Log.error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));

		try {
			bag.setContent(content);
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(false);
			}
		}catch(Exception e) {
			Log.error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
			e.printStackTrace();
		}
	}

	/**
	 * Updates a bag's content and visual metadata (texture, model data, item model)
	 * from the given bag {@link ItemStack}, then marks it dirty for the next save cycle.
	 *
	 * <p>This overload reads the UUID from the item's PDC. If
	 * {@code capacity-based-textures} is enabled in config, the bag's texture is
	 * also updated to reflect fill level. If {@code source} is
	 * {@link UpdateSource#PLAYER}, the bag is marked as closed.</p>
	 *
	 * @param bagItem the bag {@link ItemStack} to read UUID and visual data from
	 * @param content the new content list to store
	 * @param source  optional — pass {@link UpdateSource#PLAYER} to also mark the bag closed
	 */
	@SuppressWarnings("deprecation")
	public static void updateBag(@NotNull ItemStack bagItem, @NotNull List<ItemStack> content, UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		String uuid = PDC.getString(bagItem, "uuid");

		Bag bag = getBag(uuid);

		if(bag == null) Log.error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));

		try {
			bag.setContent(content);

			if(bagItem.getType() == Material.PLAYER_HEAD) {
				bag.setTexture(HeadCreator.getTextureValue(bagItem));
				bag.setModeldata(0);
			}else {
				if(bagItem.hasItemMeta()) {
					if(bagItem.getItemMeta().hasCustomModelData()) {
						bag.setModeldata(bagItem.getItemMeta().getCustomModelData());
					}else {
						bag.setModeldata(0);
					}
					if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
						if(ItemUtils.GetItemModel(bagItem) != null) {
							bag.setItemmodel(ItemUtils.GetItemModel(bagItem).toString());
						}else {
							bag.setItemmodel("");
						}
					}else {
						bag.setItemmodel("");
					}
				}else {
					bag.setModeldata(0);
					bag.setItemmodel("");
				}
				bag.setTexture(Main.config.getString("bag.texture"));
			}

			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(false);
			}

			if(Main.config.getBool("capacity-based-textures.enabled")) {
				bag.setTexture(HavenBags.capacityTexture(bagItem, content));
				//bag.setTexture(HavenBags.CapacityTexture(bagItem, content));
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
					@Override
					public void run(){
						HavenBags.updateBagItem(bagItem, bag.getViewer());
					}
				}, 1L);

			}
		}catch(Exception e) {
			Log.error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new bag, stores it in memory, and persists it immediately for
	 * MYSQLPLUS. Fires a {@link valorless.havenbags.events.BagCreateEvent}.
	 *
	 * <p>Visual data (texture, model data, item model) and metadata (autopickup,
	 * blacklist, tooltip-style, etc.) are extracted from {@code bag}'s PDC and
	 * applied to the new {@link Bag} instance.</p>
	 *
	 * @param uuid    UUID string to assign to the new bag
	 * @param owner   UUID string of the owning player
	 * @param content initial content list
	 * @param creator the {@link Player} creating the bag, or {@code null}
	 * @param bag     the bag {@link ItemStack} to read visual/PDC data from
	 * @return the newly created {@link Bag} instance
	 */
	@SuppressWarnings("deprecation")
	public static Bag createBag(@NotNull String uuid, @NotNull String owner, @NotNull List<ItemStack> content, Player creator, ItemStack bag) {
		Bag dat = new Bag(uuid, owner, bag.getType());
		dat.setContent(content);
		if(creator != null)	dat.setCreator(creator.getUniqueId().toString());
		int size = 0;
		for(ItemStack item : content) {
			if(!PDC.has(item, "locked")) size++;
		}
		dat.setSize(size);
		if(bag.getType() == Material.PLAYER_HEAD) {
			dat.setTexture(HeadCreator.getTextureValue(bag));
			dat.setModeldata(0);
			dat.setItemmodel(null);
		}else {
			dat.setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=");
			if(bag.hasItemMeta()) {
				if(bag.getItemMeta().hasCustomModelData()) {
					dat.setModeldata(bag.getItemMeta().getCustomModelData());
				}

				if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
					if(ItemUtils.GetItemModel(bag) != null) {
						dat.setItemmodel(ItemUtils.GetItemModel(bag).toString());
					}else {
						dat.setItemmodel("");
					}
				}else {
					dat.setItemmodel("");
				}
			}
		}
		dat.setTrusted(new ArrayList<>());

		if(PDC.has(bag, "filter")) {
			dat.setAutopickup(PDC.getString(bag, "filter"));
		}else {
			dat.setAutopickup("null");
		}

		if(PDC.has(bag, "blacklist")) {
			dat.setBlacklist(PDC.getStringList(bag, "blacklist"));
			dat.setWhitelist(PDC.getBoolean(bag, "whitelist"));
			dat.setIgnoreGlobalBlacklist(PDC.getBoolean(bag, "igb"));
		}

		if(PDC.has(bag, "tooltip")) {
			dat.setTooltipStyle(PDC.getString(bag, "tooltip"));
		}else {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {
				if(bag.getItemMeta().hasTooltipStyle()) {
					dat.setTooltipStyle(bag.getItemMeta().getTooltipStyle().toString());
				}
			}
		}

		dat.setChanged(true);
		data.put(UUID.fromString(uuid), dat);
		if(databaseType == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				getMysql().saveBag(dat);
			});
		}
		Log.debug(Main.plugin, "[DI-30] " + "New bag data created: " + owner + "/" + uuid);
		Bukkit.getPluginManager().callEvent(new BagCreateEvent(creator, bag, dat));
		return dat;
	}

	/**
	 * Registers an already-constructed {@link Bag} into memory and persists it
	 * immediately for MYSQLPLUS. Fires a {@link valorless.havenbags.events.BagCreateEvent}.
	 *
	 * @param dat the pre-built {@link Bag} to register; must not be {@code null}
	 * @return the same {@link Bag} instance
	 * @throws IllegalArgumentException if {@code dat} is {@code null}
	 */
	public static Bag createBag(@NotNull Bag dat) {
		if(dat == null) throw new IllegalArgumentException("Data cannot be null");
		dat.setChanged(true);
		data.put(UUID.fromString(dat.getUuid()), dat);
		if(databaseType == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				getMysql().saveBag(dat);
			});
		}
		Log.debug(Main.plugin, "[DI-30] " + "New bag data created: " + dat.getOwner() + "/" + dat.getUuid());
		Player creator = Bukkit.getPlayer(UUID.fromString(dat.getCreator()));
		Bukkit.getPluginManager().callEvent(new BagCreateEvent(creator, null, dat));
		return dat;
	}

	/**
	 * Loads all bags from the active storage backend into the in-memory map.
	 *
	 * <p>Clears the ready flag for the duration of the load, then sets it back to
	 * {@code true} on completion. Existing in-memory data is replaced. Called
	 * automatically by {@link #init()} and {@link #forceReload()}.</p>
	 */
	@DoNotCall("Meant to be called by HavenBags only.") @Internal
	public static void loadData(){
		ready = false;
		Log.info(Main.plugin, "Loading bags..");
		long startTime = System.currentTimeMillis();
		int i = 0;
		if(getDatabaseType() == DatabaseType.FILES) {
			List<String> owners	= getBagOwners();
			for(String owner : owners) {
				List<String> bags = Files.getBags(owner);
				for(String bag : bags) {
					String path = String.format("%s/bags/%s/%s.yml", Main.plugin.getDataFolder(), owner, bag);

					File bagData;
					try {
						bagData = new File(path);
					} catch(Exception e) {
						e.printStackTrace();
						continue;
					}
					if(!bagData.exists()) {
						continue;
					}
					try {
						Bag dat = Files.loadBag(owner, bag);
						data.put(UUID.fromString(dat.getUuid()), dat);
						i++;
					} catch (Exception e) {
						Log.error(Main.plugin, bag);
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		else if(getDatabaseType() == DatabaseType.MYSQL) {
			HashMap<UUID, Bag> bags = getMysql().loadAllBags();
			data = bags;
			i = bags.size();
		}
		else if(getDatabaseType() == DatabaseType.SQLITE) {
			for(String uuid : sqlite.getAllBagUUIDs()) {
				Bag bag = sqlite.loadBag(uuid);
				data.put(UUID.fromString(uuid), bag);
				i++;
			}
		}
		else if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
			HashMap<UUID, Bag> bags = getMysql().loadAllBags();
			data = bags;
			i = bags.size();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.info(Main.plugin, String.format("Loaded %s bags. %sms", i, duration));
		ready = true;
	}

	/**
	 * Persists all dirty bags to the active storage backend.
	 *
	 * <p>Bags are collected for saving from two sources:</p>
	 * <ul>
	 *   <li><b>Full dump</b> — all bags in memory are included when
	 *       {@code shutdown} is {@code true} <em>or</em> when a non-empty
	 *       {@code conversion} array is supplied (e.g. during a database
	 *       conversion command).</li>
	 *   <li><b>Dirty-only</b> — on a normal auto-save tick ({@code saveData(false)}),
	 *       only bags registered in {@link #changedBags} are saved.
	 *       The map is drained as part of this process.</li>
	 * </ul>
	 *
	 * <p>SQLite and MYSQL saves run asynchronously during normal auto-save to avoid
	 * blocking the main thread; synchronous writes are used on shutdown and
	 * conversion to guarantee completion before the plugin unloads.</p>
	 *
	 * @param shutdown   {@code true} to perform a full synchronous save of all bags
	 *                   (used on plugin disable)
	 * @param conversion optional flag — pass {@code true} to include all bags in a
	 *                   synchronous save for database conversion purposes
	 */
	public static void saveData(boolean shutdown, boolean... conversion) {
		long startTime = System.currentTimeMillis();
		List<Bag> toSave = new ArrayList<>();

		if(shutdown || (conversion != null && conversion.length > 0)) {
			toSave.addAll(data.values());
		}

		List<UUID> keys = new ArrayList<>(changedBags.keySet());
		for(UUID uuid : keys) {
			if(uuid == null) continue;
			Bag dat = changedBags.get(uuid);
			toSave.add(dat);
			dat.setChanged(false);
			changedBags.remove(uuid); // Double remove to be sure
		}

		if(toSave.isEmpty()) {
			return;
		}
		if(Main.config.getBool("auto-save.message") || shutdown) Log.info(Main.plugin, "Saving bags..");
		for(Bag bag : toSave) {
			String uuid = bag.getUuid();
			String owner = bag.getOwner();

			if(getDatabaseType() == DatabaseType.FILES) {
				Log.debug(Main.plugin, "[DI-31] [FILES] " + "Attempting to write bag " + owner + "/" + uuid + " onto server");
				Files.saveBag(bag);
			}
			else if(getDatabaseType() == DatabaseType.SQLITE) {
				Log.debug(Main.plugin, "[DI-231] [SQLITE] " + "Attempting to write bag " + owner + "/" + uuid + " onto database");
				if(shutdown || conversion != null) {
					sqlite.saveBag(bag);
				}else {
					Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
						sqlite.saveBag(bag);
					});
				}
			}else
			if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
				if(!shutdown && conversion == null) {
					Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
						getMysql().saveBag(bag);
					});
				}
			}
		}

		if(getDatabaseType() == DatabaseType.MYSQL) {
			Log.debug(Main.plugin, "[DI-232] [MYSQL] " + "Attempting to write bags onto database");
			if(shutdown || conversion != null) {
				for(List<Bag> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
					getMysql().saveBags(chunk);
				}
			}else {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					for(List<Bag> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
						getMysql().saveBags(chunk);
					}
				});
			}
		}
		else if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
			Log.debug(Main.plugin, "[DI-233] [MYSQLPLUS] " + "Attempting to write bags onto database");
			if(shutdown || conversion != null) {
				for(List<Bag> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
					getMysql().saveBags(chunk);
				}
			}else {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					for(List<Bag> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
						getMysql().saveBags(chunk);
					}
				});
			}
		}


		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		if(Main.config.getBool("auto-save.message") || shutdown) Log.info(Main.plugin, String.format("Saved %s bags. %sms", toSave.size(), duration));
	}

	/**
	 * Removes the bag with the given UUID from the in-memory map and the
	 * {@link #changedBags} dirty set. Does <em>not</em> delete data from
	 * the storage backend — use {@link #deleteBag(String, Player...)} for that.
	 *
	 * @param uuid the bag UUID string to remove
	 */
	public static void removeBag(@NotNull String uuid) {
		UUID uid = UUID.fromString(uuid);
		Bag bag = data.get(uid);
		if(bag != null) {
			data.remove(uid);
			changedBags.remove(uid);
			Log.info(Main.plugin, String.format("Removed cached data for %s.", uuid));
			return;
		}
		Log.error(Main.plugin, String.format("Failed to remove cached data for %s.", uuid));
	}

	/**
	 * Permanently deletes a bag from both the storage backend and the in-memory map.
	 * Fires a {@link valorless.havenbags.events.BagDeleteEvent}.
	 *
	 * @param uuid   the bag UUID string to delete
	 * @param player optional — the {@link Player} responsible for the deletion
	 *               (attached to the fired event)
	 * @return {@code true} if deletion succeeded, {@code false} if the bag was
	 *         not found or a storage error occurred
	 */
	public static Boolean deleteBag(@NotNull String uuid, @Nullable Player... player) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			if(getDatabaseType() == DatabaseType.FILES) {
				try {
					Files.deleteFile(bag.getOwner(), uuid);
				}catch(Exception e) {
					Log.error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
					e.printStackTrace();
					return false;
				}
			}else if(getDatabaseType() == DatabaseType.MYSQL) {
				getMysql().deleteBag(uuid);
			}
			else if(getDatabaseType() == DatabaseType.SQLITE) {
				sqlite.deleteBag(uuid);
			}

			if(player != null && player.length > 0) {
				Bukkit.getPluginManager().callEvent(new BagDeleteEvent(player[0], bag.clone()));
			}else {
				Bukkit.getPluginManager().callEvent(new BagDeleteEvent(null, bag.clone()));
			}
			removeBag(uuid);
			changedBags.remove(UUID.fromString(uuid));
			Log.info(Main.plugin, String.format("Deleted data for %s.", uuid));
			return true;
		}
		Log.error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
		return false;
	}

	/**
	 * Returns a list of all bag UUID strings owned by the specified player.
	 *
	 * @param playerUUID the player's UUID string
	 * @return unmodifiable list of bag UUID strings; empty if the player owns none
	 */
	public static List<String> getBags(@NotNull String playerUUID) {
		Log.debug(Main.plugin, "[DI-32] " + playerUUID);
		return data.values().stream()
				.filter(dat -> dat.getOwner().equals(playerUUID))
				.map(Bag::getUuid)
				.toList();
	}

	/**
	 * Returns a list of all {@link Bag} instances owned by the specified player.
	 *
	 * @param playerUUID the player's UUID string
	 * @return unmodifiable list of {@link Bag} objects; empty if the player owns none
	 */
	public static List<Bag> getBagsData(@NotNull String playerUUID) {
		Log.debug(Main.plugin, "[DI-260] " + playerUUID);
		return data.values().stream()
				.filter(dat -> dat.getOwner().equals(playerUUID))
				.toList();
	}

	/**
	 * Returns a list of all player UUID strings that own at least one bag.
	 *
	 * <p>For the FILES backend this is derived from the bag directory structure;
	 * for SQL backends it is queried directly from the database.</p>
	 *
	 * @return list of owner UUID strings; empty if none found or on error
	 */
	public static List<String> getBagOwners(){
		if(getDatabaseType() == DatabaseType.FILES) {
			try {
				return Stream.of(new File(String.format("%s/bags/", Main.plugin.getDataFolder())).listFiles())
						.filter(File::isDirectory)
						.map(File::getName)
						.toList();
			} catch (Exception e) {
				return new ArrayList<>();
			}
		}else if(getDatabaseType() == DatabaseType.MYSQL) {
			return getMysql().getBagOwners();
		}
		else if(getDatabaseType() == DatabaseType.SQLITE) {
			return sqlite.getBagOwners();
		}

		return new ArrayList<>();
	}

	/**
	 * Checks whether the bag with the given UUID is currently open.
	 * Removes the bag item from the world ({@code amount = 0}) if the bag is not found.
	 *
	 * @param uuid    the bag UUID string
	 * @param bagItem the bag {@link ItemStack} (cleared if bag not found); may be {@code null}
	 * @return {@code true} if the bag is open
	 */
	public static boolean isBagOpen(@NotNull String uuid, ItemStack bagItem) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			return bag.isOpen();
		}
		Log.error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
		return false;
	}

	/**
	 * Checks whether the bag represented by the given {@link ItemStack} is
	 * currently open, using the UUID stored in its PDC.
	 *
	 * @param bagItem the bag {@link ItemStack}
	 * @return {@code true} if the bag is open; {@code false} if not found or
	 *         the item is not a used bag
	 */
	public static boolean isBagOpen(ItemStack bagItem) {
		if(BagState.getState(bagItem) != BagState.USED) return false;
		String uuid = HavenBags.getBagUUID(bagItem);
		if("null".equalsIgnoreCase(uuid)) {
			return false;
		}

		try {
			UUID.fromString(uuid);
		}catch(IllegalArgumentException e) {
			return false;
		}
		Bag bag = getbag(uuid);
		if(bag != null) {
			return bag.isOpen();
		}
		return false;
	}

	/**
	 * Returns the {@link Player} currently viewing the bag, or {@code null} if it
	 * is not open.
	 *
	 * @param uuid    the bag UUID string
	 * @param bagItem the bag {@link ItemStack} (cleared if bag not found); may be {@code null}
	 * @return the viewing {@link Player}, or {@code null}
	 */
	public static Player bagOpenBy(@NotNull String uuid, ItemStack bagItem) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			if(isBagOpen(uuid, bagItem)) {
				return bag.getViewer();
			}else return null;
		}
		Log.error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
		return null;
	}

	/**
	 * Marks the bag as open and records the viewing player.
	 * For MYSQLPLUS, the updated state is persisted immediately.
	 *
	 * @param uuid    the bag UUID string
	 * @param bagItem the bag {@link ItemStack} (cleared if bag not found); may be {@code null}
	 * @param player  the {@link Player} opening the bag
	 */
	public static void markBagOpen(@NotNull String uuid, ItemStack bagItem, Player player) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			bag.setOpen(true);
			bag.setViewer(player);
			if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					getMysql().saveBag(bag);
				});
			}
			return;
		}
		Log.error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}

	/**
	 * Marks the bag as open, records the viewing player, and attaches the
	 * associated {@link BagGUI} instance.
	 * For MYSQLPLUS, the updated state is persisted immediately.
	 *
	 * @param uuid    the bag UUID string
	 * @param bagItem the bag {@link ItemStack} (cleared if bag not found); may be {@code null}
	 * @param player  the {@link Player} opening the bag
	 * @param gui     the {@link BagGUI} instance managing the bag's inventory UI
	 */
	public static void markBagOpen(@NotNull String uuid, ItemStack bagItem, Player player, BagGUI gui) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			bag.setOpen(true);
			bag.setViewer(player);
			bag.setGui(gui);
			if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					getMysql().saveBag(bag);
				});
			}
			return;
		}
		Log.error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}

	/**
	 * Marks the bag as closed and clears the viewer and GUI references.
	 * For MYSQLPLUS, the updated state is persisted immediately.
	 *
	 * @param uuid the bag UUID string
	 */
	public static void markBagClosed(@NotNull String uuid) {
		Bag bag = getBag(uuid); // This will throw an error if the bag does not exist, which is fine.
		if(bag == null) {
			Log.error(Main.plugin, String.format("Failed to mark bag '%s' as closed, this bag was not found.", uuid));
			return;
		}
		bag.setOpen(false);
		bag.setViewer(null);
		bag.setGui(null);
		if(getDatabaseType() == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				getMysql().saveBag(bag);
			});
		}
	}

	/**
	 * Registers the bag with the given UUID in {@link #changedBags} so it will be
	 * included in the next save cycle.
	 *
	 * @param uuid the bag UUID string to mark as changed
	 */
	private void markBagChanged(@NotNull String uuid) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
		}
	}

	/**
	 * Returns all bags that are currently marked as open (i.e. being viewed by a player).
	 *
	 * @return unmodifiable list of open {@link Bag} instances
	 */
	public static List<Bag> getOpenBags() {
		return data.values().stream()
				.filter(Bag::isOpen)
				.toList();
	}

	/**
	 * Deserialises a JSON array string into a list of {@link JsonObject} elements.
	 * {@code null} JSON elements are preserved as {@code null} entries in the list
	 * to maintain slot alignment.
	 *
	 * @param json JSON array string produced by the bag content serialiser
	 * @return list of {@link JsonObject} entries, with {@code null} for empty slots
	 */
	public static List<JsonObject> deserializeItemStackList(String json) {
		JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

		List<JsonObject> itemList = new ArrayList<>();
		for (JsonElement element : jsonArray) {
			if (!element.isJsonNull()) { // Ignore null values
				itemList.add(element.getAsJsonObject());
			}else {
				itemList.add(null);
			}
		}

		return itemList;
	}

	/**
	 * Clears the contents of every bag currently loaded in memory.
	 *
	 * <p>Each bag's slots are replaced with {@code null} values. This is a
	 * destructive operation — use with caution.</p>
	 *
	 * @return {@code true} if all bags were cleared successfully, {@code false}
	 *         if an exception occurred
	 */
	@DoNotCall
	public static Boolean clearAllBagContents() {
		try {
			for(Bag dat : data.values()) {
				clearBagContent(dat.getUuid());
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Clears the contents of all bags owned by the specified player.
	 *
	 * @param playeruuid the owning player's UUID string
	 * @return {@code true} if all bags were cleared successfully, {@code false}
	 *         if an exception occurred
	 */
	public static Boolean clearBagContentPlayer(@NotNull String playeruuid) {
		try {
			for(String bag : getBags(playeruuid)) {
				clearBagContent(bag);
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Clears the contents of the bag with the given UUID.
	 *
	 * <p>If the bag has an open {@link BagGUI}, it is force-closed first.
	 * Slots are replaced with {@code null} values to preserve the bag's size.</p>
	 *
	 * @param uuid the bag UUID string
	 * @return {@code true} if the bag was found and cleared, {@code false} otherwise
	 */
	public static Boolean clearBagContent(@NotNull String uuid) {
		Bag bag = getbag(uuid);
		if(bag != null) {
			if(bag.getGui() != null) {
				bag.getGui().close(true);
			}

			bag.setContent(new ArrayList<>(Collections.nCopies(bag.getContent().size(), null)));
			Log.info(Main.plugin, String.format("Cleared content for %s.", uuid));
			return true;
		}
		Log.error(Main.plugin, String.format("Failed to clear content for %s.", uuid));
		return false;
	}

	/**
	 * Returns the currently active {@link DatabaseType}.
	 *
	 * @return the active database type
	 */
	public static DatabaseType getDatabaseType() {
		return databaseType;
	}

	/**
	 * Sets the active {@link DatabaseType}. Internal use only — call
	 * {@link #changeDatabase(DatabaseType)} for a safe runtime switch.
	 *
	 * @param databaseType the new database type
	 */
	@DoNotCall("Meant to be called by HavenBags only.") @Internal
	protected static void setDatabaseType(DatabaseType databaseType) {
		Database.databaseType = databaseType;
	}

	/**
	 * Returns the active {@link MySQL} connection wrapper, or {@code null} if
	 * the current backend is not MySQL-based.
	 *
	 * @return the {@link MySQL} instance, or {@code null}
	 */
	public static MySQL getMysql() {
		return mysql;
	}

	/**
	 * Sets the active {@link MySQL} connection wrapper. Internal use only.
	 *
	 * @param mysql the {@link MySQL} instance to use
	 */
	@DoNotCall("Meant to be called by HavenBags only.") @Internal
	protected static void setMysql(MySQL mysql) {
		Database.mysql = mysql;
	}

	/**
	 * Reset the tooltip-styles of ALL bags to the default one specified in the config.<br>
	 * This is used when the tooltip-style is changed in the config, to update all bags to the new style.
	 * <p>
	 * If the server is a version that does not support TooltipStyle, then all are set null.
	 */
	@DoNotCall("This method is used internally to reset the tooltip-styles of all bags to the value in config.yml. It should not be called outside of HavenBags.")
	public static void resetTooltipStyles() {
		for (Bag data : data.values()) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {
				data.setTooltipStyle(Main.config.getString("bag.tooltip-style"));
			}else {
				data.setTooltipStyle(null);
			}
		}
		// Forcefully save all changes
		saveData(true);
	}
}
