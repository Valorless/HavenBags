package valorless.havenbags.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.datamodels.Data;

/**
 * Called when a player opens a HavenBag inventory.
 * <p>
 * This event provides access to the {@link Player}, the {@link Inventory} that was opened,
 * the bag {@link ItemStack}, and the corresponding {@link Data} object storing bag metadata.
 * </p>
 *
 * <p>Use this event to handle initialization, logging, or custom logic
 * when a bag is opened.</p>
 */
public class BagOpenEvent extends Event {

    /** Required HandlerList for custom Bukkit events. */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Required boilerplate: returns the list of handlers listening to this event.
     *
     * @return HandlerList for BagOpenEvent
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required boilerplate: returns the static HandlerList for this event type.
     *
     * @return HandlerList for BagOpenEvent
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
    /** The inventory instance that was opened. */
    private final Inventory inv;

    /** The player who opened the bag. */
    private final Player player;

    /** The ItemStack representing the bag item. */
    private final ItemStack bagItem;

    /** The bag's associated data model. */
    private final Data bagData;
	
    /**
     * Constructs a new BagOpenEvent.
     *
     * @param inventory The bag's inventory that was opened
     * @param player The player who opened the bag
     * @param bagItem The ItemStack representing the bag
     * @param bagData The bag's associated {@link Data}
     */
    public BagOpenEvent(Inventory inventory, Player player, ItemStack bagItem, Data bagData) {
        this.inv = inventory;
        this.player = player;
        this.bagItem = bagItem;
        this.bagData = bagData;
    }

    /**
     * Gets the bag inventory that was opened.
     *
     * @return The opened inventory
     */
    public Inventory getInventory() {
        return inv;
    }
	
    /**
     * Gets the player who opened the bag.
     *
     * @return The player who opened the inventory
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the ItemStack representing the bag.
     *
     * @return The bag ItemStack
     */
    public ItemStack getBagItem() {
        return bagItem;
    }

    /**
     * Gets the {@link Data} associated with the bag.
     *
     * @return The bag data
     */
    public Data getBagData() {
        return bagData;
    }
}
