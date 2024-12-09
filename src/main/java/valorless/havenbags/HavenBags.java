package valorless.havenbags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

import valorless.havenbags.BagData.Bag;
import valorless.havenbags.Main.ServerVersion;
import valorless.havenbags.mods.HavenBagsPreview;
import valorless.havenbags.utils.Base64Validator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.havenbags.utils.HeadCreator;
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
		if(item.hasItemMeta()) {
			if(NBT.Has(item, "bag-uuid")) {
				return true;
			}
		}
		return false;
	}

	public static Boolean IsSkinToken(ItemStack item) {
		if(item == null) return false;
		if(item.hasItemMeta()) {
			if(NBT.Has(item, "bag-token-skin")) {
				return true;
			}
		}
		return false;
	}
	
	public static String GetBagUUID(@NotNull ItemStack item) {
		if(IsBag(item)) return NBT.GetString(item, "bag-uuid");
		else return null;
	}
	
	public enum BagState { Null, New, Used }
	public static BagState BagState(ItemStack item) {
		if(item == null) return BagState.Null;
		if(IsBag(item)) {
			if(NBT.GetString(item, "bag-owner").equalsIgnoreCase("null")) {
				return BagState.New;
			}else {
				return BagState.Used;
			}
		}
		return BagState.Null;
	}
	
	public static Boolean IsBagOpen(ItemStack item) {
	    for (ActiveBag openBag : Main.activeBags) {
	    	if(openBag.uuid.equalsIgnoreCase(NBT.GetString(item, "bag-uuid"))) {
	    		return true;
	    	}
	    }
	    return false;
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
    				SFX.Play(Main.config.GetString("inventory-full-sound"), 
    						Main.config.GetFloat("inventory-full-volume").floatValue(), 
    						Main.config.GetFloat("inventory-full-pitch").floatValue(), player);
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
				SFX.Play(Main.config.GetString("inventory-full-sound"), 
						Main.config.GetFloat("inventory-full-volume").floatValue(), 
						Main.config.GetFloat("inventory-full-pitch").floatValue(), player);
    			player.getWorld().dropItem(player.getLocation(), bag);
    		}
    	}
	}
	
	/*public static void WriteBagToServer(ItemStack bag, List<ItemStack> inventory, Player player) {
		String uuid = NBT.GetString(bag, "bag-uuid");
    	String owner = NBT.GetString(bag, "bag-owner");
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
	
	public static List<ItemStack> LoadBagContentFromServer(ItemStack bag, @Nullable Player player){
    	String uuid = NBT.GetString(bag, "bag-uuid");
    	//String owner = NBT.GetString(bag, "bag-owner");
		//return LoadBagContentFromServer(uuid, owner, player);
		return BagData.GetBag(uuid, bag).getContent();
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
	
	static void UpdateNBT(ItemStack bag) {
		String uuid = NBT.GetString(bag, "bag-uuid");
		//String display = bag.getItemMeta().getDisplayName();
		String id = uuid.replace(".json", "");
		Config data = BagData.GetBag(id, null).getData();
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
		
		//NBT.SetString(bag, "bag-uuid", uuid);

		if(bag.getType() == Material.PLAYER_HEAD) {
			String texture = data.GetString("texture");
			if(!Utils.IsStringNullOrEmpty(texture)) {
				if(!texture.contains("null")) {
					BagData.setTextureValue(bag, texture);
				}
			}
		}else {
			int cmd = data.GetInt("custommodeldata");
			if(cmd != 0) {
				if(bag.hasItemMeta()) {
					bag.getItemMeta().setCustomModelData(cmd);
				}
			}
		}
		
		
		NBT.SetString(bag, "bag-owner", data.GetString("owner"));
		
		if(data.GetString("owner").equalsIgnoreCase("ownerless")) {
			NBT.SetBool(bag, "bag-canBind", false);
		} else {
			NBT.SetBool(bag, "bag-canBind", true);
		}
		NBT.SetInt(bag, "bag-size", data.GetInt("size"));
		if(data.GetString("auto-pickup").equalsIgnoreCase("null")) {
			NBT.SetString(bag, "bag-filter", null);
		}else {
			NBT.SetString(bag, "bag-filter", data.GetString("auto-pickup"));
		}
		if(Main.weight.GetBool("enabled")){
			if(data.GetFloat("weight-max") > 0) {
				NBT.SetDouble(bag, "bag-weight-limit", data.GetFloat("weight-max"));
			}else {
				if(Main.weight.GetBool("weight-per-size")) {
					NBT.SetDouble(bag, "bag-weight-limit", Main.weight.GetFloat(String.format("weight-size-%s", data.GetInt("size"))));
					BagData.SetWeightMax(id, Main.weight.GetFloat(String.format("weight-size-%s", data.GetInt("size"))));
				}else {
					NBT.SetDouble(bag, "bag-weight-limit", Main.weight.GetFloat("weight-limit"));
					BagData.SetWeightMax(id, Main.weight.GetFloat("weight-limit"));
				}
			}
		}
		NBT.SetString(bag, "bag-creator", data.GetString("creator"));
		NBT.SetStringList(bag, "bag-trust", data.GetStringList("trusted"));
	}
	
	public static void UpdateBagItem(ItemStack bag, List<ItemStack> inventory, OfflinePlayer player, boolean...preview) {
		//Log.Error(Main.plugin, preview.toString());
		//Log.Error(Main.plugin, preview.length + "");
		if(preview.length == 0) {
			UpdateNBT(bag);
		}
		
    	String owner = NBT.GetString(bag, "bag-owner");
    	//String owner = BagData.GetOwner(HavenBags.GetBagUUID(bag));
    	
    	if(!owner.equalsIgnoreCase("null")) {
    		if(inventory == null) {
    			inventory = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
    		}
    		if(Main.plugins.GetBool("mods.HavenBagsPreview.enabled")) {
    			NBT.SetString(bag, "bag-preview-content", gson.toJson(new HavenBagsPreview(inventory)));
    		}
    	}
		
    	
    	List<Placeholder> placeholders = new ArrayList<Placeholder>();

    	ItemMeta bagMeta = bag.getItemMeta();
    	
    	
		List<ItemStack> cont = new ArrayList<ItemStack>();
        int a = 0;
        List<String> items = new ArrayList<String>();
        if(inventory != null) {
        for(int i = 0; i < inventory.size(); i++) {
    		cont.add(inventory.get(i));
    		if(inventory.get(i) != null && inventory.get(i).getType() != Material.AIR) {
    			List<Placeholder> itemph = new ArrayList<Placeholder>();
    			if(inventory.get(i).hasItemMeta()) {
    				if(inventory.get(i).getItemMeta().hasDisplayName()) {
    					itemph.add(new Placeholder("%item%", inventory.get(i).getItemMeta().getDisplayName()));
    					itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));
        			
    					if(inventory.get(i).getAmount() != 1) {
    						items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
    						//items.add(Lang.Get("bag-content-item-amount", inventory.get(i).getItemMeta().getDisplayName(), inventory.get(i).getAmount()));
    					} else {
    						items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
    						//items.add(Lang.Get("bag-content-item", inventory.get(i).getItemMeta().getDisplayName()));
    					}
    				}
    				else if(Main.VersionCompare(Main.server, ServerVersion.v1_20_5) >= 0) {
    					if(ItemUtils.HasItemName(inventory.get(i))) {
    						itemph.add(new Placeholder("%item%", ItemUtils.GetItemName(inventory.get(i))));
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
        					//items.add(Lang.Get("bag-content-item-amount", Main.translator.Translate(inventory.get(i).getType().getTranslationKey()), inventory.get(i).getAmount()));
        				} else {
        					items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
        					//items.add(Lang.Get("bag-content-item", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
        				}
        			}
    			}else {
	    			itemph.add(new Placeholder("%item%", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
	    			itemph.add(new Placeholder("%amount%", inventory.get(i).getAmount()));
	    			
    				if(inventory.get(i).getAmount() != 1) {
    					items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
    					//items.add(Lang.Get("bag-content-item-amount", Main.translator.Translate(inventory.get(i).getType().getTranslationKey()), inventory.get(i).getAmount()));
    				} else {
    					items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
    					//items.add(Lang.Get("bag-content-item", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
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
        if(NBT.GetBool(bag, "bag-canBind") == true && owner.equalsIgnoreCase("null") == false) {
            placeholders.add(new Placeholder("%owner%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()));
            placeholders.add(new Placeholder("%bound-to%", Lang.Parse(Lang.Get("bound-to"), placeholders, player)));
            //if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()), player));
        }
        if(NBT.Has(bag, "bag-size")) {
        	placeholders.add(new Placeholder("%size%", NBT.GetInt(bag, "bag-size")));
        	placeholders.add(new Placeholder("%bag-size%", Lang.Parse(Lang.Get("bag-size"), placeholders, player)));
        	//if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
        }
        
        if(NBT.Has(bag, "bag-filter")) {
        	placeholders.add(new Placeholder("%filter%", AutoPickup.GetFilterDisplayname(NBT.GetString(bag, "bag-filter"))));
        	placeholders.add(new Placeholder("%bag-auto-pickup%", Lang.Parse(Lang.Get("bag-auto-pickup"), placeholders, player)));
        	//lore.add(Lang.Parse(Lang.Parse(String.format(Lang.Get("bag-auto-pickup"), AutoPickup.GetFilterDisplayname(NBT.GetString(bag, "bag-filter"))), player)));
        	//lore.add(Lang.Parse("&7Auto Loot: " + AutoPickup.GetFilterDisplayname(NBT.GetString(bag, "bag-filter")), player));
        }
        
        if(NBT.Has(bag, "bag-weight") && NBT.Has(bag, "bag-weight-limit") && Main.weight.GetBool("enabled")) {
        	placeholders.add(new Placeholder("%bar%", TextFeatures.CreateBarWeight(GetWeight(bag), NBT.GetDouble(bag, "bag-weight-limit"), Main.weight.GetInt("bar-length"))));
        	placeholders.add(new Placeholder("%weight%", TextFeatures.LimitDecimal(String.valueOf(GetWeight(bag)),2)));
        	placeholders.add(new Placeholder("%limit%", String.valueOf(NBT.GetDouble(bag, "bag-weight-limit").intValue())));
        	placeholders.add(new Placeholder("%percent%", TextFeatures.LimitDecimal(String.valueOf(Utils.Percent(GetWeight(bag), NBT.GetDouble(bag, "bag-weight-limit"))), 2) + "%"));
        	placeholders.add(new Placeholder("%bag-weight%", Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)));
        	//lore.add(Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player));
        }
        
        boolean hasTrust = false;
        if(NBT.Has(bag, "bag-trust")) {
			List<String> trust = NBT.GetStringList(bag, "bag-trust");
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
        
        
        
        for(String line : Lang.lang.GetStringList("bag-lore-add")) {
        	if(owner.equalsIgnoreCase("null") == false) {
        		if(line.contains("%bound-to%") && !NBT.GetBool(bag, "bag-canBind")) continue;
        		if(line.contains("%bag-trusted%") && !hasTrust) continue;
        		if(line.contains("%bag-auto-pickup%") && !NBT.Has(bag, "bag-filter")) continue;
        		if(line.contains("%bag-weight%") && !Main.weight.GetBool("enabled")) continue;
        	}else {
        		if(line.contains("%bound-to%")) continue;
        		if(line.contains("%bag-trusted%") && !hasTrust) continue;
        		if(line.contains("%bag-auto-pickup%") && !NBT.Has(bag, "bag-filter")) continue;
        		if(line.contains("%bag-weight%")) continue;
        	}
        	lore.add(Lang.Parse(line, placeholders, player));
        }
        
        for(int i = 0; i < lore.size(); i++) {
        	if(lore.get(i).contains("%bag-weight%")) lore.remove(i);
        }
        
        if(a > 0 && Lang.lang.GetBool("show-bag-content")) {
        	//placeholders.add(new Placeholder("%bag-content-title%", Lang.Parse(Lang.Get("bag-content-title"), player)));
        	lore.add(Lang.Parse(Lang.Get("bag-content-title"), player));
        	//Log.Debug(Main.plugin, items.size() + "");
        	/*for(int k = 1; k < items.size(); k++) {
        		if(k < Lang.lang.GetInt("bag-content-preview-size")) {
        			content = content + "\n  " + items.get(k);
        			//lore.add("  " + items.get(i));
        		}
        	}*/
        	for(int k = 0; k < items.size(); k++) {
        		if(k < Lang.lang.GetInt("bag-content-preview-size")) {
        			lore.add("  " + items.get(k));
        		}

        	}
        	if(a > Lang.lang.GetInt("bag-content-preview-size")) {
    			//content = content + "\n" + Lang.Parse(Lang.Get("bag-content-and-more"), player);
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
			UpdateBagItem(bag, LoadBagContentFromServer(bag, player), player, preview);
		} catch (Exception e) {
			UpdateBagItem(bag, null, player, preview);
		}
	}
	
	public static void EmptyBag(ItemStack bag, Player player) {
		String uuid = NBT.GetString(bag, "bag-uuid");
    	//String owner = NBT.GetString(bag, "bag-owner");
		Log.Debug(Main.plugin, "[DI-110] " + "Attempting to initialize bag items");
		//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, player);
		List<ItemStack> content = BagData.GetBag(uuid, bag).getContent();
		SFX.Play(Main.config.GetString("close-sound"), 
				Main.config.GetFloat("close-volume").floatValue(), 
				Main.config.GetFloat("close-pitch").floatValue(), player);
		for(int i = 0; i < content.size(); i++) {
			try {
				player.getWorld().dropItem(player.getLocation(), content.get(i));
				content.set(i, null);
			} catch (Exception e) {
				continue;
			}
		}
		//WriteBagToServer(bag, content, player);
		BagData.UpdateBag(uuid, content);
		UpdateBagItem(bag, content, player);
	}
	
	public static boolean IsOwner(ItemStack bag, Player player) {
    	String owner = NBT.GetString(bag, "bag-owner");
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
		return false;
	}
	
	public static ItemStack GetDisplayBagItem() {
		ItemStack bagItem;
		String bagTexture = Main.config.GetString("bag-texture");
		if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
			bagItem = HeadCreator.itemFromBase64(bagTexture);
		} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
		} else {
			Log.Error(Main.plugin, (Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM."));
			return null;
		}
		return bagItem;
	}
	
	public static Double GetWeight(ItemStack bag) {
		if(NBT.Has(bag, "bag-weight")) {
			return NBT.GetDouble(bag, "bag-weight");
		}else {
			try {
				Double weight = 0.0;
				String uuid = NBT.GetString(bag, "bag-uuid");
    			//String owner = NBT.GetString(bag, "bag-owner");
				//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, null);
				List<ItemStack> content = BagData.GetBag(uuid, bag).getContent();
				for(ItemStack item : content) {
					weight += (Main.weight.GetFloat(item.getType().toString()) * item.getAmount());
				}
				NBT.SetDouble(bag, "bag-weight", weight);
				BagData.GetBag(uuid, bag).getData().Set("weight", weight);
				return weight;
			} catch(Exception e) {
				return (double) 0;
			}
		}
	}
	
	public static Double GetWeight(List<ItemStack> content) {
		Double weight = 0.0;
		for(ItemStack item : content) {
			try {
				weight += (Main.weight.GetFloat(item.getType().toString()) * item.getAmount());
			} catch(Exception e) {
				continue;
			}
		}
		return weight;
	}
	
	public static Double ItemWeight(ItemStack item) {
		return (Main.weight.GetFloat(item.getType().toString()) * item.getAmount());
	}
	
	public static boolean CanCarry(ItemStack item, ItemStack bag) {
		Log.Debug(Main.plugin, "[DI-111] " + "Can carry?");
		HasWeightLimit(bag);
		double maxWeight = NBT.GetDouble(bag, "bag-weight-limit");
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
		HasWeightLimit(bag);
		double maxWeight = NBT.GetDouble(bag, "bag-weight-limit");
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
		if(NBT.Has(bag, "bag-weight-limit")) {
			return true;
		}else {
			if(Main.weight.GetBool("weight-per-size")) {
				NBT.SetDouble(bag, "bag-weight-limit", Main.weight.GetFloat("weight-size-" + NBT.GetInt(bag, "bag-size")));
				BagData.GetBag(HavenBags.GetBagUUID(bag), bag).getData().Set("weight", Main.weight.GetFloat("weight-size-" + NBT.GetInt(bag, "bag-size")));
			}else {
				NBT.SetDouble(bag, "bag-weight-limit", Main.weight.GetFloat("weight-limit"));
				BagData.GetBag(HavenBags.GetBagUUID(bag), bag).getData().Set("weight", Main.weight.GetFloat("weight-limit"));
			}
		}
		return false;
	}
	
	/***
	 * Not in use
	 * @param content
	 * @return
	 */
	public static ItemStack[] ShowWeight(ItemStack[] content) {
		for(ItemStack item : content) {
			if(item == null) continue;
			if(IsBag(item)) continue;
			if(item.getItemMeta() == null) continue;
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			if(meta.getLore() == null) lore = new ArrayList<String>();
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
        	placeholders.add(new Placeholder("%weight%", ItemWeight(item).toString()));
        	lore.add(Lang.Parse(Main.weight.GetString("item-weight"), placeholders));
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		return content;
	}
	
	/***
	 * Not in use
	 * @param content
	 * @return
	 */
	public static ItemStack[] HideWeight(ItemStack[] content) {
		String target = Lang.Parse(Main.weight.GetString("item-weight").replace("%weight%", ""), null);
		for(ItemStack item : content) {
			if(item == null) continue;
			if(IsBag(item)) continue;
			if(item.getItemMeta() == null) continue;
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			if(lore == null) continue;
			try {
				lore.removeIf(i -> i == target);
				if(lore.size() > 1) {
					lore.remove(lore.size()-1);
				}else {
					lore.clear();
				}
				meta.setLore(lore);
				if(lore.size() == 0) meta.setLore(null);
				item.setItemMeta(meta);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return content;
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
	
	public static boolean IsItemBlacklisted(ItemStack item) {
		if(item == null) return false;
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
			if(NBT.Has(item, nk.key)) {
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
	
	public static boolean IsPlayerTrusted(ItemStack item, String player) {
		if(!NBT.Has(item, "bag-trust")) return false;
		List<String> list = NBT.GetStringList(item, "bag-trust");
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).equalsIgnoreCase(player)) {
				return true;
			}
		}
		return false;
	}
	
	public static ItemStack CreateToken(String value, String...skin) {
		String name = Main.config.GetString("skin-token.display-name");
		Material material = Main.config.GetMaterial("skin-token.material");
		int cmd = Main.config.GetInt("skin-token.custommodeldata");
		List<String> lore = Main.config.GetStringList("skin-token.lore");
		List<Placeholder> ph = new ArrayList<Placeholder>();
		if(skin.length != 0) {
			ph.add(new Placeholder("%skin%", skin[0]));
		}else if(!Base64Validator.isValidBase64(value)) {
			ph.add(new Placeholder("%skin%", value));
		}else {
			ph.add(new Placeholder("%skin%", ""));
		}
		
		ItemStack item = new ItemStack(material);
		NBT.SetString(item, "bag-token-skin", value); // Set this first to give the item ItemMeta
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Lang.Parse(name, ph));
		if(cmd > 0) {
			meta.setCustomModelData(cmd);
		}else {
			try {
				meta.setCustomModelData(Integer.valueOf(value));
			}catch(Exception e) {}
		}
		List<String> l = new ArrayList<String>();
		for (String line : lore) {
			if(!Utils.IsStringNullOrEmpty(line)) {
				l.add(Lang.Parse(line, ph));
			}
		}
		meta.setLore(l);
		item.setItemMeta(meta);
		
		if(material == Material.PLAYER_HEAD && Base64Validator.isValidBase64(value)) {
			BagData.setTextureValue(item, value);
		}
		
		return item;
	}
	
	public static boolean IsBagFull(ItemStack bag) {
		try {
			int size = NBT.GetInt(bag, "bag-size");
			List<ItemStack> content = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
			content.removeIf(item -> item.getType() == Material.AIR);
			content.removeIf(item -> item == null);
			if(content.size() >= size) return true;
			else return false;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static int SlotsEmpty(ItemStack bag) {
		int size = NBT.GetInt(bag, "bag-size");
		List<ItemStack> content = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
		content.removeIf(item -> item == null);
		content.removeIf(item -> item.getType() == Material.AIR);
		return size - content.size();
	}
	
	public static double UsedCapacity(ItemStack bag, List<ItemStack> content) {
		//Log.Error(Main.plugin, "content " + NBT.GetInt(bag, "bag-size"));
		Double size = Double.valueOf(NBT.GetInt(bag, "bag-size") + ".0");
		//Log.Error(Main.plugin, "size " + size);
		List<ItemStack> used = BagData.GetBag(HavenBags.GetBagUUID(bag), null).getContent();
		used.removeIf(item -> item == null);
		used.removeIf(item -> item.getType() == Material.AIR);
		//Log.Error(Main.plugin, "empty " + (size - used.size()));
		//Double empty = size - Double.valueOf(used.size() + ".0");
		//Log.Error(Main.plugin, "used " + used.size());
		return (used.size()/size)*100;
	}
	
	public static List<Bag> GetBagsInInventory(Player player) {
		List<Bag> bags = new ArrayList<Bag>();
		Log.Debug(Main.plugin, "[DI-156] " + "Checking for bags.");
		for(ItemStack i : player.getInventory().getContents()) {
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i, null)));
			}
		}
		return bags;
	}
}
