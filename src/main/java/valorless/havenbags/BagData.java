package valorless.havenbags;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import org.json.JSONException;
import valorless.annotations.Internal;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.havenbags.annotations.Nullable;
import valorless.havenbags.database.Files;
import valorless.havenbags.database.MySQL;
import valorless.havenbags.database.SQLite;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.enums.DatabaseType;
import valorless.havenbags.events.BagCreateEvent;
import valorless.havenbags.events.BagDeleteEvent;
import valorless.havenbags.gui.BagGUI;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Reflect;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.nbtapi.iface.ReadWriteNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBTList;

public class BagData {
	
	private static DatabaseType database = DatabaseType.SQLITE;
	private static MySQL mysql;
	static SQLite sqlite;
	
	static BukkitRunnable autosave;
	
	public enum UpdateSource { NULL, PLAYER }
	
	private static HashMap<UUID, Data> data = new HashMap<UUID, Data>();

	@Internal
	public static HashMap<UUID, Data> changedBags = new HashMap<UUID, Data>();
	public static long interval;
	
	private static boolean ready = false;

	public static boolean isReady() {
		return ready;
	}
	
	public static void init() {
		data.clear(); // Just in case
		DatabaseType type = DatabaseType.get(Main.config.getString("save-type").toUpperCase());
		if(type != null) setDatabase(type);
		else {
			Log.error(Main.plugin, String.format("Invalid database type \"%s\"\n"
					+ "Please choose either FILES, MYSQL, or SQLITE.", Main.config.getString("save-type")));
			Bukkit.getPluginManager().disablePlugin(Main.plugin);
		}
		
		if(getDatabase() == DatabaseType.MYSQL || getDatabase() == DatabaseType.MYSQLPLUS) {
			setMysql(new MySQL());
		}
		else if(getDatabase() == DatabaseType.SQLITE) {
			sqlite = new SQLite();
		}
		
		interval = Main.config.getInt("auto-save.interval")*20;
		loadData();
		/*
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
            @Override
            public void run() {
            	SaveData(false, null);
            }
        }, interval, interval);*/
		
		if(getDatabase() == DatabaseType.MYSQLPLUS) return;
		
		autosave = new BukkitRunnable() {
		    @Override
		    public void run() {
            	saveData(false);
		    }
		};

		autosave.runTaskTimer(Main.plugin, interval, interval);
		//Log.info(Main.plugin, "Loaded bags: " + data.size());
	}
	
	public static void shutdown() {
		if(getDatabase() == DatabaseType.MYSQL) {
			try {
				if(getMysql() != null) {
					getMysql().disconnect();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(getDatabase() == DatabaseType.SQLITE) {
			try {
				if(sqlite != null) {
					sqlite.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(getDatabase() == DatabaseType.MYSQLPLUS) {
			try {
				if(getMysql() != null) {
					getMysql().disconnect();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void changeDatabase(DatabaseType type) {
		if(getDatabase() == DatabaseType.MYSQL || getDatabase() == DatabaseType.MYSQLPLUS) {
			try {
				getMysql().disconnect();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(getDatabase() == DatabaseType.SQLITE) {
			try {
				sqlite.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		setDatabase(type);
		autosave = new BukkitRunnable() {
		    @Override
		    public void run() {
            	saveData(false);
		    }
		};

		autosave.runTaskTimer(Main.plugin, interval, interval);
		
		if(type == DatabaseType.MYSQL || type == DatabaseType.MYSQLPLUS) {
			setMysql(new MySQL());
			autosave.cancel();
		}
		else if(type == DatabaseType.SQLITE) {
			sqlite = new SQLite();
		}
		
		if(type == DatabaseType.MYSQLPLUS) {
			autosave.cancel();
		}
		
	}
	
	public static void reload() {
		interval = Main.config.getInt("auto-save.interval");
		Log.info(Main.plugin, "Bag data was not reloaded. You can force bag data reload with /havenbags reload force");
		Log.warning(Main.plugin, "Any unsaved bag data will be lost!");
	}
	
	public static void forceReload() {
		loadData();
	}
	
	public static class Bag {
		public ItemStack item;
		public List<ItemStack> content = new ArrayList<ItemStack>();
		
		public Bag (ItemStack item, List<ItemStack> content) {
			this.item = item;
			this.content = content;
		}
	}
	
	public static boolean contains(@NotNull String uuid) {
        return getBag(uuid, null) != null;
	}

	public static Boolean bagExists(@NotNull String uuid) {
		if("null".equalsIgnoreCase(uuid)) {
			return false;
		}

		try {
			UUID.fromString(uuid);
		}catch(IllegalArgumentException e) {
			return false;
		}
		return data.containsKey(UUID.fromString(uuid));
	}
	
	public static Data getBag(@NotNull String uuid, @Nullable ItemStack bagItem, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		
		Data bag = getbag(uuid);
		if(bag != null) {
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(true);
			}
			return bag;
		}
		Log.debug(Main.plugin, String.format("Failed to get bag '%s', this bag was not found.", uuid));
		Log.debug(Main.plugin, "If you keep seeing this error, please replace the bag causing it.");
		//if(bagItem != null) bagItem.setAmount(0);
		return null;
	}
	
	private static Data getbag(String uuid) {
		
		if("null".equalsIgnoreCase(uuid)) {
			//Log.error(Main.plugin, "Attempted to get bag with UUID 'null'.");
			return null;
		}
		
		try {
			UUID.fromString(uuid);
		}catch(IllegalArgumentException e) {
			Log.error(Main.plugin, String.format("Invalid UUID format: '%s'.", uuid));
			return null;
		}
		
		return data.get(UUID.fromString(uuid));
	}
	
	public static void updateBag(@NotNull String uuid, @NotNull List<ItemStack> content, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		
		Data bag = getBag(uuid, null);
		
		if(bag == null) Log.error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
		
		try {
			//bag.setContent(content);
			bag.setContent(content);
			//bag.data.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(false);
			}
		}catch(Exception e) {
			Log.error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void updateBag(@NotNull ItemStack bagItem, @NotNull List<ItemStack> content, UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		String uuid = PDC.getString(bagItem, "uuid");
		
		Data bag = getBag(uuid, null);
		
		if(bag == null) Log.error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
		
		try {
			//bag.setContent(content);
			bag.setContent(content);
			//bag.data.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
			
			if(bagItem.getType() == Material.PLAYER_HEAD) {
				bag.setTexture(getTextureValue(bagItem));
				bag.setModeldata(0);
			}else {
				if(bagItem.hasItemMeta()) {
					if(bagItem.getItemMeta().hasCustomModelData()) {
						bag.setModeldata(bagItem.getItemMeta().getCustomModelData());
					}else {
						bag.setModeldata(0);
					}
					if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
						if(ItemUtils.GetItemModel(bagItem) != null) {
							bag.setItemmodel(ItemUtils.GetItemModel(bagItem).toString());
						}else {
							bag.setItemmodel("");
						}
					}else {
						bag.setItemmodel("");
					}
				}else {
					bag.setModeldata(0);
					bag.setItemmodel("");
				}
				bag.setTexture(Main.config.GetString("bag.texture"));
			}
			//bag.data.Set("texture", getTextureValue(bagItem));
			 
			 
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(false);
			}
			

			if(Main.config.getBool("capacity-based-textures.enabled")) {
				bag.setTexture(HavenBags.capacityTexture(bagItem, content));
				//bag.setTexture(HavenBags.CapacityTexture(bagItem, content));
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
		            @Override
		            public void run(){
		            	HavenBags.updateBagItem(bagItem, bag.getViewer());
		            }
		        }, 1L);
				
			}
		}catch(Exception e) {
			Log.error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
			e.printStackTrace();
			if(m_source == UpdateSource.PLAYER) {
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static Data createBag(@NotNull String uuid, @NotNull String owner, @NotNull List<ItemStack> content, Player creator, ItemStack bag) {
		Data dat = new Data(uuid, owner, bag.getType());
		dat.setContent(content);
		if(creator != null)	dat.setCreator(creator.getUniqueId().toString());
		int size = 0;
		for(ItemStack item : content) {
			if(!PDC.has(item, "locked")) size++;
		}
		//dat.setSize(PDC.GetInteger(bag, "size"));
		dat.setSize(size);
		if(bag.getType() == Material.PLAYER_HEAD) {
			dat.setTexture(getTextureValue(bag));
			dat.setModeldata(0);
			dat.setItemmodel(null);
		}else {
			dat.setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=");
			if(bag.hasItemMeta()) {
				if(bag.getItemMeta().hasCustomModelData()) {
					dat.setModeldata(bag.getItemMeta().getCustomModelData());
				}

				if(Server.VersionHigherOrEqualTo(Version.v1_21_2)) {
					if(ItemUtils.GetItemModel(bag) != null) {
						dat.setItemmodel(ItemUtils.GetItemModel(bag).toString());
					}else {
						dat.setItemmodel("");
					}
				}else {
					dat.setItemmodel("");
				}
			}
		}
		dat.setTrusted(new ArrayList<String>());
		
		if(PDC.has(bag, "filter")) {
			dat.setAutopickup(PDC.getString(bag, "filter"));
		}else {
			dat.setAutopickup("null");
		}
		
		if(PDC.has(bag, "blacklist")) {
			dat.setBlacklist(PDC.getStringList(bag, "blacklist"));
			dat.setWhitelist(PDC.getBoolean(bag, "whitelist"));
			dat.setIgnoreGlobalBlacklist(PDC.getBoolean(bag, "igb"));
		}
		
		if(PDC.has(bag, "tooltip")) {
			dat.setTooltipStyle(PDC.getString(bag, "tooltip"));
		}else {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {
				if(bag.getItemMeta().hasTooltipStyle()) {
					dat.setTooltipStyle(bag.getItemMeta().getTooltipStyle().toString());
				}
			}
		}
		
		dat.setChanged(true);
		data.put(UUID.fromString(uuid), dat);
		if(database == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				getMysql().saveBag(dat);
			});
		}
		Log.debug(Main.plugin, "[DI-30] " + "New bag data created: " + owner + "/" + uuid);
		Bukkit.getPluginManager().callEvent(new BagCreateEvent(creator, bag, dat));
		return dat;
	}
	
	public static Data createBag(@NotNull Data dat) {
		if(dat == null) throw new IllegalArgumentException("Data cannot be null");
		dat.setChanged(true);
		data.put(UUID.fromString(dat.getUuid()), dat);
		if(database == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				getMysql().saveBag(dat);
			});
		}
		Log.debug(Main.plugin, "[DI-30] " + "New bag data created: " + dat.getOwner() + "/" + dat.getUuid());
		Player creator = Bukkit.getPlayer(UUID.fromString(dat.getCreator()));
		Bukkit.getPluginManager().callEvent(new BagCreateEvent(creator, null, dat));
		return dat;
	}
	
	public static void loadData(){
		ready = false;
		Log.info(Main.plugin, "Loading bags..");
		long startTime = System.currentTimeMillis();
		int i = 0;
		if(getDatabase() == DatabaseType.FILES) {
			List<String> owners	= getBagOwners();
			for(String owner : owners) {
				List<String> bags = Files.getBags(owner);
				for(String bag : bags) {
					String path = String.format("%s/bags/%s/%s.yml", Main.plugin.getDataFolder(), owner, bag);

					File bagData;
					try {
						bagData = new File(path);
					} catch(Exception e) {
						e.printStackTrace();
						continue;
					}
					if(!bagData.exists()) {
						continue;
					}
					try {
						Data dat = Files.loadBag(owner, bag);
						data.put(UUID.fromString(dat.getUuid()), dat);
						i++;
					} catch (Exception e) {
						Log.error(Main.plugin, bag);
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		else if(getDatabase() == DatabaseType.MYSQL) {
			HashMap<UUID, Data> bags = getMysql().loadAllBags();
			data = bags;
			i = bags.size();
		}
		else if(getDatabase() == DatabaseType.SQLITE) {
			for(String uuid : sqlite.getAllBagUUIDs()) {
				Data bag = sqlite.loadBag(uuid);
				data.put(UUID.fromString(uuid), bag);
				i++;
			}
		}
		else if(getDatabase() == DatabaseType.MYSQLPLUS) {
			HashMap<UUID, Data> bags = getMysql().loadAllBags();
			data = bags;
			i = bags.size();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.info(Main.plugin, String.format("Loaded %s bags. %sms", i, duration));
		ready = true;
	}
	
	public static void saveData(boolean shutdown, boolean... conversion) {
		long startTime = System.currentTimeMillis();
		List<Data> toSave = new ArrayList<>();
		
		if(shutdown || conversion != null) {
			toSave.addAll(data.values());
		}
		
		//Iterator<Map.Entry<UUID, Data>> iterator = changedBags.entrySet().iterator();
		//while (iterator.hasNext()) {
		//	Map.Entry<UUID, Data> entry = iterator.next();
		//	Data bag = entry.getValue();
		//    toSave.add(bag);
		//    iterator.remove();
		//    bag.setChanged(false);
		//}

		List<UUID> keys = new ArrayList<>(changedBags.keySet());
		for(UUID uuid : keys) {
			if(uuid == null) continue;
			Data dat = changedBags.get(uuid);
			toSave.add(dat);
			dat.setChanged(false);
		}

		/*
		for(Map.Entry<UUID, Data> entry : changedBags.entrySet()) {
			try {
				Data bag = entry.getValue();
				toSave.add(bag);
				bag.setChanged(false);
			}catch(Exception e) {
				Log.error(Main.plugin, String.format("Failed to save bag '%s'.", entry.getKey().toString()));
				e.printStackTrace();
				String msg = e.getMessage();
				msg += "\n Cause: " + e.getCause();
				for(int i = 0; i < Utils.Clamp(e.getStackTrace().length,0,4); i++)
					msg += " " + e.getStackTrace()[i];
				ErrorLog.addLog("Failed to save bag '" + entry.getKey().toString() + "': " + msg);
				continue;
			}
		}
		*/

		if(toSave.isEmpty()) {
			return;
		}
		if(Main.config.getBool("auto-save.message") || shutdown) Log.info(Main.plugin, "Saving bags..");
		for(Data bag : toSave) {
			String uuid = bag.getUuid();
	    	String owner = bag.getOwner();
	    	
	    	if(getDatabase() == DatabaseType.FILES) {
	        	Log.debug(Main.plugin, "[DI-31] [FILES] " + "Attempting to write bag " + owner + "/" + uuid + " onto server");
	    		Files.saveBag(bag);
	    	}
	    	else if(getDatabase() == DatabaseType.SQLITE) {
	    		Log.debug(Main.plugin, "[DI-231] [SQLITE] " + "Attempting to write bag " + owner + "/" + uuid + " onto database");
	    		if(shutdown || conversion != null) {
	    			sqlite.saveBag(bag);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
	    				sqlite.saveBag(bag);
	    			});
	    		}
	    	}else 
		    	if(getDatabase() == DatabaseType.MYSQLPLUS) {
		    		if(!shutdown && conversion == null) {
		    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
		    				getMysql().saveBag(bag);
		    			});
		    		}
		    	}
		}

		if(!toSave.isEmpty()) {
	    	if(getDatabase() == DatabaseType.MYSQL) {
	    		Log.debug(Main.plugin, "[DI-232] [MYSQL] " + "Attempting to write bags onto database");
	    		if(shutdown || conversion != null) {
	    			for(List<Data> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
	    				getMysql().saveBags(chunk);
	    			}
	    			//getMysql().saveBags(toSave);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
		    			for(List<Data> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
		    				getMysql().saveBags(chunk);
		    			}
	    				//getMysql().saveBags(toSave);
	    			});
	    		}
	    	}
	    	else if(getDatabase() == DatabaseType.MYSQLPLUS) {
	    		Log.debug(Main.plugin, "[DI-233] [MYSQLPLUS] " + "Attempting to write bags onto database");
	    		if(shutdown || conversion != null) {
	    			for(List<Data> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
	    				getMysql().saveBags(chunk);
	    			}
	    			//getMysql().saveBags(toSave);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
		    			for(List<Data> chunk : mysql.chunkify(toSave, mysql.getMaxChunkSize())) {
		    				getMysql().saveBags(chunk);
		    			}
	    				//getMysql().saveBags(toSave);
	    			});
	    		}
	    	}
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		if(Main.config.getBool("auto-save.message") || shutdown) Log.info(Main.plugin, String.format("Saved %s bags. %sms", toSave.size(), duration));
	}
	
	public static void removeBag(@NotNull String uuid) {
		UUID uid = UUID.fromString(uuid);
		Data bag = data.get(uid);
		if(bag != null) {
			data.remove(uid);
            changedBags.remove(uid);
			Log.info(Main.plugin, String.format("Removed cached data for %s.", uuid));
			return;
		}
		Log.error(Main.plugin, String.format("Failed to remove cached data for %s.", uuid));
	}
	
	public static Boolean deleteBag(@NotNull String uuid, @Nullable Player... player) {
		Data bag = getbag(uuid);
		if(bag != null) {
			if(getDatabase() == DatabaseType.FILES) {
				try {
					Files.deleteFile(bag.getOwner(), uuid);
				}catch(Exception e) {
					Log.error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
					e.printStackTrace();
					return false;
				}
			}else if(getDatabase() == DatabaseType.MYSQL) {
	    		getMysql().deleteBag(uuid);
	    	}
	    	else if(getDatabase() == DatabaseType.SQLITE) {
	    		sqlite.deleteBag(uuid);
	    	}

			if(player != null && player.length > 0) {
				Bukkit.getPluginManager().callEvent(new BagDeleteEvent(player[0], bag.clone()));
			}else {
				Bukkit.getPluginManager().callEvent(new BagDeleteEvent(null, bag.clone()));
			}
			removeBag(uuid);
            changedBags.remove(UUID.fromString(uuid));
			Log.info(Main.plugin, String.format("Deleted data for %s.", uuid));
			return true;
		}
		Log.error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
		return false;
	}
	
	public static List<String> getBags(@NotNull String playerUUID) {
	    Log.debug(Main.plugin, "[DI-32] " + playerUUID);
	    return data.values().stream()
	        .filter(dat -> dat.getOwner().equals(playerUUID))
	        .map(Data::getUuid)
	        .toList();
	}
	
	public static List<Data> getBagsData(@NotNull String playerUUID) {
	    Log.debug(Main.plugin, "[DI-260] " + playerUUID);
	    return data.values().stream()
	        .filter(dat -> dat.getOwner().equals(playerUUID))
	        .toList();
	}
	
	public static List<String> getBagOwners(){
		if(getDatabase() == DatabaseType.FILES) {
			try {
				return Stream.of(new File(String.format("%s/bags/", Main.plugin.getDataFolder())).listFiles())
						.filter(File::isDirectory)
						.map(File::getName)
						.toList();
			} catch (Exception e) {
				return new ArrayList<String>();
			}
		}else if(getDatabase() == DatabaseType.MYSQL) {
			return getMysql().getBagOwners();
    	}
    	else if(getDatabase() == DatabaseType.SQLITE) {
    		return sqlite.getBagOwners();
    	}
		
		return new ArrayList<String>();
	}
	
	public static boolean isBagOpen(@NotNull String uuid, ItemStack bagItem) {
		Data bag = getbag(uuid);
		if(bag != null) {
			return bag.isOpen();
		}
		Log.error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
		return false;
	}
	
	public static boolean isBagOpen(ItemStack bagItem) {
		if(BagState.getState(bagItem) != BagState.USED) return false;
		String uuid = HavenBags.getBagUUID(bagItem);
		if("null".equalsIgnoreCase(uuid)) {
			return false;
		}
		
		try {
			UUID.fromString(uuid);
		}catch(IllegalArgumentException e) {
			return false;
		}
		Data bag = getbag(uuid);
		if(bag != null) {
			return bag.isOpen();
		}
		return false;
	}
	
	public static Player bagOpenBy(@NotNull String uuid, ItemStack bagItem) {
		Data bag = getbag(uuid);
		if(bag != null) {
			if(isBagOpen(uuid, bagItem)) {
				return bag.getViewer();
			}else return null;
		}
		Log.error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
		return null;
	}
	
	public static void markBagOpen(@NotNull String uuid, ItemStack bagItem, Player player) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setOpen(true);
			bag.setViewer(player);
			if(getDatabase() == DatabaseType.MYSQLPLUS) {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					getMysql().saveBag(bag);
				});
			}
			return;
		}
		Log.error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}
	
	public static void markBagOpen(@NotNull String uuid, ItemStack bagItem, Player player, BagGUI gui) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setOpen(true);
			bag.setViewer(player);
			bag.setGui(gui);
			if(getDatabase() == DatabaseType.MYSQLPLUS) {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					getMysql().saveBag(bag);
				});
			}
			return;
		}
		Log.error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}
	
	public static void markBagClosed(@NotNull String uuid) {
		Data bag = getBag(uuid, null); // This will throw an error if the bag does not exist, which is fine.
		if(bag == null) {
			Log.error(Main.plugin, String.format("Failed to mark bag '%s' as closed, this bag was not found.", uuid));
			return;
		}
		bag.setOpen(false);
		bag.setViewer(null);
		bag.setGui(null);
		if(getDatabase() == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				getMysql().saveBag(bag);
			});
		}
	}
	
	public List<String> getTrusted(@NotNull String uuid) {
		Data bag = getbag(uuid);
		return bag != null ? bag.getTrusted() : null;
	}
	
	public static String getOwner(@NotNull String uuid) {
		Data bag = getbag(uuid);
		return bag != null ? bag.getOwner() : null;
	}
	
	public static String getCreator(@NotNull String uuid) {
		Data bag = getbag(uuid);
		return bag != null ? bag.getCreator() : null;
	}
	
	public static void addTrusted(@NotNull String uuid, @NotNull String player) {
		Data bag = getbag(uuid);
		if(bag != null) {
			List<String> trusted = bag.getTrusted();
			if(!trusted.contains(player)) {
				trusted.add(player);
				bag.setTrusted(trusted);
				bag.setChanged(true);
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
			return;
		}
	}
	
	public static void removeTrusted(@NotNull String uuid, @NotNull String player) {
		Data bag = getbag(uuid);
		if(bag != null) {
			List<String> trusted = bag.getTrusted();
			if(trusted.isEmpty()) return;
			for(int i = 0; i < trusted.size(); i++) {
				if(trusted.get(i).equalsIgnoreCase(player)) {
					trusted.remove(i);
					bag.setTrusted(trusted);
					bag.setChanged(true);
					if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
				}
			}
			return;
		}
	}
	
	public static void setAutoPickup(@NotNull String uuid, @NotNull String filter) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setAutopickup(filter);
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			return;
		}
	}
	
	public static String getAutoPickup(@NotNull String uuid) {
		Data bag = getbag(uuid);
		return bag != null ? bag.getAutopickup() : null;
	}
	
	public static void removeAutoPickup(@NotNull String uuid) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setAutopickup("null");
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			return;
		}	
	}
	
	public static void setWeight(@NotNull String uuid, @NotNull double weight) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setWeight(weight);
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			return;
		}
	}
	
	public static void setWeightMax(@NotNull String uuid, @NotNull double weightmax) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setWeightMax(weightmax);
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			return;
		}
	}
	
	@SuppressWarnings("unused")
	private void markBagChanged(@NotNull String uuid) {
		Data bag = getbag(uuid);
		if(bag != null) {
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			return;
		}
	}

	public static String getTextureValue(Data bag) {
		return bag.getTexture();
	}
	
	public static String getTextureValue(ItemStack head) {
        if (head == null || head.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("ItemStack must be a Player Head");
        }
        
        if(Server.VersionHigherOrEqualTo(Version.v1_21_1)) {
        	SkullMeta meta = (SkullMeta) head.getItemMeta();
			//if(meta.getOwnerProfile().getTextures().getSkin() == null){
			//	return getbag(HavenBags.GetBagUUID(head)).getTexture();
			//}
        	return HeadCreator.convertUrlToBase64(meta.getOwnerProfile().getTextures().getSkin().toString());
        }else {

        	// Use NBTAPI to access the NBT data
        	if (!valorless.valorlessutils.nbtapi.NBT.readNbt(head).hasTag("SkullOwner")) {
            	return null;
        	}

        	// Access the SkullOwner NBT compound
        	//NBTCompound skullOwner = nbti.getCompound("SkullOwner");
        	ReadableNBT skullOwner = valorless.valorlessutils.nbtapi.NBT.readNbt(head).getCompound("SkullOwner");
        	if (skullOwner == null || !skullOwner.hasTag("Properties")) {
            	return null;
        	}

        	// Access the Properties NBT compound
        	ReadableNBT properties = skullOwner.getCompound("Properties");
        	if (properties == null || !properties.hasTag("textures")) {
            	return null;
        	}

        	// Access the textures NBT list
        	ReadableNBTList<ReadWriteNBT> textures = properties.getCompoundList("textures");
        	if (textures == null || textures.isEmpty()) {
        		return null;
        	}

        	// Get the first texture compound
        	ReadWriteNBT texture = textures.get(0);
        	if (texture == null || !texture.hasTag("Value")) {
            	return null;
        	}

        	// Return the texture value
        	return texture.getString("Value");
        }
    }

	public static void setTextureValue(@NotNull ItemStack item, @NotNull String value) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        
        //if (!(item.getItemMeta() instanceof SkullMeta meta)) return;
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        UUID uuid = UUID.nameUUIDFromBytes(value.getBytes());
        
        if(Server.VersionHigherOrEqualTo(Version.v1_21_1) || Server.VersionEqualTo(Version.NULL)) {
        	try {
            	// Create a new GameProfile with a random UUID and apply the texture
            	PlayerProfile profile = Bukkit.getServer().createPlayerProfile(uuid, "bag");
            	PlayerTextures textures = profile.getTextures();
				try {
					textures.setSkin(new URL(HeadCreator.extractUrlFromBase64(value)));
				}catch (JSONException e) { return; }
            	profile.setTextures(textures);

            	// Use the API method to set the profile (this method was introduced in recent Spigot versions)
            	meta.setOwnerProfile(profile);
            }catch(Exception E) {
            	E.printStackTrace();
            }
        }else {
        	try {
        		GameProfile profile = new GameProfile(uuid, "null");
        		profile.getProperties().put("textures", new Property("textures", value));

        		Method method = Reflect.getMethod(meta.getClass(), "setProfile", GameProfile.class);
        		if (method != null) {
        			Reflect.invokeMethod(method, meta, profile);
        		} else {
            		Reflect.setFieldValue(meta, "profile", profile);
        		}
        	}catch(Exception e) {}
        }

        item.setItemMeta(meta);
    }
	
	public static List<Data> getOpenBags() {
	    return data.values().stream()
	               .filter(Data::isOpen)
	               .toList();
	}
	
	public static List<JsonObject> deserializeItemStackList(String json) {
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

        List<JsonObject> itemList = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            if (!element.isJsonNull()) { // Ignore null values
                itemList.add(element.getAsJsonObject());
            }else {
            	itemList.add(null);
            }
        }

        return itemList;
    }
	
	public static Boolean clearAllBagContents() {
		try {
			for(Data dat : data.values()) {
				clearBagContent(dat.getUuid());
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean clearBagContentPlayer(@NotNull String playeruuid) {
		try {
			for(String bag : getBags(playeruuid)) {
				clearBagContent(bag);
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean clearBagContent(@NotNull String uuid) {
		Data bag = getbag(uuid);
		if(bag != null) {
			if(bag.getGui() != null) {
				bag.getGui().close(true);
			}
			
			bag.setContent(new ArrayList<>(Collections.nCopies(bag.getContent().size(), null)));
			Log.info(Main.plugin, String.format("Cleared content for %s.", uuid));
			return true;
		}
		Log.error(Main.plugin, String.format("Failed to clear content for %s.", uuid));
		return false;
	}

	public static DatabaseType getDatabase() {
		return database;
	}

	protected static void setDatabase(DatabaseType database) {
		BagData.database = database;
	}

	public static MySQL getMysql() {
		return mysql;
	}

	protected static void setMysql(MySQL mysql) {
		BagData.mysql = mysql;
	}

	/**
	 * Reset the tooltip-styles of ALL bags to the default one specified in the config.<br>
	 * This is used when the tooltip-style is changed in the config, to update all bags to the new style.
	 * <p>
	 * If the server is a version that does not support TooltipStyle, then all are set null.
	 */
	@DoNotCall("This method is used internally to reset the tooltip-styles of all bags to the value in config.yml. It should not be called outside of HavenBags.")
	public static void resetTooltipStyles() {
		for (Data data : data.values()) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {
				data.setTooltipStyle(Main.config.getString("bag.tooltip-style"));
			}else {
				data.setTooltipStyle(null);
			}
		}
		// Forcefully save all changes
		saveData(true);
	}
}
