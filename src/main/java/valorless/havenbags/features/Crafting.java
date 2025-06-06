package valorless.havenbags.features;

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
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.utils.Utils;

@Deprecated
public class Crafting implements Listener {

	public static Config config = new Config(Main.plugin, "recipes.yml");
	public static List<NamespacedKey> Recipes = new ArrayList<NamespacedKey>();

	public static void init() {
		Log.Debug(Main.plugin, "[DI-15] Registering Crafting");
		Bukkit.getServer().getPluginManager().registerEvents(new Crafting(), Main.plugin);
		PrepareRecipes();
	}
	
	public static void PrepareRecipes() {
		if(config.GetBool("enabled") == false) return;
		//Log.Error(HavenBags.plugin, config.Get("recipes").toString());
		Object[] recipes = config.GetConfigurationSection("recipes").getKeys(false).toArray();
		//Log.Error(HavenBags.plugin, String.valueOf(recipes.length));
		
		for(Object recipe : recipes) {
			if(config.GetBool("recipes." + recipe + ".enabled")) {
				NamespacedKey key = new NamespacedKey(Main.plugin, String.valueOf(recipe));
				ItemStack result = PrepareResult(String.valueOf(recipe));
				
				ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
				List<String> shape = config.GetStringList("recipes." + recipe + ".recipe");
				for(String s : shape) { s = s.replace("X", " "); }
				shapedRecipe.shape(shape.get(0), shape.get(1), shape.get(2));
							
				SetIngredients(String.valueOf(recipe), shapedRecipe);
				
				Permission perm = new Permission(config.GetString("recipes." + recipe + ".permission"));
				if(Bukkit.getPluginManager().getPermission(config.GetString("recipes." + recipe + ".permission")) == null) {
					Bukkit.getPluginManager().addPermission(perm);
				}
				Recipes.add(key);
				Bukkit.addRecipe(shapedRecipe);
				Log.Info(Main.plugin, String.format("Recipe '%s' added.", key.toString()));
			}
		}
	}
	
	static ItemStack PrepareResult(String recipe) {
		ItemStack bagItem = new ItemStack(Material.AIR);
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		
		String bagTexture = "";
		if(!Utils.IsStringNullOrEmpty(config.GetString("recipes." + recipe + ".bag-texture"))) {
			bagTexture = config.GetString("recipes." + recipe + ".bag-texture");
		}else {
			bagTexture = Main.config.GetString("bag-texture");
		}
		int size = Utils.Clamp(config.GetInt("recipes." + recipe + ".bag-size"), 1, 6);
		if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
			bagItem = HeadCreator.itemFromBase64(bagTexture);
		} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
		} else {
			Log.Error(Main.plugin, Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
			return null;
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
        	lore.add(Lang.Parse(l, null));
        }
		//lore.add(Lang.Get("bag-size", size*9));
        placeholders.add(new Placeholder("%size%", size*9));
        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders));
		//for (String l : Lang.lang.GetStringList("bag-size")) {
		//	lore.add(Lang.Parse(String.format(l, size*9)));
		//}
		bagMeta.setLore(lore);
		bagItem.setItemMeta(bagMeta);
		//NBT.SetString(bagItem, "bag-uuid", "null");
		//NBT.SetString(bagItem, "bag-owner", "null");
		NBT.SetInt(bagItem, "bag-size", size*9);
		if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("bound")) {
			NBT.SetBool(bagItem, "bag-canBind", true);
		} else if(config.GetString("recipes." + recipe + ".type").equalsIgnoreCase("ownerless")) {
			NBT.SetBool(bagItem, "bag-canBind", false);
		}
		return bagItem;
	}
	
	static void SetIngredients(String recipe, ShapedRecipe shapedRecipe) {
		Object[] ingredients = config.GetConfigurationSection("recipes." + recipe + ".ingredients").getKeys(false).toArray();
		for(Object ingredient : ingredients) {
			String letter = String.valueOf(ingredient);
			Material material = Material.getMaterial(config.GetString("recipes." + recipe + ".ingredients." + ingredient + ".material"));
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
				shapedRecipe.setIngredient(letter.charAt(0), new RecipeChoice.ExactChoice(item));
			}else {
				shapedRecipe.setIngredient(letter.charAt(0), material);
			}
		}
	}
	
	public static void RemoveRecipes() {
		if(Recipes.size() != 0) {
    		for(NamespacedKey recipe : Recipes) {
    			Bukkit.removeRecipe(recipe);
    		}
    		Recipes.clear();
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
			Log.Debug(Main.plugin, "[DI-88] " + r.getKey().toString());
			
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
		if(config.GetBool("enabled") == false) return;
		Log.Debug(Main.plugin, "[DI-89] " + event.getInventory().getType().toString());
		Log.Debug(Main.plugin, "[DI-90] " + event.getRecipe().toString());
		try {
			//if(event.getInventory().getType() != InventoryType.WORKBENCH) return;
			ShapedRecipe r = (ShapedRecipe)event.getRecipe();
			if(r == null) return;
			if(r.getKey() == null) return;
			//String recipe = r.getKey().getKey();
			if(!Recipes.contains(r.getKey())) return;
			//if(!event.isLeftClick()) event.setCancelled(true);
			if(event.isShiftClick()) { 
				event.setCancelled(true);
				return;
			}
			ItemStack item = event.getInventory().getResult();
			NBT.SetString(item, "bag-uuid", "null");
			NBT.SetString(item, "bag-owner", "null");
			if(item.getItemMeta() != null) {
				if(NBT.Has(item, "bag-uuid")) {
					NBT.SetString(event.getInventory().getResult(), "bag-uuid", UUID.randomUUID().toString());
				}
			}
		} catch(Exception e) {}
		
	}
}
