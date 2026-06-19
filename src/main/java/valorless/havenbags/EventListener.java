package valorless.havenbags;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.inventory.ItemStack;
import valorless.havenbags.Database.Bag;
import valorless.havenbags.api.HavenBagsAPI;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.gui.FeaturesGUI;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.havenbags.utils.NoteBlockUtils;
import valorless.valorlessutils.logging.Log;

public class EventListener implements Listener {

	public static Material upgradeBlock = Material.FLETCHING_TABLE; // The block that opens the upgrade GUI

	public static void init() {
		Log.debug(Main.plugin, "[DI-266] Registering EventListener");
		Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), Main.plugin);

		try {
			upgradeBlock = Main.config.getMaterial("upgrade-gui.block");
		} catch (Exception e) {
			Log.error(Main.plugin, "[DI-286] Failed to get upgrade block from config, using default: " + upgradeBlock);
		}
	}

	//@EventHandler Unused, but kept for future reference
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(HavenBags.inventoryContainsBag(player)) {
			for(Bag bag : HavenBags.getBagsDataInInventory(player)) {
				HavenBags.updateBagLore(bag.item, player);
			}
		}

	}

	@EventHandler
	public void onUpgradeGUI(PlayerInteractEvent event) {
		if(!Main.config.getBool("upgrade-gui.enabled")) return;
		Player player = event.getPlayer();

		if(event.getHand() != EquipmentSlot.HAND) return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if(block != null) {
				if(block.getType() == upgradeBlock) {
					if(upgradeBlock == Material.NOTE_BLOCK) {
						if(block.getBlockData() instanceof NoteBlock nb) {
							if(NoteBlockUtils.compateNoteBlock(nb,
									Main.config.getString("upgrade-gui.noteblock.instrument"),
									Main.config.getInt("upgrade-gui.noteblock.note"))) {
								// If the block is a Note Block with the correct instrument and note, open the upgrade GUI
								event.setCancelled(true);
								new UpgradeGUI(player);
							}
						}
					}else {
						event.setCancelled(true);
						new UpgradeGUI(player);
					}
				}
			}
		}

	}

	/**
	 * Handles inventory click events to open the features GUI when the configured item is clicked.
	 * <p>
	 * Checks if the features GUI is enabled in the config, and if the clicked item matches the configured "opens-by" item.
	 * If both conditions are met, cancels the event and opens the FeaturesGUI for the player.
	 *
	 * @param event The InventoryClickEvent triggered when a player clicks in their inventory
	 */
	@EventHandler
	public void onFeatureGUI(InventoryClickEvent event) {
		if(!Main.config.getBool("features-gui.enabled")) return;
		//if(event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.PLAYER) return; // Only trigger for player inventory
		ClickType reqClick = ClickType.valueOf(Main.config.getString("features-gui.opens-by").toUpperCase());
		if(event.getClick() == reqClick) {
			Player player = (Player) event.getWhoClicked();
			ItemStack clickedItem = event.getCurrentItem();
			if (HavenBags.isBag(clickedItem) && BagState.getState(clickedItem) == BagState.USED) {
				event.setCancelled(true);
				if(!player.hasPermission("havenbags.use")) {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-cannot-use"), null));
					return;
				}

				if(FeaturesGUI.OpenGUIs.get(player) != null) return;

				Data data = HavenBagsAPI.getBag(HavenBags.getBagUUID(clickedItem));

				if(!HavenBags.isOwner(clickedItem, player) && !data.isPlayerTrusted(player.getName())) {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-cannot-use"), player));
					return;
				}

				new FeaturesGUI(player, clickedItem, data);
			}
		}
	}

}
