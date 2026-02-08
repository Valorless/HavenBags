package valorless.havenbags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.enums.TokenType;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.features.AutoSorter;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.mods.HavenBagsPreview;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.utils.TextFeatures;
import valorless.valorlessutils.sound.SFX;
import valorless.valorlessutils.utils.Utils;

public class HavenBags {
	private static final Gson gson = new Gson();

	public static class BagHashes {
		
		// Static list of HashCodes from bags.
		private static ArrayList<Integer> hashes = new ArrayList<Integer>();
		
		public static void Add(Integer hash) {
			if(!hashes.contains(hash)) {
				hashes.add(hash);
			}
		}
		
		public static Boolean Contains(Integer hash) {
			if(hashes.contains(hash)) return true;
			return false;
		}
		
	}
	
	public static Boolean IsBag(ItemStack item) {
		if(item == null) return false;
		if(PDC.Has(item, "uuid")) {
			return true;
		}
		return false;
	}

	public static Boolean IsSkinToken(ItemStack item) {
		if(item == null) return false;
		if(item.hasItemMeta()) {
			if(PDC.Has(item, "token-skin")) {
				return true;
			}
		}
		return false;
	}
	
	public static String GetBagUUID(@NotNull ItemStack item) {
		if(IsBag(item)) return PDC.GetString(item, "uuid");
		else return null;
	}
	
	public enum BagState { Null, New, Used }
	public static BagState BagState(ItemStack item) {
		if(item == null) return BagState.Null;
		if(IsBag(item)) {
			if(!BagData.BagExists(GetBagUUID(item))) {
				return BagState.New;
			}else {
				return BagState.Used;
			}
		}
		return BagState.Null;
	}

	
	public static void ReturnBag(ItemStack bag, Player player) {
		Log.Debug(Main.plugin, "[DI-104] " + "Returning bag to " + player.getName());
		Log.Debug(Main.plugin, "[DI-105] " + "health " + player.getHealth());
    	
    	if(player.isDead()) {
    		if (Bukkit.getPluginManager().getPlugin("AngelChest") == null) {
    			Log.Debug(Main.plugin, "[DI-106] " + "Player dead, dropping bag instead.");
    			player.getWorld().dropItem(player.getLocation(), bag);
    			return;
    		}else {
    			if(player.getInventory().getItemInMainHand() != null) {
        			if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
    	   	 			Log.Debug(Main.plugin, "[DI-107] " + "Hand Empty.");
        				player.getInventory().setItemInMainHand(bag);
        				return;
        			}
        		}
        		if(player.getInventory().firstEmpty() != -1) {
        			player.getInventory().addItem(bag);
        		} else {
        			player.sendMessage(Lang.Get("prefix") + Lang.Get("inventory-full"));
    				SFX.Play(Main.config.GetString("sound.inventory-full.key"), 
    						Main.config.GetDouble("sound.inventory-full.volume").floatValue(), 
    						Main.config.GetDouble("sound.inventory-full.pitch").floatValue(), player);
        			player.getWorld().dropItem(player.getLocation(), bag);
        		}
    		}
    	}else {
    		Log.Debug(Main.plugin, "[DI-108] " + "Player alive.");
    		if(player.getInventory().getItemInMainHand() != null) {
    			if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
	   	 			Log.Debug(Main.plugin, "[DI-109] " + "Hand Empty.");
    				player.getInventory().setItemInMainHand(bag);
    				return;
    			}
    		}
    		if(player.getInventory().firstEmpty() != -1) {
    			player.getInventory().addItem(bag);
    		} else {
    			player.sendMessage(Lang.Get("prefix") + Lang.Get("inventory-full"));
				SFX.Play(Main.config.GetString("sound.inventory-full.key"), 
						Main.config.GetDouble("sound.inventory-full.volume").floatValue(), 
						Main.config.GetDouble("sound.inventory-full.pitch").floatValue(), player);
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
	
	public static List<ItemStack> LoadBagContentFromServer(ItemStack bag){
		return BagData.GetBag(GetBagUUID(bag), bag).getContent();
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
	
	public static void UpdatePDC(ItemStack bag) {
		String uuid = PDC.GetString(bag, "uuid");
		//String display = bag.getItemMeta().getDisplayName();
		String id = uuid.replace(".json", "");
		Data data = BagData.GetBag(id, null);
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
			int cmd = data.getModeldata();
			if(cmd != 0) {
				if(bag.hasItemMeta()) {
					bag.getItemMeta().setCustomModelData(cmd);
				}
			}
			String im = data.getItemmodel();
			if(!Utils.IsStringNullOrEmpty(im)) {
				if(bag.hasItemMeta()) {
					ItemUtils.SetItemModel(bag, im);
				}
			}
		}
		
		
		PDC.SetString(bag, "owner", data.getOwner());
		
		if(data.getOwner().equalsIgnoreCase("ownerless")) {
			PDC.SetBoolean(bag, "binding", false);
		} else {
			PDC.SetBoolean(bag, "binding", true);
		}
		if(isPowerOfNine(data.getSize())) {
			PDC.SetInteger(bag, "size", data.getSize());
		}
		
		if(data.getAutopickup().equalsIgnoreCase("null")) {
			PDC.SetString(bag, "filter", null);
		}else {
			PDC.SetString(bag, "filter", data.getAutopickup());
		}
		if(Main.weight.GetBool("enabled")){
			if(data.getWeightMax() > 0) {
				PDC.SetDouble(bag, "weight-limit", data.getWeightMax());
			}else {
				if(Main.weight.GetBool("weight-per-size")) {
					PDC.SetDouble(bag, "weight-limit", Main.weight.GetDouble(String.format("weight-size-%s", data.getSize())));
					BagData.SetWeightMax(id, Main.weight.GetDouble(String.format("weight-size-%s", data.getSize())));
				}else {
					PDC.SetDouble(bag, "weight-limit", Main.weight.GetDouble("weight-limit"));
					BagData.SetWeightMax(id, Main.weight.GetDouble("weight-limit"));
				}
			}
		}
		//PDC.SetString(bag, "bag-creator", data.getCreator());
	}
	
	public static void UpdatePDC(ItemStack bag, Data data) {
		String uuid = PDC.GetString(bag, "uuid");
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
			int cmd = data.getModeldata();
			if(cmd != 0) {
				if(bag.hasItemMeta()) {
					bag.getItemMeta().setCustomModelData(cmd);
				}
			}
			String im = data.getItemmodel();
			if(!Utils.IsStringNullOrEmpty(im)) {
				if(bag.hasItemMeta()) {
					ItemUtils.SetItemModel(bag, im);
				}
			}
		}
		
		
		PDC.SetString(bag, "owner", data.getOwner());
		
		if(data.getOwner().equalsIgnoreCase("ownerless")) {
			PDC.SetBoolean(bag, "binding", false);
		} else {
			PDC.SetBoolean(bag, "binding", true);
		}
		if(isPowerOfNine(data.getSize())) {
			PDC.SetInteger(bag, "size", data.getSize());
		}
		
		if(data.getAutopickup().equalsIgnoreCase("null")) {
			PDC.SetString(bag, "filter", null);
		}else {
			PDC.SetString(bag, "filter", data.getAutopickup());
		}
		if(Main.weight.GetBool("enabled")){
			if(data.getWeightMax() > 0) {
				PDC.SetDouble(bag, "weight-limit", data.getWeightMax());
			}else {
				if(Main.weight.GetBool("weight-per-size")) {
					PDC.SetDouble(bag, "weight-limit", Main.weight.GetDouble(String.format("weight-size-%s", data.getSize())));
					BagData.SetWeightMax(id, Main.weight.GetDouble(String.format("weight-size-%s", data.getSize())));
				}else {
					PDC.SetDouble(bag, "weight-limit", Main.weight.GetDouble("weight-limit"));
					BagData.SetWeightMax(id, Main.weight.GetDouble("weight-limit"));
				}
			}
		}
		//PDC.SetString(bag, "bag-creator", data.getCreator());
	}

	public static void UpdateBagItem(ItemStack bag, OfflinePlayer player, boolean...preview) {
		if(bag == null || bag.getType() == Material.AIR) {
			Log.Warning(Main.plugin, String.format("Failed to update bag item for player '%s'.\n"
					+ "It's possible they have a mod allowing them to move the item away.", player.getName()));
			return;
		}
		
		String uuid = HavenBags.GetBagUUID(bag);

		if(BagState(bag) == BagState.Used) {
			if(preview.length == 0) {
				UpdatePDC(bag);
			}
			UpdateUsed(bag, BagData.GetBag(uuid, bag), player);
		}else if (BagState(bag) == BagState.New) {
			UpdateNew(bag, player);
		}
	}
	
	private static void UpdateNew(ItemStack bag, OfflinePlayer player) {
		if(Server.VersionHigherOrEqualTo(Version.v1_21)) {
			ItemUtils.SetMaxStackSize(bag, 1);
		}
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();

		ItemMeta bagMeta = bag.getItemMeta();

		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.GetStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
		}
		if(PDC.Has(bag, "lore")) {
			lore.clear();
			for (String l : PDC.GetStringList(bag, "lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
			}
		}
		if(PDC.Has(bag, "size")) {
			placeholders.add(new Placeholder("%size%", PDC.GetInteger(bag, "size")));
			placeholders.add(new Placeholder("%bag-size%", Lang.Parse(Lang.Get("bag-size"), placeholders, player)));
			//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
		}

		if(PDC.Has(bag, "filter")) {
			placeholders.add(new Placeholder("%filter%", AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "filter"))));
			placeholders.add(new Placeholder("%bag-auto-pickup%", Lang.Parse(Lang.Get("bag-auto-pickup"), placeholders, player)));
			//lore.add(Lang.Parse(Lang.Parse(String.format(Lang.Get("bag-auto-pickup"), AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter"))), player)));
			//lore.add(Lang.Parse("&7Auto Loot: " + AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter")), player));
		}
		
        for(String line : Lang.lang.GetStringList("bag-lore-add")) {
        	if(line.contains("%bag-auto-pickup%")) {
        		if(!PDC.Has(bag, "filter")) continue;
        		if(PDC.GetString(bag, "filter").equalsIgnoreCase("null")) continue;
        		lore.add(Lang.Parse(line, placeholders, player));
        	}
        	if(line.contains("%bag-size%")) {
        		lore.add(Lang.Parse(line, placeholders, player));
        	}
        }
        
        bagMeta.setLore(lore);
		bag.setItemMeta(bagMeta);
	}
	
	public static void UpdateUsed(ItemStack bag, Data data, OfflinePlayer player) {
		if(Server.VersionHigherOrEqualTo(Version.v1_21)) {
			ItemUtils.SetMaxStackSize(bag, 1);
		}
		
		List<ItemStack> inventory = data.getContent();
		if(data.hasAutoSort()) {
			inventory = AutoSorter.SortInventory(inventory);
		}
		if(Main.plugins.GetBool("mods.HavenBagsPreview.enabled")) {
			try {
				NBT.SetString(bag, "bag-preview-content", gson.toJson(new HavenBagsPreview(inventory)));
				NBT.SetString(bag, "bag-uuid", "yes");
				NBT.SetInt(bag, "bag-size", PDC.GetInteger(bag, "size"));
			}catch(Exception e) {} // Moved away from NBT, but need it for the mod.
		}

		List<Placeholder> placeholders = new ArrayList<Placeholder>();

		ItemMeta bagMeta = bag.getItemMeta();

		List<ItemStack> cont = new ArrayList<ItemStack>();
		int a = 0;
		List<String> items = new ArrayList<String>();
		if(inventory != null) {
			for(int i = 0; i < inventory.size(); i++) {
				if(PDC.Has(inventory.get(i), "locked")) continue;
				cont.add(inventory.get(i));
				if(inventory.get(i) != null && inventory.get(i).getType() != Material.AIR) {
					List<Placeholder> itemph = new ArrayList<Placeholder>();
					if(inventory.get(i).hasItemMeta()) {
						if(inventory.get(i).getItemMeta().hasDisplayName()) {
							itemph.add(new Placeholder("%item%", inventory.get(i).getItemMeta().getDisplayName()));
							itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

							if(inventory.get(i).getAmount() != 1) {
								items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
							} else {
								items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
							}
						}
						else if(Server.VersionHigherOrEqualTo(Version.v1_20_5)) {
							if(ItemUtils.HasItemName(inventory.get(i))) {
								itemph.add(new Placeholder("%item%", ItemUtils.GetItemName(inventory.get(i))));
								itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

								if(inventory.get(i).getAmount() != 1) {
									items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
								} else {
									items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
								}
							}
							else {
								itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
								itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

								if(inventory.get(i).getAmount() != 1) {
									items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
								} else {
									items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
								}
							}
						}
						else {
							itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
							itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

							if(inventory.get(i).getAmount() != 1) {
								items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
							} else {
								items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
							}
						}
					}else {
						itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
						itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));

						if(inventory.get(i).getAmount() != 1) {
							items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
						} else {
							items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
						}
					}
					a++;
				}
			}
		}
		
		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.GetStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
		}
		if(PDC.Has(bag, "lore")) {
			lore.clear();
			for (String l : PDC.GetStringList(bag, "lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
			}
		}
		if(PDC.GetBoolean(bag, "binding") == true) {
			placeholders.add(new Placeholder("%owner%", Bukkit.getOfflinePlayer(UUID.fromString(data.getOwner())).getName()));
			placeholders.add(new Placeholder("%bound-to%", Lang.Parse(Lang.Get("bound-to"), placeholders, player)));
			//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()), player));
		}
		if(PDC.Has(bag, "size")) {
			placeholders.add(new Placeholder("%size%", PDC.GetInteger(bag, "size")));
			placeholders.add(new Placeholder("%slots_used%", items.size()));
			placeholders.add(new Placeholder("%slots_free%", PDC.GetInteger(bag, "size") - items.size()));
			placeholders.add(new Placeholder("%bag-size%", Lang.Parse(Lang.Get("bag-size"), placeholders, player)));
			//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
		}

		if(PDC.Has(bag, "filter")) {
			placeholders.add(new Placeholder("%filter%", AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "filter"))));
			placeholders.add(new Placeholder("%bag-auto-pickup%", Lang.Parse(Lang.Get("bag-auto-pickup"), placeholders, player)));
			//lore.add(Lang.Parse(Lang.Parse(String.format(Lang.Get("bag-auto-pickup"), AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter"))), player)));
			//lore.add(Lang.Parse("&7Auto Loot: " + AutoPickup.GetFilterDisplayname(PDC.GetString(bag, "bag-filter")), player));
		}

		if(PDC.Has(bag, "weight") && PDC.Has(bag, "weight-limit") && Main.weight.GetBool("enabled")) {
			placeholders.add(new Placeholder("%bar%", TextFeatures.CreateBarWeight(GetWeight(bag), PDC.GetDouble(bag, "weight-limit"), Main.weight.GetInt("bar-length"))));
			placeholders.add(new Placeholder("%weight%", TextFeatures.LimitDecimal(String.valueOf(GetWeight(bag)),2)));
			placeholders.add(new Placeholder("%limit%", String.valueOf(PDC.GetDouble(bag, "weight-limit").intValue())));
			placeholders.add(new Placeholder("%percent%", TextFeatures.LimitDecimal(String.valueOf(Utils.Percent(GetWeight(bag), PDC.GetDouble(bag, "weight-limit"))), 2) + "%"));
			placeholders.add(new Placeholder("%bag-weight%", Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)));
			//lore.add(Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player));
		}

		boolean hasTrust = false;
		if(!data.getTrusted().isEmpty()) {
			List<String> trust = data.getTrusted();
			String trusted = "";
			if(trust.size() != 0) {
				for(int i = 0; i < trust.size(); i++) { 
					if(i != 0) {
						trusted = trusted + ", " + trust.get(i); 
					}else {
						trusted = trust.get(i); 
					}
				}
			}
			placeholders.add(new Placeholder("%trusted%", trusted));
			placeholders.add(new Placeholder("%bag-trusted%", Lang.Parse(Lang.Get("bag-trusted"), placeholders, player)));

			if(!Utils.IsStringNullOrEmpty(trusted)) hasTrust = true;
		}
		
		if(data.hasAutoSort()) {
			placeholders.add(new Placeholder("%sorting%", Lang.Parse(Lang.Get("bag-autosort-on"), placeholders, player)));
		}else {
			placeholders.add(new Placeholder("%sorting%", Lang.Parse(Lang.Get("bag-autosort-off"), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-autosort%", Lang.Parse(Lang.Get("bag-autosort"), placeholders, player)));

		if(data.hasMagnet()) {
			placeholders.add(new Placeholder("%magnet%", Lang.Parse(Lang.Get("bag-magnet-on"), placeholders, player)));
		}else {
			placeholders.add(new Placeholder("%magnet%", Lang.Parse(Lang.Get("bag-magnet-off"), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-magnet%", Lang.Parse(Lang.Get("bag-magnet"), placeholders, player)));

		if(data.hasRefill()) {
			placeholders.add(new Placeholder("%refill%", Lang.Parse(Lang.Get("bag-refill-on"), placeholders, player)));
		}else {
			placeholders.add(new Placeholder("%refill%", Lang.Parse(Lang.Get("bag-refill-off"), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-refill%", Lang.Parse(Lang.Get("bag-refill"), placeholders, player)));

		if(data.getEffect() != null) {
			placeholders.add(new Placeholder("%effect%", Lang.Parse(BagEffects.getEffectDisplayname(data.getEffect()), placeholders, player)));
		}
		placeholders.add(new Placeholder("%bag-effect%", Lang.Parse(Lang.Get("bag-effect"), placeholders, player)));
		
        for(String line : Lang.lang.GetStringList("bag-lore-add")) {
        	if(line.contains("%bound-to%") && !PDC.GetBoolean(bag, "binding")) continue;
    		if(line.contains("%bag-effect%")) {
    			if(Lang.lang.GetBool("bag-effect-hide")) continue;
    			if(data.getEffect() == null) continue;
    			if(data.getEffect() != null && data.getEffect().equalsIgnoreCase("null")) continue;
    		}
    		if(line.contains("%bag-trusted%") && !hasTrust) continue;
    		if(line.contains("%bag-auto-pickup%") && !PDC.Has(bag, "filter")) continue;
    		if(line.contains("%bag-weight%") && !Main.weight.GetBool("enabled")) continue;
    		if(line.contains("%bag-autosort%") && Lang.lang.GetBool("bag-autosort-off-hide")) continue;
    		if(line.contains("%bag-magnet%") && Lang.lang.GetBool("bag-magnet-off-hide")) continue;
    		if(line.contains("%bag-refill%") && Lang.lang.GetBool("bag-refill-off-hide")) continue;
        	lore.add(Lang.Parse(line, placeholders, player));
        }
        
        for(int i = 0; i < lore.size(); i++) {
        	if(lore.get(i).contains("%bag-weight%")) lore.remove(i);
        }
        
        if(a > 0 && Lang.lang.GetBool("show-bag-content")) {
        	lore.add(Lang.Parse(Lang.Get("bag-content-title"), player));
        	for(int k = 0; k < items.size(); k++) {
        		if(k < Lang.lang.GetInt("bag-content-preview-size")) {
        			lore.add("  " + items.get(k));
        		}

        	}
        	if(a > Lang.lang.GetInt("bag-content-preview-size")) {
        		lore.add(Lang.Get("bag-content-and-more"));
        	}
        }
        
        bagMeta.setLore(lore);
		bag.setItemMeta(bagMeta);
	}
	
	public static String CapacityTexture(ItemStack bag, List<ItemStack> content) {
		Double capacity = UsedCapacity(bag, content);
		//Log.Error(Main.plugin, capacity + "");
		Map<Double, String> map = new HashMap<Double, String>();
		for(Object entry : Main.config.GetConfigurationSection("capacity-based-textures.textures").getKeys(false)) {
			Double key = Double.valueOf(entry.toString());
			String value = Main.config.GetString("capacity-based-textures.textures." + entry.toString());
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
	
	public static void UpdateBagLore(ItemStack bag, Player player, boolean...preview) {
		try {
			UpdateBagItem(bag, player, preview);
		} catch (Exception e) {
			UpdateBagItem(bag, player, preview);
		}
	}
	
	public static void EmptyBag(ItemStack bag, Player player) {
		String uuid = PDC.GetString(bag, "uuid");
    	//String owner = PDC.GetString(bag, "bag-owner");
		Log.Debug(Main.plugin, "[DI-110] " + "Attempting to initialize bag items");
		//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, player);
		List<ItemStack> content = BagData.GetBag(uuid, bag).getContent();
		SFX.Play(Main.config.GetString("sound.close.key"), 
				Main.config.GetDouble("sound.close.volume").floatValue(), 
				Main.config.GetDouble("sound.close.pitch").floatValue(), player);
		for(int i = 0; i < content.size(); i++) {
			try {
				if(PDC.Has(content.get(i), "locked")) continue;
				Item dropped = player.getWorld().dropItem(player.getLocation(), content.get(i));
				dropped.setPickupDelay(100);
				content.set(i, null);
			} catch (Exception e) {
				continue;
			}
		}
		//WriteBagToServer(bag, content, player);
		BagData.UpdateBag(uuid, content);
		UpdateBagItem(bag, player);
	}
	
	public static boolean IsOwner(ItemStack bag, Player player) {
    	String owner = PDC.GetString(bag, "owner");
    	if(owner.equalsIgnoreCase("ownerless")) {
    		return true;
    	} else if (player.hasPermission("havenbags.bypass")) {
			return true;
		} else if (owner.equalsIgnoreCase(Bukkit.getPlayer(player.getName()).getUniqueId().toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean InventoryContainsBag(Player player) {
		for(ItemStack item : player.getInventory().getContents()) {
			if(IsBag(item)) return true;
		}
		if(EtherealBags.hasBags(player.getUniqueId())) return true;
		return false;
	}
	
	public static ItemStack GetDisplayBagItem() {
		ItemStack bagItem;
		String bagTexture = Main.config.GetString("bag.texture");
		if(Main.config.GetString("bag.type").equalsIgnoreCase("HEAD")){
			bagItem = HeadCreator.itemFromBase64(bagTexture);
		} else if(Main.config.GetString("bag.type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.GetMaterial("bag.material"));
		} else {
			Log.Error(Main.plugin, (Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM."));
			return null;
		}
		return bagItem;
	}
	
	public static Double GetWeight(ItemStack bag) {
		if(PDC.Has(bag, "weight")) {
			return PDC.GetDouble(bag, "weight");
		}else {
			try {
				Double weight = 0.0;
				String uuid = PDC.GetString(bag, "uuid");
    			//String owner = PDC.GetString(bag, "bag-owner");
				//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, null);
				List<ItemStack> content = BagData.GetBag(uuid, bag).getContent();
				for(ItemStack item : content) {
					weight += (Main.weight.GetDouble(item.getType().toString()) * item.getAmount());
				}
				PDC.SetDouble(bag, "weight", weight);
				BagData.GetBag(uuid, bag).setWeight(weight);
				return weight;
			} catch(Exception e) {
				return (double) 0;
			}
		}
	}
	
	public static Double GetWeight(List<ItemStack> content) {
		Double weight = 0.0;
		for(ItemStack item : content) {
			if(item == null) continue; 
			if(item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
				int cmd = item.getItemMeta().getCustomModelData();
				if(Main.weight.HasKey(item.getType().toString() + "-" + cmd)) {
					try {
						weight += (Main.weight.GetDouble(item.getType().toString() + "-" + cmd) * item.getAmount());
					} catch(Exception e) {
						continue;
					}
				}else {
					try {
						weight += (Main.weight.GetDouble(item.getType().toString()) * item.getAmount());
					} catch(Exception e) {
						continue;
					}
				}
			}else {
				try {
					weight += (Main.weight.GetDouble(item.getType().toString()) * item.getAmount());
				} catch(Exception e) {
					continue;
				}
			}
		}
		return weight;
	}
	
	public static Double ItemWeight(ItemStack item) {
		if(item == null) return 0.0;
		if(item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
			int cmd = item.getItemMeta().getCustomModelData();
			if(Main.weight.HasKey(item.getType().toString() + "-" + cmd)) {
				return Main.weight.GetDouble(item.getType().toString() + "-" + cmd) * item.getAmount();
			}else {
				return Main.weight.GetDouble(item.getType().toString()) * item.getAmount();
			}
		}else {
			return Main.weight.GetDouble(item.getType().toString()) * item.getAmount();
		}
		
		//return Main.weight.GetDouble(item.getType().toString()) * item.getAmount();
	}
	
	public static boolean CanCarry(ItemStack item, ItemStack bag) {
		Log.Debug(Main.plugin, "[DI-111] " + "Can carry?");
		HasWeightLimit(bag);
		double maxWeight = PDC.GetDouble(bag, "weight-limit");
		double weight = GetWeight(bag);
		double itemWeight = ItemWeight(item);
		
		Log.Debug(Main.plugin, "[DI-112] " + (weight + itemWeight) + "");
		if(weight + itemWeight <= maxWeight) {
			Log.Debug(Main.plugin, "[DI-113] " + "true");
			return true;
		}else {
			Log.Debug(Main.plugin, "[DI-114] " + "false");
			return false;
		}
	}
	
	public static boolean CanCarry(ItemStack item, ItemStack bag, List<ItemStack> content) {
		Log.Debug(Main.plugin, "[DI-115] " + "Can carry?");
		if(bag == null) return false;
		HasWeightLimit(bag);
		double maxWeight = PDC.GetDouble(bag, "weight-limit");
		double weight = GetWeight(content);
		double itemWeight = ItemWeight(item);
		
		Log.Debug(Main.plugin, (weight + itemWeight) + "");
		if(weight + itemWeight <= maxWeight) {
			Log.Debug(Main.plugin, "[DI-116] " + "true");
			return true;
		}else {
			Log.Debug(Main.plugin, "[DI-117] " + "false");
			return false;
		}
	}
	
	public static boolean HasWeightLimit(ItemStack bag) {
		if(PDC.Has(bag, "weight-limit")) {
			return true;
		}else {
			if(Main.weight.GetBool("weight-per-size")) {
				PDC.SetDouble(bag, "weight-limit", Main.weight.GetDouble("weight-size-" + PDC.GetInteger(bag, "size")));
				BagData.GetBag(HavenBags.GetBagUUID(bag), bag).setWeight(Main.weight.GetDouble("weight-size-" + PDC.GetInteger(bag, "size")));
			}else {
				PDC.SetDouble(bag, "weight-limit", Main.weight.GetDouble("weight-limit"));
				BagData.GetBag(HavenBags.GetBagUUID(bag), bag).setWeight(Main.weight.GetDouble("weight-limit"));
			}
		}
		return false;
	}
	
	public static boolean AddItemToInventory(List<ItemStack> items, int inventorySlots, ItemStack itemToAdd, Player player) {
		Log.Debug(Main.plugin, "[DI-118] " + "Put item in bag?");	
		
		items = RemoveAir(items);
		
	    // Check if there is still space in the list for new items
		
	    if (items.size() >= inventorySlots && allSlotsFull(items, itemToAdd)) {
			Log.Debug(Main.plugin, "[DI-119] " + "bag full!");	
	        return false; // The bag is full and item is dropped
	    }
		
	    boolean added = false;
		Log.Debug(Main.plugin, "[DI-120] " + "checking bag.");	
	    for (ItemStack stack : items) {
	    	if(stack == null) continue;
	        if (stack.isSimilar(itemToAdd)) {
	            int maxStackSize = stack.getMaxStackSize();
	            int totalAmount = stack.getAmount() + itemToAdd.getAmount();

	            if (totalAmount <= maxStackSize) {
	        		Log.Debug(Main.plugin, "[DI-121] " + "stack has space.");	
	                stack.setAmount(totalAmount); // Perfect fit or less
	                return true;
	            } else {
	        		Log.Debug(Main.plugin, "[DI-122] " + "stack overflow, adjusting.");	
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
        		Log.Debug(Main.plugin, "[DI-123] " + "success.");	
	            return true; // New stack added successfully
	        } else {
        		Log.Debug(Main.plugin, "[DI-124] " + "no space.");	
	            DropItem(player.getLocation(), itemToAdd); // No space left, drop the remaining items
	            return false;
	        }
	    }
		Log.Debug(Main.plugin, "[DI-125] " + "failed.");	

	    return true; // This line is theoretically unreachable
	}
	
	public static HashMap<Boolean, List<ItemStack>> AddItemToEtherealInventory(Player player, String bagId, ItemStack itemToAdd) {
		HashMap<Boolean, List<ItemStack>> result = new HashMap<Boolean, List<ItemStack>>();
		Log.Debug(Main.plugin, "[DI-118] " + "Put item in bag? (ethereal)");	
		
		List<ItemStack> items = EtherealBags.getBagContentsOrNull(player.getUniqueId(), bagId);
		
	    // Check if there is still space in the list for new items
		
	    if (EtherealBags.isBagFull(player.getUniqueId(), bagId)) {
			Log.Debug(Main.plugin, "[DI-119] " + "bag full!");	
            result.put(false, items);
            return result;
	    }
		
		Log.Debug(Main.plugin, "[DI-120] " + "checking bag.");	
	    for (ItemStack stack : items) {
	    	if(stack == null) continue;
	        if (stack.isSimilar(itemToAdd)) {
	            int maxStackSize = stack.getMaxStackSize();
	            int totalAmount = stack.getAmount() + itemToAdd.getAmount();

	            if (totalAmount <= maxStackSize) {
	        		Log.Debug(Main.plugin, "[DI-121] " + "stack has space.");	
	                stack.setAmount(totalAmount); // Perfect fit or less
	                result.put(true, items);
	                return result;
	            } else {
	        		Log.Debug(Main.plugin, "[DI-122] " + "stack overflow, adjusting.");	
	                stack.setAmount(maxStackSize); // Max out the stack
	                itemToAdd.setAmount(totalAmount - maxStackSize); // Adjust remaining
	            }
	        }
	    }

	    // Try to add remaining part of itemToAdd in a new slot if not all added
	    if (itemToAdd.getAmount() > 0) {
	    	Log.Debug(Main.plugin, "[DI-124] " + "adding new stack.");
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
	    		Log.Debug(Main.plugin, "[DI-123] " + "success.");	
	            result.put(true, items);
	            return result;
	    	}else {
	            result.put(false, items);
	            return result;
	    	}
	    }
		Log.Debug(Main.plugin, "[DI-125] " + "failed.");
        result.put(false, items);
        return result;
	}
	
	private static boolean allSlotsFull(List<ItemStack> items, ItemStack add) {
	    for (ItemStack item : items) {
	    	if(item.getAmount() == item.getMaxStackSize()) continue;
	    	if(item.isSimilar(add)) {
	    		if(item.getMaxStackSize() >= item.getAmount() + add.getAmount()) {
	    			return false;
	    		}else {
	    			return true;
	    		}
	    	}
	    }
	    return true; // All stacks are full
	}
	
	private static List<ItemStack> RemoveAir(List<ItemStack> items){
		Log.Debug(Main.plugin, "[DI-126] " + "removing air, if any.");	
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

	private static void DropItem(Location location, ItemStack itemStack) {
	    location.getWorld().dropItemNaturally(location, itemStack);
	}
	
	public static boolean IsItemBlacklisted(ItemStack item, Data... bagData) {
		if(item == null) return false;
		if(item.getType() == Material.AIR) return false;
		
		// Check bag's own blacklist
		if(bagData != null && bagData.length != 0) {
			Data data = bagData[0];
			if(data.getBlacklist() != null && !data.getBlacklist().isEmpty()) {
				Log.Debug(Main.plugin, "[DI-243] " + "Checking bag's whitelist.");	
				boolean whitelist = data.isWhitelist();
				if(whitelist) Log.Debug(Main.plugin, "[DI-244] " + "Treating blacklist as whitelist!");	
				for(String entry : data.getBlacklist()) {
					Log.Debug(Main.plugin, entry);
					Material mat = null;
					int cmd = 0;
					if(entry.contains(":")) {
						mat = Material.valueOf(entry.split(":")[0]);
						cmd = Integer.valueOf(entry.split(":")[1]);
					}else if(entry.contains("-")) {
						mat = Material.valueOf(entry.split("-")[0]);
						cmd = Integer.valueOf(entry.split("-")[1]);
					}
					else {
						mat = Material.valueOf(entry);
					}
					if(item.getType() == mat) {
						Log.Debug(Main.plugin, "[DI-245] " + "Material blacklisted!");	
						if(whitelist) return false;
						return true;
					}
					if(item.hasItemMeta()) {					
						if(item.getItemMeta().hasCustomModelData()) {
							if(cmd == item.getItemMeta().getCustomModelData()) {
								Log.Debug(Main.plugin, "[DI-246] " + "CustomModelData blacklisted!");	
								if(whitelist) return false;
								return true;
							}
						}
					}
				}
				if(whitelist) return true; // If using as whitelist, and not found, return true
				if(data.isIngoreGlobalBlacklist()) return false; // If ignoring global blacklist, return false
			}
		}
		
		
		Config blacklist = Main.blacklist;
		if(!blacklist.GetBool("enabled")) {
			return false;
		}
		boolean whitelist = blacklist.GetBool("use-as-whitelist");
		if(item.getType() == Material.AIR) return false;
		//if(HavenBags.IsBag(item));
		if(whitelist) Log.Debug(Main.plugin, "[DI-127] " + "Treating blacklist as whitelist!");	
		Log.Debug(Main.plugin, "[DI-128] " + "Is item blacklisted?");	
		//Log.Debug(Main.plugin, item.toString());	
		List<Material> materials = new ArrayList<Material>();
		List<String> names = blacklist.GetStringList("blacklist.displayname");
		List<Integer> cmd = blacklist.GetIntList("blacklist.custommodeldata");
		List<BlacklistNBT> nbt = new ArrayList<BlacklistNBT>();
		
		for(String mat : blacklist.GetStringList("blacklist.materials")) {
			//Log.Debug(Main.plugin, "Blacklisted Material: " + mat);	
			materials.add(Material.valueOf(mat));
		}
		
		for(@SuppressWarnings("unused") String name : names) {
			//Log.Debug(Main.plugin, "Blacklisted Name: " + name);	
		}
		
		for(String n : blacklist.GetStringList("blacklist.nbt")) {
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
			Log.Debug(Main.plugin, "[DI-129] " + "Material blacklisted!");	
			if(whitelist) return false;
			return true;
		}
		
		if(item.hasItemMeta()) {
			for(String name : names) {
				if(name.equalsIgnoreCase(Lang.RemoveColorFormatting(item.getItemMeta().getDisplayName()))) {
					Log.Debug(Main.plugin, "[DI-130] " + "Name blacklisted!");	
					if(whitelist) return false;
					return true;
				}
			}
			
			for(Integer c : cmd) {
				if(item.getItemMeta().hasCustomModelData()) {
					if(c == item.getItemMeta().getCustomModelData()) {
						Log.Debug(Main.plugin, "[DI-208] " + "CustomModelData blacklisted!");	
						if(whitelist) return false;
						return true;
					}
				}
			}
		}
		
		for(BlacklistNBT nk : nbt) {
			if(PDC.Has(item, nk.key)) {
				Log.Debug(Main.plugin, "[DI-131] " + "NBT blacklisted!");	
				if(whitelist) return false;
				return true;
			}
		}

		if(whitelist) return true;
		return false;
	}
	
	public static class BlacklistNBT {
		public String key;
		public String value;
		public BlacklistNBT(String key, String value) {
			this.key = key; this.value = value;
		}
	}
	
	public static boolean CanCarryMoreBags(Player player) {
		int max = Main.config.GetInt("bags-carry-max");
		int invBags = 0;
		
		for(ItemStack item : player.getInventory().getContents()) {
			if(IsBag(item)) invBags++;
		}
		
		if(invBags >= max) {
			return false;
		}
		
		return true;
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
	
	public static ItemStack CreateSkinToken(String value, TokenType type) {
		String name = Main.config.GetString("token.skin.displayname");
		Material material = Main.config.GetMaterial("token.skin.material");
		int cmd = Main.config.GetInt("token.skin.custommodeldata");
		String skin = null;
		List<String> lore = Main.config.GetStringList("token.skin.lore");
		List<Placeholder> ph = new ArrayList<Placeholder>();
		if(value.chars().count() < 30) {
			skin = Main.textures.GetString(String.format("textures.%s", value));
		}
		//Log.Info(Main.plugin, "Creating skin token with value: " + value + " and resolved skin: " + skin);
		
		ItemStack item = new ItemStack(material);
		// Set this first to give the item ItemMeta
		PDC.SetString(item, "token-skin", value);
		PDC.SetString(item, "token-type", type.toString());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Lang.Parse(name, ph));
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
				l.add(Lang.Parse(line.replace("%skin%", value.chars().count() > 30 ? "" : value), null));
			}
		}
		meta.setLore(l);
		item.setItemMeta(meta);
		
		if(material == Material.PLAYER_HEAD && (value.chars().count() > 30 || skin != null)) {
			BagData.setTextureValue(item, skin != null ? skin : value);
		}
		
		return item;
	}
	
	public static ItemStack CreateEffectToken(String value) {
		String name = Main.config.GetString("token.effect.displayname");
		Material material = Main.config.GetMaterial("token.effect.material");
		int cmd = Main.config.GetInt("token.effect.custommodeldata");
		List<String> lore = Main.config.GetStringList("token.effect.lore");
		String skin = Main.config.GetString("token.effect.texture");
		List<Placeholder> ph = new ArrayList<Placeholder>();
		ph.add(new Placeholder("%effect%", BagEffects.getEffectDisplayname(value)));
		
		ItemStack item = new ItemStack(material);
		// Set this first to give the item ItemMeta
		PDC.SetString(item, "token-effect", value);
		PDC.SetString(item, "token-type", TokenType.Effect.toString());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Lang.Parse(name, ph));
		if(cmd > 0) {
			meta.setCustomModelData(cmd);
		}
		List<String> l = new ArrayList<String>();
		for (String line : lore) {
			if(!Utils.IsStringNullOrEmpty(line)) {
				l.add(Lang.Parse(line, ph));
			}
		}
		meta.setLore(l);
		item.setItemMeta(meta);
		
		if(material == Material.PLAYER_HEAD) {
			if(Base64Validator.isValidBase64(skin)) {
				BagData.setTextureValue(item, skin);
			}else {
				Log.Error(Main.plugin, "token.effect.texture is not a valid base64 skin.");
			}
		}
		
		return item;
	}
	
	public static boolean IsBagFull(ItemStack bag) {
		try {
			int size = PDC.GetInteger(bag, "size");
			List<ItemStack> content = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(item -> item == null);
			if(content.size() >= size) return true;
			else return false;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static boolean IsBagFull(UUID uuid) {
		try {
			Data data = BagCache.get(uuid);
			int size = data.getSize();
			List<ItemStack> content = data.getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(item -> item == null);
			if(content.size() >= size) return true;
			else return false;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static boolean IsBagFull(String uuid) {
		try {
			Data data = BagCache.get(UUID.fromString(uuid));
			int size = data.getSize();
			List<ItemStack> content = data.getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(item -> item == null);
			if(content.size() >= size) return true;
			else return false;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static int SlotsEmpty(ItemStack bag) {
		int size = PDC.GetInteger(bag, "size");
		List<ItemStack> content = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
		content.removeIf(item -> item == null);
		content.removeIf(item -> item.getType() == Material.AIR);
		return size - content.size();
	}
	
	public static double UsedCapacity(ItemStack bag, List<ItemStack> content) {
		//Log.Error(Main.plugin, "content " + PDC.GetInt(bag, "bag-size"));
		Double size = Double.valueOf(PDC.GetInteger(bag, "size") + ".0");
		//Log.Error(Main.plugin, "size " + size);
		List<ItemStack> used = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
		used.removeIf(item -> item == null);
		used.removeIf(item -> item.getType() == Material.AIR);
		//Log.Error(Main.plugin, "empty " + (size - used.size()));
		//Double empty = size - Double.valueOf(used.size() + ".0");
		//Log.Error(Main.plugin, "used " + used.size());
		return (used.size()/size)*100;
	}
	
	public static List<Bag> GetBagsDataInInventory(Player player) {
		List<Bag> bags = new ArrayList<Bag>();
		//Log.Debug(Main.plugin, "[DI-156-1] " + "Checking for bags.");
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i)));
			}
		}
		return bags;
	}
	
	public static int GetBagsInInventory(Player player) {
		int bags = 0;
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.IsBag(i)) { 
				bags++;
			}
		}
		return bags;
	}
	
	public static int GetBagsInInventoryCarryLimit(Player player) {
		int bags = 0;
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.IsBag(i)) { 
				if(PDC.Has(i, "climit") && PDC.GetBoolean(i, "climit")) {
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
    
    public static int GetBagSlotsInInventory(Player player) {
		int count = 0;

		for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
			count += BagData.GetBag(HavenBags.GetBagUUID(bag.item), null).getSize();
		}

		return count;
    }
    
    public static boolean HasOthersBag(Player player) {
    	boolean access = false;
    	String uuid = player.getUniqueId().toString();
    	for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
    		if(BagData.GetBag(HavenBags.GetBagUUID(bag.item), null).getOwner().equalsIgnoreCase(uuid)) access = true;
    		for(String trusted : BagData.GetBag(HavenBags.GetBagUUID(bag.item), null).getTrusted()) {
    			if(trusted.equalsIgnoreCase(uuid)) access = true;
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
		if(size % 9 == 0) return true;
		return false;
	}
}
