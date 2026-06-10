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
		this.message = new TextComponent(Lang.parse(message, null));
	}
	
	public Message(String message, ChatColor color) {
		this.message = new TextComponent(Lang.parse(message, null));
		setColor(color);
	}
	
	public Message(String message, String hover) {
		this.message = new TextComponent(Lang.parse(message, null));
		sethovertext(this.message, hover);
	}
	
	public Message(ChatMessageType type, String message) {
		this.message = new TextComponent(Lang.parse(message, null));
		this.type = type;
	}
	
	public Message(ChatMessageType type, String message, ChatColor color) {
		this.message = new TextComponent(Lang.parse(message, null));
		setColor(color);
		this.type = type;
	}
	
	public Message(ChatMessageType type, String message, String hover) {
		this.message = new TextComponent(Lang.parse(message, null));
		sethovertext(this.message, hover);
		this.type = type;
	}
	
	public void send(Player player) {
		if(type == null) {
			player.spigot().sendMessage(message);
		}
		else {
			player.spigot().sendMessage(type, message);
		}
	}
	
	public void setColor(ChatColor color) {
		message.setColor(color);
	}
	
	@SuppressWarnings("deprecation")
	TextComponent sethovertext(TextComponent comp, String message) {
		comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Lang.parse(message, null)).create()));
		return comp;
	}

	@SuppressWarnings("deprecation")
	public void sethovertext(String message) {
		this.message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Lang.parse(message, null)).create()));
	}
	
	public void addTextPart(String message) {
		this.message.addExtra(new TextComponent(Lang.parse(message, null)));
	}
	
	public void addTextPart(String message, String hover) {
		this.message.addExtra(
				sethovertext(new TextComponent(Lang.parse(message, null)), hover)
				);
	}
	
	public void addNewLine(String message) {
		this.message.addExtra(new TextComponent("\n" + Lang.parse(message, null)));
	}
	
	public void addNewLine(String message, String hover) {
		this.message.addExtra(
				sethovertext(new TextComponent("\n" + Lang.parse(message, null)), hover)
				);
	}
	
	public void addClickEvent(String command) {
		message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, command));
	}
	
	public void addSuggestEvent(String command) {
		message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, command));
	}
	
	public void addOpenUrlEvent(String url) {
		message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, url));
	}
	
	public void addCopyToClipboardEvent(String text) {
		message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.COPY_TO_CLIPBOARD, text));
	}
	

}

