package valorless.havenbags.prevention;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.features.BagHealth;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;

public class BagDamagePrevention implements Listener{
	String Name = "§7[§aHaven§bBags§7]§r";

	public static void init() {
		Log.Debug(Main.plugin, "[DI-8] Registering BagDamagePrevention");
		Bukkit.getServer().getPluginManager().registerEvents(new BagDamagePrevention(), Main.plugin);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().getType() == EntityType.ITEM) {
			if(e.getEntity() instanceof Item dropped){
				ItemStack item = dropped.getItemStack();
				if(HavenBags.IsBag(item)) {
					if(Main.config.GetBool("protect-bags.enabled")) {
						boolean safe = true;
						boolean protect = false;
						
						// If bag health is enabled, the bag is only considered "safe" if its current health is above 0.
						// If bag health is disabled, all bags are considered safe.
						if(BagHealth.isEnabled()) {
							safe = BagHealth.isBagSafe(dropped);
						}
						
						if(Main.config.GetBool("protect-bags.unbound") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == true) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.GetBool("protect-bags.bound") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == true) {
							if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.GetBool("protect-bags.unused") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.GetBool("protect-bags.used") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
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
					if(Main.config.GetBool("hardcore-bags.enabled")) {
						String bagID = HavenBags.GetBagUUID(item);
						if(Main.config.GetBool("hardcore-bags.unbound") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == true) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
						if(Main.config.GetBool("hardcore-bags.bound") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == true) {
							if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
						if(Main.config.GetBool("hardcore-bags.unused") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
						if(Main.config.GetBool("hardcore-bags.used") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
								BagData.DeleteBag(bagID);
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
			if(HavenBags.IsBag(item)) {
				if(HavenBags.IsBag(item)) {
					if(Main.config.GetBool("protect-bags.enabled")) {
						boolean safe = true;
						boolean protect = false;
						
						// If bag health is enabled, the bag is only considered "safe" if its current health is above 0.
						// If bag health is disabled, all bags are considered safe.
						if(BagHealth.isEnabled()) {
							safe = BagHealth.isBagSafe(dropped);
						}
						
						if(Main.config.GetBool("protect-bags.unbound") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == true) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.GetBool("protect-bags.bound") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == true) {
							if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.GetBool("protect-bags.unused") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								protect = true;
							}
						}
						if(Main.config.GetBool("protect-bags.used") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
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
					if(Main.config.GetBool("hardcore-bags.enabled")) {
						String bagID = HavenBags.GetBagUUID(item);
						if(Main.config.GetBool("hardcore-bags.unbound") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == true) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
						if(Main.config.GetBool("hardcore-bags.bound") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == true) {
							if(!PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
						if(Main.config.GetBool("hardcore-bags.unused") && HavenBags.BagState(item) == HavenBags.BagState.New &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("null")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
						if(Main.config.GetBool("hardcore-bags.used") && HavenBags.BagState(item) == HavenBags.BagState.Used &&
								PDC.GetBoolean(item, "binding") == false) {
							if(PDC.GetString(item, "owner").equalsIgnoreCase("ownerless")) {
								BagData.DeleteBag(bagID);
								return;
							}
						}
					}
				}
			}
		}
	}

}
