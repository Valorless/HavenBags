package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandClearContent {
	
	// This command is only runnable from the console for safety reasons.
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {

		if (command.args.length == 2){
			String target = command.args[1];
			Data bag = BagData.GetBag(target, null);
			if(bag != null) {
				return BagData.ClearBagContent(target);
			}
			else if(target.equalsIgnoreCase("all")) {
				return BagData.ClearAllBagContents();
			}
			else if(Bukkit.getPlayer(target) != null) {
				Player player = Bukkit.getPlayer(target);
				return BagData.ClearBagContentPlayer(player.getUniqueId().toString());
			}
			else {
				Log.Error(Main.plugin, "Unable to find the targetted bag(s) to clear.");
				return true;
			}
		}
		else {
			Log.Error(Main.plugin, "/havenbags clearcontent <all/player/bag-uuid>");
			return true;
		}
		
	}
}
