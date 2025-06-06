package valorless.havenbags.mods;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A utility class to create a simplified preview of a list of {@link ItemStack}s,
 * typically for visual representation or storage without the full complexity of Bukkit's ItemStack.
 */
public class HavenBagsPreview {

    /** A list containing simplified data representations of the original ItemStacks. */
    public List<ItemData> items;

    /**
     * Constructs a new {@code HavenBagsPreview} from a list of {@link ItemStack}s.
     * Each valid item (not null or AIR) is converted to an {@link ItemData} and added to the preview list.
     *
     * @param itemStacks The list of ItemStacks to convert into a preview format.
     */
    public HavenBagsPreview(List<ItemStack> itemStacks) {
        this.items = new ArrayList<>();

        for (int i = 0; i < itemStacks.size(); i++) {
            addItem(itemStacks.get(i), i);
        }
    }

    /**
     * Converts an {@link ItemStack} to an {@link ItemData} object and adds it to the {@code items} list.
     *
     * @param itemStack The item to convert.
     * @param i The slot index of the item in the original list.
     */
    private void addItem(ItemStack itemStack, int i) {
        // Ignore empty or null items
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        ItemData itemData = new ItemData();

        // Store basic item properties
        itemData.i = itemStack.getType().toString(); // Material name
        itemData.c = itemStack.getAmount();          // Count
        itemData.s = i;                              // Slot index

        // Check for item metadata
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {

            // If the item is damageable, store its damage value
            if (meta instanceof Damageable damageable) {
                if (damageable.getDamage() > 0) itemData.d = damageable.getDamage();
            }

            // If the item has enchantments, flag it
            if (!itemStack.getEnchantments().isEmpty()) {
                itemData.e = true;
            }

            // Store custom model data if present
            if (meta.hasCustomModelData()) {
                itemData.m = meta.getCustomModelData();
            }
        }

        this.items.add(itemData);
    }

    /**
     * A simplified data structure representing a Minecraft item for preview purposes.
     * This inner class holds basic properties needed to represent the item visually or save lightweight data.
     */
    @SuppressWarnings("unused")
    public static class ItemData {
        /** Slot index of the item. */
        private int s;

        /** Material name (as a string). */
        private String i;

        /** Item Count. */
        private int c;

        /** Damage value, if applicable. */
        private int d;

        /** Whether the item is enchanted. */
        private boolean e;

        /** Custom model data, if any. */
        private int m;
    }
}
