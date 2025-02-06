package valorless.havenbags;

import java.util.ArrayList;

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
	Main.config.AddValidationEntry("debug", false);
	Main.config.AddValidationEntry("config-version", 2);
	Main.config.AddValidationEntry("check-updates", true);
	Main.config.AddValidationEntry("auto-save-interval", 1200);
	Main.config.AddValidationEntry("auto-save-message", true);
	Main.config.AddValidationEntry("language", "en_us");
	Main.config.AddValidationEntry("bag-type", "HEAD");
	Main.config.AddValidationEntry("bag-material", "ENDER_CHEST");
	Main.config.AddValidationEntry("bag-custom-model-data", 0);
	Main.config.AddValidationEntry("bag-custom-model-datas.enabled", false);
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
	Main.config.AddValidationEntry("bag-texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=");
	Main.config.AddValidationEntry("bag-textures.enabled", false);
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
	
	Main.config.AddValidationEntry("capacity-based-textures.enabled", false);
	Main.config.AddValidationEntry("capacity-based-textures.textures.0", "");

	Main.config.AddValidationEntry("open-sound", "ITEM_BUNDLE_INSERT");
	Main.config.AddValidationEntry("open-volume", 1);
	Main.config.AddValidationEntry("open-pitch", 1);
	Main.config.AddValidationEntry("close-sound", "ITEM_BUNDLE_DROP_CONTENTS");
	Main.config.AddValidationEntry("close-volume", 1);
	Main.config.AddValidationEntry("close-pitch", 1);
	Main.config.AddValidationEntry("inventory-full-sound", "ENTITY_VILLAGER_NO");
	Main.config.AddValidationEntry("inventory-full-volume", 1);
	Main.config.AddValidationEntry("inventory-full-pitch", 1);
	Main.config.AddValidationEntry("max-bags", 0);
	Main.config.AddValidationEntry("protect-bags", true);
	Main.config.AddValidationEntry("protect-bags-players", false);
	Main.config.AddValidationEntry("bags-in-bags", false);
	Main.config.AddValidationEntry("bags-in-shulkers", true);
	Main.config.AddValidationEntry("keep-bags", true);
	Main.config.AddValidationEntry("old-help-menu", false);
	Main.config.AddValidationEntry("auto-pickup", true);
	Main.config.AddValidationEntry("auto-pickup-sound", "ENTITY_ITEM_PICKUP");
	Main.config.AddValidationEntry("auto-pickup-volume", 0.8);
	Main.config.AddValidationEntry("auto-pickup-pitch-min", 1.05);
	Main.config.AddValidationEntry("auto-pickup-pitch-max", 1.25);
	Main.config.AddValidationEntry("auto-pickup-inventory.enabled", false);
	Main.config.AddValidationEntry("auto-pickup-inventory.events.onBlockBreak", true);
	Main.config.AddValidationEntry("auto-pickup-inventory.events.onItemPickup", true);
	Main.config.AddValidationEntry("trusting", true);
	Main.config.AddValidationEntry("quiver-bags", true);
	Main.config.AddValidationEntry("upgrades.enabled", false);
	Main.config.AddValidationEntry("upgrades.keep-texture", false);
	Main.config.AddValidationEntry("upgrades.from-9-to-18", "EMERALD:5:90000");
	Main.config.AddValidationEntry("upgrades.from-18-to-27", "DIAMOND:10:90001");
	Main.config.AddValidationEntry("upgrades.from-27-to-36", "NETHERITE_INGOT:1:90002");
	Main.config.AddValidationEntry("upgrades.from-36-to-45", "EMERALD:5:NETHERITE_BLOCK:1:90003");
	Main.config.AddValidationEntry("upgrades.from-45-to-54", "END_CRYSTAL:1");
	Main.config.AddValidationEntry("skin-token.display-name", "&aSkin Token");
	Main.config.AddValidationEntry("skin-token.material", "PLAYER_HEAD");
	Main.config.AddValidationEntry("skin-token.custommodeldata", 0);
	Main.config.AddValidationEntry("skin-token.lore", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
	{ add("&7Combine with a bag in an anvil to apply."); add("&7Skin: &e%skin%"); }} );
	
	
	Main.config.AddValidationEntry("blacklist", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
	{ add("world_name"); add("world_name_nether"); add("another_world"); }} );
	Main.config.AddValidationEntry("allowed-containers", new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
	{ add("CHEST"); add("ENDER_CHEST"); add("BARREL"); add("SHULKER_BOX"); add("MERCHANT"); }} );
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
		Lang.lang.AddValidationEntry("item-blacklisted", "&cSorry, this item cannot go into bags.");
		Lang.lang.AddValidationEntry("player-trusted", "&aAdded %trusted% as trusted.");
		Lang.lang.AddValidationEntry("player-untrusted", "&eRemoved %trusted% as trusted.");
		Lang.lang.AddValidationEntry("quiver-no-space", "&cNo space available in inventory, cannot draw arrow!");
		
		// Admin Lang
		//Lang.lang.AddValidationEntry("bag-create", ""); //unsure wtf this was for
		Lang.lang.AddValidationEntry("player-no-exist", "&cNo bags found for this player.");
		Lang.lang.AddValidationEntry("bag-not-found", "&cNo bag found with that UUID.");
		Lang.lang.AddValidationEntry("bag-ownerless-no-size", "&cOwnerless bag must have a size.");
		Lang.lang.AddValidationEntry("bag-given", "&aYou've been given an %name%!");
		Lang.lang.AddValidationEntry("number-conversion-error", "&cCannot convert '%value%' to a number!");
		Lang.lang.AddValidationEntry("player-no-bags", "&cPlayer '%player%' has no bags.");
		Lang.lang.AddValidationEntry("bags-of", "Bags of %player%:");
		
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
				add("%bag-auto-pickup%"); 
				add("%bag-trusted%"); 
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
		Lang.lang.AddValidationEntry("bag-auto-pickup", "&7Auto Loot: %filter%");
		Lang.lang.AddValidationEntry("bag-trusted", "&7Trusted: %trusted%");
		
		// Admin GUI
		Lang.lang.AddValidationEntry("too-many-bags", "&cThis player has over 53 bags.\\nPlease restore their bags through &e/bags restore&c!");
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
		Lang.lang.AddValidationEntry("main-info", "&eInformation");
		Lang.lang.AddValidationEntry("main-info-lore", new ArrayList<String>() {
			private static final long serialVersionUID = 1L; { 
				add("&7You can also restore bags of offline players,");
				add("&7by using &e/bags gui restore <player-uuid>&7.");
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
		Main.plugins.AddValidationEntry("plugins.ChestSort.enabled", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.enabled", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.tagged", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.pvp", true);
		Main.plugins.AddValidationEntry("plugins.PvPManager.message", "&cYou cannot use this while in PvP.");
		//plugins.AddValidationEntry("plugins.Oraxen.enabled", true);
		
		Main.plugins.AddValidationEntry("mods.HavenBagsPreview.enabled", true);
		Main.plugins.AddValidationEntry("mods.HavenBagsPreview.enable-command", true);
		Main.plugins.AddValidationEntry("mods.HavenBagsPreview.command-message", "§7[§aHaven§bBags§7]§r §9§nhttps://modrinth.com/mod/havenbagspreview");
		
		Log.Debug(Main.plugin, "[DI-6] Validating plugins.yml");
		Main.plugins.Validate();
	}
}
