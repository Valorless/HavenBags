package valorless.havenbags;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;

public class BagData {
	
	public enum UpdateSource { NULL, PLAYER }
	
	private static List<Data> data = new ArrayList<Data>();
	public static long interval;
	
	public static void Initiate() {
		data.clear(); // Just in case
		interval = Main.config.GetInt("auto-save-interval")*20;
		LoadData();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
            @Override
            public void run() {
            	SaveData();
            }
        }, interval, interval);
		Log.Debug(Main.plugin, "Loaded bags: " + data.size());
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
		private List<ItemStack> content = new ArrayList<ItemStack>();
		private boolean changed = false;
		private boolean isOpen = false;
		
		public Data(@NotNull String uuid, @NotNull String owner) {
			this.setUuid(uuid); this.setOwner(owner);
		}
		
		public Data(@NotNull String uuid, @NotNull String owner, @NotNull List<ItemStack> content) {
			this.setUuid(uuid); this.setOwner(owner); this.setContent(content);
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(@NotNull String uuid) {
			this.uuid = uuid;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(@NotNull String owner) {
			this.owner = owner;
		}

		public List<ItemStack> getContent() {
			return content;
		}

		public void setContent(@NotNull List<ItemStack> content) {
			this.content = content;
		}
	}
	
	public static boolean Contains(@NotNull String uuid) {
		if(GetBag(uuid, null) != null) {
			return true;
		}
		else return false;
	}
	
	public static Data GetBag(@NotNull String uuid,  ItemStack bagItem, UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source.length != 0) {
			m_source = source[0];
		}
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				Log.Debug(Main.plugin, bag.uuid);
				Log.Debug(Main.plugin, bag.owner);
				if(m_source == UpdateSource.PLAYER) {
					bag.isOpen = true;
				}
				//Log.Debug(Main.plugin, bag.content.toString());
				return bag;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to load bag '%s', this bag was not found.", uuid));
		Log.Error(Main.plugin, "If you keep seeing this error, please replace the bag causing it.");
		if(bagItem != null) bagItem.setAmount(0);
		return null;
	}
	
	public static void UpdateBag(@NotNull String uuid, @NotNull List<ItemStack> content, UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source.length != 0) {
			m_source = source[0];
		}
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				try {
					bag.setContent(content);
					bag.changed = true;
					if(m_source == UpdateSource.PLAYER) {
						bag.isOpen = false;
					}
				}catch(Exception e) {
					Log.Error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
				}
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
	}
	
	public static void UpdateBag(@NotNull ItemStack bagItem, @NotNull List<ItemStack> content, UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source.length != 0) {
			m_source = source[0];
		}
		String uuid = NBT.GetString(bagItem, "bag-uuid");
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				try {
					bag.setContent(content);
					bag.changed = true;
					if(m_source == UpdateSource.PLAYER) {
						bag.isOpen = false;
					}
				}catch(Exception e) {
					Log.Error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
				}
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
	}
	
	public static void CreateBag(@NotNull String uuid,@NotNull String owner,@NotNull List<ItemStack> content) {
		Data dat = new Data(uuid, owner, content);
		dat.changed = true;
		data.add(dat);
		Log.Debug(Main.plugin, "New bag data created: " + owner + "/" + uuid);
	}
	
	public static void LoadData(){
		Log.Info(Main.plugin, "Loading bags.");
		List<String> owners	= GetBagOwners();
		for(String owner : owners) {
			List<String> bags	= GetBags(owner);
			for(String bag : bags) {
				String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, bag);
				
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
					Data bagdata = new Data(bag, owner);
					Path filePath = Path.of(path);
					bagdata.setContent(JsonUtils.fromJson(Files.readString(filePath)));
					data.add(bagdata);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		Log.Info(Main.plugin, "Bags loaded.");
	}
	
	public static void SaveData() {
		if(Main.config.GetBool("auto-save-message")) Log.Info(Main.plugin, "Saving bags.");
		for(Data bag : data) {
			if(!bag.changed) continue;
			String uuid = bag.uuid;
	    	String owner = bag.owner;
	    	Log.Debug(Main.plugin, "Attempting to write bag " + owner + "/" + uuid + " onto server");
	    	
	    	File bagData;
	    	List<ItemStack> cont = new ArrayList<ItemStack>();
	        for(int i = 0; i < bag.content.size(); i++) {
	    		cont.add(bag.content.get(i));
	    	}
	    	if(owner != "ownerless") {
	    		bagData = new File(Main.plugin.getDataFolder() + "/bags/", owner + "/" + uuid + ".json");
	    		if(!bagData.exists()) {
	            	bagData.getParentFile().mkdirs();
	                Log.Debug(Main.plugin, String.format("Bag data for (%s) %s does not exist, creating new.", owner, uuid));
	            }
	    	}else {
	    		bagData = new File(Main.plugin.getDataFolder() + "/bags/", owner + "/" + uuid + ".json");
	    		if(!bagData.exists()) {
	            	bagData.getParentFile().mkdirs();
	                Log.Debug(Main.plugin, String.format("Bag data for (%s) %s does not exist, creating new.", owner, uuid));
	            }
	    	}
	    	
	    	Path path = Paths.get(Main.plugin.getDataFolder() + "/bags/", owner + "/" + uuid + ".json");
	    	List<String> lines = Arrays.asList(JsonUtils.toPrettyJson(cont));
	    	try {
	    		Files.write(path, lines, StandardCharsets.UTF_8);
	    	}catch(IOException e){
				e.printStackTrace();
	    	}
	    	bag.changed = false;
		}
		if(Main.config.GetBool("auto-save-message")) Log.Info(Main.plugin, "Bags saved.");
	}
	
	private static List<String> GetBags(@NotNull String player){
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), player)).listFiles())
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < bags.size(); i++) {
				bags.set(i, bags.get(i).replace(".json", ""));
			}
			return bags;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	private static List<String> GetBagOwners(){
		try {
			List<String> bagOwners = Stream.of(new File(String.format("%s/bags/", Main.plugin.getDataFolder())).listFiles())
					.filter(file -> file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
			return bagOwners;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	public static boolean IsBagOpen(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.isOpen;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to check if bag '%s' is open, this bag was not found.", uuid));
		return false;
	}
	
	public static void MarkBagOpen(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.isOpen = true;
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as open, this bag was not found.", uuid));
	}
	
	public static void MarkBagClosed(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.isOpen = false;
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as closed, this bag was not found.", uuid));
	}
}
