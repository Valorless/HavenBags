package valorless.havenbags.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import valorless.havenbags.Database;
import valorless.havenbags.Database.BagSimple;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Bag;
import valorless.valorlessutils.logging.Log;

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
	private static final HashMap<UUID, BukkitTask> tasks = new HashMap<>();
	
	public static void init() {
		Log.debug(Main.plugin, "[DI-256] Registering BagEffects");
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
		Object[] f = Main.effects.getConfigurationSection("effects").getKeys(false).toArray();
		Log.debug(Main.plugin, "[DI-258] " + "Effects: " + f.length);
		for(int i = 0; i < f.length; i++) {
			String id = String.valueOf(f[i]);
			String name = Lang.parse(Main.effects.getString(String.format("effects.%s.displayname", id)), null);
			HashMap<PotionEffectType, Integer> potions = new HashMap<>();
			for(Object e : Main.effects.getConfigurationSection(String.format("effects.%s.potions", id)).getKeys(false).toArray()) {
				PotionEffectType type = null;
				try {
					type = getPotionType(e.toString());
				} catch (Exception e1) {
					Log.error(Main.plugin, e1.getMessage());
					//e1.printStackTrace();
					continue;
				}
				Integer level = Main.effects.getInt(String.format("effects.%s.potions.%s", id, e.toString()));
				potions.put(type, level);
			}
			
			BagEffect effect = new BagEffect(name, potions);
			effects.put(id, effect);

			Log.debug(Main.plugin, "[DI-259] " + "Effect: " + id);
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
		for(PotionEffectType e : Registry.EFFECT) {
			//Log.Debug(Main.plugin, e.getName());
			if(e.getKey().toString().equalsIgnoreCase(key)) return e;
		}
		PotionEffectType legacy = LegacyEffect(key);
		if(legacy != null) return legacy;
		else 
		throw new Exception(String.format("Invalid PotionEffectType '%s'.", key));
	}
	
	private static PotionEffectType LegacyEffect(String key) {
		// Just as a backup for new effect names, as HavenBags is 1.20 native,
		// and new names were set in 1.21.
		// Mainly for if you use 1.21 names while on 1.20.
        return switch (key) {
            case "RESISTANCE" -> {
                Log.debug(Main.plugin, "Legacy Effect: RESISTANCE - PotionEffectType.DAMAGE_RESISTANCE");
                yield PotionEffectType.RESISTANCE;
            }
            case "STRENGTH" -> {
                Log.debug(Main.plugin, "Legacy Effect: STRENGTH - PotionEffectType.INCREASE_DAMAGE");
                yield PotionEffectType.STRENGTH;
            }
            case "HASTE" -> {
                Log.debug(Main.plugin, "Legacy Effect: HASTE - PotionEffectType.FAST_DIGGING");
                yield PotionEffectType.HASTE;
            }
            case "MINING_FATIGUE" -> {
                Log.debug(Main.plugin, "Legacy Effect: MINING_FATIGUE - PotionEffectType.SLOW_DIGGING");
                yield PotionEffectType.MINING_FATIGUE;
            }
            case "JUMP_BOOST" -> {
                Log.debug(Main.plugin, "Legacy Effect: JUMP_BOOST - PotionEffectType.JUMP");
                yield PotionEffectType.JUMP_BOOST;
            }
            default -> null;
        };
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
        int duration = Main.config.getInt("effects.refresh-rate") + 5;
	    BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
	        
	        for(BagSimple bag : HavenBags.getBagsDataInInventory(player)) {
	        	Bag data = Database.getBag(HavenBags.getBagUUID(bag.item), null);
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
