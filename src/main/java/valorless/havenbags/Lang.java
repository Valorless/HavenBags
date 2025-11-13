package valorless.havenbags;

import valorless.valorlessutils.config.Config;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.hooks.PlaceholderAPIHook;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;

public class Lang {
	
	public static Config lang;
	//public static String plugin = "§7[§aHaven§bBags§7]§r";
	
	public static String Parse(String text, OfflinePlayer player) {
		if(!Utils.IsStringNullOrEmpty(text)) {

			if(player != null) {
				text = ParsePlaceholderAPI(text, player);
			} else {
				OfflinePlayer[] offp = Bukkit.getOfflinePlayers();
				// Choose random player as placeholder to parse strings, without a defined player.
				try {
					text = ParsePlaceholderAPI(text, offp[0]);
				}catch(Exception e) {
					text = ParsePlaceholderAPI(text, null);
				}
			}
		
			text = hex(text);
			text = text.replace("&", "§");
			text = text.replace("\\n", "\n");
			if(player != null) {
				text = text.replace("%player%", player.getName());
			}
		}
		return text;
	}
	
	public static String Parse(String text, List<Placeholder> placeholders, OfflinePlayer... player) {
		for(Placeholder ph : placeholders) {
			text = text.replace(ph.getKey(), ph.getValue());
			if(player.length != 0) {
				text = Parse(text, player[0]);
			}else {
				text = Parse(text, null);
			}
		}
		return text;
	}
	
	/* Old Code
	public static String Parse(String text, String player) {
		if(!Utils.IsStringNullOrEmpty(text)) {		
			text = hex(text);
			text = text.replace("&", "§");
			text = text.replace("\\n", "\n");
			if(!Utils.IsStringNullOrEmpty(player)) {
				text = text.replace("%player%", player);
				text = text.replace("%s", player);
			}
		}
		return text;
	}
	*/
	
	public static String Get(String key, OfflinePlayer... player) {
		if(lang.Get(key) == null) {
			Log.Error(Main.plugin, String.format("Lang.yml is missing the key '%s'!", key));
			return "§4error";
		}
		return player == null ?  Parse(lang.GetString(key), null) : Parse(lang.GetString(key), player[0]);
	}

	/* Old Code
	public static String Get(String key) {
		if(lang.Get(key) == null) {
			Log.Error(Main.plugin, String.format("Lang.yml is missing the key '%s'!", key));
			return "§4error";
		}
		if(lang.GetString(key).contains("%player%")) {
			return lang.GetString(key);
		}else {
			return Parse(lang.GetString(key));
		}
	}
	
	public static String Get(String key, Object arg) {
		if(lang.Get(key) == null) {
			Log.Error(Main.plugin, String.format("Lang.yml is missing the key '%s'!", key));
			return "§4error";
		}
		if(lang.GetString(key).contains("%player%")) {
			return Parse(lang.GetString(key), arg.toString());
		}else {
			return Parse(String.format(lang.GetString(key), arg.toString()));
		}
	}
	
	public static String Get(String key, Object arg1, Object arg2) {
		if(lang.Get(key) == null) {
			Log.Error(Main.plugin, String.format("Lang.yml is missing the key '%s'!", key));
			return "§4error";
		}
		return Parse(String.format(lang.GetString(key), arg1.toString(), arg2.toString()));
	}
	*/
	
	public static String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
           
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }
           
            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
	
	/***
	 * PlaceholderAPI
	 * @param text
	 * @param player
	 * @return
	 */
	public static String ParsePlaceholderAPI(String text, OfflinePlayer player) {
		if(PlaceholderAPIHook.isHooked()) {
			String t = "";
			text = text.replace("{", "%");
			text = text.replace("}", "%");
			try {
				t =  PlaceholderAPI.setPlaceholders(player, text);  
        	}catch (Exception e) {
        		Log.Error(Main.plugin, "Failed to get PlaceholderAPI. Is it up to date?");
        		t = text;
        	}
			return t;
		}else {
			return text;
		}
	}
	
	public static String RemoveColorFormatting(String text) {
		if(!Utils.IsStringNullOrEmpty(text)) {
			text = text.replace("§§", "§");
			text = RemoveHex(text);
			text = text.replace("§0", "");
			text = text.replace("§1", "");
			text = text.replace("§2", "");
			text = text.replace("§3", "");
			text = text.replace("§4", "");
			text = text.replace("§5", "");
			text = text.replace("§6", "");
			text = text.replace("§7", "");
			text = text.replace("§8", "");
			text = text.replace("§9", "");
			text = text.replace("§0", "");
			text = text.replace("§a", "");
			text = text.replace("§b", "");
			text = text.replace("§c", "");
			text = text.replace("§d", "");
			text = text.replace("§e", "");
			text = text.replace("§f", "");
			text = text.replace("§o", "");
			text = text.replace("§l", "");
			text = text.replace("§k", "");
			text = text.replace("§m", "");
			text = text.replace("§n", "");
			text = text.replace("§r", "");
			text = text.replace("§A", "");
			text = text.replace("§B", "");
			text = text.replace("§C", "");
			text = text.replace("§D", "");
			text = text.replace("§E", "");
			text = text.replace("§F", "");
			text = text.replace("§x", "");
			text = text.replace("§", "");
		}
		return text;
	}
	
	public static String RemoveHex(String text) {
        // Regex to match the pattern of hex color codes
    	//Log.Info(Main.plugin, text);
        String hexColorRegex = "§x(§[0-9A-Fa-f]){6}";
        return text.replaceAll(hexColorRegex, "");
    }
	
	public static char ParsePlaceholderChar(String text) {
		String parsed = ParsePlaceholderAPI(text, null);
		return parsed.length() == 1 ? parsed.charAt(0) : '?';
	}
}
