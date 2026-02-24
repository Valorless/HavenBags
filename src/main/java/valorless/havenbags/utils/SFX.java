package valorless.havenbags.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import valorless.valorlessutils.ValorlessUtils;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;

/**
 * Sound effects utility class for playing sounds in various contexts.
 * <p>
 * This class provides methods to play sounds to players or at locations, supporting
 * both legacy enum-based sound keys and modern namespaced identifiers. It includes
 * utilities for extracting sound information from blocks and materials using reflection
 * to maintain compatibility across Minecraft versions.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Play sounds to players or at world locations</li>
 *   <li>Support for both enum and namespaced sound identifiers</li>
 *   <li>Integration with {@link valorless.havenbags.datamodels.ravencrest.Sound} model</li>
 *   <li>Block and material sound extraction using reflection</li>
 *   <li>Fallback handling for version compatibility</li>
 * </ul>
 * </p>
 * 
 * @author Valorless
 * @since 1.0
 */
public class SFX {
	/**
	 * Plays a sound at the player's location with the given volume and pitch.
	 * <p>
	 * Automatically detects whether the sound identifier is namespaced (contains a period)
	 * and delegates to the appropriate playback method. If the sound string is null or empty,
	 * no action is taken.
	 * </p>
	 *
	 * @param sound   The name of the sound to play.
	 * @param volume  The volume of the sound (1.0 is normal volume).
	 * @param pitch   The pitch of the sound (1.0 is normal pitch).
	 * @param player  The player for whom the sound will be played.
	 */
	@SuppressWarnings("deprecation")
	public static void Play(String sound, float volume, float pitch, Player player) {
	    if (!Utils.IsStringNullOrEmpty(sound)) {
	    	try {
	    		
	    		if(sound.contains(".")) {
	    			PlayNamespace(sound, volume, pitch, player);
	    			return;
	    		}
	    		
	    		player.playSound(player, Sound.valueOf(sound), SoundCategory.MASTER, volume, pitch);
	    	} catch (Exception e) {
	    		Log.Error(ValorlessUtils.GetInstance(), e.getMessage());
	    	}
	    }
	}
	
	/**
	 * Plays a namespaced sound to a player.
	 * <p>
	 * Uses the modern namespaced sound format (e.g., "minecraft:entity.player.levelup").
	 * If the sound string is null or empty, no action is taken. Errors are logged but
	 * do not interrupt execution.
	 * </p>
	 * 
	 * @param sound  the namespaced sound identifier
	 * @param volume the volume (1.0 is normal volume)
	 * @param pitch  the pitch (1.0 is normal pitch)
	 * @param player the player who should hear the sound
	 */
	public static void PlayNamespace(String sound, float volume, float pitch, Player player) {
	    if (!Utils.IsStringNullOrEmpty(sound)) {
	    	try {
	    		player.playSound(player, sound, SoundCategory.MASTER, volume, pitch);
	    	} catch (Exception e) {
	    		Log.Error(ValorlessUtils.GetInstance(), e.getMessage());
	    	}
	    }
	}
	
	/**
	 * Plays a sound at a specific location with the given volume and pitch.
	 * <p>
	 * Automatically detects whether the sound identifier is namespaced (contains a period)
	 * and delegates to the appropriate playback method. If the sound string is null or empty,
	 * no action is taken.
	 * </p>
	 * 
	 * @param sound    the sound identifier (enum name or namespaced)
	 * @param volume   the volume (1.0 is normal volume)
	 * @param pitch    the pitch (1.0 is normal pitch)
	 * @param location the location where the sound should be played
	 */
	public static void Play(String sound, float volume, float pitch, Location location) {
	    if (!Utils.IsStringNullOrEmpty(sound)) {
	    	try {
	    		
	    		if(sound.contains(".")) {
	    			PlayNamespace(sound, volume, pitch, location);
	    			return;
	    		}
	    		
    			valorless.valorlessutils.sound.SFX.Play(sound, volume, pitch, location);
	    	} catch (Exception e) {
	    		Log.Error(ValorlessUtils.GetInstance(), e.getMessage());
	    	}
	    }
	}
	
	/**
	 * Plays a namespaced sound at a specific location.
	 * <p>
	 * Uses the modern namespaced sound format (e.g., "minecraft:block.stone.break").
	 * If the sound string is null or empty, no action is taken. Errors are logged but
	 * do not interrupt execution.
	 * </p>
	 * 
	 * @param sound    the namespaced sound identifier
	 * @param volume   the volume (1.0 is normal volume)
	 * @param pitch    the pitch (1.0 is normal pitch)
	 * @param location the location where the sound should be played
	 */
	public static void PlayNamespace(String sound, float volume, float pitch, Location location) {
	    if (!Utils.IsStringNullOrEmpty(sound)) {
	    	try {
	    		location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
	    	} catch (Exception e) {
	    		Log.Error(ValorlessUtils.GetInstance(), e.getMessage());
	    	}
	    }
	}
	
	/**
	 * Retrieves a Bukkit Sound enum constant by name.
	 * <p>
	 * Searches the sound registry for a matching sound identifier (case-insensitive).
	 * Returns null if no matching sound is found.
	 * </p>
	 * 
	 * @param sound the sound identifier to search for
	 * @return the matching Sound enum constant, or null if not found
	 */
	public static Sound GetSound(String sound) {
		for (Sound key : Registry.SOUNDS) {
			if(key.toString().equalsIgnoreCase(sound)) return key;
		}
		return null;
	}
	
	/**
	 * Plays a Sound model to a player.
	 * <p>
	 * Convenience method that extracts the key, volume, and pitch from a
	 * {@link valorless.havenbags.datamodels.ravencrest.Sound} instance and plays it to the player.
	 * </p>
	 * 
	 * @param sound  the sound model containing playback parameters
	 * @param player the player who should hear the sound
	 */
	public static void Play(valorless.havenbags.datamodels.Sound sound, Player player) {
	    Play(sound.key, sound.volume, sound.pitch, player);
	}
	
	/**
	 * Plays a Sound model at a specific location.
	 * <p>
	 * Convenience method that extracts the key, volume, and pitch from a
	 * {@link valorless.havenbags.datamodels.ravencrest.Sound} instance and plays it at the location.
	 * </p>
	 * 
	 * @param sound    the sound model containing playback parameters
	 * @param location the location where the sound should be played
	 */
	public static void Play(valorless.havenbags.datamodels.Sound sound, Location location) {
	    Play(sound.key, sound.volume, sound.pitch, location);
	}
	
	/** Default sound identifier when block place sound cannot be determined. */
	private static final String DEFAULT_PLACE = "BLOCK_STONE_PLACE";
	
	/** Default sound identifier when block break sound cannot be determined. */
    private static final String DEFAULT_BREAK = "BLOCK_STONE_BREAK";
	
	/**
	 * Retrieves the placement sound identifier for a block.
	 * <p>
	 * Uses reflection to access the SoundGroup API, which may vary between versions.
	 * Returns a default stone placement sound if the block or sound cannot be determined.
	 * </p>
	 * 
	 * @param block the block to get the placement sound for
	 * @return the sound identifier string, or {@link #DEFAULT_PLACE} if unavailable
	 */
	public static String getSoundPlace(Block block) {
        if (block == null) return DEFAULT_PLACE;
        try {
            BlockData blockData = block.getBlockData();
            if (blockData == null) return DEFAULT_PLACE;

            // Use reflection to avoid static linkage to org.bukkit.Sound
            java.lang.reflect.Method getPlaceSound = SoundGroup.class.getMethod("getPlaceSound");
            Object soundGroup = blockData.getSoundGroup();
            if (soundGroup == null) return DEFAULT_PLACE;

            Object placeSound = getPlaceSound.invoke(soundGroup);
            if (placeSound == null) return DEFAULT_PLACE;

            return extractSoundKey(placeSound, DEFAULT_PLACE);
        } catch (ReflectiveOperationException e) {
            // If anything goes wrong, fall back safely
            e.printStackTrace();
            return DEFAULT_PLACE;
        }
    }

	/**
	 * Retrieves the placement sound identifier for a material.
	 * <p>
	 * Uses reflection to access the SoundGroup API via created block data.
	 * Returns null if the material is not a block, or the default sound if
	 * the sound cannot be determined.
	 * </p>
	 * 
	 * @param material the material to get the placement sound for
	 * @return the sound identifier string, or null if not a block
	 */
    public static String getSoundPlace(Material material) {
        if (material == null || !material.isBlock()) return null;
        try {
            BlockData data = Bukkit.createBlockData(material);
            if (data == null) return DEFAULT_PLACE;

            java.lang.reflect.Method getPlaceSound = SoundGroup.class.getMethod("getPlaceSound");
            Object soundGroup = data.getSoundGroup();
            if (soundGroup == null) return DEFAULT_PLACE;

            Object placeSound = getPlaceSound.invoke(soundGroup);
            if (placeSound == null) return DEFAULT_PLACE;

            return extractSoundKey(placeSound, DEFAULT_PLACE);
        } catch (IllegalArgumentException iae) {
            // createBlockData can throw for some materials; treat as no-sound
            return DEFAULT_PLACE;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return DEFAULT_PLACE;
        }
    }

	/**
	 * Retrieves the break sound identifier for a block.
	 * <p>
	 * Uses reflection to access the SoundGroup API, which may vary between versions.
	 * Returns a default stone break sound if the block or sound cannot be determined.
	 * </p>
	 * 
	 * @param block the block to get the break sound for
	 * @return the sound identifier string, or {@link #DEFAULT_BREAK} if unavailable
	 */
    public static String getSoundBreak(Block block) {
        if (block == null) return DEFAULT_BREAK;
        try {
            BlockData blockData = block.getBlockData();
            if (blockData == null) return DEFAULT_BREAK;

            java.lang.reflect.Method getBreakSound = SoundGroup.class.getMethod("getBreakSound");
            Object soundGroup = blockData.getSoundGroup();
            if (soundGroup == null) return DEFAULT_BREAK;

            Object breakSound = getBreakSound.invoke(soundGroup);
            if (breakSound == null) return DEFAULT_BREAK;

            return extractSoundKey(breakSound, DEFAULT_BREAK);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return DEFAULT_BREAK;
        }
    }

	/**
	 * Retrieves the break sound identifier for a material.
	 * <p>
	 * Uses reflection to access the SoundGroup API via created block data.
	 * Returns null if the material is not a block, or the default sound if
	 * the sound cannot be determined.
	 * </p>
	 * 
	 * @param material the material to get the break sound for
	 * @return the sound identifier string, or null if not a block
	 */
    public static String getSoundBreak(Material material) {
        if (material == null || !material.isBlock()) return null;
        try {
            BlockData data = Bukkit.createBlockData(material);
            if (data == null) return DEFAULT_BREAK;

            java.lang.reflect.Method getBreakSound = SoundGroup.class.getMethod("getBreakSound");
            Object soundGroup = data.getSoundGroup();
            if (soundGroup == null) return DEFAULT_BREAK;

            Object breakSound = getBreakSound.invoke(soundGroup);
            if (breakSound == null) return DEFAULT_BREAK;

            return extractSoundKey(breakSound, DEFAULT_BREAK);
        } catch (IllegalArgumentException iae) {
            return DEFAULT_BREAK;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return DEFAULT_BREAK;
        }
    }

	/**
	 * Extracts a sound identifier from a Sound object using reflection.
	 * <p>
	 * This method attempts multiple strategies to extract the sound identifier,
	 * as the API varies across Minecraft versions:
	 * <ol>
	 *   <li>Try getKeyOrThrow() -&gt; getKey()</li>
	 *   <li>Try getKey() directly</li>
	 *   <li>Try name() (for enum-based sounds)</li>
	 *   <li>Fall back to toString()</li>
	 * </ol>
	 * Returns the provided fallback if all strategies fail or return null/empty.
	 * </p>
	 * 
	 * @param soundObj the sound object to extract the identifier from
	 * @param fallback the fallback identifier to return if extraction fails
	 * @return the extracted sound identifier, or the fallback value
	 */
    private static String extractSoundKey(Object soundObj, String fallback) {
        if (soundObj == null) return fallback;
        try {
            // Try getKeyOrThrow() -> getKey()
            try {
                java.lang.reflect.Method getKeyOrThrow = soundObj.getClass().getMethod("getKeyOrThrow");
                Object keyObj = getKeyOrThrow.invoke(soundObj);
                if (keyObj != null) {
                    try {
                        java.lang.reflect.Method getKey = keyObj.getClass().getMethod("getKey");
                        Object keyVal = getKey.invoke(keyObj);
                        if (keyVal != null) return keyVal.toString();
                    } catch (NoSuchMethodException ignored) {}
                    if (keyObj.toString() != null) return keyObj.toString();
                }
            } catch (NoSuchMethodException ignored) {}

            // Try getKey()
            try {
                java.lang.reflect.Method getKey = soundObj.getClass().getMethod("getKey");
                Object key = getKey.invoke(soundObj);
                if (key != null) return key.toString();
            } catch (NoSuchMethodException ignored) {}

            // Try name() (enum)
            try {
                java.lang.reflect.Method name = soundObj.getClass().getMethod("name");
                Object enumName = name.invoke(soundObj);
                if (enumName != null) return enumName.toString();
            } catch (NoSuchMethodException ignored) {}

            // Fall back to toString()
            String s = soundObj.toString();
            return (s == null || s.isEmpty()) ? fallback : s;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return fallback;
        }
    }
}