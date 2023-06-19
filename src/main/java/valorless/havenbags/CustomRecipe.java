package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permission;

import valorless.valorlessutils.ValorlessUtils.Utils;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;

public class CustomRecipe implements Listener {
	
	public static Config config = new Config(HavenBags.plugin, "recipes.yml");
	public static List<NamespacedKey> Recipes = new ArrayList<NamespacedKey>();
	
	public static void PrepareRecipes() {
		if(config.GetBool("enabled") == false) return;
		//Log.Error(HavenBags.plugin, config.Get("recipes").toString());
		Object[] recipes = config.GetConfigurationSection("recipes").getKeys(false).toArray();
		//Log.Error(HavenBags.plugin, String.valueOf(recipes.length));
		
		for(Object recipe : recipes) {
			if(config.GetBool("recipes." + recipe + ".enabled")) {
				NamespacedKey key = new NamespacedKey(HavenBags.plugin, String.valueOf(recipe));
				//Log.Warning(HavenBags.plugin, String.valueOf(recipe));
				
				String bagTexture = "";
				if(!Utils.IsStringNullOrEmpty(config.GetString("recipes." + recipe + ".bag-texture"))) {
					bagTexture = config.GetString("recipes." + recipe + ".bag-texture");
				}else {
					bagTexture = HavenBags.config.GetString("bag-texture");
				}
				int size = config.GetInt("recipes." + recipe + ".bag-size");
				ItemStack bagItem = SkullCreator.itemFromBase64(bagTexture);
				SkullMeta bagMeta = (SkullMeta)bagItem.getItemMeta();
				if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("bound")) {
					bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
				} else if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("ownerless")) {
					bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
				}
				List<String> lore = new ArrayList<String>();
				lore.add(Lang.Get("bag-size", size*9));
				bagMeta.setLore(lore);
				bagItem.setItemMeta(bagMeta);
				NBT.SetString(bagItem, "bag-uuid", "null");
				NBT.SetString(bagItem, "bag-owner", "null");
				NBT.SetInt(bagItem, "bag-size", size*9);
				if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("bound")) {
					NBT.SetBool(bagItem, "bag-canBind", true);
				} else if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("ownerless")) {
					NBT.SetBool(bagItem, "bag-canBind", false);
				}
				
				ShapedRecipe r = new ShapedRecipe(key, bagItem);
				List<String> shape = config.GetStringList("recipes." + recipe + ".recipe");
				for(String s : shape) { s = s.replace("X", " "); }
				r.shape(shape.get(0), shape.get(1), shape.get(2));
							
				Object[] ingredients = config.GetConfigurationSection("recipes." + recipe + ".ingredients").getKeys(false).toArray();
				for(Object ingredient : ingredients) {
					String letter = String.valueOf(ingredient);
					Material material = Material.getMaterial(config.GetString("recipes." + recipe + ".ingredients." + ingredient + ".material"));
					//Log.Error(HavenBags.plugin, "" + letter.charAt(0));
					r.setIngredient(letter.charAt(0), material);
				}
				
				Permission perm = new Permission(config.GetString("recipes." + recipe + ".permission"));
				Bukkit.getPluginManager().addPermission(perm);
				Recipes.add(key);
				Bukkit.addRecipe(r);
			}
		}
	}
	
	public static void RemoveRecipes() {
		if(CustomRecipe.Recipes.size() != 0) {
    		for(NamespacedKey recipe : CustomRecipe.Recipes) {
    			Bukkit.removeRecipe(recipe);
    		}
    	}
	}
	
	@EventHandler
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		if(config.GetBool("enabled") == false) return;
		ShapedRecipe r = (ShapedRecipe)event.getRecipe();
		if(r == null) return;
		if(r.getKey() == null) return;
		String recipe = r.getKey().getKey();
		if(!Recipes.contains(r.getKey())) return;
		
		for(HumanEntity player : event.getViewers()) {
			//Log.Error(HavenBags.plugin, config.GetString("recipes." + recipe + ".permission"));
			if(!player.hasPermission(config.GetString("recipes." + recipe + ".permission"))) {
				event.getInventory().setResult(null);
			}
		}
	}

}
