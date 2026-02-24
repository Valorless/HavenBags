package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import dev.lone.itemsadder.api.CustomStack;
import net.md_5.bungee.api.ChatMessageType;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.BagData.Bag;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Message;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.datamodels.PluginTags;
import valorless.havenbags.datamodels.Sound;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.TextFeatures;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.tags.TagType;
import valorless.valorlessutils.tags.Tags;
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

	public static Config filter;
	
	private static List<Filter> filters = new ArrayList<Filter>();
		
	public static void init() {
		Log.Debug(Main.plugin, "[DI-16] Registering AutoPickup");
		Bukkit.getServer().getPluginManager().registerEvents(new AutoPickup(), Main.plugin);
		

		if(!Main.config.GetBool("auto-pickup.enabled")) return;
		Integer ticks = 10;
		new BukkitRunnable() {
		    @Override
		    public void run() {
		        for (Player player : Bukkit.getOnlinePlayers()) {
		        	if (player.getInventory().firstEmpty() != -1) continue; // Inventory isn't full
		        	for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
		                if (!(entity instanceof Item)) continue;
		                Item dropped = (Item) entity;
		                if (dropped.isDead() || !dropped.isValid()) continue;
		                if (dropped.getOwner() != null && dropped.getOwner() != player.getUniqueId()) continue;
		                if (dropped.getPickupDelay() > 0) continue;
		                
		        		if(HavenBags.InventoryContainsBag(player) == false) continue;
		        		ItemStack item = dropped.getItemStack();
		        		
		        		Log.Debug(Main.plugin, "[DI-153] " + item.getType().toString());
		        		boolean cancel = PutItemInBag(item, player);
		        		Log.Debug(Main.plugin, "[DI-154] " + "Cancelled: " + cancel);
		        		if(cancel) {
		        			PickupParticles(player, dropped.getLocation(), item);
		        			dropped.remove();
		        		}
		            }	
		        }
		    }
		}.runTaskTimer(Main.plugin, ticks, ticks);
	}
	
	public static void Initiate() {
		if(!Main.config.GetBool("auto-pickup.enabled")) return;
		filters.clear();
		Object[] f = filter.GetConfigurationSection("filters").getKeys(false).toArray();
		Log.Debug(Main.plugin, "[DI-145] " + "Filters: " + f.length);
		for(int i = 0; i < f.length; i++) {
			String filterName = String.valueOf(f[i]);
			filters.add(new Filter(filterName, filter.GetString(String.format("filters.%s.displayname", filterName)), filter.GetStringList(String.format("filters.%s.items", filterName))));

			Log.Debug(Main.plugin, "[DI-146] " + "Filter: " + filterName);
		}
		
		try {
			if(filter.GetBool("allow-specific")) {
				long startTime = System.currentTimeMillis();
				Log.Info(Main.plugin, "Creating filters..");
				int i = 0;
				List<Material> validMaterials = Arrays.stream(Material.values())
						.filter(Material::isItem)
						.collect(Collectors.toList());
				for(Material mat : validMaterials) {
					List<String> thisMat = new ArrayList<>();
					thisMat.add(mat.toString());
					try {
						filters.add(new Filter(Main.translator.Translate(
								mat.getTranslationKey()).toLowerCase().replace(" ", "_"), 
								Main.translator.Translate(mat.getTranslationKey()), thisMat));
						i++;
					}catch(Exception e) {
						Log.Error(Main.plugin, String.format("Failed to translate '%s'.", mat.getTranslationKey()));
						//e.printStackTrace();
					}
				}
				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime;
				Log.Info(Main.plugin, String.format("Created %s filters. %sms", i, duration));
			}
		}catch(Exception e) {
			Log.Error(Main.plugin, "Something went wrong creating filters for specific items:");
			e.printStackTrace();
		}
	}
	
	public static List<Filter> GetFilters(){
		return filters;
	}
	
	public static List<String> GetFilterNames(Player player){
		List<String> filternames = new ArrayList<String>();
		for(Filter filter : filters) {
			if(AutoPickup.filter.HasKey("filters." + filter.name + ".permission.node") && player != null) {
				if(!player.hasPermission("havenbags.bypass")) {
					if(!AutoPickup.filter.GetString("filters." + filter.name + ".permission.node").equalsIgnoreCase("none")) {
						if(AutoPickup.filter.GetBool("filters." + filter.name + ".permission.apply")) {
							if(!player.hasPermission(AutoPickup.filter.GetString("filters." + filter.name + ".permission.node"))) continue;
						}else {
							continue;
						}
					}
				}
			}
			filternames.add(filter.name);
		}
		return filternames;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(Main.config.GetBool("auto-pickup.inventory.enabled")) {
			if(Main.config.GetBool("auto-pickup.inventory.events.onBlockBreak")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				    public void run() {
				    	FromInventory(event.getPlayer());
				    }
				}, 5L);
			}
		}
	}
	
	@EventHandler
	public void onArrowPickup(PlayerPickupArrowEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();
		if(event.getItem().getOwner() != null) {
			if(event.getItem().getOwner() != player.getUniqueId()) return;
		}
		Log.Debug(Main.plugin, "[DI-149] " + "AutoPickupArrow");
		List<String> blacklist = Main.config.GetStringList("blacklist");
		if(blacklist != null) {
			if(blacklist.size() != 0) {
				Log.Debug(Main.plugin, "[DI-150] " + "Player World: " + player.getWorld().getName());
				for(String world : blacklist) {
					Log.Debug(Main.plugin, "[DI-151] " + "Blacklist: " + world);
					if(player.getWorld().getName().equalsIgnoreCase(world)) return;
				}
			}
		}
		if(HavenBags.InventoryContainsBag(player) == false) return;
		if(HavenBags.IsBag(item)) return;
		Log.Debug(Main.plugin, "[DI-153] " + item.getType().toString());
		boolean cancel = PutItemInBag(item, player);
		Log.Debug(Main.plugin, "[DI-154] " + "Cancelled: " + cancel);
		if(cancel) {
			event.setCancelled(true);
			int count = 10;
			double force = 0.1;
			try {
				player.spawnParticle(Particle.BLOCK, event.getItem().getLocation(), count, 0, 0.1, 0, force, item.getType().createBlockData());
			} catch (Exception e) {
				player.spawnParticle(Particle.ITEM, event.getItem().getLocation(), count, 0, 0.1, 0, force, item);
			}
			player.spawnParticle(Particle.SMOKE, event.getItem().getLocation(), 5, 0, 0.1, 0, 0.02);
			event.getArrow().remove();
			event.getItem().remove();
		}

		if(Main.config.GetBool("auto-pickup.inventory.enabled")) {
			if(Main.config.GetBool("auto-pickup.inventory.events.onItemPickup")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				    public void run() {
				    	FromInventory(player);
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
		
		if(HavenBags.InventoryContainsBag(player) == false) return;
		ItemStack item = event.getItem().getItemStack();
		
		Log.Debug(Main.plugin, "[DI-153] " + item.getType().toString());
		boolean cancel = PutItemInBag(item, player);
		Log.Debug(Main.plugin, "[DI-154] " + "Cancelled: " + cancel);
		if(cancel) {
			event.setCancelled(true);
			PickupParticles(player, event.getItem().getLocation(), item);
			event.getItem().remove();
		}

		if(Main.config.GetBool("auto-pickup.inventory.enabled")) {
			if(Main.config.GetBool("auto-pickup.inventory.events.onItemPickup")) {
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
				if(PDC.Has(i, "bag-filter")) {
					bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i, null)));
				}
			}
		}
		Log.Debug(Main.plugin, "bags:" + bags.size());
		Log.Debug(Main.plugin, "Checking bag filters.");
		for(Bag bag : bags) {
			Log.Debug(Main.plugin, "bag: " + PDC.GetString(bag.item, "bag-uuid"));
			boolean c = false;
			for(Filter f : filters) {
				//Log.Debug(Main.plugin, "Filter: " + f.name);
				//Log.Debug(Main.plugin, "Bag Filter: " + PDC.GetString(bag.item, "bag-filter"));
				if(f.name.equalsIgnoreCase(PDC.GetString(bag.item, "bag-filter"))) {
					c = true;
					break;
				}
			}
			Log.Debug(Main.plugin, "Filter " + c);
			if(!c) {
				Log.Debug(Main.plugin, "No filters, skipping.");
				continue;
			}
			
			if(!IsItemInFilter(PDC.GetString(bag.item, "bag-filter"), item)) {
				Log.Debug(Main.plugin, "Item " + item.getType().toString() + " is not in the filter. Skipping.");
				continue;
			}
			

	    	List<Placeholder> placeholders = new ArrayList<Placeholder>();
	        if(PDC.Has(bag.item, "bag-weight") && PDC.Has(bag.item, "bag-weight-limit") && Main.weight.GetBool("enabled")) {
	        	placeholders.add(new Placeholder("%bar%", TextFeatures.CreateBarWeight(HavenBags.GetWeight(bag.item), PDC.GetDouble(bag.item, "bag-weight-limit"), Main.weight.GetInt("bar-length"))));
	        	placeholders.add(new Placeholder("%weight%", TextFeatures.LimitDecimal(String.valueOf(HavenBags.GetWeight(bag.item)),2)));
	        	placeholders.add(new Placeholder("%limit%", String.valueOf(PDC.GetDouble(bag.item, "bag-weight-limit").intValue())));
	        	placeholders.add(new Placeholder("%percent%", TextFeatures.LimitDecimal(String.valueOf(Utils.Percent(HavenBags.GetWeight(bag.item), PDC.GetDouble(bag.item, "bag-weight-limit"))), 2) + "%"));
	        	placeholders.add(new Placeholder("%bag-weight%", Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)));
	        }
			
			
			int maxContent = PDC.GetInt(bag.item, "bag-size");
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
						        	PDC.SetDouble(bag.item, "bag-weight", HavenBags.GetWeight(bag.content));
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
						        	PDC.SetDouble(bag.item, "bag-weight", HavenBags.GetWeight(bag.content));
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
	
	static boolean PutItemInBag(ItemStack item, Player player){
		if(BagData.isReady() == false) {
			return false;
		}
		
		Log.Debug(Main.plugin, "[DI-149] " + "AutoPickup");
		List<String> blacklist = Main.config.GetStringList("blacklist");
		if(blacklist != null) {
			if(blacklist.size() != 0) {
				Log.Debug(Main.plugin, "[DI-150] " + "Player World: " + player.getWorld().getName());
				for(String world : blacklist) {
					Log.Debug(Main.plugin, "[DI-151] " + "Blacklist: " + world);
					if(player.getWorld().getName().equalsIgnoreCase(world)) return false;
				}
			}
		}
		
		// For some reason handling ExecutableItems items, would duplicate the item.
		if (Bukkit.getPluginManager().getPlugin("ExecutableItems") != null) {
			if(Tags.Has((JavaPlugin)Bukkit.getPluginManager().getPlugin("ExecutableItems"), item.getItemMeta().getPersistentDataContainer(), "ei-id", TagType.STRING)) {
				Log.Debug(Main.plugin, "[DI-152] " + "Item is ExecutableItem, skipping.");
				return false;
			}
		}
		
		if(HavenBags.IsBag(item)) return false;
		
		Log.Debug(Main.plugin, "[DI-154] " + "PutItemInBag?");
		
		if(ItemFilter(item) == null) {
			Log.Debug(Main.plugin, "[DI-155] " + "false");
			//return false;
		}
		
        if(item.getType() == Material.AIR) return false;
	
		List<Bag> bags = new ArrayList<Bag>();
		Log.Debug(Main.plugin, "[DI-156] " + "Checking for bags.");
		for(ItemStack i : player.getInventory().getContents()) {
			//Log.Debug(Main.plugin, HavenBags.BagState(i).toString());
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				if(PDC.Has(i, "filter")) {
					bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i)));
				}
			}
		}
		Log.Debug(Main.plugin, "[DI-157] " + "bags:" + bags.size());
		Log.Debug(Main.plugin, "[DI-158] " + "Checking bag filters.");
		//if(HavenBags.IsItemBlacklisted(item)) return false;
		for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
			Data data = BagData.GetBag(HavenBags.GetBagUUID(bag.item), null);
			if(HavenBags.IsItemBlacklisted(item, data)) continue;
			Log.Debug(Main.plugin, "[DI-159] " + "bag: " + PDC.GetString(bag.item, "uuid"));
			if(BagData.IsBagOpen(PDC.GetString(bag.item, "uuid"), bag.item)) continue;
			if(HavenBags.IsBagFull(bag.item)) continue;
			boolean c = false;
			for(Filter f : filters) {
				//Log.Debug(Main.plugin, "Filter: " + f.name);
				//Log.Debug(Main.plugin, "Bag Filter: " + PDC.GetString(bag.item, "bag-filter"));
				if(f.name.equalsIgnoreCase(PDC.GetString(bag.item, "filter"))) {
					if(AutoPickup.filter.HasKey("filters." + f.name + ".permission.node")) {
						Log.Debug(Main.plugin, "[DI-160] " + "[AutoPickup] Permission");
						if(!AutoPickup.filter.GetString("filters." + f.name + ".permission.node").equalsIgnoreCase("none")) {
							Log.Debug(Main.plugin, "[DI-161] " + "[AutoPickup] Permission " + f.name);
							if(AutoPickup.filter.GetBool("filters." + f.name + ".permission.use")) {
								if(!player.hasPermission(AutoPickup.filter.GetString("filters." + f.name + ".permission.node"))) {
									Log.Debug(Main.plugin, "[DI-162] " + "[AutoPickup] Permission Use true - Player false");
									c = false; break;
								}else {
									Log.Debug(Main.plugin, "[DI-163] " + "[AutoPickup] Permission Use true - Player true");
								}
							}else {
								Log.Debug(Main.plugin, "[DI-164] " + "[AutoPickup] Permission Use false");
								c = false; break;
							}
						}
					}
					c = true;
					break;
				}
			}
			
			Log.Debug(Main.plugin, "[DI-165] " + "Filter " + c);
			if(!c) {
				Log.Debug(Main.plugin, "[DI-166] " + "No filters, skipping.");
				continue;
			}
			
			if(!IsItemInFilter(PDC.GetString(bag.item, "filter"), item)) {
				Log.Debug(Main.plugin, "[DI-167] " + "Item " + item.getType().toString() + " is not in the filter. Skipping.");
				continue;
			}
			

	    	List<Placeholder> placeholders = new ArrayList<Placeholder>();
	        if(PDC.Has(bag.item, "weight") && PDC.Has(bag.item, "weight-limit") && Main.weight.GetBool("enabled")) {
	        	placeholders.add(new Placeholder("%bar%", TextFeatures.CreateBarWeight(HavenBags.GetWeight(bag.item), PDC.GetDouble(bag.item, "weight-limit"), Main.weight.GetInt("bar-length"))));
	        	placeholders.add(new Placeholder("%weight%", TextFeatures.LimitDecimal(String.valueOf(HavenBags.GetWeight(bag.item)),2)));
	        	placeholders.add(new Placeholder("%limit%", String.valueOf(PDC.GetDouble(bag.item, "weight-limit").intValue())));
	        	placeholders.add(new Placeholder("%percent%", TextFeatures.LimitDecimal(String.valueOf(Utils.Percent(HavenBags.GetWeight(bag.item), PDC.GetDouble(bag.item, "weight-limit"))), 2) + "%"));
	        	placeholders.add(new Placeholder("%bag-weight%", Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)));
	        }
			
			
			int maxContent = PDC.GetInteger(bag.item, "size");
			Log.Debug(Main.plugin, "[DI-168] " + "cont:" + bag.content.size());
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
			Log.Debug(Main.plugin, "[DI-169] " + "Checking bag content.");
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
			
			Log.Debug(Main.plugin, "[DI-170] " + "maxContent:" + maxContent);
			//if(contSize >= maxContent) return false;
			Log.Debug(Main.plugin, "[DI-171] " + "contSize:" + contSize);
			if(contSize == 0) {
				if(bag.content.size() > 0) {
					bag.content.set(0, item);
				}else bag.content.add(item);
				if(Main.weight.GetBool("enabled")) {
		        	PDC.SetDouble(bag.item, "weight", HavenBags.GetWeight(bag.content));
					if(Main.weight.GetBool("weight-text-pickup")) {
						Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
								Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
							);
						weightMessage.Send(player);
					}
		        }
				HavenBags.UpdateBagItem(bag.item, player);
				//HavenBags.WriteBagToServer(bag.item, bag.content, player);
				BagData.UpdateBag(bag.item, bag.content);
				PickupSound(player);
				return true;
			}
			
			// Can't deal with empty bags.
			if(HavenBags.AddItemToInventory(bag.content, PDC.GetInteger(bag.item, "size"), item, player)) {
				if(Main.weight.GetBool("enabled")) {
		        	PDC.SetDouble(bag.item, "weight", HavenBags.GetWeight(bag.content));
					if(Main.weight.GetBool("weight-text-pickup")) {
						Message weightMessage = new Message(ChatMessageType.ACTION_BAR, 
								Lang.Parse(Main.weight.GetString("weight-lore"), placeholders, player)
							);
						weightMessage.Send(player);
					}
		        }
				HavenBags.UpdateBagItem(bag.item, player);
				//HavenBags.WriteBagToServer(bag.item, bag.content, player);
				BagData.UpdateBag(bag.item, bag.content);
				PickupSound(player);
				Log.Debug(Main.plugin, "[DI-172] " + "Item put in bag.");
				return true;
			}else {
				Log.Debug(Main.plugin, "[DI-173] " + "Item was not put in bag.");
				return false;
			}
		}
		
		for(String ebag : EtherealBags.getPlayerBags(player.getUniqueId())) {
			String key = EtherealBags.formatBagId(player.getUniqueId(), ebag);
			if(HavenBags.IsItemBlacklisted(item)) continue;
			Log.Debug(Main.plugin, "[DI-159] " + "ethereal bag: " + key);
			if(EtherealBags.isOpen(key)) continue;
			if(EtherealBags.isBagFull(player.getUniqueId(), ebag)) continue;
			boolean c = false;
			String filter = EtherealBags.getBagAutoPickup(player.getUniqueId(), ebag);
			for(Filter f : filters) {
				//Log.Debug(Main.plugin, "Filter: " + f.name);
				//Log.Debug(Main.plugin, "Bag Filter: " + PDC.GetString(bag.item, "bag-filter"));
				if(f.name.equalsIgnoreCase(filter)) {
					if(AutoPickup.filter.HasKey("filters." + f.name + ".permission.node")) {
						Log.Debug(Main.plugin, "[DI-160] " + "[AutoPickup] Permission");
						if(!AutoPickup.filter.GetString("filters." + f.name + ".permission.node").equalsIgnoreCase("none")) {
							Log.Debug(Main.plugin, "[DI-161] " + "[AutoPickup] Permission " + f.name);
							if(AutoPickup.filter.GetBool("filters." + f.name + ".permission.use")) {
								if(!player.hasPermission(AutoPickup.filter.GetString("filters." + f.name + ".permission.node"))) {
									Log.Debug(Main.plugin, "[DI-162] " + "[AutoPickup] Permission Use true - Player false");
									c = false; break;
								}else {
									Log.Debug(Main.plugin, "[DI-163] " + "[AutoPickup] Permission Use true - Player true");
								}
							}else {
								Log.Debug(Main.plugin, "[DI-164] " + "[AutoPickup] Permission Use false");
								c = false; break;
							}
						}
					}
					c = true;
					break;
				}
			}
			
			Log.Debug(Main.plugin, "[DI-165] " + "Filter " + c);
			if(!c) {
				Log.Debug(Main.plugin, "[DI-166] " + "No filters, skipping.");
				continue;
			}
			
			if(!IsItemInFilter(filter, item)) {
				Log.Debug(Main.plugin, "[DI-167] " + "Item " + item.getType().toString() + " is not in the filter. Skipping.");
				continue;
			}
			
			List<ItemStack> content = EtherealBags.getBagContentsOrEmpty(player.getUniqueId(), ebag);
			Log.Debug(Main.plugin, "[DI-169] " + "Checking bag content.");
			int contSize = 0;
			for(ItemStack i : content) {
				try {
					if(i.getType() != Material.AIR) {
						contSize++;
					}
				} catch (Exception e) {
					continue;
				}
			}
			
			
			Log.Debug(Main.plugin, "[DI-170] " + "maxContent:" + content.size());
			//if(contSize >= maxContent) return false;
			Log.Debug(Main.plugin, "[DI-171] " + "contSize:" + contSize);
			if(contSize == 0) {
				if(content.size() > 0) {
					content.set(0, item);
				}else content.add(item);
				EtherealBags.updateBagContents(player.getUniqueId(), ebag, content);
				PickupSound(player);
				return true;
			}
			
			// Can't deal with empty bags.
			HashMap<Boolean, List<ItemStack>> modified = HavenBags.AddItemToEtherealInventory(player, ebag, item);
			if(modified.containsKey(true)) {
				PickupSound(player);
				Log.Debug(Main.plugin, "[DI-172] " + "Item put in bag.");
				EtherealBags.updateBagContents(player.getUniqueId(), ebag, modified.get(true));
				return true;
			}else {
				Log.Debug(Main.plugin, "[DI-173] " + "Item was not put in bag.");
				return false;
			}
		}
		
		Log.Debug(Main.plugin, "[DI-174] " + "Item was not put in bag.");
		return false;
	}
	
	static String ItemFilter(ItemStack item) {
		for(Filter f : filters) {
			if(f.entries.contains(item.getType().toString())){
				return f.name;
			}
		}
		return null;
	}
	
	public static boolean IsItemInFilter(String filter, ItemStack item) {
		//Log.Debug(Main.plugin, "[DI-175] " + "IsItemInFilter?");
		int cmd = 0;
		if(item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
			cmd = item.getItemMeta().getCustomModelData();
		}
		
		for(Filter f : filters) {
			if(f.name.equalsIgnoreCase(filter)) {
				if(checkPlugins(f.entries, item)) return true;
				
				if(cmd != 0) {
					if(f.entries.contains(item.getType().toString() + "-" + cmd)){
						return true;
					}else if(f.entries.contains(item.getType().toString() + ":" + cmd)){
						return true;
					}else{
						if(f.entries.contains(item.getType().toString())){
							return true;
						}
					}
				}else {
					if(f.entries.contains(item.getType().toString())){
						return true;
					}
				}
			}
		}

		//Log.Debug(Main.plugin, "[DI-177] " + "IsItemInFilter false");
		return false;
	}
	
	// Check for items from other plugins, like Nexo, Oraxen, etc.
	public static Boolean checkPlugins(List<String> entries, ItemStack item) {
		// Assuming plugins use PDC.
		List<PluginTags> pluginTags = List.of(
				new PluginTags("Nexo", "nexo:", "id"),
				new PluginTags("Oraxen", "oraxen:", "id")
			);
		
		for(PluginTags plugin : pluginTags) {
			if(Main.plugins.GetBool("plugins." + plugin.name + ".enabled")) {
				if(Bukkit.getPluginManager().getPlugin(plugin.name) != null) {
					JavaPlugin jplugin = (JavaPlugin)Bukkit.getPluginManager().getPlugin(plugin.name);
					try {
						if(entries.contains(plugin.namespace + Tags.Get(jplugin, item.getItemMeta().getPersistentDataContainer(), plugin.pdcKey, TagType.STRING))) {
							return true;
						}
					}catch (Exception e) {} // Ignore. Faster than checking if it has the itemmeta or tag first.
				}
			}
		}
		

		if(Main.plugins.GetBool("plugins.ItemsAdder.enabled")) {
			if(Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
				CustomStack citem = CustomStack.byItemStack(item);
				if(citem != null) {
					if(entries.contains("itemsadder:" + citem.getId())) {
						return true;
					}
				}
			}
		}
		
		/*
		if(Main.plugins.GetBool("plugins.Oraxen.enabled")) {
			if(Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
				JavaPlugin oraxen = (JavaPlugin)Bukkit.getPluginManager().getPlugin("Oraxen");
				try {
					if(entries.contains("oraxen:" + Tags.Get(oraxen, item.getItemMeta().getPersistentDataContainer(), "id", TagType.STRING))) {
						return true;
					}
				}catch (Exception e) {} // Ignore. Faster than checking if it has the tag first.
			}
		}
		if(Main.plugins.GetBool("plugins.Nexo.enabled")) {
			if(Bukkit.getPluginManager().getPlugin("Nexo") != null) {
				JavaPlugin nexo = (JavaPlugin)Bukkit.getPluginManager().getPlugin("Nexo");
				try {
					if(entries.contains("nexo:" + Tags.Get(nexo, item.getItemMeta().getPersistentDataContainer(), "id", TagType.STRING))) {
						return true;
					}
				}catch (Exception e) {} // Ignore. Faster than checking if it has the tag first.

			}
		}
		*/
		return false;
	}
	
	boolean StackHasSpace(ItemStack stack, ItemStack pickup) {
		Log.Debug(Main.plugin, "[DI-178] " + "StackHasSpace?");
		int comb = stack.getAmount() + pickup.getAmount();
		Log.Debug(Main.plugin, "[DI-179] " + "comb: " + comb);
		if((stack.getAmount() + pickup.getAmount()) <= stack.getMaxStackSize()) {
			Log.Debug(Main.plugin, "[DI-180] " + "StackHasSpace true");
			return true;
		}else {
			Log.Debug(Main.plugin, "[DI-181] " + "StackHasSpace false");
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
	
	static void PickupSound(Player player) {
		Double pitch = 1.0;
		if(!Server.VersionEqualTo(Version.v1_17) && !Server.VersionEqualTo(Version.v1_17_1)) {
			pitch = Utils.RandomRange(Main.config.GetDouble("auto-pickup.sound.pitch.min"), Main.config.GetDouble("auto-pickup.sound.pitch.max"));
		}
		

		Sound sound = new Sound(Main.config.GetString("auto-pickup.sound.key"), 
    			Main.config.GetDouble("auto-pickup.sound.volume"), 
    			pitch);	
		sound.play(player);
		
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
		Log.Debug(Main.plugin, "[DI-182] " + "Checking for items in inventory, to put into bag.");
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
				if(item.getType().toString().contains("_SWORD") ||
						item.getType().toString().contains("_PICKAXE") ||
						item.getType().toString().contains("_AXE") ||
						item.getType().toString().contains("_SHOVEL") ||
						item.getType().toString().contains("_HOE") ||
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
	
	public static void PickupParticles(Player player, Location loc, ItemStack item) {
		int count = 10;
		double force = 0.1;
		try {
			player.spawnParticle(Particle.BLOCK, loc, count, 0, 0.1, 0, force, item.getType().createBlockData());
		} catch (Exception e) {
			player.spawnParticle(Particle.ITEM, loc, count, 0, 0.1, 0, force, item);
		}
		player.spawnParticle(Particle.SMOKE, loc, 5, 0, 0.1, 0, 0.02);
	}
}
