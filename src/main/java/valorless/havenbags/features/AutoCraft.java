package valorless.havenbags.features;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Bag;
import valorless.valorlessutils.logging.Log;

import java.util.*;

public class AutoCraft {

    public static void condenseInventory(Bag bag, Player player) {
        List<ItemStack> content = bag.getContent();
        List<ItemStack> toCraft = new ArrayList<>();
        List<ItemStack> crafted = new ArrayList<>();

        for (ItemStack item : content) {
            if (item != null && item.getType() != Material.AIR) {
                int amount = item.getAmount();
                while(amount >= 9) {
                    ItemStack clone = item.clone();
                    clone.setAmount(9);
                    toCraft.add(clone);
                    amount -= 9;
                    item.setAmount(amount);
                    //Log.info(Main.plugin, "Added " + clone.getType() + " x9 to crafting list. Remaining: " + amount);
                }
                //if(item.getAmount() >= 9) {
                //    ItemStack clone = item.clone();
                //    clone.setAmount(9);
                //    toCraft.add(clone);
                //    int newAmount = item.getAmount() - 9;
                //    item.setAmount(newAmount);
                //}
            }
        }

        Iterator<ItemStack> it = toCraft.iterator();
        while (it.hasNext()) {
            ItemStack ingredient = it.next().clone();
            it.remove();
            List<ItemStack> ingredientsList = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack clone = ingredient.clone();
                clone.setAmount(1);
                ingredientsList.add(clone);
            }

            Recipe recipe = Bukkit.getCraftingRecipe(ingredientsList.toArray(new ItemStack[0]), Bukkit.getWorlds().getFirst());

            if (recipe != null) {
                ItemStack result = recipe.getResult();
                //Log.info(Main.plugin, "Crafting result: " + result.getAmount());
                crafted.add(result);
            }else{
                //Failed, put the old back in
                //Log.info(Main.plugin, "Failed to craft " + ingredient.getType() + " x9. Returning to inventory.");
                crafted.add(ingredient);
            }
        }

        AutoSorter.sortInventory(content);

        for(ItemStack item : crafted) {
            HavenBags.addItemToInventory(bag.getContent() ,bag.getSize(), item, player);
        }

    }
}
