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

import me.chancesd.pvpmanager.player.CombatPlayer;
import valorless.havenbags.database.BagCache.Observer;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.datamodels.Sound;
import valorless.havenbags.enums.DatabaseType;
import valorless.havenbags.gui.BagGUI;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.logging.Log;

/**
 * Listener for handling player interactions with bags.
 * This class manages the opening of bags, ensuring proper permissions,
 * cooldowns, and bag ownership checks are enforced.
 * <p>
 * To activate this listener, call the static {@link #init()} method during your plugin's startup.
 * </p>
 */
public class BagListener implements Listener{
	
	/**
	 * Initializes the BagListener by registering it with the Bukkit event system.
	 * This method should be called during plugin startup to ensure the listener is active.
	 */
	public static void init() {
		Log.debug(Main.plugin, "[DI-9] Registering BagListener");
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
			
			Observer.checkInventory(player.getInventory().getContents());

			//player.sendMessage("Right click");
			if(HavenBags.isBag(hand)) {

			    long now = System.currentTimeMillis();
			    long last = interactCooldowns.getOrDefault(uuid, 0L);

			    if (now - last < 250) { // 250ms cooldown (~5 ticks)
			    	//player.sendMessage("Bag cooldown");
			        return;
			    }

			    interactCooldowns.put(uuid, now);
			    
				if(!BagData.isReady()) {
					event.setCancelled(true);
					return;
				}

				if(!player.hasPermission("havenbags.use")) {
					player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-cannot-use"), null));
					event.setCancelled(true);
					return;
				}else {    			
					ItemMeta item = player.getInventory().getItemInMainHand().getItemMeta();
					if(item == null) {
						return;
					}

					if(Main.plugins.getBool("plugins.PvPManager.enabled")) {
						if(Bukkit.getPluginManager().getPlugin("PvPManager") != null) {
							try {
								Log.debug(Main.plugin, "[DI-47] " + "Checking if player is pvp.");
								//PlayerHandler playerHandler = PvPManager.getInstance().getPlayerHandler();
								CombatPlayer pvplayer = CombatPlayer.get(player);
								boolean pvp = pvplayer.hasPvPEnabled();
								boolean tagged = pvplayer.isInCombat();
								//if(!Settings.isGlobalStatus()) pvp = false;
								//if(PvPManager.getInstance().)  //if global pvp = false, set pvp to false.
								if(pvp && Main.plugins.getBool("plugins.PvPManager.pvp") == false) {
									Log.debug(Main.plugin, "[DI-48] " + "Pvp.");
									player.sendMessage(Lang.parse(Lang.get("prefix") + Main.plugins.getString("plugins.PvPManager.message"), player));
									return;
								}
								if(tagged && Main.plugins.getBool("plugins.PvPManager.tagged") == false) {
									player.sendMessage(Lang.parse(Lang.get("prefix") + Main.plugins.getString("plugins.PvPManager.message"), player));
									Log.debug(Main.plugin, "[DI-49] " + "Pvp.");
									return;
								}
								Log.debug(Main.plugin, "[DI-50] " + "No pvp.");
							}catch (Exception e) {
								Log.error(Main.plugin, "[DI-51] " + "Failed to get PvPManager's API. Is it up to date?");
								e.printStackTrace();
							}
						}
					}

					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					List<String> blacklist = Main.config.getStringList("blacklist");
					if(blacklist != null) {
						if(!blacklist.isEmpty()) {
							Log.debug(Main.plugin, "[DI-52] " + "Player World: " + player.getWorld().getName());
							for(String world : blacklist) {
								Log.debug(Main.plugin, "[DI-53] " + "Blacklist: " + world);
								if(player.getWorld().getName().equalsIgnoreCase(world)) return;
							}
						}
					}

					Log.debug(Main.plugin, "[DI-54] " + player.getName() + " is attempting to open a bag");
					
					boolean ownerless = !PDC.getBoolean(hand, "binding");
					
					int size = HavenBags.findClosestNine(PDC.getInteger(hand, "size"));
					for(int i = 9; i <= 54; i += 9) {
						Log.debug(Main.plugin, "[DI-63] " + "havenbags.open." + String.valueOf(i) + ": "+ player.hasPermission("havenbags.open." + i));
						if(size != i) continue;
						if(!player.hasPermission("havenbags.open." + String.valueOf(i))) {
							player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-cannot-use"), null));
							event.setCancelled(true);
							return;
						}
					}
					
					if(PDC.getString(hand, "owner").equalsIgnoreCase("null")) {
						if(creationLimit(player)) {
							player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("max-bags"), player));
							event.setCancelled(true);
							return;
						}
					}
					
					if(createBag(hand, ownerless, player, placeholders)) {
						if(BagData.getDatabase() == DatabaseType.MYSQL || BagData.getDatabase() == DatabaseType.MYSQLPLUS) {
							return;
						}
						Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
							openBag(hand, ownerless, player, event);
						}, 1L);
					}else {
						openBag(hand, ownerless, player, event);
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
	
	@SuppressWarnings("deprecation")
	private boolean createBag(ItemStack bag, boolean ownerless, Player player, List<Placeholder> placeholders) {
		if(!PDC.getString(bag, "owner").equalsIgnoreCase("null")) return false;
		
		String uuid = PDC.getString(bag, "uuid");
		if(uuid.equalsIgnoreCase("null")) {
			Log.debug(Main.plugin, "[DI-55] " + "bag-uuid null");
			uuid = UUID.randomUUID().toString();
			PDC.setString(bag, "uuid", uuid);
			//return;
		}
		
		ItemMeta meta = bag.getItemMeta();
		
		meta.setDisplayName(ownerless ? Lang.get("bag-ownerless-used") : Lang.parse(Lang.lang.getString("bag-bound-name"), player));
		if(PDC.has(bag, "name")) meta.setDisplayName(Lang.parse(PDC.getString(bag, "name"), player));
		
		List<String> lore = new ArrayList<String>() ;

		for (String l : Lang.lang.getStringList("bag-lore")) {
			lore.add(Lang.parse(l, player));
		}
		
		if(!ownerless) {
			placeholders.add(new Placeholder("%owner%", player.getName()));
			lore.add(Lang.parse(Lang.get("bound-to"), placeholders, player));
		}
		
		if(PDC.has(bag, "size")) {
			placeholders.add(new Placeholder("%size%", PDC.getInteger(bag, "size")));
			lore.add(Lang.parse(Lang.get("bag-size"), placeholders, player));
		}
		
		meta.setLore(lore);

		bag.setItemMeta(meta);
		PDC.setString(bag, "owner", ownerless ? "ownerless" : player.getUniqueId().toString());
		//PDC.SetString(bag, "bag-creator", player.getUniqueId().toString());
		PDC.setDouble(bag, "weight", 0.0);

		List<ItemStack> cont = new ArrayList<ItemStack>();
		for(int i = 0; i < HavenBags.findClosestNine(PDC.getInteger(bag, "size")); i++) {
			if(i < PDC.getInteger(bag, "size")) {
				cont.add(null);
			} else {
				ItemStack blocker = new ItemStack(Material.BARRIER);
				ItemMeta bm = blocker.getItemMeta();
				bm.setCustomModelData(99999);
				// hide tooltip
				if(Server.VersionHigherOrEqualTo(Server.Version.v1_20_5)) {
					bm.setHideTooltip(true);
				}
				bm.setDisplayName(" ");
				blocker.setItemMeta(bm);
				PDC.setBoolean(blocker, "locked", true);
				
				cont.add(blocker);
			}
		}
		
		if(ownerless) {
			BagData.createBag(uuid, "ownerless", cont, player, bag);
			Log.debug(Main.plugin, "[DI-56] " + "Ownerless bag created.");
			Log.debug(Main.plugin, "[DI-57] " + "Creating timestamp for " + uuid);
		}else {
			BagData.createBag(uuid, player.getUniqueId().toString(), cont, player, bag);
			Log.debug(Main.plugin, "[DI-58] " + "Bound new bag to: " + player.getName());
			Log.debug(Main.plugin, "[DI-59] " + "Creating timestamp for " + uuid);
		}

		//HavenBags.HasWeightLimit(bag);
		//HavenBags.UpdateBagItem(bag, null, player);
		return true;
	}
	
	@SuppressWarnings("unused")
	private void openBag(ItemStack bag, boolean ownerless, Player player, PlayerInteractEvent event) {
		//String uuid = PDC.GetString(bag, "bag-uuid");
		if(bag.getType() == Material.AIR) return;
		String uuid = HavenBags.getBagUUID(bag);
		Log.debug(Main.plugin, "[DI-226] " + "Opening " +  uuid);
		Data data = BagData.getBag(uuid, bag);
		//String owner = PDC.GetString(bag, "bag-owner");
		
		if(data == null) {
			// This bag doesnt exist and has possibly been removed.
			// Removing the item to prevent further issues.
			Log.error(Main.plugin, String.format("Player %s tried opening a removed bag, removing the item. (uuid: %s)",
					player.getName(), uuid));
			bag.setAmount(0);
			player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-does-not-exist"), null));
			return;
		}

		if(data.getViewer() != null && data.getViewer() == player) {
			player.sendMessage(String.format("viewer: %s", data.getViewer() == null ? "null" : data.getViewer().getName()));
			Log.debug(Main.plugin, "[DI-227] " + "This bag is already open by " + player.getName() + ".");
			event.setCancelled(true);
			return;
		}
		
		if(data.isOpen()) {
			if(data.getViewer() != null && data.getViewer() != player) {
				player.sendMessage(String.format("Open by: %s", data.getViewer() == null ? "null" : data.getViewer().getName()));
				player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-already-open"), null));
				Log.debug(Main.plugin, "[DI-60] " + "This bag is already open.");
				event.setCancelled(true);
				return;
			}
		}
		if(data.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
			Log.debug(Main.plugin, "[DI-64] " + "Attempting to open bag");
			
			try {
				event.setCancelled(true);
				HavenBags.hasWeightLimit(bag);
				HavenBags.updateBagItem(bag, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				Sound sound = new Sound(Main.config.getString("sound.open.key"),
		    			Main.config.getDouble("sound.open.volume"),
		    			Main.config.getDouble("sound.open.pitch"));
				sound.play(player);
			}catch(Exception e) {
				e.printStackTrace();
				BagData.markBagClosed(uuid);
			}
			return;
		}
		else if(ownerless) {
			Log.debug(Main.plugin, "[DI-62] " + "Attempting to open ownerless bag");
			try {
				event.setCancelled(true);
				HavenBags.hasWeightLimit(bag);
				HavenBags.updateBagItem(bag, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				Sound sound = new Sound(Main.config.getString("sound.open.key"),
		    			Main.config.getDouble("sound.open.volume"),
		    			Main.config.getDouble("sound.open.pitch"));
				sound.play(player);
			}catch(Exception e) {
				e.printStackTrace();
				BagData.markBagClosed(uuid);
			}
			return;
		}
		else if (player.hasPermission("havenbags.bypass") && !ownerless) {
			try {
				event.setCancelled(true);
				HavenBags.hasWeightLimit(bag);
				HavenBags.updateBagItem(bag, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				//BagData.MarkBagOpen(uuid, bag, player, gui);
				Sound sound = new Sound(Main.config.getString("sound.open.key"),
		    			Main.config.getDouble("sound.open.volume"),
		    			Main.config.getDouble("sound.open.pitch"));
				sound.play(player);
				Log.debug(Main.plugin, "[DI-65] " + player + "has attempted to open a bag, bypassing the lock");
			}catch(Exception e) {
				e.printStackTrace();
				BagData.markBagClosed(uuid);
			}
			return;
		}
		else if(data.isPlayerTrusted(player.getName()) && !ownerless) {
			try {
				event.setCancelled(true);
				HavenBags.hasWeightLimit(bag);
				HavenBags.updateBagItem(bag, player);
				BagGUI gui = new BagGUI(Main.plugin, data.getSize(), player, bag, bag.getItemMeta());
				//BagData.MarkBagOpen(uuid, bag, player, gui);
				Sound sound = new Sound(Main.config.getString("sound.open.key"),
		    			Main.config.getDouble("sound.open.volume"),
		    			Main.config.getDouble("sound.open.pitch"));
				sound.play(player);
			}catch(Exception e) {
				e.printStackTrace();
				BagData.markBagClosed(uuid);
			}
			return;
		}
		else{
			//player.sendMessage(Name + "§c You cannot use this bag.");
			player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-cannot-use"), null));
			event.setCancelled(true);
			return;
		}
	}
	
	public static int getPlayerBagLimit(Player player) {
        int highestNumber = 0;
	    for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
	        String permName = perm.getPermission();

	        if (permName.startsWith("havenbags.max.")) {
	            try {
	            	int num = Integer.parseInt(permName.substring("havenbags.max.".length())); // Extract full number
	                if(num > highestNumber) highestNumber = num;
	            } catch (Exception e) {
	                return Main.config.getInt("max-bags"); // Fallback if parsing fails
	            }
	        }
	     
	    }
	    return highestNumber != 0 ? highestNumber : Main.config.getInt("max-bags");
	}
	
	public boolean creationLimit(Player player) {
		if(Main.config.getInt("max-bags") > 0) {
			if(!player.hasPermission("havenbags.bypass")) {
				int limit = getPlayerBagLimit(player);
				if(BagData.getBags(player.getUniqueId().toString()).size() >= limit) {
					return true;
				}
			}
		}
		return false;
	}

}
