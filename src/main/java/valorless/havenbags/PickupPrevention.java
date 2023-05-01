package valorless.havenbags;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.valorlessutils.ValorlessUtils.Tags;

public class PickupPrevention implements Listener {
	public static JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
    	ItemStack bag = event.getItem().getItemStack();
    	SkullMeta bagMeta = (SkullMeta)bag.getItemMeta();
    	if(Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING) == null) return;
    	String owner = (String)Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING);
		if(!owner.equalsIgnoreCase(Bukkit.getPlayer(event.getEntity().getName()).getUniqueId().toString()) && Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "canbind", PersistentDataType.STRING) == "true") {
			event.setCancelled(true);
		}
    }
	
}
