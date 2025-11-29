package valorless.havenbags.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Bag;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.Nullable;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.EtherealBagSettings;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.items.BagItemFactory;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.utils.TextFeatures;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.translate.Translator;
import valorless.valorlessutils.utils.Utils;

/**
 * Public API for interacting with HavenBags.
 * Provides helpers to inspect items, fetch and mutate bag data,
 * manage ethereal bags, and create tokens.
 * <p>
 * I also threw in a bunch of random methods.<br>
 * Have fun!
 */
public class HavenBagsAPI {
	
	/**
	 * Gets the main plugin instance.
	 * @return HavenBags JavaPlugin instance
	 */
	public static JavaPlugin getInstance() {
		return Main.plugin;
	}
	
	/**
	 * Forces the closure of all open bags for all players.
	 */
	public static void forceCloseAllBags() {
		Main.CloseBags();
	}
	
	/**
	 * Gets the Translator instance used by HavenBags.
	 * @return Translator instance
	 */
	public static Translator getTranslator() {
		return Main.translator;
	}
	
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
	 * Generates a bag item from the provided Data.
	 * @param bagData Data for the bag
	 * @return ItemStack representing the bag
	 */
	public static ItemStack generateBagItem(Data bagData) {
		return BagItemFactory.toItemStack(bagData);
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
	public static Player bagOpenBy(String uuid) {
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
	 * Creates and persists a new bag from the provided Data object.
	 * @param bagData bag Data
	 * @return Data for the created bag
	 */
	public static Data createBag(Data bagData) {
		return BagData.CreateBag(bagData);
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
    
    /** Extracts the URL from a Base64-encoded texture string.
     * 
     * @param base64 The Base64-encoded texture string.
     * @return The extracted URL.
     */
    public static String extractUrlFromBase64(String base64) {
		return HeadCreator.extractUrlFromBase64(base64);
	}
    
    /** Converts a texture URL to a Base64-encoded string.
	 * 
	 * @param url The texture URL.
	 * @return The Base64-encoded texture string.
	 */
    public static String convertUrlToBase64(String url) {
		return HeadCreator.convertUrlToBase64(url);
	}
    
    /** Creates a player head ItemStack with a custom texture from a Base64 string.
     * 
     * @param base64 The Base64-encoded texture string.
     * @return The custom player head ItemStack.
     */
    public static ItemStack creteHeadFromBase64(String base64) {
    	return HeadCreator.itemFromBase64(base64);
    }
    
    public static ItemStack createUnusedBagItem(int size, boolean binding) {
		Config config = valorless.havenbags.Main.config;
		String bagTexture = config.GetString("bag-texture");
		ItemStack bagItem = new ItemStack(Material.AIR);

		if(config.GetString("bag-type").equalsIgnoreCase("HEAD")){
			if(config.GetBool("bag-textures.enabled")) {
				for(int s = 9; s <= 54; s += 9) {
					if(size == s) {
						bagItem = HeadCreator.itemFromBase64(config.GetString("bag-textures.size-" + size));
					}
				}
			}else {
				bagItem = HeadCreator.itemFromBase64(bagTexture);
			}
		} else if(config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(config.GetMaterial("bag-material"));
		}
		
		ItemMeta bagMeta = bagItem.getItemMeta();
		if(config.GetInt("bag-custom-model-data") != 0 && config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
			bagMeta.setCustomModelData(config.GetInt("bag-custom-model-data"));
			if(config.GetBool("bag-custom-model-datas.enabled")) {
				for(int s = 9; s <= 54; s += 9) {
					if(size == s) {
						bagMeta.setCustomModelData(config.GetInt("bag-custom-model-datas.size-" + size));
					}
				}
			}
		}

		bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.GetStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, null));
		}
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		placeholders.add(new Placeholder("%size%", size));
		lore.add(Lang.Get("bag-size").replace("%size", "" + size));
		
		bagMeta.setLore(lore);
		bagItem.setItemMeta(bagMeta);

		if(Server.VersionHigherOrEqualTo(Version.v1_21_5)) {
			ItemUtils.SetMaxStackSize(bagItem, 1);
		}

		if(config.GetBool("bag-custom-model-datas.enabled")) {
			for(int s = 9; s <= 54; s += 9) {
				if(size == s) {
					if(!Utils.IsStringNullOrEmpty(config.GetString("bag-custom-model-datas.size-" + size)) && 
							!config.GetString("bag-custom-model-datas.size-" + size).matches("-?\\d+(\\.\\d+)?")) {
						ItemUtils.SetItemModel(bagItem, config.GetString("bag-custom-model-datas.size-" + size));
					}
				}
			}
		}

		if(!Utils.IsStringNullOrEmpty(config.GetString("bag-item-model"))) {
			ItemUtils.SetItemModel(bagItem, config.GetString("bag-item-model"));
		}

		PDC.SetString(bagItem, "uuid", "null");
		PDC.SetString(bagItem, "owner", "null");
		PDC.SetInteger(bagItem, "size", size);
		PDC.SetBoolean(bagItem, "binding", binding);
		
		return bagItem;
	}
    
    /*
    public static Data getUnusedBagData(ItemStack bagItem) {
    	String uuid = PDC.GetString(bagItem, "uuid");
    	String owner = PDC.GetString(bagItem, "owner");
    	int size = PDC.GetInteger(bagItem, "size");
    	//Boolean binding = PDC.GetBoolean(bagItem, "binding");
    	Data bagData = new Data(uuid, owner);
    	bagData.setSize(size);
    	return bagData;
    }
    */
    
    /**
	 * GUI-related utilities for HavenBags.
	 */
    public static class GUI {
    	/**
		 * Creates a paginated inventory GUI for displaying items.<br>
		 * if the number of items exceeds the capacity of a single page,
		 * multiple pages will be created.
		 * <p>
		 * Each page will have the specified number of rows (up to 6),
		 * and navigation controls will be added to move between pages.<br>
		 * The bottom row is reserved for navigation buttons.
		 * <p>
		 * The buttons for navigation are placed in the bottom row of the inventory.<br>
		 * Each button have their action stored in their PDC for handling clicks, as "havenbags:bag-action".
		 * <p>
		 * <code>PDC.SetString(button, "bag-action", action.toString());</code>
		 * <p>
		 * Possible actions are:<br>
		 * - "prev-page": Go to the previous page.<br>
		 * - "next-page": Go to the next page.<br>
		 * - "return": Exit button.
		 * <p>
		 * 
		 * See {@link #getPageActionKey(ItemStack button)} to retrieve the action from a button.
		 * <p>
		 * Note: This method only creates a single page based on the provided page number.<br>
		 * To handle multiple pages, you will need to call this method with different page numbers
		 * and manage the navigation logic in your plugin.
		 * <p>
		 * <b>Example usage:</b>
		 * <pre>
		 * List&lt;ItemStack&gt; items = ...; // Your list of items to display
		 * int rows = 5; // Number of rows for the inventory
		 * int page = 0; // Page number to display
		 * Inventory inventory = HavenBagsAPI.GUI.createPage(player, "My Items", page, items, rows);
		 * player.openInventory(inventory);
		 * </pre>
		 * 
		 * @param player The player for whom the inventory is created.
		 * @param title The title of the inventory GUI.
		 * @param page The page number to display (0-indexed).
		 * @param items The list of ItemStacks to paginate.
		 * @param rows The number of rows in the inventory (max 6).
		 * @return The created Inventory object representing the paginated GUI.
		 */
    	public static Inventory createPage(Player player, String title, int page, List<ItemStack> items, int rows) {
    		return valorless.havenbags.utils.GUI.CreatePage(player, title, page, items, rows);
    	}
    	
    	/**
		 * Retrieves the action associated with a pagination button.
		 * 
		 * @param button The ItemStack representing the pagination button.
		 * @return The action string stored in the button's PDC, or null if not found.
		 */
    	public static String getPageActionKey(ItemStack button) {
    		try {
    			return PDC.GetString(button, "bag-action");
    		}catch(Exception e) {
				return null;
			}
		}
    	
    	/**
    	 * Gets the list of page number textures used for paginated GUIs.
    	 * 
    	 * @return List of 32 (0-31) Base64-encoded texture strings for page numbers.
    	 */
    	public static List<String> getPageNumberTextures(){
			return valorless.havenbags.utils.GUI.PageNumberTextures;
		}
    	
    	/**
    	 * Creates a textual progress bar representation.
    	 * 
    	 * @param progress The current progress value
    	 * @param total The total value for completion
    	 * @param barLength The length of the bar in characters
    	 * @return A String representation of the progress bar
    	 */
    	public static String createBar(double progress, double total, int barLength) {
			return TextFeatures.CreateBar(progress, total, barLength);
		}
    	
    	/**
		 * Creates a customized textual progress bar representation.
		 * 
		 * @param progress The current progress value
		 * @param total The total value for completion
		 * @param barLength The length of the bar in characters
		 * @param barColor The color code for the unfilled portion of the bar
		 * @param fillColor The color code for the filled portion of the bar
		 * @param barStyle The character to use for the unfilled portion of the bar
		 * @param fillStyle The character to use for the filled portion of the bar
		 * @return A String representation of the customized progress bar
		 */
    	public static String createBar(double progress, double total, int barLength, String barColor, String fillColor, char barStyle, char fillStyle) {
    		return TextFeatures.CreateBar(progress, total, barLength, barColor, fillColor, barStyle, fillStyle);
    	}
    }
}