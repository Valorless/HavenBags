package valorless.havenbags.database;

import valorless.havenbags.Database;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.persistentdatacontainer.PDC;
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
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;

public class Files {
	
	public static void saveBag(Bag data) {
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
		
		if(!file.hasKey("uuid")) file.set("uuid", data.getUuid());
		if(!file.hasKey("owner")) file.set("owner", data.getOwner());
		if(!file.hasKey("creator")) file.set("creator", data.getCreator());
		file.set("size", data.getSize());
		file.set("texture", data.getTexture());
		file.set("custommodeldata", data.getModeldata());
		file.set("itemmodel", data.getItemmodel());
		file.set("trusted", data.getTrusted());
		file.set("auto-pickup", data.getAutopickup());
		file.set("weight", data.getWeight());
		file.set("weight-max", data.getWeightMax());
		file.set("autosort", data.hasAutoSort());
		if(data.getMaterial() != null) file.set("material", data.getMaterial().toString());
		if(data.getName() != null) file.set("name", data.getName());
		file.set("blacklist", data.getBlacklist());
		file.set("whitelist", data.isWhitelist());
		file.set("ignoreglobalblacklist", data.isIngoreGlobalBlacklist());
		file.set("magnet", data.hasMagnet());
		file.set("refill", data.hasRefill());
		file.set("effect", data.getEffect());
		file.set("tooltip-style", data.getTooltipStyle());
		file.set("autosort", data.hasAutoCraft());
		
		file.set("content", JsonUtils.toJson(data.getContent()).replace("'", "◊"));
		file.saveConfig();
	}

	public static Bag loadBag(String owner, String uuid) {
		Bag data = new Bag(uuid, owner);
		Config file = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
		
		data.setCreator(file.getString("creator"));
		data.setSize(file.getInt("size"));
		data.setTexture(file.getString("texture"));
		data.setModeldata(file.getInt("custommodeldata"));
		data.setItemmodel(file.getString("itemmodel"));
		data.setTrusted(file.getStringList("trusted"));
		data.setAutopickup(file.getString("auto-pickup"));
		data.setWeight(file.getInt("weight"));
		data.setWeightMax(file.getInt("weight-max"));
		data.setContent(loadContent(file));
		data.setAutoSort((file.hasKey("autosort")) ? file.getBool("autosort") : false);
		data.setMaterial((file.hasKey("material")) ? Material.valueOf(file.getString("material").toUpperCase()) : null);
		data.setName((file.hasKey("name")) ? file.getString("name") : null);
		data.setBlacklist(file.getStringList("blacklist"));
		data.setWhitelist(file.getBool("whitelist"));
		data.setIgnoreGlobalBlacklist(file.getBool("ignoreglobalblacklist"));
		data.setMagnet(file.getBool("magnet"));
		data.setRefill(file.getBool("refill"));
		data.setEffect(file.getString("effect"));
		data.setTooltipStyle(file.getString("tooltip-style"));
		data.setAutoCraft(file.getBool("autocraft"));
		
		return data;
	}
	
	private static List<ItemStack> loadContent(Config file) {
		String uuid = file.getString("uuid");
		List<JsonObject> json = Database.deserializeItemStackList(file.getString("content"));
		
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
			if(Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
				try {
					item = JsonUtils.fromJson(
							FoodComponentFixer.fixFoodJson(entry)
							);
				}catch(Exception E) {
					Log.error(Main.plugin, uuid);
					Log.error(Main.plugin, entry);
					Log.info(Main.plugin, FoodComponentFixer.fixFoodJson(entry));
					E.printStackTrace();
				}
			}else {
				item = JsonUtils.fromJson(entry);
			}
			items.add(item);
		}
		return items;
	}
	
	public static void deleteFile(String owner, String uuid) {
		Config file = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, uuid));
		file.getFile().deleteFile();
	}

	public static List<String> getBags(@NotNull String playerUUID){
		Log.debug(Main.plugin, "[DI-32] " + playerUUID);
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), playerUUID)).listFiles())
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.filter(name -> !name.contains(".json"))
					.collect(Collectors.toList());
            bags.replaceAll(s -> {
                //Log.Debug(Main.plugin, bags.get(i));
                return s.replace(".yml", "");
            });
			return bags;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}
	
}
