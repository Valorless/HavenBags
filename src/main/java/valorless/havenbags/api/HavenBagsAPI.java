package valorless.havenbags.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.NotNull;
import valorless.havenbags.annotations.Nullable;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.datamodels.EtherealBagSettings;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.features.BagHealth;
import valorless.havenbags.features.CustomBags;
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
		Main.closeBags();
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
	public static boolean isBag(@NotNull ItemStack item) {
		return HavenBags.isBag(item);
	}
	
	/**
	 * Gets the UUID stored on a bag item.
	 * @param item bag item
	 * @return bag UUID string, or null if not a bag
	 */
	public static String getBagUUID(@NotNull ItemStack item) {
		return HavenBags.getBagUUID(item);
	}
	
	/**
	 * Resolves the live Data object for the given bag item.
	 * @param item bag item
	 * @return Data for the bag, or null if not a bag
	 */
	public static Bag getBagData(@NotNull ItemStack item) {
		if(!isBag(item)) return null;
		return Database.getBag(getBagUUID(item));
	}
	
	/**
	 * Generates a bag item from the provided Data.
	 * @param bagData Data for the bag
	 * @return ItemStack representing the bag
	 */
	public static ItemStack generateBagItem(@NotNull Bag bagData) {
		return BagItemFactory.toItemStack(bagData);
	}
	
	/**
	 * Checks whether a bag exists in storage.
	 * @param uuid bag UUID
	 * @return true if the bag exists
	 */
	public static boolean bagExists(@NotNull String uuid) {
		return Database.bagExists(uuid);
	}
	
	/**
	 * Fetches Data for a bag by UUID.
	 * @param uuid bag UUID
	 * @return Data for the bag or null if not found
	 */
	public static Bag getBag(@NotNull String uuid) {
		return Database.getBag(uuid);
	}
	
	/**
	 * Gets the owner UUID of a bag.
	 * @param uuid bag UUID
	 * @return owner UUID string
	 */
	public static String getBagOwner(@NotNull String uuid) {
		return Database.getBag(uuid).getOwner();
	}
	
	/**
	 * Lists all bags owned by a player.
	 * @param playerUUID player UUID
	 * @return list of Data for the player's bags
	 */
	public static List<Bag> getPlayerBags(@NotNull String playerUUID) {
		return Database.getBagsData(playerUUID);
	}
	
	/**
	 * Lists all bag Data instances present in a player's inventory.
	 * @param player player to inspect
	 * @return list of Data for bags found
	 */
	public static List<Bag> getBagsOnPlayer(@NotNull Player player){
		List<Bag> bags = new ArrayList<>();
		for(Database.BagSimple bag : HavenBags.getBagsDataInInventory(player)) {
			bags.add(Database.getBag(getBagUUID(bag.item)));
		}
		return bags;
	}
	
	/**
	 * Checks if a bag is currently open by any viewer.
	 * @param uuid bag UUID
	 * @return true if open
	 */
	public static boolean isBagOpen(@NotNull String uuid) {
		return Database.isBagOpen(uuid, null);
	}
	
	/**
	 * Closes the bag with the specified UUID if it is currently open.
	 * @param uuid bag UUID
	 * @return true if the bag was open and is now closed, false if it was not open
	 */
	public static boolean closeBag(@NotNull String uuid) {
		if(isBagOpen(uuid)) {
			 return Database.getBag(uuid).getGui().close(true);
		}
		else return false;
	}
	
	/**
	 * Gets the player who has the bag open.
	 * @param uuid bag UUID
	 * @return Player who opened the bag, or null if not open
	 */
	public static Player bagOpenBy(@NotNull String uuid) {
		return Database.bagOpenBy(uuid, null);
	}
	
	/**
	 * Returns Data objects for all currently open bags.
	 * @return list of open bag Data
	 */
	public static List<Bag> getOpenBagsUUIDs() {
		return Database.getOpenBags();
	}
	
	/**
	 * Returns the active database type used by HavenBags.
	 * @return database type as string. i.e, "FILES", "SQLITE", etc.
	 */
	public static String getDatabaseType() {
		return Database.getDatabaseType().toString();
	}
	
	/**
	 * Creates and persists a new bag from the provided creation descriptor.
	 * @param creationObject bag creation parameters
	 * @return Data for the created bag
	 */
	public static Bag createBag(@NotNull BagCreationObject creationObject) {
		return Database.createBag(creationObject.uuid, creationObject.owner, creationObject.contents,
				creationObject.creator.equalsIgnoreCase("null") ? null : Bukkit.getOfflinePlayer(UUID.fromString(creationObject.creator)).getPlayer(), 
				createUnusedBagItem(creationObject.contents.size(), !creationObject.owner.equalsIgnoreCase("ownerless")));
	}
	
	/**
	 * Creates and persists a new bag from the provided Data object.
	 * @param bagData bag Data
	 * @return Data for the created bag
	 */
	public static Bag createBag(@NotNull Bag bagData) {
		return Database.createBag(bagData);
	}

	/**
	 * Creates a new bag item with the specified properties.
	 * - If binding is true, the bag is considered bound to a player; otherwise it's ownerless.
	 * - The size determines the slots and may affect the texture/model used.
	 * - Player parameter is used for placeholder parsing in lore and may influence texture/model selection if configured.
	 *
	 * @param binding Whether the bag should be bound to a player
	 * @param size The size/slots of the bag (e.g., 9, 18, 27, etc.)
	 * @param player Optional player context for placeholder parsing placeholders.
	 * @return A new ItemStack representing the bag with the specified properties
	 */
	public static ItemStack createBagItem(boolean binding, int size, @Nullable Player player){
		return BagItemFactory.createBagItem(binding, size, player);
	}

	/**
	 * Creates a custom bag item for the provided key.
	 * Custom bags are defined in {@code custom-bags.yml}
	 * <p>
	 * The {@code player} argument is only used when {@link #customBagRequiresPlayer(String)}
	 * returns {@code true} for the provided key. If it returns {@code false}, the
	 * player value is not used for predefined content initialization.
	 *
	 * @param key custom bag key
	 * @param player player context; only required/used when
	 *               {@link #customBagRequiresPlayer(String)} is {@code true}
	 * @return the created and configured bag {@link ItemStack}
	 * @throws NullPointerException if a player context is required but {@code player} is {@code null}
	 * @throws IllegalArgumentException if the provided key does not exist in the custom bags configuration
	 */
	@NotNull
	public static ItemStack createCustomBagItem(@NotNull String key, @Nullable Player player){
		return CustomBags.get(key, player);
	}

	/**
	 * Checks if a custom bag key requires a player context when created via
	 * {@link #createCustomBagItem(String, Player)}.
	 * <p>
	 * Returns {@code true} when the bag has predefined/custom content and needs a
	 * player to initialize owner-bound data.
	 *
	 * @param key custom bag key
	 * @return {@code true} if a player is required for this bag key
	 */
	public static boolean customBagRequiresPlayer(@NotNull String key){
		return CustomBags.requiresPlayer(key);
	}

	/**
	 * Lists all available custom bag keys.
	 * @return list of custom bag keys
	 */
	public static List<String> listCustomBags(){
		return CustomBags.list();
	}
	
	/**
	 * Deletes a bag by UUID.
	 * @param uuid bag UUID
	 * @return true if deletion succeeded
	 */
	public static boolean deleteBag(@NotNull String uuid) {
		return Database.deleteBag(uuid);
	}
	
	/**
	 * Creates a skin token ItemStack for the given value and type.
	 * @param value token payload (e.g., texture or model key)
	 * @param type token type
	 * @return token item
	 */
	public static ItemStack createToken(@NotNull String value, @NotNull TokenType type) {
		return HavenBags.createSkinToken(value, type);
	}
	
	/**
	 * Creates an effect token for the given effect identifier.
	 * @param value effect id
	 * @return token item
	 */
	public static ItemStack createEffectToken(@NotNull String value) {
		return HavenBags.createEffectToken(value);
	}
	
	/**
	 * Checks whether the given item is a valid skin token.
	 * @param item item to test
	 * @return true if the item is a skin token
	 */
	public static boolean isToken(@NotNull ItemStack item) {
		return HavenBags.isSkinToken(item);
	}
	
	/**
	 * Creates an ethereal bag entry for a player.
	 * @param bagId bag identifier
	 * @param player target player
	 * @param size number of slots for the bag, in rows
	 * @return true on success
	 * @throws IllegalArgumentException if the size is not between 1 and 6 (inclusive)
	 */
	public static boolean createEtherealBag(@NotNull String bagId, @NotNull Player player, int size) {
		if(size <= 0 || size > 6) throw new IllegalArgumentException("Size must be between 1 and 6 (inclusive)");
		return EtherealBags.addBag(player.getUniqueId(), bagId, size);
	}
	
	/**
	 * Removes an ethereal bag entry from a player.
	 * @param bagId bag identifier
	 * @param player target player
	 * @return true on success
	 */
	public static boolean removeEtherealBag(@NotNull String bagId, @NotNull Player player) {
		return EtherealBags.removeBag(player.getUniqueId(), bagId);
	}
	
	/**
	 * Gets the contents of an ethereal bag for a player.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @return contents as a list of ItemStacks, or null if not found
	 */
	public static List<ItemStack> getEtherealBags(@NotNull String bagId, @NotNull Player player) {
		return EtherealBags.getBagContentsOrNull(player.getUniqueId(), bagId);
	}
	
	/**
	 * Checks if the player has the specified ethereal bag.
	 * @param bagId bag identifier
	 * @param player player to check
	 * @return true if present
	 */
	public static boolean hasEtherialBag(@NotNull String bagId, @NotNull Player player) {
		return EtherealBags.hasBag(player.getUniqueId(), bagId);
	}
	
	/**
	 * Checks if the player has any ethereal bags.
	 * @param bagId ignored identifier
	 * @param player player to check
	 * @return true if the player has one or more ethereal bags
	 */
	public static boolean hasEtherialBags(@NotNull Player player) {
		return EtherealBags.hasBags(player.getUniqueId());
	}
	
	/**
	 * Checks if the specified ethereal bag is currently open.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @return true if open
	 */
	public static boolean isEtherealBagOpen(@NotNull String bagId, @NotNull Player player) {
		return EtherealBags.isOpen(player, bagId);
	}
	
	/**
	 * Replaces the contents of an ethereal bag for a player.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @param contents new contents list
	 * @return true on success
	 */
	public static boolean setEtherealBagContents(@NotNull String bagId, @NotNull Player player, @NotNull List<ItemStack> contents) {
		return EtherealBags.updateBagContents(player.getUniqueId(), bagId, contents);
	}
	
	/**
	 * Retrieves settings for a player's ethereal bag.
	 * @param bagId bag identifier
	 * @param player player owning the bag
	 * @return the bag's settings, or null if absent
	 */
	public static EtherealBagSettings getEtherealBagSettings(@NotNull String bagId, @NotNull Player player) {
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
	public static ItemStack upgradeBag(@NotNull ItemStack bag) {
		if(PDC.has(bag, "upgrade") && PDC.getBoolean(bag, "upgrade") == false) {
			return bag;
		}
		int size = PDC.getInteger(bag, "size");
		if(size == 54) return bag;
		int newSize = size+9;
		ItemStack clonedBag = bag.clone();
		String owner = PDC.getString(bag, "owner");
		
		PDC.setinteger(clonedBag, "size", newSize);
		if(Main.weight.getBool("weight-per-size")) {
			PDC.setDouble(clonedBag, "weight-limit", Main.weight.getDouble(String.format("weight-size-%s", newSize)));
		}
		HavenBags.updateBagLore(clonedBag, null, true);
		if(Main.config.getBool("bag-textures.enabled") && !Main.config.getBool("upgrades.keep-texture")) {
			if(owner.equalsIgnoreCase("ownerless")) {
				HeadCreator.setTextureValue(clonedBag, Main.config.getString(String.format("bag-textures.size-ownerless-%s", newSize)));
			}else {
				HeadCreator.setTextureValue(clonedBag, Main.config.getString(String.format("bag-textures.size-%s", newSize)));
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
	public static void setTexture(@NotNull ItemStack bag, @NotNull String base64Texture) {
		if(Utils.IsStringNullOrEmpty(base64Texture) || !Base64Validator.isValidBase64(base64Texture)) {
			throw new IllegalArgumentException("Provided texture is not valid Base64!");
		}
		HeadCreator.setTextureValue(bag, base64Texture);
	}
	
	/**
	 * Retrieves the custom texture Base64 string from the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to inspect.
	 * @return A Base64-encoded string representing the bag's texture.
	 */
	public static String getTexture(@NotNull ItemStack bag) {
		return HeadCreator.getTextureValue(bag);
	}
	
	/**
	 * Refreshes the lore of the specified bag item to reflect its current state.
	 * 
	 * @param bag The ItemStack representing the bag to refresh.
	 */
	public static void refreshBagLore(@NotNull ItemStack bag) {
		HavenBags.updateBagLore(bag, null);
	}
	
	/**
	 * Refreshes the lore of the specified bag item to reflect its current state.
	 * 
	 * @param bag The ItemStack representing the bag to refresh.
	 * @param player The player used in placeholder parsing.
	 */
	public static void refreshBagLore(@NotNull ItemStack bag, @NotNull Player player) {
		HavenBags.updateBagLore(bag, player);
	}
	
	/**
	 * Calculates the total weight of items contained within the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to evaluate.
	 * @return The total weight of the bag's contents.
	 */
	public static double getBagWeight(@NotNull ItemStack bag) {
		return HavenBags.getWeight(bag);
	}
	
	/**
	 * Calculates the total weight of a list of ItemStacks, typically representing bag contents.
	 * 
	 * @param bagContents A list of ItemStacks to evaluate.
	 * @return The total weight of the provided items.
	 */
	public static double getBagWeight(@NotNull List<ItemStack> bagContents) {
		return HavenBags.getWeight(bagContents);
	}
	
	/**
	 * Retrieves the weight of a single ItemStack.
	 * 
	 * @param item The ItemStack to evaluate.
	 * @return The weight of the specified item.
	 */
	public static double getItemWeight(@NotNull ItemStack item) {
		return HavenBags.itemWeight(item);
	}
	
	/**
	 * Checks if the specified item can be carried in the given bag, considering weight limits.
	 * 
	 * @param item The ItemStack to check.
	 * @param bag The ItemStack representing the bag.
	 * @return true if bag cannot hold more weight, false otherwise.
	 */
	public static boolean canCarry(@NotNull ItemStack item, @NotNull ItemStack bag) {
		return HavenBags.canCarry(item, bag);
	}
	
	/**
	 * Checks if the specified item is blacklisted from being stored in HavenBags.
	 * <p>
	 * Takes into account if using blacklist or whitelist mode.<br>
	 * Please check the {@link #blacklistAsWhitelist(Bag bagData)} before calling this method to ensure correct context.<br>
	 * For global context refer to {@link #blacklistAsWhitelist()}.
	 * 
	 * @param item The ItemStack to check.
	 * @param bagData (Optional) The Data object of the bag for context-specific checks; can be null.
	 * @return true if the item is blacklisted, false otherwise.
	 */
	public static boolean isItemBlacklisted(@NotNull ItemStack item, @Nullable Bag bagData) {
		return HavenBags.isItemBlacklisted(item, bagData);
	}
	
	/**
	 * Checks if the global configuration is set to treat the blacklist as a whitelist.
	 * 
	 * @return true if blacklist is used as whitelist, false otherwise.
	 */
	public static boolean blacklistAsWhitelist() {
		return Main.config.getBool("blacklist-as-whitelist");
	}
	
	/**
	 * Checks if the specified bag's configuration is set to treat its blacklist as a whitelist.
	 * 
	 * @param bagData The Data object of the bag to check.
	 * @return true if blacklist is used as whitelist for this bag, false otherwise.
	 */
	public static boolean blacklistAsWhitelist(@NotNull Bag bagData) {
		return bagData.isWhitelist();
	}
	
	/**
	 * Checks if the player can carry more bags based on configured limits.
	 * 
	 * @param player The player to check.
	 * @return true if the player can carry more bags, false otherwise.
	 */
	public static boolean canCarryMoreBags(@NotNull Player player) {
		return HavenBags.canCarryMoreBags(player);
	}
	
	/**
	 * Checks if the specified bag is full (no empty slots).
	 * 
	 * @param bag The ItemStack representing the bag to check.
	 * @return true if the bag is full, false otherwise.
	 */
	public static boolean isBagFull(@NotNull ItemStack bag) {
		return HavenBags.isBagFull(bag);
	}
	
	/**
	 * Checks if the bag with the specified UUID is full (no empty slots).
	 * 
	 * @param uuid The UUID string of the bag to check.
	 * @return true if the bag is full, false otherwise.
	 */
	public static boolean isBagFull(@NotNull UUID uuid) {
		return HavenBags.isBagFull(uuid);
	}
	
	/**
	 * Checks if the bag with the specified UUID is full (no empty slots).
	 * 
	 * @param uuid The UUID string of the bag to check.
	 * @return true if the bag is full, false otherwise.
	 */
	public static boolean isBagFull(@NotNull String uuid) {
		return HavenBags.isBagFull(uuid);
	}
	
	/**
	 * Counts the number of empty slots in the specified bag item.
	 * 
	 * @param bag The ItemStack representing the bag to check.
	 * @return The number of empty slots in the bag.
	 */
	public static int bagSlotsEmpty(@NotNull ItemStack bag) {
		return HavenBags.slotsEmpty(bag);
	}
	
	/**
	 * Calculates how full the specified bag is.
	 * 
	 * @param bag The ItemStack representing the bag to evaluate.
	 * @return The percentage of used capacity in the bag (0.0 to 100.0).
	 */
	public static double usedCapacity(@NotNull ItemStack bag) {
		return HavenBags.usedCapacity(bag, getBagData(bag).getContent());
	}
    
	/** Calculates the total number of bag slots in a player's inventory.
	 * 
	 * @param player The player whose inventory to check.
	 * @return The total number of bag slots.
	 */
    public static int getBagSlotsInInventory(@NotNull Player player) {
		return HavenBags.getBagSlotsInInventory(player);
    }
    
    /** Checks if the player has any bags belonging to other players.
	 * 
	 * @param player The player to check.
	 * @return true if the player has bags owned by others, false otherwise.
	 */
    public static boolean hasOthersBag(@NotNull Player player) {
    	return HavenBags.hasOthersBag(player);
    }
    
    /** Determines the appropriate texture for a bag based on its used capacity.
     *  (e.g., changes texture as the bag fills up)
     * 
     * @param bag The ItemStack representing the bag to evaluate.
     * @return A Base64-encoded string representing the texture for the bag's current capacity.<br>
     * If no special texture is defined in the configuration, returns the bag's current texture.
     */
    public static String capacityTexture(@NotNull ItemStack bag) {
		return HavenBags.capacityTexture(bag, getBagData(bag).getContent());
	}
    
    /** Retrieves the current state of the bag.
	 * 
	 * @param item The ItemStack representing the bag to evaluate.
	 * @return "NEW" if the bag is empty, "USED" otherwise.<br>
	 * May return "NULL" if the item is not a valid bag.
	 */
    public static String bagState(@NotNull ItemStack item) {
		return BagState.getState(item).toString().toUpperCase();
	}
    
    /** Extracts the URL from a Base64-encoded texture string.
     * 
     * @param base64 The Base64-encoded texture string.
     * @return The extracted URL.
     */
    public static String extractUrlFromBase64(@NotNull String base64) {
		return HeadCreator.extractUrlFromBase64(base64);
	}
    
    /** Converts a texture URL to a Base64-encoded string.
	 * 
	 * @param url The texture URL.
	 * @return The Base64-encoded texture string.
	 */
    public static String convertUrlToBase64(@NotNull String url) {
		return HeadCreator.convertUrlToBase64(url);
	}
    
    /** Creates a player head ItemStack with a custom texture from a Base64 string.
     * 
     * @param base64 The Base64-encoded texture string.
     * @return The custom player head ItemStack.
     */
    public static ItemStack creteHeadFromBase64(@NotNull String base64) {
    	return HeadCreator.itemFromBase64(base64);
    }
    
    /** Creates an unused bag ItemStack with specified size and binding status.
     * 
     * @param size The size of the bag (number of slots, to the power of 9).
     * @param binding Whether the bag should be bound to an owner upon creation.
     * @return The created unused bag ItemStack.
     */
    @SuppressWarnings("deprecation")
	public static ItemStack createUnusedBagItem(@NotNull int size, @NotNull boolean binding) {
		if(size <= 0 || size > 54) throw new IllegalArgumentException("Size must be between 1 - 54 (inclusive)");
		Config config = valorless.havenbags.Main.config;
		String bagTexture = config.getString("bag.texture");
		ItemStack bagItem = new ItemStack(Material.AIR);

		if(config.getString("bag.type").equalsIgnoreCase("HEAD")){
			if(config.getBool("bag-textures.enabled")) {
				for(int s = 9; s <= 54; s += 9) {
					if(size == s) {
						bagItem = HeadCreator.itemFromBase64(config.getString("bag-textures.size-" + size));
					}
				}
			}else {
				bagItem = HeadCreator.itemFromBase64(bagTexture);
			}
		} else if(config.getString("bag.type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(config.getMaterial("bag.material"));
		}
		
		ItemMeta bagMeta = bagItem.getItemMeta();
		if(config.getInt("bag.modeldata") != 0 && config.getString("bag.type").equalsIgnoreCase("ITEM")) {
			bagMeta.setCustomModelData(config.getInt("bag.modeldata"));
			if(config.getBool("bag-custom-model-datas.enabled")) {
				for(int s = 9; s <= 54; s += 9) {
					if(size == s) {
						bagMeta.setCustomModelData(config.getInt("bag-custom-model-datas.size-" + size));
					}
				}
			}
		}

		bagMeta.setDisplayName(Lang.get("bag-unbound-name"));
		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.getStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, null));
		}

		lore.add(Lang.get("bag-size").replace("%size%", "" + size));
		
		bagMeta.setLore(lore);
		bagItem.setItemMeta(bagMeta);

		if(Server.VersionHigherOrEqualTo(Version.v1_21_5)) {
			ItemUtils.SetMaxStackSize(bagItem, 1);
		}

		if(config.getBool("bag-custom-model-datas.enabled")) {
			for(int s = 9; s <= 54; s += 9) {
				if(size == s) {
					if(!Utils.IsStringNullOrEmpty(config.getString("bag-custom-model-datas.size-" + size)) &&
							!config.getString("bag-custom-model-datas.size-" + size).matches("-?\\d+(\\.\\d+)?")) {
						ItemUtils.SetItemModel(bagItem, config.getString("bag-custom-model-datas.size-" + size));
					}
				}
			}
		}

		if(!Utils.IsStringNullOrEmpty(config.getString("bag.itemmodel"))) {
			ItemUtils.SetItemModel(bagItem, config.getString("bag.itemmodel"));
		}

		PDC.setString(bagItem, "uuid", "null");
		PDC.setString(bagItem, "owner", "null");
		PDC.setinteger(bagItem, "size", size);
		PDC.setBoolean(bagItem, "binding", binding);
		
		return bagItem;
	}
    
    /**
	 * Checks if the bag is empty.
	 * 
	 * @param uuid The item of the bag to check.
	 * @return true if the bag is empty, false otherwise.
	 */
	public static boolean isBagEmpty(@NotNull ItemStack bag) {
		return HavenBags.isBagEmpty(bag);
	}
	
	/**
	 * Checks if the bag with the specified UUID is empty.
	 * 
	 * @param uuid The UUID of the bag to check.
	 * @return true if the bag is empty, false otherwise.
	 */
	public static boolean isBagEmpty(@NotNull UUID uuid) {
		return HavenBags.isBagEmpty(uuid);
	}
	
	/**
	 * Checks if the bag with the specified UUID is empty.
	 * 
	 * @param uuid The UUID string of the bag to check.
	 * @return true if the bag is empty, false otherwise.
	 */
	public static boolean isBagEmpty(@NotNull String uuid) {
		return HavenBags.isBagEmpty(uuid);
	}

	/**
	 * Checks whether the bag health (durability) feature is enabled in the configuration.
	 * 
	 * @return true if bag health is enabled, false otherwise
	 */
	public static boolean isBagHealthEnabled() {
		return BagHealth.isEnabled();
	}

	/**
	 * Gets the current stored damage value for a bag item.
	 * <p>
	 * Damage is stored in the bag's persistent data and is used to derive remaining health.
	 * 
	 * @param bag bag ItemStack
	 * @return current damage value, or 0 if not set
	 */
	public static int getBagDamage(@NotNull ItemStack bag) {
		return BagHealth.getDamage(bag);
	}

	/**
	 * Sets the stored damage value for a bag item.
	 * 
	 * @param bag bag ItemStack
	 * @param damage new damage value to store
	 */
	public static void setBagDamage(@NotNull ItemStack bag, int damage) {
		BagHealth.setDamage(bag, damage);
	}

	/**
	 * Adds damage to a bag item.
	 * 
	 * @param bag bag ItemStack
	 * @param damageToAdd amount of damage to add
	 */
	public static void addBagDamage(@NotNull ItemStack bag, int damageToAdd) {
		BagHealth.addDamage(bag, damageToAdd);
	}

	/**
	 * Gets the current remaining health for a bag item.
	 * 
	 * @param bag bag ItemStack
	 * @return current remaining health
	 */
	public static int getBagCurrentHealth(@NotNull ItemStack bag) {
		return BagHealth.getCurrentHealth(bag);
	}

	/**
	 * Gets the maximum health for a bag item based on its configured size tier.
	 * 
	 * @param bag bag ItemStack
	 * @return maximum health for the bag
	 */
	public static int getBagMaxHealth(@NotNull ItemStack bag) {
		return BagHealth.getMaxHealth(bag);
	}
	
	/**
	 * Checks the state of the bag (e.g., "NEW", "USED", "NULL") based on its contents and configuration.
	 * The state is determined by factors such as whether the has been used before, or if the item is not a valid bag.
	 * 
	 * @param bag ItemStack to evaluate
	 * @return the BagState enum value representing the bag's state, or BagState.NULL if the item is not a valid bag
	 */
	public static BagState getBagState(@NotNull ItemStack bag) {
		return BagState.getState(bag);
	}
    
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
    	public static Inventory createPage(@NotNull Player player, @NotNull String title, int page, @NotNull List<ItemStack> items, int rows) {
    		return valorless.havenbags.utils.GUI.createPage(player, title, page, items, rows);
    	}
    	
    	/**
		 * Retrieves the action associated with a pagination button.
		 * 
		 * @param button The ItemStack representing the pagination button.
		 * @return The action string stored in the button's PDC, or null if not found.
		 */
    	public static String getPageActionKey(@NotNull ItemStack button) {
    		try {
    			return PDC.getString(button, "bag-action");
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
			return TextFeatures.createBar(progress, total, barLength);
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
    		return TextFeatures.createBar(progress, total, barLength, barColor, fillColor, barStyle, fillStyle);
    	}
    }
}
