package valorless.havenbags.persistentdatacontainer;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import valorless.havenbags.Main;
import valorless.valorlessutils.tags.Tags;
import valorless.valorlessutils.utils.Utils;

public class PDC {
	
	public static Boolean Has(ItemStack item, String key) {
	    //if (!item.hasItemMeta()) return false;
	    try {
	    	return Tags.Has(Main.plugin, item, key);
	    } catch (Exception e) {
	    	//e.printStackTrace();
	    	return false;
	    }
	}
	
	public static Boolean Remove(ItemStack item, String key) {
	    if (!item.hasItemMeta()) return false;
        try {
        	ItemMeta meta = item.getItemMeta();
        	PersistentDataContainer container = meta.getPersistentDataContainer();
            container.remove(new NamespacedKey(Main.plugin, key));
            item.setItemMeta(meta);
        } catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }
        return true;
    }

    // ===== STRING =====
    public static void SetString(ItemStack item, String key, String value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetString(Main.plugin, item, key, value);
    }

    public static String GetString(ItemStack item, String key) {
        return Tags.GetString(Main.plugin, item, key);
    }

    public static void SetStringList(ItemStack item, String key, List<String> value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetStringList(Main.plugin, item, key, value);
    }

    public static List<String> GetStringList(ItemStack item, String key) {
    	List<String> value = Tags.GetStringList(Main.plugin, item, key);
        return value == null ? new ArrayList<String>() : value;
    }

    // ===== INTEGER =====
    public static void SetInteger(ItemStack item, String key, Integer value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetInteger(Main.plugin, item, key, value);
    }

    public static Integer GetInteger(ItemStack item, String key) {
        return Tags.GetInteger(Main.plugin, item, key);
    }

    public static void SetIntegerList(ItemStack item, String key, List<Integer> value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetIntegerList(Main.plugin, item, key, value);
    }

    public static List<Integer> GetIntegerList(ItemStack item, String key) {
    	List<Integer> value = Tags.GetIntegerList(Main.plugin, item, key);
        return value == null ? new ArrayList<Integer>() : value;
    }

    // ===== DOUBLE =====
    public static void SetDouble(ItemStack item, String key, Double value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetDouble(Main.plugin, item, key, value);
    }

    public static Double GetDouble(ItemStack item, String key) {
        return Tags.GetDouble(Main.plugin, item, key);
    }

    public static void SetDoubleList(ItemStack item, String key, List<Double> value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetDoubleList(Main.plugin, item, key, value);
    }

    public static List<Double> GetDoubleList(ItemStack item, String key) {
    	List<Double> value = Tags.GetDoubleList(Main.plugin, item, key);
        return value == null ? new ArrayList<Double>() : value;
    }

    // ===== FLOAT =====
    public static void SetFloat(ItemStack item, String key, Float value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetFloat(Main.plugin, item, key, value);
    }

    public static Float GetFloat(ItemStack item, String key) {
        return Tags.GetFloat(Main.plugin, item, key);
    }

    public static void SetFloatList(ItemStack item, String key, List<Float> value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
        Tags.SetFloatList(Main.plugin, item, key, value);
    }

    public static List<Float> GetFloatList(ItemStack item, String key) {
    	List<Float> value = Tags.GetFloatList(Main.plugin, item, key);
        return value == null ? new ArrayList<Float>() : value;
    }

    // ===== BOOLEAN =====
    public static void SetBoolean(ItemStack item, String key, Boolean value) {
    	if(value == null) {
    		Remove(item, key);
    		return;
    	}
    	Tags.SetInteger(Main.plugin, item, key, Utils.Bool.ToValue(value));
    	// Using int to support 1.18, as PersistentDataType.BOOLEAN doesn't exist then.
    }

    public static Boolean GetBoolean(ItemStack item, String key) {
    	try {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            return container.get(new NamespacedKey(Main.plugin, key), PersistentDataType.BYTE) == (byte) 1;
    	}catch(Exception e) {}
    	
    	return (Tags.GetInteger(Main.plugin, item, key) != null) ? 
    			Utils.Bool.FromValue(Tags.GetInteger(Main.plugin, item, key)) : null;
    }

    // ===== UUID =====
	public static void SetUUID(ItemStack item, String key, UUID uuid) {
    	if(uuid == null) {
    		Remove(item, key);
    		return;
    	}
		Tags.SetString(Main.plugin, item, key, uuid.toString());
	}
	
	public static UUID GetUUID(ItemStack item, String key) {
	    String value = Tags.GetString(Main.plugin, item, key);
	    if (value == null || value.equalsIgnoreCase("null")) {
	        return null;
	    }
	    return UUID.fromString(value);
	}
	
	
	
	public static byte ConvertBoolean(Boolean bool) {
		if(bool) return (byte) 1;
		else return (byte) 0;
	}
}
