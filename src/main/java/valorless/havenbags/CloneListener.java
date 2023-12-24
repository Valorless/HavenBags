package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.ValorlessUtils.Utils;
import valorless.valorlessutils.nbt.NBT;

public class CloneListener implements Listener{

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Proccess(event.getPlayer());
    }
	
	public void Proccess(HumanEntity entity) {
		Player player = (Player)entity;
		for(ItemStack item : player.getInventory().getContents()) {
			if(item == null) { return; }
			if(item.getItemMeta() == null) { return; }
			
			if(NBT.Has(item, "bag-uuid")) {
				while(item.getAmount() > 1) {
					Log.Debug(Main.plugin, "Stacked bag found!");
					
					ItemStack clone = item.clone();
					clone.setAmount(1);
					ItemMeta meta = clone.getItemMeta();
					
					boolean canbind = NBT.GetBool(clone, "bag-canBind");
					if(canbind) {
						meta.setDisplayName(Lang.Get("bag-unbound-name"));
						List<String> lore = new ArrayList<String>();
						for (String l : Lang.lang.GetStringList("bag-lore")) {
							if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, (Player)player));
						}
						for (String l : Lang.lang.GetStringList("bag-size")) {
							if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, NBT.GetInt(clone, "bag-size")), (Player)player));
						}
						meta.setLore(lore);
						clone.setItemMeta(meta);
					}else {
						meta.setDisplayName(Lang.Get("bag-ownerless-unused"));
						List<String> lore = new ArrayList<String>();
						for (String l : Lang.lang.GetStringList("bag-lore")) {
							if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, (Player)player));
						}
						for (String l : Lang.lang.GetStringList("bag-size")) {
							if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, NBT.GetInt(clone, "bag-size")), (Player)player));
						}
						meta.setLore(lore);
						clone.setItemMeta(meta);
					}
					
					Log.Debug(Main.plugin, "Giving cloned bag a new id");
					NBT.SetString(clone, "bag-uuid", UUID.randomUUID().toString());
					NBT.SetString(clone, "bag-owner", "null");

					Log.Debug(Main.plugin, "Splitting bags apart");
					item.setAmount(item.getAmount() - 1);
					player.getInventory().addItem(clone);
				}
			}
        }
	}
}
