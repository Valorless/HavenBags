package valorless.havenbags.commands.debug;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.commands.HBCommand;
import valorless.havenbags.datamodels.Sound;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.ValorlessUtils.Log;

import java.util.List;
import java.util.Random;

public class CommandResetTooltipStyle {
	public static boolean Run(HBCommand command) {
		BagData.resetTooltipStyles();
		command.sender.sendMessage("TooltipStyles reset");
		return true;
	}
}
