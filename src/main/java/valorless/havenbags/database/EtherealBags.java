package valorless.havenbags.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Main;
import valorless.havenbags.datamodels.EtherealBagSettings;
import valorless.havenbags.gui.EtherealGUI;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;

/**
 * Manages in-memory storage and persistence for Ethereal Bags.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Track which bag IDs belong to which player (by UUID).</li>
 *   <li>Store serialized bag contents by a composite bag key.</li>
 *   <li>Load data on plugin start ({@link #init()}) and save on shutdown ({@link #shutdown()}).</li>
 *   <li>Track open Ethereal GUIs to close them safely during shutdown.</li>
 *   <li>Provide methods to add, remove, update, and query bags and their contents.</li>
 *   <li>Normalize bag IDs by stripping or adding the UUID prefix.</li>
 *   <li>Check and manage open GUIs for players and bag IDs.</li>
 * </ul>
 * Storage notes:
 * <ul>
 *   <li>bags: Map keyed by a player's UUID string. Value is a list of raw bag IDs.</li>
 *   <li>bagData: Map keyed by a composite string "&lt;uuid&gt;-&lt;bagId&gt;".</li>
 *   <li>bagSettings: Map keyed by the same composite string, storing {@link EtherealBagSettings} instances.</li>
 *   <li>openGUIs: List of currently open {@link EtherealGUI} instances.</li>
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
	 * Key: player's UUID as a String<br>
	 * Value: List of raw bag identifiers owned by the player (without UUID prefix)
	 */
	private static HashMap<String, List<String>> bags = new HashMap<>();
	/**
	 * Bag data storage.
	 * <p>
	 * Key: composite bag key "&lt;uuid&gt;-&lt;bagId&gt;".<br>
	 * Value: serialized bag contents as a list of {@link ItemStack}.
	 */
	private static HashMap<String, List<ItemStack>> bagData = new HashMap<>();
	
	/**
	 * Bag settings storage.
	 * <p>
	 * Key: composite bag key "&lt;uuid&gt;-&lt;bagId&gt;".<br>
	 * Value: {@link EtherealBagSettings} instance storing bag settings.
	 */
	private static HashMap<String, EtherealBagSettings> bagSettings = new HashMap<>();
	
	/**
	 * Currently open Ethereal GUIs, used to ensure they are closed before saving on shutdown.
	 */
	public static List<EtherealGUI> openGUIs = new ArrayList<>();
	
	/**
	 * Check if any specific player has a specific bag GUI open.
	 * @param player Player to check
	 * @param bagId Raw bag identifier
	 * @return true if the player has the specified bag GUI open; false otherwise
	 */
	public static Boolean isOpen(Player player, String bagId) {
		for(EtherealGUI gui : openGUIs) {
			if(gui.player.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())
					&& gui.bagId.equalsIgnoreCase(bagId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if any player has a specific bag GUI open by its composite ID.
	 * <p>
	 * The composite ID is in the format "&lt;uuid&gt;-&lt;bagId&gt;".
	 * @param bagId Composite bag identifier to check
	 * @return true if any player has the specified bag GUI open; false otherwise
	 */
	public static Boolean isOpen(String bagId) {
		for(EtherealGUI gui : openGUIs) {
			String compare = formatBagId(gui.player.getUniqueId(), gui.bagId);
			if(compare.equalsIgnoreCase(bagId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Close a specific player's open bag GUI by raw bag ID.
	 * @param player Player whose GUI to close
	 * @param bagId Raw bag identifier
	 * @return true if the GUI was found and closed; false otherwise
	 */
	public static Boolean closeGUI(Player player, String bagId) {
		for(EtherealGUI gui : openGUIs) {
			if(gui.player.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())
					&& gui.bagId.equalsIgnoreCase(bagId)) {
				gui.Close(true);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Close any player's open bag GUI by its composite ID.
	 * <p>
	 * The composite ID is in the format "&lt;uuid&gt;-&lt;bagId&gt;".
	 * @param bagId Composite bag identifier
	 * @return true if the GUI was found and closed; false otherwise
	 */
	public static Boolean closeGUI(String bagId) {
		for(EtherealGUI gui : openGUIs) {
			String compare = formatBagId(gui.player.getUniqueId(), gui.bagId);
			if(compare.equalsIgnoreCase(bagId)) {
				gui.Close(true);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Initialize the Ethereal Bags subsystem.
	 * <p>
	 * Ensures storage directories/files exist, loads persisted JSON strings from
	 * the YAML config into in-memory maps, clears any tracked open GUIs, and logs load duration.
	 */
	public static void init() {
		Log.Info(Main.plugin, "Loading skin cache..");
		long startTime = System.currentTimeMillis();
		bags.clear();
		bagData.clear();
		bagSettings.clear();
		openGUIs.clear();
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
			if(bags == null) bags = new HashMap<String, List<String>>();
			bagData = JsonUtils.fromJson(config.GetString("data"));
			if(bagData == null) bagData = new HashMap<String, List<ItemStack>>();
			bagSettings = JsonUtils.fromJson(config.GetString("features"));
			if(bagSettings == null) bagSettings = new HashMap<String, EtherealBagSettings>();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.Info(Main.plugin, String.format("Loaded %s ethereal bags. %sms", bagData.size(), duration));
		
	}
	
	/**
	 * Persist all bag ownership and contents to disk.
	 * <p>
	 * Closes any tracked open Ethereal GUIs first, then writes JSON strings for both
	 * maps to the YAML file and logs save duration.
	 */
	public static void shutdown() {
		try {
			long startTime = System.currentTimeMillis();
			Log.Info(Main.plugin, "Saving ethereal bags..");
			
			// close any open Ethereal GUIs to prevent issues on reload
			for (EtherealGUI gui : new ArrayList<>(openGUIs)) {
			    gui.Close(true);
			}

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
				config.Set("features", JsonUtils.toJson(bagSettings));
				config.SaveConfig();
			} else {
				config.Set("bags", JsonUtils.toJson(bags));
				config.Set("data", JsonUtils.toJson(bagData));
				config.Set("features", JsonUtils.toJson(bagSettings));
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
	 * @param uuid UUID to check
	 * @return true if the player has at least one bag; false otherwise
	 */
	public static Boolean hasBags(UUID uuid) {
		return bags.containsKey(uuid.toString()) && !bags.get(uuid.toString()).isEmpty();
	}
	
	/**
	 * Check if a player owns a specific bag ID.
	 * <p>
	 * This checks for the existence of the composite key "&lt;uuid&gt;-&lt;bagId&gt;" in {@code bagData}.
	 * @param uuid UUID to check
	 * @param bagId Bag identifier to look for
	 * @return true if found; false otherwise
	 */
	public static Boolean hasBag(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		if(bagData.containsKey(id)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Add a bag to a player and store initial contents.
	 * <p>
	 * Records the raw bag ID under the player in {@code bags}, and stores
	 * contents in {@code bagData} under the composite key "<uuid>-<bagId>".
	 * <p>
	 * Returns false if this player already has the specified bag ID or if the
	 * composite key already exists in {@code bagData}; otherwise returns true and
	 * persists the provided contents.
	 *
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @param contents Initial contents to store (will be saved as-is)
	 * @return true if the new bag was created; false if it already exists
	 */
	public static Boolean addBag(UUID uuid, String bagId, List<ItemStack> contents) {
		String id = uuid.toString() + "-" + bagId;
		
		// If the bag data already exists, don't overwrite — treat as "already added".
	    if (bagData.containsKey(id)) {
	        return false;
	    }
		
	    // Ensure the player's bag list exists, then add the new bagId (avoid duplicates)
	    List<String> playerBags = bags.get(uuid.toString());
	    if (playerBags == null) {
	        playerBags = new ArrayList<>();
	        playerBags.add(bagId);
	        bags.put(uuid.toString(), playerBags);
	    } else {
	        if (playerBags.contains(bagId)) {
	            // Player already has this bag id
	            return false;
	        }
	        playerBags.add(bagId);
	        bags.put(uuid.toString(), playerBags);
	    }

	    // Store the bag contents
	    bagData.put(id, contents);
	    bagSettings.put(id, new EtherealBagSettings());
	    return true;
	}
	
	/**
	 * Add a bag to a player with empty initial contents.
	 * <p>
	 * Records the raw bag ID under the player in {@code bags}, and stores
	 * empty contents in {@code bagData} under the composite key "<uuid>-<bagId>".
	 * The size parameter determines how many rows of 9 slots to create.
	 * Empty slots are represented by {@code null} entries.
	 * <p>
	 * Returns false if this player already has the specified bag ID or if the
	 * composite key already exists in {@code bagData}; otherwise returns true and
	 * initializes the contents to {@code size * 9} slots.
	 *
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @param size Number of rows (of 9 slots each) for the bag's initial contents
	 * @return true if the new bag was created; false if it already exists
	 */
	public static Boolean addBag(UUID uuid, String bagId, int size) {
	    String id = uuid.toString() + "-" + bagId;

	    // If the bag data already exists, don't overwrite — treat as "already added".
	    if (bagData.containsKey(id)) {
	        return false;
	    }

	    // Prepare empty contents
	    List<ItemStack> contents = new ArrayList<>();
	    for (int i = 0; i < size * 9; i++) {
	        contents.add(null); // null represents an empty slot
	    }

	    // Ensure the player's bag list exists, then add the new bagId (avoid duplicates)
	    List<String> playerBags = bags.get(uuid.toString());
	    if (playerBags == null) {
	        playerBags = new ArrayList<>();
	        playerBags.add(bagId);
	        bags.put(uuid.toString(), playerBags);
	    } else {
	        if (playerBags.contains(bagId)) {
	            // Player already has this bag id
	            return false;
	        }
	        playerBags.add(bagId);
	        bags.put(uuid.toString(), playerBags);
	    }

	    // Store the bag contents
	    bagData.put(id, contents);
	    bagSettings.put(id, new EtherealBagSettings());
	    return true;
	}
	
	/**
	 * Remove a bag ID from a player and delete its stored contents.
	 * <p>
	 * Removes the raw bag ID from {@code bags}. Also removes the entry from
	 * {@code bagData} using the composite key for the player and bag ID.
	 * If the player's bag list becomes empty, their entry is removed from {@code bags}.
	 * @param uuid Owner of the bag
	 * @param bagId Bag identifier to remove
	 * @return true if the bag existed and was removed; false otherwise
	 */
	public static Boolean removeBag(UUID uuid, String bagId) {
	    String key = uuid.toString();
	    String id = key + "-" + bagId;
	    
	    if(isOpen(id)) closeGUI(id);

	    if (bags.containsKey(key)) {
	        List<String> playerBags = bags.get(key);
	        if (playerBags != null) {
	            playerBags.remove(bagId);
	            if (playerBags.isEmpty()) {
	                bags.remove(key);
	            } else {
	                bags.put(key, playerBags);
	            }
	        }

		    bagData.remove(id);
		    bagSettings.remove(id);
		    return true;
	    }
	    return false;
	}
	
	/**
	 * Update stored contents for a player's bag.
	 * <p>
	 * Contents are saved under the composite key "&lt;uuid&gt;-&lt;bagId&gt;".
	 * No-op if the composite key does not exist.
	 * @param uuid Owner of the bag
	 * @param bagId Bag identifier
	 * @param contents New contents to store
	 * @return true if the bag existed and was updated; false otherwise
	 */
	public static Boolean updateBagContents(UUID uuid, String bagId, List<ItemStack> contents) {
		String id = uuid.toString() + "-" + bagId;
		if(bagData.containsKey(id)) {
			bagData.put(id, contents);
			return true;
		}
		return false;
	}
	
	/**
	 * Get the contents of a player's bag or null if unknown.
	 * <p>
	 * Builds the composite key from the player UUID and bagId and queries {@code bagData}.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return List of ItemStack if present; otherwise null
	 */
	public static List<ItemStack> getBagContentsOrNull(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		return bagData.getOrDefault(id, null);
	}
	
	/**
	 * Get the contents of a player's bag or an empty list if unknown.
	 * <p>
	 * Builds the composite key from the player UUID and bagId and queries {@code bagData}.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return Bag contents if present; otherwise an empty list
	 */
	public static List<ItemStack> getBagContentsOrEmpty(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		return bagData.getOrDefault(id, new ArrayList<ItemStack>());
	}
	
	/**
	 * Get a player's raw bag IDs.
	 * @param uuid UUID to query
	 * @return List of raw bag IDs owned by the player; empty if none
	 */
	public static List<String> getPlayerBags(UUID uuid) {
		for(String key : bags.keySet()) {
			if(key.equalsIgnoreCase(uuid.toString())){
				return bags.getOrDefault(key, new ArrayList<String>());
			}
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Get a player's bag IDs normalized by stripping any "&lt;uuid&gt;-" prefix.
	 * <p>
	 * If IDs are already raw, the list is returned unchanged.
	 * @param uuid UUID to query
	 * @return List of normalized bag IDs
	 */
	public static List<String> getPlayerBagsFormatted(UUID uuid) {
	    // get the bags (or an empty list) and remove the "<uuid>-" prefix from each id
		//Log.Info(Main.plugin, "[EtherealBags][DI-295] Retrieving formatted bag list for player " + uuid.toString());
	    List<String> playerBags = getPlayerBags(uuid);
	    //Log.Info(Main.plugin, "[EtherealBags][DI-296] Raw bag list: " + playerBags.toString());

	    String prefix = uuid.toString() + "-";
	    //Log.Info(Main.plugin, "[EtherealBags][DI-297] Using prefix: " + prefix);
	    List<String> formattedBags = playerBags.stream()
                .map(bagId -> bagId.replace(prefix, ""))
                .collect(Collectors.toList());
	    //Log.Info(Main.plugin, "[EtherealBags][DI-298] Formatted bag list: " + formattedBags.toString());
	    return formattedBags;
	}
	
	/**
	 * Get the settings of a player's bag or null if unknown.
	 * <p>
	 * Builds the composite key from the player UUID and bagId and queries {@code bagFeatures}.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return EtherealBagSettings instance if present; otherwise null
	 */
	public static EtherealBagSettings getBagSettings(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		if(bagSettings.containsKey(id)) {
			return bagSettings.get(id);
		}
		return null;
	}
	
	/**
	 * Get the autoPickup setting of a player's bag or "null" if unknown.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return autoPickup setting if present; otherwise "null"
	 */
	public static String getBagAutoPickup(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		EtherealBagSettings bag = getBagSettings(uuid, id);
		if(bag != null) {
			return bag.autoPickup;
		}
		return "null";
	}
	
	/**
	 * Get the magnet setting of a player's bag or false if unknown.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return magnet setting if present; otherwise false
	 */
	public static Boolean getBagMagnet(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		EtherealBagSettings bag = getBagSettings(uuid, id);
		if(bag != null) {
			return bag.magnet;
		}
		return false;
	}
	
	/**
	 * Get the autoSort setting of a player's bag or false if unknown.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return autoSort setting if present; otherwise false
	 */
	public static Boolean getBagAutoSort(UUID uuid, String bagId) {
		String id = uuid.toString() + "-" + bagId;
		EtherealBagSettings bag = getBagSettings(uuid, id);
		if(bag != null) {
			return bag.autoSort;
		}
		return false;
	}
	
	/**
	 * Normalize a bag ID by stripping the player's "&lt;uuid&gt;-" prefix if present.
	 * @param uuid Owner of the bag
	 * @param bagId Raw or composite bag identifier
	 * @return The bagId with the player's "&lt;uuid&gt;-" prefix removed, if it existed
	 */
	public static String stripBagId(UUID uuid, String bagId) {
		String prefix = uuid.toString() + "-";
		return bagId.replace(prefix, "");
	}
	
	/**
	 * Format a raw bag ID by adding the player's "&lt;uuid&gt;-" prefix.
	 * @param uuid Owner of the bag
	 * @param bagId Raw bag identifier
	 * @return The composite bag key "&lt;uuid&gt;-&lt;bagId&gt;"
	 */
	public static String formatBagId(UUID uuid, String bagId) {
		String prefix = uuid.toString() + "-" + bagId;
		return prefix;
	}
	
}