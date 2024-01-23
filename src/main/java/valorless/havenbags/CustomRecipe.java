package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;

public class CustomRecipe implements Listener {
	
	public static Config config = new Config(Main.plugin, "recipes.yml");
	public static List<NamespacedKey> Recipes = new ArrayList<NamespacedKey>();
	
	public static void PrepareRecipes() {
		if(config.GetBool("enabled") == false) return;
		//Log.Error(HavenBags.plugin, config.Get("recipes").toString());
		Object[] recipes = config.GetConfigurationSection("recipes").getKeys(false).toArray();
		//Log.Error(HavenBags.plugin, String.valueOf(recipes.length));
		
		for(Object recipe : recipes) {
			if(config.GetBool("recipes." + recipe + ".enabled")) {
				NamespacedKey key = new NamespacedKey(Main.plugin, String.valueOf(recipe));
				//Log.Warning(HavenBags.plugin, String.valueOf(recipe));

				ItemStack bagItem = new ItemStack(Material.AIR);
				
				String bagTexture = "";
				if(!Utils.IsStringNullOrEmpty(config.GetString("recipes." + recipe + ".bag-texture"))) {
					bagTexture = config.GetString("recipes." + recipe + ".bag-texture");
				}else {
					bagTexture = Main.config.GetString("bag-texture");
				}
				int size = config.GetInt("recipes." + recipe + ".bag-size");
				if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
					bagItem = SkullCreator.itemFromBase64(bagTexture);
				} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
					bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
				} else {
					Log.Error(Main.plugin, Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
					return;
				}
				ItemMeta bagMeta = bagItem.getItemMeta();
				if(config.GetInt("recipes." + recipe + ".custom-model-data") != 0) {
					bagMeta.setCustomModelData(config.GetInt("recipes." + recipe + ".custom-model-data"));
				}
				if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("bound")) {
					bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
				} else if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("ownerless")) {
					bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
				}
				List<String> lore = new ArrayList<String>();
		        for (String l : Lang.lang.GetStringList("bag-lore")) {
		        	lore.add(Lang.Parse(l));
		        }
				//lore.add(Lang.Get("bag-size", size*9));
				for (String l : Lang.lang.GetStringList("bag-size")) {
					lore.add(Lang.Parse(String.format(l, size*9)));
				}
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
					Integer custommodeldata = config.GetInt("recipes." + recipe + ".ingredients." + ingredient + ".custom-model-data");
					String name = config.GetString("recipes." + recipe + ".ingredients." + ingredient + ".name");
					ItemStack item = new ItemStack(material);
					if(custommodeldata != 0) {
						ItemMeta meta = item.getItemMeta();
						meta.setCustomModelData(custommodeldata);
						item.setItemMeta(meta);
					}
					if(!Utils.IsStringNullOrEmpty(name)) {
						ItemMeta meta = item.getItemMeta();
						meta.setDisplayName(name);
						item.setItemMeta(meta);
					}
					if(item.hasItemMeta()) {
						r.setIngredient(letter.charAt(0), new RecipeChoice.ExactChoice(item));
					}else {
						r.setIngredient(letter.charAt(0), material);
					}
				}
				
				Permission perm = new Permission(config.GetString("recipes." + recipe + ".permission"));
				if(Bukkit.getPluginManager().getPermission(config.GetString("recipes." + recipe + ".permission")) == null) {
					Bukkit.getPluginManager().addPermission(perm);
				}
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
		//if(event.getRecipe() == null) return;
		//if(event.getRecipe().getClass() != ShapedRecipe.class) return;
		try {
			//if(event.getInventory().getType() != InventoryType.WORKBENCH) return;
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
		} catch(Exception e) {}
	}
	
	@EventHandler
	public void onCraftItem (CraftItemEvent event) {
		Log.Debug(Main.plugin, event.getInventory().getType().toString());
		Log.Debug(Main.plugin, event.getRecipe().toString());
		try {
			//if(event.getInventory().getType() != InventoryType.WORKBENCH) return;
			ShapedRecipe r = (ShapedRecipe)event.getRecipe();
			if(r == null) return;
			if(r.getKey() == null) return;
			//String recipe = r.getKey().getKey();
			if(!Recipes.contains(r.getKey())) return;
			//if(!event.isLeftClick()) event.setCancelled(true);
			if(event.isShiftClick()) event.setCancelled(true);
			ItemStack item = event.getCurrentItem();
			if(item.getItemMeta() != null) {
				if(NBT.Has(item, "bag-uuid")) {
					NBT.SetString(event.getInventory().getResult(), "bag-uuid", UUID.randomUUID().toString());
				}
			}
		} catch(Exception e) {}
		
	}

}