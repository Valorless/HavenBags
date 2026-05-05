package valorless.havenbags.items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.Nullable;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.annotations.NotNull;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.utils.Utils;

import static valorless.havenbags.gui.AdminGUI.modifyMaxStack;

/**
 * Factory for creating the in-inventory representation of a Haven Bag.
 */
public final class BagItemFactory {

    /**
     * Create an ItemStack representing the given bag data.
     * - Uses Data.material if set; otherwise uses a player head with Data.texture if present; otherwise defaults to CHEST.
     * - Applies name, custom model data, and a concise lore.
     * - Stores identifying information in the PersistentDataContainer for later retrieval.
     *
     * @param bag Data instance
     * @return ItemStack representing the bag
     */
    public static ItemStack toItemStack(@NotNull Data bag) {
        ItemStack base;
        if (bag.getMaterial() != null) {
            base = new ItemStack(bag.getMaterial());
        } else if (bag.getTexture() != null && !bag.getTexture().isEmpty()) {
            base = HeadCreator.itemFromBase64(bag.getTexture());
        } else {
            base = new ItemStack(Material.CHEST); // Fallback material
        }
        
        OfflinePlayer owner = resolveOwner(bag.getOwner());

        ItemMeta meta = base.getItemMeta();

        // Display name
        String displayName = "";
        if(bag.getName() != null && !bag.getName().isEmpty() && owner != null) {
			displayName = bag.getName();
		}
        else if(!bag.getUuid().equalsIgnoreCase("null")) {
        	displayName = bag.getOwner().equalsIgnoreCase("ownerless") ? Lang.Get("bag-ownerless-used") : Lang.Get("bag-bound-name", owner);
        }else {
        	displayName = bag.getOwner().equalsIgnoreCase("ownerless") ? Lang.Get("bag-ownerless-unused") : Lang.Get("bag-unbound-name");
        }
        meta.setDisplayName(displayName);

        // Let HavenBags manage lore and dynamic fields; avoid setting lore here.
        base.setItemMeta(meta);

        // Tag with our PDC helper for runtime identification
        PDC.SetString(base, "uuid", bag.getUuid()); // Only uuid, the rest is automatically resolved.
        
        HavenBags.UpdatePDC(base, bag);
        HavenBags.UpdateBagItem(base, owner);

        return base;
    }

    private static OfflinePlayer resolveOwner(String owner) {
        try {
            return Bukkit.getOfflinePlayer(UUID.fromString(owner));
        } catch (Exception e) {
            // ownerless or invalid;
            return null;
        }
    }

    public static ItemStack createBagItem(boolean binding, int size, @Nullable Player player){

        List<Placeholder> placeholders = new ArrayList<Placeholder>();
        String bagTexture = Main.config.getString("bag.texture");
        ItemStack bagItem = new ItemStack(Material.AIR);

        if(Main.config.getString("bag.type.material").startsWith("nexo:")){
            String nexoId = Main.config.getString("bag.type.material").substring(5);
            bagItem = NexoItems.exists(nexoId) ? NexoItems.itemFromId(nexoId).build() : new ItemStack(Material.PLAYER_HEAD);
        }else {
            if (Main.config.getString("bag.type").equalsIgnoreCase("HEAD")) {
                if (Main.config.getBool("bag-textures.enabled")) {
                    for (int s = 9; s <= 54; s += 9) {
                        if (size == s) {
                            bagItem = HeadCreator.itemFromBase64(Main.config.getString("bag-textures.size-" + size));
                        }
                    }
                } else {
                    bagItem = HeadCreator.itemFromBase64(bagTexture);
                }
            } else if (Main.config.getString("bag.type").equalsIgnoreCase("ITEM")) {
                bagItem = new ItemStack(Main.config.getMaterial("bag.material"));
            } else {
                Log.error(Main.plugin, "Invalid bag type in config: " + Main.config.getString("bag.type"));
                bagItem = new ItemStack(Material.PLAYER_HEAD);
            }
        }
        ItemMeta bagMeta = bagItem.getItemMeta();
        if(Main.config.getInt("bag.modeldata") != 0 && Main.config.getString("bag.type").equalsIgnoreCase("ITEM")) {
            bagMeta.setCustomModelData(Main.config.getInt("bag.modeldata"));
            if(Main.config.getBool("bag-custom-model-datas.enabled")) {
                for(int s = 9; s <= 54; s += 9) {
                    if(size == s) {
                        bagMeta.setCustomModelData(Main.config.getInt("bag-custom-model-datas.size-" + size));
                    }
                }
            }
        }

        bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
        List<String> lore = new ArrayList<String>();
        for (String l : Lang.lang.getStringList("bag-lore")) {
            if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
        }
        placeholders.add(new Placeholder("%size%", size));
        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
        //for (String l : Lang.lang.GetStringList("bag-size")) {
        //	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size), player));
        //}
        bagMeta.setLore(lore);

        if(Server.VersionHigherOrEqualTo(Server.Version.v1_21_3)) {
            bagMeta.setTooltipStyle(NamespacedKey.fromString(Main.config.getString("bag.tooltip-style")));
        }

        bagItem.setItemMeta(bagMeta);

        modifyMaxStack(bagItem, 1);

        if(Main.config.getBool("bag-custom-model-datas.enabled")) {
            for(int s = 9; s <= 54; s += 9) {
                if(size == s) {
                    if(!Utils.IsStringNullOrEmpty(Main.config.getString("bag-custom-model-datas.size-" + size)) &&
                            !Main.config.getString("bag-custom-model-datas.size-" + size).matches("-?\\d+(\\.\\d+)?")) {
                        ItemUtils.SetItemModel(bagItem, Main.config.getString("bag-custom-model-datas.size-" + size));
                    }
                    //bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + size));
                }
            }
        }

        if(!Utils.IsStringNullOrEmpty(Main.config.getString("bag.itemmodel"))) {
            ItemUtils.SetItemModel(bagItem, Main.config.getString("bag.itemmodel"));
        }

        //Log.Warning(plugin, bagItem.toString());
        //PDC.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
        PDC.SetString(bagItem, "uuid", "null");
        PDC.SetString(bagItem, "owner", "null");
        PDC.SetInteger(bagItem, "size", size);
        PDC.SetBoolean(bagItem, "binding", binding);
        return bagItem;
    }
}