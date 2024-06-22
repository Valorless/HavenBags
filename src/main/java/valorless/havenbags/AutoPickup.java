package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatMessageType;
import valorless.havenbags.Main.ServerVersion;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.ValorlessUtils.Tags;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.sound.SFX;
import valorless.valorlessutils.utils.Utils;

@SuppressWarnings("deprecation")
public class AutoPickup implements Listener {
	
	private static boolean enabled = true;
	
	public static class Filter {
		public String name;
		public String displayname;
		public List<String> entries = new ArrayList<String>();
		
		public Filter(String name, String displayname, List<String> entries) {
			this.name = name;
			this.displayname = displayname;
			this.entries = entries;
		}
	}
	
	private static class Bag {
		public ItemStack item;
		public List<ItemStack> content = new ArrayList<ItemStack>();
		
		public Bag (ItemStack item, List<ItemStack> content) {
			this.item = item;
			this.content = content;
		}
	}

	public static Config filter;
	
	private static List<Filter> filters = new ArrayList<Filter>();
	
	private static List<String> oraxenIDs = new ArrayList<String>();
	
	public static void Initiate() {
		enabled = Main.config.GetBool("auto-pickup");
		if(!enabled) return;
		filters.clear();
		Object[] f = filter.GetConfigurationSection("filters").getKeys(false).toArray();
		Log.Debug(Main.plugin, "Filters: " + f.length);
		for(int i = 0; i < f.length; i++) {
			String filterName = String.valueOf(f[i]);
			filters.add(new Filter(filterName, filter.GetString(String.format("filters.%s.displayname", filterName)), filter.GetStringList(String.format("filters.%s.items", filterName))));

			Log.Debug(Main.plugin, "Filter: " + filterName);
		}
		
		if(Main.plugins.GetBool("plugins.Oraxen.enabled")) {
			if(Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
        		try {
        			Log.Debug(Main.plugin, "Adding Oraxen items to auto-pickup.");
        			oraxenIDs = Main.plugins.GetStringList("plugins.Oraxen.items");
        		}catch (Exception e) {
        			Log.Error(Main.plugin, "Failed to get Oraxen API. Is it up to date?");
        		}
        	}
		}
	}
	
	public static List<Filter> GetFilters(){
		return filters;
	}
	
	public static List<String> GetFilterNames(){
		List<String> filternames = new ArrayList<String>();
		for(Filter filter : filters) {
			filternames.add(filter.name);
		}
		return filternames;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(Main.config.GetBool("auto-pickup-inventory.enabled")) {
			if(Main.config.GetBool("auto-pickup-inventory.events.onBlockBreak")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				    public void run() {
				    	FromInventory(event.getPlayer());
				    }
				}, 5L);
			}
		}
	}
	
	@EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
		if(!enabled) return;
		if(event.getEntityType() != EntityType.PLAYER) return;
		Player player = (Player)event.getEntity();
		if(event.getItem().getOwner() != null) {
			if(event.getItem().getOwner() != player.getUniqueId()) return;
		}
		Log.Debug(Main.plugin, "AutoPickup");
		List<String> blacklist = Main.config.GetStringList("blacklist");
		if(blacklist != null) {
			if(blacklist.size() != 0) {
				Log.Debug(Main.plugin, "Player World: " + player.getWorld().getName());
				for(String world : blacklist) {
					Log.Debug(Main.plugin, "Blacklist: " + world);
					if(player.getWorld().getName().equalsIgnoreCase(world)) return;
				}
			}
		}
		if(HavenBags.InventoryContainsBag(player) == false) return;
		ItemStack item = event.getItem().getItemStack();
		/*
		if(HavenBags.IsBag(item)) {
			if(!HavenBags.CanCarryMoreBags(player)) {
				event.setCancelled(true);
				return;
			}
		}
		*/
		
		// For some reason handling ExecutableItems items, would duplicate the item.
		if (Bukkit.getPluginManager().getPlugin("ExecutableItems") != null) {
			if(Tags.Has((JavaPlugin)Bukkit.getPluginManager().getPlugin("ExecutableItems"), item.getItemMeta().getPersistentDataContainer(), "ei-id", PersistentDataType.STRING)) {
				Log.Debug(Main.plugin, "Item is ExecutableItem, skipping.");
				return;
			}
		}
		
		if(HavenBags.IsBag(item)) return;
		Log.Debug(Main.plugin, item.getType().toString());
		boolean cancel = PutItemInBag(item, player);
		Log.Debug(Main.plugin, "Cancelled: " + cancel);
		if(cancel) {
			event.setCancelled(true);
			int count = 10;
			double force = 0.1;
			try {
				player.spawnParticle(Particle.BLOCK_DUST, event.getItem().getLocation(), count, 0, 0.1, 0, force, item.getType().createBlockData());
			} catch (Exception e) {
				player.spawnParticle(Particle.ITEM_CRACK, event.getItem().getLocation(), count, 0, 0.1, 0, force, item);
			}
			player.spawnParticle(Particle.SMOKE_NORMAL, event.getItem().getLocation(), 5, 0, 0.1, 0, 0.02);
			event.getItem().remove();
		}

		if(Main.config.GetBool("auto-pickup-inventory.enabled")) {
			if(Main.config.GetBool("auto-pickup-inventory.events.onItemPickup")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				    public void run() {
				    	FromInventory(player);
				    }
				}, 5L);
			}
		}
    }
	
	/*
	boolean PutItemInBag(ItemStack item, Player player){
		Log.Debug(Main.plugin, "PutItemInBag?");
		
		if(ItemFilter(item) == null) {
			Log.Debug(Main.plugin, "false");
			return false;
		}
	
		List<Bag> bags = new ArrayList<Bag>();
		Log.Debug(Main.plugin, "Checking for bags.");
		for(ItemStack i : player.getInventory().getContents()) {
			//Log.Debug(Main.plugin, HavenBags.BagState(i).toString());
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				if(NBT.Has(i, "bag-filter")) {
					bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i, null)));
				}
			}
		}
		Log.Debug(Main.plugin, "bags:" + bags.size());
		Log.Debug(Main.plugin, "Checking bag filters.");
		for(Bag bag : bags) {
			Log.Debug(Main.plugin, "bag: " + NBT.GetString(bag.item, "bag-uuid"));
			boolean c = false;
			for(Filter f : filters) {
				//Log.Debug(Main.plugin, "Filter: " + f.name);
				//Log.Debug(Main.plugin, "Bag Filter: " + NBT.GetString(bag.item, "bag-filter"));
				if(f.name.equalsIgnoreCase(NBT.GetString(bag.item, "bag-filter"))) {
					c = true;
					break;
				}
			}
			Log.Debug(Main.plugin, "Filter " + c);
			if(!c) {
				Log.Debug(Main.plugin, "No filters, skipping.");
				continue;
			}
			
			if(!IsItemInFilter(NBT.GetString(bag.item, "bag-filter"), item)) {
				Log.Debug(Main.plugin, "Item " + item.getType().toString() + " is not in the filter. Skipping.");
				continue;
			}
			

	    	List<Placeholder> placeholders = new ArrayList<Placeholder>();
	        if(NBT.Has(bag.item, "bag-weight") && NBT.Has(bag.item, "bag-weight-limit") && Main.weight.GetBool("enabled")) {
	        	placeholders.add(new Placeholder("%bar%", TextFeatures.CreateBarWeight(HavenBags.GetWeight(bag.item), NBT.GetDouble(bag.item, "bag-weight-limit"), Main.weight.GetInt("bar-length"))));
	        	placeholders.add(new Placeholder("%weight%", TextFeatures.LimitDecimal(String.valueOf(HavenBags.GetWeight(bag.item)),2)));
	        	placeholders.add(new Placeholder("%limit%", String.valueOf(NBT.GetDouble(bag.item, "bag-weight-limit").intValue())));
	        	placeholders.add(new Placeholder("%percent%", TextFeatures.LimitDecimal(String.valueOf(Utils.Percent(HavenBags.GetWeight(bag.item), NBT.GetDouble(bag.item, "bag-weight-limit"))), 2) + "%"));
	        	placeholders.add(new Placeholder("%bag-weight%", Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)));
	        }
			
			
			int maxContent = NBT.GetInt(bag.item, "bag-size");
			Log.Debug(Main.plugin, "cont:" + bag.content.size());
			int contSize = 0;
//			for(int i = 0; i < bag.content.size(); i++) {
//				if(bag.content.get(i) == null) {
//					bag.content.remove(i);
//					continue;
//				}
//				if(bag.content.get(i).getType() != Material.AIR) {
//					contSize++;
//				}
//			}
			Log.Debug(Main.plugin, "Checking bag content.");
			for(ItemStack i : bag.content) {
				try {
					if(i.getType() != Material.AIR) {
						contSize++;
					}
				} catch (Exception e) {
					continue;
				}
			}
			
			if(HavenBags.CanCarry(item, bag.item) == false) return false;
			
			Log.Debug(Main.plugin, "contsize:" + contSize);
			Log.Debug(Main.plugin, "size:" + maxContent);
			if(Contains(bag.content, item)) {
				Log.Debug(Main.plugin, "Contains");
				for(ItemStack i : bag.content) {
					if(i == null) continue;
					if(i.getAmount() == i.getMaxStackSize()) continue;
					if(StackHasSpace(i, item)) {
						Log.Debug(Main.plugin, "stack Has Space");
						if(item.getType().equals(i.getType())){
							if(i.getAmount() != i.getMaxStackSize()) {
								i.setAmount(i.getAmount() + item.getAmount());
								Log.Debug(Main.plugin, bag.content.toString());
								if(Main.weight.GetBool("enabled")) {
						        	NBT.SetDouble(bag.item, "bag-weight", HavenBags.GetWeight(bag.content));
									if(Main.weight.GetBool("weight-text-pickup")) {
										Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
												Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
											);
										weightMessage.Send(player);
									}
						        }
								HavenBags.UpdateBagItem(bag.item, bag.content, player);
								HavenBags.WriteBagToServer(bag.item, bag.content, player);
								PickupSound(player);
								return true; // Only do it to the first stack found.
							}
							else {
								continue;
							}
						}
					}else {
						Log.Debug(Main.plugin, "stack overflow, adding new.");
						for(int is = 0; is < bag.content.size(); is++) {
							if(bag.content.get(is) == null) {
								bag.content.set(is, item);
								Log.Debug(Main.plugin, bag.content.toString());
								if(Main.weight.GetBool("enabled")) {
						        	NBT.SetDouble(bag.item, "bag-weight", HavenBags.GetWeight(bag.content));
									if(Main.weight.GetBool("weight-text-pickup")) {
										Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
												Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
											);
										weightMessage.Send(player);
									}
						        }
								HavenBags.UpdateBagItem(bag.item, bag.content, player);
								HavenBags.WriteBagToServer(bag.item, bag.content, player);
								PickupSound(player);
								return true; // Only do it to the first stack found.
								//break;
							}
						}
						
					}
				}
			}
			if(contSize < maxContent) {
				Log.Debug(Main.plugin, "Has Space");
				for(int i = 0; i < bag.content.size(); i++) {
					if(bag.content.get(i) == null) {
						bag.content.set(i, item);
						break;
					}
				}
				
				//bag.content.add(item);
				Log.Debug(Main.plugin, bag.content.toString());
				HavenBags.UpdateBagItem(bag.item, bag.content, player);
				HavenBags.WriteBagToServer(bag.item, bag.content, player);
				PickupSound(player);
				if(Main.weight.GetBool("enabled")) {
					if(Main.weight.GetBool("weight-text-pickup")) {
						Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
								Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
								);
						weightMessage.Send(player);
					}
				}
				return true;
			}
		}
		Log.Debug(Main.plugin, "Item was not put in bag.");
		return false;
	}
	*/
	
	boolean PutItemInBag(ItemStack item, Player player){
		Log.Debug(Main.plugin, "PutItemInBag?");
		
		if(ItemFilter(item) == null) {
			Log.Debug(Main.plugin, "false");
			return false;
		}

        if(item.getType() == Material.AIR) return false;
		if(HavenBags.IsItemBlacklisted(item)) return false;
	
		List<Bag> bags = new ArrayList<Bag>();
		Log.Debug(Main.plugin, "Checking for bags.");
		for(ItemStack i : player.getInventory().getContents()) {
			//Log.Debug(Main.plugin, HavenBags.BagState(i).toString());
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				if(NBT.Has(i, "bag-filter")) {
					bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i, null)));
				}
			}
		}
		Log.Debug(Main.plugin, "bags:" + bags.size());
		Log.Debug(Main.plugin, "Checking bag filters.");
		for(Bag bag : bags) {
			Log.Debug(Main.plugin, "bag: " + NBT.GetString(bag.item, "bag-uuid"));
			if(BagData.IsBagOpen(NBT.GetString(bag.item, "bag-uuid"), bag.item)) continue;
			if(HavenBags.IsBagFull(bag.item)) continue;
			boolean c = false;
			for(Filter f : filters) {
				//Log.Debug(Main.plugin, "Filter: " + f.name);
				//Log.Debug(Main.plugin, "Bag Filter: " + NBT.GetString(bag.item, "bag-filter"));
				if(f.name.equalsIgnoreCase(NBT.GetString(bag.item, "bag-filter"))) {
					c = true;
					break;
				}
			}
			Log.Debug(Main.plugin, "Filter " + c);
			if(!c) {
				Log.Debug(Main.plugin, "No filters, skipping.");
				continue;
			}
			
			if(!IsItemInFilter(NBT.GetString(bag.item, "bag-filter"), item)) {
				Log.Debug(Main.plugin, "Item " + item.getType().toString() + " is not in the filter. Skipping.");
				continue;
			}
			

	    	List<Placeholder> placeholders = new ArrayList<Placeholder>();
	        if(NBT.Has(bag.item, "bag-weight") && NBT.Has(bag.item, "bag-weight-limit") && Main.weight.GetBool("enabled")) {
	        	placeholders.add(new Placeholder("%bar%", TextFeatures.CreateBarWeight(HavenBags.GetWeight(bag.item), NBT.GetDouble(bag.item, "bag-weight-limit"), Main.weight.GetInt("bar-length"))));
	        	placeholders.add(new Placeholder("%weight%", TextFeatures.LimitDecimal(String.valueOf(HavenBags.GetWeight(bag.item)),2)));
	        	placeholders.add(new Placeholder("%limit%", String.valueOf(NBT.GetDouble(bag.item, "bag-weight-limit").intValue())));
	        	placeholders.add(new Placeholder("%percent%", TextFeatures.LimitDecimal(String.valueOf(Utils.Percent(HavenBags.GetWeight(bag.item), NBT.GetDouble(bag.item, "bag-weight-limit"))), 2) + "%"));
	        	placeholders.add(new Placeholder("%bag-weight%", Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)));
	        }
			
			
			int maxContent = NBT.GetInt(bag.item, "bag-size");
			Log.Debug(Main.plugin, "cont:" + bag.content.size());
			int contSize = 0;
//			for(int i = 0; i < bag.content.size(); i++) {
//				if(bag.content.get(i) == null) {
//					bag.content.remove(i);
//					continue;
//				}
//				if(bag.content.get(i).getType() != Material.AIR) {
//					contSize++;
//				}
//			}
			Log.Debug(Main.plugin, "Checking bag content.");
			for(ItemStack i : bag.content) {
				try {
					if(i.getType() != Material.AIR) {
						contSize++;
					}
				} catch (Exception e) {
					continue;
				}
			}
			
			
			
			if(HavenBags.CanCarry(item, bag.item) == false) return false;
			
			Log.Debug(Main.plugin, "maxContent:" + maxContent);
			//if(contSize >= maxContent) return false;
			Log.Debug(Main.plugin, "contSize:" + contSize);
			if(contSize == 0) {
				bag.content.set(0, item);
				if(Main.weight.GetBool("enabled")) {
		        	NBT.SetDouble(bag.item, "bag-weight", HavenBags.GetWeight(bag.content));
					if(Main.weight.GetBool("weight-text-pickup")) {
						Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
								Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
							);
						weightMessage.Send(player);
					}
		        }
				HavenBags.UpdateBagItem(bag.item, bag.content, player);
				//HavenBags.WriteBagToServer(bag.item, bag.content, player);
				BagData.UpdateBag(bag.item, bag.content);
				PickupSound(player);
				return true;
			}
			
			// Can't deal with empty bags.
			if(HavenBags.AddItemToInventory(bag.content, NBT.GetInt(bag.item, "bag-size"), item, player)) {
				if(Main.weight.GetBool("enabled")) {
		        	NBT.SetDouble(bag.item, "bag-weight", HavenBags.GetWeight(bag.content));
					if(Main.weight.GetBool("weight-text-pickup")) {
						Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
								Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
							);
						weightMessage.Send(player);
					}
		        }
				HavenBags.UpdateBagItem(bag.item, bag.content, player);
				//HavenBags.WriteBagToServer(bag.item, bag.content, player);
				BagData.UpdateBag(bag.item, bag.content);
				PickupSound(player);
				Log.Debug(Main.plugin, "Item put in bag.");
				return true;
			}else {
				Log.Debug(Main.plugin, "Item was not put in bag.");
				return false;
			}
		}
		Log.Debug(Main.plugin, "Item was not put in bag.");
		return false;
	}
	
	String ItemFilter(ItemStack item) {
		for(Filter f : filters) {
			if(f.entries.contains(item.getType().toString())){
				return f.name;
			}
		}
		return null;
	}
	
	boolean IsItemInFilter(String filter, ItemStack item) {
		Log.Debug(Main.plugin, "IsItemInFilter?");
		if(Main.plugins.GetBool("plugins.Oraxen.enabled")) {
			if(Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
				if(item.hasItemMeta()) {
					JavaPlugin oraxen = (JavaPlugin)Bukkit.getPluginManager().getPlugin("Oraxen");
					if(Tags.Has(oraxen, item.getItemMeta().getPersistentDataContainer(), "id", PersistentDataType.STRING)) {
						if(oraxenIDs.contains(Tags.Get(oraxen, item.getItemMeta().getPersistentDataContainer(), "id", PersistentDataType.STRING))) {
							//return true;
							// Not ready yet.
						}
					}
				}
        	}
		}
		for(Filter f : filters) {
			if(f.name.equalsIgnoreCase(filter)) {
				if(f.entries.contains(item.getType().toString())){
					Log.Debug(Main.plugin, "IsItemInFilter true");
					return true;
				}
			}
		}

		Log.Debug(Main.plugin, "IsItemInFilter false");
		return false;
	}
	
	boolean StackHasSpace(ItemStack stack, ItemStack pickup) {
		Log.Debug(Main.plugin, "StackHasSpace?");
		int comb = stack.getAmount() + pickup.getAmount();
		Log.Debug(Main.plugin, "comb: " + comb);
		if((stack.getAmount() + pickup.getAmount()) <= stack.getMaxStackSize()) {
			Log.Debug(Main.plugin, "StackHasSpace true");
			return true;
		}else {
			Log.Debug(Main.plugin, "StackHasSpace false");
			return false;
		}
	}
	
	boolean Contains(List<ItemStack> content, ItemStack item) {
		for(ItemStack i : content) {
			if (i == null) continue;
			if (i.getType() == item.getType()) return true;
		}
		return false;
	}
	
	void PickupSound(Player player) {
		Double pitch = 1.0;
		if(Main.server != ServerVersion.v1_17 && Main.server != ServerVersion.v1_17_1) {
			pitch = Utils.RandomRange(Main.config.GetFloat("auto-pickup-pitch-min"), Main.config.GetFloat("auto-pickup-pitch-max"));
		}
		
		SFX.Play(Main.config.GetString("auto-pickup-sound"), 
				Main.config.GetFloat("auto-pickup-volume").floatValue(), 
				pitch.floatValue(), player);
		
	}
	
	public static String GetFilterDisplayname(String filter) {
		for(Filter f : filters) {
			if(f.name.equalsIgnoreCase(filter)) {
				return f.displayname;
			}
		}
		return null;
	}
	
	void FromInventory(Player player) {
		Log.Debug(Main.plugin, "Checking for items in inventory, to put into bag.");
		PlayerInventory inv = player.getInventory();
		for(int i = 0; i < inv.getContents().length; i++) {
			ItemStack item = inv.getItem(i);
			if(item == null) continue;
			if(item.getType() == Material.AIR) continue;
			
			// Ignore equipped items
			if(i == 36|| //Boots
				i == 37 || //Leggings
				i == 38 || //Chestplate
				i == 39 || //Helmet
				i == 40) { //Off-hand
				if(item.getType().toString().contains("_sword") ||
						item.getType().toString().contains("_pickaxe") ||
						item.getType().toString().contains("_axe") ||
						item.getType().toString().contains("_shovel") ||
						item.getType().toString().contains("_hoe") ||
						item.getType() == Material.FISHING_ROD ||
						item.getType() == Material.BOW ||
						item.getType() == Material.CROSSBOW ||
						item.getType() == Material.SHEARS) {
					if(i == 0 || // Hotbar slots
							i == 1 ||
							i == 2 ||
							i == 3 ||
							i == 4 ||
							i == 5 ||
							i == 6 ||
							i == 7 ||
							i == 8) {
						continue;
					}
						
				}
				continue;
			}
			
			if(PutItemInBag(item, player)) {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
		}
	}
}
