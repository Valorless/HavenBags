package valorless.havenbags.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import valorless.havenbags.datamodels.Bag;

/**
 * Called when a HavenBag is deleted.
 * <p>
 * This event provides access to the {@link Player} responsible for the deletion
 * (if applicable) and the {@link Bag} object associated with the bag.
 * </p>
 *
 * <p>Use this event to handle cleanup, saving, or applying custom logic
 * when a bag is permanently removed.</p>
 */
public class BagDeleteEvent extends Event {

    /** Required HandlerList for custom Bukkit events. */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Required boilerplate: returns the list of handlers listening to this event.
     *
     * @return HandlerList for BagDeleteEvent
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required boilerplate: returns the static HandlerList for this event type.
     *
     * @return HandlerList for BagDeleteEvent
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
    /** The player who deleted the bag (if applicable). */
    private final Player player;

    /** The bag's associated data model that is being deleted. */
    private final Bag bagData;
	
    /**
     * Constructs a new BagDeleteEvent.
     *
     * @param player The player who deleted the bag
     * @param bagData The bag's associated {@link Bag}
     */
    public BagDeleteEvent(Player player, Bag bagData) {
        this.player = player;
        this.bagData = bagData;
    }

    /**
     * Gets the player who deleted the bag.
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link Bag} associated with the deleted bag.
     *
     * @return The bag data
     */
    public Bag getBagData() {
        return bagData;
    }
}
