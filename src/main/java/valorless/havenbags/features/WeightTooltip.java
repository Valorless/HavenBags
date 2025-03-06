package valorless.havenbags.features;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Data;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;

public class WeightTooltip {
    public static void registerTooltipListener(Plugin plugin) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, 
        		PacketType.Play.Server.SET_SLOT, 
        		PacketType.Play.Server.WINDOW_ITEMS
        		) {
            @Override
            public void onPacketSending(PacketEvent event) {
            	//Log.Error(Main.plugin, event.getPacketType().toString());
            	
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    modifyItemTooltip(event);
                }
                else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                    modifyInventoryTooltips(event);
                }
            }
        });
    }

    private static void modifyItemTooltip(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        ItemStack item = packet.getItemModifier().read(0);
        if (item != null && item.getType() != Material.AIR) {
        	if(!event.getPlayer().getOpenInventory().getTopInventory().contains(item)) return;
            item = addWeightTooltip(item, event.getPlayer());
            packet.getItemModifier().write(0, item);
        }
    }

    private static void modifyInventoryTooltips(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        List<ItemStack> items = packet.getItemListModifier().read(0);
        int size = items.size();
        for(Data bag : BagData.GetOpenBags()) {
    		if(bag.getViewer() == event.getPlayer() && bag.getGui() != null) size = bag.getGui().size;
    	}
        for (int i = 0; i < size; i++) {
        	ItemStack item = items.get(i);
            if (item != null && item.getType() != Material.AIR) {
                items.set(i, addWeightTooltip(item, event.getPlayer()));
            }
        }
        packet.getItemListModifier().write(0, items);
    }

    private static ItemStack addWeightTooltip(ItemStack item, Player player) {
    	if(!Main.weight.GetBool("enabled")) return item;
    	boolean show = false;
    	for(Data bag : BagData.GetOpenBags()) {
    		if(bag.getViewer() == player) show = true;
    	}
    	if(!show) return item;
    	if(HavenBags.IsBag(item)) return item;
        //double weight = HavenBags.ItemWeight(item);
        DecimalFormat df = new DecimalFormat("#.00");
        double weight = Double.parseDouble(df.format(HavenBags.ItemWeight(item)));
        if(weight == 0.0) return item;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        String line = Lang.Parse(Main.weight.GetString("weight-tooltip"), player);
        lore.add(line.replace("%weight%", String.valueOf(weight)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}