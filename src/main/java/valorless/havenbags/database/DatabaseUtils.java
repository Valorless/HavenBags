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

	public class Extra {
		public boolean autoSort;
	}
	
	public static String Extra(Data data) {
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
		text += String.format(", tooltip:%s", data.getTooltipStyle());
		
		text += "}";
		//Log.Info(Main.plugin, text);
		return text;
	}
	
	public static void ApplyExtra(Data data, String datastring) {
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
        if(extra.containsKey("tooltip")) data.setTooltipStyle((String) extra.get("tooltip"));
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

        // Updated regex to handle values with escaped quotes (e.g., name:"Shulker \"King\"")
        Matcher pairMatcher = Pattern.compile("(\\w+):(\"(?:\\\\\"|[^\"])*\"|\\w+)").matcher(content);
        while (pairMatcher.find()) {
            String key = pairMatcher.group(1);
            String value = pairMatcher.group(2);

            // Remove surrounding quotes if present
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1).replace("\\\"", "\""); // Unescape quotes
            }

            // Parse the value into the appropriate data type
            Object parsedValue = parseValue(value);
            extraData.put(key, parsedValue);
        }

        return extraData;
    }

    private static Object parseValue(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        if (value.matches("-?\\d+")) return Integer.parseInt(value);  // Integer
        if (value.matches("-?\\d+\\.\\d+")) return Double.parseDouble(value); // Double
        if (value.matches("-?\\d+[lL]")) return Long.parseLong(value.substring(0, value.length() - 1)); // Long (e.g., 12345L)
        return value; // Default to String
    }
	
}
