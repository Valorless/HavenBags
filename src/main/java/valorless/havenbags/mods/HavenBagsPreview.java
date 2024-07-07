package valorless.havenbags.mods;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class HavenBagsPreview {
    public List<ItemData> items;

    public HavenBagsPreview(List<ItemStack> itemStacks) {
        this.items = new ArrayList<>();

        for (int i = 0; i < itemStacks.size(); i++) {
            addItem(itemStacks.get(i), i);
        }
    }

    private void addItem(ItemStack itemStack, int i) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        ItemData itemData = new ItemData();

        itemData.i = itemStack.getType().toString();
        itemData.c = itemStack.getAmount();
        itemData.s = i;

        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            if (meta instanceof Damageable) {
                Damageable damageable = ((Damageable) meta);
                if (damageable.getDamage() > 0) itemData.d = damageable.getDamage();
            }

            if (!itemStack.getEnchantments().isEmpty()) {
                itemData.e = true;
            }

            if (meta.hasCustomModelData()) {
                itemData.m = meta.getCustomModelData();
            }
        }

        this.items.add(itemData);
    }

    // Inner class to represent item data
    @SuppressWarnings("unused")
    public static class ItemData {
        /** slot */
		private int s;
        /** itemName */
        private String i;
        /** count */
        private int c;
        /** damage */
        private int d;
        /** enchanted */
        private boolean e;
        /** modelData */
        private int m;
    }
}
