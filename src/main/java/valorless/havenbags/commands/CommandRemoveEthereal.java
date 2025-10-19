package valorless.havenbags.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.database.EtherealBags;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandRemoveEthereal {
	
	public static boolean Run(HBCommand command) {
		// -1  0         1      2       3
		// bag ethereal remove <player> <id>
		OfflinePlayer target;
		String bagId;
		
		if(command.args[1].equalsIgnoreCase("remove") && command.sender.hasPermission("havenbags.ethereal.remove")) {
			if(command.args.length < 4) {
				command.sender.sendMessage("§cUsage: /bag ethereal remove <player> <id>");
				return true;
			}
			
			try {
				List<OfflinePlayer> offlinePlayers = Arrays.asList(Bukkit.getOfflinePlayers());
				target = offlinePlayers.stream()
						.filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(command.args[2]))
						.findFirst()
						.orElse(null);
				bagId = command.args[3];
			}catch(Exception e) {
				command.sender.sendMessage("§cUsage: /bag ethereal remove <player> <id>");
				return true;
			}
			
			if(target == null) {
				command.sender.sendMessage("§cPlayer not found.");
				return true;
			}
			if(!EtherealBags.hasBag(target.getUniqueId(), bagId)) {
				command.sender.sendMessage(Lang.Parse(Lang.Get("prefix") + Lang.Get("bag-does-not-exist"), null));
				return true;
			}
			if(EtherealBags.removeBag(target.getUniqueId(), bagId)){
				command.sender.sendMessage(Lang.Parse(Lang.Get("prefix") + "Ethereal bag removed.", null));
				Log.Info(Main.plugin, "[CommandEthereal][DI-305] " + command.sender.getName() + " removed ethereal bag " + bagId + " from " + target.getName());
			}else {
				command.sender.sendMessage(Lang.Parse(Lang.Get("prefix") + "§cError removing ethereal bag.", null));
				Log.Error(Main.plugin, "[CommandEthereal][DI-306] " + "Error removing ethereal bag " + bagId + " from " + target.getName());
				
			}
			return true;
		}
		return true;
	}
}
