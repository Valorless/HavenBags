package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Bag;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.ValorlessUtils.Log;

public class BagEffects implements Listener {
	
	public static class BagEffect {
		public String name;
		public HashMap<PotionEffectType, Integer> potions = new HashMap<>();
		
		public BagEffect(String name, HashMap<PotionEffectType, Integer> potions) {
			this.name = name;
			this.potions = potions;
		}
	}
	
	public static HashMap<String, BagEffect> effects = new HashMap<>();
	private static HashMap<UUID, BukkitTask> tasks = new HashMap<>();
	
	public static void init() {
		Log.Debug(Main.plugin, "[DI-256] Registering BagEffects");
		Bukkit.getServer().getPluginManager().registerEvents(new BagEffects(), Main.plugin);
		loadEffects();
		reload();
	}
	
	public static void reload() {
		tasks.clear();
		for(Player player : Bukkit.getOnlinePlayers()) {
		    createTask(player);
		}
	}
	
	public static void shutdown() {
		for(BukkitTask task : tasks.values()) {
			task.cancel();
		}
	}
	
	public static void loadEffects() {
		Object[] f = Main.effects.GetConfigurationSection("effects").getKeys(false).toArray();
		Log.Debug(Main.plugin, "[DI-258] " + "Effects: " + f.length);
		for(int i = 0; i < f.length; i++) {
			String id = String.valueOf(f[i]);
			String name = Lang.Parse(Main.effects.GetString(String.format("effects.%s.displayname", id)), null);
			HashMap<PotionEffectType, Integer> potions = new HashMap<>();
			for(Object e : Main.effects.GetConfigurationSection(String.format("effects.%s.potions", id)).getKeys(false).toArray()) {
				PotionEffectType type = null;
				try {
					type = getPotionType(e.toString());
				} catch (Exception e1) {
					Log.Error(Main.plugin, e1.getMessage());
					//e1.printStackTrace();
					continue;
				}
				Integer level = Main.effects.GetInt(String.format("effects.%s.potions.%s", id, e.toString()));
				potions.put(type, level);
			}
			
			BagEffect effect = new BagEffect(name, potions);
			effects.put(id, effect);

			Log.Debug(Main.plugin, "[DI-259] " + "Effect: " + id);
		}
	}
	
	public static boolean hasEffect(String id) {
		return effects.containsKey(id);
	}
	
	public static BagEffect getEffect(String id){
		return effects.get(id);
	}
	
	public static String getEffectDisplayname(String id) {
		return effects.containsKey(id) ? effects.get(id).name : "null";
	}
	
	public static List<String> getEffectNames(){
		List<String> effectnames = new ArrayList<String>();
		for(Entry<String, BagEffect> effect : effects.entrySet()) {
			effectnames.add(effect.getKey());
		}
		return effectnames;
	}
	
	public static PotionEffectType getPotionType(String key) throws Exception {
		for(PotionEffectType e : PotionEffectType.values()) {
			//Log.Debug(Main.plugin, e.getName());
			if(e.getName().equalsIgnoreCase(key)) return e;
		}
		PotionEffectType legacy = LegacyEffect(key);
		if(legacy != null) return legacy;
		else throw new Exception(String.format("Invalid PotionEffectType '%s'.", key));
	}
	
	private static PotionEffectType LegacyEffect(String key) {
		// Just as a backup for new effect names, as HavenBags is 1.20 native,
		// and new names were set in 1.21.
		// Mainly for if you use 1.21 names while on 1.20.
		switch(key) {
			case "RESISTANCE" : 
				Log.Debug(Main.plugin, "Legacy Effect: RESISTANCE - PotionEffectType.DAMAGE_RESISTANCE");
				return PotionEffectType.DAMAGE_RESISTANCE;
			case "STRENGTH" : 
				Log.Debug(Main.plugin, "Legacy Effect: STRENGTH - PotionEffectType.INCREASE_DAMAGE");
				return PotionEffectType.INCREASE_DAMAGE;
			case "HASTE" : 
				Log.Debug(Main.plugin, "Legacy Effect: HASTE - PotionEffectType.FAST_DIGGING");
				return PotionEffectType.FAST_DIGGING;
			case "MINING_FATIGUE" : 
				Log.Debug(Main.plugin, "Legacy Effect: MINING_FATIGUE - PotionEffectType.SLOW_DIGGING");
				return PotionEffectType.SLOW_DIGGING;
			case "JUMP_BOOST" : 
				Log.Debug(Main.plugin, "Legacy Effect: JUMP_BOOST - PotionEffectType.JUMP");
				return PotionEffectType.JUMP;
		}
		return null;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
	    createTask(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		BukkitTask task = tasks.get(e.getPlayer().getUniqueId());
		if(task == null) return;
		task.cancel();
		tasks.remove(e.getPlayer().getUniqueId());
	}
	
	public static void createTask(Player player) {
	    UUID uuid = player.getUniqueId();

	    // Avoid duplicate tasks, just to be sure.
	    if (tasks.containsKey(uuid)) return;

        //int duration = 105;
        int duration = Main.config.GetInt("effects.refresh-rate") + 5;
	    BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
	        
	        for(Bag bag : HavenBags.GetBagsDataInInventory(player)) {
	        	Data data = BagData.GetBag(HavenBags.GetBagUUID(bag.item), null);
	        	if(data.getEffect() == null) continue;
	        	if(data.getEffect().equalsIgnoreCase("null")) continue;
	        	
	        	BagEffect effect = getEffect(data.getEffect());
        		for(Entry<PotionEffectType, Integer> ent : effect.potions.entrySet()) {
	        	    player.addPotionEffect(new PotionEffect(ent.getKey(), duration, ent.getValue()-1, false, false));
        		}
	        }
	        
	    }, 0L, duration-5); // 5 extra ticks to be sure it doesn't run out.

	    tasks.put(uuid, task);
	}
	
}
