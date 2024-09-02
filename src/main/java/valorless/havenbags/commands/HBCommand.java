package valorless.havenbags.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HBCommand {
	
	public final CommandSender sender;
	public final Command command;
	public final String label;
	public final String[] args;
	
	public HBCommand(CommandSender sender, Command command, String label, String[] args) {
		this.sender = sender;
		this.command = command;
		this.label = label;
		this.args = args;
	}

}
