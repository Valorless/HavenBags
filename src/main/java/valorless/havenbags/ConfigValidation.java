package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;

import valorless.havenbags.features.AutoPickup;
import valorless.valorlessutils.ValorlessUtils.Log;

public class ConfigValidation {

	/**
	 * Validates and initializes various components of the application.<br>
	 * This method sequentially calls:<br>
	 * - {@link #Config()} to configure the system settings.<br>
	 * - {@link #Lang()} to handle language or localization setup.<br>
	 * - {@link #Weight()} to set up or validate weight configurations.<br>
	 * - {@link #Blacklist()} to process or validate the blacklist.<br>
	 * - {@link #Filters()} to process or validate the filters.<br>
	 * - {@link #Plugins()} to initialize or validate plugins.
	 */
	public static void Validate() {
		Config();
		Lang();
		Weight();
		Blacklist();
		Filters();
		Plugins();
	}
	
	private static void Config() {
	// Core
		Main.config.AddValidationEntry("config-version", 5, List.of("DO NOT EDIT - Used for config updates"));
	Main.config.AddValidationEntry("debug", false, List.of("Additional messages used for debugging."));
	Main.config.AddValidationEntry("check-updates", true, List.of("# Should we check for plugin updates?"));
	Main.config.AddValidationEntry("save-type", "sqlite", List.of(
			"How should HavenBags store the bag data?",
			"'files' - Store each bag as individual files.",
			"'sqlite' - Store all data in a database file.",
			"'mysql' -  Store all data in an external database.",
			"'mysqlplus' -  Store all bags on an external database, loading/saving directly to the database. Can be used to use bags across servers."));
	
	// Database
	Main.config.AddValidationEntry("mysql.host", "localhost", List.of("MySQL database settings."));
	Main.config.AddValidationEntry("mysql.port", 3306);
	Main.config.AddValidationEntry("mysql.name", "minecraft", List.of("Database name."));
	Main.config.AddValidationEntry("mysql.user", "admin", List.of(""));
	Main.config.AddValidationEntry("mysql.password", "pass", List.of(""));
	Main.config.AddValidationEntry("mysql.connect_timeout", 30, List.of("Connection timeout in seconds."));
	Main.config.AddValidationEntry("mysql.socket_timeout", 60, List.of("Socket timeout in seconds."));
	Main.config.AddValidationEntry("mysql.max_chunk_size", 200, List.of("Maximum number of bags to load/save in a single database chunk.",
			"Adjust based on your server's performance and memory."));
	
	// Auto-save
	Main.config.AddValidationEntry("auto-save.interval", 1200, List.of(
			"How often should we save bags onto the server?",
			"In seconds.",
			"Default: 1200 = 20min"));
	Main.config.AddValidationEntry("auto-save.message", true, List.of("Should HavenBags write in the console when saving?"));
	
	// Language
	Main.config.AddValidationEntry("language", "en_us", List.of(
			"Language used when translating material and item names.",
			"# Supported languages:",
			"English (US) --------- en_us",
			"English -------------- en_gb",
			"English (Pirate) ----- en_pt",
			"Danish --------------- da_dk",
			"German --------------- de_de",
			"Spanish -------------- es_es",
			"French --------------- fr_fr",
			"Turkish -------------- tr_tr",
			"Dutch ---------------- nl_nl",
			"Japenese ------------- ja_jp",
			"Korean --------------- ko_kr",
			"Chinese (Simplified) - zh_cn",
			"Russian -------------- ru_ru",
			"Polish --------------- pl_pl",
			"Portuguese ----------- pt_pt",
			"Portuguese (Brazil) -- pt_br"));
	
	// Bag base config (nested keys)
	Main.config.AddValidationEntry("bag.type", "HEAD", List.of(
			"Should the bags use heads or items?",
			"HEAD - use Player Heads.",
			"ITEM - use regular items."));
	Main.config.AddValidationEntry("bag.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=", 
			List.of("Texture of the bag if the bag-type is 'HEAD'. Must be Base64 format.",
					"You can get textures from https://minecraft-heads.com/",
					"The texture by default: https://minecraft-heads.com/custom-heads/decoration/1906-bag",
					"At the bottom of the page there's a section called \"For Developers\".",
					"Inside \"For Developers\" is a value based off of a large amount of random letters, numbers and symbols. That is the Base64 texture."));
	Main.config.AddValidationEntry("bag.material", "ENDER_CHEST", List.of("Item material if the bag-type is 'ITEM'"));
	Main.config.AddValidationEntry("bag.modeldata", 0, List.of("Custom Model Data if the bag-type is 'ITEM'"));
	Main.config.AddValidationEntry("bag.itemmodel", "", List.of("Item Model if the bag-type is 'ITEM'"));
	Main.config.AddValidationEntry("bag.tooltip-style", "", List.of("Tooltip Style"));
	
	// Per-size model data and textures
	Main.config.AddValidationEntry("bag-custom-model-datas.enabled", false, List.of(
			"Want CustomModelData per size instead?",
			"You can fill these with the same method as above.",
			"Existing bags are not updated.",
			"Accepts item models too."));
	Main.config.AddValidationEntry("bag-custom-model-datas.size-9", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-18", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-27", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-36", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-45", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-54", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-ownerless-9", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-ownerless-18", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-ownerless-27", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-ownerless-36", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-ownerless-45", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.size-ownerless-54", 0);
	
	Main.config.AddValidationEntry("bag-textures.enabled", false, List.of(
			"Want textures per size instead?",
			"You can fill these with the same method as above.",
			"Existing bags are not updated."));
	Main.config.AddValidationEntry("bag-textures.size-9", "");
	Main.config.AddValidationEntry("bag-textures.size-18", "");
	Main.config.AddValidationEntry("bag-textures.size-27", "");
	Main.config.AddValidationEntry("bag-textures.size-36", "");
	Main.config.AddValidationEntry("bag-textures.size-45", "");
	Main.config.AddValidationEntry("bag-textures.size-54", "");
	Main.config.AddValidationEntry("bag-textures.size-ownerless-9", "");
	Main.config.AddValidationEntry("bag-textures.size-ownerless-18", "");
	Main.config.AddValidationEntry("bag-textures.size-ownerless-27", "");
	Main.config.AddValidationEntry("bag-textures.size-ownerless-36", "");
	Main.config.AddValidationEntry("bag-textures.size-ownerless-45", "");
	Main.config.AddValidationEntry("bag-textures.size-ownerless-54", "");
	
	Main.config.AddValidationEntry("capacity-based-textures.enabled", false, List.of(
			"Should we change the bag's texture regarless of other settings,",
			"based on how full the bag is?"));
	Main.config.AddValidationEntry("capacity-based-textures.textures.0", "", List.of(
			"Defined capacities and their defined textures.",
			"If a bag is over the capacity defined, they get the texture."));
	
	// Sound (nested)
	Main.config.AddValidationEntry("sound.open.key", "ITEM_BUNDLE_INSERT", List.of(
			"Sound played when opening a bag.",
			"Sounds: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html"));
	Main.config.AddValidationEntry("sound.open.volume", 1.0);
	Main.config.AddValidationEntry("sound.open.pitch", 1.0);
	Main.config.AddValidationEntry("sound.close.key", "ITEM_BUNDLE_DROP_CONTENTS", List.of("Sound played when closing a bag."));
	Main.config.AddValidationEntry("sound.close.volume", 1.0);
	Main.config.AddValidationEntry("sound.close.pitch", 1.0);
	Main.config.AddValidationEntry("sound.inventory-full.key", "ENTITY_VILLAGER_NO", List.of("Sound played when a bag is dropped due to the inventory being full."));
	Main.config.AddValidationEntry("sound.inventory-full.volume", 1.0);
	Main.config.AddValidationEntry("sound.inventory-full.pitch", 1.0);
	
	// Limits and protection
	Main.config.AddValidationEntry("max-bags", 9, List.of(
			"The max amount of bags a player can create.",
			"Note that is they lose a bag, they'll need it restored, ",
			"or their max-bags would essentially be 8. (if 9)",
			"(Ownerless bags do not count, set 0 for unlimited)"));
	Main.config.AddValidationEntry("carry-limit", 3, List.of(
			"How many bags the player can carry at a time.",
			"set 0 for unlimited."));
	Main.config.AddValidationEntry("hardcore-bags", false, List.of("Should bags' data get deleted if the bag item is destroyed?"));
	Main.config.AddValidationEntry("protect-bags", true, List.of("Should bags be protected from being destroyed by things such as Fire, Lava, Cactus, and other things?"));
	Main.config.AddValidationEntry("protect-bags-players", false, List.of("Should dropped bags only be able to be picked up by their owner?"));
	Main.config.AddValidationEntry("bags-in-bags", false, List.of("Can bags be put inside other bags?"));
	Main.config.AddValidationEntry("bags-in-shulkers", true, List.of("Can bags be put inside shulkers?"));
	Main.config.AddValidationEntry("bags-in-bundles", false, List.of("Can bags be put inside bundles?"));
	Main.config.AddValidationEntry("inventory-lock.enabled", false, List.of(
			"Should bags always remain in the player's inventory?",
			"Good to use alongside Soulbound, otherwise death will drop the item."));
	Main.config.AddValidationEntry("inventory-lock.unbound", false);
	Main.config.AddValidationEntry("inventory-lock.bound", true);
	Main.config.AddValidationEntry("inventory-lock.unused", false);
	Main.config.AddValidationEntry("inventory-lock.used", true);
	Main.config.AddValidationEntry("soulbound", false, List.of(
			"Should bags be kept through death?",
			"May be incompatible with other plugins."));
	Main.config.AddValidationEntry("old-help-menu", false, List.of("Should /havenbags help, show commands without mouseover text, and the information below each command?"));
	
	// Auto-pickup (nested)
	Main.config.AddValidationEntry("auto-pickup.enabled", true, List.of("Can bags automatically pick up drops?"));
	Main.config.AddValidationEntry("auto-pickup.sound.key", "ENTITY_ITEM_PICKUP");
	Main.config.AddValidationEntry("auto-pickup.sound.volume", 0.8);
	Main.config.AddValidationEntry("auto-pickup.sound.pitch.min", 1.05);
	Main.config.AddValidationEntry("auto-pickup.sound.pitch.max", 1.25);
	Main.config.AddValidationEntry("auto-pickup.inventory.enabled", true, List.of("Should Auto-Pickup put items from the inventory into the bag as well?"));
	Main.config.AddValidationEntry("auto-pickup.inventory.events.onBlockBreak", true);
	Main.config.AddValidationEntry("auto-pickup.inventory.events.onItemPickup", true);
	
	// Trusting and quiver
	Main.config.AddValidationEntry("trusting", true, List.of("Can players trust other players with access to their bound bags?"));
	Main.config.AddValidationEntry("quiver-bags", true, List.of(
			"Should bags be able to used as quivers?",
			"Will prioritize offhand over inventory."));
	Main.config.AddValidationEntry("quiver-shield-fix", 2, List.of(
			"Holding a shield while using the quiver feature tends to bug out.",
			"Here are a few fixes you can choose:",
			"0 - No fix.",
			"1 - Semi fix, shield animation blocked and arrow gets drawn, but bow/crossbow isnt charged.",
			"2 - Quivers are disabled if holding a shield."));
	
	// Magnet (nested booleans and numbers)
	Main.config.AddValidationEntry("magnet.enabled", true, List.of(
			"Should players be able to magnify their bags?",
			"If enabled, nearby drops will move towards the player."));
	Main.config.AddValidationEntry("magnet.tick-rate", 2, List.of("How often the task should run."));
	Main.config.AddValidationEntry("magnet.range", 5.0);
	Main.config.AddValidationEntry("magnet.speed", 0.1);
	Main.config.AddValidationEntry("magnet.vertical", false, List.of("Should drops ignore gravity to move vertically towards the player?"));
	Main.config.AddValidationEntry("magnet.require-autopickup", false);
	Main.config.AddValidationEntry("magnet.only-autopickup-items", false);
	Main.config.AddValidationEntry("magnet.instant", false, List.of(
			"This will teleport the items to the player instantly,",
			"instead of moving them over time."));
	
	// Effects
	Main.config.AddValidationEntry("effects.refresh-rate", 100, List.of("How often the task should run. 100 ticks = 5s."));
	
	// Upgrades
	Main.config.AddValidationEntry("upgrades.enabled", false, List.of(
			"Bag Upgrades",
			"If enabled, remember to allow \"ANVIL\" in 'allowed-containers'.",
			"Can players upgrade bags in Anvils?"));
	Main.config.AddValidationEntry("upgrades.keep-texture", false, List.of(
			"Should bags keep their texture or upgrade to the next bag size?",
			"(Ignored if 'bag-textures' is disabled)"));
	Main.config.AddValidationEntry("upgrades.from-9-to-18", "EMERALD:5:90000", List.of(
			"Bag upgrades.",
			"<> = Optional",
			"MATERIAL:AMOUNT:<CUSTOMMODELDATA>"));
	Main.config.AddValidationEntry("upgrades.from-18-to-27", "DIAMOND:10:90001");
	Main.config.AddValidationEntry("upgrades.from-27-to-36", "NETHERITE_INGOT:1:90002");
	Main.config.AddValidationEntry("upgrades.from-36-to-45", "NETHERITE_BLOCK:1:90003");
	Main.config.AddValidationEntry("upgrades.from-45-to-54", "END_CRYSTAL:1");
	
	// Tokens (nested)
	Main.config.AddValidationEntry("token.skin.displayname", "&aSkin Token", List.of(
			"Token",
			"Used to create a generic token, which you can edit in-game.",
			"The rest of the data for the token is given when using the command.",
			"You can create skins in the 'textures.yml' file.",
			"You can create effects in the 'effects.yml' file.",
			"Placeholders work in names and lore."));
	Main.config.AddValidationEntry("token.skin.material", "PLAYER_HEAD", List.of("If material is PLAYER_HEAD, the head's texture will be the same as the skin."));
	Main.config.AddValidationEntry("token.skin.custommodeldata", 0, List.of("# 0 to ignore"));
	Main.config.AddValidationEntry("token.skin.itemmodel", "");
	Main.config.AddValidationEntry("token.skin.lore", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{ add("&7Combine with a bag in a fletching table to apply."); add("&7Skin: &e%skin%"); }
	}, List.of(
			"Placeholders:",
			"%skin% - Name of the skin found in textures.yml. ",
			"You can add extra lore in-game, with things such as which skin it is."));
	Main.config.AddValidationEntry("token.effect.displayname", "&eEffect Token");
	Main.config.AddValidationEntry("token.effect.material", "PLAYER_HEAD");
	Main.config.AddValidationEntry("token.effect.texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmMzNGQxMmY3YWM5MzliMTE1MWQxMjE0NmQwMjM5ZWYwMTg4ZTQwM2VlMTk2NmQzZGIxOTllNjY0ZmYzODI4MyJ9fX0=", 
			List.of("base64 texture for PLAYER_HEAD material, can also be a value from textures.yml."));
	Main.config.AddValidationEntry("token.effect.custommodeldata", 0, List.of("0 to ignore"));
	Main.config.AddValidationEntry("token.effect.itemmodel", "");
	Main.config.AddValidationEntry("token.effect.lore", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{ add("&7Combine with a bag in a fletching table to apply."); add("&7Effect: &e%effect%"); }
	}, List.of(
			"Placeholders:",
			"%effect% - Name of the effect found in effects.yml. ",
			"You can add extra lore in-game, with things such as effect effects."));
	
	// Anvil upgrades and GUI
	Main.config.AddValidationEntry("bag-upgrades-anvil", false, List.of("Should upgrades and skins be applied through an anvil?"));
	Main.config.AddValidationEntry("upgrade-gui.enabled", true, List.of("Custom GUI used to upgrade and skin bags."));
	Main.config.AddValidationEntry("upgrade-gui.block", "FLETCHING_TABLE");
	Main.config.AddValidationEntry("upgrade-gui.noteblock.instrument", "BASS_DRUM", List.of(
			"Requires \"block\" to be NOTE_BLOCK.",
			"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Instrument.html"));
	Main.config.AddValidationEntry("upgrade-gui.noteblock.note", 0);
	Main.config.AddValidationEntry("upgrade-gui.title", "&eBag Upgrade");
	Main.config.AddValidationEntry("upgrade-gui.gui-size", 27, List.of("Must be to the power of 9. i.e. Rows*9, 3*9 = 27"));
	Main.config.AddValidationEntry("upgrade-gui.slots.bag", 10, List.of("Bag and Token can be used interchangeable."));
	Main.config.AddValidationEntry("upgrade-gui.slots.token", 12);
	Main.config.AddValidationEntry("upgrade-gui.slots.result", 15, List.of("upgrade/skin result"));
	Main.config.AddValidationEntry("upgrade-gui.filler", "GRAY_STAINED_GLASS_PANE", List.of("Set to AIR to have no fillers, useful when using custom gui."));
	if(!Main.config.HasKey("upgrade-gui.custom-filler")) {
		Main.config.AddValidationEntry("upgrade-gui.custom-filler.0", "LIME_STAINED_GLASS_PANE", List.of("Each corner of the 27 slot gui."));
		Main.config.AddValidationEntry("upgrade-gui.custom-filler.8", "LIME_STAINED_GLASS_PANE");
		Main.config.AddValidationEntry("upgrade-gui.custom-filler.18", "LIME_STAINED_GLASS_PANE");
		Main.config.AddValidationEntry("upgrade-gui.custom-filler.26", "LIME_STAINED_GLASS_PANE");
	}
	Main.config.AddValidationEntry("upgrade-gui.success-sound", "ENTITY_VILLAGER_WORK_FLETCHER", List.of("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html"));
	
	// Blacklist worlds and allowed containers
	Main.config.AddValidationEntry("blacklist", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{ add("world_name"); add("world_name_nether"); add("another_world"); }
	}, List.of(
			"Which containers are the player allowed to interact with bags?",
			"Use this to prevent the player using bags in unintended ways, such as crafting.",
			"Full list of InventoryTypes: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryType.html",
			"Default: CRAFTING, HOPPER, PLAYER, CREATIVE"));
	Main.config.AddValidationEntry("allowed-containers", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{ add("CHEST"); add("ENDER_CHEST"); add("BARREL"); add("SHULKER_BOX"); add("MERCHANT"); add("ANVIL"); }
	}, List.of(
			"Specify if bags should not work in specific worlds.",
			"Worlds do not include _nether or _the_end.",
			"You have to manually add those."));
	
	// Player GUI
	Main.config.AddValidationEntry("player-gui.enabled", false, List.of("GUI where the players can restore or delete their own bags."));
	Main.config.AddValidationEntry("player-gui.self-restore", true, List.of("Can players restore their own bags?"));
	Main.config.AddValidationEntry("player-gui.self-delete", true, List.of("Can players delete their own bags?"));
	
	Main.config.AddValidationEntry("insurance.enabled", false, List.of(
			"Allow players to make \"Insurance Claims\" in order to",
			"restore/delete their bags from the PlayerGUI.",
			"Requires 'player-gui' to be enabled."));
	Main.config.AddValidationEntry("insurance.default-cost", 1000, List.of("The default claim cost."));
	Main.config.AddValidationEntry("insurance.type", "PERCENT", List.of(
			"Should the cost be increased by adding a static value or percentage?",
			"Valid types: ADD, PERCENT"));
	Main.config.AddValidationEntry("insurance.increment-value", 0.20, List.of(
			"Cost increase, by either a static value or percentage.",
			"2000 = Increase by 2000 each restore.",
			"0.20 = Increase by 20% each restore. i.e 1200 if default is 1000. Next would be 1440, then 1728, etc."));
	Main.config.AddValidationEntry("insurance.cooldown-seconds", 300, List.of(
			"How often can players make claims?",
			"300s = 5m"));
	Main.config.AddValidationEntry("insurance.reset-time-seconds", 3600, List.of(
			"How often should the cost increase reset?",
			"Set -1 for never",
			"3600s = 1h"));
	Main.config.AddValidationEntry("insurance.lore", "&fCost: &a$%cost%", List.of(
			"Lore added to bags in the gui showing the cost, max 1 line.",
			"Placeholders: %cost% - The cost of the claim."));
	
	Log.Debug(Main.plugin, "[DI-2] Validating config.yml");
	Main.config.Validate();
	}
	
	private static void Lang() {
		Lang.lang.AddValidationEntry("prefix", "&7[&aHaven&bBags&7] &r");
		Lang.lang.AddValidationEntry("malformed-command", "&cUnknown command, are you missing some parameters?");
		Lang.lang.AddValidationEntry("feature-disabled", "&cSorry, this feature is disabled.");
		Lang.lang.AddValidationEntry("bag-load-error", "&cBag failed to load.\nPlease notify staff.");
		Lang.lang.AddValidationEntry("bag-rename", "&fRenamed bag to %name%.");
		Lang.lang.AddValidationEntry("bag-rename-reset", "&fReset bag's name.");
		Lang.lang.AddValidationEntry("bag-cannot-rename", "&cYou can only rename bags.");
		Lang.lang.AddValidationEntry("bag-cannot-use", "&cYou cannot use this bag.");
		Lang.lang.AddValidationEntry("bag-does-not-exist", "&cThis bag does not exist.");
		Lang.lang.AddValidationEntry("inventory-full", "&cInventory full, dropping bag on the ground!");
		Lang.lang.AddValidationEntry("bag-already-open", "&cThis bag is already open!");
		Lang.lang.AddValidationEntry("max-bags", "&cSorry, you cannot make any more bags.");
		Lang.lang.AddValidationEntry("bag-in-bag-error", "&cBags cannot be put inside other bags.");
		Lang.lang.AddValidationEntry("bag-in-shulker-error", "&cBags cannot be put inside shulker boxes.");
		Lang.lang.AddValidationEntry("bag-in-bundles-error", "&cBags cannot be put inside bundles.");
		Lang.lang.AddValidationEntry("item-blacklisted", "&cSorry, this item cannot go into bags.");
		Lang.lang.AddValidationEntry("player-trusted", "&aAdded %trusted% as trusted.");
		Lang.lang.AddValidationEntry("player-untrusted", "&eRemoved %trusted% as trusted.");
		Lang.lang.AddValidationEntry("quiver-no-space", "&cNo space available in inventory, cannot draw arrow!");
		Lang.lang.AddValidationEntry("carry-limit", "&cSorry, you cannot carry more than %max% bags.");
		Lang.lang.AddValidationEntry("auto-pickup-command", "&fAuto-pickup has been set to: %value%.");
		Lang.lang.AddValidationEntry("auto-sort-command", "&fAuto-sort has been set to: %value%.");
		Lang.lang.AddValidationEntry("magnet-command", "&fMagnetic has been set to: %value%.");
		Lang.lang.AddValidationEntry("refill-command", "&fRefill has been set to: %value%.");
		Lang.lang.AddValidationEntry("ethereal-open", "&cThe player is currently using their bag.");
		Lang.lang.AddValidationEntry("effects-command", "&fEffect has been set to: %value%.");
		Lang.lang.AddValidationEntry("ethereal-open-admin", "&cThis bag is currently being viewed by an admin.");
		Lang.lang.AddValidationEntry("insurance.fail", "&cYou do not have enough money.");
		Lang.lang.AddValidationEntry("insurance.cooldown", "&cYour insurance is still on cooldown.");
		
		// Admin Lang
		//Lang.lang.AddValidationEntry("bag-create", ""); //unsure wtf this was for
		Lang.lang.AddValidationEntry("player-no-exist", "&cNo bags found for this player.");
		Lang.lang.AddValidationEntry("bag-not-found", "&cNo bag found with that UUID.");
		Lang.lang.AddValidationEntry("bag-ownerless-no-size", "&cOwnerless bag must have a size.");
		Lang.lang.AddValidationEntry("bag-given", "&aYou've been given an %name%!");
		Lang.lang.AddValidationEntry("number-conversion-error", "&cCannot convert '%value%' to a number!");
		Lang.lang.AddValidationEntry("bag-texture-not-found", "&cNo texture called %texture% found.");
		
		// Bag GUI
		Lang.lang.AddValidationEntry("bag-inventory-title", "");
		Lang.lang.AddValidationEntry("per-size-title", false);
		Lang.lang.AddValidationEntry("bag-inventory-title-9", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-18", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-27", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-36", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-45", "");
		Lang.lang.AddValidationEntry("bag-inventory-title-54", "");
		
		// BagItem Lang
		Lang.lang.AddValidationEntry("bag-bound-name", "&a%player%'s Bag");
		Lang.lang.AddValidationEntry("bag-unbound-name", "&aUnbound Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-unused", "&aUnused Bag");
		Lang.lang.AddValidationEntry("bag-ownerless-used", "&aBag");
		Lang.lang.AddValidationEntry("bag-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&fA well crafted bag, suited for carrying stuff."); 
				add("&8ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ");
				}
			} 
		);
		Lang.lang.AddValidationEntry("bag-lore-add", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("%bound-to%"); 
				add("%bag-size%"); 
				add("%bag-effect%"); 
				add("%bag-auto-pickup%"); 
				add("%bag-trusted%"); 
				add("%bag-autosort%"); 
				add("%bag-magnet%"); 
				add("%bag-refill%"); 
				add("%bag-weight%"); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("bound-to", "&7Bound to %owner%");
		Lang.lang.AddValidationEntry("bag-size", "&7Size: %size%");
		Lang.lang.AddValidationEntry("show-bag-content", true);
		Lang.lang.AddValidationEntry("bag-content-title", "&7Content:");
		Lang.lang.AddValidationEntry("bag-content-preview-size", 5);
		Lang.lang.AddValidationEntry("bag-content-item", "&7%item%");
		Lang.lang.AddValidationEntry("bag-content-item-amount", "&7%item% &7x%amount%");
		Lang.lang.AddValidationEntry("bag-content-and-more", "&7And more..");
		Lang.lang.AddValidationEntry("bag-effect", "&7Effect: %effect%");
		Lang.lang.AddValidationEntry("bag-effect-hide", false);
		Lang.lang.AddValidationEntry("bag-auto-pickup", "&7Auto Loot: %filter%");
		Lang.lang.AddValidationEntry("bag-trusted", "&7Trusted: %trusted%");
		Lang.lang.AddValidationEntry("bag-autosort", "&7Auto-Sort: %sorting%");
		Lang.lang.AddValidationEntry("bag-autosort-on", "&aOn");
		Lang.lang.AddValidationEntry("bag-autosort-off", "&cOff");
		Lang.lang.AddValidationEntry("bag-autosort-off-hide", false);
		Lang.lang.AddValidationEntry("bag-magnet", "&7Magnetic: %magnet%");
		Lang.lang.AddValidationEntry("bag-magnet-on", "&aOn");
		Lang.lang.AddValidationEntry("bag-magnet-off", "&cOff");
		Lang.lang.AddValidationEntry("bag-magnet-off-hide", false);
		Lang.lang.AddValidationEntry("bag-refill", "&7Refilling: %refill%");
		Lang.lang.AddValidationEntry("bag-refill-on", "&aOn");
		Lang.lang.AddValidationEntry("bag-refill-off", "&cOff");
		Lang.lang.AddValidationEntry("bag-refill-off-hide", false);
		
		// Admin GUI
		Lang.lang.AddValidationEntry("gui-main", "&aHaven&bBags &rGUI");
		Lang.lang.AddValidationEntry("gui-create", "&aHaven&bBags &eCreation GUI");
		Lang.lang.AddValidationEntry("gui-restore", "&aHaven&bBags &bRestoration GUI");
		Lang.lang.AddValidationEntry("gui-preview", "&aHaven&bBags &dPreview GUI");
		Lang.lang.AddValidationEntry("gui-delete", "&aHaven&bBags &cDeletion GUI");
		Lang.lang.AddValidationEntry("gui-confirm", "&aHaven&bBags &4&lDELETE&r this bag?");
		Lang.lang.AddValidationEntry("main-create", "&aBag Creation");
		Lang.lang.AddValidationEntry("main-create-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Create bags easy."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-restore", "&bBag Restoration");
		Lang.lang.AddValidationEntry("main-restore-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Restore bags of online players.");
				add("");
				add("&7Only the basic bag with it's content will be restored."); 
				add("&7Things such as weight and auto-pickup filter, will not be restored."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-preview", "&dBag Preview");
		Lang.lang.AddValidationEntry("main-preview-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Preview bags of online players."); 
				add("&7You can take items from the preview,"); 
				add("&7without affecting the real bag."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("main-delete", "&4Bag Deletion");
		Lang.lang.AddValidationEntry("main-delete-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Delete bags of online players."); 
				add("&c&oDeleted bags cannot be restored!"); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("confirm-cancel", "&4Cancel");
		Lang.lang.AddValidationEntry("confirm-cancel-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Cancel deletion of this bag."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("confirm-confirm", "&aConfirm");
		Lang.lang.AddValidationEntry("confirm-confirm-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Confirm deletion of this bag."); 
				add("&7This cannot be undone."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("return", "&eReturn");
		Lang.lang.AddValidationEntry("return-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Go back."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("page", "&fPage: %page%");
		Lang.lang.AddValidationEntry("next-page", "&aNext Page");
		Lang.lang.AddValidationEntry("prev-page", "&cPrevious Page");
		
		
		Lang.lang.AddValidationEntry("playergui-title-main", "&aHaven&bBags &rPlayer GUI");
		Lang.lang.AddValidationEntry("playergui-title-confirm", "&aHaven&bBags &4&lDELETE&r this bag?");
		Lang.lang.AddValidationEntry("playergui-bags-of", "Bags of %player%:");  
		Lang.lang.AddValidationEntry("playergui-restore", "&bBag Restoration");
		Lang.lang.AddValidationEntry("playergui-restore-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Restore bags."); 
				add(""); 
				add("&7Only the basic bag with it''s content will be restored."); 
				add("&7Things such as weight and auto-pickup filter, will not be restored."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("playergui-delete", "&4Bag Deletion");
		Lang.lang.AddValidationEntry("playergui-delete-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Delete bags."); 
				add("&c&oDeleted bags cannot be restored!"); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("playergui-cancel", "&4Cancel");
		Lang.lang.AddValidationEntry("playergui-cancel-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Cancel deletion of this bag."); 
				}
			} 
		);
		Lang.lang.AddValidationEntry("playergui-confirm", "&aConfirm");
		Lang.lang.AddValidationEntry("playergui-confirm-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7Confirm deletion of this bag."); 
				add("&7This cannot be undone."); 
				}
			} 
		);
	
		Log.Debug(Main.plugin, "[DI-3] Validating lang.yml");
		Lang.lang.Validate();
	}
	
	private static void Weight() {
		Main.weight.AddValidationEntry("enabled", false);
		Main.weight.AddValidationEntry("weight-limit", 3500);
		Main.weight.AddValidationEntry("weight-per-size", true);
		Main.weight.AddValidationEntry("weight-size-9", 1300);
		Main.weight.AddValidationEntry("weight-size-18", 2400);
		Main.weight.AddValidationEntry("weight-size-27", 3500);
		Main.weight.AddValidationEntry("weight-size-36", 4600);
		Main.weight.AddValidationEntry("weight-size-45", 5700);
		Main.weight.AddValidationEntry("weight-size-54", 6800);
		Main.weight.AddValidationEntry("weight-tooltip", "&7Weight: &f%weight%");
		Main.weight.AddValidationEntry("over-encumber.enabled", false);
		Main.weight.AddValidationEntry("over-encumber.percent", 80);
		Main.weight.AddValidationEntry("over-encumber.effects", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("SLOWNESS:1"); 
				}
			} 
		);
		Main.weight.AddValidationEntry("over-encumber.message", "&cYou feel over-encumbered.");
		Main.weight.AddValidationEntry("over-encumber.not", "&aYou feel lighter.");
		Main.weight.AddValidationEntry("weight-lore", "&7Weight: %bar% &7%percent% (%weight%/%limit%)");
		Main.weight.AddValidationEntry("bar-length", 10);
		Main.weight.AddValidationEntry("bar-style", "⬜");
		Main.weight.AddValidationEntry("fill-style", "⬛");
		Main.weight.AddValidationEntry("bar-start", "[");
		Main.weight.AddValidationEntry("bar-end", "]");
		Main.weight.AddValidationEntry("bar-color", "&7");
		Main.weight.AddValidationEntry("fill-color", "&e");
		Main.weight.AddValidationEntry("weight-text-pickup", true);
		Main.weight.AddValidationEntry("bag-cant-carry", "&cCannot carry any more items.\n%item% weighs %weight%, but you can only carry %remaining%.");
		Main.weight.AddValidationEntry("enabled", false);
		Log.Debug(Main.plugin, "[DI-4] Validating weight.yml");
		Main.weight.Validate();	}
	
	private static void Blacklist() {
		Main.blacklist.AddValidationEntry("enabled", false);
		Main.blacklist.AddValidationEntry("use-as-whitelist", false);
		Main.blacklist.AddValidationEntry("blacklist.materials", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("DRAGON_EGG"); 
				add("SPAWNER"); 
				}
			} 
		);
		Main.blacklist.AddValidationEntry("blacklist.displayname", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("Domination Sword"); 
				add("Decapitation Shovel"); 
				add("Anti-air Axe"); 
				add("Cheese");
				}
			} 
		);
		Main.blacklist.AddValidationEntry("blacklist.custommodeldata", new ArrayList<Integer>() {
			private static final long serialVersionUID = 1L; { 
				add(1234567); 
				add(70274); 
				}
			} 
		);
		Main.blacklist.AddValidationEntry("blacklist.nbt", new ArrayList<String>());
		Log.Debug(Main.plugin, "[DI-5] Validating blacklist.yml");
		Main.blacklist.Validate();
	}
	
	private static void Filters() {
		AutoPickup.filter.AddValidationEntry("allow-specific", false);
		
		Log.Debug(Main.plugin, "[DI-6] Validating filters.yml");
		AutoPickup.filter.Validate();
	}
	
	private static void Plugins() {
		Main.plugins.AddValidationEntry("plugins.PlaceholderAPI.enabled", true);
		//Main.plugins.AddValidationEntry("plugins.ChestSort.enabled", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.enabled", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.tagged", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.pvp", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.message", "&cYou cannot use this while in PvP.");
		Main.plugins.AddValidationEntry("plugins.Nexo.enabled", true);
		Main.plugins.AddValidationEntry("plugins.Oraxen.enabled", true);
		Main.plugins.AddValidationEntry("plugins.ItemsAdder.enabled", true);
		
		Main.plugins.AddValidationEntry("mods.HavenBagsPreview.enabled", true);
		Main.plugins.AddValidationEntry("mods.HavenBagsPreview.enable-command", true);
		Main.plugins.AddValidationEntry("mods.HavenBagsPreview.command-message", "§7[§aHaven§bBags§7]§r §9§nhttps://modrinth.com/mod/havenbagspreview");
		
		Log.Debug(Main.plugin, "[DI-6] Validating plugins.yml");
		Main.plugins.Validate();
	}
}
