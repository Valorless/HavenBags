package valorless.havenbags.utils;

import org.bukkit.Location;

import java.util.Random;

/**
 * Miscellaneous utility class providing common helper methods.
 * <p>
 * This class contains various utility functions that don't fit into more specific
 * utility classes, including string formatting and probability calculations.
 * All methods are static and the class cannot be instantiated.
 * </p>
 */
public interface Extra {

	/**
	 * Converts an object to a String, formatting Doubles to two decimal places.
	 * <p>
	 * Example: 3.14159 -> "3.14"; other objects use String.valueOf semantics.
	 * </p>
	 *
	 * @param d the object to stringify
	 * @return a string formatted to two decimal places
	 */
	public static String formatDouble(Double d) {
		return String.format("%.2f", d);
	}

	/**
	 * Converts a string to title case, capitalizing the first letter of each word.
	 * <p>
	 * Underscores are converted to spaces, and the first letter of each word
	 * (separated by spaces or other non-letter characters) is capitalized while
	 * all other letters are lowercased.
	 * </p>
	 * <p>
	 * Example: "DIAMOND_SWORD" becomes "Diamond Sword"
	 * </p>
	 *
	 * @param string the input string to format
	 * @return the formatted string with each word capitalized
	 */
	public static String UppercaseFirstLetter(String string) {
    	string = string.replace('_', ' ');
        char[] charArray = string.toCharArray();
        boolean foundSpace = true;
        for(int i = 0; i < charArray.length; i++) {
        	charArray[i] = Character.toLowerCase(charArray[i]);
        	if(Character.isLetter(charArray[i])) {
        		if(foundSpace) {
        			charArray[i] = Character.toUpperCase(charArray[i]);
        			foundSpace = false;
        		}
        	}
        	else {
        		foundSpace = true;
        	}
        }
        string = String.valueOf(charArray);
    	return string;
    }
	

	/** Shared Random instance used for probability calculations */
    static Random rand = new Random();
    
	/**
	 * Determines if a random event occurs based on the given probability percentage.
	 * <p>
	 * Generates a random number between 0 and 100 (exclusive) and compares it to the
	 * provided percentage. This is useful for implementing random drop chances,
	 * proc effects, and other probability-based game mechanics.
	 * </p>
	 * <p>
	 * Example: Chance(25.0) has a 25% probability of returning true.
	 * </p>
	 *
	 * @param percent the probability percentage (0.0 to 100.0)
	 * @return true if the random roll is less than or equal to the percentage; false otherwise
	 */
	public static Boolean Chance(double percent) {
        int low = 0;
        int high = 100;
        int result = rand.nextInt(high-low) + low;
        if (result <= percent) return true;
        else return false;
    }

	/**
	 * Formats a Location object into a string based on a provided format template.
	 * <p>
	 * The format string can contain the following placeholders:
	 * <ul>
	 *   <li>%w - world name</li>
	 *   <li>%x - X coordinate (formatted to 2 decimal places)</li>
	 *   <li>%y - Y coordinate (formatted to 2 decimal places)</li>
	 *   <li>%z - Z coordinate (formatted to 2 decimal places)</li>
	 * </ul>
	 * Example: formatLocation(loc, "%w: %x, %y, %z") might return "world: 100.00, 64.00, -50.00".
	 *
	 * @param loc the Location to format
	 * @param format the format string containing placeholders for world and coordinates
	 * @return the formatted location string with placeholders replaced by actual values
	 */
	public static String formatLocation(Location loc, String format) {
		return format.replace("%w", loc.getWorld().getName())
				.replace("%x", formatDouble(loc.getX()))
				.replace("%y", formatDouble(loc.getY()))
				.replace("%z", formatDouble(loc.getZ()));
	}
}
