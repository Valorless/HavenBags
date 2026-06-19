package valorless.havenbags.gui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import valorless.havenbags.*;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.datamodels.Sound;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.events.gui.PrepareUpgradeEvent;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.items.ItemUtils;

/**
 * UpgradeGUI is a GUI for upgrading bags or applying skins to them.
 * It allows players to upgrade their bags to larger sizes or apply skins using tokens.
 * The GUI is opened by right-clicking a specific block (defined in the config).
 */
public class UpgradeGUI implements Listener {
	
	/**
	 * Enum to define the type of result for the upgrade GUI.
	 * It can either be an upgrade to a larger bag size or applying a skin to a bag.
	 */
	public enum ResultType {
		Upgrade, // Upgrade a bag to a larger size
		Skin, // Skin a bag with a token
		Effect // Apply an effect to a bag
	}
	
	/**
	 * Static class to keep track of all open UpgradeGUIs.
	 * Used to close all open GUIs when the server stops or the plugin is disabled.
	 */
	public static class OpenGUIs {

	    // Static map of Players -> their open UpgradeGUI
	    private static final HashMap<Player, UpgradeGUI> guis = new HashMap<>();

	    public static void add(Player player, UpgradeGUI gui) {
	        guis.put(player, gui); // replaces existing if already present
	    }

	    public static boolean contains(Player player) {
	        return guis.containsKey(player);
	    }

	    public static boolean contains(UpgradeGUI gui) {
	        return guis.containsValue(gui);
	    }

	    public static UpgradeGUI get(Player player) {
	        return guis.get(player); // returns null if not present
	    }

	    public static void remove(Player player) {
	        guis.remove(player);
	    }

	    public static void remove(UpgradeGUI gui) {
	        guis.entrySet().removeIf(entry -> entry.getValue().equals(gui));
	    }

	    public static void closeAll() {
	        for (UpgradeGUI gui : guis.values()) {
	            gui.close(new ArrayList<ItemStack>());
	        }
	    }
	}

	
	Inventory inv;
	int invSize = 27; // 3 rows of 9 slots
	Player player;
	
	int itemSlot1 = 10;
	int itemSlot2 = 12;
	int resultSlot = 15;
	
	ResultType resultType = ResultType.Upgrade;
	
	Material filler = Material.GRAY_STAINED_GLASS_PANE;
	
	int upgAmount = 0;
	
	public UpgradeGUI(Player player) {
		
		this.player = player;
		
		this.invSize = Main.config.getInt("upgrade-gui.gui-size");
		this.itemSlot1 = Main.config.getInt("upgrade-gui.slots.bag");
		this.itemSlot2 = Main.config.getInt("upgrade-gui.slots.token");
		this.resultSlot = Main.config.getInt("upgrade-gui.slots.result");
		
		this.inv = Bukkit.createInventory(player, invSize, Lang.parse(Main.config.getString("upgrade-gui.title"), player));
		
		try {
			filler = Material.getMaterial(Main.config.getString("upgrade-gui.filler"));
		} catch (Exception e) {
			Log.error(Main.plugin, "[DI-287] Failed to get filler material from config, using default: GRAY_STAINED_GLASS_PANE");
		}

		if(filler != Material.AIR) {
			for(int i = 0; i < invSize; i++) {
				if(i == itemSlot1 || i == itemSlot2 || i == resultSlot) continue;
				ItemStack fill = new ItemStack(filler);
				ItemMeta fillMeta = fill.getItemMeta();
				fillMeta.setDisplayName(" ");
				try {
					if (Server.VersionHigherOrEqualTo(Version.v1_21)) {
						Method setHideTooltip = fillMeta.getClass().getMethod("setHideTooltip", boolean.class);
						setHideTooltip.setAccessible(true);
						setHideTooltip.invoke(fillMeta, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				fill.setItemMeta(fillMeta);
				inv.setItem(i, fill);
			}
			Object[] f = Main.config.getConfigurationSection("upgrade-gui.custom-filler").getKeys(false).toArray();
			for(int i = 0; i < f.length; i++) {
				String slot = String.valueOf(f[i]);
				if(Integer.parseInt(slot) == itemSlot1 || Integer.parseInt(slot) == itemSlot2 || Integer.parseInt(slot) == resultSlot) continue;
				Material customFiller = Material.AIR;
				
				try {
					customFiller = Main.config.getMaterial(String.format("upgrade-gui.custom-filler.%s", slot));
				} catch (Exception e) {
					Log.error(Main.plugin, "[DI-287] Failed to get filler material from config, using default: AIR");
				}
				ItemStack fill = new ItemStack(customFiller);
				ItemMeta fillMeta = fill.getItemMeta();
				fillMeta.setDisplayName(" ");
				try {
					if (Server.VersionHigherOrEqualTo(Version.v1_21)) {
						Method setHideTooltip = fillMeta.getClass().getMethod("setHideTooltip", boolean.class);
						setHideTooltip.setAccessible(true);
						setHideTooltip.invoke(fillMeta, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				fill.setItemMeta(fillMeta);
				inv.setItem(Integer.parseInt(slot), fill);
			}
		}
		
		BukkitRunnable task = new BukkitRunnable() {
		    @Override
		    public void run() {
		    	player.openInventory(inv);
		    }
		};

		task.runTaskLater(Main.plugin, 1); // Delay to ensure the inventory is ready before opening
		
		OpenGUIs.add(player, this);
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		Log.debug(Main.plugin, "[DI-284] [UpgradeGUI] Opening UpgradeGUI for " + player.getName());
	}
	
	public Inventory GetIn() {
		return inv;
	}
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inv)) return;
        event.setCancelled(true);
    }
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().equals(inv)) return;
		//Log.debug(Main.plugin, "[DI-2xx] [UpgradeGUI] " + event.getRawSlot());
		
		if(event.getAction() == InventoryAction.HOTBAR_SWAP) {
			event.setCancelled(true);
			return;
		}
		
		BukkitRunnable task = new BukkitRunnable() {
		    @Override
		    public void run() {
		    	updateGUI(event);
		    }
		};
		task.runTaskLater(Main.plugin, 1);
		
		
		if(event.getRawSlot() == resultSlot && event.getInventory().getItem(resultSlot) == null) {
			event.setCancelled(true);
			return;
		}
		
		if(event.getRawSlot() != itemSlot1 && event.getRawSlot() != itemSlot2 && event.getRawSlot() != resultSlot && event.getRawSlot() < invSize) {
			event.setCancelled(true);
			return;
		} 
	}
	
	@EventHandler
	public void onInventoryShiftClick(InventoryClickEvent event) {
	    HumanEntity clicker = event.getWhoClicked();

	    if (!(clicker instanceof Player)) return;
	    if (!event.isShiftClick()) return;
	    if (event.getInventory() != inv) return;

	    ItemStack currentItem = event.getCurrentItem();
	    if (currentItem == null || currentItem.getType().isAir()) return;

	    int clickedSlot = event.getRawSlot(); // Absolute slot index
	    int[] targetSlots = {itemSlot1, itemSlot2};
	    
	    if(clickedSlot == resultSlot) return;

	    event.setCancelled(true); // Always cancel to override behavior

	    // CASE 1: Shift-clicked from player inventory → move to GUI slots
	    if (clickedSlot >= inv.getSize()) {
	        for (int slot : targetSlots) {
	            ItemStack target = inv.getItem(slot);
	            if (target == null || target.getType().isAir()) {
	                inv.setItem(slot, currentItem.clone());
	                event.setCurrentItem(null);
	                return;
	            }
	        }
	    }

	    // CASE 2: Shift-clicked from GUI input slots → move to player inventory
	    else if (clickedSlot == itemSlot1 || clickedSlot == itemSlot2) {
	        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(currentItem.clone());
	        if (leftovers.isEmpty()) {
	            event.setCurrentItem(null); // Clear the item from the GUI slot
	        }
	    }

	}
	
	@EventHandler
	public void onInventoryClickResult(InventoryClickEvent event) {
		if (!event.getInventory().equals(inv)) return;
		if(event.getAction() == InventoryAction.HOTBAR_SWAP) {
			event.setCancelled(true);
			return;
		}
		if(event.getRawSlot() != resultSlot) return; // Only handle clicks in the result slot
		if(event.getInventory().getItem(itemSlot1) == null) return;
		if(event.getInventory().getItem(itemSlot2) == null) return;
		if(event.getInventory().getItem(resultSlot) == null) return;
		
		Player player = (Player) event.getWhoClicked();
		Sound sound = Sound.parse(Main.config.getString("upgrade-gui.success-sound"));
	    sound.play(player);
	    
		ItemStack clicked = event.getInventory().getItem(resultSlot);

		ItemStack token = null;
		ItemStack upgrade = null;
		
		if(resultType == ResultType.Upgrade) {
			String owner = PDC.getString(clicked, "owner");
			Database.getBag(HavenBags.getBagUUID(clicked), clicked).setSize(PDC.getInteger(clicked, "size"));
			Log.debug(Main.plugin, "[DI-83] " + "[BagUpgrade] Size set to " + PDC.getInteger(clicked, "size"));
			
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(itemSlot1), event.getInventory().getItem(itemSlot2)))) {
				if(!HavenBags.isBag(item)) {
					upgrade = item;
				}
			}

			if(Main.weight.getBool("weight-per-size")) {
				Database.getBag(HavenBags.getBagUUID(clicked), null).setWeightMax(Main.weight.getDouble(String.format("weight-size-%s", PDC.getInteger(clicked, "size"))));
				//Database.setWeightMax(HavenBags.getBagUUID(clicked), Main.weight.getDouble(String.format("weight-size-%s", PDC.getInteger(clicked, "size"))));
				Log.debug(Main.plugin, "[DI-84] " + "[BagUpgrade] Weight Limit set to " + Main.weight.getDouble(String.format("weight-size-%s", PDC.getInteger(clicked, "size"))));
			}
			if(clicked.getType() == Material.PLAYER_HEAD) {
				if(Main.config.getBool("bag-textures.enabled") && !Main.config.getBool("upgrades.keep-texture")) {
					if(!owner.equalsIgnoreCase("ownerless")) {
						Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(Main.config.getString(String.format("bag-textures.size-%s", PDC.getInteger(clicked, "size"))));
					}else {
						Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(Main.config.getString(String.format("bag-textures.size-ownerless-%s", PDC.getInteger(clicked, "size"))));
					}
				}
			}
			HavenBags.updateBagLore(clicked, null);
		}
		else if(resultType == ResultType.Skin) {
			
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(itemSlot1), event.getInventory().getItem(itemSlot2)))) {
				if(!HavenBags.isBag(item)) {
					token = item;
				}
			}
		
			ItemMeta meta = clicked.getItemMeta();
			String value = PDC.getString(token, "token-skin");
			try {
				int cmd = Integer.parseInt(value);
				if(value != null && meta.hasCustomModelData()) {
					Log.debug(Main.plugin, "[DI-287] " + "[BagSkin] CustomModelData Skin.");
					meta.setCustomModelData(cmd);
					clicked.setItemMeta(meta);
				}
			}catch(Exception e) {
				if(value.chars().count() < 30) {
					Log.debug(Main.plugin, "[DI-278] [UpgradeGUI] Textures.yml Skin.");
					String texture = Main.textures.GetString(String.format("textures.%s", value));
					if(BagState.getState(clicked) == BagState.NEW) {
						HeadCreator.setTextureValue(clicked, texture);
					}else {
						Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(texture);
					}
				}else {
					Log.debug(Main.plugin, "[DI-278] [UpgradeGUI] Texture Skin.");
					if(Base64Validator.isValidBase64(value)) {
						if(BagState.getState(clicked) == BagState.NEW) {
							HeadCreator.setTextureValue(clicked, value);
						}else {
							Database.getBag(HavenBags.getBagUUID(clicked), clicked).setTexture(value);
						}
					}else {
						Log.debug(Main.plugin, "[DI-278] [UpgradeGUI] Invalid Skin.");
						return;
					}
				}
			}
			Log.debug(Main.plugin, "[DI-289] " + "[BagSkin] Applied skin!");
		}
		else if(resultType == ResultType.Effect) {			
			for (ItemStack item : new ArrayList<>(List.of(event.getInventory().getItem(itemSlot1), event.getInventory().getItem(itemSlot2)))) {
				if(!HavenBags.isBag(item)) {
					token = item;
				}
			}
		
			String value = PDC.getString(token, "token-effect");
			Database.getBag(HavenBags.getBagUUID(clicked), clicked).setEffect(value);
			HavenBags.updateBagLore(clicked, null);
		}

		// Reset slots
		if(event.getInventory().getItem(itemSlot1).isSimilar(upgrade)) {
			if(upgrade != null && upgrade.getAmount() > upgAmount) {
				int amount = upgrade.getAmount() - upgAmount;
				upgrade.setAmount(amount);
			}else {
				event.getInventory().setItem(itemSlot1, new ItemStack(Material.AIR));
			}
		}else {
			event.getInventory().setItem(itemSlot1, new ItemStack(Material.AIR));
		}

		if(event.getInventory().getItem(itemSlot2).isSimilar(upgrade)) {
			if(upgrade != null && upgrade.getAmount() > upgAmount) {
				int amount = upgrade.getAmount() - upgAmount;
				upgrade.setAmount(amount);
			}else {
				event.getInventory().setItem(itemSlot2, new ItemStack(Material.AIR));
			}
		}else {
			event.getInventory().setItem(itemSlot2, new ItemStack(Material.AIR));
		}

		/*final ItemStack upg = upgrade;
		BukkitRunnable task = new BukkitRunnable() {
			@Override
			public void run() {

				// Reset slots
				if(event.getInventory().getItem(itemSlot1) == upg) {
					if(upg != null && upg.getAmount() > upgAmount) {
						int amount = upg.getAmount() - upgAmount;
						upg.setAmount(amount);
					}else {
						event.getInventory().setItem(itemSlot1, new ItemStack(Material.AIR));
					}
				}else {
					event.getInventory().setItem(itemSlot1, new ItemStack(Material.AIR));
				}

				if(event.getInventory().getItem(itemSlot2) == upg) {
					if(upg != null && upg.getAmount() > upgAmount) {
						int amount = upg.getAmount() - upgAmount;
						upg.setAmount(amount);
					}else {
						event.getInventory().setItem(itemSlot2, new ItemStack(Material.AIR));
					}
				}else {
					event.getInventory().setItem(itemSlot2, new ItemStack(Material.AIR));
				}
			}
		};
		task.runTaskLater(Main.plugin, 1);*/
	}
	
	void updateGUI(InventoryClickEvent event) {		
		upgAmount = 0;
		ItemStack slot1 = event.getInventory().getItem(itemSlot1);
		ItemStack slot2 = event.getInventory().getItem(itemSlot2);
		ItemStack result = prepareResult(player, 
				slot1 != null ? slot1.clone() : new ItemStack(Material.AIR), 
				slot2 != null ? slot2.clone() : new ItemStack(Material.AIR));
		
		Bukkit.getPluginManager().callEvent(new PrepareUpgradeEvent(inv, player, slot1, slot2, result));
	}
	
	private ItemStack prepareResult(Player player, ItemStack slot1, ItemStack slot2) {
		Log.debug(Main.plugin, "[DI-285] [UpgradeGUI] Preparing result.");
		ItemStack bag = null;
		ItemStack token = null;
		
		try {
			Log.debug(Main.plugin, "[DI-267] [UpgradeGUI] Checking items.");
			int i = 0;
			for (ItemStack item : new ArrayList<>(List.of(slot1, slot2))) {
				if(HavenBags.isBag(item)) {
					Log.debug(Main.plugin, "[DI-268] [UpgradeGUI] Bag in slot " + i + ".");
					bag = item;
				}
				else token = item;
				i++;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(bag == null || token == null) return null;
		
		if(PDC.has(bag, "skin") && PDC.getBoolean(bag, "skin") == false) {
			Log.debug(Main.plugin, "[DI-269] [UpgradeGUI] Bag cannot be skinned.");
			return null;
		}
		if(PDC.has(token, "token-skin")) {
			Log.debug(Main.plugin, "[DI-270] [UpgradeGUI] Bag is skinnable.");
			TokenType type = PDC.has(token, "token-type") ? TokenType.get(PDC.getString(token, "token-type")) : null;
			resultType = ResultType.Skin;
			return getSkinResult(bag, token, type);
		}
		
		if(PDC.has(token, "token-effect")) {
			resultType = ResultType.Effect;
			return getEffectResult(bag, token);
		}


		if(PDC.has(bag, "upgrade") && PDC.getBoolean(bag, "upgrade") == false) {
			Log.debug(Main.plugin, "[DI-271] [UpgradeGUI] Bag cannot upgrade.");
			return null;
		}else {
			int size = PDC.getInteger(bag, "size");
			Log.debug(Main.plugin, "[DI-272] [UpgradeGUI] Is bag max size?");
			if(size == 54) return null;
			Log.debug(Main.plugin, "[DI-273] [UpgradeGUI] Is bag used?");
			if(BagState.getState(bag) == BagState.NEW) return null;
			Log.debug(Main.plugin, "[DI-274] [UpgradeGUI] Is player allowed to upgrade this size?");
			if(!player.hasPermission(String.format("havenbags.upgrade.%s", size))) return null;
			Log.debug(Main.plugin, "[DI-274] [UpgradeGUI] Is player allowed to open the new size?");
			if(!player.hasPermission(String.format("havenbags.open.%s", size+9))) return null;
			Log.debug(Main.plugin, "[DI-274] [UpgradeGUI] Is player the owner of the bag, or has bypass?");
			if(!HavenBags.isOwner(bag, player)) return null;


			Log.debug(Main.plugin, "[DI-274] [UpgradeGUI] Do materials match?");
			String[] split = Main.config.getString(String.format("upgrades.from-%s-to-%s", size, size+9)).split(":");
			int cmd = 0;
			String model = null;
			Material requirement = Material.getMaterial(split[0]);
			int amount = Integer.parseInt(split[1]);
			upgAmount = amount;
			try {
				if(split.length == 3) {
					cmd = Integer.parseInt(split[2]);
					if(token.hasItemMeta()) {
						if(token.getItemMeta().hasCustomModelData() == false) return null;
						if(token.getItemMeta().getCustomModelData() != cmd) return null;
					}else return null;
				}
			}catch(Exception e) { //ItemModel
				if(split.length == 3 && Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
					model = split[2];
					if(token.hasItemMeta()) {
						if(ItemUtils.GetItemModel(token) == null) return null;
						if(!ItemUtils.GetItemModel(token).getKey().equalsIgnoreCase(model)) return null;
					}else return null;
				}
			}
			if(token.getType() != requirement || token.getAmount() < amount) return null;
			ItemStack result = bag.clone();
			resultType = ResultType.Upgrade;
			return getUpgradeResult(result, size, size+9, bag, token);
		}
		//return null;
	}
	
	@EventHandler
	void onPrepareUpgrade(PrepareUpgradeEvent event) {
		if(!event.getInventory().equals(inv)) return;
		inv.setItem(resultSlot, event.getResult());
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getInventory().equals(inv)) return;
		List<ItemStack> returnItems = new ArrayList<>();
		
		if(event.getInventory().getItem(itemSlot1) != null) {
			returnItems.add(event.getInventory().getItem(itemSlot1));
		}
			
		if(event.getInventory().getItem(itemSlot2) != null) {
			returnItems.add(event.getInventory().getItem(itemSlot2));
		}
		
		close(returnItems);
	}
	
	/**
	 * Closes the UpgradeGUI and returns the items to the player's inventory.
	 * @param items The items to return to the player.
	 */
	public void close(List<ItemStack> items) {
		for(ItemStack item : items) {
			returnItem(player, item);
		}
		
		Log.debug(Main.plugin, "[DI-283] [UpgradeGUI] Unregistering listener for " + player.getName());
		HandlerList.unregisterAll(this);
		OpenGUIs.remove(this);
	}
	
	void returnItem(Player player, ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
		    // Attempt to add the item to the player's inventory
		    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
		    if (!leftover.isEmpty()) {
		        // Drop any items that didn't fit
		        for (ItemStack drop : leftover.values()) {
		            player.getWorld().dropItemNaturally(player.getLocation(), drop);
		        }
		    }
		}
	}
	
	ItemStack getUpgradeResult(ItemStack item, int from, int to, ItemStack bag, ItemStack upgrade) {
		ItemMeta meta = item.getItemMeta();
		List<String> newLore = new ArrayList<String>();
		String owner = PDC.getString(bag, "owner");

		String[] split = Main.config.getString(String.format("upgrades.from-%s-to-%s", from, to)).split(":");
		Material requirement = Material.getMaterial(split[0]);
		int amount = Integer.parseInt(split[1]);
		if(upgrade.getType() == requirement && upgrade.getAmount() >= amount) {
			meta.setLore(newLore);
			item.setItemMeta(meta);
			PDC.setinteger(item, "size", to);
			if(Main.weight.getBool("weight-per-size")) {
				PDC.setDouble(item, "weight-limit", Main.weight.getDouble(String.format("weight-size-%s", to)));
			}
			HavenBags.updateBagLore(item, null, true);
			if(Main.config.getBool("bag-textures.enabled") && !Main.config.getBool("upgrades.keep-texture")) {
				if(owner.equalsIgnoreCase("ownerless")) {
					HeadCreator.setTextureValue(item, Main.config.getString(String.format("bag-textures.size-ownerless-%s", to)));
				}else {
					HeadCreator.setTextureValue(item, Main.config.getString(String.format("bag-textures.size-%s", to)));
				}
			}
		}
		return item;
	}
	
	ItemStack getSkinResult(ItemStack item, ItemStack skin, TokenType type) {
		Log.debug(Main.plugin, "[DI-275] [UpgradeGUI] Preparing Result.");
		ItemMeta meta = item.getItemMeta();
		String value = PDC.getString(skin, "token-skin");
		//TokenType type = PDC.Has(skin, "token-type") ? TokenType.get(PDC.GetString(skin, "token-type")) : null;
		
		if(type != null) {
			if(type ==  TokenType.Texture) {
				if(value.chars().count() < 30) {
					Log.debug(Main.plugin, "[DI-276] [UpgradeGUI] Textures.yml Skin.");
					String texture = Main.textures.getString(String.format("textures.%s", value));
					HeadCreator.setTextureValue(item, texture);
				}else {
					Log.debug(Main.plugin, "[DI-276] [UpgradeGUI] Texture Skin.");
					if(Base64Validator.isValidBase64(value)) {
						HeadCreator.setTextureValue(item, value);
					}else {
						Log.debug(Main.plugin, "[DI-277] [UpgradeGUI] Invalid Skin.");
						item = new ItemStack(Material.AIR);
					}
				}
				
			}
			else if(type == TokenType.ModelData) {
				try {
					int cmd = Integer.parseInt(value);
					if(value != null) {
						Log.debug(Main.plugin, "[DI-278] [UpgradeGUI] CustomModelData Skin.");
						meta.setCustomModelData(cmd);
						item.setItemMeta(meta);
					}
				}catch(Exception e) {}
			}
			else if(type == TokenType.ItemModel && Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
				Log.debug(Main.plugin, "[DI-279] [UpgradeGUI] ItemModel Skin.");
				ItemUtils.SetItemModel(item, value);
			}
			
		}else { // Handle old tokens
			try {
				int cmd = Integer.parseInt(value);
				if(value != null) {
					Log.debug(Main.plugin, "[DI-280] [UpgradeGUI] CustomModelData Skin.");
					meta.setCustomModelData(cmd);
					item.setItemMeta(meta);
				}
			}catch(Exception e) {
				if(value.chars().count() < 30) {
					Log.debug(Main.plugin, "[DI-281] [UpgradeGUI] Textures.yml Skin.");
					String texture = Main.textures.getString(String.format("textures.%s", value));
					HeadCreator.setTextureValue(item, texture);
				}else {
					Log.debug(Main.plugin, "[DI-281] [UpgradeGUI] Texture Skin.");
					if(Base64Validator.isValidBase64(value)) {
						HeadCreator.setTextureValue(item, value);
					}else {
						Log.debug(Main.plugin, "[DI-282] [UpgradeGUI] Invalid Skin.");
						item = new ItemStack(Material.AIR);
					}
				}
			}

		}
		
		return item;
	}
	
	ItemStack getEffectResult(ItemStack item, ItemStack skin) {
		Log.debug(Main.plugin, "[DI-293] [UpgradeGUI] Preparing Result.");
		String value = PDC.getString(skin, "token-effect");
		String uuid = HavenBags.getBagUUID(item);
				
		if(BagEffects.hasEffect(value)) {
			Bag data = Database.getBag(uuid, null).clone();
			data.setEffect(value);
			HavenBags.updateUsed(item, data, player);
			return item;
		}
		else return new ItemStack(Material.AIR);
	}
	
}
