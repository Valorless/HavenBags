package valorless.havenbags.database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.nbt.NBT;

public class BagCache implements Listener {

	protected static final HashMap<UUID, Bag> cache = new HashMap<>();
	
	public static void reload() {
		cache.clear();
		for(Player player : Bukkit.getOnlinePlayers()) {
			Observer.checkInventory(player.getInventory().getContents());
		}
	}

	public static boolean remove(UUID key) {
		try {
			cache.remove(key);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static Bag get(UUID key) {
		return cache.get(key);
	}

	public static boolean containsKey(UUID key) {
		return cache.containsKey(key);
	}

	public static boolean containsData(Bag data) {
		return cache.containsValue(data);
	}
	
	public static List<Bag> getAllBags(Player player) {
	    String uuid = player.getUniqueId().toString();
	    return cache.values().stream()
	        .filter(data -> uuid.equals(data.getOwner()))
	        .toList();
	}
	
	public static List<Bag> getPlayerBagsFromInventory(Player player) {
	    ItemStack[] contents = player.getInventory().getContents();

	    return Arrays.stream(contents)
	        .map(HavenBags::getBagUUID) // returns String or null if not a bag
	        .filter(Objects::nonNull)
	        .distinct()
	        .map(uuidStr -> {
	            try {
	                return UUID.fromString(uuidStr);
	            } catch (IllegalArgumentException e) {
	                // invalid UUID string, ignore this entry
	                return null;
	            }
	        })
	        .filter(Objects::nonNull)
	        .map(cache::get)
	        .filter(Objects::nonNull)
	        .toList();
	}
	
	public static List<Bag> getOwnerlessBags() {
	    return cache.values().stream()
	        .filter(data -> "ownerless".equalsIgnoreCase(data.getOwner()))
	        .toList();
	}

	// Events

	public static class Observer implements Listener {
		public static void init() {
			Log.debug(Main.plugin, "[DI-257] Registering BagCache.Observer");
			Bukkit.getServer().getPluginManager().registerEvents(new Observer(), Main.plugin);
			reload();
		}

		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent e) {
			Log.debug(Main.plugin, "[DI-258] [BagCache] Checking for bags on " + e.getPlayer().getName() + ".");
			checkInventory(e.getPlayer().getInventory().getContents());
		}

		@EventHandler
		public void onInventoryOpen(InventoryOpenEvent e) {
			checkInventory(e.getInventory().getContents());
		}

		@EventHandler
		public void onInventoryClose(InventoryCloseEvent e) {
			checkInventory(e.getInventory().getContents());
		}
		
		public static void checkInventory(ItemStack[] content) {
			for(ItemStack item : content) {
				if(item == null) continue;
				try {
					if(NBT.Has(item, "bag-uuid") && !NBT.GetString(item, "bag-uuid").equalsIgnoreCase("yes")) {
						Converter.convertBag(item);
						cache.put(NBT.GetUUID(item, "bag-uuid"), Database.getBag(NBT.GetString(item, "bag-uuid"), null));
					}
					if(NBT.Has(item, "bag-token-skin")) {
						Converter.convertToken(item);
					}
				}catch(Exception E) { continue; }
				
				if(PDC.has(item, "uuid")) {
					cache.put(PDC.getUUID(item, "uuid"), Database.getBag(PDC.getString(item, "uuid"), null));
				}
			}
		}
		
		public static void checkItem(ItemStack item) {
			if(item == null) return;
			try {
				if(NBT.Has(item, "bag-uuid") && !NBT.GetString(item, "bag-uuid").equalsIgnoreCase("yes")) {
					Converter.convertBag(item);
					cache.put(NBT.GetUUID(item, "bag-uuid"), Database.getBag(NBT.GetString(item, "bag-uuid"), null));
				}
				if(NBT.Has(item, "bag-token-skin")) {
					Converter.convertToken(item);
				}
			}catch(Exception E) { return; }

			if(PDC.has(item, "uuid")) {
				cache.put(PDC.getUUID(item, "uuid"), Database.getBag(PDC.getString(item, "uuid"), null));
			}
		}
	}

	// Bag Converter (NBT -> PDC)

	public static class Converter {
		public static void convertBag(ItemStack bag) {
			Log.debug(Main.plugin, String.format("[DI-260] [BagCache] Converting Bag NBT => PDC %s", NBT.GetString(bag, "bag-uuid")));
			if(NBT.Has(bag, "bag-uuid"))
				PDC.setString(bag, "uuid", NBT.GetString(bag, "bag-uuid"));
				NBT.Remove(bag, "bag-uuid");
			if(NBT.Has(bag, "bag-owner"))
				PDC.setString(bag, "owner", NBT.GetString(bag, "bag-owner"));
				NBT.Remove(bag, "bag-owner");
			if(NBT.Has(bag, "bag-creator"))
				PDC.setString(bag, "creator", NBT.GetString(bag, "bag-creator"));
				NBT.Remove(bag, "bag-creator");
			if(NBT.Has(bag, "bag-size"))
				PDC.setinteger(bag, "size", NBT.GetInt(bag, "bag-size"));
			if(NBT.Has(bag, "bag-canBind"))
				PDC.setBoolean(bag, "binding", NBT.GetBool(bag, "bag-canBind"));
				NBT.Remove(bag, "bag-canBind");
			if(NBT.Has(bag, "bag-filter"))
				PDC.setString(bag, "filter", NBT.GetString(bag, "bag-filter"));
				NBT.Remove(bag, "bag-filter");
			if(NBT.Has(bag, "bag-weight"))
				PDC.setDouble(bag, "weight", NBT.GetDouble(bag, "bag-weight"));
				NBT.Remove(bag, "bag-weight");
			if(NBT.Has(bag, "bag-weight-limit"))
				PDC.setDouble(bag, "weight-limit", NBT.GetDouble(bag, "bag-weight-limit"));
				NBT.Remove(bag, "bag-weight-limit");
			if(NBT.Has(bag, "bag-trusted"))
				PDC.setStringList(bag, "trusted", NBT.GetStringList(bag, "bag-trusted"));
				NBT.Remove(bag, "bag-trusted");
			if(NBT.Has(bag, "bag-trust"))
				PDC.setStringList(bag, "trusted", NBT.GetStringList(bag, "bag-trust"));
				NBT.Remove(bag, "bag-trust");
			if(NBT.Has(bag, "bag-skin"))
				PDC.setBoolean(bag, "skin", NBT.GetBool(bag, "bag-skin"));
				NBT.Remove(bag, "bag-skin");
			if(NBT.Has(bag, "bag-upgrade"))
				PDC.setBoolean(bag, "upgrade", NBT.GetBool(bag, "bag-upgrade"));
				NBT.Remove(bag, "bag-upgrade");
			if(NBT.Has(bag, "bag-whitelist"))
				PDC.setBoolean(bag, "whitelist", NBT.GetBool(bag, "bag-whitelist"));
				NBT.Remove(bag, "bag-whitelist");
			if(NBT.Has(bag, "bag-blacklist"))
				PDC.setBoolean(bag, "blacklist", NBT.GetBool(bag, "bag-blacklist"));
				NBT.Remove(bag, "bag-blacklist");
			if(NBT.Has(bag, "bag-igb"))
				PDC.setBoolean(bag, "igb", NBT.GetBool(bag, "bag-igb"));
				NBT.Remove(bag, "bag-igb");
			if(NBT.Has(bag, "bag-name"))
				PDC.setString(bag, "name", NBT.GetString(bag, "bag-name"));
				NBT.Remove(bag, "bag-name");
			if(NBT.Has(bag, "bag-lore"))
				PDC.setString(bag, "lore", NBT.GetString(bag, "bag-lore"));
				NBT.Remove(bag, "bag-lore");
			if(NBT.Has(bag, "bag-predefined"))
				PDC.setString(bag, "predefined", NBT.GetString(bag, "bag-predefined"));
				NBT.Remove(bag, "bag-predefined");
		}

		public static void convertToken(ItemStack bag) {
			Log.debug(Main.plugin, String.format("[DI-261] [BagCache] Converting Token NBT => PDC %s", NBT.GetString(bag, "bag-token-type")));
			if(NBT.Has(bag, "bag-token-skin"))
				PDC.setString(bag, "token-skin", NBT.GetString(bag, "bag-token-skin"));
				NBT.Remove(bag, "bag-token-skin");
			if(NBT.Has(bag, "bag-token-type"))
				PDC.setString(bag, "token-type", NBT.GetString(bag, "bag-token-type"));
				NBT.Remove(bag, "bag-token-type");
		}
	}

}
