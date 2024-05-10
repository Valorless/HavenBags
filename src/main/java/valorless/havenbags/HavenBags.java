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

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.ValorlessUtils.Tags;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;
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
		Log.Debug(Main.plugin, "Returning bag to " + player.getName());
		Log.Debug(Main.plugin, "health " + player.getHealth());
    	
    	if(player.isDead()) {
    		if (Bukkit.getPluginManager().getPlugin("AngelChest") == null) {
    			Log.Debug(Main.plugin, "Player dead, dropping bag instead.");
    			player.getWorld().dropItem(player.getLocation(), bag);
    			return;
    		}else {
    			if(player.getInventory().getItemInMainHand() != null) {
        			if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
    	   	 			Log.Debug(Main.plugin, "Hand Empty.");
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
    		Log.Debug(Main.plugin, "Player alive.");
    		if(player.getInventory().getItemInMainHand() != null) {
    			if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
	   	 			Log.Debug(Main.plugin, "Hand Empty.");
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
    	String owner = NBT.GetString(bag, "bag-owner");
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
	
	public static void UpdateBagItem(ItemStack bag, List<ItemStack> inventory, Player player) {
    	String owner = NBT.GetString(bag, "bag-owner");
    	
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
        
        for(String line : Lang.lang.GetStringList("bag-lore-add")) {
        	if(owner.equalsIgnoreCase("null") == false) {
        		if(line.contains("%bound-to%") && !NBT.GetBool(bag, "bag-canBind")) continue;
        		if(line.contains("%bag-auto-pickup%") && !NBT.Has(bag, "bag-filter")) continue;
        		if(line.contains("%bag-weight%") && !Main.weight.GetBool("enabled")) continue;
        	}else {
        		if(line.contains("%bound-to%")) continue;
        		if(line.contains("%bag-auto-pickup%") && !NBT.Has(bag, "bag-filter")) continue;
        		if(line.contains("%bag-weight%")) continue;
        	}
        	lore.add(Lang.Parse(line, placeholders, player));
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
	
	public static void UpdateBagLore(ItemStack bag, Player player) {
		try {
			UpdateBagItem(bag, LoadBagContentFromServer(bag, player), player);
		} catch (Exception e) {
			UpdateBagItem(bag, null, player);
		}
	}
	
	public static void EmptyBag(ItemStack bag, Player player) {
		String uuid = NBT.GetString(bag, "bag-uuid");
    	String owner = NBT.GetString(bag, "bag-owner");
		Log.Debug(Main.plugin, "Attempting to initialize bag items");
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
			bagItem = SkullCreator.itemFromBase64(bagTexture);
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
    			String owner = NBT.GetString(bag, "bag-owner");
				//List<ItemStack> content = LoadBagContentFromServer(uuid, owner, null);
				List<ItemStack> content = BagData.GetBag(uuid, bag).getContent();
				for(ItemStack item : content) {
					weight += (Main.weight.GetFloat(item.getType().toString()) * item.getAmount());
				}
				NBT.SetDouble(bag, "bag-weight", weight);
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
		Log.Debug(Main.plugin, "Can carry?");
		HasWeightLimit(bag);
		double maxWeight = NBT.GetDouble(bag, "bag-weight-limit");
		double weight = GetWeight(bag);
		double itemWeight = ItemWeight(item);
		
		Log.Debug(Main.plugin, (weight + itemWeight) + "");
		if(weight + itemWeight <= maxWeight) {
			Log.Debug(Main.plugin, "true");
			return true;
		}else {
			Log.Debug(Main.plugin, "false");
			return false;
		}
	}
	
	public static boolean CanCarry(ItemStack item, ItemStack bag, List<ItemStack> content) {
		Log.Debug(Main.plugin, "Can carry?");
		HasWeightLimit(bag);
		double maxWeight = NBT.GetDouble(bag, "bag-weight-limit");
		double weight = GetWeight(content);
		double itemWeight = ItemWeight(item);
		
		Log.Debug(Main.plugin, (weight + itemWeight) + "");
		if(weight + itemWeight <= maxWeight) {
			Log.Debug(Main.plugin, "true");
			return true;
		}else {
			Log.Debug(Main.plugin, "false");
			return false;
		}
	}
	
	public static boolean HasWeightLimit(ItemStack bag) {
		if(NBT.Has(bag, "bag-weight-limit")) {
			return true;
		}else {
			if(Main.weight.GetBool("weight-per-size")) {
				NBT.SetDouble(bag, "bag-weight-limit", Main.weight.GetFloat("weight-size-" + NBT.GetInt(bag, "bag-size")));
			}else {
				NBT.SetDouble(bag, "bag-weight-limit", Main.weight.GetFloat("weight-limit"));
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
		Log.Debug(Main.plugin, "Put item in bag?");	
		
		items = RemoveAir(items);
		
	    // Check if there is still space in the list for new items
	    if (items.size() >= inventorySlots && allSlotsFull(items, itemToAdd)) {
			Log.Debug(Main.plugin, "bag full!");	
	        return false; // The bag is full and item is dropped
	    }

	    boolean added = false;
		Log.Debug(Main.plugin, "checking bag.");	
	    for (ItemStack stack : items) {
	    	if(stack == null) continue;
	        if (stack.isSimilar(itemToAdd)) {
	            int maxStackSize = stack.getMaxStackSize();
	            int totalAmount = stack.getAmount() + itemToAdd.getAmount();

	            if (totalAmount <= maxStackSize) {
	        		Log.Debug(Main.plugin, "stack has space.");	
	                stack.setAmount(totalAmount); // Perfect fit or less
	                return true;
	            } else {
	        		Log.Debug(Main.plugin, "stack overflow, adjusting.");	
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
        		Log.Debug(Main.plugin, "success.");	
	            return true; // New stack added successfully
	        } else {
        		Log.Debug(Main.plugin, "no space.");	
	            DropItem(player.getLocation(), itemToAdd); // No space left, drop the remaining items
	            return false;
	        }
	    }
		Log.Debug(Main.plugin, "failed.");	

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
		Log.Debug(Main.plugin, "removing air, if any.");	
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
		Log.Debug(Main.plugin, "Is item blacklisted?");	
		//Log.Debug(Main.plugin, item.toString());	
		List<Material> materials = new ArrayList<Material>();
		List<String> names = blacklist.GetStringList("blacklist.displayname");
		List<BlacklistNBT> nbt = new ArrayList<BlacklistNBT>();
		
		for(String mat : blacklist.GetStringList("blacklist.materials")) {
			//Log.Debug(Main.plugin, "Blacklisted Material: " + mat);	
			materials.add(Material.valueOf(mat));
		}
		
		for(String name : names) {
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
			Log.Debug(Main.plugin, "Material blacklisted!");	
			return true;
		}
		
		if(item.hasItemMeta()) {
			for(String name : names) {
				if(name.equalsIgnoreCase(Lang.RemoveColorFormatting(item.getItemMeta().getDisplayName()))) {
					Log.Debug(Main.plugin, "Name blacklisted!");	
					return true;
				}
			}
		}
		
		for(BlacklistNBT nk : nbt) {
			if(NBT.Has(item, nk.key)) {
				Log.Debug(Main.plugin, "NBT blacklisted!");	
				return true;
			}
		}
		
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
}
