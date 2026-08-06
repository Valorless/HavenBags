package valorless.havenbags.hooks;

import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import valorless.havenbags.Main;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.logging.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Integration hook for the Nexo plugin.
 * <p>
 * Oraxen is a custom items and blocks plugin that allows server owners to create
 * unique items with custom textures, properties, and behaviors. This hook enables
 * Ravencrest to integrate with Oraxen's custom items and access its recipe system.
 * </p><p>
 * The hook is active only when Oraxen is installed and enabled on the server.
 * Additionally, this hook implements {@link Listener} to track when Oraxen items
 * are fully loaded and ready for use.
 */
public class OraxenHook implements Listener {

	private static boolean hooked = false;
	private static JavaPlugin plugin = null;

	/**
	 * Indicates whether Oraxen items have been loaded and are ready for use.
	 * <p>
	 * This flag is set to {@code true} when the {@link OraxenItemsLoadedEvent} is fired,
	 * signaling that all Oraxen custom items are available.
	 */
	public static boolean ready = false;

	/**
	 * Event handler for Oraxen items loaded event.
	 * <p>
	 * This method is called when Oraxen finishes loading all custom items.
	 * It sets the {@link #ready} flag to indicate that Oraxen is fully initialized.
	 *
	 * @param event the OraxenItemsLoadedEvent triggered when items are loaded
	 */
	@EventHandler
	public void onItemsLoaded(OraxenItemsLoadedEvent event) {
		Log.info(Main.plugin, "Oraxen Ready.");
		ready = true;
	}

	/**
	 * Attempts to hook into Nexo.
	 * <p>
	 * Checks if Nexo is installed and logs the integration status.
	 * The hook is active only when the plugin is detected.
	 * </p><p>
	 * Note: Even after hooking, items may not be immediately available.
	 * Check the {@link #ready} flag to ensure items are loaded.
	 */
	public OraxenHook() {
		Log.debug(Main.plugin, "Attempting to hook Oraxen.");
		if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
			plugin = (JavaPlugin)Bukkit.getPluginManager().getPlugin("Oraxen");
			Log.info(Main.plugin, "Oraxen integrated!");
			hooked = true;
			Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
		} else {
			Log.debug(Main.plugin, "Oraxen not detected.");
		}

	}

	/**
	 * Checks if Oraxen is currently hooked.
	 * <p>
	 * This method verifies whether Oraxen is present and available for integration.
	 * 
	 * @return {@code true} if Oraxen is installed and active, {@code false} otherwise
	 */
	public static boolean isHooked() {
		return hooked;
	}

	/**
	 * Gets the Oraxen plugin instance.
	 * <p>
	 * This instance can be used to access Oraxen functionality directly.
	 * 
	 * @return the Oraxen plugin instance as a JavaPlugin, or {@code null} if not hooked
	 */
	public static JavaPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Retrieves all recipe configuration sections from a Oraxen config file.
	 * <p>
	 * This method extracts all top-level configuration sections from the provided
	 * config, which typically represent individual recipes in Oraxen's format.
	 * 
	 * @param config the configuration file to extract recipes from
	 * @return a list of ConfigurationSection objects representing individual recipes
	 */
	@SuppressWarnings("rawtypes")
	public static List<ConfigurationSection> getRecipes(Config config) {
		List<ConfigurationSection> list = new ArrayList<ConfigurationSection>();
		Iterator keys = config.getFile().getConfig().getKeys(false).iterator();

		Object o;
		while(keys.hasNext()) {
			o = keys.next();
			list.add(config.getConfigurationSection(o.toString()));
		}

		keys = config.getFile().getConfig().getKeys(false).iterator();

		while(keys.hasNext()) {
			o = keys.next();
			list.add(config.getConfigurationSection(o.toString()));
		}

		return list;
	}

}
