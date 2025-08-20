package valorless.havenbags.events.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

// Custom event used in UpgradeGUI to prepare the upgrade result.
// This event is triggered before the upgrade result is finalized, allowing for modifications or checks.
// It contains the player, the items in the upgrade slots, and the resulting item.
public class PrepareUpgradeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
	
    private final Inventory inv;
	private final Player player;
	private final ItemStack slot1;
	private final ItemStack slot2;
	private final ItemStack result;
	
	/**
	 * Constructs a new PrepareUpgradeEvent.
	 *
	 * @param player The player who is preparing the upgrade.
	 * @param slot1 The item in the first upgrade slot.
	 * @param slot2 The item in the second upgrade slot.
	 * @param result The resulting item after the upgrade.
	 */
	public PrepareUpgradeEvent(Inventory inventory, Player player, ItemStack slot1, ItemStack slot2, ItemStack result) {
		this.inv = inventory;
		this.player = player;
		this.slot1 = slot1;
		this.slot2 = slot2;
		this.result = result;
	}

	/**
	 * Gets the inventory associated with this upgrade event.
	 *
	 * @return The inventory where the upgrade is being prepared.
	 */
	public Inventory getInventory() {
		return inv;
	}

	/**
	 * Gets the player who is preparing the upgrade.
	 *
	 * @return The player involved in the upgrade.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the item in the first upgrade slot.
	 *
	 * @return The item in the first slot.
	 */
	public ItemStack getSlot1() {
		return slot1;
	}

	/**
	 * Gets the item in the second upgrade slot.
	 *
	 * @return The item in the second slot.
	 */
	public ItemStack getSlot2() {
		return slot2;
	}

	/**
	 * Gets the resulting item after the upgrade.
	 *
	 * @return The item that results from the upgrade.
	 */
	public ItemStack getResult() {
		return result;
	}

    // Required boilerplate
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
