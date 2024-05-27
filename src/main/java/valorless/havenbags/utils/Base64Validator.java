package valorless.havenbags.utils;

import java.util.Base64;

public class Base64Validator {

    public static boolean isValidBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }

        // Regular expression to check if the string contains only valid Base64 characters
        String base64Pattern = "^[a-zA-Z0-9+/]*={0,2}$";
        if (!base64.matches(base64Pattern)) {
            return false;
        }

        try {
            // Decode the Base64 string
            byte[] decodedBytes = Base64.getDecoder().decode(base64);

            // Re-encode and compare with the original string (ignore padding for the comparison)
            String encodedString = Base64.getEncoder().encodeToString(decodedBytes).replaceAll("=*$", "");
            String originalStringWithoutPadding = base64.replaceAll("=*$", "");
            
            return originalStringWithoutPadding.equals(encodedString);
        } catch (IllegalArgumentException e) {
            // If decoding fails, it's not a valid Base64 string
        	//throw new IllegalArgumentException("Value is not a valid Base64");
            return false;
        }
    }
}