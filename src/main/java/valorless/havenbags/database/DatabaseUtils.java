package valorless.havenbags.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.utils.Utils;

public class DatabaseUtils {

	public static class Extra {
		public boolean autoSort;
		// Wtf was this even for?
	}
	
	public static String extra(Data data) {
		String text = "extra{";
		
		text += String.format("autosort:%s", data.hasAutoSort());
		if(data.getMaterial() != null) text += String.format(", material:%s", data.getMaterial());
		//if(data.getName() != null) text += String.format(", name:%s", data.getName());
		if(!Utils.IsStringNullOrEmpty(data.getName())) {
			String safeName = "\"" + data.getName().replace("\"", "\\\"") + "\"";
			text += String.format(", name:%s", safeName);
		}
		
		if(data.getBlacklist() != null) {
			text += String.format(", blacklist:%s", formatList(data.getBlacklist()));
			text += String.format(", whitelist:%s", data.isWhitelist());
			text += String.format(", ignoreglobalblacklist:%s", data.isIngoreGlobalBlacklist());
		}
		
		text += String.format(", magnet:%s", data.hasMagnet());
		text += String.format(", refill:%s", data.hasRefill());
		text += String.format(", effect:%s", data.getEffect());
		text += String.format(", tooltip:%s", formatNamespacedKey(data.getTooltipStyle()));
		
		text += "}";
		//Log.Info(Main.plugin, text);
		return text;
	}
	
	public static void applyExtra(Data data, String datastring) {
		Map<String, Object> extra = DatabaseUtils.ParseExtra(datastring);
        if(extra.containsKey("autosort")) data.setAutoSort((Boolean) extra.get("autosort"));
        if(extra.containsKey("material")) data.setMaterial((String) extra.get("material"));
        if(extra.containsKey("name")) data.setName((String) extra.get("name"));
        if(extra.containsKey("blacklist")) data.setBlacklist(parseList((String) extra.get("blacklist")));
        if(extra.containsKey("whitelist")) data.setWhitelist((Boolean) extra.get("whitelist"));
        if(extra.containsKey("ignoreglobalblacklist")) data.setIgnoreGlobalBlacklist((Boolean) extra.get("ignoreglobalblacklist"));
        if(extra.containsKey("magnet")) data.setMagnet((Boolean) extra.get("magnet"));
        if(extra.containsKey("refill")) data.setRefill((Boolean) extra.get("refill"));
        if(extra.containsKey("effect")) data.setEffect((String) extra.get("effect"));
        if(extra.containsKey("tooltip")) data.setTooltipStyle(parseNamespacedKey((String) extra.get("tooltip")));
	}
	
	public static Object formatList(List<String> list) {
		if(list.isEmpty()) return "[]";
		return String.join("=", list);
	}
	
	public static List<String> parseList(String text){
		if(text.equalsIgnoreCase("[]")) return new ArrayList<String>();
		List<String> blacklist = new ArrayList<>();
		for(String entry : text.split("=")) {
			blacklist.add(entry);
		}
		return blacklist;
	}

	// I hate Regex, I just do not understand it..
	// So I had AI do some grunt work, and I did some touch-ups to make sure it works.
	
	public static Map<String, Object> ParseExtra(String input) {
        Map<String, Object> extraData = new HashMap<>();

        // Match `extra{...}` block
        Matcher extraMatcher = Pattern.compile("extra\\{(.*?)}").matcher(input);
        if (!extraMatcher.find()) return extraData; // No "extra{}" found

        String content = extraMatcher.group(1); // Extract content inside extra{}

		// Split key/value pairs on commas, but only when outside quoted strings.
		List<String> tokens = splitPairs(content);
		for (String token : tokens) {
			int colonIndex = token.indexOf(':');
			if (colonIndex <= 0) continue;

			String key = token.substring(0, colonIndex).trim();
			String value = token.substring(colonIndex + 1).trim();

			// Remove surrounding quotes if present and unescape inner quotes.
			if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1).replace("\\\"", "\"");
			}

			Object parsedValue = parseValue(value);
			extraData.put(key, parsedValue);
		}

        return extraData;
    }

	private static List<String> splitPairs(String content) {
		List<String> pairs = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		boolean escaping = false;

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);

			if (escaping) {
				current.append(c);
				escaping = false;
				continue;
			}

			if (c == '\\') {
				current.append(c);
				escaping = true;
				continue;
			}

			if (c == '"') {
				inQuotes = !inQuotes;
				current.append(c);
				continue;
			}

			if (c == ',' && !inQuotes) {
				String token = current.toString().trim();
				if (!token.isEmpty()) pairs.add(token);
				current.setLength(0);
				continue;
			}

			current.append(c);
		}

		String token = current.toString().trim();
		if (!token.isEmpty()) pairs.add(token);

		return pairs;
	}

    private static Object parseValue(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        if (value.matches("-?\\d+")) return Integer.parseInt(value);  // Integer
        if (value.matches("-?\\d+\\.\\d+")) return Double.parseDouble(value); // Double
        if (value.matches("-?\\d+[lL]")) return Long.parseLong(value.substring(0, value.length() - 1)); // Long (e.g., 12345L)
        return value; // Default to String
    }

	private static String formatNamespacedKey(String key) {
		if(key == null) return "null";
		return key.replace(":", "=");
	}

	private static String parseNamespacedKey(String key) {
		if(key == null || key.equalsIgnoreCase("null")) return null;
		return key.replace("=", ":");
	}
	
}
