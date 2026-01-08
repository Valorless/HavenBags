package valorless.havenbags.hooks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.MarkedForRemoval;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.Base64Validator;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.utils.Utils;

@MarkedForRemoval("Originally part of HavenBags API, now deprecated in favor of direct usage of the HavenBagsAPI.")
@Deprecated(since = "1.39.0", forRemoval = true)
public class CustomRecipes {
	
	public class BagInfo {
	    private Material mat = Material.PLAYER_HEAD;
	    private int size = 1;
	    private boolean canBind = true;
	    private Player player = null;
	    private int customModelData = 0; 
	    private String texture = null;

	    /**
	     * Default constructor.
	     * Initializes a new BagInfo object with default values: mat as Material.PLAYER_HEAD, 
	     * size as 1, canBind as true, player as null, and customModelData as 0.
	     */
	    public BagInfo() {}

	    /**
	     * Parameterized constructor.
	     * Initializes a BagInfo object with specified player, material, size, and binding capability.
	     *
	     * @param player the Player object, can be null
	     * @param material the Material type for the bag
	     * @param size the size of the bag (clamped between 1 and 6)
	     * @param bind true if the bag can be bound, false otherwise
	     * @param texture the head texture (base64), can be null
	     */
	    public BagInfo(Player player, @NotNull Material material, @NotNull int size, @NotNull boolean bind, String texture) {
	        this.setPlayer(player);
	        this.setMaterial(material);
	        this.setSize(size);
	        this.setCanBind(bind);
	        try {
				this.setTexture(texture);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }

	    /**
	     * Getter for material.
	     * @return the Material assigned to the bag.
	     */
	    public Material getMaterial() {
	        return mat;
	    }

	    /**
	     * Setter for material.
	     * Sets the Material for the bag.
	     * @param material the Material to set for the bag.
	     */
	    public void setMaterial(Material material) {
	        this.mat = material;
	    }

	    /**
	     * Getter for size.
	     * @return the current size of the bag.
	     */
	    public int getSize() {
	        return size;
	    }

	    /**
	     * Setter for size.
	     * Sets the size of the bag. The value is clamped between 1 and 6.
	     * @param size the size to set for the bag.
	     */
	    public void setSize(int size) {
	        this.size = Utils.Clamp(size, 1, 6);
	    }

	    /**
	     * Getter for canBind.
	     * @return true if the bag can be bound, false otherwise.
	     */
	    public boolean isCanBind() {
	        return canBind;
	    }

	    /**
	     * Setter for canBind.
	     * Sets whether the bag can be bound.
	     * @param canBind true if the bag can be bound, false otherwise.
	     */
	    public void setCanBind(boolean canBind) {
	        this.canBind = canBind;
	    }

	    /**
	     * Getter for player.
	     * @return the Player object associated with the bag, can be null.
	     */
	    public Player getPlayer() {
	        return player;
	    }

	    /**
	     * Setter for player.
	     * Sets the Player object for placeholder parsing, can be null.
	     * @param player the Player to associate with the bag.
	     */
	    public void setPlayer(Player player) {
	        this.player = player;
	    }

	    /**
	     * Getter for custom model data.
	     * @return the custom model data value.
	     */
	    public int getModelData() {
	        return customModelData;
	    }

	    /**
	     * Setter for custom model data.
	     * Sets the custom model data value.
	     * @param modelData the custom model data to set.
	     */
	    public void setModelData(int modelData) {
	        this.customModelData = modelData;
	    }

		public String getTexture() {
			return texture;
		}

		public void setTexture(String texture) throws Exception {
			if(Base64Validator.isValidBase64(texture)) {
				this.texture = texture;
			}else {
				throw new Exception("Recipe texture is not a valid Base64!");
			}
		}
	}

	/**
	 * Creates an ItemStack representing a custom bag based on the given BagInfo template.
	 * The bag's material, size, model data, display name, and lore are configured based on the template and config settings.
	 *
	 * @param template The BagInfo object containing the template information for creating the bag.
	 * @return An ItemStack representing the created bag.
	 */
	public static ItemStack CreateBag(BagInfo template) {
	    // Create a new ItemStack with the material from the template
	    ItemStack bag = new ItemStack(template.getMaterial());
	    List<Placeholder> placeholders = new ArrayList<Placeholder>();

	    // Calculate the size of the bag in inventory slots (multiples of 9)
	    int size = template.getSize() * 9;

	    // Check the configuration for the type of bag (HEAD or ITEM)
	    if (template.getMaterial() == Material.PLAYER_HEAD) {
	    	if(!Utils.IsStringNullOrEmpty(template.getTexture()) || !template.getTexture().equalsIgnoreCase("none")) {
	    		bag = HeadCreator.itemFromBase64(template.getTexture());
	    	}else {
	    		if (Main.config.GetBool("bag-textures.enabled")) {
	            	// Use specific textures based on the bag size
	            	for (int s = 9; s <= 54; s += 9) {
	            		if (size == s) {
	            			bag = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-" + size));
	            		}
	            	}
	    		} else {
	    			// Use a general texture if specific sizes are not configured
	    			bag = HeadCreator.itemFromBase64(Main.config.GetString("bag-texture"));
	    		}
	    	}
	    } else if (Main.config.GetString("bag.type").equalsIgnoreCase("ITEM")) {
	        // Use a material specified in the config for ITEM type bags
	        bag = new ItemStack(Main.config.GetMaterial("bag.material"));
	    }

	    // Set metadata (custom model data, display name, lore) for the bag
	    ItemMeta bagMeta = bag.getItemMeta();
	    
	    // If the template contains custom model data, apply it
	    if (template.getModelData() == 0) {
	        if (Main.config.GetInt("bag.modeldata") != 0) {
	            bagMeta.setCustomModelData(Main.config.GetInt("bag.modeldata"));
	        }
	        if (Main.config.GetBool("bag-custom-model-datas.enabled")) {
	            // Apply custom model data based on the bag size
	            for (int s = 9; s <= 54; s += 9) {
	                if (size == s) {
	                    bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + size));
	                }
	            }
	        }
	    } else {
	        // Apply the template's custom model data if present
	        bagMeta.setCustomModelData(template.getModelData());
	    }

	    // Set the display name for the bag
	    bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));

	    // Set the lore for the bag by parsing the template and adding placeholders for size
	    List<String> lore = new ArrayList<String>();
	    for (String l : Lang.lang.GetStringList("bag-lore")) {
	        if (!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, template.getPlayer()));
	    }
	    placeholders.add(new Placeholder("%size%", size));
	    lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, template.getPlayer()));

	    // Apply the lore to the bag metadata
	    bagMeta.setLore(lore);
	    bag.setItemMeta(bagMeta);

	    // Set custom NBT data for the bag (UUID, owner, size, and binding capability)
	    PDC.SetString(bag, "uuid", "null");
	    PDC.SetString(bag, "owner", "null");
	    PDC.SetInteger(bag, "size", size);
	    PDC.SetBoolean(bag, "binding", template.isCanBind());

	    // Return the created ItemStack representing the bag
	    return bag;
	}

	
}
