package valorless.havenbags.hooks;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import valorless.havenbags.Main;
import valorless.havenbags.features.CustomBags;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.logging.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Integration hook for the Nexo plugin.
 * <p>
 * Nexo is a custom items and blocks plugin that allows server owners to create
 * unique items with custom textures, properties, and behaviors. This hook enables
 * Ravencrest to integrate with Nexo's custom items and access its recipe system.
 * </p><p>
 * The hook is active only when Nexo is installed and enabled on the server.
 * Additionally, this hook implements {@link Listener} to track when Nexo items
 * are fully loaded and ready for use.
 */
public class NexoHook implements Listener {

	private static boolean hooked = false;
	private static JavaPlugin plugin = null;

	/**
	 * Indicates whether Nexo items have been loaded and are ready for use.
	 * <p>
	 * This flag is set to {@code true} when the {@link NexoItemsLoadedEvent} is fired,
	 * signaling that all Nexo custom items are available.
	 */
	public static boolean ready = false;

	/**
	 * Event handler for Nexo items loaded event.
	 * <p>
	 * This method is called when Nexo finishes loading all custom items.
	 * It sets the {@link #ready} flag to indicate that Nexo is fully initialized.
	 * 
	 * @param event the NexoItemsLoadedEvent triggered when items are loaded
	 */
	@EventHandler
	public void onItemsLoaded(NexoItemsLoadedEvent event) {
		Log.info(Main.plugin, "Nexo Ready.");
		ready = true;
		CustomBags.initiate();
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
	public NexoHook() {
		Log.debug(Main.plugin, "Attempting to hook Nexo.");
		if (Bukkit.getPluginManager().getPlugin("Nexo") != null) {
			plugin = (JavaPlugin)Bukkit.getPluginManager().getPlugin("Nexo");
			Log.info(Main.plugin, "Nexo integrated!");
			hooked = true;
			Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
		} else {
			Log.debug(Main.plugin, "Nexo not detected.");
		}

	}

	/**
	 * Checks if Nexo is currently hooked.
	 * <p>
	 * This method verifies whether Nexo is present and available for integration.
	 * 
	 * @return {@code true} if Nexo is installed and active, {@code false} otherwise
	 */
	public static boolean isHooked() {
		return hooked;
	}

	/**
	 * Gets the Nexo plugin instance.
	 * <p>
	 * This instance can be used to access Nexo functionality directly.
	 * 
	 * @return the Nexo plugin instance as a JavaPlugin, or {@code null} if not hooked
	 */
	public static JavaPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Retrieves all recipe configuration sections from a Nexo config file.
	 * <p>
	 * This method extracts all top-level configuration sections from the provided
	 * config, which typically represent individual recipes in Nexo's format.
	 * 
	 * @param config the configuration file to extract recipes from
	 * @return a list of ConfigurationSection objects representing individual recipes
	 */
	@SuppressWarnings("rawtypes")
	public static List<ConfigurationSection> GetRecipes(Config config) {
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
