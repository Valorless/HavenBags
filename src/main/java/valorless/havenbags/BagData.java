package valorless.havenbags;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
import valorless.havenbags.Main.ServerVersion;
import valorless.havenbags.database.DatabaseType;
import valorless.havenbags.database.Files;
import valorless.havenbags.database.MySQL;
import valorless.havenbags.database.SQLite;
import valorless.havenbags.utils.Reflex;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.nbtapi.iface.ReadWriteNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBTList;
import valorless.havenbags.utils.HeadCreator;

public class BagData {
	
	protected static DatabaseType database = DatabaseType.SQLITE;
	static MySQL mysql;
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
		if(type != null) database = type;
		else {
			Log.Error(Main.plugin, String.format("Invalid database type \"%s\"\n"
					+ "Please choose either FILES, MYSQL, or SQLITE.", Main.config.GetString("save-type")));
			Bukkit.getPluginManager().disablePlugin(Main.plugin);
		}
		
		if(database == DatabaseType.MYSQL || database == DatabaseType.MYSQLPLUS) {
			mysql = new MySQL();
		}
		else if(database == DatabaseType.SQLITE) {
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
		
		if(database == DatabaseType.MYSQLPLUS) return;
		
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
		if(database == DatabaseType.MYSQL) {
			try {
				if(mysql != null) {
					mysql.disconnect();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(database == DatabaseType.SQLITE) {
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
		if(database == DatabaseType.MYSQL || database == DatabaseType.MYSQLPLUS) {
			try {
				mysql.disconnect();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(database == DatabaseType.SQLITE) {
			try {
				sqlite.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		database = type;
		autosave = new BukkitRunnable() {
		    @Override
		    public void run() {
            	SaveData(false, null);
		    }
		};

		autosave.runTaskTimer(Main.plugin, interval, interval);
		
		if(type == DatabaseType.MYSQL || type == DatabaseType.MYSQLPLUS) {
			mysql = new MySQL();
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
	
	public static class Data {
		private String uuid;
		private String owner;
		private String creator;
		private int size;
		private String texture;
		private Material material;
		private String name = "";
		private int modeldata;
		private String itemmodel;
		private List<String> trusted;
		private String autopickup;
		private double weight;
		private double weightMax;
		private List<ItemStack> content = new ArrayList<ItemStack>();
		private boolean autosort;

		//private Config dataFile;
		
		private boolean changed = false;
		private boolean isOpen = false;

		private Player viewer = null;
		private BagGUI gui;
		
		public Data(@NotNull String uuid, @NotNull String owner) {
			this.setUuid(uuid); this.setOwner(owner);
		}
		
		public Data(@NotNull String uuid, @NotNull String owner, @NotNull Material material) {
			this.setUuid(uuid); this.setOwner(owner); this.setMaterial(material);
		}
		
		//public Data(@NotNull String uuid, @NotNull String owner, @NotNull Config data) {
		//	this.setUuid(uuid); this.setOwner(owner); this.SetData(data);
		//}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(@NotNull String uuid) {
			this.changed = true;
			this.uuid = uuid;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(@NotNull String owner) {
			this.changed = true;
			this.owner = owner;
		}
		
		public String getCreator() {
			return creator;
		}

		public void setCreator(String creator) {
			this.changed = true;
			this.creator = creator;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.changed = true;
			this.size = size;
		}
		
		public void setContent(List<ItemStack> content){
			this.changed = true;
			this.content = content;
			//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
		}

		public List<ItemStack> getContent(){
			return content;
			//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
		}
		
		public String getTexture() {
			return texture;
		}
		
		public void setTexture(String base64) {
			this.changed = true;
			this.texture = base64;
		}

		/*public Config getDataFile() {
			return dataFile;
		}

		public void SetDataFile(@NotNull Config data) {
			this.dataFile = data;
		}*/

		public int getModeldata() {
			return modeldata;
		}

		public void setModeldata(int modeldata) {
			this.changed = true;
			this.modeldata = modeldata;
		}

		public String getItemmodel() {
			return itemmodel;
		}

		public void setItemmodel(String itemmodel) {
			this.changed = true;
			this.itemmodel = itemmodel;
		}

		public List<String> getTrusted() {
			return trusted;
		}
		
		public boolean isPlayerTrusted(String uuid) {
			if(trusted.isEmpty()) return false;
			for(int i = 0; i < trusted.size(); i++) {
				if(trusted.get(i).equalsIgnoreCase(uuid)) {
					return true;
				}
			}
			return false;
		}

		public void setTrusted(List<String> trusted) {
			this.changed = true;
			this.trusted = trusted;
		}

		public String getAutopickup() {
			return autopickup;
		}

		public void setAutopickup(String autopickup) {
			this.changed = true;
			this.autopickup = autopickup;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.changed = true;
			this.weight = weight;
		}

		public double getWeightMax() {
			return weightMax;
		}

		public void setWeightMax(double weightMax) {
			this.changed = true;
			this.weightMax = weightMax;
		}

		public BagGUI getGui() {
			return gui;
		}

		public void setGui(BagGUI gui) {
			this.changed = true;
			this.gui = gui;
		}

		public Player getViewer() {
			return viewer;
		}

		public boolean isChanged() {
			return changed;
		}

		public boolean isOpen() {
			return isOpen;
		}

		public void setOpen(boolean open) {
			this.changed = true;
			this.isOpen = open;
		}

		public boolean hasAutoSort() {
			return autosort;
		}

		public void setAutoSort(boolean autosort) {
			this.changed = true;
			this.autosort = autosort;
		}

		public Material getMaterial() {
			return material;
		}

		public void setMaterial(Material material) {
			this.changed = true;
			this.material = material;
		}

		public void setMaterial(String material) {
			this.changed = true;
			if(material.equalsIgnoreCase("null")) {
				this.material = null;
				return;
			}
			this.material = Material.valueOf(material.toUpperCase());
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.changed = true;
			this.name = name;
		}
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
					bag.isOpen = true;
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
			bag.content = content;
			//bag.data.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
			bag.changed = true;
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.isOpen = false;
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
		String uuid = NBT.GetString(bagItem, "bag-uuid");
		
		Data bag = GetBag(uuid, null);
		
		if(bag == null) Log.Error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
		
		try {
			//bag.setContent(content);
			bag.content = content;
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
					if(Main.VersionCompare(Main.server, Main.ServerVersion.v1_21_2) >= 0) {
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
			 
			 
			bag.changed = true;
			if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			if(m_source == UpdateSource.PLAYER) {
				bag.isOpen = false;
			}
			

			if(Main.config.GetBool("capacity-based-textures.enabled")) {
				bag.setTexture(HavenBags.CapacityTexture(bagItem, content));
				//bag.setTexture(HavenBags.CapacityTexture(bagItem, content));
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
		            @Override
		            public void run(){
		            	HavenBags.UpdateBagItem(bagItem, content, bag.viewer);
		            }
		        }, 1L);
				
			}
		}catch(Exception e) {
			Log.Error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
			if(m_source == UpdateSource.PLAYER) {
			}
		}
	}
	
	public static void CreateBag(@NotNull String uuid,@NotNull String owner,@NotNull List<ItemStack> content, Player creator, ItemStack bag) {
		Data dat = new Data(uuid, owner, bag.getType());
		dat.content = content;
		dat.creator = creator.getUniqueId().toString();
		
		dat.size = NBT.GetInt(bag, "bag-size");
		if(bag.getType() == Material.PLAYER_HEAD) {
			dat.texture = getTextureValue(bag);
			dat.modeldata = 0;
			dat.itemmodel = null;
		}else {
			dat.texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
			if(bag.hasItemMeta()) {
				if(bag.getItemMeta().hasCustomModelData()) {
					dat.modeldata = bag.getItemMeta().getCustomModelData();
				}

				if(Main.VersionCompare(Main.server, Main.ServerVersion.v1_21_2) >= 0) {
					if(ItemUtils.GetItemModel(bag) != null) {
						dat.itemmodel = ItemUtils.GetItemModel(bag).toString();
					}else {
						dat.setItemmodel("");
					}
				}else {
					dat.setItemmodel("");
				}
			}
		}
		dat.trusted = new ArrayList<String>();
		dat.autopickup = "null";
		
		dat.changed = true;
		data.add(dat);
		Log.Debug(Main.plugin, "[DI-30] " + "New bag data created: " + owner + "/" + uuid);
	}
	
	public static void LoadData(){
		ready = false;
		Log.Info(Main.plugin, "Loading bags..");
		long startTime = System.currentTimeMillis();
		int i = 0;
		if(database == DatabaseType.FILES) {
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
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		else if(database == DatabaseType.MYSQL) {
			List<Data> bags = mysql.loadAllBags();
			data = bags;
			i = bags.size();
		}
		else if(database == DatabaseType.SQLITE) {
			for(String uuid : sqlite.getAllBagUUIDs()) {
				data.add(sqlite.loadBag(uuid));
				i++;
			}
		}
		else if(database == DatabaseType.MYSQLPLUS) {
			List<Data> bags = mysql.loadAllBags();
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
		    bag.changed = false;
		}
		
		for(Data bag : toSave) {
			String uuid = bag.uuid;
	    	String owner = bag.owner;
	    	
	    	if(database == DatabaseType.FILES) {
	        	Log.Debug(Main.plugin, "[DI-31] " + "Attempting to write bag " + owner + "/" + uuid + " onto server");
	    		Files.saveBag(bag);
	    	}
	    	else if(database == DatabaseType.SQLITE) {
	    		Log.Debug(Main.plugin, "[DI-231] " + "Attempting to write bag " + owner + "/" + uuid + " onto database");
	    		if(shutdown || conversion != null) {
	    			sqlite.saveBag(bag);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
	    				sqlite.saveBag(bag);
	    			});
	    		}
	    	}else 
		    	if(database == DatabaseType.MYSQLPLUS) {
		    		if(!shutdown && conversion == null) {
		    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
		    				mysql.saveBag(bag);
		    			});
		    		}
		    	}
		}
		
		if(!toSave.isEmpty()) {
	    	if(database == DatabaseType.MYSQL) {
	    		Log.Debug(Main.plugin, "[DI-232] " + "Attempting to write bags onto database");
	    		if(shutdown || conversion != null) {
	    			mysql.saveBags(toSave);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
	    				mysql.saveBags(toSave);
	    			});
	    		}
	    	}
	    	else if(database == DatabaseType.MYSQLPLUS) {
	    		Log.Debug(Main.plugin, "[DI-233] " + "Attempting to write bags onto database");
	    		if(shutdown || conversion != null) {
	    			mysql.saveBags(toSave);
	    		}else {
	    			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
	    				mysql.saveBags(toSave);
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
				Log.Info(Main.plugin, String.format("Removed cached data for %s.", uuid));
				return;
			}
		}
		if(changedBags.containsKey(UUID.fromString(uuid))) {
			changedBags.remove(UUID.fromString(uuid));
		}
		Log.Error(Main.plugin, String.format("Failed to remove cached data for %s.", uuid));
	}
	
	public static void DeleteBag(@NotNull String uuid) {
		for(int i = 0; i < data.size(); i++) {
			Data bag = data.get(i);
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				RemoveBag(uuid);
				if(database == DatabaseType.FILES) {
					try {
						Files.deleteFile(bag.getOwner(), uuid);
					}catch(Exception e) {
						Log.Error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
						e.printStackTrace();
					}
				}else if(database == DatabaseType.MYSQL) {
		    		mysql.deleteBag(uuid);
		    	}
		    	else if(database == DatabaseType.SQLITE) {
		    		sqlite.deleteBag(uuid);
		    	}
				Log.Info(Main.plugin, String.format("Deleted data for %s.", uuid));
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to delete data for %s.", uuid));
	}
	
	public static List<String> GetBags(@NotNull String playerUUID){
		Log.Debug(Main.plugin, "[DI-32] " + playerUUID);
		List<String> bags = new ArrayList<String>();
		for(Data dat : data) {
			if(dat.owner.equalsIgnoreCase(playerUUID)) {
				bags.add(dat.uuid);
			}
		}
		return bags;
	}
	
	public static List<String> GetBagOwners(){
		if(database == DatabaseType.FILES) {
			try {
				List<String> bagOwners = Stream.of(new File(String.format("%s/bags/", Main.plugin.getDataFolder())).listFiles())
						.filter(file -> file.isDirectory())
						.map(File::getName)
						.collect(Collectors.toList());
				return bagOwners;
			} catch (Exception e) {
				return new ArrayList<String>();
			}
		}else if(database == DatabaseType.MYSQL) {
			return mysql.getBagOwners();
    	}
    	else if(database == DatabaseType.SQLITE) {
    		return sqlite.getBagOwners();
    	}
		
		return new ArrayList<String>();
	}
	
	public static boolean IsBagOpen(@NotNull String uuid,  ItemStack bagItem) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.isOpen;
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
				return bag.isOpen;
			}
		}
		return false;
	}
	
	public static Player BagOpenBy(@NotNull String uuid,  ItemStack bagItem) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				if(IsBagOpen(uuid, bagItem)) {
					return bag.viewer;
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
				bag.isOpen = true;
				bag.viewer = player;
				if(database == DatabaseType.MYSQLPLUS) {
					Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
    					mysql.saveBag(bag);
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
				bag.isOpen = true;
				bag.viewer = player;
				bag.gui = gui;
				if(database == DatabaseType.MYSQLPLUS) {
					Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
    					mysql.saveBag(bag);
    				});
				}
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
		if(bagItem != null) bagItem.setAmount(0);
	}
	
	public static void MarkBagClosed(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.isOpen = false;
				bag.gui = null;
				if(database == DatabaseType.MYSQLPLUS) {
					Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
    					mysql.saveBag(bag);
    				});
				}
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as closed, this bag was not found.", uuid));
	}
	
	public List<String> GetTrusted(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.trusted;
			}
		}
		return null;
	}
	
	public static String GetOwner(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.owner;
			}
		}
		return null;
	}
	
	public static String GetCreator(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.creator;
			}
		}
		return null;
	}
	
	public static void AddTrusted(@NotNull String uuid, @NotNull String player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				List<String> trusted = bag.trusted;
				if(!trusted.contains(player)) {
					trusted.add(player);
					bag.trusted = trusted;
					bag.changed = true;
					if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
				}
			}
		}
	}
	
	public static void RemoveTrusted(@NotNull String uuid, @NotNull String player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				List<String> trusted = bag.trusted;
				if(trusted.size() == 0) return;
				for(int i = 0; i < trusted.size(); i++) {
					if(trusted.get(i).equalsIgnoreCase(player)) {
						trusted.remove(i);
						bag.trusted = trusted;
						bag.changed = true;
						if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
					}
				}
			}
		}
	}
	
	public static void SetAutoPickup(@NotNull String uuid, @NotNull String filter) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.autopickup = filter;
				bag.changed = true;
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	public static String GetAutoPickup(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.autopickup;
			}
		}
		return null;
	}
	
	public static void RemoveAutoPickup(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.autopickup = "null";
				bag.changed = true;
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}	
	}
	
	public static void SetWeight(@NotNull String uuid, @NotNull double weight) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.weight = weight;
				bag.changed = true;
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	public static void SetWeightMax(@NotNull String uuid, @NotNull double weightmax) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.weightMax = weightmax;
				bag.changed = true;
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void MarkBagChanged(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.changed = true;
				if(!changedBags.containsKey(UUID.fromString(uuid))) changedBags.put(UUID.fromString(uuid), bag);
			}
		}
	}
	
	public static String getTextureValue(ItemStack head) {
        if (head == null || head.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("ItemStack must be a Player Head");
        }
        
        if(Main.VersionCompare(Main.server, ServerVersion.v1_21_1) >= 0) {
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
        
        if(Main.VersionCompare(Main.server, ServerVersion.v1_21_1) >= 0 || Main.server == ServerVersion.NULL) {
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
	
	public static List<Data> GetOpenBags(){
		List<Data> open = new ArrayList<>();
		for(Data bag : data) {
			if(bag.isOpen) open.add(bag);
		}
		return open;
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
}
