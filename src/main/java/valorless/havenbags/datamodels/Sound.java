package valorless.havenbags.datamodels;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.sound.SFX;

/**
 * Data model representing a sound to be played by Ravencrest.
 * <p>
 * This class encapsulates a sound identifier along with playback parameters (volume and pitch).
 * It supports both namespaced sound keys (e.g., "minecraft:entity.player.levelup") and
 * legacy enum-based keys (e.g., "ENTITY_PLAYER_LEVELUP"), providing flexibility across
 * different Minecraft versions.
 * </p>
 * <p>
 * Instances can be created directly or parsed from a compact string format using
 * {@link #parse(String)}.
 * </p>
 * 
 * @author Valorless
 * @since 1.0
 */
public class Sound {

    /** Namespaced or legacy sound key identifier, e.g. "minecraft:entity.player.levelup" or "ENTITY_PLAYER_LEVELUP". */
    public String key;
    
    /** Playback volume. Typical range: 0.0+; 1.0 is normal volume. */
    public Float volume;
    
    /** Playback pitch. Typical range: 0.5–2.0; 1.0 is normal pitch. */
    public Float pitch;
    
    /**
     * Creates a sound model with the given identifier and playback parameters.
     *
     * @param sound  the sound key identifier (must not be null)
     * @param volume the desired volume as Double (must not be null); converted to float
     * @param pitch  the desired pitch as Double (must not be null); converted to float
     */
    public Sound(String key, Double volume, Double pitch) {
        this.key = key;                            // store key as provided
        this.volume = volume.floatValue();             // coerce to float for Bukkit API
        this.pitch = pitch.floatValue();               // coerce to float for Bukkit API
    }
    
    /**
     * Parses a sound definition from a compact string.
     * <p>
     * Expected format: <code>key:volume:pitch</code>
     * Examples: <code>ENTITY_PLAYER_LEVELUP:0.5:1.2</code>.
     * Behavior:
     * <ul>
     *   <li>If the input does not contain a colon or does not have exactly 3 parts, the entire input is treated as the key and volume/pitch default to 1.0.</li>
     *   <li>If volume or pitch fail to parse as numbers, fall back to defaults (1.0, 1.0).</li>
     * </ul>
     * </p>
     *
     * @param string the input string to parse
     * @return a constructed Sound model based on the parsed values or sensible defaults
     */
    public static Sound parse(String string) {
        // No explicit volume/pitch provided; use the whole input as the key and default values
        if(!string.contains(":")) return new Sound(string, 1.0, 1.0);
        // Split into key, volume, pitch
        String[] parts = string.split(":");
        if(parts.length != 3) return new Sound(string, 1.0, 1.0);
        String key = parts[0];
        try {
            Double volume = Double.parseDouble(parts[1]);
            Double pitch = Double.parseDouble(parts[2]);
            return new Sound(key, volume, pitch);
        } catch(Exception e) {
            // On parsing failure, revert to defaults to keep behavior predictable
            return new Sound(string, 1.0, 1.0);
        }
    }
    
	/**
	 * Returns a string representation of this sound for debugging purposes.
	 * 
	 * @return a formatted string containing the sound's key, volume, and pitch
	 */
    @Override
    public String toString() {
        // Fields are guaranteed non-null, so print them all unconditionally.
        return "Sound{key=\"" + key + "\", volume=" + volume + ", pitch=" + pitch + "}";
    }
    
    /**
	 * Plays this sound at the specified location.
	 *
	 * @param location the location where the sound should be played
	 */
    @SuppressWarnings("deprecation")
	public void play(Location location) {

		try {
			location.getWorld().playSound(location, org.bukkit.Sound.valueOf(key), volume, pitch);
		}catch(IncompatibleClassChangeError e) {
			SFX.Play(key, volume, pitch, location);
		}catch(Exception e) {
			Log.Debug(Main.plugin, "Failed to play sound: " + this.toString());
		}
	}
    
    /**
     * Plays this sound to the specified player.
     * 
     * @param player the player where the sound should be played
     */
    @SuppressWarnings("deprecation")
	public void play(Player player) {
    	try {
    		player.getWorld().playSound(player, org.bukkit.Sound.valueOf(key), volume, pitch);
		}catch(IncompatibleClassChangeError e) {
			SFX.Play(key, volume, pitch, player);
		}catch(Exception e) {
			Log.Debug(Main.plugin, "Failed to play sound: " + this.toString());
		}
    	
	}
}
