package valorless.havenbags.database;

import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Data;
import valorless.havenbags.Main.ServerVersion;
import valorless.havenbags.utils.FoodComponentFixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;

public class Files {
	
	public static void saveBag(Data data) {
		String uuid = data.getUuid();
    	String owner = data.getOwner();
    	
		File path = new File(Main.plugin.getDataFolder() + "/bags");
		File path2 = new File(Main.plugin.getDataFolder() + String.format("/bags/%s", owner));
		File path3 = new File(Main.plugin.getDataFolder() + String.format("/bags/%s/%s.yml", owner, uuid));
		
		if(!path.exists()) path.mkdir();
		if(!path2.exists()) path2.mkdir();
		if(!path3.exists())
			try {
				path3.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		Config file = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
		
		if(!file.HasKey("uuid")) file.Set("uuid", data.getUuid());
		if(!file.HasKey("owner")) file.Set("owner", data.getOwner());
		if(!file.HasKey("creator")) file.Set("creator", data.getCreator());
		file.Set("size", data.getSize());
		file.Set("texture", data.getTexture());
		file.Set("custommodeldata", data.getModeldata());
		file.Set("itemmodel", data.getItemmodel());
		file.Set("trusted", data.getTrusted());
		file.Set("auto-pickup", data.getAutopickup());
		file.Set("weight", data.getWeight());
		file.Set("weight-max", data.getWeightMax());
		file.Set("autosort", data.hasAutoSort());
		if(data.getMaterial() != null) file.Set("material", data.getMaterial().toString());
		if(data.getName() != null) file.Set("name", data.getName());
		
		file.Set("content", JsonUtils.toJson(data.getContent()).replace("'", "◊"));
		file.SaveConfig();
	}

	public static Data loadBag(String owner, String uuid) {
		Data data = new Data(uuid, owner);
		Config file = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
		
		data.setCreator(file.GetString("creator"));
		data.setSize(file.GetInt("size"));
		data.setTexture(file.GetString("texture"));
		data.setModeldata(file.GetInt("custommodeldata"));
		data.setItemmodel(file.GetString("itemmodel"));
		data.setTrusted(file.GetStringList("trusted"));
		data.setAutopickup(file.GetString("auto-pickup"));
		data.setWeight(file.GetInt("weight"));
		data.setWeightMax(file.GetInt("weight-max"));
		data.setContent(loadContent(file));
		data.setAutoSort((file.HasKey("autosort")) ? file.GetBool("autosort") : false);
		data.setMaterial((file.HasKey("material")) ? Material.valueOf(file.GetString("material").toUpperCase()) : null);
		data.setName((file.HasKey("name")) ? JsonUtils.fromJson(file.GetString("name")) : null);
		
		return data;
		
	}
	
	private static List<ItemStack> loadContent(Config file) {
		String uuid = file.GetString("uuid");
		List<JsonObject> json = BagData.deserializeItemStackList(file.GetString("content"));
		
		List<ItemStack> items = new ArrayList<>();
		for(JsonObject e : json) {
			if(e == null) {
				items.add(null); 
				continue;
			}
			String entry = e.toString();
			//Log.Info(Main.plugin, entry + "");
			entry = entry.replace("◊","'");
			ItemStack item = null;
			if(entry.equalsIgnoreCase("null")) {
				items.add(null); 
				continue;
			}
			if(Main.VersionCompare(Main.server, ServerVersion.v1_21_4) >= 0) {
				try {
					item = JsonUtils.fromJson(
							FoodComponentFixer.fixFoodJson(entry)
							);
				}catch(Exception E) {
					Log.Error(Main.plugin, uuid);
					Log.Error(Main.plugin, entry);
					Log.Info(Main.plugin, FoodComponentFixer.fixFoodJson(entry));
					E.printStackTrace();
				}
			}else {
				item = JsonUtils.fromJson(entry);
			}
			items.add(item);
		}
		return items;
	}
	
	public static Config createBag(@NotNull String uuid,@NotNull String owner,@NotNull List<ItemStack> content, Player creator, ItemStack bag) {
		Config bagData = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
		bagData.Set("uuid", uuid);
		bagData.Set("owner", owner);
		if(creator != null) {
			bagData.Set("creator", creator.getUniqueId().toString());
		}else {
			bagData.Set("creator", owner);
		}
		bagData.Set("size", content.size());
		if(bag.getType() == Material.PLAYER_HEAD) {
			bagData.Set("texture", BagData.getTextureValue(bag));
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
		if(NBT.Has(bag, "bag-filter")) {
			bagData.Set("auto-pickup", NBT.GetString(bag, "bag-filter"));
		}else {
			bagData.Set("auto-pickup", "null");
		}
		bagData.Set("weight-max", 0);
		bagData.Set("content", JsonUtils.toJson(content).replace("'", "◊"));
		bagData.Set("autosort", false);
		bagData.SaveConfig();
		return bagData;
	}
	
	public static void deleteFile(String owner, String uuid) {
		Config file = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
		file.GetFile().deleteFile();
	}

	public static List<String> GetBags(@NotNull String playerUUID){
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
	
}
