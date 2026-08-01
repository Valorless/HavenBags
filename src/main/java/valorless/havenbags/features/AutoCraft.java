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
        //if(!player.getName().equalsIgnoreCase("alynie")) return;
        condense(bag, player);
        condense(bag, player);
    }

    static void condense(Bag bag, Player player) {
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
                if(amount == 0){
                    item.setType(Material.AIR);
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
                //if(player.getName().equalsIgnoreCase("alynie")) Log.info(Main.plugin, "Crafting result: " + result.getAmount());
                crafted.add(result);
            }else{
                //Failed, put the old back in
                //if(player.getName().equalsIgnoreCase("alynie")) Log.info(Main.plugin, "Failed to craft " + ingredient.getType() + " x9. Returning to inventory.");
                crafted.add(ingredient);
            }
        }

        //AutoSorter.sortInventory(content);
        restackInventory(content);

        for(ItemStack item : crafted) {
            restackInventory(content);
            if(!HavenBags.addItemToInventory(bag.getContent() ,bag.getSize(), item, player)){
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
    }

    static void jrestackInventory(List<ItemStack> content) {
        for(int index = 0; index < content.size()-1; index++){
            ItemStack stack1 = content.get(index);
            if(stack1 == null) continue;
            if(stack1.getType() == Material.AIR) continue;
            int two = index+1;
            ItemStack stack2 = content.get(two);
            while(stack2 == null || stack1.getType() == Material.AIR){
                if(two == content.size()-1) break;
                two++;
                stack2 = content.get(two);
            }
            if(stack1.isSimilar(stack2)){
                int amount = stack1.getAmount() + stack2.getAmount();
                if(stack1.getMaxStackSize() != stack1.getType().getMaxStackSize()) continue;
                if(amount > stack1.getMaxStackSize()){
                    stack1.setAmount(stack1.getMaxStackSize());
                    stack2.setAmount(amount - stack1.getMaxStackSize());
                }else{
                    stack1.setAmount(amount);
                    stack2.setType(Material.AIR);
                }
            }
        }
    }

    static void restackInventory(List<ItemStack> content) {
        for(int index = 0; index < content.size()-1; index++){
            ItemStack stack1 = content.get(index);
            if(stack1 == null) continue;
            if(stack1.getType() == Material.AIR) continue;
            int two = index+1;
            ItemStack stack2 = content.get(two);
            while(stack2 == null || stack2.getType() == Material.AIR || !stack1.isSimilar(stack2)){
                if(two == content.size()-1) break;
                two++;
                stack2 = content.get(two);
            }
            if(stack1.isSimilar(stack2)){
                int amount = stack1.getAmount() + stack2.getAmount();
                if(stack1.getMaxStackSize() != stack1.getType().getMaxStackSize()) continue;
                if(amount > stack1.getMaxStackSize()){
                    stack1.setAmount(stack1.getMaxStackSize());
                    stack2.setAmount(amount - stack1.getMaxStackSize());
                }else{
                    stack1.setAmount(amount);
                    stack2.setType(Material.AIR);
                }
            }
        }
    }
}
