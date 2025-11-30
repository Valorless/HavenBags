package valorless.havenbags.items;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import valorless.havenbags.datamodels.Data;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.annotations.NotNull;

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
}