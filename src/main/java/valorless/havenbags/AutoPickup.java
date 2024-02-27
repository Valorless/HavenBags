package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.sound.SFX;
import valorless.valorlessutils.utils.Utils;

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
    public void onEntityPickupItem(EntityPickupItemEvent event) {
		if(event.getEntityType() != EntityType.PLAYER) return;
		ItemStack item = event.getItem().getItemStack();
		if(HavenBags.IsBag(item))return;
		Player player = (Player)event.getEntity();
		Log.Debug(Main.plugin, item.getType().toString());
		event.setCancelled(PutItemInBag(item, player));
		if(event.isCancelled()) {
			int count = 10;
			double force = 0.1;
			try {
				//player.spawnParticle(Particle.BLOCK_DUST, event.getItem().getLocation(), 10, item.getType().createBlockData());
				player.spawnParticle(Particle.BLOCK_DUST, event.getItem().getLocation(), count, 0, 0.1, 0, force, item.getType().createBlockData());
			} catch (Exception e) {
				//player.spawnParticle(Particle.ITEM_CRACK, event.getItem().getLocation(), 10, item);
				player.spawnParticle(Particle.ITEM_CRACK, event.getItem().getLocation(), count, 0, 0.1, 0, force, item);
			}
			player.spawnParticle(Particle.SMOKE_NORMAL, event.getItem().getLocation(), 5, 0, 0.1, 0, 0.02);
			event.getItem().remove();
		}

		Log.Debug(Main.plugin, "Cancelled: " + event.isCancelled());
    }
	
	boolean PutItemInBag(ItemStack item, Player player){
		if(ItemFilter(item) == null) return false;
		
		List<Bag> bags = new ArrayList<Bag>();
		for(ItemStack i : player.getInventory().getContents()) {
			//Log.Debug(Main.plugin, HavenBags.BagState(i).toString());
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				if(NBT.Has(i, "bag-filter")) {
					bags.add(new Bag(i, HavenBags.LoadBagContentFromServer(i, null)));
				}
			}
		}
		Log.Debug(Main.plugin, "bags:" + bags.size());
		for(Bag bag : bags) {
			boolean c = false;
			for(Filter f : filters) {
				Log.Debug(Main.plugin, "Filter: " + f.name);
				Log.Debug(Main.plugin, "Bag Filter: " + NBT.GetString(bag.item, "bag-filter"));
				if(f.name.equalsIgnoreCase(NBT.GetString(bag.item, "bag-filter"))) {
					c = true;
					break;
				}
			}
			Log.Debug(Main.plugin, "c " + c);
			if(!c) continue;
			
			if(!IsItemInFilter(NBT.GetString(bag.item, "bag-filter"), item)) continue;
			
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
			for(ItemStack i : bag.content) {
				try {
					if(i.getType() != Material.AIR) {
						contSize++;
					}
				} catch (Exception e) {
					continue;
				}
			}
			Log.Debug(Main.plugin, "contsize:" + contSize);
			Log.Debug(Main.plugin, "size:" + maxContent);
			if(Contains(bag.content, item)) {
				Log.Debug(Main.plugin, "Contains");
				for(ItemStack i : bag.content) {
					if(i == null) continue;
					if(StackHasSpace(i, item)) {
						Log.Debug(Main.plugin, "stack Has Space");
						if(item.getType().equals(i.getType())){
							if(i.getAmount() != i.getMaxStackSize()) {
								i.setAmount(i.getAmount() + item.getAmount());
								Log.Debug(Main.plugin, bag.content.toString());
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
			else if(contSize < maxContent) {
				Log.Debug(Main.plugin, "Has Space");
				for(int i = 0; i < bag.content.size(); i++) {
					if(bag.content.get(i) == null) {
						bag.content.set(i, item);
						break;
					}
				}
				/*
				for(ItemStack i : bag.content) {
					if(i == null) {
						i = item;
						Log.Debug(Main.plugin, "Item Added");
						break;
					}
				}
				*/
				//bag.content.add(item);
				Log.Debug(Main.plugin, bag.content.toString());
				HavenBags.UpdateBagItem(bag.item, bag.content, player);
				HavenBags.WriteBagToServer(bag.item, bag.content, player);
				PickupSound(player);
				return true;
			}else {

				Log.Debug(Main.plugin, "else");
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
		for(Filter f : filters) {
			if(f.name.equalsIgnoreCase(filter)) {
				if(f.entries.contains(item.getType().toString())){
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	boolean StackHasSpace(ItemStack stack, ItemStack pickup) {
		//int diff = (stack.getAmount() + pickup.getAmount()) - 64;
		//diff = -diff;
		int comb = stack.getAmount() + pickup.getAmount();
		Log.Debug(Main.plugin, "comb: " + comb);
		if((stack.getAmount() + pickup.getAmount()) <= stack.getMaxStackSize()) {
			return true;
		}else return false;
	}
	
	boolean Contains(List<ItemStack> content, ItemStack item) {
		for(ItemStack i : content) {
			if (i == null) continue;
			if (i.getType() == item.getType()) return true;
		}
		return false;
	}
	
	void PickupSound(Player player) {
		Double pitch = Utils.RandomRange(Main.config.GetFloat("auto-pickup-pitch-min"), Main.config.GetFloat("auto-pickup-pitch-max"));
		//SFX.Play("ENTITY_ITEM_PICKUP", 0.8f, pitch.floatValue(), player);
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
}
