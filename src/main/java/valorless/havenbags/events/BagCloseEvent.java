package valorless.havenbags.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.datamodels.Data;

/**
 * Called when a player closes a HavenBag inventory.
 * <p>
 * This event provides access to the {@link Player}, the {@link Inventory} that was closed,
 * the bag {@link ItemStack}, and the corresponding {@link Data} object storing bag metadata.
 * </p>
 *
 * <p>Use this event to handle cleanup, saving, or triggering custom logic
 * when a bag is closed.</p>
 */
public class BagCloseEvent extends Event {

    /** Required HandlerList for custom Bukkit events. */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Required boilerplate: returns the list of handlers listening to this event.
     *
     * @return HandlerList for BagCloseEvent
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required boilerplate: returns the static HandlerList for this event type.
     *
     * @return HandlerList for BagCloseEvent
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
    /** The inventory instance that was closed. */
    private final Inventory inv;

    /** The player who closed the bag. */
    private final Player player;

    /** The ItemStack representing the bag item. */
    private final ItemStack bagItem;

    /** The bag's associated data model. */
    private final Data bagData;
    
    private final boolean forced;
	
    /**
     * Constructs a new BagCloseEvent.
     *
     * @param inventory The bag's inventory that was closed
     * @param player The player who closed the bag
     * @param bagItem The ItemStack representing the bag
     * @param bagData The bag's associated {@link Data}
     * @param forced 
     */
    public BagCloseEvent(Inventory inventory, Player player, ItemStack bagItem, Data bagData, boolean forced) {
        this.inv = inventory;
        this.player = player;
        this.bagItem = bagItem;
        this.bagData = bagData;
        this.forced = forced;
    }

    /**
     * Gets the bag inventory that was closed.
     *
     * @return The closed inventory
     */
    public Inventory getInventory() {
        return inv;
    }
	
    /**
     * Gets the player who closed the bag.
     *
     * @return The player who closed the inventory
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

	public boolean isForceClosed() {
		return forced;
	}
}
