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

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;

public class BagCache implements Listener {

	protected static final HashMap<UUID, Data> cache = new HashMap<>();
	
	public static void reload() {
		cache.clear();
		for(Player player : Bukkit.getOnlinePlayers()) {
			Observer.CheckInventory(player.getInventory().getContents());
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

	public static Data get(UUID key) {
		return cache.get(key);
	}

	public static boolean containsKey(UUID key) {
		return cache.containsKey(key);
	}

	public static boolean containsData(Data data) {
		return cache.containsValue(data);
	}
	
	public static List<Data> getAllBags(Player player) {
	    String uuid = player.getUniqueId().toString();
	    return cache.values().stream()
	        .filter(data -> uuid.equals(data.getOwner()))
	        .toList();
	}
	
	public static List<Data> getPlayerBagsFromInventory(Player player) {
	    ItemStack[] contents = player.getInventory().getContents();

	    return Arrays.stream(contents)
	        .map(HavenBags::GetBagUUID) // returns String or null if not a bag
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
	        .map(uuid -> cache.get(uuid))
	        .filter(Objects::nonNull)
	        .toList();
	}
	
	public static List<Data> getOwnerlessBags() {
	    return cache.values().stream()
	        .filter(data -> "ownerless".equalsIgnoreCase(data.getOwner()))
	        .toList();
	}

	// Events

	public static class Observer implements Listener {
		public static void init() {
			Log.Debug(Main.plugin, "[DI-257] Registering BagCache.Observer");
			Bukkit.getServer().getPluginManager().registerEvents(new Observer(), Main.plugin);
			reload();
		}

		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent e) {
			Log.Debug(Main.plugin, "[DI-258] [BagCache] Checking for bags on " + e.getPlayer().getName() + ".");
			CheckInventory(e.getPlayer().getInventory().getContents());
		}

		@EventHandler
		public void onInventoryOpen(InventoryOpenEvent e) {
			CheckInventory(e.getInventory().getContents());
		}

		@EventHandler
		public void onInventoryClose(InventoryCloseEvent e) {
			CheckInventory(e.getInventory().getContents());
		}
		
		public static void CheckInventory(ItemStack[] content) {
			for(ItemStack item : content) {
				if(item == null) continue;
				try {
					if(NBT.Has(item, "bag-uuid") && !NBT.GetString(item, "bag-uuid").equalsIgnoreCase("yes")) {
						Converter.ConvertBag(item);
						cache.put(NBT.GetUUID(item, "bag-uuid"), BagData.GetBag(NBT.GetString(item, "bag-uuid"), null));
					}
					if(NBT.Has(item, "bag-token-skin")) {
						Converter.ConvertToken(item);
					}
				}catch(Exception E) { continue; }
				
				if(PDC.Has(item, "uuid")) {
					cache.put(PDC.GetUUID(item, "uuid"), BagData.GetBag(PDC.GetString(item, "uuid"), null));
				}
			}
		}
		
		public static void CheckItem(ItemStack item) {
			if(item == null) return;
			try {
				if(NBT.Has(item, "bag-uuid") && !NBT.GetString(item, "bag-uuid").equalsIgnoreCase("yes")) {
					Converter.ConvertBag(item);
					cache.put(NBT.GetUUID(item, "bag-uuid"), BagData.GetBag(NBT.GetString(item, "bag-uuid"), null));
				}
				if(NBT.Has(item, "bag-token-skin")) {
					Converter.ConvertToken(item);
				}
			}catch(Exception E) { return; }

			if(PDC.Has(item, "uuid")) {
				cache.put(PDC.GetUUID(item, "uuid"), BagData.GetBag(PDC.GetString(item, "uuid"), null));
			}
		}
	}

	// Bag Converter (NBT -> PDC)

	public class Converter {
		public static void ConvertBag(ItemStack bag) {
			Log.Info(Main.plugin, String.format("[DI-260] [BagCache] Converting Bag NBT => PDC %s", NBT.GetString(bag, "bag-uuid")));
			if(NBT.Has(bag, "bag-uuid"))
				PDC.SetString(bag, "uuid", NBT.GetString(bag, "bag-uuid"));
				NBT.Remove(bag, "bag-uuid");
			if(NBT.Has(bag, "bag-owner"))
				PDC.SetString(bag, "owner", NBT.GetString(bag, "bag-owner"));
				NBT.Remove(bag, "bag-owner");
			if(NBT.Has(bag, "bag-creator"))
				PDC.SetString(bag, "creator", NBT.GetString(bag, "bag-creator"));
				NBT.Remove(bag, "bag-creator");
			if(NBT.Has(bag, "bag-size"))
				PDC.SetInteger(bag, "size", NBT.GetInt(bag, "bag-size"));
			if(NBT.Has(bag, "bag-canBind"))
				PDC.SetBoolean(bag, "binding", NBT.GetBool(bag, "bag-canBind"));
				NBT.Remove(bag, "bag-canBind");
			if(NBT.Has(bag, "bag-filter"))
				PDC.SetString(bag, "filter", NBT.GetString(bag, "bag-filter"));
				NBT.Remove(bag, "bag-filter");
			if(NBT.Has(bag, "bag-weight"))
				PDC.SetDouble(bag, "weight", NBT.GetDouble(bag, "bag-weight"));
				NBT.Remove(bag, "bag-weight");
			if(NBT.Has(bag, "bag-weight-limit"))
				PDC.SetDouble(bag, "weight-limit", NBT.GetDouble(bag, "bag-weight-limit"));
				NBT.Remove(bag, "bag-weight-limit");
			if(NBT.Has(bag, "bag-trusted"))
				PDC.SetStringList(bag, "trusted", NBT.GetStringList(bag, "bag-trusted"));
				NBT.Remove(bag, "bag-trusted");
			if(NBT.Has(bag, "bag-trust"))
				PDC.SetStringList(bag, "trusted", NBT.GetStringList(bag, "bag-trust"));
				NBT.Remove(bag, "bag-trust");
			if(NBT.Has(bag, "bag-skin"))
				PDC.SetBoolean(bag, "skin", NBT.GetBool(bag, "bag-skin"));
				NBT.Remove(bag, "bag-skin");
			if(NBT.Has(bag, "bag-upgrade"))
				PDC.SetBoolean(bag, "upgrade", NBT.GetBool(bag, "bag-upgrade"));
				NBT.Remove(bag, "bag-upgrade");
			if(NBT.Has(bag, "bag-whitelist"))
				PDC.SetBoolean(bag, "whitelist", NBT.GetBool(bag, "bag-whitelist"));
				NBT.Remove(bag, "bag-whitelist");
			if(NBT.Has(bag, "bag-blacklist"))
				PDC.SetBoolean(bag, "blacklist", NBT.GetBool(bag, "bag-blacklist"));
				NBT.Remove(bag, "bag-blacklist");
			if(NBT.Has(bag, "bag-igb"))
				PDC.SetBoolean(bag, "igb", NBT.GetBool(bag, "bag-igb"));
				NBT.Remove(bag, "bag-igb");
			if(NBT.Has(bag, "bag-name"))
				PDC.SetString(bag, "name", NBT.GetString(bag, "bag-name"));
				NBT.Remove(bag, "bag-name");
			if(NBT.Has(bag, "bag-lore"))
				PDC.SetString(bag, "lore", NBT.GetString(bag, "bag-lore"));
				NBT.Remove(bag, "bag-lore");
			if(NBT.Has(bag, "bag-predefined"))
				PDC.SetString(bag, "predefined", NBT.GetString(bag, "bag-predefined"));
				NBT.Remove(bag, "bag-predefined");
		}

		public static void ConvertToken(ItemStack bag) {
			Log.Info(Main.plugin, String.format("[DI-261] [BagCache] Converting Token NBT => PDC %s", NBT.GetString(bag, "bag-token-type")));
			if(NBT.Has(bag, "bag-token-skin"))
				PDC.SetString(bag, "token-skin", NBT.GetString(bag, "bag-token-skin"));
				NBT.Remove(bag, "bag-token-skin");
			if(NBT.Has(bag, "bag-token-type"))
				PDC.SetString(bag, "token-type", NBT.GetString(bag, "bag-token-type"));
				NBT.Remove(bag, "bag-token-type");
		}
	}

}
