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
import valorless.havenbags.enums.BagState;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.utils.Utils;


public class Encumbering implements Listener {
	
	public static void init() {
		Log.debug(Main.plugin, "[DI-17] Registering Encumbering");
		Bukkit.getServer().getPluginManager().registerEvents(new Encumbering(), Main.plugin);
		
		reload();
	}
	
	private static final List<PotionEffect> effects = new ArrayList<>();
	private static Double percent = 0.0;
	private static String message;
	private static String not;
	private static boolean enabled;
	private static final List<BagWeight> bagWeights = new ArrayList<>();
	private static final List<Player> encumbered = new ArrayList<>();
	
	private static class BagWeight{
		public Double weight = 0.0;
		public Player player = null;
		public BagWeight(Player player, Double weight) {
			this.player = player;
		}
	}
	
	static BagWeight getBag(Player player) {
		for(BagWeight bag : bagWeights) {
			if(bag.player == player) return bag;
		}
		return null;
	}
	
	static boolean contains(Player player) {
		for(BagWeight bag : bagWeights) {
			if(bag.player == player) return true;
		}
		return false;
	}
	
	static boolean isEncumbered(Player player) {
		for(BagWeight bag : bagWeights) {
			if(bag.player == player) {
				if(bag.weight > percent) return true;
			}
		}
		return false;
	}
	
	public static void reload() {
		Log.debug(Main.plugin, "[DI-91] " + "[Encumbering] Reloading.");
		enabled = Main.weight.getBool("over-encumber.enabled");
		Log.debug(Main.plugin, "[DI-92] " + "[Encumbering] " + enabled);
		percent = Main.weight.getDouble("over-encumber.percent");
		Log.debug(Main.plugin, "[DI-93] " + "[Encumbering] " + percent);
		message = Lang.parse(Lang.get("prefix") + Main.weight.getString("over-encumber.message"), null);
		Log.debug(Main.plugin, "[DI-94] " + "[Encumbering] " + message);
		not = Lang.parse(Lang.get("prefix") + Main.weight.getString("over-encumber.not"), null);
		Log.debug(Main.plugin, "[DI-95] " + "[Encumbering] " + not);
		reloadEffects();
		encumbered.clear();
		bagWeights.clear();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!player.getActivePotionEffects().isEmpty()) {
				for(PotionEffect effect : effects) {
					Log.debug(Main.plugin, "[DI-96] " + effect.toString());
					if(player.hasPotionEffect(effect.getType())) {
						for(PotionEffect eff : player.getActivePotionEffects()) {
							if(eff.getAmplifier() != effect.getAmplifier()) continue;
								player.removePotionEffect(effect.getType());
						
						}
					}
				}
			}
			updateWeight(player);
		}
	}
	
	public static void reloadEffects() {
		List<String> effectsCfg = Main.weight.getStringList("over-encumber.effects");
		effects.clear();
		for(String eff : effectsCfg) {
			String[] split = eff.split(":");
			PotionEffectType type = PotionEffectType.getByName(split[0]);
			int level = Integer.parseInt(split[1]) - 1;
			try {
				PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, level, false, false, true);
				effects.add(effect);
			}catch(Exception e) {
				Log.error(Main.plugin, "[Encumbering] Failed to load PotionEffect '" + split[0] + "'");
				Log.error(Main.plugin, "[Encumbering] It may have a different name on some server versions.");
			}
			Log.debug(Main.plugin, "[DI-97] " + "[Encumbering] " + eff);
		}
	}
		
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false) // Have this listen last.
	public static void onInventoryClose(InventoryCloseEvent e) {
		if(!enabled) return;
		Player player = (Player)e.getPlayer();
        updateWeight(player);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false) // Have this listen last.
    public void onEntityPickupItem(EntityPickupItemEvent e) {
		if(!enabled) return;
		if(e.getEntityType() != EntityType.PLAYER) return;
		Player player = (Player)e.getEntity();
		if(e.getItem().getOwner() != null) {
			if(e.getItem().getOwner() != player.getUniqueId()) return;
		}
		if(HavenBags.isBag(e.getItem().getItemStack())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			    public void run() {
					updateWeight(player);
			    }
			}, 5L);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false) // Have this listen last.
    public void onPlayerDropItem(PlayerDropItemEvent e) {
		if(!enabled) return;
		Player player = e.getPlayer();
		if(HavenBags.isBag(e.getItemDrop().getItemStack())) {
			updateWeight(player);
		}
	}
	
	
	public static void updateWeight(Player player) {
		Log.debug(Main.plugin, "[DI-98] " + "[Encumbering] " + player.toString());
		for(BagWeight bw : bagWeights) {
			if(bw.player == player) bw.weight = 0.0; 
		}
		for(ItemStack i : player.getInventory().getContents()) {
			//Log.Debug(Main.plugin, HavenBags.BagState(i).toString());
			if(HavenBags.isBag(i) && BagState.getState(i) == BagState.USED) {
				String uuid = PDC.getString(i, "uuid");
				if(uuid.equalsIgnoreCase("null")) continue;
				if(PDC.has(i, "weight")) {
					Double weight = Utils.Percent(PDC.getDouble(i, "weight"), PDC.getDouble(i, "weight-limit"));
					Log.debug(Main.plugin, "[DI-99] " + "[Encumbering] " + weight);
					//bagWeights.add(new Bag(uuid, Utils.Percent(PDC.GetDouble(i, "bag-weight"), PDC.GetDouble(i, "bag-weight-limit"))));
					if(!contains(player)) {
						BagWeight bag = new BagWeight(player, weight);
						bagWeights.add(bag);
						Log.debug(Main.plugin, "[DI-100] " + "[Encumbering] Added weight");
					}else {
						if(weight > getBag(player).weight) {
							getBag(player).weight = weight;
							Log.debug(Main.plugin, "[DI-101] " + "[Encumbering] Updated weight");
						}
					}
					
				}
			}
		}
		
		if(!encumbered.contains(player)) {
			if(!isEncumbered(player)) return;
			player.sendMessage(message);
			encumbered.add(player);
			for(PotionEffect effect : effects) {
				Log.debug(Main.plugin, "[DI-102] " + effect.toString());
				if(!player.hasPotionEffect(effect.getType())) {
					player.addPotionEffect(effect);
				}
			}
		}
		if(encumbered.contains(player)){
			if(isEncumbered(player)) return;
			player.sendMessage(not);
			encumbered.remove(player);
			if(player.getActivePotionEffects().isEmpty()) return;
			for(PotionEffect effect : effects) {
				Log.debug(Main.plugin, "[DI-103] " + effect.toString());
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
