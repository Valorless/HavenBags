package valorless.havenbags;

import valorless.valorlessutils.config.Config;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.PlaceholderAPI;
import valorless.havenbags.hooks.PlaceholderAPIHook;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;

public class Lang {
	
	public static Config lang;
		
	public static class Placeholders{
		public static String plugin = "§7[§aHaven§bBags§7]§r";
	}
	
	public static String Parse(String text, OfflinePlayer... player) {
		if(!Utils.IsStringNullOrEmpty(text)) {

			if(player.length != 0) {
				text = ParsePlaceholders(text, player[0]);
			}
		
			text = hex(text);
			text = text.replace("&", "§");
			text = text.replace("\\n", "\n");
			if(player.length != 0) {
				text = text.replace("%player%", player[0].getName());
				text = text.replace("%s", player[0].getName());
			}
		}
		return text;
	}
	
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
	public static String ParsePlaceholders(String text, OfflinePlayer player) {
		if(PlaceholderAPIHook.isHooked()) {
			text = text.replace("{", "%");
			text = text.replace("}", "%");
			return PlaceholderAPI.setPlaceholders(player, text);
		}else {
			return text;
		}
	}
	
	public static String ParseCustomPlaceholders(String text, List<Placeholder> placeholders) {
		for(Placeholder ph : placeholders) {
			text = text.replace(ph.key, ph.value);
			text = Parse(text);
		}
		return text;
	}
}
