package valorless.havenbags.hooks;

import java.math.BigDecimal;
import org.bukkit.entity.Player;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;

/**
 * Handles economy integration for the transmog system.
 * 
 * <p>This class integrates with the Essentials economy plugin to manage costs
 * for applying transmog appearances. Different appearances can have custom prices
 * configured, with a default price for unconfigured transmogs.</p>
 * 
 * <p>Pricing is based on the transmog type and item model, allowing administrators
 * to set higher costs for rare or desirable appearances.</p>
 * 
 * <p><strong>Note:</strong> Currently disabled in the codebase but ready for future use.</p>
 */
public class Eco {
	
	private static IEssentials ess;
	
	/**
	 * Initializes the economy integration with Essentials.
	 */
	public static void init() {
        ess = EssentialsHook.getInstance();
	}
	
	/**
	 * Checks if a player can afford the transaction.
	 * 
	 * @param player the player to check
	 * @param price the price of the transaction
	 * @return true if the player has enough money, false otherwise
	 */
	public static Boolean canAfford(Player player, double price) {
		User user = ess.getUser(player);
		BigDecimal bal = user.getMoney();
		BigDecimal cost = BigDecimal.valueOf(price);
		
		if(bal.compareTo(cost) == -1) return false;
		else return true;
	}
	
	/**
	 * Deducts money from a player's account.
	 * 
	 * @param player the player to charge
	 * @param amount the amount to deduct
	 */
	public static void takeMoney(Player player, double amount) {
		User user = ess.getUser(player);
		user.takeMoney(BigDecimal.valueOf(amount));
	}
	
}
