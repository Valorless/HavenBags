package valorless.havenbags.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONObject;

import valorless.havenbags.BagData;

import java.util.Base64;
import java.util.UUID;

public class HeadCreator {

    private HeadCreator() {}

    /**
     * Creates a player skull item.
     *
     * @return The player skull item.
     */
    public static ItemStack createSkull() {
        return new ItemStack(Material.PLAYER_HEAD);
    }

    /**
     * Creates a player skull item with a custom texture from a base64 string.
     *
     * @param base64 The base64 string containing the texture.
     * @return The custom player head item.
     */
    public static ItemStack itemFromBase64(String base64) {
        ItemStack skull = createSkull();
        /*SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta == null) {
        	//Log.Error(Main.plugin, "null");
            return null;
        }
    	//Log.Error(Main.plugin, "skull");

        skull.setItemMeta(skullMeta);

        // Use NBTAPI to set the profile data
        NBTItem nbtItem = new NBTItem(skull);
        NBTCompound skullOwner = nbtItem.addCompound("SkullOwner");

        skullOwner.setString("Id", UUID.randomUUID().toString());
        NBTCompound properties = skullOwner.addCompound("Properties");
        properties.getCompoundList("textures").addCompound().setString("Value", base64);*/
        
        BagData.setTextureValue(skull, base64);
        
        //Log.Error(Main.plugin, skull.toString());
        //Log.Error(Main.plugin, nbtItem.getItem().toString());
        return skull;
    }
    
    public static ItemStack itemFromUuid(UUID id) {
    	ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    	SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
		
		item.setItemMeta(meta);

		return item;
	}
    
    public static String extractUrlFromBase64(String base64Texture) {
        // Decode the Base64 string back into the original JSON string
        byte[] decodedBytes = Base64.getDecoder().decode(base64Texture);
        String json = new String(decodedBytes);

        // Parse the JSON string to extract the texture URL
        JSONObject jsonObj = new JSONObject(json);
        String textureUrl = jsonObj
                .getJSONObject("textures")
                .getJSONObject("SKIN")
                .getString("url");

        return textureUrl;
    }
    

    public static String convertUrlToBase64(String textureUrl) {
        // Construct the JSON structure with the texture URL
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";

        // Encode the JSON structure to Base64
        String base64 = Base64.getEncoder().encodeToString(json.getBytes());
        return base64;
    }
}