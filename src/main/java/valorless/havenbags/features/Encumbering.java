package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.utils.Utils;


public class Encumbering implements Listener {
	
	private static List<PotionEffect> effects = new ArrayList<PotionEffect>();
	private static Double percent = 0.0;
	private static String message;
	private static String not;
	private static boolean enabled;
	private static List<BagWeight> bagWeights = new ArrayList<BagWeight>();
	private static List<Player> encumbered = new ArrayList<Player>();
	
	private static class BagWeight{
		public Double weight = 0.0;
		public Player player = null;
		public BagWeight(Player player, Double weight) {
			this.player = player;
		}
	}
	
	static BagWeight GetBag(Player player) {
		for(BagWeight bag : bagWeights) {
			if(bag.player == player) return bag;
		}
		return null;
	}
	
	static boolean Contains(Player player) {
		for(BagWeight bag : bagWeights) {
			if(bag.player == player) return true;
		}
		return false;
	}
	
	static boolean IsEncumbered(Player player) {
		for(BagWeight bag : bagWeights) {
			if(bag.player == player) {
				if(bag.weight > percent) return true;
			}
		}
		return false;
	}
	
	public static void Reload() {
		Log.Debug(Main.plugin, "[DI-91] " + "[Encumbering] Reloading.");
		enabled = Main.weight.GetBool("over-encumber.enabled");
		Log.Debug(Main.plugin, "[DI-92] " + "[Encumbering] " + enabled);
		percent = Main.weight.GetFloat("over-encumber.percent");
		Log.Debug(Main.plugin, "[DI-93] " + "[Encumbering] " + percent);
		message = Lang.Parse(Lang.Get("prefix") + Main.weight.GetString("over-encumber.message"), null);
		Log.Debug(Main.plugin, "[DI-94] " + "[Encumbering] " + message);
		not = Lang.Parse(Lang.Get("prefix") + Main.weight.GetString("over-encumber.not"), null);
		Log.Debug(Main.plugin, "[DI-95] " + "[Encumbering] " + not);
		ReloadEffects();
		encumbered.clear();
		bagWeights.clear();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getActivePotionEffects().size() != 0) {
				for(PotionEffect effect : effects) {
					Log.Debug(Main.plugin, "[DI-96] " + effect.toString());
					if(player.hasPotionEffect(effect.getType())) {
						for(PotionEffect eff : player.getActivePotionEffects()) {
							if(eff.getAmplifier() != effect.getAmplifier()) continue;
								player.removePotionEffect(effect.getType());
						
						}
					}
				}
			}
			UpdateWeight(player);
		}
	}
	
	public static void ReloadEffects() {
		List<String> effectsCfg = Main.weight.GetStringList("over-encumber.effects");
		effects.clear();
		for(String eff : effectsCfg) {
			String[] split = eff.split(":");
			PotionEffectType type = PotionEffectType.getByName(split[0]);
			int level = Integer.valueOf(split[1]) - 1;
			try {
				PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, level, false, false, true);
				effects.add(effect);
			}catch(Exception e) {
				Log.Error(Main.plugin, "[Encumbering] Failed to load PotionEffect '" + split[0] + "'");
				Log.Error(Main.plugin, "[Encumbering] It may have a different name on some server versions.");
			}
			Log.Debug(Main.plugin, "[DI-97] " + "[Encumbering] " + eff);
		}
	}
		
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false) // Have this listen last.
	public static void OnInventoryClose(InventoryCloseEvent e) {
		if(!enabled) return;
		Player player = (Player)e.getPlayer();
		if(player == null) return;
		UpdateWeight(player);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false) // Have this listen last.
    public void onEntityPickupItem(EntityPickupItemEvent e) {
		if(!enabled) return;
		if(e.getEntityType() != EntityType.PLAYER) return;
		Player player = (Player)e.getEntity();
		if(e.getItem().getOwner() != null) {
			if(e.getItem().getOwner() != player.getUniqueId()) return;
		}
		if(HavenBags.IsBag(e.getItem().getItemStack())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			    public void run() {
					UpdateWeight(player);
			    }
			}, 5L);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false) // Have this listen last.
    public void onPlayerDropItem(PlayerDropItemEvent e) {
		if(!enabled) return;
		Player player = (Player)e.getPlayer();
		if(HavenBags.IsBag(e.getItemDrop().getItemStack())) {
			UpdateWeight(player);
		}
	}
	
	
	public static void UpdateWeight(Player player) {
		Log.Debug(Main.plugin, "[DI-98] " + "[Encumbering] " + player.toString());
		for(BagWeight bw : bagWeights) {
			if(bw.player == player) bw.weight = 0.0; 
		}
		for(ItemStack i : player.getInventory().getContents()) {
			//Log.Debug(Main.plugin, HavenBags.BagState(i).toString());
			if(HavenBags.IsBag(i) && HavenBags.BagState(i) == HavenBags.BagState.Used) { 
				String uuid = NBT.GetString(i, "bag-uuid");
				if(uuid.equalsIgnoreCase("null")) continue;
				if(NBT.Has(i, "bag-weight")) {
					Double weight = Utils.Percent(NBT.GetDouble(i, "bag-weight"), NBT.GetDouble(i, "bag-weight-limit"));
					Log.Debug(Main.plugin, "[DI-99] " + "[Encumbering] " + weight);
					//bagWeights.add(new Bag(uuid, Utils.Percent(NBT.GetDouble(i, "bag-weight"), NBT.GetDouble(i, "bag-weight-limit"))));
					if(!Contains(player)) {
						BagWeight bag = new BagWeight(player, weight);
						bagWeights.add(bag);
						Log.Debug(Main.plugin, "[DI-100] " + "[Encumbering] Added weight");
					}else {
						if(weight > GetBag(player).weight) {
							GetBag(player).weight = weight;
							Log.Debug(Main.plugin, "[DI-101] " + "[Encumbering] Updated weight");
						}
					}
					
				}
			}
		}
		
		if(!encumbered.contains(player)) {
			if(!IsEncumbered(player)) return;
			player.sendMessage(message);
			encumbered.add(player);
			for(PotionEffect effect : effects) {
				Log.Debug(Main.plugin, "[DI-102] " + effect.toString());
				if(!player.hasPotionEffect(effect.getType())) {
					player.addPotionEffect(effect);
				}
			}
		}
		if(encumbered.contains(player)){
			if(IsEncumbered(player)) return;
			player.sendMessage(not);
			encumbered.remove(player);
			if(player.getActivePotionEffects().size() == 0) return;
			for(PotionEffect effect : effects) {
				Log.Debug(Main.plugin, "[DI-103] " + effect.toString());
				if(player.hasPotionEffect(effect.getType())) {
					for(PotionEffect eff : player.getActivePotionEffects()) {
						if(eff.getAmplifier() != effect.getAmplifier()) continue;
							player.removePotionEffect(effect.getType());
					
					}
				}
			}
		}
	}
}
