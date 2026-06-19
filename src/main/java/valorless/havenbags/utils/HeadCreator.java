package valorless.havenbags.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.json.JSONException;
import org.json.JSONObject;

import valorless.havenbags.annotations.NotNull;
import valorless.havenbags.datamodels.Bag;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.nbtapi.iface.ReadWriteNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBT;
import valorless.valorlessutils.nbtapi.iface.ReadableNBTList;

import java.lang.reflect.Method;
import java.net.URL;
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

        setTextureValue(skull, base64);

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
        return jsonObj
                .getJSONObject("textures")
                .getJSONObject("SKIN")
                .getString("url");
    }


    public static String convertUrlToBase64(String textureUrl) {
        // Construct the JSON structure with the texture URL
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";

        // Encode the JSON structure to Base64
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    public static String getTextureValue(Bag bag) {
        return bag.getTexture();
    }

    public static String getTextureValue(ItemStack head) {
        if (head == null || head.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("ItemStack must be a Player Head");
        }

        if(Server.VersionHigherOrEqualTo(Server.Version.v1_21_1)) {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            //if(meta.getOwnerProfile().getTextures().getSkin() == null){
            //	return getbag(HavenBags.GetBagUUID(head)).getTexture();
            //}
            return convertUrlToBase64(meta.getOwnerProfile().getTextures().getSkin().toString());
        }else {

            // Use NBTAPI to access the NBT data
            if (!valorless.valorlessutils.nbtapi.NBT.readNbt(head).hasTag("SkullOwner")) {
                return null;
            }

            // Access the SkullOwner NBT compound
            //NBTCompound skullOwner = nbti.getCompound("SkullOwner");
            ReadableNBT skullOwner = valorless.valorlessutils.nbtapi.NBT.readNbt(head).getCompound("SkullOwner");
            if (skullOwner == null || !skullOwner.hasTag("Properties")) {
                return null;
            }

            // Access the Properties NBT compound
            ReadableNBT properties = skullOwner.getCompound("Properties");
            if (properties == null || !properties.hasTag("textures")) {
                return null;
            }

            // Access the textures NBT list
            ReadableNBTList<ReadWriteNBT> textures = properties.getCompoundList("textures");
            if (textures == null || textures.isEmpty()) {
                return null;
            }

            // Get the first texture compound
            ReadWriteNBT texture = textures.get(0);
            if (texture == null || !texture.hasTag("Value")) {
                return null;
            }

            // Return the texture value
            return texture.getString("Value");
        }
    }

    public static void setTextureValue(@NotNull ItemStack item, @NotNull String value) {
        if (item.getType() != Material.PLAYER_HEAD) return;

        //if (!(item.getItemMeta() instanceof SkullMeta meta)) return;
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        UUID uuid = UUID.nameUUIDFromBytes(value.getBytes());

        if(Server.VersionHigherOrEqualTo(Server.Version.v1_21_1) || Server.VersionEqualTo(Server.Version.NULL)) {
            try {
                // Create a new GameProfile with a random UUID and apply the texture
                PlayerProfile profile = Bukkit.getServer().createPlayerProfile(uuid, "bag");
                PlayerTextures textures = profile.getTextures();
                try {
                    textures.setSkin(new URL(extractUrlFromBase64(value)));
                }catch (JSONException e) { return; }
                profile.setTextures(textures);

                // Use the API method to set the profile (this method was introduced in recent Spigot versions)
                meta.setOwnerProfile(profile);
            }catch(Exception E) {
                E.printStackTrace();
            }
        }else {
            try {
                GameProfile profile = new GameProfile(uuid, "null");
                profile.getProperties().put("textures", new Property("textures", value));

                Method method = Reflect.getMethod(meta.getClass(), "setProfile", GameProfile.class);
                if (method != null) {
                    Reflect.invokeMethod(method, meta, profile);
                } else {
                    Reflect.setFieldValue(meta, "profile", profile);
                }
            }catch(Exception e) {}
        }

        item.setItemMeta(meta);
    }
}