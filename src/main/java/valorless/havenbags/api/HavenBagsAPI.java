package valorless.havenbags.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Bag;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.Nullable;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.EtherealBagSettings;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;

/**
 * Public API for interacting with HavenBags.
 * Provides helpers to inspect items, fetch and mutate bag data,
 * manage ethereal bags, and create tokens.
 */
public class HavenBagsAPI {
	/**
	 * Checks whether the given item is a HavenBag.
	 * @param item item to check
	 * @return true if the item represents a bag
	 */
	public static boolean isBag(ItemStack item) {
		return HavenBags.IsBag(item);
	}
	
	/**
	 * Gets the UUID stored on a bag item.
	 * @param item bag item
	 * @return bag UUID string, or null if not a bag
	 */
	public static String getBagUUID(ItemStack item) {
		return HavenBags.GetBagUUID(item);
	}
	
	/**
	 * Resolves the live Data object for the given bag item.
	 * @param item bag item
	 * @return Data for the bag, or null if not a bag
	 */
	public static Data getBagData(ItemStack item) {
		if(!isBag(item)) return null;
		return BagData.GetBag(getBagUUID(item), null);
	}
	
	/**
	 * Checks whether a bag exists in storage.
	 * @param uuid bag UUID
	 * @return true if the bag exists
	 */
	public static boolean bagExists(String uuid) {
		return BagData.BagExists(uuid);
	}
	
	/**
	 * Fetches Data for a bag by UUID.
	 * @param uuid bag UUID
	 * @return Data for the bag or null if not found
	 */
	public static Data getBag(String uuid) {
		return BagData.GetBag(uuid, null);
	}
	
	/**
	 * Gets the owner UUID of a bag.
	 * @param uuid bag UUID
	 * @return owner UUID string
	 */
	public static String getBagOwner(String uuid) {
		return BagData.GetOwner(uuid);
	}
	
	/**
	 * Lists all bags owned by a player.
	 * @param playerUUID player UUID
	 * @return list of Data for the player's bags
	 */
	public static List<Data> getPlayerBags(String playerUUID) {
		return BagData.GetBagsData(playerUUID);
	}
	
	/**
	 * Lists all bag Data instances present in a player's inventory.
	 * @param player player to inspect
	 * @return list of Data for bags found
	 */
	public static List<Data> getBagsOnPlayer(Player player){
		List<Data> bags = new ArrayList<>();
		for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
			bags.add(BagData.GetBag(getBagUUID(bag.item), null));
		}
		return bags;
	}
	
	/**
	 * Checks if a bag is currently open by any viewer.
	 * @param uuid bag UUID
	 * @return true if open
	 */
	public static boolean isBagOpen(String uuid) {
		return BagData.IsBagOpen(uuid, null);
	}
	
	/**
	 * Gets the player who has the bag open.
	 * @param uuid bag UUID
	 * @return Player who opened the bag, or null if not open
	 */
	public static Player BagOpenBy(String uuid) {
		return BagData.BagOpenBy(uuid, null);
	}
	
	/**
	 * Returns Data objects for all currently open bags.
	 * @return list of open bag Data
	 */
	public static List<Data> getOpenBagsUUIDs() {
		return BagData.GetOpenBags();
	}
	
	/**
	 * Returns the active database type used by HavenBags.
	 * @return database type as string. i.e, "FILES", "SQLITE", etc.
	 */
	public static String getDatabaseType() {
		return BagData.getDatabase().toString();
	}
	
	/**
	 * Creates and persists a new bag from the provided creation descriptor.
	 * @param creationObject bag creation parameters
	 * @return Data for the created bag
	 */
	public static Data createBag(BagCreationObject creationObject) {
		return BagData.CreateBag(creationObject.uuid, creationObject.owner, creationObject.contents, creationObject.creator, null);
	}
	
	/**
	 * Deletes a bag by UUID.
	 * @param uuid bag UUID
	 * @return true if deletion succeeded
	 */
	public static boolean deleteBag(String uuid) {
		return BagData.DeleteBag(uuid);
	}
	
	/**
	 * Creates a skin token ItemStack for the given value and type.
	 * @param value token payload (e.g., texture or model key)
	 * @param type token type
	 * @return token item
	 */
	public static ItemStack createToken(String value, TokenType type) {
		return HavenBags.CreateSkinToken(value, type);
	}
	
	/**
	 * Creates an effect token for the given effect identifier.
	 * @param value effect id
	 * @return token item
	 */
	public static ItemStack createEffectToken(String value) {
		return HavenBags.CreateEffectToken(value);
	}
	
	/**
	 * Checks whether the given item is a valid skin token.
	 * @param item item to test
	 * @return true if the item is a skin token
	 */
	public static boolean isToken(ItemStack item) {
		return HavenBags.IsSkinToken(item);
	}
	
	/**
	 * Creates an ethereal bag entry for a player.
	 * @param bagId bag identifier
	 * @param player target player
	 * @param size inventory size (implementation dependent)
	 * @return true on success
	 */
	public static boolean createEtherealBag(String bagId, Player player, int size) {
		return EtherealBags.addBag(player.getUniqueId(), bagId, 0);
	}
	
	/**
	 * Removes an ethereal bag entry from a player.
	 * @param bagId bag identifier
	 * @param player target player
	 * @return true on success
	 */
	public static boolean removeEtherealBag(String bagId, Player player) {
		return EtherealBags.removeBag(player.getUniqueId(), bagId);
	}
	
	/**
	 * Gets the contents of an ethereal bag for a player.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @return contents as a list of ItemStacks, or null if not found
	 */
	public static List<ItemStack> getEtherealBags(String bagId, Player player) {
		return EtherealBags.getBagContentsOrNull(player.getUniqueId(), bagId);
	}
	
	/**
	 * Checks if the player has the specified ethereal bag.
	 * @param bagId bag identifier
	 * @param player player to check
	 * @return true if present
	 */
	public static boolean hasEtherialBag(String bagId, Player player) {
		return EtherealBags.hasBag(player.getUniqueId(), bagId);
	}
	
	/**
	 * Checks if the player has any ethereal bags.
	 * @param bagId ignored identifier
	 * @param player player to check
	 * @return true if the player has one or more ethereal bags
	 */
	public static boolean hasEtherialBags(String bagId, Player player) {
		return EtherealBags.hasBags(player.getUniqueId());
	}
	
	/**
	 * Checks if the specified ethereal bag is currently open.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @return true if open
	 */
	public static boolean isEtherealBagOpen(String bagId, Player player) {
		return EtherealBags.isOpen(player, bagId);
	}
	
	/**
	 * Replaces the contents of an ethereal bag for a player.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @param contents new contents list
	 * @return true on success
	 */
	public static boolean setEtherealBagContents(String bagId, Player player, List<ItemStack> contents) {
		return EtherealBags.updateBagContents(player.getUniqueId(), bagId, contents);
	}
	
	/**
	 * Retrieves settings for a player's ethereal bag.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @return the bag's settings, or null if absent
	 */
	public static EtherealBagSettings getEtherealBagSettings(String bagId, Player player) {
		return EtherealBags.getBagSettings(player.getUniqueId(), bagId);
	}
	
	
	
	// functions;
	
	/**
	 * Upgrades the given bag ItemStack to the next size tier.
	 * Increases the bag size by 9 slots, updates weight limit if applicable,
	 * and changes texture based on new size and owner status.
	 * 
	 * @param bag The ItemStack representing the bag to upgrade.
	 * @return A new ItemStack representing the upgraded bag, or the original bag if already at max size.
	 */
	public static ItemStack upgradeBag(ItemStack bag) {
		if(PDC.Has(bag, "upgrade") && PDC.GetBoolean(bag, "upgrade") == false) {
			return bag;
		}
		int size = PDC.GetInteger(bag, "size");
		if(size == 54) return bag;
		int newSize = size+9;
		ItemStack clonedBag = bag.clone();
		String owner = PDC.GetString(bag, "owner");
		
		PDC.SetInteger(clonedBag, "size", newSize);
		if(Main.weight.GetBool("weight-per-size")) {
			PDC.SetDouble(clonedBag, "weight-limit", Main.weight.GetDouble(String.format("weight-size-%s", newSize)));
		}
		HavenBags.UpdateBagLore(clonedBag, null, true);
		if(Main.config.GetBool("bag-textures.enabled") && !Main.config.GetBool("upgrades.keep-texture")) {
			if(owner.equalsIgnoreCase("ownerless")) {
				BagData.setTextureValue(clonedBag, Main.config.GetString(String.format("bag-textures.size-ownerless-%s", newSize)));
			}else {
				BagData.setTextureValue(clonedBag, Main.config.GetString(String.format("bag-textures.size-%s", newSize)));
			}
		}
		
		return clonedBag;
	}
	
	/**
	 * Sets a custom texture on the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to modify.
	 * @param base64Texture A valid Base64-encoded string representing the new texture.
	 * @throws IllegalArgumentException if the provided texture is not valid Base64.
	 */
	public static void setTexture(ItemStack bag, String base64Texture) {
		if(!Base64Validator.isValidBase64(base64Texture)) {
			throw new IllegalArgumentException("Provided texture is not valid Base64!");
		}
		BagData.setTextureValue(bag, base64Texture);
	}
	
	/**
	 * Retrieves the custom texture Base64 string from the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to inspect.
	 * @return A Base64-encoded string representing the bag's texture.
	 */
	public static String getTexture(ItemStack bag) {
		return BagData.getTextureValue(bag);
	}
	
	/**
	 * Refreshes the lore of the specified bag item to reflect its current state.
	 * 
	 * @param bag The ItemStack representing the bag to refresh.
	 */
	public static void refreshBagLore(ItemStack bag) {
		HavenBags.UpdateBagLore(bag, null, false);
	}
	
	/**
	 * Refreshes the lore of the specified bag item to reflect its current state.
	 * 
	 * @param bag The ItemStack representing the bag to refresh.
	 * @param player The player used in placeholder parsing.
	 */
	public static void refreshBagLore(ItemStack bag, Player player) {
		HavenBags.UpdateBagLore(bag, player);
	}
	
	/**
	 * Calculates the total weight of items contained within the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to evaluate.
	 * @return The total weight of the bag's contents.
	 */
	public static double getBagWeight(ItemStack bag) {
		return HavenBags.GetWeight(bag);
	}
	
	/**
	 * Calculates the total weight of a list of ItemStacks, typically representing bag contents.
	 * 
	 * @param bagContents A list of ItemStacks to evaluate.
	 * @return The total weight of the provided items.
	 */
	public static double getBagWeight(List<ItemStack> bagContents) {
		return HavenBags.GetWeight(bagContents);
	}
	
	/**
	 * Retrieves the weight of a single ItemStack.
	 * 
	 * @param item The ItemStack to evaluate.
	 * @return The weight of the specified item.
	 */
	public static double getItemWeight(ItemStack item) {
		return HavenBags.ItemWeight(item);
	}
	
	/**
	 * Checks if the specified item can be carried in the given bag, considering weight limits.
	 * 
	 * @param item The ItemStack to check.
	 * @param bag The ItemStack representing the bag.
	 * @return true if bag cannot hold more weight, false otherwise.
	 */
	public static boolean canCarry(ItemStack item, ItemStack bag) {
		return HavenBags.CanCarry(item, bag);
	}
	
	/**
	 * Checks if the specified item is blacklisted from being stored in HavenBags.
	 * <p>
	 * Takes into account if using blacklist or whitelist mode.<br>
	 * Please check the {@link #blacklistAsWhitelist(Data bagData)} before calling this method to ensure correct context.<br>
	 * For global context refer to {@link #blacklistAsWhitelist()}.
	 * 
	 * @param item The ItemStack to check.
	 * @param bagData (Optional) The Data object of the bag for context-specific checks; can be null.
	 * @return true if the item is blacklisted, false otherwise.
	 */
	public static boolean isItemBlacklisted(ItemStack item, @Nullable Data bagData) {
		return HavenBags.IsItemBlacklisted(item, bagData);
	}
	
	/**
	 * Checks if the global configuration is set to treat the blacklist as a whitelist.
	 * 
	 * @return true if blacklist is used as whitelist, false otherwise.
	 */
	public static boolean blacklistAsWhitelist() {
		return Main.config.GetBool("blacklist-as-whitelist");
	}
	
	/**
	 * Checks if the specified bag's configuration is set to treat its blacklist as a whitelist.
	 * 
	 * @param bagData The Data object of the bag to check.
	 * @return true if blacklist is used as whitelist for this bag, false otherwise.
	 */
	public static boolean blacklistAsWhitelist(Data bagData) {
		return bagData.isWhitelist();
	}
	
	/**
	 * Checks if the player can carry more bags based on configured limits.
	 * 
	 * @param player The player to check.
	 * @return true if the player can carry more bags, false otherwise.
	 */
	public static boolean canCarryMoreBags(Player player) {
		return HavenBags.CanCarryMoreBags(player);
	}
	
	/**
	 * Checks if the specified bag is full (no empty slots).
	 * 
	 * @param bag The ItemStack representing the bag to check.
	 * @return true if the bag is full, false otherwise.
	 */
	public static boolean isBagFull(ItemStack bag) {
		return HavenBags.IsBagFull(bag);
	}
	
	/**
	 * Checks if the bag with the specified UUID is full (no empty slots).
	 * 
	 * @param uuid The UUID string of the bag to check.
	 * @return true if the bag is full, false otherwise.
	 */
	public static boolean isBagFull(UUID uuid) {
		return HavenBags.IsBagFull(uuid);
	}
	
	/**
	 * Checks if the bag with the specified UUID is full (no empty slots).
	 * 
	 * @param uuid The UUID string of the bag to check.
	 * @return true if the bag is full, false otherwise.
	 */
	public static boolean isBagFull(String uuid) {
		return HavenBags.IsBagFull(uuid);
	}
	
	/**
	 * Counts the number of empty slots in the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to check.
	 * @return The number of empty slots in the bag.
	 */
	public static int bagSlotsEmpty(ItemStack bag) {
		return HavenBags.SlotsEmpty(bag);
	}
	
	/**
	 * Calculates how full the specified bag is.
	 * 
	 * @param bag The ItemStack representing the bag to evaluate.
	 * @return The percentage of used capacity in the bag (0.0 to 100.0).
	 */
	public static double usedCapacity(ItemStack bag) {
		return HavenBags.UsedCapacity(bag, getBagData(bag).getContent());
	}
    
	/** Calculates the total number of bag slots in a player's inventory.
	 * 
	 * @param player The player whose inventory to check.
	 * @return The total number of bag slots.
	 */
    public static int getBagSlotsInInventory(Player player) {
		return HavenBags.GetBagSlotsInInventory(player);
    }
    
    /** Checks if the player has any bags belonging to other players.
	 * 
	 * @param player The player to check.
	 * @return true if the player has bags owned by others, false otherwise.
	 */
    public static boolean hasOthersBag(Player player) {
    	return HavenBags.HasOthersBag(player);
    }
    
    /** Determines the appropriate texture for a bag based on its used capacity.
     *  (e.g., changes texture as the bag fills up)
     * 
     * @param bag The ItemStack representing the bag to evaluate.
     * @return A Base64-encoded string representing the texture for the bag's current capacity.<br>
     * If no special texture is defined in the configuration, returns the bag's current texture.
     */
    public static String capacityTexture(ItemStack bag) {
		return HavenBags.CapacityTexture(bag, getBagData(bag).getContent());
	}
    
    /** Retrieves the current state of the bag.
	 * 
	 * @param item The ItemStack representing the bag to evaluate.
	 * @return "NEW" if the bag is empty, "USED" otherwise.<br>
	 * May return "NULL" if the item is not a valid bag.
	 */
    public static String bagState(ItemStack item) {
		return HavenBags.BagState(item).toString().toUpperCase();
	}
}