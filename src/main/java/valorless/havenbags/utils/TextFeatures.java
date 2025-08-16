package valorless.havenbags.utils;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;

public class TextFeatures {
	
	public static String CreateBar(double progress, double total, int barLength) {
		double filledRatio = (double) progress / total;
	    int filledLength = (int) (barLength * filledRatio);
	    int remainingLength = barLength - filledLength;
		StringBuilder bar = new StringBuilder("[");
	    for (int i = 0; i < filledLength; i++) {
	        bar.append("⬛");
	    }
	    for (int i = 0; i < remainingLength; i++) {
	        bar.append("⬜");
	    }
	    bar.append("]");
	    return bar.toString();
	}
	
	public static String CreateBar(double progress, double total, int barLength, String barColor, String fillColor, char barStyle, char fillStyle) {
		double filledRatio = (double) progress / total;
	    int filledLength = (int) (barLength * filledRatio);
	    int remainingLength = barLength - filledLength;
		StringBuilder bar = new StringBuilder(Lang.hex(barColor) + "[");
	    for (int i = 0; i < filledLength; i++) {
	    	bar.append(Lang.hex(fillColor));
	        bar.append(fillStyle);
	        //bar.append("⬛");
	    }
	    for (int i = 0; i < remainingLength; i++) {
	    	bar.append(Lang.hex(barColor));
	        bar.append(barStyle);
	        //bar.append("⬜");
	    }
	    bar.append(Lang.hex(barColor) + "]&r");
	    return bar.toString();
	}
	
	public static String CreateBarWeight(double progress, double total, int barLength) {
		double filledRatio = (double) progress / total;
	    int filledLength = (int) (barLength * filledRatio);
	    int remainingLength = barLength - filledLength;
	    char fillStyle = Lang.ParsePlaceholderChar(Main.weight.GetString("fill-style"));
	    char barStyle = Lang.ParsePlaceholderChar(Main.weight.GetString("bar-style"));
	    String fillColor = Lang.Parse(Main.weight.GetString("fill-color"), null);
	    String barColor = Lang.Parse(Main.weight.GetString("bar-color"), null);
	    String barStart = Lang.Parse(Main.weight.GetString("bar-start"), null);
	    String barEnd = Lang.Parse(Main.weight.GetString("bar-end"), null);
	    
		StringBuilder bar = new StringBuilder(barColor + barStart);
	    for (int i = 0; i < filledLength; i++) {
	    	bar.append(fillColor);
	        bar.append(fillStyle);
	    }
	    for (int i = 0; i < remainingLength; i++) {
	    	bar.append(barColor);
	        bar.append(barStyle);
	    }
	    bar.append(barColor + barEnd + "§r");
	    return bar.toString();
	}
	
	public static String LimitCharacters(String text, int length) {
		String t = "";
		try {
			if(length > text.length()) length = text.length();
			for(int i = 0; i < length; i++) {
				t = t + text.charAt(i);
			}
		} catch (Exception e) { return text; }
		return t;
	}
	
	public static String LimitDecimal(String text, int length) {
		String t = "";
		try {
			String[] split = text.split("\\.");
			t = split[0] + ".";
			if(length > text.length()) length = split[1].length();
			for(int i = 0; i < length; i++) {
				t = t + split[1].charAt(i);
			}
		} catch (Exception e) { return text; }
		return t;
	}
	
}
