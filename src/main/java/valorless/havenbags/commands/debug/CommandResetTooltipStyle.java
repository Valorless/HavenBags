package valorless.havenbags.commands.debug;

import valorless.havenbags.Database;
import valorless.havenbags.commands.HBCommand;

public class CommandResetTooltipStyle {
	public static boolean Run(HBCommand command) {
		Database.resetTooltipStyles();
		command.sender.sendMessage("TooltipStyles reset");
		return true;
	}
}
