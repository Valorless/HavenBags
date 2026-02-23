package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.utils.Utils;

public class CustomBags {

	public static Config file;
	public static HashMap<String, ItemStack> bags = new HashMap<String, ItemStack>();
	
	public static void Initiate() {
		file = new Config(Main.plugin, "custom-bags.yml");
		bags.clear();

		//Log.Info(Main.plugin, List().size() + "");
		//Log.Info(Main.plugin, file.GetConfigurationSection("bags").getKeys(false).toString());

		for (String key : List()) {
			ItemStack item;
			Material mat = file.GetMaterial(String.format("bags.%s.material", key));
			if(mat == Material.PLAYER_HEAD) {
				String texture = file.GetString(String.format("bags.%s.texture", key));
				if(!Utils.IsStringNullOrEmpty(texture)) {
					if(texture.chars().count() > 30) {
						item = HeadCreator.itemFromBase64(!Utils.IsStringNullOrEmpty(texture) ? 
								texture : Main.config.GetString("bag.texture"));
					}else {
						item = HeadCreator.itemFromBase64(Main.textures.GetString(String.format("textures.%s", texture)));
					}
				}else {
					item = new ItemStack(mat);
				}
			}else {
				item = new ItemStack(mat);
			}
			ItemMeta meta = item.getItemMeta();
			if(file.HasKey(String.format("bags.%s.displayname", key)))
				meta.setDisplayName(Lang.Parse(file.GetString(String.format("bags.%s.displayname", key)), null));
			if(file.HasKey(String.format("bags.%s.modeldata", key)))
				meta.setCustomModelData(file.GetInt(String.format("bags.%s.modeldata", key)));
			if(file.HasKey(String.format("bags.%s.tooltip", key))) {
	        	if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
	        		meta.setTooltipStyle(NamespacedKey.fromString(file.GetString(String.format("bags.%s.tooltip", key))));
	        	}
			}
			item.setItemMeta(meta);
			
			if(file.HasKey(String.format("bags.%s.itemmodel", key)))
				ItemUtils.SetItemModel(item, file.GetString(String.format("bags.%s.itemmodel", key)));
			
			PDC.SetString(item, "uuid", "null");
			PDC.SetString(item, "owner", "null");
			PDC.SetStringList(item, "lore", file.GetStringList(String.format("bags.%s.lore", key)));
			PDC.SetString(item, "name", file.GetString(String.format("bags.%s.displayname", key)));
			PDC.SetInteger(item, "size", file.GetInt(String.format("bags.%s.properties.size", key)));
			PDC.SetBoolean(item, "binding", !file.GetBool(String.format("bags.%s.properties.ownerless", key)));
			PDC.SetBoolean(item, "upgrade", file.GetBool(String.format("bags.%s.properties.upgradeable", key)));
			PDC.SetBoolean(item, "skin", file.GetBool(String.format("bags.%s.properties.allow-skin-token", key)));
			if(file.HasKey(String.format("bags.%s.custom-content", key))) {
				PDC.SetString(item, "predefined", file.GetString(String.format("bags.%s.custom-content", key)));
			}
			PDC.SetStringList(item, "blacklist", file.GetStringList(String.format("bags.%s.properties.blacklist", key)));
			PDC.SetBoolean(item, "whitelist", file.GetBool(String.format("bags.%s.properties.whitelist", key)));
			PDC.SetBoolean(item, "igb", file.GetBool(String.format("bags.%s.properties.ignoreglobalblacklist", key)));
			PDC.SetString(item, "filter", file.GetString(String.format("bags.%s.properties.autopickup", key)));
			PDC.SetBoolean(item, "climit", file.GetBool(String.format("bags.%s.properties.carry-limit", key)));
			PDC.SetString(item, "tooltip", file.GetString(String.format("bags.%s.tooltip", key)));
			
			bags.put(key, item);
		}
		
	}
	
	public static List<String> List(){
		return new ArrayList<String>(file.GetConfigurationSection("bags").getKeys(false));
	}

	public static void Give(Player player, String value) {
		ItemStack bagItem = CustomBags.bags.get(value);
		String owner = PDC.GetBoolean(bagItem, "binding") ? player.getUniqueId().toString() : "ownerless";
		List<ItemStack> content = new ArrayList<>();
		
		if(PDC.Has(bagItem, "predefined")) {
			String uuid = UUID.randomUUID().toString();
			PDC.SetString(bagItem, "uuid", uuid);
			for(int i = 0; i < PDC.GetInteger(bagItem, "size"); i++) {
				content.add(CustomContent.load(PDC.GetString(bagItem, "predefined")).get(i));
			}
			Data data = BagData.CreateBag(uuid, owner, content, player, bagItem);
			data.setName(PDC.GetString(bagItem, "name"));
		}
		
		HavenBags.UpdateBagLore(bagItem, player);
		
		player.getInventory().addItem(bagItem);
		
	}
	
	
}
