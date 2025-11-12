package valorless.havenbags.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;

/**
 * Represents the necessary information to create a custom bag in the HavenBags plugin.
 * This object holds details such as the bag's unique identifier (UUID), owner, contents,
 * and the player who created it.
 */
public class BagCreationObject {
	/** Unique identifier for the bag */
	String uuid;
	/** UUID string of the owner */
	String owner = "ownerless";
	/** List of ItemStacks representing the bag's contents */
	List<ItemStack> contents = new ArrayList<>();
	/** Player who created the bag */
	Player creator;
	
	/** 
	 * Constructor to initialize a BagCreationObject with a specified size for contents.
	 * All contents are initialized to null.
	 * 
	 * @param size the initial size of the bag's contents list, by the power of 9 (e.g., size=27 for 3 rows)
	 */
	public BagCreationObject(int size) {
		if(!HavenBags.isPowerOfNine(size)) {
			throw new IllegalArgumentException("Size must be a power of 9 (e.g., 9, 27, 54)");
		}
		this.uuid = UUID.randomUUID().toString();
		this.contents = new ArrayList<>(Arrays.asList(new ItemStack[size])); // elements are null
	}
	
	/**
	 * Constructor to initialize a BagCreationObject with specified contents.
	 * 
	 * @param contents the list of ItemStacks to be included in the bag
	 */
	public BagCreationObject(List<ItemStack> contents) {
		this.uuid = UUID.randomUUID().toString();
		this.contents = contents;
	}

	/**
	 * Gets the unique identifier (UUID string) for this bag.
	 *
	 * @return the bag's UUID string
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the unique identifier (UUID string) for this bag.
	 *
	 * @param uuid the UUID string to assign to this bag
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the UUID string of the bag's owner.
	 *
	 * @return the owner's UUID string, or "ownerless" if none is set
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Sets the UUID string of the bag's owner.
	 *
	 * @param owner the owner's UUID string
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Gets the list of ItemStacks representing the bag's contents.
	 *
	 * @return a mutable list of ItemStacks in this bag
	 */
	public List<ItemStack> getContents() {
		return contents;
	}

	/**
	 * Replaces the bag's contents with the provided list.
	 *
	 * @param contents the new list of ItemStacks to store in this bag
	 */
	public void setContents(List<ItemStack> contents) {
		this.contents = contents;
	}

	/**
	 * Gets the player who created this bag.
	 *
	 * @return the creating Player, or null if not set
	 */
	public Player getCreator() {
		return creator;
	}

	/**
	 * Sets the player who created this bag.
	 *
	 * @param creator the creating Player
	 */
	public void setCreator(Player creator) {
		this.creator = creator;
	}
}