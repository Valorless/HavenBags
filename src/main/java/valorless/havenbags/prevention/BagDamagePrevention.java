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
					if(Main.config.GetBool("protect-bags")) {
						e.setCancelled(true);
					}
					if(Main.config.GetBool("hardcore-bags")) {
						String bagID = HavenBags.GetBagUUID(item);
						BagData.DeleteBag(bagID);
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
				if(Main.config.GetBool("protect-bags")) {
					e.setCancelled(true);
				}
				if(Main.config.GetBool("hardcore-bags")) {
					String bagID = HavenBags.GetBagUUID(item);
					BagData.DeleteBag(bagID);
				}
			}

		}
	}

}
