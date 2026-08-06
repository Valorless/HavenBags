package valorless.havenbags.features;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Main;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.logging.Log;

// Called from BagDamagePrevetion.

/**
 * Tracks and manages "bag health" (durability) for HavenBags.
 * <p>
 * Bag durability is stored on the bag {@link ItemStack} using PDC keys:
 * <ul>
 *   <li>{@code size} - bag slot count (used to resolve max health)</li>
 *   <li>{@code damage} - accumulated damage points</li>
 * </ul>
 * Current health is calculated as {@code maxHealth - damage}.
 * <p>
 * Configuration is loaded from {@code protect-bags.bag-health}:
 * <ul>
 *   <li>{@code enabled} - whether the system is active</li>
 *   <li>{@code health.default} - default max health for any bag size without an explicit override</li>
 *   <li>{@code health.<size>} - per-size max health overrides (where {@code <size>} is the bag slot count)</li>
 *   <li>{@code damage-delay} - minimum time (seconds) between applying damage in {@link #isBagSafe(Item)}</li>
 * </ul>
 * <p>
 * To avoid rapidly applying damage from frequent events (e.g. {@code EntityDamageEvent}),
 * {@link #isBagSafe(Item)} is rate-limited using an in-memory timestamp map.
 */
public class BagHealth {
	
	/** Whether the bag health system is enabled (loaded from config). */
	private static boolean enabled = false;
	/** Default maximum health to use when no per-size override exists (loaded from config). */
	private static int defaultHealth = 0;
	/** Map of bag size (slot count) to maximum health (loaded from config). */
	private static final HashMap<Integer, Integer> healthBySize = new HashMap<>();
	
	private static final Map<UUID, Long> damageCooldowns = new HashMap<>();
	private static long cooldownMs = 0L;
	
	/**
	 * Initializes the bag health system by loading the configuration values.
	 * <p>
	 * This method should be called during plugin startup to ensure that the health values and enabled
	 * state are set before any bags are processed.
	 */
	public static void init() {
		reload();
	}
	
	/**
	 * Reloads the bag health configuration from the plugin config.
	 * <p>
	 * Reads:
	 * <ul>
	 *   <li>{@code protect-bags.bag-health.enabled}</li>
	 *   <li>{@code protect-bags.bag-health.health.default}</li>
	 *   <li>{@code protect-bags.bag-health.health.<size>} (any numeric keys under {@code health})</li>
	 *   <li>{@code protect-bags.bag-health.damage-delay}</li>
	 * </ul>
	 * Non-numeric keys (besides {@code default}) will be ignored with a warning.
	 */
	public static void reload() {
		enabled = Main.config.getBool("protect-bags.bag-health.enabled") && Main.config.GetBool("protect-bags.enabled");
		defaultHealth = Main.config.getInt("protect-bags.bag-health.health.default");
		
		// Cooldown (seconds -> ms)
		double seconds = Main.config.getDouble("protect-bags.bag-health.damage-delay");
		if(seconds < 0) seconds = 0;
		cooldownMs = (long) (seconds * 1000.0);
		
		healthBySize.clear();
		if(Main.config.hasKey("protect-bags.bag-health.health")) {
			for (String key : Main.config.getFile().getSection("protect-bags.bag-health.health").getKeys(false)) {
				if("default".equalsIgnoreCase(key)) {
					// Skip the "default" key, it's not a bag size.
					continue;
				}
				try {
					int size = Integer.parseInt(key);
					int health = Main.config.getInt("protect-bags.bag-health.health." + key);
					healthBySize.put(size, health);
				} catch (NumberFormatException e) {
					Log.warning(Main.plugin, "Invalid bag size in config for bag health: " + key + ". Skipping.");
					//Main.plugin.getLogger().warning("Invalid bag size in config for bag health: " + key + ". Skipping.");
				}
			}
		}
	}
	
	/**
	 * Checks if bag health is enabled in the config.
	 * 
	 * @return {@code true} if bag health is enabled, otherwise {@code false}
	 */
	public static boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Gets the current damage value of the bag from its PersistentDataContainer.
	 * <p>
	 * The value is stored under the PDC key {@code "damage"}. If no damage is stored, returns 0.
	 * 
	 * @param bag The ItemStack representing the bag to check for damage
	 * @return The current damage value of the bag, or 0 if unset
	 */
	public static int getDamage(ItemStack bag) {
		return PDC.has(bag, "damage") ? PDC.getInteger(bag, "damage") : 0;
	}
	
	/**
	 * Sets the damage value of the bag in its PersistentDataContainer.
	 * <p>
	 * The value is stored under the PDC key {@code "damage"}.
	 * 
	 * @param bag The ItemStack representing the bag to set damage for
	 * @param damage The damage value to set for the bag
	 */
	public static void setDamage(ItemStack bag, int damage) {
		PDC.setinteger(bag, "damage", damage);
	}
	
	/**
	 * Adds the specified amount of damage to the bag's current damage value.
	 * <p>
	 * If the bag has no current damage, it will be treated as 0.
	 * 
	 * @param bag The ItemStack representing the bag to add damage to
	 * @param damageToAdd The amount of damage to add to the bag's current damage value
	 */
	public static void addDamage(ItemStack bag, int damageToAdd) {
		int currentDamage = getDamage(bag);
		setDamage(bag, currentDamage + damageToAdd);
	}
	
	/**
	 * Gets the current health of the bag.
	 * <p>
	 * Current health is calculated as {@code maxHealth - damage}. The max health is resolved by bag size
	 * (stored under PDC key {@code "size"}) using a per-size override when present, otherwise the default.
	 * 
	 * @param bag The ItemStack representing the bag to get current health for
	 * @return The current health of the bag
	 */
	public static int getCurrentHealth(ItemStack bag) {
		int slots = PDC.getInteger(bag, "size");
		int maxHealth = healthBySize.getOrDefault(slots, defaultHealth);
		int damage = getDamage(bag);
		return maxHealth - damage;
	}
	
	/**
	 * Gets the maximum health of the bag based on its size.
	 * <p>
	 * Bag size (slot count) is read from the PDC key {@code "size"}. If there is no configured value
	 * for that size, {@link #defaultHealth} is returned.
	 * 
	 * @param bag The ItemStack representing the bag to get max health for
	 * @return The maximum health of the bag based on its size
	 */
	public static int getMaxHealth(ItemStack bag) {
		int slots = PDC.getInteger(bag, "size");
        return healthBySize.getOrDefault(slots, defaultHealth);
	}
	
	/**
	 * Checks the bag's health and applies damage if necessary.
	 * <p>
	 * This method is intended to be called from damage events for dropped bag entities.
	 * Damage application is rate-limited by {@code protect-bags.bag-health.damage-delay}.
	 * 
	 * @param bag The dropped item entity representing the bag to check and apply damage to
	 * @return {@code true} if the bag is still healthy after applying damage (or during cooldown),
	 *         or {@code false} if it is broken
	 */
	public static boolean isBagSafe(Item bag) {
		ItemStack bagStack = bag.getItemStack();
		// If no cooldown is configured, behave as before.
		if(cooldownMs <= 0) {
			addDamage(bagStack, 1);
			return getCurrentHealth(bagStack) > 0;
		}
		
		long now = System.currentTimeMillis();
		long last = damageCooldowns.getOrDefault(bag.getUniqueId(), 0L);
		if(now - last < cooldownMs) {
			return true; // Still in cooldown, consider the bag safe.
		}
		damageCooldowns.put(bag.getUniqueId(), now);
		
		addDamage(bagStack, 1);
		return getCurrentHealth(bagStack) > 0;
	}

}