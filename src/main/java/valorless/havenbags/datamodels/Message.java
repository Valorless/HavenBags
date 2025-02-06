package valorless.havenbags.datamodels;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import valorless.havenbags.Lang;

public class Message {
	
	public TextComponent message;
	public ChatMessageType type = null;
	
	public Message(String message) {
		this.message = new TextComponent(Lang.Parse(message, null));
	}
	
	public Message(String message, String color) {
		this.message = new TextComponent(Lang.Parse(message, null));
		SetColor(color);
	}
	
	public Message(ChatMessageType type, String message) {
		this.message = new TextComponent(Lang.Parse(message, null));
		this.type = type;
	}
	
	public Message(ChatMessageType type, String message, String color) {
		this.message = new TextComponent(Lang.Parse(message, null));
		SetColor(color);
		this.type = type;
	}
	
	public void Send(Player player) {
		if(type == null) {
			player.spigot().sendMessage(message);
		}
		else {
			player.spigot().sendMessage(type, message);
		}
	}
	
	public void SetColor(String color) {
		message.setColor(ChatColor.getByChar(color.charAt(0)));
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

