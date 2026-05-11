package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.Nullable;
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
	
	public static void initiate() {
		file = new Config(Main.plugin, "custom-bags.yml");
		bags.clear();

		//Log.Info(Main.plugin, List().size() + "");
		//Log.Info(Main.plugin, file.getConfigurationSection("bags").getKeys(false).toString());

		for (String key : list()) {
			ItemStack item;
			String material = file.getString(String.format("bags.%s.material", key));
			if(material.startsWith("nexo:")){
				String nexoId = material.substring(5);
				item = NexoItems.exists(nexoId) ? NexoItems.itemFromId(nexoId).build() : new ItemStack(Material.PLAYER_HEAD);
			}
			else {
				Material mat = file.getMaterial(String.format("bags.%s.material", key));
				if (mat == Material.PLAYER_HEAD) {
					String texture = file.getString(String.format("bags.%s.texture", key));
					if (!Utils.IsStringNullOrEmpty(texture)) {
						if (texture.length() > 30) {
							item = HeadCreator.itemFromBase64(!Utils.IsStringNullOrEmpty(texture) ?
									texture : Main.config.getString("bag.texture"));
						} else {
							item = HeadCreator.itemFromBase64(Main.textures.getString(String.format("textures.%s", texture)));
						}
					} else {
						item = new ItemStack(mat);
					}
				} else {
					item = new ItemStack(mat);
				}
			}
			ItemMeta meta = item.getItemMeta();
			if(file.hasKey(String.format("bags.%s.displayname", key)))
				meta.setDisplayName(Lang.Parse(file.getString(String.format("bags.%s.displayname", key)), null));
			if(file.hasKey(String.format("bags.%s.modeldata", key)))
				meta.setCustomModelData(file.getInt(String.format("bags.%s.modeldata", key)));
			if(file.hasKey(String.format("bags.%s.tooltip", key))) {
	        	if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
	        		meta.setTooltipStyle(NamespacedKey.fromString(file.getString(String.format("bags.%s.tooltip", key))));
	        	}
			}
			item.setItemMeta(meta);
			
			if(file.hasKey(String.format("bags.%s.itemmodel", key)))
				ItemUtils.SetItemModel(item, file.getString(String.format("bags.%s.itemmodel", key)));
			
			PDC.SetString(item, "uuid", "null");
			PDC.SetString(item, "owner", "null");
			PDC.SetStringList(item, "lore", file.getStringList(String.format("bags.%s.lore", key)));
			PDC.SetString(item, "name", file.getString(String.format("bags.%s.displayname", key)));
			PDC.SetInteger(item, "size", file.getInt(String.format("bags.%s.properties.size", key)));
			PDC.SetBoolean(item, "binding", !file.getBool(String.format("bags.%s.properties.ownerless", key)));
			PDC.SetBoolean(item, "upgrade", file.getBool(String.format("bags.%s.properties.upgradeable", key)));
			PDC.SetBoolean(item, "skin", file.getBool(String.format("bags.%s.properties.allow-skin-token", key)));
			if(file.hasKey(String.format("bags.%s.custom-content", key))) {
				PDC.SetString(item, "predefined", file.getString(String.format("bags.%s.custom-content", key)));
			}
			PDC.SetStringList(item, "blacklist", file.getStringList(String.format("bags.%s.properties.blacklist", key)));
			PDC.SetBoolean(item, "whitelist", file.getBool(String.format("bags.%s.properties.whitelist", key)));
			PDC.SetBoolean(item, "igb", file.getBool(String.format("bags.%s.properties.ignoreglobalblacklist", key)));
			PDC.SetString(item, "filter", file.getString(String.format("bags.%s.properties.autopickup", key)));
			PDC.SetBoolean(item, "climit", file.getBool(String.format("bags.%s.properties.carry-limit", key)));
			PDC.SetString(item, "tooltip", file.getString(String.format("bags.%s.tooltip", key)));
			
			bags.put(key, item);
		}
		
	}
	
	public static List<String> list(){
		return new ArrayList<String>(file.getConfigurationSection("bags").getKeys(false));
	}

	public static void give(Player player, String value) {
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

	/**
	 * Checks if a custom bag key requires a player context when created via
	 * {@link #get(String, Player)}.
	 * <p>
	 * Returns {@code true} when the bag has predefined/custom content and needs a
	 * player to initialize owner-bound data.
	 *
	 * @param key custom bag key
	 * @return {@code true} if a player is required for this bag key
	 */
	public static boolean requiresPlayer(String key){
		ItemStack bagItem = CustomBags.bags.get(key);
		return PDC.Has(bagItem, "predefined");
	}

	/**
	 * Creates a custom bag item for the provided key.
	 * <p>
	 * The {@code player} argument is only used when {@link #requiresPlayer(String)}
	 * returns {@code true} for the provided key. If it returns {@code false}, the
	 * player value is not used for predefined content initialization.
	 *
	 * @param key custom bag key
	 * @param player player context; only required/used when
	 *               {@link #requiresPlayer(String)} is {@code true}
	 * @return the created and configured bag {@link ItemStack}
	 * @throws NullPointerException if a player context is required but {@code player} is {@code null}
	 * @throws IllegalArgumentException if the provided key does not exist in the custom bags configuration
	 */
	public static ItemStack get(String key, @Nullable Player player) {
		if(bags.get(key) == null) {
			throw new IllegalArgumentException("No custom bag found for key: " + key);
		}
		ItemStack bagItem = CustomBags.bags.get(key);
		String owner = PDC.GetBoolean(bagItem, "binding") ? player.getUniqueId().toString() : "ownerless";
		List<ItemStack> content = new ArrayList<>();

		if(PDC.Has(bagItem, "predefined")) {
			if(player == null) throw new NullPointerException("player cannot be null");
			String uuid = UUID.randomUUID().toString();
			PDC.SetString(bagItem, "uuid", uuid);
			for(int i = 0; i < PDC.GetInteger(bagItem, "size"); i++) {
				content.add(CustomContent.load(PDC.GetString(bagItem, "predefined")).get(i));
			}
			Data data = BagData.CreateBag(uuid, owner, content, player, bagItem);
			data.setName(PDC.GetString(bagItem, "name"));
		}

		HavenBags.UpdateBagLore(bagItem, player);

		return bagItem;
	}
	
	
}
