package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Main;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;

/**
 * Manages applying configured "custom data" entries onto HavenBags items.
 *
 * <p>Custom data can be written either to:</p>
 * <ul>
 *   <li><b>NBT</b> via {@link NBT} (typically for compatibility with systems reading raw item NBT), or</li>
 *   <li><b>PDC</b> (PersistentDataContainer) via Bukkit's {@link org.bukkit.persistence.PersistentDataContainer} API.</li>
 * </ul>
 *
 * <p>The definitions are loaded from the plugin config under the {@code custom-data} section.
 * Entries are grouped by bag size (slot count). When a bag is updated, only models matching
 * the bag's {@code size} (stored in PDC under key {@code "size"}) are applied.</p>
 */
public class CustomData {
	
	/** Flag indicating whether custom data is enabled in config. Checked before applying any data models. */
	private static boolean enabled = false;

	/**
	 * Storage backend for a configured data entry.
	 * <p>
	 * The backing store determines how values are written and how they are namespaced.
	 * </p>
	 */
	public enum DataType {
		/** Store the value in the item's NBT tag (using {@link NBT}). */
		NBT,
		/** Store the value in the item's {@link org.bukkit.persistence.PersistentDataContainer}. */
		PDC,
	}

	/**
	 * Model representing one configured custom-data entry for a specific bag size.
	 *
	 * <p>For PDC entries, {@link #pluginName} is used to resolve the owning plugin namespace
	 * for the created {@link NamespacedKey}. For NBT entries, only {@link #key} and {@link #value}
	 * are used.</p>
	 */
	public static class DataModel {
		/**
		 * Bag size (slot count) this model applies to.
		 * <p>
		 * Different bag sizes can have different sets of custom-data entries.
		 * </p>
		 */
		public int slots;

		/**
		 * Plugin name used for PDC namespacing.
		 * <p>
		 * This is resolved via {@link Bukkit#getPluginManager()} and used to create a
		 * {@link NamespacedKey}. Ignored for {@link DataType#NBT}.
		 * </p>
		 */
		public String pluginName;

		/**
		 * Key used for NBT tag name or PDC {@link NamespacedKey#getKey()} depending on {@link #dataType}.
		 */
		public String key;

		/**
		 * Configured value to write.
		 * <p>
		 * For NBT this may be String/Integer/Double/Boolean/Float/UUID.
		 * For PDC this should match {@link #type}.
		 * </p>
		 */
		public Object value;

		/**
		 * PDC type hint used when writing to the {@link org.bukkit.persistence.PersistentDataContainer}.
		 * Defaults to {@link PersistentDataType#STRING}.
		 */
		@SuppressWarnings("rawtypes")
		public PersistentDataType type = PersistentDataType.STRING;

		/** Storage backend for this model. */
		public DataType dataType = null;

		/**
		 * Creates a model for a given bag size.
		 *
		 * @param slots bag size (slot count)
		 */
		public DataModel(int slots) {
			this.slots = slots;
		}

		/**
		 * Debug-friendly string representation of the model.
		 * Type is nulled for NBT entries since they don't use it, and shown for PDC entries to clarify the expected value type.
		 */
		@Override
		public String toString() {
			return String.format(
					"DataModel{dataType=%s, slots=%d, pluginName='%s', key='%s', value=%s, type=%s}",
					dataType, slots, pluginName, key, value, dataType == DataType.PDC ? type.toString() : null);
		}
	}

	/**
	 * In-memory cache of configured data models.
	 * <p>Populated by {@link #init()} and cleared/reloaded by {@link #reload()}.</p>
	 */
	static List<DataModel> dataModels = new ArrayList<>();

	/**
	 * Loads {@link #dataModels} from {@link Main#config}.
	 *
	 * <p>Expected config structure:</p>
	 * <pre>
	 * custom-data:
	 *   "9":
	 *     example:
	 *       plugin: "HavenBags"
	 *       key: "some_key"
	 *       type: "NBT" | "PDC"
	 *       value: ...
	 *       pdc-type: "STRING" | "INTEGER" | "DOUBLE" | "BOOLEAN" | "FLOAT" | "UUID"   # only for PDC
	 * </pre>
	 */
	public static void init() {
		Log.Debug(Main.plugin, "Initializing custom data models from config...");
		Config config = Main.config;
		if(Main.config.GetFile().getSection("custom-data") == null) {
			// No custom data configured.
			Log.Debug(Main.plugin, "No custom data configured, skipping initialization.");
			return;
		}
		enabled = Main.config.GetBool("custom-data.enabled");
		if(!enabled) {
			// Custom data explicitly disabled.
			Log.Debug(Main.plugin, "Custom data explicitly disabled in config, skipping initialization.");
			return;
		}
		for (String slot : config.GetFile().getSection("custom-data").getKeys(false)) {
			if("enabled".equalsIgnoreCase(slot)) {
				// Skip the "enabled" key if present, it's not a bag size.
				continue;
			}
			Log.Debug(Main.plugin, "Loading custom data models for bag size: " + slot);
			for (String key : config.GetFile().getSection("custom-data." + slot).getKeys(false)) {
				Log.Debug(Main.plugin, "Loading custom data model: " + key);
				DataModel dataModel = new DataModel(Integer.parseInt(slot));
				String path = String.format("custom-data.%s.%s", slot, key);
				dataModel.pluginName = config.GetString(path + ".plugin");
				dataModel.key = config.GetString(path + ".key");
				dataModel.dataType = DataType.valueOf(config.GetString(path + ".type").toUpperCase());
				switch (dataModel.dataType) {
				case NBT:
					dataModel.value = config.Get(path + ".value");
					break;
				case PDC:
					String pdcType = config.GetString(path + ".pdc-type").toUpperCase();
					switch (pdcType) {
					case "STRING":
						dataModel.type = PersistentDataType.STRING;
						dataModel.value = config.GetString(path + ".value");
						break;
					case "INTEGER":
						dataModel.type = PersistentDataType.INTEGER;
						dataModel.value = config.GetInt(path + ".value");
						break;
					case "DOUBLE":
						dataModel.type = PersistentDataType.DOUBLE;
						dataModel.value = config.GetDouble(path + ".value");
						break;
					case "BOOLEAN":
						dataModel.type = PersistentDataType.BOOLEAN;
						dataModel.value = config.GetBool(path + ".value");
						break;
					case "FLOAT":
						dataModel.type = PersistentDataType.FLOAT;
						dataModel.value = (float) config.GetDouble(path + ".value").floatValue();
						break;
					case "UUID":
						dataModel.type = PersistentDataType.STRING;
						dataModel.value = UUID.fromString(config.GetString(path + ".value"));
						break;
					}
				}
				dataModels.add(dataModel);
				Log.Debug(Main.plugin, "Loaded custom data model: " + dataModel);
			}
		}
	}

	/**
	 * Clears the cached models and reloads them from config.
	 */
	public static void reload() {
		dataModels.clear();
		init();
	}

	/**
	 * Applies all configured custom-data entries relevant to this bag's size.
	 *
	 * <p>The bag size is read from the bag's PDC key {@code "size"}.</p>
	 *
	 * @param bag the bag item to update (mutated in-place)
	 */
	@SuppressWarnings("unchecked")
	public static void updateBag(ItemStack bag) {
		if(!enabled) {
			// Custom data globally disabled, skip processing.
			return;
		}
		int size = PDC.GetInteger(bag, "size");
		for (DataModel dataModel : dataModels) {
			if (dataModel.slots == size) {
				switch (dataModel.dataType) {
				case NBT:
					// Update NBT data
					if(dataModel.value instanceof String) {
						NBT.SetString(bag, dataModel.key, (String) dataModel.value);
					} else if(dataModel.value instanceof Integer) {
						NBT.SetInt(bag, dataModel.key, (Integer) dataModel.value);
					} else if(dataModel.value instanceof Double) {
						NBT.SetDouble(bag, dataModel.key, (Double) dataModel.value);
					} else if(dataModel.value instanceof Boolean) {
						NBT.SetBool(bag, dataModel.key, (Boolean) dataModel.value);
					} else if(dataModel.value instanceof Float) {
						NBT.SetFloat(bag, dataModel.key, (Float) dataModel.value);
					} else if(dataModel.value instanceof UUID) {
						NBT.SetUUID(bag, dataModel.key, (UUID) dataModel.value);
					}
					break;
				case PDC:
					// Update PDC data
					JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(dataModel.pluginName);
					if(plugin != null) {
						NamespacedKey key = new NamespacedKey(plugin, dataModel.key);
						ItemMeta meta = bag.getItemMeta();
						meta.getPersistentDataContainer().set(key, dataModel.type, dataModel.value);
						bag.setItemMeta(meta);
					}
					break;
				}
			}
		}
	}

}