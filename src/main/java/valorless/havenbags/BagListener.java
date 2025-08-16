package valorless.havenbags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.util.BlockIterator;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import valorless.havenbags.database.BagCache.Observer;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.gui.BagGUI;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.sound.SFX;

public class BagListener implements Listener{
	
	/**
	 * Initializes the BagListener by registering it with the Bukkit event system.
	 * This method should be called during plugin startup to ensure the listener is active.
	 */
	public static void init() {
		Log.Debug(Main.plugin, "[DI-9] Registering BagListener");
		Bukkit.getServer().getPluginManager().registerEvents(new BagListener(), Main.plugin);
	}
	
	/**
	 * This listener handles player interactions with bags.
	 * It checks for cooldowns to prevent spamming and processes bag opening.
	 */
	// Cooldown map to prevent spamming interactions
	private final Map<UUID, Long> interactCooldowns = new HashMap<>();
	
	/**
	 * Handles player login events to clear any existing cooldowns.
	 * 
	 * @param event The PlayerJoinEvent triggered when a player joins the server.
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
	    interactCooldowns.remove(event.getPlayer().getUniqueId());
	}
	
	/**
	 * Handles player interactions with bags.
	 * 
	 * @param event The PlayerInteractEvent triggered by the player.
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
	    Player player = event.getPlayer();
	    UUID uuid = player.getUniqueId();
	    
		if(event.getHand() != EquipmentSlot.HAND) return;
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

			// Since 1.20 players can now edit signs. Block bag interaction if a sign is clicked.
			try {
				if(getTargetBlock(player, 5).getType().toString().contains("SIGN")) { // My old way, before i learned about the reach attribute.
					return; 
				}
			}catch(Exception e) {}

			if(event.getClickedBlock() != null) {
				if(event.getClickedBlock().getType().toString().contains("SIGN")) {
					return;
				}
			}
			
			ItemStack hand = player.getInventory().getItemInMainHand();
			
			Observer.CheckInventory(player.getInventory().getContents());

			//player.sendMessage("Right click");
			if(HavenBags.IsBag(hand)) {

			    long now = System.currentTimeMillis();
			    long last = interactCooldowns.getOrDefault(uuid, 0L);

			    if (now - last < 250) { // 250ms cooldown (~5 ticks)
			    	//player.sendMessage("Bag cooldown");
			        return;
			    }

			    interactCooldowns.put(uuid, now);
			    
				if(BagData.isReady() == false) {
					event.setCancelled(true);
					return;
				}

				if(!player.hasPermission("havenbags.use")) {
					player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-cannot-use"), null));
					event.setCancelled(true);
					return;
				}else {    			
					ItemMeta item = player.getInventory().getItemInMainHand().getItemMeta();
					if(item == null) {
						return;
					}

					if(Main.plugins.GetBool("plugins.PvPManager.enabled")) {
						if(Bukkit.getPluginManager().getPlugin("PvPManager") != null) {
							try {
								Log.Debug(Main.plugin, "[DI-47] " + "Checking if player is pvp.");
								PlayerHandler playerHandler = PvPManager.getInstance().getPlayerHandler();
								PvPlayer pvplayer = playerHandler.get(player);
								boolean pvp = pvplayer.hasPvPEnabled();
								boolean tagged = pvplayer.isInCombat();
								//if(!Settings.isGlobalStatus()) pvp = false;
								//if(PvPManager.getInstance().)  //if global pvp = false, set pvp to false.
								if(pvp && Main.plugins.GetBool("plugins.PvPManager.pvp") == false) {
									Log.Debug(Main.plugin, "[DI-48] " + "Pvp.");
									player.sendMessage(Lang.Parse(Lang.Get("prefix") + Main.plugins.GetString("plugins.PvPManager.message"), player));
									return;
								}
								if(tagged && Main.plugins.GetBool("plugins.PvPManager.tagged") == false) {
									player.sendMessage(Lang.Parse(Lang.Get("prefix") + Main.plugins.GetString("plugins.PvPManager.message"), player));
									Log.Debug(Main.plugin, "[DI-49] " + "Pvp.");
									return;
								}
								Log.Debug(Main.plugin, "[DI-50] " + "No pvp.");
							}catch (Exception e) {
								Log.Error(Main.plugin, "[DI-51] " + "Failed to get PvPManager's API. Is it up to date?");
								e.printStackTrace();
							}
						}
					}

					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					List<String> blacklist = Main.config.GetStringList("blacklist");
					if(blacklist != null) {
						if(blacklist.size() != 0) {
							Log.Debug(Main.plugin, "[DI-52] " + "Player World: " + player.getWorld().getName());
							for(String world : blacklist) {
								Log.Debug(Main.plugin, "[DI-53] " + "Blacklist: " + world);
								if(player.getWorld().getName().equalsIgnoreCase(world)) return;
							}
						}
					}

					Log.Debug(Main.plugin, "[DI-54] " + player.getName() + " is attempting to open a bag");
					
					boolean ownerless = !PDC.GetBoolean(hand, "binding");
					
					int size = PDC.GetInteger(hand, "size");
					for(int i = 9; i <= 54; i += 9) {
						Log.Debug(Main.plugin, "[DI-63] " + "havenbags.open." + String.valueOf(i) + ": "+ player.hasPermission("havenbags.open." + String.valueOf(i)));
						if(size != i) continue;
						if(!player.hasPermission("havenbags.open." + String.valueOf(i))) {
							player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-cannot-use"), null));
							event.setCancelled(true);
							return;
						}
					}
					
					if(PDC.GetString(hand, "owner").equalsIgnoreCase("null")) {
						if(creationLimit(player)) {
							player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("max-bags"), player));
							event.setCancelled(true);
							return;
						}
					}
					
					if(CreateBag(hand, ownerless, player, placeholders)) {
						Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
							OpenBag(hand, ownerless, player, event);
						}, 1L);
					}else {
						OpenBag(hand, ownerless, player, event);
					}

				}
			}
		}
	}
	
	public final Block getTargetBlock(Player player, int range) {
		BlockIterator iter = new BlockIterator(player, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() == Material.AIR) {
				continue;
			}
			break;
		}
		return lastBlock;
	}
	
	private boolean CreateBag(ItemStack bag, boolean ownerless, Player player, List<Placeholder> placeholders) {
		if(!PDC.GetString(bag, "owner").equalsIgnoreCase("null")) return false;
		
		String uuid = PDC.GetString(bag, "uuid");
		if(uuid.equalsIgnoreCase("null")) {
			Log.Debug(Main.plugin, "[DI-55] " + "bag-uuid null");
			uuid = UUID.randomUUID().toString();
			PDC.SetString(bag, "uuid", uuid);
			//return;
		}
		
		ItemMeta meta = bag.getItemMeta();
		
		meta.setDisplayName(ownerless ? Lang.Get("bag-ownerless-used") : Lang.Parse(Lang.lang.GetString("bag-bound-name"), player));
		if(PDC.Has(bag, "name")) meta.setDisplayName(Lang.Parse(PDC.GetString(bag, "name"), player));
		
		List<String> lore = new ArrayList<String>() ;

		for (String l : Lang.lang.GetStringList("bag-lore")) {
			lore.add(Lang.Parse(l, player));
		}
		
		if(!ownerless) {
			placeholders.add(new Placeholder("%owner%", player.getName()));
			lore.add(Lang.Parse(Lang.Get("bound-to"), placeholders, player));
		}
		
		if(PDC.Has(bag, "size")) {
			placeholders.add(new Placeholder("%size%", PDC.GetInteger(bag, "size")));
			lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
		}
		
		meta.setLore(lore);

		bag.setItemMeta(meta);
		PDC.SetString(bag, "owner", ownerless ? "ownerless" : player.getUniqueId().toString());
		//PDC.SetString(bag, "bag-creator", player.getUniqueId().toString());
		PDC.SetDouble(bag, "weight", 0.0);

		List<ItemStack> cont = new ArrayList<ItemStack>();
		for(int i = 0; i < PDC.GetInteger(bag, "size"); i++) {
			cont.add(null);
		}
		
		if(ownerless) {
			BagData.CreateBag(uuid, "ownerless", cont, player, bag);
			Log.Debug(Main.plugin, "[DI-56] " + "Ownerless bag created.");
			Log.Debug(Main.plugin, "[DI-57] " + "Creating timestamp for " + uuid);
		}else {
			BagData.CreateBag(uuid, player.getUniqueId().toString(), cont, player, bag);
			Log.Debug(Main.plugin, "[DI-58] " + "Bound new bag to: " + player.getName());
			Log.Debug(Main.plugin, "[DI-59] " + "Creating timestamp for " + uuid);
		}

		//HavenBags.HasWeightLimit(bag);
		//HavenBags.UpdateBagItem(bag, null, player);
		return true;
	}
	
	@SuppressWarnings("unused")
	private void OpenBag(ItemStack bag, boolean ownerless, Player player, PlayerInteractEvent event) {		
		//String uuid = PDC.GetString(bag, "bag-uuid");
		if(bag.getType() == Material.AIR) return;
		String uuid = HavenBags.GetBagUUID(bag);
		Log.Debug(Main.plugin, "[DI-226] " + "Opening " +  uuid);
		Data data = BagData.GetBag(uuid, bag);
		//String owner = PDC.GetString(bag, "bag-owner");
		
		if(data == null) {
			// This bag doesnt exist and has possibly been removed.
			// Removing the item to prevent further issues.
			Log.Error(Main.plugin, String.format("Player %s tried opening a removed bag, removing the item. (uuid: %s)", 
					player.getName(), uuid));
			bag.setAmount(0);
			player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-does-not-exist"), null));
			return;
		}

		if(data.getViewer() != null && data.getViewer() == player) {
			player.sendMessage(String.format("viewer: %s", data.getViewer() == null ? "null" : data.getViewer().getName()));
			Log.Debug(Main.plugin, "[DI-227] " + "This bag is already open by " + player.getName() + ".");
			event.setCancelled(true);
			return;
		}
		
		if(data.isOpen()) {
			if(data.getViewer() != player) {
				player.sendMessage(String.format("Open by: %s", data.getViewer() == null ? "null" : data.getViewer().getName()));
				player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-already-open"), null));
				Log.Debug(Main.plugin, "[DI-60] " + "This bag is already open.");
				event.setCancelled(true);
				return;
			}
		}
		if(data.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
			Log.Debug(Main.plugin, "[DI-64] " + "Attempting to open bag");
			
			try {
				event.setCancelled(true);
				HavenBags.HasWeightLimit(bag);
				HavenBags.UpdateBagItem(bag, null, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				SFX.Play(Main.config.GetString("open-sound"), 
						Main.config.GetDouble("open-volume").floatValue(), 
						Main.config.GetDouble("open-pitch").floatValue(), player);
			}catch(Exception e) {
				e.printStackTrace();
				BagData.MarkBagClosed(uuid);
			}
			return;
		}
		else if(ownerless) {
			Log.Debug(Main.plugin, "[DI-62] " + "Attempting to open ownerless bag");
			try {
				event.setCancelled(true);
				HavenBags.HasWeightLimit(bag);
				HavenBags.UpdateBagItem(bag, null, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				SFX.Play(Main.config.GetString("open-sound"), 
						Main.config.GetDouble("open-volume").floatValue(), 
						Main.config.GetDouble("open-pitch").floatValue(), player);
			}catch(Exception e) {
				e.printStackTrace();
				BagData.MarkBagClosed(uuid);
			}
			return;
		}
		else if (player.hasPermission("havenbags.bypass") && !ownerless) {
			try {
				event.setCancelled(true);
				HavenBags.HasWeightLimit(bag);
				HavenBags.UpdateBagItem(bag, null, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				//BagData.MarkBagOpen(uuid, bag, player, gui);
				SFX.Play(Main.config.GetString("open-sound"), 
						Main.config.GetDouble("open-volume").floatValue(), 
						Main.config.GetDouble("open-pitch").floatValue(), player);
				Log.Debug(Main.plugin, "[DI-65] " + player + "has attempted to open a bag, bypassing the lock");
			}catch(Exception e) {
				e.printStackTrace();
				BagData.MarkBagClosed(uuid);
			}
			return;
		}
		else if(data.isPlayerTrusted(player.getName()) && !ownerless) {
			try {
				event.setCancelled(true);
				HavenBags.HasWeightLimit(bag);
				HavenBags.UpdateBagItem(bag, null, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				//BagData.MarkBagOpen(uuid, bag, player, gui);
				SFX.Play(Main.config.GetString("open-sound"), 
						Main.config.GetDouble("open-volume").floatValue(), 
						Main.config.GetDouble("open-pitch").floatValue(), player);
			}catch(Exception e) {
				e.printStackTrace();
				BagData.MarkBagClosed(uuid);
			}
			return;
		}
		else{
			//player.sendMessage(Name + "§c You cannot use this bag.");
			player.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-cannot-use"), null));
			event.setCancelled(true);
			return;
		}
	}
	
	public static int getPlayerBagLimit(Player player) {
	    for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
	        String permName = perm.getPermission();

	        if (permName.startsWith("havenbags.max.")) {
	            try {
	                return Integer.parseInt(permName.substring("havenbags.max.".length())); // Extract full number
	            } catch (NumberFormatException e) {
	                return Main.config.GetInt("max-bags");
	            }
	        }
	    }
	    return Main.config.GetInt("max-bags");
	}
	
	public boolean creationLimit(Player player) {
		if(Main.config.GetInt("max-bags") > 0) {
			if(!player.hasPermission("havenbags.bypass")) {
				int limit = getPlayerBagLimit(player);
				if(BagData.GetBags(player.getUniqueId().toString()).size() >= limit) {
					return true;
				}
			}
		}
		return false;
	}

}
