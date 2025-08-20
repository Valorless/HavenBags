package valorless.havenbags.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.datamodels.Data;

/**
 * Called when a new HavenBag is created.
 * <p>
 * This event provides access to the {@link Player} who created the bag,
 * the bag {@link ItemStack}, and the corresponding {@link Data} object storing bag metadata.
 * </p>
 *
 * <p>Use this event to handle setup, logging, or applying custom modifications
 * whenever a bag is first created.</p>
 */
public class BagCreateEvent extends Event {

    /** Required HandlerList for custom Bukkit events. */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Required boilerplate: returns the list of handlers listening to this event.
     *
     * @return HandlerList for BagCreateEvent
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required boilerplate: returns the static HandlerList for this event type.
     *
     * @return HandlerList for BagCreateEvent
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
    /** The player who created the bag. */
    private final Player player;

    /** The ItemStack representing the newly created bag. */
    private final ItemStack bagItem;

    /** The bag's associated data model. */
    private final Data bagData;
	
    /**
     * Constructs a new BagCreateEvent.
     *
     * @param player The player who created the bag
     * @param bagItem The ItemStack representing the bag
     * @param bagData The bag's associated {@link Data}
     */
    public BagCreateEvent(Player player, ItemStack bagItem, Data bagData) {
        this.player = player;
        this.bagItem = bagItem;
        this.bagData = bagData;
    }

    /**
     * Gets the player who created the bag.
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the ItemStack representing the newly created bag.
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
