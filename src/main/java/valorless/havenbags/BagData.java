package valorless.havenbags;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import valorless.havenbags.HavenBags.BagState;
import valorless.havenbags.database.Files;
import valorless.havenbags.database.MySQL;
import valorless.havenbags.database.SQLite;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.enums.DatabaseType;
import valorless.havenbags.events.BagCreateEvent;
import valorless.havenbags.events.BagDeleteEvent;
import valorless.havenbags.gui.BagGUI;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Reflex;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
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
	
	private static List<Data> data = new ArrayList<Data>();
	private static HashMap<UUID, Data> changedBags = new HashMap<UUID, Data>();
	public static long interval;
	
	private static boolean ready = false;

	public static boolean isReady() {
		return ready;
	}
	
	public static void Initiate() {
		data.clear(); // Just in case
		DatabaseType type = DatabaseType.get(Main.config.GetString("save-type").toUpperCase());
		if(type != null) setDatabase(type);
		else {
			Log.Error(Main.plugin, String.format("Invalid database type \"%s\"\n"
					+ "Please choose either FILES, MYSQL, or SQLITE.", Main.config.GetString("save-type")));
			Bukkit.getPluginManager().disablePlugin(Main.plugin);
		}
		
		if(getDatabase() == DatabaseType.MYSQL || getDatabase() == DatabaseType.MYSQLPLUS) {
			setMysql(new MySQL());
		}
		else if(getDatabase() == DatabaseType.SQLITE) {
			sqlite = new SQLite();
		}
		
		interval = Main.config.GetInt("auto-save-interval")*20;
		LoadData();
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
            	SaveData(false, null);
		    }
		};

		autosave.runTaskTimer(Main.plugin, interval, interval);
		//Log.Info(Main.plugin, "Loaded bags: " + data.size());
	}
	
	public static void Shutdown() {
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
	}
	
	public static void ChangeDatabase(DatabaseType type) {
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
            	SaveData(false, null);
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
	
	public static void Reload() {
		interval = Main.config.GetInt("auto-save-interval");
		Log.Info(Main.plugin, "Bag data was not reloaded. You can force bag data reload with /havenbags reload force");
		Log.Warning(Main.plugin, "Any unsaved bag data will be lost!");
	}
	
	public static void ForceReload() {
		LoadData();
	}
	
	public static class Bag {
		public ItemStack item;
		public List<ItemStack> content = new ArrayList<ItemStack>();
		
		public Bag (ItemStack item, List<ItemStack> content) {
			this.item = item;
			this.content = content;
		}
	}
	
	public static boolean Contains(@NotNull String uuid) {
		if(GetBag(uuid, null) != null) {
			return true;
		}
		else return false;
	}
	
	public static Boolean BagExists(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return true;
			}
		}
		return false;
	}
	
	public static Data GetBag(@NotNull String uuid,  @Nullable ItemStack bagItem, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				//Log.Debug(Main.plugin, "[DI-28] " + bag.uuid);
				//Log.Debug(Main.plugin, bag.owner);
				if(m_source == UpdateSource.PLAYER) {
					bag.setOpen(true);
				}
				//Log.Debug(Main.plugin, bag.content.toString());
				return bag;
			}
		}
		Log.Debug(Main.plugin, String.format("Failed to get bag '%s', this bag was not found.", uuid));
		Log.Debug(Main.plugin, "If you keep seeing this error, please replace the bag causing it.");
		//if(bagItem != null) bagItem.setAmount(0);
		return null;
	}
	
	public static void UpdateBag(@NotNull String uuid, @NotNull List<ItemStack> content, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		
		Data bag = GetBag(uuid, null);
		
		if(bag == null) Log.Error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
		
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
			Log.Error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
		}
	}
	
	public static void UpdateBag(@NotNull ItemStack bagItem, @NotNull List<ItemStack> content, UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		String uuid = PDC.GetString(bagItem, "uuid");
		
		Data bag = GetBag(uuid, null);
		
		if(bag == null) Log.Error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
		
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
				bag.setTexture(Main.config.GetString("bag-texture"));
			}
			//bag.data.Set("texture", getTextureValue(bagItem));
			 
			 
			bag.setChanged(true);
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.setOpen(false);
			}
			

			if(Main.config.GetBool("capacity-based-textures.enabled")) {
				bag.setTexture(HavenBags.CapacityTexture(bagItem, content));
				//bag.setTexture(HavenBags.CapacityTexture(bagItem, content));
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
		            @Override
		            public void run(){
		            	HavenBags.UpdateBagItem(bagItem, content, bag.getViewer());
		            }
		        }, 1L);
				
			}
		}catch(Exception e) {
			Log.Error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
			if(m_source == UpdateSource.PLAYER) {
			}
		}
	}
	
	public static Data CreateBag(@NotNull String uuid,@NotNull String owner,@NotNull List<ItemStack> content, Player creator, ItemStack bag) {
		Data dat = new Data(uuid, owner, bag.getType());
		dat.setContent(content);
		dat.setCreator(creator.getUniqueId().toString());
		
		dat.setSize(PDC.GetInteger(bag, "size"));
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
		
		if(PDC.Has(bag, "bag-filter")) {
			dat.setAutopickup(PDC.GetString(bag, "filter"));
		}else {
			dat.setAutopickup("null");
		}
		
		if(PDC.Has(bag, "bag-blacklist")) {
			dat.setBlacklist(PDC.GetStringList(bag, "blacklist"));
			dat.setWhitelist(PDC.GetBoolean(bag, "whitelist"));
			dat.setIgnoreGlobalBlacklist(PDC.GetBoolean(bag, "igb"));
		}
		
		dat.setChanged(true);
		data.add(dat);
		Log.Debug(Main.plugin, "[DI-30] " + "New bag data created: " + owner + "/" + uuid);
		Bukkit.getPluginManager().callEvent(new BagCreateEvent(creator, bag, dat));
		return dat;
	}
	
	public static void LoadData(){
		ready = false;
		Log.Info(Main.plugin, "Loading bags..");
		long startTime = System.currentTimeMillis();
		int i = 0;
		if(getDatabase() == DatabaseType.FILES) {
			List<String> owners	= GetBagOwners();
			for(String owner : owners) {
				List<String> bags = Files.GetBags(owner);
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
						data.add(Files.loadBag(owner, bag));
						i++;
					} catch (Exception e) {
						Log.Error(Main.plugin, bag);
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		else if(getDatabase() == DatabaseType.MYSQL) {
			List<Data> bags = getMysql().loadAllBags();
			data = bags;
			i = bags.size();
		}
		else if(getDatabase() == DatabaseType.SQLITE) {
			for(String uuid : sqlite.getAllBagUUIDs()) {
				data.add(sqlite.loadBag(uuid));
				i++;
			}
		}
		else if(getDatabase() == DatabaseType.MYSQLPLUS) {
			List<Data> bags = getMysql().loadAllBags();
			data = bags;
			i = bags.size();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		Log.Info(Main.plugin, String.format("Loaded %s bags. %sms", i, duration));
		ready = true;
	}
	
	public static void SaveData(boolean shutdown, boolean... conversion) {
		long startTime = System.currentTimeMillis();
		if(Main.config.GetBool("auto-save-message")) Log.Info(Main.plugin, "Saving bags..");
		List<Data> toSave = new ArrayList<>();
		
		if(shutdown || conversion != null) {
			for(Data bag : data) {
				toSave.add(bag);
			}
		}
		
		Iterator<Map.Entry<UUID, Data>> iterator = changedBags.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Data> entry = iterator.next();
			Data bag = entry.getValue();
		    toSave.add(bag);
		    iterator.remove();
		    bag.setChanged(false);
		}
		
		for(Data bag : toSave) {
			String uuid = bag.getUuid();
	    	String owner = bag.getOwner();
	    	
	    	if(getDatabase() == DatabaseType.FILES) {
	        	Log.Debug(Main.plugin, "[DI-31] [FILES] " + "Attempting to write bag " + owner + "/" + uuid + " onto server");
	    		Files.saveBag(bag);
	    	}
	    	else if(getDatabase() == DatabaseType.SQLITE) {
	    		Log.Debug(Main.plugin, "[DI-231] [SQLITE] " + "Attempting to write bag " + owner + "/" + uuid + " onto database");
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
	    		Log.Debug(Main.plugin, "[DI-232] [MYSQL] " + "Attempting to write bags onto database");
	    		if(shutdown || conversion != null) {
	    			getMysql().saveBags(toSave);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
	    				getMysql().saveBags(toSave);
	    			});
	    		}
	    	}
	    	else if(getDatabase() == DatabaseType.MYSQLPLUS) {
	    		Log.Debug(Main.plugin, "[DI-233] [MYSQLPLUS] " + "Attempting to write bags onto database");
	    		if(shutdown || conversion != null) {
	    			getMysql().saveBags(toSave);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
	    				getMysql().saveBags(toSave);
	    			});
	    		}
	    	}
		}
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		if(Main.config.GetBool("auto-save-message")) Log.Info(Main.plugin, String.format("Saved %s bags. %sms", toSave.size(), duration));
	}
	
	public static void RemoveBag(@NotNull String uuid) {
		for(int i = 0; i < data.size(); i++) {
			Data bag = data.get(i);
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				data.remove(i);
				if(changedBags.containsKey(UUID.fromString(uuid))) {
					changedBags.remove(UUID.fromString(uuid));
				}
				Log.Info(Main.plugin, String.format("Removed cached data for %s.", uuid));
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to remove cached data for %s.", uuid));
	}
	
	public static Boolean DeleteBag(@NotNull String uuid) {
		for(int i = 0; i < data.size(); i++) {
			Data bag = data.get(i);
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				if(getDatabase() == DatabaseType.FILES) {
					try {
						Files.deleteFile(bag.getOwner(), uuid);
					}catch(Exception e) {
						Log.Error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
						e.printStackTrace();
						return false;
					}
				}else if(getDatabase() == DatabaseType.MYSQL) {
		    		getMysql().deleteBag(uuid);
		    	}
		    	else if(getDatabase() == DatabaseType.SQLITE) {
		    		sqlite.deleteBag(uuid);
		    	}
				
				RemoveBag(uuid);
				if(changedBags.containsKey(UUID.fromString(uuid))) {
					changedBags.remove(UUID.fromString(uuid));
				}
				Log.Info(Main.plugin, String.format("Deleted data for %s.", uuid));
				return true;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
		return false;
	}
	
	public static List<String> GetBags(@NotNull String playerUUID) {
	    Log.Debug(Main.plugin, "[DI-32] " + playerUUID);
	    return data.stream()
	        .filter(dat -> dat.getOwner().equals(playerUUID))
	        .map(Data::getUuid)
	        .toList();
	}
	
	public static List<Data> GetBagsData(@NotNull String playerUUID) {
	    Log.Debug(Main.plugin, "[DI-260] " + playerUUID);
	    return data.stream()
	        .filter(dat -> dat.getOwner().equals(playerUUID))
	        .toList();
	}
	
	public static List<String> GetBagOwners(){
		if(getDatabase() == DatabaseType.FILES) {
			try {
				List<String> bagOwners = Stream.of(new File(String.format("%s/bags/", Main.plugin.getDataFolder())).listFiles())
						.filter(file -> file.isDirectory())
						.map(File::getName)
						.toList();
				return bagOwners;
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
	
	public static boolean IsBagOpen(@NotNull String uuid,  ItemStack bagItem) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.isOpen();
			}
		}
		Log.Error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
		return false;
	}
	
	public static boolean IsBagOpen(ItemStack bagItem) {
		if(HavenBags.BagState(bagItem) != BagState.Used) return false;
		String uuid = HavenBags.GetBagUUID(bagItem);
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.isOpen();
			}
		}
		return false;
	}
	
	public static Player BagOpenBy(@NotNull String uuid,  ItemStack bagItem) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				if(IsBagOpen(uuid, bagItem)) {
					return bag.getViewer();
				}else return null;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
		return null;
	}
	
	public static void MarkBagOpen(@NotNull String uuid, ItemStack bagItem, Player player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.setOpen(true);
				bag.setViewer(player);
				if(getDatabase() == DatabaseType.MYSQLPLUS) {
					Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
    					getMysql().saveBag(bag);
    				});
				}
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}
	
	public static void MarkBagOpen(@NotNull String uuid, ItemStack bagItem, Player player, BagGUI gui) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
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
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}
	
	public static void MarkBagClosed(@NotNull String uuid) {
		Data bag = GetBag(uuid, null); // This will throw an error if the bag does not exist, which is fine.
		if(bag == null) {
			Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as closed, this bag was not found.", uuid));
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
	
	public List<String> GetTrusted(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.getTrusted();
			}
		}
		return null;
	}
	
	public static String GetOwner(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.getOwner();
			}
		}
		return null;
	}
	
	public static String GetCreator(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.getCreator();
			}
		}
		return null;
	}
	
	public static void AddTrusted(@NotNull String uuid, @NotNull String player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				List<String> trusted = bag.getTrusted();
				if(!trusted.contains(player)) {
					trusted.add(player);
					bag.setTrusted(trusted);
					bag.setChanged(true);
					if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
				}
			}
		}
	}
	
	public static void RemoveTrusted(@NotNull String uuid, @NotNull String player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				List<String> trusted = bag.getTrusted();
				if(trusted.size() == 0) return;
				for(int i = 0; i < trusted.size(); i++) {
					if(trusted.get(i).equalsIgnoreCase(player)) {
						trusted.remove(i);
						bag.setTrusted(trusted);
						bag.setChanged(true);
						if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
					}
				}
			}
		}
	}
	
	public static void SetAutoPickup(@NotNull String uuid, @NotNull String filter) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.setAutopickup(filter);
				bag.setChanged(true);
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	public static String GetAutoPickup(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.getAutopickup();
			}
		}
		return null;
	}
	
	public static void RemoveAutoPickup(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.setAutopickup("null");
				bag.setChanged(true);
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}	
	}
	
	public static void SetWeight(@NotNull String uuid, @NotNull double weight) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.setWeight(weight);
				bag.setChanged(true);
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	public static void SetWeightMax(@NotNull String uuid, @NotNull double weightmax) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.setWeightMax(weightmax);
				bag.setChanged(true);
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void MarkBagChanged(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.setChanged(true);
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	public static String getTextureValue(ItemStack head) {
        if (head == null || head.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("ItemStack must be a Player Head");
        }
        
        if(Server.VersionHigherOrEqualTo(Version.v1_21_1)) {
        	SkullMeta meta = (SkullMeta) head.getItemMeta();
        	return HeadCreator.convertUrlToBase64(meta.getOwnerProfile().getTextures().getSkin().toString());
        }else {

        	// Use NBTAPI to access the NBT data
        	if (valorless.valorlessutils.nbtapi.NBT.readNbt(head).hasTag("SkullOwner") == false) {
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
        	if (textures == null || textures.size() == 0) {
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
            	textures.setSkin(new URL(HeadCreator.extractUrlFromBase64(value)));
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

        		Method method = Reflex.getMethod(meta.getClass(), "setProfile", GameProfile.class);
        		if (method != null) {
        			Reflex.invokeMethod(method, meta, profile);
        		} else {
            		Reflex.setFieldValue(meta, "profile", profile);
        		}
        	}catch(Exception e) {}
        }

        item.setItemMeta(meta);
    }
	
	public static List<Data> GetOpenBags() {
	    return data.stream()
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
	
	public static Boolean ClearAllBagContents() {
		try {
			for(Data dat : data) {
				ClearBagContent(dat.getUuid());
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean ClearBagContentPlayer(@NotNull String playeruuid) {
		try {
			for(String bag : GetBags(playeruuid)) {
				ClearBagContent(bag);
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean ClearBagContent(@NotNull String uuid) {
		for(int i = 0; i < data.size(); i++) {
			Data dat = data.get(i);
			if(dat.getUuid().equalsIgnoreCase(uuid)) {
				if(dat.getGui() != null) {
					dat.getGui().Close(true);
				}
				
				dat.setContent(new ArrayList<>(Collections.nCopies(dat.getContent().size(), null)));
				Log.Info(Main.plugin, String.format("Cleared content for %s.", uuid));
				return true;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to clear content for %s.", uuid));
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
}
