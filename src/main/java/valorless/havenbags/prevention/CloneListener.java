package valorless.havenbags.prevention;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;
import valorless.valorlessutils.nbt.NBT;

public class CloneListener implements Listener{

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		//Log.Debug(Main.plugin, event.getEventName());
		//Log.Debug(Main.plugin, event.getView().getPlayer().getName());
		//Log.Debug(Main.plugin, event.getPlayer().getName());
		//Log.Debug(Main.plugin, event.getInventory().getType().name());
		//for(ItemStack item : event.getPlayer().getInventory().getContents()) {
		//	if(item == null) continue;
		//	Log.Debug(Main.plugin, item.getType().name());
		//}
		Proccess(event.getPlayer());
    }
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		//Log.Debug(Main.plugin, event.getEventName());
		//Log.Debug(Main.plugin, event.getWhoClicked().getName());
		//Log.Debug(Main.plugin, event.getInventory().getType().name());
		Proccess(event.getWhoClicked());
	}
	
	public void Proccess(HumanEntity entity) {
		Player player = (Player)entity;
		for(ItemStack item : player.getInventory().getContents()) {
			if(item == null) continue;
			if(item.getItemMeta() == null) { return; }
			
			if(NBT.Has(item, "bag-uuid")) {
				while(item.getAmount() > 1) {
					Log.Debug(Main.plugin, "[DI-191] " + "Stacked bag found!");
					
					ItemStack clone = item.clone();
					clone.setAmount(1);
					
					ItemMeta meta = clone.getItemMeta();
					
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
	            	placeholders.add(new Placeholder("%size%", NBT.GetInt(clone, "bag-size")));
					
					boolean canbind = NBT.GetBool(clone, "bag-canBind");
					if(canbind) {
						meta.setDisplayName(Lang.Get("bag-unbound-name"));
						List<String> lore = new ArrayList<String>();
						for (String l : Lang.lang.GetStringList("bag-lore")) {
				        	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
				        }
				        if(NBT.Has(clone, "bag-size")) {
				        	lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
				        }
						meta.setLore(lore);
						clone.setItemMeta(meta);
					}else {
						meta.setDisplayName(Lang.Get("bag-ownerless-unused"));
						List<String> lore = new ArrayList<String>();
						for (String l : Lang.lang.GetStringList("bag-lore")) {
				        	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
				        }
				        if(NBT.Has(clone, "bag-size")) {
				        	lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
				        }
						meta.setLore(lore);
						clone.setItemMeta(meta);
					}
					
					
					Log.Debug(Main.plugin, "[DI-192] " + "Giving cloned bag a new id");
					NBT.SetString(clone, "bag-uuid", UUID.randomUUID().toString());
					NBT.SetString(clone, "bag-owner", "null");

					Log.Debug(Main.plugin, "[DI-193] " + "Splitting bags apart");
					item.setAmount(item.getAmount() - 1);
					player.getInventory().addItem(clone);
				}
			}
        }
	}
}
