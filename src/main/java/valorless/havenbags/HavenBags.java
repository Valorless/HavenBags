package valorless.havenbags;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

import valorless.havenbags.BagData.Bag;
import valorless.havenbags.database.BagCache;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.BlacklistNBT;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.datamodels.Sound;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.features.AutoSorter;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.features.CustomData;
import valorless.havenbags.mods.HavenBagsPreview;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.utils.TextFeatures;
import valorless.valorlessutils.utils.Utils;

public class HavenBags {
	private static final Gson gson = new Gson();

	// ngl, forgot what this is used for..
	public static class BagHashes {

		// Static list of HashCodes from bags.
		private static final ArrayList<Integer> hashes = new ArrayList<Integer>();

		public static void add(Integer hash) {
			if(!hashes.contains(hash)) {
				hashes.add(hash);
			}
		}

		public static Boolean contains(Integer hash) {
            return hashes.contains(hash);
        }

	}

	public static Boolean isBag(ItemStack item) {
		if(item == null) return false;
		if(PDC.has(item, "uuid")) {
			return true;
		}
		return false;
	}

	public static Boolean isSkinToken(ItemStack item) {
		if(item == null) return false;
		if(item.hasItemMeta()) {
            return PDC.has(item, "token-skin");
		}
		return false;
	}

	public static String getBagUUID(@NotNull ItemStack item) {
		if(isBag(item)) return PDC.getString(item, "uuid");
		else return null;
	}

	public static void returnBag(ItemStack bag, Player player) {
		Log.debug(Main.plugin, "[DI-104] " + "Returning bag to " + player.getName());
		Log.debug(Main.plugin, "[DI-105] " + "health " + player.getHealth());

		if(player.isDead()) {
			if (Bukkit.getPluginManager().getPlugin("AngelChest") == null) {
				Log.debug(Main.plugin, "[DI-106] " + "Player dead, dropping bag instead.");
				player.getWorld().dropItem(player.getLocation(), bag);
				return;
			}else {
				if(player.getInventory().getItemInMainHand() != null) {
					if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
						Log.debug(Main.plugin, "[DI-107] " + "Hand Empty.");
						player.getInventory().setItemInMainHand(bag);
						return;
					}
				}
				if(player.getInventory().firstEmpty() != -1) {
					player.getInventory().addItem(bag);
				} else {
					player.sendMessage(Lang.get("prefix") + Lang.get("inventory-full"));

					Sound sound = new Sound(Main.config.getString("sound.inventory-full.key"),
							Main.config.getDouble("sound.inventory-full.volume"),
							Main.config.getDouble("sound.inventory-full.pitch"));
					sound.play(player);
					player.getWorld().dropItem(player.getLocation(), bag);
				}
			}
		}else {
			Log.debug(Main.plugin, "[DI-108] " + "Player alive.");
			if(player.getInventory().getItemInMainHand() != null) {
				if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
					Log.debug(Main.plugin, "[DI-109] " + "Hand Empty.");
					player.getInventory().setItemInMainHand(bag);
					return;
				}
			}
			if(player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(bag);
			} else {
				player.sendMessage(Lang.get("prefix") + Lang.get("inventory-full"));
				Sound sound = new Sound(Main.config.getString("sound.inventory-full.key"),
						Main.config.getDouble("sound.inventory-full.volume"),
						Main.config.getDouble("sound.inventory-full.pitch"));
				sound.play(player);
				player.getWorld().dropItem(player.getLocation(), bag);
			}
		}
	}


	/*public static void WriteBagToServer(ItemStack bag, List<ItemStack> inventory, Player player) {
		String uuid = PDC.GetString(bag, "bag-uuid");
    	String owner = PDC.GetString(bag, "bag-owner");
    	Log.Debug(Main.plugin, "Attempting to write bag " + owner + "/" + uuid + " onto server");

    	File bagData;
    	List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < inventory.size(); i++) {
    		cont.add(inventory.get(i));
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
			player.sendMessage("§7[§aHaven§bBags§7]§r §c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
    	}

    }

	public static List<ItemStack> LoadBagContentFromServer(String uuid, String owner, @Nullable Player player){
		String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, uuid);

		File bagData;
		try {
			bagData = new File(path);
		} catch(Exception e) {
			player.sendMessage(e.toString());
			e.printStackTrace();
			return null;
		}
        if(!bagData.exists()) {
        	//player.sendMessage(Name + "§c No bag found with that UUID.");
        	if(player != null) player.sendMessage(Lang.Get("bag-does-not-exist"));
        	return null;
        }
        String content = "";
		try {
			Path filePath = Path.of(path);
			content = Files.readString(filePath);
			return JsonUtils.fromJson(content);
		} catch (IOException e) {
			if(player != null) player.sendMessage("§7[§aHaven§bBags§7]§r §c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:LoadContent()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
			return null;
		}
	}*/

	public static List<ItemStack> loadBagContentFromServer(ItemStack bag){
		return BagData.getBag(getBagUUID(bag), bag).getContent();
	}

	/*public static boolean DoesBagExist(String uuid, String owner, @Nullable Player player) {
		String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, uuid);

		File bagData;
		try {
			bagData = new File(path);
		} catch(Exception e) {
			player.sendMessage(e.toString());
			return false;
		}
        if(!bagData.exists()) {
        	//player.sendMessage(Name + "§c No bag found with that UUID.");
        	if(player != null) player.sendMessage(Lang.Get("bag-does-not-exist"));
        	Log.Debug(Main.plugin, "This bag does not exist.");
        	return false;
        }
        return true;
	}*/

	public static void updatePDC(ItemStack bag) {
		String uuid = PDC.getString(bag, "uuid");
		//String display = bag.getItemMeta().getDisplayName();
		String id = uuid.replace(".json", "");
		Data data = BagData.getBag(id, null);
		//Log.Error(Main.plugin, uuid);
		//Log.Error(Main.plugin, id);
		//Log.Error(Main.plugin, data + "");

		//ItemStack skull = HeadCreator.itemFromBase64(data.GetString("texture"));
		//if(!Utils.IsStringNullOrEmpty(data.GetString("texture"))) {
		//	bag.setItemMeta((SkullMeta)HeadCreator.itemFromBase64(data.GetString("texture")).getItemMeta());
		//}

		//ItemMeta meta = bag.getItemMeta();
		//meta.setDisplayName(display);
		//bag.setItemMeta(meta);

		//PDC.SetString(bag, "bag-uuid", uuid);

		if(bag.getType() == Material.PLAYER_HEAD) {
			String texture = data.getTexture();
			if(!Utils.IsStringNullOrEmpty(texture)) {
				if(!texture.contains("null")) {
					BagData.setTextureValue(bag, texture);
				}
			}
		}else {
			int cmd = data.getModeldata() != null ? data.getModeldata() : 0;
			if(cmd != 0) {
				if(bag.hasItemMeta()) {
					bag.getItemMeta().setCustomModelData(cmd);
				}
			}
			if(Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
				String im = data.getItemmodel();
				if(!Utils.IsStringNullOrEmpty(im)) {
					if(bag.hasItemMeta()) {
						ItemUtils.SetItemModel(bag, im);
					}
				}
			}
		}


		PDC.setString(bag, "owner", data.getOwner());

		if(data.getOwner().equalsIgnoreCase("ownerless")) {
			PDC.setBoolean(bag, "binding", false);
		} else {
			PDC.setBoolean(bag, "binding", true);
		}
		if(isPowerOfNine(data.getSize())) {
			PDC.setinteger(bag, "size", data.getSize());
		}

		if(data.getAutopickup().equalsIgnoreCase("null")) {
			PDC.setString(bag, "filter", null);
		}else {
			PDC.setString(bag, "filter", data.getAutopickup());
		}
		if(Main.weight.getBool("enabled")){
			if(data.getWeightMax() > 0) {
				PDC.setDouble(bag, "weight-limit", data.getWeightMax());
			}else {
				if(Main.weight.getBool("weight-per-size")) {
					PDC.setDouble(bag, "weight-limit", Main.weight.getDouble(String.format("weight-size-%s", data.getSize())));
					BagData.setWeightMax(id, Main.weight.getDouble(String.format("weight-size-%s", data.getSize())));
				}else {
					PDC.setDouble(bag, "weight-limit", Main.weight.getDouble("weight-limit"));
					BagData.setWeightMax(id, Main.weight.getDouble("weight-limit"));
				}
			}
		}
		//PDC.SetString(bag, "bag-creator", data.getCreator());
	}

	public static void updatePDC(ItemStack bag, Data data) {
		String uuid = PDC.getString(bag, "uuid");
		//String display = bag.getItemMeta().getDisplayName();
		String id = uuid.replace(".json", "");
		//Log.Error(Main.plugin, uuid);
		//Log.Error(Main.plugin, id);
		//Log.Error(Main.plugin, data + "");

		//ItemStack skull = HeadCreator.itemFromBase64(data.GetString("texture"));
		//if(!Utils.IsStringNullOrEmpty(data.GetString("texture"))) {
		//	bag.setItemMeta((SkullMeta)HeadCreator.itemFromBase64(data.GetString("texture")).getItemMeta());
		//}

		//ItemMeta meta = bag.getItemMeta();
		//meta.setDisplayName(display);
		//bag.setItemMeta(meta);

		//PDC.SetString(bag, "bag-uuid", uuid);

		if(bag.getType() == Material.PLAYER_HEAD) {
			String texture = data.getTexture();
			if(!Utils.IsStringNullOrEmpty(texture)) {
				if(!texture.contains("null")) {
					BagData.setTextureValue(bag, texture);
				}
			}
		}else {
			int cmd = data.getModeldata() != null ? data.getModeldata() : 0;
			if(cmd != 0) {
				if(bag.hasItemMeta()) {
					bag.getItemMeta().setCustomModelData(cmd);
				}
			}
			if(Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
				String im = data.getItemmodel();
				if(!Utils.IsStringNullOrEmpty(im)) {
					if(bag.hasItemMeta()) {
						ItemUtils.SetItemModel(bag, im);
					}
				}
			}
		}


		PDC.setString(bag, "owner", data.getOwner());

		if(data.getOwner().equalsIgnoreCase("ownerless")) {
			PDC.setBoolean(bag, "binding", false);
		} else {
			PDC.setBoolean(bag, "binding", true);
		}
		if(isPowerOfNine(data.getSize())) {
			PDC.setinteger(bag, "size", data.getSize());
		}

		if(data.getAutopickup().equalsIgnoreCase("null")) {
			PDC.setString(bag, "filter", null);
		}else {
			PDC.setString(bag, "filter", data.getAutopickup());
		}
		if(Main.weight.getBool("enabled")){
			if(data.getWeightMax() > 0) {
				PDC.setDouble(bag, "weight-limit", data.getWeightMax());
			}else {
				if(Main.weight.getBool("weight-per-size")) {
					PDC.setDouble(bag, "weight-limit", Main.weight.getDouble(String.format("weight-size-%s", data.getSize())));
					BagData.setWeightMax(id, Main.weight.getDouble(String.format("weight-size-%s", data.getSize())));
				}else {
					PDC.setDouble(bag, "weight-limit", Main.weight.getDouble("weight-limit"));
					BagData.setWeightMax(id, Main.weight.getDouble("weight-limit"));
				}
			}
		}
		//PDC.SetString(bag, "bag-creator", data.getCreator());
	}

	public static void updateBagItem(ItemStack bag, OfflinePlayer player, boolean...preview) {
		if(bag == null || bag.getType() == Material.AIR) {
			Log.warning(Main.plugin, String.format("Failed to update bag item for player '%s'.\n"
					+ "It's possible they have a mod allowing them to move the item away.", player.getName()));
			return;
		}

		String uuid = HavenBags.getBagUUID(bag);
		
		try {
			// Apply custom data before anything else, so that it can be used in the following methods.
			CustomData.updateBag(bag);
		} catch(Exception e) {
			//Log.Error(Main.plugin, "Failed to apply custom data to bag " + uuid + " for player " + player.getName());
			e.printStackTrace();
		}

		if(BagState.getState(bag) == BagState.USED) {
			if(preview.length == 0) {
				updatePDC(bag);
			}
			updateUsed(bag, BagData.getBag(uuid, bag), player);
		}else if (BagState.getState(bag) == BagState.NEW) {
			updateNew(bag, player);
		}
	}

	private static void updateNew(ItemStack bag, OfflinePlayer player) {
		if(Server.VersionHigherOrEqualTo(Version.v1_21)) {
			ItemUtils.SetMaxStackSize(bag, 1);
		}

		List<Placeholder> placeholders = new ArrayList<Placeholder>();

		ItemMeta bagMeta = bag.getItemMeta();

		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.getStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, player));
		}
		if(PDC.has(bag, "lore")) {
			lore.clear();
			for (String l : PDC.getStringList(bag, "lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, player));
			}
		}
		if(PDC.has(bag, "size")) {
			placeholders.add(new Placeholder("%size%", PDC.getInteger(bag, "size")));
			placeholders.add(new Placeholder("%bag-size%", Lang.parse(Lang.get("bag-size"), placeholders, player)));
			//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
		}

		if(PDC.has(bag, "filter")) {
			placeholders.add(new Placeholder("%filter%", AutoPickup.getFilterDisplayname(PDC.getString(bag, "filter"))));
			placeholders.add(new Placeholder("%bag-auto-pickup%", Lang.parse(Lang.get("bag-auto-pickup"), placeholders, player)));
			//lore.add(Lang.Parse(Lang.Parse(String.format(Lang.Get("bag-auto-pickup"), AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter"))), player)));
			//lore.add(Lang.Parse("&7Auto Loot: " + AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter")), player));
		}

		for(String line : Lang.lang.getStringList("bag-lore-add")) {
			if(line.contains("%bag-auto-pickup%")) {
				if(!PDC.has(bag, "filter")) continue;
				if(PDC.getString(bag, "filter").equalsIgnoreCase("null")) continue;
				lore.add(Lang.parse(line, placeholders, player));
			}
			if(line.contains("%bag-size%")) {
				lore.add(Lang.parse(line, placeholders, player));
			}
		}

		if(PDC.has(bag, "tooltip")) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
				bagMeta.setTooltipStyle(NamespacedKey.fromString(PDC.getString(bag, "tooltip")));
			}
		}

		bagMeta.setLore(lore);
		bag.setItemMeta(bagMeta);
	}

	public static void updateUsed(ItemStack bag, Data data, OfflinePlayer player) {
		if(Server.VersionHigherOrEqualTo(Version.v1_21)) {
			ItemUtils.SetMaxStackSize(bag, 1);
		}

		List<ItemStack> inventory = data.getContent();
		if(data.hasAutoSort()) {
			inventory = AutoSorter.sortInventory(inventory);
		}
		if(Main.plugins.getBool("mods.HavenBagsPreview.enabled")) {
			try {
				NBT.SetString(bag, "bag-preview-content", gson.toJson(new HavenBagsPreview(inventory)));
				NBT.SetString(bag, "bag-uuid", "yes");
				NBT.SetInt(bag, "bag-size", PDC.getInteger(bag, "size"));
			}catch(Exception e) {} // Moved away from NBT, but need it for the mod.
			PDC.setString(bag, "mod", gson.toJson(new HavenBagsPreview(inventory)));
		}

		List<Placeholder> placeholders = new ArrayList<Placeholder>();

		ItemMeta bagMeta = bag.getItemMeta();

		List<ItemStack> cont = new ArrayList<ItemStack>();
		int a = 0;
		List<String> items = new ArrayList<String>();
		if(inventory != null) {
			for(int i = 0; i < inventory.size(); i++) {
				if(PDC.has(inventory.get(i), "locked")) continue;
				cont.add(inventory.get(i));
				if(inventory.get(i) != null && inventory.get(i).getType() != Material.AIR) {
					List<Placeholder> itemph = new ArrayList<Placeholder>();
					if(inventory.get(i).hasItemMeta()) {
						if(inventory.get(i).getItemMeta().hasDisplayName()) {
							itemph.add(new Placeholder("%item%", inventory.get(i).getItemMeta().getDisplayName()));
							itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

							if(inventory.get(i).getAmount() != 1) {
								items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, player));
							} else {
								items.add(Lang.parse(Lang.get("bag-content-item"), itemph, player));
							}
						}
						else if(Server.VersionHigherOrEqualTo(Version.v1_20_5)) {
							if(ItemUtils.HasItemName(inventory.get(i))) {
								itemph.add(new Placeholder("%item%", ItemUtils.GetItemName(inventory.get(i))));
								itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

								if(inventory.get(i).getAmount() != 1) {
									items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, player));
								} else {
									items.add(Lang.parse(Lang.get("bag-content-item"), itemph, player));
								}
							}
							else {
								itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
								itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

								if(inventory.get(i).getAmount() != 1) {
									items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, player));
								} else {
									items.add(Lang.parse(Lang.get("bag-content-item"), itemph, player));
								}
							}
						}
						else {
							itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
							itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

							if(inventory.get(i).getAmount() != 1) {
								items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, player));
							} else {
								items.add(Lang.parse(Lang.get("bag-content-item"), itemph, player));
							}
						}
					}else {
						itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
						itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

						if(inventory.get(i).getAmount() != 1) {
							items.add(Lang.parse(Lang.get("bag-content-item-amount"), itemph, player));
						} else {
							items.add(Lang.parse(Lang.get("bag-content-item"), itemph, player));
						}
					}
					a++;
				}
			}
		}

		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.getStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, player));
		}
		if(PDC.has(bag, "lore")) {
			lore.clear();
			for (String l : PDC.getStringList(bag, "lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, player));
			}
		}
		if(PDC.getBoolean(bag, "binding") == true) {
			placeholders.add(new Placeholder("%owner%", Bukkit.getOfflinePlayer(UUID.fromString(data.getOwner())).getName()));
			placeholders.add(new Placeholder("%bound-to%", Lang.parse(Lang.get("bound-to"), placeholders, player)));
			//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()), player));
		}
		if(PDC.has(bag, "size")) {
			placeholders.add(new Placeholder("%size%", PDC.getInteger(bag, "size")));
			placeholders.add(new Placeholder("%slots_used%", items.size()));
			placeholders.add(new Placeholder("%slots_free%", PDC.getInteger(bag, "size") - items.size()));
			placeholders.add(new Placeholder("%bag-size%", Lang.parse(Lang.get("bag-size"), placeholders, player)));
			//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
		}

		if(PDC.has(bag, "filter")) {
			placeholders.add(new Placeholder("%filter%", AutoPickup.getFilterDisplayname(PDC.getString(bag, "filter"))));
			placeholders.add(new Placeholder("%bag-auto-pickup%", Lang.parse(Lang.get("bag-auto-pickup"), placeholders, player)));
			//lore.add(Lang.Parse(Lang.Parse(String.format(Lang.Get("bag-auto-pickup"), AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter"))), player)));
			//lore.add(Lang.Parse("&7Auto Loot: " + AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter")), player));
		}

		if(PDC.has(bag, "weight") && PDC.has(bag, "weight-limit") && Main.weight.getBool("enabled")) {
			placeholders.add(new Placeholder("%bar%", TextFeatures.createBarWeight(getWeight(bag), PDC.getDouble(bag, "weight-limit"), Main.weight.GetInt("bar-length"))));
			placeholders.add(new Placeholder("%weight%", TextFeatures.limitDecimal(String.valueOf(getWeight(bag)),2)));
			placeholders.add(new Placeholder("%limit%", String.valueOf(PDC.getDouble(bag, "weight-limit").intValue())));
			placeholders.add(new Placeholder("%percent%", TextFeatures.limitDecimal(String.valueOf(Utils.Percent(getWeight(bag), PDC.getDouble(bag, "weight-limit"))), 2) + "%"));
			placeholders.add(new Placeholder("%bag-weight%", Lang.parse(Main.weight.getString("weight-lore"), placeholders, player)));
			//lore.add(Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player));
		}

		boolean hasTrust = false;
		if(!data.getTrusted().isEmpty()) {
			List<String> trust = data.getTrusted();
			String trusted = "";
			if(!trust.isEmpty()) {
				for(int i = 0; i < trust.size(); i++) { 
					if(i != 0) {
						trusted = trusted + ", " + trust.get(i); 
					}else {
						trusted = trust.get(i); 
					}
				}
			}
			placeholders.add(new Placeholder("%trusted%", trusted));
			placeholders.add(new Placeholder("%bag-trusted%", Lang.parse(Lang.get("bag-trusted"), placeholders, player)));

			if(!Utils.IsStringNullOrEmpty(trusted)) hasTrust = true;
		}

		if(data.hasAutoSort()) {
			placeholders.add(new Placeholder("%sorting%", Lang.parse(Lang.get("bag-autosort-on"), placeholders, player)));
		}else {
			placeholders.add(new Placeholder("%sorting%", Lang.parse(Lang.get("bag-autosort-off"), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-autosort%", Lang.parse(Lang.get("bag-autosort"), placeholders, player)));

		if(data.hasMagnet()) {
			placeholders.add(new Placeholder("%magnet%", Lang.parse(Lang.get("bag-magnet-on"), placeholders, player)));
		}else {
			placeholders.add(new Placeholder("%magnet%", Lang.parse(Lang.get("bag-magnet-off"), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-magnet%", Lang.parse(Lang.get("bag-magnet"), placeholders, player)));

		if(data.hasRefill()) {
			placeholders.add(new Placeholder("%refill%", Lang.parse(Lang.get("bag-refill-on"), placeholders, player)));
		}else {
			placeholders.add(new Placeholder("%refill%", Lang.parse(Lang.get("bag-refill-off"), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-refill%", Lang.parse(Lang.get("bag-refill"), placeholders, player)));

		if(data.getEffect() != null) {
			placeholders.add(new Placeholder("%effect%", Lang.parse(BagEffects.getEffectDisplayname(data.getEffect()), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-effect%", Lang.parse(Lang.get("bag-effect"), placeholders, player)));

		for(String line : Lang.lang.getStringList("bag-lore-add")) {
			if(line.contains("%bound-to%") && !PDC.getBoolean(bag, "binding")) continue;
			if(line.contains("%bag-effect%")) {
				if(Lang.lang.getBool("bag-effect-hide")) continue;
				if(data.getEffect() == null) continue;
				if(data.getEffect() != null && data.getEffect().equalsIgnoreCase("null")) continue;
			}
			if(line.contains("%bag-trusted%") && !hasTrust) continue;
			if(line.contains("%bag-auto-pickup%") && !PDC.has(bag, "filter")) continue;
			if(line.contains("%bag-weight%") && !Main.weight.getBool("enabled")) continue;
			if(line.contains("%bag-autosort%") && Lang.lang.getBool("bag-autosort-off-hide")) continue;
			if(line.contains("%bag-magnet%") && Lang.lang.getBool("bag-magnet-off-hide")) continue;
			if(line.contains("%bag-refill%") && Lang.lang.getBool("bag-refill-off-hide")) continue;
			lore.add(Lang.parse(line, placeholders, player));
		}

		for(int i = 0; i < lore.size(); i++) {
			if(lore.get(i).contains("%bag-weight%")) lore.remove(i);
		}

		if(a > 0 && Lang.lang.getBool("show-bag-content")) {
			lore.add(Lang.parse(Lang.get("bag-content-title"), player));
			for(int k = 0; k < items.size(); k++) {
				if(k < Lang.lang.getInt("bag-content-preview-size")) {
					lore.add("  " + items.get(k));
				}

			}
			if(a > Lang.lang.getInt("bag-content-preview-size")) {
				lore.add(Lang.get("bag-content-and-more"));
			}
		}

		if(data.getTooltipStyle() != null) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
				if(!Utils.IsStringNullOrEmpty(data.getTooltipStyle())) {
					bagMeta.setTooltipStyle(NamespacedKey.fromString(data.getTooltipStyle()));
				}
				
			}
		}else {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
				if(Utils.IsStringNullOrEmpty(Main.config.getString("bag.tooltip-style"))) {
					bagMeta.setTooltipStyle(NamespacedKey.fromString(Main.config.getString("bag.tooltip-style")));
				}
			}
		}
		if(PDC.has(bag, "tooltip")) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
				bagMeta.setTooltipStyle(NamespacedKey.fromString(PDC.getString(bag, "tooltip")));
			}
		}

		if(Server.VersionHigherOrEqualTo(Version.v1_21_3)) {	
			if(bagMeta.hasTooltipStyle()) {
				if(bagMeta.getTooltipStyle().toString().equalsIgnoreCase("minecraft:null") || bagMeta.getTooltipStyle().toString().equalsIgnoreCase("minecraft:minecraft")) {
					if(Utils.IsStringNullOrEmpty(Main.config.getString("bag.tooltip-style"))) {
						bagMeta.setTooltipStyle(null);
						data.setTooltipStyle(null);
					}
					else {
						bagMeta.setTooltipStyle(NamespacedKey.fromString(Main.config.getString("bag.tooltip-style")));
						data.setTooltipStyle(Main.config.getString("bag.tooltip-style"));
					}
				}
			}
		}

		bagMeta.setLore(lore);
		bag.setItemMeta(bagMeta);
	}

	public static String capacityTexture(ItemStack bag, List<ItemStack> content) {
		double capacity = usedCapacity(bag, content);
		//Log.Error(Main.plugin, capacity + "");
		Map<Double, String> map = new HashMap<Double, String>();
		for(Object entry : Main.config.getConfigurationSection("capacity-based-textures.textures").getKeys(false)) {
			Double key = Double.valueOf(entry.toString());
			String value = Main.config.getString("capacity-based-textures.textures." + entry.toString());
			map.put(key, value);
		}
		// Sorting the map by keys in descending order
		Map<Double, String> sortedMap = map.entrySet()
				.stream()
				.sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())) // Descending order
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, // Handle duplicate keys
						LinkedHashMap::new // Maintain sorted order
						));

		for(Entry<Double, String> entry : sortedMap.entrySet()) {
			if(capacity >= entry.getKey()) {
				//Log.Error(Main.plugin, entry.getKey() + "");
				return entry.getValue();
			}
		}
		return BagData.getTextureValue(bag);
	}

	public static void updateBagLore(ItemStack bag, Player player, boolean...preview) {
		try {
			updateBagItem(bag, player, preview);
		} catch (Exception e) {
			updateBagItem(bag, player, preview);
		}
	}

	public static void emptyBag(ItemStack bag, Player player) {
		String uuid = PDC.getString(bag, "uuid");
		//String owner = PDC.GetString(bag, "bag-owner");
		Log.debug(Main.plugin, "[DI-110] " + "Attempting to initialize bag items");
		//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, player);
		List<ItemStack> content = BagData.getBag(uuid, bag).getContent();

		Sound sound = new Sound(Main.config.getString("sound.close.key"),
				Main.config.getDouble("sound.close.volume"),
				Main.config.getDouble("sound.close.pitch"));
		sound.play(player);
		for(int i = 0; i < content.size(); i++) {
			try {
				if(PDC.has(content.get(i), "locked")) continue;
				Item dropped = player.getWorld().dropItem(player.getLocation(), content.get(i));
				dropped.setPickupDelay(100);
				content.set(i, null);
			} catch (Exception e) {
				continue;
			}
		}
		//WriteBagToServer(bag, content, player);
		BagData.updateBag(uuid, content);
		updateBagItem(bag, player);
	}

	public static boolean isOwner(ItemStack bag, Player player) {
		String owner = PDC.getString(bag, "owner");
        return owner.equalsIgnoreCase("ownerless") ||
                player.hasPermission("havenbags.bypass") ||
                owner.equalsIgnoreCase(Bukkit.getPlayer(player.getName()).getUniqueId().toString());
	}

	public static boolean inventoryContainsBag(Player player) {
		for(ItemStack item : player.getInventory().getContents()) {
			if(isBag(item)) return true;
		}
        return EtherealBags.hasBags(player.getUniqueId());
    }

	public static ItemStack getDisplayBagItem() {
		ItemStack bagItem;
		String bagTexture = Main.config.getString("bag.texture");

		if(Main.config.getString("bag.type.material").startsWith("nexo:")){
			String nexoId = Main.config.getString("bag.type.material").substring(5);
			return NexoItems.exists(nexoId) ? NexoItems.itemFromId(nexoId).build() : new ItemStack(Material.PLAYER_HEAD);
		}
		if(Main.config.getString("bag.type").equalsIgnoreCase("HEAD")){
			bagItem = HeadCreator.itemFromBase64(bagTexture);
		} else if(Main.config.getString("bag.type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.getMaterial("bag.material"));
		} else {
			Log.error(Main.plugin, (Lang.get("prefix") + "&cbag.type must be either HEAD or ITEM."));
			return null;
		}
		return bagItem;
	}

	public static Double getWeight(ItemStack bag) {
		if(PDC.has(bag, "weight")) {
			return PDC.getDouble(bag, "weight");
		}else {
			try {
				double weight = 0.0;
				String uuid = PDC.getString(bag, "uuid");
				//String owner = PDC.GetString(bag, "bag-owner");
				//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, null);
				List<ItemStack> content = BagData.getBag(uuid, bag).getContent();
				for(ItemStack item : content) {
					weight += (Main.weight.getDouble(item.getType().toString()) * item.getAmount());
				}
				PDC.setDouble(bag, "weight", weight);
				BagData.getBag(uuid, bag).setWeight(weight);
				return weight;
			} catch(Exception e) {
				return (double) 0;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static Double getWeight(List<ItemStack> content) {
		double weight = 0.0;
		for(ItemStack item : content) {
			if(item == null) continue; 
			if(item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
				int cmd = item.getItemMeta().getCustomModelData();
				if(Main.weight.hasKey(item.getType().toString() + "-" + cmd)) {
					try {
						weight += (Main.weight.getDouble(item.getType().toString() + "-" + cmd) * item.getAmount());
					} catch(Exception e) {
						continue;
					}
				}else {
					try {
						weight += (Main.weight.getDouble(item.getType().toString()) * item.getAmount());
					} catch(Exception e) {
						continue;
					}
				}
			}else {
				try {
					weight += (Main.weight.getDouble(item.getType().toString()) * item.getAmount());
				} catch(Exception e) {
					continue;
				}
			}
		}
		return weight;
	}

	@SuppressWarnings("deprecation")
	public static Double itemWeight(ItemStack item) {
		if(item == null) return 0.0;
		if(item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
			int cmd = item.getItemMeta().getCustomModelData();
			if(Main.weight.hasKey(item.getType().toString() + "-" + cmd)) {
				return Main.weight.getDouble(item.getType().toString() + "-" + cmd) * item.getAmount();
			}else {
				return Main.weight.getDouble(item.getType().toString()) * item.getAmount();
			}
		}else {
			return Main.weight.getDouble(item.getType().toString()) * item.getAmount();
		}

		//return Main.weight.GetDouble(item.getType().toString()) * item.getAmount();
	}

	public static boolean canCarry(ItemStack item, ItemStack bag) {
		Log.debug(Main.plugin, "[DI-111] " + "Can carry?");
		hasWeightLimit(bag);
		double maxWeight = PDC.getDouble(bag, "weight-limit");
		double weight = getWeight(bag);
		double itemWeight = itemWeight(item);

		Log.debug(Main.plugin, "[DI-112] " + (weight + itemWeight) + "");
		if(weight + itemWeight <= maxWeight) {
			Log.debug(Main.plugin, "[DI-113] " + "true");
			return true;
		}else {
			Log.debug(Main.plugin, "[DI-114] " + "false");
			return false;
		}
	}

	public static boolean canCarry(ItemStack item, ItemStack bag, List<ItemStack> content) {
		Log.debug(Main.plugin, "[DI-115] " + "Can carry?");
		if(bag == null) return false;
		hasWeightLimit(bag);
		double maxWeight = PDC.getDouble(bag, "weight-limit");
		double weight = getWeight(content);
		double itemWeight = itemWeight(item);

		Log.debug(Main.plugin, (weight + itemWeight) + "");
		if(weight + itemWeight <= maxWeight) {
			Log.debug(Main.plugin, "[DI-116] " + "true");
			return true;
		}else {
			Log.debug(Main.plugin, "[DI-117] " + "false");
			return false;
		}
	}

	public static boolean hasWeightLimit(ItemStack bag) {
		if(PDC.has(bag, "weight-limit")) {
			return true;
		}else {
			if(Main.weight.getBool("weight-per-size")) {
				PDC.setDouble(bag, "weight-limit", Main.weight.getDouble("weight-size-" + PDC.getInteger(bag, "size")));
				BagData.getBag(HavenBags.getBagUUID(bag), bag).setWeight(Main.weight.getDouble("weight-size-" + PDC.getInteger(bag, "size")));
			}else {
				PDC.setDouble(bag, "weight-limit", Main.weight.getDouble("weight-limit"));
				BagData.getBag(HavenBags.getBagUUID(bag), bag).setWeight(Main.weight.getDouble("weight-limit"));
			}
		}
		return false;
	}

	public static boolean addItemToInventory(List<ItemStack> items, int inventorySlots, ItemStack itemToAdd, Player player) {
		Log.debug(Main.plugin, "[DI-118] " + "Put item in bag?");

		items = removeAir(items);

		// Check if there is still space in the list for new items

		if (items.size() >= inventorySlots && allSlotsFull(items, itemToAdd)) {
			Log.debug(Main.plugin, "[DI-119] " + "bag full!");
			return false; // The bag is full and item is dropped
		}

		boolean added = false;
		Log.debug(Main.plugin, "[DI-120] " + "checking bag.");
		for (ItemStack stack : items) {
			if(stack == null) continue;
			if (stack.isSimilar(itemToAdd)) {
				int maxStackSize = stack.getMaxStackSize();
				int totalAmount = stack.getAmount() + itemToAdd.getAmount();

				if (totalAmount <= maxStackSize) {
					Log.debug(Main.plugin, "[DI-121] " + "stack has space.");
					stack.setAmount(totalAmount); // Perfect fit or less
					return true;
				} else {
					Log.debug(Main.plugin, "[DI-122] " + "stack overflow, adjusting.");
					stack.setAmount(maxStackSize); // Max out the stack
					itemToAdd.setAmount(totalAmount - maxStackSize); // Adjust remaining
					added = true; // Partially added
				}
			}
		}

		// Try to add remaining part of itemToAdd in a new slot if not all added
		if (!added || itemToAdd.getAmount() > 0) {
			if (items.size() < inventorySlots) {
				items.add(itemToAdd.clone());
				itemToAdd.setAmount(0);
				Log.debug(Main.plugin, "[DI-123] " + "success.");
				return true; // New stack added successfully
			} else {
				Log.debug(Main.plugin, "[DI-124] " + "no space.");
				dropItem(player.getLocation(), itemToAdd); // No space left, drop the remaining items
				return false;
			}
		}
		Log.debug(Main.plugin, "[DI-125] " + "failed.");

		return true; // This line is theoretically unreachable
	}

	public static HashMap<Boolean, List<ItemStack>> addItemToEtherealInventory(Player player, String bagId, ItemStack itemToAdd) {
		HashMap<Boolean, List<ItemStack>> result = new HashMap<Boolean, List<ItemStack>>();
		Log.debug(Main.plugin, "[DI-118] " + "Put item in bag? (ethereal)");

		List<ItemStack> items = EtherealBags.getBagContentsOrNull(player.getUniqueId(), bagId);

		// Check if there is still space in the list for new items

		if (EtherealBags.isBagFull(player.getUniqueId(), bagId)) {
			Log.debug(Main.plugin, "[DI-119] " + "bag full!");
			result.put(false, items);
			return result;
		}

		Log.debug(Main.plugin, "[DI-120] " + "checking bag.");
		for (ItemStack stack : items) {
			if(stack == null) continue;
			if (stack.isSimilar(itemToAdd)) {
				int maxStackSize = stack.getMaxStackSize();
				int totalAmount = stack.getAmount() + itemToAdd.getAmount();

				if (totalAmount <= maxStackSize) {
					Log.debug(Main.plugin, "[DI-121] " + "stack has space.");
					stack.setAmount(totalAmount); // Perfect fit or less
					result.put(true, items);
					return result;
				} else {
					Log.debug(Main.plugin, "[DI-122] " + "stack overflow, adjusting.");
					stack.setAmount(maxStackSize); // Max out the stack
					itemToAdd.setAmount(totalAmount - maxStackSize); // Adjust remaining
				}
			}
		}

		// Try to add remaining part of itemToAdd in a new slot if not all added
		if (itemToAdd.getAmount() > 0) {
			Log.debug(Main.plugin, "[DI-124] " + "adding new stack.");
			int free = -1;
			for(int i = 0; i < items.size(); i++) {
				if(items.get(i) == null || items.get(i).getType() == Material.AIR) {
					free = i;
					break;
				}
			}
			if(free != -1) {
				items.set(free, itemToAdd.clone());
				itemToAdd.setAmount(0);
				Log.debug(Main.plugin, "[DI-123] " + "success.");
				result.put(true, items);
				return result;
			}else {
				result.put(false, items);
				return result;
			}
		}
		Log.debug(Main.plugin, "[DI-125] " + "failed.");
		result.put(false, items);
		return result;
	}

	private static boolean allSlotsFull(List<ItemStack> items, ItemStack add) {
		for (ItemStack item : items) {
			if(item.getAmount() == item.getMaxStackSize()) continue;
			if(item.isSimilar(add)) {
                return item.getMaxStackSize() < item.getAmount() + add.getAmount();
			}
		}
		return true; // All stacks are full
	}

	private static List<ItemStack> removeAir(List<ItemStack> items){
		Log.debug(Main.plugin, "[DI-126] " + "removing air, if any.");
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i) == null) {
				items.remove(i);
				continue;
			}else {
				if(items.get(i).getType() == Material.AIR) items.remove(i);
			}
		}
		return items;
	}

	private static void dropItem(Location location, ItemStack itemStack) {
		location.getWorld().dropItemNaturally(location, itemStack);
	}

	public static boolean isItemBlacklisted(ItemStack item, Data... bagData) {
		if(item == null) return false;
		if(item.getType() == Material.AIR) return false;

		// Check bag's own blacklist
		if(bagData != null && bagData.length != 0) {
			Data data = bagData[0];
			if(data.getBlacklist() != null && !data.getBlacklist().isEmpty()) {
				Log.debug(Main.plugin, "[DI-243] " + "Checking bag's whitelist.");
				boolean whitelist = data.isWhitelist();
				if(whitelist) Log.debug(Main.plugin, "[DI-244] " + "Treating blacklist as whitelist!");
				for(String entry : data.getBlacklist()) {
					Log.debug(Main.plugin, entry);
					Material mat = null;
					int cmd = 0;
					if(entry.contains(":")) {
						mat = Material.valueOf(entry.split(":")[0]);
						cmd = Integer.parseInt(entry.split(":")[1]);
					}else if(entry.contains("-")) {
						mat = Material.valueOf(entry.split("-")[0]);
						cmd = Integer.parseInt(entry.split("-")[1]);
					}
					else {
						mat = Material.valueOf(entry);
					}
					if(item.getType() == mat) {
						Log.debug(Main.plugin, "[DI-245] " + "Material blacklisted!");
                        return !whitelist;
                    }
					if(item.hasItemMeta()) {					
						if(item.getItemMeta().hasCustomModelData()) {
							if(cmd == item.getItemMeta().getCustomModelData()) {
								Log.debug(Main.plugin, "[DI-246] " + "CustomModelData blacklisted!");
                                return !whitelist;
                            }
						}
					}
				}
				if(whitelist) return true; // If using as whitelist, and not found, return true
				if(data.isIngoreGlobalBlacklist()) return false; // If ignoring global blacklist, return false
			}
		}


		Config blacklist = Main.blacklist;
		if(!blacklist.getBool("enabled")) {
			return false;
		}
		boolean whitelist = blacklist.getBool("use-as-whitelist");
		if(item.getType() == Material.AIR) return false;
		//if(HavenBags.IsBag(item));
		if(whitelist) Log.debug(Main.plugin, "[DI-127] " + "Treating blacklist as whitelist!");
		Log.debug(Main.plugin, "[DI-128] " + "Is item blacklisted?");
		//Log.Debug(Main.plugin, item.toString());	
		List<Material> materials = new ArrayList<Material>();
		List<String> names = blacklist.getStringList("blacklist.displayname");
		List<Integer> cmd = blacklist.getIntList("blacklist.custommodeldata");
		List<BlacklistNBT> nbt = new ArrayList<BlacklistNBT>();

		for(String mat : blacklist.getStringList("blacklist.materials")) {
			//Log.Debug(Main.plugin, "Blacklisted Material: " + mat);	
			materials.add(Material.valueOf(mat));
		}

		for(String n : blacklist.getStringList("blacklist.nbt")) {
			String[] split = n.split(":");
			if(split.length != 1) {
				//Log.Debug(Main.plugin, "Blacklisted NBT key: " + split[0]);
				//Log.Debug(Main.plugin, "Blacklisted NBT value: " + split[1]);
				nbt.add(new BlacklistNBT(split[0], split[1]));
			}else {
				//Log.Debug(Main.plugin, "Blacklisted NBT key: " + split[0]);
				//Log.Debug(Main.plugin, "Blacklisted NBT value: " + "null");
				nbt.add(new BlacklistNBT(split[0], null));
			}
		}

		if(materials.contains(item.getType())) {
			Log.debug(Main.plugin, "[DI-129] " + "Material blacklisted!");
            return !whitelist;
        }

		if(item.hasItemMeta()) {
			for(String name : names) {
				if(name.equalsIgnoreCase(Lang.removeColorFormatting(item.getItemMeta().getDisplayName()))) {
					Log.debug(Main.plugin, "[DI-130] " + "Name blacklisted!");
                    return !whitelist;
                }
			}

			for(Integer c : cmd) {
				if(item.getItemMeta().hasCustomModelData()) {
					if(c == item.getItemMeta().getCustomModelData()) {
						Log.debug(Main.plugin, "[DI-208] " + "CustomModelData blacklisted!");
                        return !whitelist;
                    }
				}
			}
		}

		for(BlacklistNBT nk : nbt) {
			if(PDC.has(item, nk.key)) {
				Log.debug(Main.plugin, "[DI-131] " + "NBT blacklisted!");
                return !whitelist;
            }
		}

        return whitelist;
    }

	public static boolean canCarryMoreBags(Player player) {
		int max = Main.config.getInt("bags-carry-max");
		int invBags = 0;

		for(ItemStack item : player.getInventory().getContents()) {
			if(isBag(item)) invBags++;
		}

        return invBags < max;
    }

	/*public static boolean IsPlayerTrusted(ItemStack item, String player) {
		if(!PDC.Has(item, "bag-trust")) return false;
		List<String> list = PDC.GetStringList(item, "bag-trust");
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).equalsIgnoreCase(player)) {
				return true;
			}
		}
		return false;
	}*/

	public static ItemStack createSkinToken(String value, TokenType type) {
		String name = Main.config.getString("token.skin.displayname");
		Material material = Main.config.getMaterial("token.skin.material");
		int cmd = Main.config.getInt("token.skin.custommodeldata");
		String itemModel = Main.config.getString("token.skin.itemmodel");
		String skin = null;
		List<String> lore = Main.config.getStringList("token.skin.lore");
		List<Placeholder> ph = new ArrayList<Placeholder>();
		boolean isBase64 = (long) value.length() > 30;
		if(!isBase64) {
			skin = Main.textures.getString(String.format("textures.%s", value));
		}
		//Log.Info(Main.plugin, "Creating skin token with value: " + value + " and resolved skin: " + skin);

		ItemStack item = new ItemStack(material);
		// Set this first to give the item ItemMeta
		PDC.setString(item, "token-skin", value);
		PDC.setString(item, "token-type", type.toString());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Lang.parse(name, ph));
		if(cmd > 0) {
			meta.setCustomModelData(cmd);
		}else {
			try {
				meta.setCustomModelData(Integer.valueOf(value));
			}catch(Exception e) {
				try {
					ItemUtils.SetItemModel(item, value);
				}catch(Exception E) {}
			}
		}
		List<String> l = new ArrayList<String>();
		for (String line : lore) {
			if(!Utils.IsStringNullOrEmpty(line)) {
				l.add(Lang.parse(line.replace("%skin%", isBase64 ? "" : value), null));
			}
		}
		meta.setLore(l);
		if(!Utils.IsStringNullOrEmpty(itemModel)) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
				meta.setItemModel(NamespacedKey.fromString(itemModel));
			}
		}
		item.setItemMeta(meta);

		if(material == Material.PLAYER_HEAD && (isBase64 || skin != null)) {
			BagData.setTextureValue(item, skin != null ? skin : value);
		}

		return item;
	}

	public static ItemStack createEffectToken(String value) {
		String name = Main.config.getString("token.effect.displayname");
		Material material = Main.config.getMaterial("token.effect.material");
		int cmd = Main.config.getInt("token.effect.custommodeldata");
		List<String> lore = Main.config.getStringList("token.effect.lore");
		String skin = Main.config.getString("token.effect.texture");
		String itemModel = Main.config.getString("token.effect.itemmodel");
		List<Placeholder> ph = new ArrayList<Placeholder>();
		ph.add(new Placeholder("%effect%", BagEffects.getEffectDisplayname(value)));

		ItemStack item = new ItemStack(material);
		// Set this first to give the item ItemMeta
		PDC.setString(item, "token-effect", value);
		PDC.setString(item, "token-type", TokenType.Effect.toString());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Lang.parse(name, ph));
		if(cmd > 0) {
			meta.setCustomModelData(cmd);
		}
		List<String> l = new ArrayList<String>();
		for (String line : lore) {
			if(!Utils.IsStringNullOrEmpty(line)) {
				l.add(Lang.parse(line, ph));
			}
		}
		meta.setLore(l);
		if(!Utils.IsStringNullOrEmpty(itemModel)) {
			if(Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
				meta.setItemModel(NamespacedKey.fromString(itemModel));
			}
		}
		item.setItemMeta(meta);

		if(material == Material.PLAYER_HEAD) {
			if(Base64Validator.isValidBase64(skin)) {
				BagData.setTextureValue(item, skin);
			}else {
				Log.error(Main.plugin, "token.effect.texture is not a valid base64 skin.");
			}
		}

		return item;
	}

	public static boolean isBagFull(ItemStack bag) {
		try {
			int size = PDC.getInteger(bag, "size");
			List<ItemStack> content = BagData.getBag(HavenBags.getBagUUID(bag), null).getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(Objects::isNull);
            return content.size() >= size;
		}catch(Exception e) {
			return false;
		}
	}

	public static boolean isBagFull(UUID uuid) {
		try {
			Data data = BagCache.get(uuid);
			int size = data.getSize();
			List<ItemStack> content = data.getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(Objects::isNull);
            return content.size() >= size;
		}catch(Exception e) {
			return false;
		}
	}

	public static boolean isBagFull(String uuid) {
		try {
			Data data = BagCache.get(UUID.fromString(uuid));
			int size = data.getSize();
			List<ItemStack> content = data.getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(Objects::isNull);
            return content.size() >= size;
		}catch(Exception e) {
			return false;
		}
	}

	public static boolean isBagEmpty(ItemStack bag) {
		try {
			int size = PDC.getInteger(bag, "size");
			List<ItemStack> content = BagData.getBag(HavenBags.getBagUUID(bag), null).getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(Objects::isNull);
            return size == 0 || content.isEmpty();
		}catch(Exception e) {
			return false;
		}
	}

	public static boolean isBagEmpty(UUID uuid) {
		try {
			Data data = BagCache.get(uuid);
			int size = data.getSize();
			List<ItemStack> content = data.getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(Objects::isNull);
            return size == 0 || content.isEmpty();
		}catch(Exception e) {
			return false;
		}
	}

	public static boolean isBagEmpty(String uuid) {
		try {
			Data data = BagCache.get(UUID.fromString(uuid));
			int size = data.getSize();
			List<ItemStack> content = data.getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(Objects::isNull);
            return size == 0 || content.isEmpty();
		}catch(Exception e) {
			return false;
		}
	}

	public static int slotsEmpty(ItemStack bag) {
		int size = PDC.getInteger(bag, "size");
		List<ItemStack> content = BagData.getBag(HavenBags.getBagUUID(bag), null).getContent();
		content.removeIf(Objects::isNull);
		content.removeIf(item -> item.getType() == Material.AIR);
		return size - content.size();
	}

	public static double usedCapacity(ItemStack bag, List<ItemStack> content) {
		//Log.Error(Main.plugin, "content " + PDC.GetInt(bag, "bag-size"));
		double size = Double.parseDouble(PDC.getInteger(bag, "size") + ".0");
		//Log.Error(Main.plugin, "size " + size);
		List<ItemStack> used = BagData.getBag(HavenBags.getBagUUID(bag), null).getContent();
		used.removeIf(Objects::isNull);
		used.removeIf(item -> item.getType() == Material.AIR);
		//Log.Error(Main.plugin, "empty " + (size - used.size()));
		//Double empty = size - Double.valueOf(used.size() + ".0");
		//Log.Error(Main.plugin, "used " + used.size());
		return (used.size()/size)*100;
	}

	public static List<Bag> getBagsDataInInventory(Player player) {
		List<Bag> bags = new ArrayList<Bag>();
		//Log.Debug(Main.plugin, "[DI-156-1] " + "Checking for bags.");
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.isBag(i) && BagState.getState(i) == BagState.USED) {
				bags.add(new Bag(i, HavenBags.loadBagContentFromServer(i)));
			}
		}
		return bags;
	}

	public static int getBagsInInventory(Player player) {
		int bags = 0;
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.isBag(i)) {
				bags++;
			}
		}
		return bags;
	}

	public static int getBagsInInventoryCarryLimit(Player player) {
		int bags = 0;
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.isBag(i)) {
				if(PDC.has(i, "climit") && PDC.getBoolean(i, "climit")) {
					continue;
				}
				bags++;
			}
		}
		return bags;
	}

	public static int countItems(List<ItemStack> items, Material material) {
		int count = 0;
		for (ItemStack item : items) {
			if (item != null && item.getType() == material) {
				count += item.getAmount();
			}
		}
		return count;
	}

	public static int getBagSlotsInInventory(Player player) {
		int count = 0;

		for(Bag bag : HavenBags.getBagsDataInInventory(player)) {
			count += BagData.getBag(HavenBags.getBagUUID(bag.item), null).getSize();
		}

		return count;
	}

	public static boolean hasOthersBag(Player player) {
		boolean access = false;
		String uuid = player.getUniqueId().toString();
		for(Bag bag : HavenBags.getBagsDataInInventory(player)) {
			if(BagData.getBag(HavenBags.getBagUUID(bag.item), null).getOwner().equalsIgnoreCase(uuid)) access = true;
			for(String trusted : BagData.getBag(HavenBags.getBagUUID(bag.item), null).getTrusted()) {
                if (trusted.equalsIgnoreCase(uuid)) {
                    access = true;
                    break;
                }
			}
			if(player.hasPermission("havenbags.bypass")) access = true;
		}

		return !access;
	}

	public static Integer findClosestNine(Integer size) {
		if(size <= 9) return 9;
		if(size <= 18) return 18;
		if(size <= 27) return 27;
		if(size <= 36) return 36;
		if(size <= 45) return 45;
		if(size <= 54) return 54;
		return 54;
	}

	public static boolean isPowerOfNine(int size) {
        return size % 9 == 0;
    }
}
