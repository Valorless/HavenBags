package valorless.havenbags.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;

/**
 * Manages in-memory storage and persistence for Ethereal Bags.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Track which bag IDs belong to which player.</li>
 *   <li>Store serialized bag contents by bag ID.</li>
 *   <li>Load data on plugin start ({@link #init()}) and save on shutdown ({@link #shutdown()}).</li>
 * </ul>
 * Storage notes:
 * <ul>
 *   <li>Player-owned bag IDs in {@code bags} are stored per-player.</li>
 *   <li>Bag contents in {@code bagData} are stored under a composite key of the form
 *       "&lt;player-uuid&gt;-&lt;bagId&gt;" (player UUID, a dash, and the raw bag ID).</li>
 * </ul>
 * Data is persisted to plugins/HavenBags/bags/etherealbags.yml using
 * ValorlessUtils {@link Config} with JSON-serialized payloads.
 */
public class EtherealBags {
	
	/** Data file configuration representing bags/etherealbags.yml. */
	protected static Config config;

	/**
	 * Player bag ownership mapping.
	 * <p>
	 * Key: {@link Player} instance<br>
	 * Value: List of raw bag identifiers owned by the player (without UUID prefix)
	 */
	private static HashMap<Player, List<String>> bags = new HashMap<>();
	/**
	 * Bag data storage.
	 * <p>
	 * Key: bag identifier; may be the raw bag ID or a composite "&lt;uuid&gt;-&lt;bagId&gt;".
	 * Value: serialized bag contents as a list of {@link ItemStack}.
	 */
	private static HashMap<String, List<ItemStack>> bagData = new HashMap<>();
	
	/**
	 * Initialize the Ethereal Bags subsystem.
	 * <p>
	 * Ensures storage directories/files exist, loads persisted JSON strings from
	 * the YAML config into in-memory maps, and logs load duration.
	 */
	public static void init() {
		Log.Info(Main.plugin, "Loading skin cache..");
		long startTime = System.currentTimeMillis();
		bags.clear();
		bagData.clear();
		File root = new File(Main.plugin.getDataFolder() + "/bags");
		File file = new File(Main.plugin.getDataFolder() + "/bags/etherealbags.yml");
		
		if(!root.exists()) root.mkdir();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			config = new Config(Main.plugin, "/bags/etherealbags.yml");
		} else {
			config = new Config(Main.plugin, "/bags/etherealbags.yml");
			bags = JsonUtils.fromJson(config.GetString("bags"));
			if(bags == null) bags = new HashMap<Player, List<String>>();
			bagData = JsonUtils.fromJson(config.GetString("data"));
			if(bagData == null) bagData = new HashMap<String, List<ItemStack>>();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.Info(Main.plugin, String.format("Loaded %s ethereal bags. %sms", bagData.size(), duration));
		
	}
	
	/**
	 * Persist all bag ownership and contents to disk.
	 * <p>
	 * Writes JSON strings for both maps to the YAML file and logs save duration.
	 */
	public static void shutdown() {
		try {
			long startTime = System.currentTimeMillis();
			Log.Info(Main.plugin, "Saving ethereal bags..");

			if(config == null) {
				File root = new File(Main.plugin.getDataFolder() + "/bags");
				File file = new File(Main.plugin.getDataFolder() + "/bags/etherealbags.yml");

				if(!root.exists()) root.mkdir();
				if(!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}

					config = new Config(Main.plugin, "/bags/etherealbags.yml");
				} else {
					config = new Config(Main.plugin, "/bags/etherealbags.yml");
				}

				config.Set("bags", JsonUtils.toJson(bags));
				config.Set("data", JsonUtils.toJson(bagData));
				config.SaveConfig();
			} else {
				config.Set("bags", JsonUtils.toJson(bags));
				config.Set("data", JsonUtils.toJson(bagData));
				config.SaveConfig();
			}

			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			Log.Info(Main.plugin, String.format("Saved %s ethereal bags. %sms", bagData.size(), duration));
		}catch(Exception e) { 
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Check whether a player owns any bags.
	 * @param player Player to check
	 * @return true if the player has at least one bag; false otherwise
	 */
	public static Boolean hasBags(Player player) {
		if(bags.containsKey(player)) return true;
		return false;
	}
	
	/**
	 * Check if a player owns a specific bag ID.
	 * <p>
	 * Internally builds the composite id "&lt;uuid&gt;-&lt;bagId&gt;" for comparison.
	 * @param player Player to check
	 * @param bagId Bag identifier to look for
	 * @return true if found; false otherwise
	 */
	public static Boolean hasBag(Player player, String bagId) {
		String id = player.getUniqueId().toString() + "-" + bagId;
		if(bags.containsKey(player)) {
			List<String> playerBags = bags.get(player);
			return playerBags.contains(id);
		}
		return false;
	}
	
	/**
	 * Add a bag to a player and store initial contents.
	 * <p>
	 * Records the raw bag ID under the player in {@code bags}, and stores
	 * contents in {@code bagData} under the composite key "&lt;uuid&gt;-&lt;bagId&gt;".
	 * @param player Owner of the bag
	 * @param bagId Raw bag identifier
	 * @param contents Initial contents to store
	 * @return true if the bag was added; false if the player already has an entry
	 */
	public static Boolean addBag(Player player, String bagId, List<ItemStack> contents) {
		String id = player.getUniqueId().toString() + "-" + bagId;
		if(bags.containsKey(player)) {
			return false;
		}else {
			List<String> playerBags = new ArrayList<>();
			playerBags.add(bagId);
			bags.put(player, playerBags);
		}
		
		bagData.put(id, contents);
		return true;
	}
	
	/**
	 * Remove a bag ID from a player and delete its stored contents.
	 * <p>
	 * Removes the raw bag ID from {@code bags}. Also removes the entry from
	 * {@code bagData} using the composite key for the player and bag ID.
	 * @param player Owner of the bag
	 * @param bagId Bag identifier to remove
	 */
	public static void removeBag(Player player, String bagId) {
		String id = player.getUniqueId().toString() + "-" + bagId;
		if(bags.containsKey(player)) {
			List<String> playerBags = bags.get(player);
			playerBags.remove(bagId);
			bags.put(player, playerBags);
		}
		
		if(bagData.containsKey(id)) {
			bagData.remove(id);
		}
	}
	
	/**
	 * Update stored contents for a player's bag.
	 * <p>
	 * Contents are saved under the composite key "&lt;uuid&gt;-&lt;bagId&gt;".
	 * No-op if the composite key does not exist.
	 * @param player Owner of the bag
	 * @param bagId Bag identifier
	 * @param contents New contents to store
	 */
	public static void updateBagContents(Player player, String bagId, List<ItemStack> contents) {
		String id = player.getUniqueId().toString() + "-" + bagId;
		if(bagData.containsKey(bagId)) {
			bagData.put(id, contents);
		}
	}
	
	/**
	 * Get the contents of a player's bag or null if unknown.
	 * <p>
	 * Builds the composite key from the player and bagId and queries {@code bagData}.
	 * @param player Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return List of ItemStack if present; otherwise null
	 */
	public static List<ItemStack> getBagContentsOrNull(Player player, String bagId) {
		String id = player.getUniqueId().toString() + "-" + bagId;
		if(bagData.containsKey(id)) {
			return bagData.get(id);
		}
		return null;
	}
	
	/**
	 * Get the contents of a player's bag or an empty list if unknown.
	 * <p>
	 * Builds the composite key from the player and bagId and queries {@code bagData}.
	 * @param player Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return Bag contents if present; otherwise an empty list
	 */
	public static List<ItemStack> getBagContentsOrEmpty(Player player, String bagId) {
		String id = player.getUniqueId().toString() + "-" + bagId;
		if(bagData.containsKey(id)) {
			return bagData.get(id);
		}
		return new ArrayList<ItemStack>();
	}
	
	/**
	 * Get a player's raw bag IDs.
	 * @param player Player to query
	 * @return List of raw bag IDs owned by the player; empty if none
	 */
	public static List<String> getPlayerBags(Player player) {
		if(bags.containsKey(player)) {
			return bags.get(player);
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Get a player's bag IDs normalized by stripping any "&lt;uuid&gt;-" prefix.
	 * <p>
	 * If IDs are already raw, the list is returned unchanged.
	 * @param player Player to query
	 * @return List of normalized bag IDs
	 */
	public static List<String> getPlayerBagsFormatted(Player player) {
	    // get the bags (or an empty list) and remove the "<uuid>-" prefix from each id
	    List<String> playerBags = getPlayerBags(player);

	    String prefix = player.getUniqueId().toString() + "-";
	    return playerBags.stream()
	                     .map(bagId -> bagId.replace(prefix, ""))
	                     .collect(Collectors.toList());
	}
	
	/**
	 * Normalize a bag ID by stripping the player's "&lt;uuid&gt;-" prefix if present.
	 * @param player Owner of the bag
	 * @param bagId Raw or composite bag identifier
	 * @return The bagId with the player's "&lt;uuid&gt;-" prefix removed, if it existed
	 */
	public static String formatBagId(Player player, String bagId) {
		String prefix = player.getUniqueId().toString() + "-";
		return bagId.replace(prefix, "");
	}
	
}