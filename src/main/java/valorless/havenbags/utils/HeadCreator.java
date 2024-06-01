package valorless.havenbags.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBTCompound;
import valorless.valorlessutils.nbt.NBTItem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

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
        properties.getCompoundList("textures").addCompound().setString("Value", base64);
        //Log.Error(Main.plugin, skull.toString());
        //Log.Error(Main.plugin, nbtItem.getItem().toString());
        return nbtItem.getItem();
    }
}