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
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.sound.SFX;
import valorless.valorlessutils.utils.Utils;

public class HavenBags {

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
    	if(player.getInventory().getItemInMainHand() != null) {
    		if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
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
	
	public static void WriteBagToServer(ItemStack bag, List<ItemStack> inventory, Player player) {
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
	
	public static List<ItemStack> LoadBagContentFromServer(String uuid, String owner, Player player){
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
	}
	
	public static List<ItemStack> LoadBagContentFromServer(ItemStack bag, Player player){
    	String uuid = NBT.GetString(bag, "bag-uuid");
    	String owner = NBT.GetString(bag, "bag-owner");
		return LoadBagContentFromServer(uuid, owner, player);
	}
	
	public static boolean DoesBagExist(String uuid, String owner, Player player) {
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
        	player.sendMessage(Lang.Get("bag-does-not-exist"));
        	Log.Debug(Main.plugin, "This bag does not exist.");
        	return false;
        }
        return true;
	}
	
	public static void UpdateBagItem(ItemStack bag, List<ItemStack> inventory, Player player) {
    	String owner = NBT.GetString(bag, "bag-owner");
    	ItemMeta bagMeta = bag.getItemMeta();
		List<ItemStack> cont = new ArrayList<ItemStack>();
        int a = 0;
        List<String> items = new ArrayList<String>();
        for(int i = 0; i < inventory.size(); i++) {
    		cont.add(inventory.get(i));
    		if(inventory.get(i) != null) {
    			if(inventory.get(i).getItemMeta().hasDisplayName()) {
    				if(inventory.get(i).getAmount() != 1) {
    					items.add(Lang.Get("bag-content-item-amount", inventory.get(i).getItemMeta().getDisplayName(), inventory.get(i).getAmount()));
    				} else {
    					items.add(Lang.Get("bag-content-item", inventory.get(i).getItemMeta().getDisplayName()));
    				}
    			}else {
    				if(inventory.get(i).getAmount() != 1) {
    					items.add(Lang.Get("bag-content-item-amount", Main.translator.Translate(inventory.get(i).getType().getTranslationKey()), inventory.get(i).getAmount()));
    				} else {
    					items.add(Lang.Get("bag-content-item", Main.translator.Translate(inventory.get(i).getType().getTranslationKey())));
    				}
    			}
    			a++;
    		}
    	}
        List<String> lore = new ArrayList<String>();
        for (String l : Lang.lang.GetStringList("bag-lore")) {
        	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
        }
        if(NBT.GetBool(bag, "bag-canBind")) {
            for (String l : Lang.lang.GetStringList("bound-to")) {
            	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName()), player));
            }
        }
        for (String l : Lang.lang.GetStringList("bag-size")) {
        	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, inventory.size()), player));
        }
        
        if(NBT.Has(bag, "bag-filter")) {
        	lore.add(Lang.Parse(Lang.Parse(String.format(Lang.Get("bag-auto-pickup"), AutoPickup.GetFilterDisplayname(NBT.GetString(bag, "bag-filter"))), player)));
        	//lore.add(Lang.Parse("&7Auto Loot: " + AutoPickup.GetFilterDisplayname(NBT.GetString(bag, "bag-filter")), player));
        }
        
        if(a > 0 && Lang.lang.GetBool("show-bag-content")) {
        	lore.add(Lang.Get("bag-content-title"));
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
	
	public static void UpdateBagLore(ItemStack bag, Player player) {
		UpdateBagItem(bag, LoadBagContentFromServer(bag, player), player);
	}
	
	public static void EmptyBag(ItemStack bag, Player player) {
		String uuid = NBT.GetString(bag, "bag-uuid");
    	String owner = NBT.GetString(bag, "bag-owner");
		Log.Debug(Main.plugin, "Attempting to initialize bag items");
		List<ItemStack> content = LoadBagContentFromServer(uuid, owner, player);
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
		WriteBagToServer(bag, content, player);
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
}
