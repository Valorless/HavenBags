package valorless.havenbags;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Message {
	
	public TextComponent message;
	
	public Message(String message) {
		this.message = new TextComponent(Lang.Parse(message, null));
	}
	
	public Message(String message, ChatColor color) {
		this.message = new TextComponent(Lang.Parse(message, null));
		this.message.setColor(color);
	}
	
	public void Send(Player player) {
		player.spigot().sendMessage(message);
	}
	
	public void SetColor(String color) {
		ChatColor.getByChar(color.charAt(0));
	}
	
	@SuppressWarnings("deprecation")
	TextComponent SetHoverText(TextComponent comp, String message) {
		comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Lang.Parse(message, null)).create()));
		return comp;
	}
	
	public void AddTextPart(String message) {
		this.message.addExtra(new TextComponent(Lang.Parse(message, null)));
	}
	
	public void AddTextPart(String message, String hover) {
		this.message.addExtra(
				SetHoverText(new TextComponent(Lang.Parse(message, null)), hover)
				);
	}
	
	public void AddNewLine(String message) {
		this.message.addExtra(new TextComponent("\n" + Lang.Parse(message, null)));
	}
	
	public void AddNewLine(String message, String hover) {
		this.message.addExtra(
				SetHoverText(new TextComponent("\n" + Lang.Parse(message, null)), hover)
				);
	}

}

