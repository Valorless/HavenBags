package valorless.havenbags.commands.debug;

import valorless.havenbags.Database;
import valorless.havenbags.commands.HBCommand;

public class DebugHandler {
    public static boolean debug(HBCommand command) {
        if(command.args.length < 2) return true;
        if(!"debug".equalsIgnoreCase(command.args[0])) return true;
        // Don't proceed if the database isn't ready.
        if(!Database.isReady()) return true;
        String debug = command.args[1];

        // Debug commands
        if(debug.equalsIgnoreCase("resetTooltipStyle") && command.sender.isOp()) {
            return CommandResetTooltipStyle.Run(command);
        }

        // return true to not trigger "Unknown command".
        return true;
    }
}
