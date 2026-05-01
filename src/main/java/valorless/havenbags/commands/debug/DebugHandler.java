package valorless.havenbags.commands.debug;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import valorless.havenbags.BagData;
import valorless.havenbags.commands.HBCommand;
import valorless.havenbags.commands.fun.CommandExplode;
import valorless.havenbags.gui.PlayerGUI;
import valorless.havenbags.commands.CommandAutoSort;
import valorless.havenbags.commands.CommandAutopickup;
import valorless.havenbags.commands.CommandClearContent;
import valorless.havenbags.commands.CommandConvertDatabase;
import valorless.havenbags.commands.CommandConvertEpicBackpacks;
import valorless.havenbags.commands.CommandConvertMinepacks;
import valorless.havenbags.commands.CommandCreate;
import valorless.havenbags.commands.CommandCustomContent;
import valorless.havenbags.commands.CommandEffect;
import valorless.havenbags.commands.CommandEmpty;
import valorless.havenbags.commands.CommandEthereal;
import valorless.havenbags.commands.CommandGUI;
import valorless.havenbags.commands.CommandGive;
import valorless.havenbags.commands.CommandInfo;
import valorless.havenbags.commands.CommandItemModel;
import valorless.havenbags.commands.CommandMagnet;
import valorless.havenbags.commands.CommandMod;
import valorless.havenbags.commands.CommandModelData;
import valorless.havenbags.commands.CommandOpen;
import valorless.havenbags.commands.CommandPreview;
import valorless.havenbags.commands.CommandRawInfo;
import valorless.havenbags.commands.CommandRefill;
import valorless.havenbags.commands.CommandReload;
import valorless.havenbags.commands.CommandReloadPlugin;
import valorless.havenbags.commands.CommandRename;
import valorless.havenbags.commands.CommandRestore;
import valorless.havenbags.commands.CommandTexture;
import valorless.havenbags.commands.CommandToken;
import valorless.havenbags.commands.CommandTrust;
import valorless.havenbags.commands.CommandUntrust;
import valorless.havenbags.commands.CommandWeight;

import valorless.valorlessutils.ValorlessUtils.Log;

public class DebugHandler {
    public static boolean debug(HBCommand command) {
        if(command.args.length < 2) return true;
        if(!"debug".equalsIgnoreCase(command.args[0])) return true;
        // Don't proceed if the database isn't ready.
        if(!BagData.isReady()) return true;
        String debug = command.args[1];

        // Debug commands
        if(debug.equalsIgnoreCase("resetTooltipStyle") && command.sender.isOp()) {
            return CommandResetTooltipStyle.Run(command);
        }

        // return true to not trigger "Unknown command".
        return true;
    }
}
