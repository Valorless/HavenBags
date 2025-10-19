package valorless.havenbags.events;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.EtherealBagSettings;

/**
 * Called when a new ethereal HavenBag is created.
 * <p>
 * This event is triggered after the bag has been successfully created
 * and assigned to the player, but before any items are added to it.
 * The bag is represented by a unique ID and key, and its settings
 * can be accessed via the EtherealBagSettings object.
 * The player receiving the bag and the command sender
 * who issued the creation command are also provided.
 * The bag starts empty, and items can be added afterward.
 * The bag's inventory can be accessed and modified using the provided
 * uuid and bagId, or using the bagKey, which includes both the uuid and bagId.
 * The bag's settings can be modified via the EtherealBagSettings object.
 * </p>
 *
 * <p>Use this event to handle setup, logging, or applying custom modifications
 * whenever a bag is first created.</p>
 */
public class BagEtherealCreateEvent extends Event {

    /** Required HandlerList for custom Bukkit events. */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Required boilerplate: returns the list of handlers listening to this event.
     *
     * @return HandlerList for BagEtherealCreateEvent
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required boilerplate: returns the static HandlerList for this event type.
     *
     * @return HandlerList for BagEtherealCreateEvent
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
    /** The player who received the bag. */
    private final Player player;
    /** The command sender who issued the bag creation. */
    private final CommandSender sender;
    /** The unique identifier (UUID) of the player who received the bag. */
    private final UUID uuid;
    /** The unique identifier for the created bag. */
    private final String bagId;
    /** The unique key associated with the created bag. */
    private final String bagKey;
    /** The settings/configuration of the created ethereal bag. */
    private final EtherealBagSettings bagSettings;
	
    /**
     * Constructs a new BagCreateEvent.
     *
     * @param player      The player who received the bag.
     * @param sender      The command sender who issued the bag creation.
     * @param bagId       The unique identifier for the created bag.
     * @param bagKey      The unique key associated with the created bag.
     * @param bagSettings The settings/configuration of the created ethereal bag.
     */
    public BagEtherealCreateEvent(Player player, CommandSender sender, String bagId, String bagKey, EtherealBagSettings bagSettings) {
        this.player = player;
        this.sender = sender;
        this.uuid = player.getUniqueId();
        this.bagId = bagId;
        this.bagKey = bagKey;
        this.bagSettings = bagSettings;
    }

    /**
     * Gets the player who received the bag.
     *
     * @return The player who received the bag.
     */
    public Player getPlayer() {
        return player;
    }

    /**
	 * Gets the command sender who issued the bag creation.
	 *
	 * @return The command sender who issued the bag creation.
	 */
	public CommandSender getSender() {
		return sender;
	}

	/**
	 * Gets the unique identifier (UUID) of the player who received the bag.
	 *
	 * @return The UUID of the player who received the bag.
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Gets the unique identifier for the created bag.
	 *
	 * @return The unique identifier for the created bag.
	 */
	public String getBagId() {
		return bagId;
	}

	/**
	 * Gets the unique key associated with the created bag.
	 *
	 * @return The unique key associated with the created bag.
	 */
	public String getBagKey() {
		return bagKey;
	}

	/**
	 * Gets the settings/configuration of the created ethereal bag.
	 *
	 * @return The EtherealBagSettings object containing the bag's settings.
	 */
	public EtherealBagSettings getBagSettings() {
		return bagSettings;
	}
	
	public Boolean setContents(List<ItemStack> contents) {
		return EtherealBags.updateBagContents(uuid, bagId, contents);
	}
}
