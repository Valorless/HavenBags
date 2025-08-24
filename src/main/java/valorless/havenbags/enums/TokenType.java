package valorless.havenbags.enums;

/**
 * Enum representing different types of tokens in the HavenBags plugin.
 * Each token type corresponds to a specific category of items or data.
 */
public enum TokenType {
	Texture,
	ModelData,
	ItemModel,
	Effect;
	
	/**
	 * Retrieves the TokenType corresponding to the given string value.
	 * The comparison is case-insensitive.
	 *
	 * @param value The string representation of the token type.
	 * @return The matching TokenType, or null if no match is found.
	 */
	public static TokenType get(String value) {
		for(TokenType type : values()) {
			if(type.name().equalsIgnoreCase(value)) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Returns the string representation of the TokenType in lowercase.
	 *
	 * @return The lowercase name of the TokenType.
	 */
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
