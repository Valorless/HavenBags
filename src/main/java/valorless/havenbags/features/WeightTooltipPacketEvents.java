package valorless.havenbags.features;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.github.retrooper.packetevents.PacketEvents;
/*
import com.github.retrooper.packetevents.event.player.PlayerPacketEvent;
import com.github.retrooper.packetevents.event.player.PlayerPacketListener;
import com.github.retrooper.packetevents.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.packetwrappers.play.server.WrappedPacketWindowItems;
import com.github.retrooper.packetevents.packetwrappers.play.server.WrappedPacketSetSlot;
*/

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.ValorlessUtils.Log;

public class WeightTooltipPacketEvents {
	
	// I don't have a fucking clue what I'm doing here..
	
	/*

    public static void registerTooltipListener(Plugin plugin) {
        Log.Debug(Main.plugin, "[DI-251] Registering WeightTooltip with PacketEvents (Retrooper)");

        PacketEvents.getAPI().getPlayerUtils().registerListener(new PlayerPacketListener() {

            @Override
            public void onPlayerPacket(PlayerPacketEvent event) {
                if (event.getPacketType() == Server.SET_SLOT) {
                    try {
                        modifyItemTooltip(event);
                    } catch (Exception ignored) {}
                } else if (event.getPacketType() == Server.WINDOW_ITEMS) {
                    try {
                        modifyInventoryTooltips(event);
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private static void modifyItemTooltip(PlayerPacketEvent event) {
        WrappedPacketSetSlot packet = new WrappedPacketSetSlot(event.getPacket());
        Player player = event.getPlayer();

        ItemStack item = packet.getItem();
        if (item != null && item.getType() != Material.AIR) {
            // The original code checks if the top inventory contains the item - 
            // you can add a similar check here if needed.

            ItemStack newItem = addWeightTooltip(item, player);
            packet.setItem(newItem);
        }
    }

    private static void modifyInventoryTooltips(PlayerPacketEvent event) {
        WrappedPacketWindowItems packet = new WrappedPacketWindowItems(event.getPacket());
        Player player = event.getPlayer();

        List<ItemStack> items = packet.getItems();
        int size = items.size();

        for (Data bag : BagData.GetOpenBags()) {
            if (bag.getViewer() == player && bag.getGui() != null) size = bag.getGui().size;
        }

        for (int i = 0; i < size; i++) {
            ItemStack item = items.get(i);
            if (item != null && item.getType() != Material.AIR) {
                items.set(i, addWeightTooltip(item, player));
            }
        }

        packet.setItems(items);
    }

    private static ItemStack addWeightTooltip(ItemStack item, Player player) {
        if (!Main.weight.GetBool("enabled")) return item;

        boolean show = false;
        for (Data bag : BagData.GetOpenBags()) {
            if (bag.getViewer() == player) show = true;
        }
        if (!show) return item;
        if (HavenBags.IsBag(item)) return item;

        DecimalFormat df = new DecimalFormat("#.00");
        double weight = Double.parseDouble(df.format(HavenBags.ItemWeight(item)));
        if (weight == 0.0) return item;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        String line = Lang.Parse(Main.weight.GetString("weight-tooltip"), player);
        lore.add(line.replace("%weight%", String.valueOf(weight)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    */
}
