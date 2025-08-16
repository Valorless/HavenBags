package valorless.havenbags;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import valorless.havenbags.BagData.Bag;
import valorless.havenbags.gui.UpgradeGUI;
import valorless.havenbags.utils.NoteBlockUtils;
import valorless.valorlessutils.ValorlessUtils.Log;

public class EventListener implements Listener {

	public static Material upgradeBlock = Material.FLETCHING_TABLE; // The block that opens the upgrade GUI

	public static void init() {
		Log.Debug(Main.plugin, "[DI-266] Registering EventListener");
		Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), Main.plugin);

		try {
			upgradeBlock = Main.config.GetMaterial("upgrade-gui.block");
		} catch (Exception e) {
			Log.Error(Main.plugin, "[DI-286] Failed to get upgrade block from config, using default: " + upgradeBlock);
		}
	}

	//@EventHandler Unused, but kept for future reference
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(HavenBags.InventoryContainsBag(player)) {
			for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
				HavenBags.UpdateBagLore(bag.item, player);
			}
		}

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!Main.config.GetBool("upgrade-gui.enabled")) return;
		Player player = event.getPlayer();

		if(event.getHand() != EquipmentSlot.HAND) return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if(block != null) {
				if(block.getType() == upgradeBlock) {
					if(upgradeBlock == Material.NOTE_BLOCK) {
						if(block.getBlockData() instanceof NoteBlock nb) {
							if(NoteBlockUtils.compateNoteBlock(nb, 
									Main.config.GetString("upgrade-gui.noteblock.instrument"), 
									Main.config.GetInt("upgrade-gui.noteblock.note"))) {
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

}
