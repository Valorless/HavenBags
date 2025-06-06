package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.nbt.NBT;
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
								texture : Main.config.GetString("bag-texture"));
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
			item.setItemMeta(meta);
			
			if(file.HasKey(String.format("bags.%s.itemmodel", key)))
				ItemUtils.SetItemModel(item, file.GetString(String.format("bags.%s.itemmodel", key)));
			
			NBT.SetString(item, "bag-uuid", "null");
			NBT.SetString(item, "bag-owner", "null");
			NBT.SetStringList(item, "bag-lore", file.GetStringList(String.format("bags.%s.lore", key)));
			NBT.SetString(item, "bag-name", file.GetString(String.format("bags.%s.displayname", key)));
			NBT.SetInt(item, "bag-size", file.GetInt(String.format("bags.%s.properties.size", key)));
			NBT.SetBool(item, "bag-canBind", !file.GetBool(String.format("bags.%s.properties.ownerless", key)));
			NBT.SetBool(item, "bag-upgrade", file.GetBool(String.format("bags.%s.properties.upgradeable", key)));
			NBT.SetBool(item, "bag-skin", file.GetBool(String.format("bags.%s.properties.allow-skin-token", key)));
			if(file.HasKey(String.format("bags.%s.custom-content", key))) {
				NBT.SetString(item, "bag-predefined", file.GetString(String.format("bags.%s.custom-content", key)));
			}
			NBT.SetStringList(item, "bag-blacklist", file.GetStringList(String.format("bags.%s.properties.blacklist", key)));
			NBT.SetBool(item, "bag-whitelist", file.GetBool(String.format("bags.%s.properties.whitelist", key)));
			NBT.SetBool(item, "bag-igb", file.GetBool(String.format("bags.%s.properties.ignoreglobalblacklist", key)));
			NBT.SetString(item, "bag-filter", file.GetString(String.format("bags.%s.properties.autopickup", key)));
			
			bags.put(key, item);
		}
		
	}
	
	public static List<String> List(){
		return new ArrayList<String>(file.GetConfigurationSection("bags").getKeys(false));
	}

	public static void Give(Player player, String value) {
		ItemStack bagItem = CustomBags.bags.get(value);
		String owner = NBT.GetBool(bagItem, "bag-canBind") ? player.getUniqueId().toString() : "ownerless";
		List<ItemStack> content = new ArrayList<>();
		
		if(NBT.Has(bagItem, "bag-predefined")) {
			String uuid = UUID.randomUUID().toString();
			NBT.SetString(bagItem, "bag-uuid", uuid);
			for(int i = 0; i < NBT.GetInt(bagItem, "bag-size"); i++) {
				content.add(CustomContent.load(NBT.GetString(bagItem, "bag-predefined")).get(i));
			}
			BagData.CreateBag(uuid, owner, content, player, bagItem);
		}
		
		HavenBags.UpdateBagLore(bagItem, player);
		
		player.getInventory().addItem(bagItem);
		
	}
	
	
}
