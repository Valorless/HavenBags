package valorless.havenbags.hooks;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

/**
 * Minimal wrapper around the active Vault economy provider.
 * <p>
 * This utility centralizes balance checks and withdrawals used by HavenBags features.
 * The provider instance is resolved through {@link VaultHook#getEconomy()} during
 * {@link #init()} and then reused by the static helper methods in this class.
 * </p>
 * <p>
 * Call {@link #init()} once during plugin startup before using
 * {@link #canAfford(UUID, double)} or {@link #takeMoney(UUID, double)}.
 * </p>
 */
public class Eco {

	private static Economy eco;

	/**
	 * Initializes the cached Vault economy provider.
	 */
	public static void init() {
        eco = VaultHook.getEconomy();
	}
	
	/**
	 * Checks whether a player has enough balance for a charge.
	 *
	 * @param player the UUID of the player to check
	 * @param price the amount that would be charged
	 * @return {@code true} when the player balance is greater than or equal to {@code price}
	 */
	public static Boolean canAfford(UUID player, double price) {
		double bal = eco.getBalance(Bukkit.getOfflinePlayer(player));
        return !(bal - price < 0);
	}

	/**
	 * Gets the current balance of a player.
	 *
	 * @param player the UUID of the player to check
	 * @return the player's current balance
	 */
	public static double getBalance(UUID player) {
		return eco.getBalance(Bukkit.getOfflinePlayer(player));
	}
	
	/**
	 * Withdraws funds from a player's account.
	 *
	 * @param player the UUID of the player to charge
	 * @param amount the amount to withdraw
	 */
	public static void takeMoney(UUID player, double amount) {
		eco.withdrawPlayer(Bukkit.getOfflinePlayer(player), amount);
	}

	/**
	 * Deposits funds to a player's account.
	 * @param player the UUID of the player to give
	 * @param amount the amount to deposit
	 */
	public static void giveMoney(UUID player, double amount) {
		eco.depositPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
}
