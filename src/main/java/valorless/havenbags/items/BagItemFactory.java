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
import valorless.havenbags.datamodels.Bag;
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
    public static ItemStack toItemStack(@NotNull Bag bag) {
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
        	displayName = bag.getOwner().equalsIgnoreCase("ownerless") ? Lang.get("bag-ownerless-used") : Lang.get("bag-bound-name", owner);
        }else {
        	displayName = bag.getOwner().equalsIgnoreCase("ownerless") ? Lang.get("bag-ownerless-unused") : Lang.get("bag-unbound-name");
        }
        meta.setDisplayName(displayName);

        // Let HavenBags manage lore and dynamic fields; avoid setting lore here.
        base.setItemMeta(meta);

        // Tag with our PDC helper for runtime identification
        PDC.setString(base, "uuid", bag.getUuid()); // Only uuid, the rest is automatically resolved.
        
        HavenBags.updatePDC(base, bag);
        HavenBags.updateBagItem(base, owner);

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

    /**
     * Creates a new bag item with the specified properties.
     * - If binding is true, the bag is considered bound to a player; otherwise it's ownerless.
     * - The size determines the capacity and may affect the texture/model used.
     * - Player parameter is used for placeholder parsing in lore and may influence texture/model selection if configured.
     *
     * @param binding Whether the bag should be bound to a player
     * @param size The size/capacity of the bag (e.g., 9, 18, 27, etc.)
     * @param player Optional player context for placeholder parsing placeholders.
     * @return A new ItemStack representing the bag with the specified properties
     */
    @SuppressWarnings("deprecation")
    public static ItemStack createBagItem(boolean binding, int size, @Nullable Player player){
        List<Placeholder> placeholders = new ArrayList<Placeholder>(); // Old and stupid
        String bagTexture = Main.config.getString("bag.texture");
        ItemStack bagItem = new ItemStack(Material.AIR);

        if(Main.config.getString("bag.material").startsWith("nexo:")){
            String nexoId = Main.config.getString("bag.material").substring(5);
            bagItem = NexoItems.exists(nexoId) ? NexoItems.itemFromId(nexoId).build() : new ItemStack(Material.PLAYER_HEAD);
        }else {
            if (Main.config.getString("bag.type").equalsIgnoreCase("HEAD")) {
                if (Main.config.getBool("bag-textures.enabled")) {
                    for (int s = 9; s <= 54; s += 9) {
                        if (size == s) {
                            bagItem = HeadCreator.itemFromBase64(binding ?
                                    Main.config.getString("bag-textures.size-" + size) :
                                    Main.config.getString("bag-textures.size-ownerless-" + size));
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
                        bagMeta.setCustomModelData(binding ?
                                Main.config.getInt("bag-custom-model-datas.size-" + size) :
                                Main.config.getInt("bag-custom-model-datas.size-ownerless-" + size));
                    }
                }
            }
        }

        bagMeta.setDisplayName(binding ? Lang.get("bag-unbound-name") : Lang.get("bag-ownerless-unused"));
        List<String> lore = new ArrayList<String>();
        for (String l : Lang.lang.getStringList("bag-lore")) {
            if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.parse(l, player));
        }
        placeholders.add(new Placeholder("%size%", size));
        lore.add(Lang.parse(Lang.get("bag-size"), placeholders, player));
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
                }
            }
        }

        if(!Utils.IsStringNullOrEmpty(Main.config.getString("bag.itemmodel"))) {
            ItemUtils.SetItemModel(bagItem, Main.config.getString("bag.itemmodel"));
        }

        if(!HavenBags.isPowerOfNine(size)) {
            // Cannot upgrade non-9 bags.
            PDC.setBoolean(bagItem, "upgrade", false);
        }

        PDC.setString(bagItem, "uuid", "null");
        PDC.setString(bagItem, "owner", "null");
        PDC.setinteger(bagItem, "size", size);
        PDC.setBoolean(bagItem, "binding", binding);
        return bagItem;
    }
}