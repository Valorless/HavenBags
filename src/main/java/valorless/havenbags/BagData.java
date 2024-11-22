package valorless.havenbags;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import valorless.havenbags.Main.ServerVersion;
import valorless.havenbags.utils.Reflex;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.nbtapi.iface.ReadWriteNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBTList;
import valorless.havenbags.utils.HeadCreator;

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
		Log.Info(Main.plugin, "Loaded bags: " + data.size());
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
		private Config data;
		private List<ItemStack> content = new ArrayList<ItemStack>();
		private boolean changed = false;
		private boolean isOpen = false;
		private Player viewer = null;
		
		public Data(@NotNull String uuid, @NotNull String owner) {
			this.setUuid(uuid); this.setOwner(owner);
		}
		
		public Data(@NotNull String uuid, @NotNull String owner, @NotNull Config data) {
			this.setUuid(uuid); this.setOwner(owner); this.SetData(data);
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
		
		public List<ItemStack> getContent(){
			return content;
			//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
		}
		
		public String getTexture() {
			return data.GetString("texture");
		}
		
		public void setTexture(String base64) {
			data.Set("texture", base64);
		}

		public Config getData() {
			return data;
		}

		public void SetData(@NotNull Config data) {
			this.data = data;
		}
	}
	
	public static boolean Contains(@NotNull String uuid) {
		if(GetBag(uuid, null) != null) {
			return true;
		}
		else return false;
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
				Log.Debug(Main.plugin, "[DI-28] " + bag.uuid);
				//Log.Debug(Main.plugin, bag.owner);
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
	
	public static void UpdateBag(@NotNull String uuid, @NotNull List<ItemStack> content, @Nullable UpdateSource... source) {
		UpdateSource m_source = UpdateSource.NULL;
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				try {
					//bag.setContent(content);
					bag.content = content;
					//bag.data.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
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
		if(source != null) {
			if(source.length != 0) {
				m_source = source[0];
			}
		}
		String uuid = NBT.GetString(bagItem, "bag-uuid");
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				try {
					//bag.setContent(content);
					bag.content = content;
					//bag.data.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
					
					if(bagItem.getType() == Material.PLAYER_HEAD) {
						bag.data.Set("texture", getTextureValue(bagItem));
						bag.data.Set("custommodeldata", 0);
					}else {
						if(bagItem.hasItemMeta()) {
							if(bagItem.getItemMeta().hasCustomModelData()) {
								bag.data.Set("custommodeldata", bagItem.getItemMeta().getCustomModelData());
							}else {
								bag.data.Set("custommodeldata", 0);
							}
						}else {
							bag.data.Set("custommodeldata", 0);
						}
						bag.data.Set("texture", Main.config.GetString("bag-texture"));
					}
					//bag.data.Set("texture", getTextureValue(bagItem));
					 
					 
					bag.changed = true;
					if(m_source == UpdateSource.PLAYER) {
						bag.isOpen = false;
					}
				}catch(Exception e) {
					Log.Error(Main.plugin, String.format("Failed to update bag '%s'.", uuid));
					if(m_source == UpdateSource.PLAYER) {
					}
				}
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to update bag '%s', this bag was not found.", uuid));
	}
	
	public static void CreateBag(@NotNull String uuid,@NotNull String owner,@NotNull List<ItemStack> content, Player creator, ItemStack bag) {
		//String config = String.format("/bags/%s/%s.yml", Main.plugin.getDataFolder(), owner, uuid);
		Config bagData = null;
		try {
			try {
		    	File file = new File(Main.plugin.getDataFolder() + "/bags/", owner + "/" + uuid + ".yml");
		    	if(!file.exists()) {
		    		file.getParentFile().mkdirs();
		    		file.createNewFile();
		        	Log.Debug(Main.plugin, "[DI-29] " + String.format("Bag data for (%s) %s does not exist, creating new.", owner, uuid));
		        }
		    	
		    	
				/*
				Path path = Paths.get(Main.plugin.getDataFolder() + config);
				List<String> lines = new ArrayList<String>();
				try {
					Files.write(path, lines, StandardCharsets.UTF_8);
				}catch(IOException e){
					e.printStackTrace();
					Log.Error(Main.plugin, "(0) Failed to create new bag data: " + owner + "/" + uuid);
					return;
				}*/
			}catch(Exception e){
				e.printStackTrace();
				Log.Error(Main.plugin, "(1) Failed to create new bag data: " + owner + "/" + uuid);
				return;
			}finally {
				try {
					bagData = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
					bagData.Set("uuid", uuid);
					bagData.Set("owner", owner);
					if(creator != null) {
						bagData.Set("creator", creator.getUniqueId().toString());
					}else {
						bagData.Set("creator", owner);
					}
					bagData.Set("size", content.size());
					if(bag.getType() == Material.PLAYER_HEAD) {
						bagData.Set("texture", getTextureValue(bag));
						bagData.Set("custommodeldata", 0);
					}else {
						if(bag.hasItemMeta()) {
							if(bag.getItemMeta().hasCustomModelData()) {
								bagData.Set("custommodeldata", bag.getItemMeta().getCustomModelData());
							}else {
								bagData.Set("custommodeldata", 0);
							}
						}else {
							bagData.Set("custommodeldata", 0);
						}
						bagData.Set("texture", Main.config.GetString("bag-texture"));
					}
					bagData.Set("trusted", new ArrayList<String>());
					bagData.Set("auto-pickup", "null");
					bagData.Set("weight-max", 0);
					bagData.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
					bagData.SaveConfig();
				}catch(Exception E) {
					E.printStackTrace();
					// Error: Top level is not a Map.
					// Unsure why this is thrown, but the file is converted successfully without issues..
					//Log.Error(plugin, String.format("Something went wrong while converting %s!.", String.format("/bags/%s/%s", owner, bag)));
				}
			}
		} catch(Exception e) {}
		
		if(bagData == null) {
			Log.Error(Main.plugin, "(2) Failed to create new bag data: " + owner + "/" + uuid);
			return;
		}
		Data dat = new Data(uuid, owner, bagData);
		dat.changed = true;
		dat.content = content;
		data.add(dat);
		Log.Debug(Main.plugin, "[DI-30] " + "New bag data created: " + owner + "/" + uuid);
	}
	
	public static void LoadData(){
		Log.Info(Main.plugin, "Loading bags.");
		List<String> owners	= GetBagOwners();
		for(String owner : owners) {
			List<String> bags	= GetBags(owner);
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
					Config d = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, bag));
					Data bagdata = new Data(bag, owner, d);
					bagdata.content = JsonUtils.fromJson(bagdata.data.GetString("content").replace("◊","'"));
					//Path filePath = Path.of(path);
					//bagdata.setContent(JsonUtils.fromJson(Files.readString(filePath)));
					//bagdata.SetData();
					data.add(bagdata);
				} catch (Exception e) {
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
	    	Log.Debug(Main.plugin, "[DI-31] " + "Attempting to write bag " + owner + "/" + uuid + " onto server");
	    	bag.data.Set("content", JsonUtils.toJson(bag.content).replace("'", "◊"));
	    	bag.data.SaveConfig();
	    	bag.changed = false;
		}
		if(Main.config.GetBool("auto-save-message")) Log.Info(Main.plugin, "Bags saved.");
	}
	
	public static void DeleteBag(@NotNull String uuid) {
		for(int i = 0; i < data.size(); i++) {
			Data bag = data.get(i);
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				data.remove(i);
				Log.Info(Main.plugin, String.format("Removed cached data for %s.", uuid));
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to remove cached data for %s.", uuid));
		
	}
	
	protected static List<String> GetBags(@NotNull String playerUUID){
		Log.Debug(Main.plugin, "[DI-32] " + playerUUID);
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), playerUUID)).listFiles())
					.filter(file -> !file.isDirectory())
					.filter(file -> !file.getName().contains(".json"))
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < bags.size(); i++) {
				//Log.Debug(Main.plugin, bags.get(i));
				bags.set(i, bags.get(i).replace(".yml", ""));
			}
			return bags;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
	protected static List<String> GetBagOwners(){
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
	
	public static void MarkBagOpen(@NotNull String uuid,  ItemStack bagItem, Player player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.isOpen = true;
				bag.viewer = player;
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
				return;
			}
		}
		Log.Error(Main.plugin, String.format("Failed to mark bag '%s' as closed, this bag was not found.", uuid));
	}
	
	public List<String> GetTrusted(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.data.GetStringList("trusted");
			}
		}
		return null;
	}
	
	public static String GetOwner(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.data.GetString("owner");
			}
		}
		return null;
	}
	
	public static String GetCreator(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.data.GetString("creator");
			}
		}
		return null;
	}
	
	public static void AddTrusted(@NotNull String uuid, @NotNull String player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				List<String> trusted = bag.data.GetStringList("trusted");
				if(!trusted.contains(player)) {
					trusted.add(player);
					bag.data.Set("trusted", trusted);
					bag.changed = true;
				}
			}
		}
	}
	
	public static void RemoveTrusted(@NotNull String uuid, @NotNull String player) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				List<String> trusted = bag.data.GetStringList("trusted");
				if(trusted.size() == 0) return;
				for(int i = 0; i < trusted.size(); i++) {
					if(trusted.get(i).equalsIgnoreCase(player)) {
						trusted.remove(i);
						bag.data.Set("trusted", trusted);
						bag.changed = true;
					}
				}
			}
		}
	}
	
	public static void SetAutoPickup(@NotNull String uuid, @NotNull String filter) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.data.Set("auto-pickup", filter);
				bag.changed = true;
			}
		}
	}
	
	public static String GetAutoPickup(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				return bag.data.GetString("auto-pickup");
			}
		}
		return null;
	}
	
	public static void RemoveAutoPickup(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.data.Set("auto-pickup", "null");
				bag.changed = true;
			}
		}	
	}
	
	/*public static void SetWeight(@NotNull String uuid, @NotNull double weight) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.data.Set("weight", weight);
				bag.changed = true;
			}
		}
	}*/
	
	public static void SetWeightMax(@NotNull String uuid, @NotNull double weightmax) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.data.Set("weight-max", weightmax);
				bag.changed = true;
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void MarkBagChanged(@NotNull String uuid) {
		for(Data bag : data) {
			if(bag.getUuid().equalsIgnoreCase(uuid)) {
				bag.changed = true;
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
        
        if(Main.VersionCompare(Main.server, ServerVersion.v1_21_1) >= 0) {
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
}
