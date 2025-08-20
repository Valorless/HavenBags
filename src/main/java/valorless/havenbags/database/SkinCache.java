package valorless.havenbags.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.profile.PlayerProfile;

import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;

/**
 * This class is used to cache player skins and profiles.
 * It listens for player join events to update the cache and saves the cache to a file.
 */
public class SkinCache implements Listener {
	
	/**
	 * The configuration object for the skin cache.
	 * This is a static Config object that is used to read and write the skins.yml file.
	 */
	protected static Config config;
	
	/**
	 * The cache of player profiles.
	 * This is a static HashMap that stores player names as keys and PlayerProfile as values.
	 */
	private static HashMap<String, PlayerProfile> cache = new HashMap<>();

	/**
	 * Returns the cache of player profiles.
	 * The cache is a HashMap where the key is the player's name and the value is their PlayerProfile.
	 * 
	 * @return HashMap<String, PlayerProfile> - The cache of player profiles.
	 */
	public static HashMap<String, PlayerProfile> getCache() {
		return cache;
	}
	
	/**
	 * Initializes the SkinCache by registering the event listener and loading the cache.
	 * This method should be called when the plugin is enabled.
	 */
	public static void init() {
		Log.Debug(Main.plugin, "[DI-291] Registering SkinCache");
		Bukkit.getServer().getPluginManager().registerEvents(new SkinCache(), Main.plugin);
		loadCache();
		for(Player player : Bukkit.getOnlinePlayers()) {
			cache.put(player.getName(), player.getPlayerProfile());
		}
	}
	
	/**
	 * Saves the skin cache to the skins.yml file.
	 * This method is called on shutdown to ensure the cache is saved.
	 */
	public static void shutdown() {
		long startTime = System.currentTimeMillis();
		Log.Info(Main.plugin, "Saving skin cache..");
		cleanup(); // Quick cleanup before saving..
		
		config.Set("skins", JsonUtils.toJson(cache));
		config.SaveConfig();
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.Info(Main.plugin, String.format("Saved %s skins. %sms", cache.size(), duration));
	}
	
	/**
	 * Loads the skin cache from the skins.yml file.
	 * If the file does not exist, it creates a new one.
	 * The cache is stored in a HashMap with player names as keys and PlayerProfile as values.
	 */
	static void loadCache() {
		Log.Info(Main.plugin, "Loading skin cache..");
		long startTime = System.currentTimeMillis();
		cache.clear();
		File root = new File(Main.plugin.getDataFolder() + "/cache");
		File cacheFile = new File(Main.plugin.getDataFolder() + "/cache/skins.yml");
		
		if(!root.exists()) root.mkdir();
		if(!cacheFile.exists()) {
			try {
				cacheFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			config = new Config(Main.plugin, "/cache/skins.yml");
		} else {
			config = new Config(Main.plugin, "/cache/skins.yml");
			cache = JsonUtils.fromJson(config.GetString("skins"));
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.Info(Main.plugin, String.format("Loaded %s skins. %sms", cache.size(), duration));
	}
	
	/**
	 * Cleans up the cache by removing players who have no bags.
	 * This is called on shutdown to ensure the cache only contains players with bags.
	 */
	public static void cleanup() {
	    Iterator<Entry<String, PlayerProfile>> iterator = cache.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Entry<String, PlayerProfile> player = iterator.next();
	        UUID uuid = player.getValue().getUniqueId();
	        if (BagData.GetBags(uuid.toString()).isEmpty()) {
	        	Log.Debug(Main.plugin, "[DI-292] Removing player " + player.getKey() + " from skin cache, no bags found.");
	            iterator.remove();
	        }
	    }
	}
	
	/**
	 * Event handler for player join events.
	 * When a player joins, their profile is added to the cache.
	 * 
	 * @param e The PlayerJoinEvent that is triggered when a player joins the server.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		cache.put(e.getPlayer().getName(), e.getPlayer().getPlayerProfile());
	}
	
	/**
	 * Gets the PlayerProfile for a given player name.
	 * 
	 * @param player The name of the player whose profile is to be retrieved.
	 * @return PlayerProfile - The PlayerProfile of the specified player, or null if not found.
	 */
	public static PlayerProfile getProfile(String player) {
		return cache.get(player);
	}
	
	/**
	 * Gets the skin URL for a given player name.
	 * 
	 * @param player The name of the player whose skin URL is to be retrieved.
	 * @return URL - The URL of the player's skin, or null if not found.
	 */
	public static URL getSkin(String player) {
		return cache.get(player).getTextures().getSkin();
	}

}
