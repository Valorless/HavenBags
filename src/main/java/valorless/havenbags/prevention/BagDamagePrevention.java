package valorless.havenbags.prevention;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.features.BagHealth;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.logging.Log;

public class BagDamagePrevention implements Listener{

	public static void init() {
		Log.debug(Main.plugin, "[DI-8] Registering BagDamagePrevention");
		Bukkit.getServer().getPluginManager().registerEvents(new BagDamagePrevention(), Main.plugin);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().getType() == EntityType.ITEM) {
			if(e.getEntity() instanceof Item dropped){
				ItemStack item = dropped.getItemStack();
				if(HavenBags.isBag(item)) {
					if(Main.config.getBool("protect-bags.enabled")) {
						boolean safe = true;
						boolean protect = false;
						
						// If bag health is enabled, the bag is only considered "safe" if its current health is above 0.
						// If bag health is disabled, all bags are considered safe.
						if(BagHealth.isEnabled()) {
							safe = BagHealth.isBagSafe(dropped);
						}
						
						if(Main.config.getBool("protect-bags.unbound") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == true) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.getBool("protect-bags.bound") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == true) {
							if(!PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.getBool("protect-bags.unused") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.getBool("protect-bags.used") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("ownerless")) {
								protect = true;
							}
						}
						
						if(protect && safe) { // safe is considered true if either bag health is disabled or the bag's current health is above 0
							e.setCancelled(true);
							return;
						}
						// If the bag is not protected, it will be allowed to take damage as normal, and if bag health is enabled, it will lose durability as normal. If the bag's health reaches 0, it will be considered "broken" and will be deleted on drop/despawn regardless of protection settings.
					}
					
					// Bags that are not protected will be deleted immediately on drop/despawn if hardcore bags is enabled, regardless of their health.
					// This is to prevent players from dropping unprotected bags and leaving them to despawn, which would allow them to bypass the protection settings.
					if(Main.config.getBool("hardcore-bags.enabled")) {
						String bagID = HavenBags.getBagUUID(item);
						if(Main.config.getBool("hardcore-bags.unbound") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == true) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								Database.deleteBag(bagID);
								return;
							}
						}
						if(Main.config.getBool("hardcore-bags.bound") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == true) {
							if(!PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								Database.deleteBag(bagID);
								return;
							}
						}
						if(Main.config.getBool("hardcore-bags.unused") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								Database.deleteBag(bagID);
								return;
							}
						}
						if(Main.config.getBool("hardcore-bags.used") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("ownerless")) {
								Database.deleteBag(bagID);
								return;
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		if(e.getEntity().getType() == EntityType.ITEM) {
			Item dropped = e.getEntity();
			ItemStack item = dropped.getItemStack();
			if(HavenBags.isBag(item)) {
				if(HavenBags.isBag(item)) {
					if(Main.config.getBool("protect-bags.enabled")) {
						boolean safe = true;
						boolean protect = false;
						
						// If bag health is enabled, the bag is only considered "safe" if its current health is above 0.
						// If bag health is disabled, all bags are considered safe.
						if(BagHealth.isEnabled()) {
							safe = BagHealth.isBagSafe(dropped);
						}
						
						if(Main.config.getBool("protect-bags.unbound") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == true) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.getBool("protect-bags.bound") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == true) {
							if(!PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.getBool("protect-bags.unused") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.getBool("protect-bags.used") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("ownerless")) {
								protect = true;
							}
						}
						
						if(protect && safe) { // safe is considered true if either bag health is disabled or the bag's current health is above 0
							// Prevent the bag from despawning by cancelling the event.
							e.setCancelled(true);
							return;
						}
						// If the bag is not protected, it will be allowed to take damage as normal, and if bag health is enabled, it will lose durability as normal. If the bag's health reaches 0, it will be considered "broken" and will be deleted on drop/despawn regardless of protection settings.
					}
					
					// Bags that are not protected will be deleted immediately on drop/despawn if hardcore bags is enabled, regardless of their health.
					// This is to prevent players from dropping unprotected bags and leaving them to despawn, which would allow them to bypass the protection settings.
					if(Main.config.getBool("hardcore-bags.enabled")) {
						String bagID = HavenBags.getBagUUID(item);
						if(Main.config.getBool("hardcore-bags.unbound") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == true) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								Database.deleteBag(bagID);
								return;
							}
						}
						if(Main.config.getBool("hardcore-bags.bound") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == true) {
							if(!PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								Database.deleteBag(bagID);
								return;
							}
						}
						if(Main.config.getBool("hardcore-bags.unused") && BagState.getState(item) == BagState.NEW &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("null")) {
								Database.deleteBag(bagID);
								return;
							}
						}
						if(Main.config.getBool("hardcore-bags.used") && BagState.getState(item) == BagState.USED &&
								PDC.getBoolean(item, "binding") == false) {
							if(PDC.getString(item, "owner").equalsIgnoreCase("ownerless")) {
								Database.deleteBag(bagID);
								return;
							}
						}
					}
				}
			}
		}
	}

}
