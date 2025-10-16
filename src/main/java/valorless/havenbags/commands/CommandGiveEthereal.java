package valorless.havenbags.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.events.BagEtherealCreateEvent;

public class CommandGiveEthereal {
	
	public static boolean Run(HBCommand command) {
		//     0    1      2        3 4
		// bag give PLAYER ethereal 3 id
		
		String playerName = command.args[1];
		List<OfflinePlayer> offlinePlayers = Arrays.asList(Bukkit.getOfflinePlayers());
		OfflinePlayer target = offlinePlayers.stream()
				.filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
				.findFirst()
				.orElse(null);
		Integer size = Integer.valueOf(command.args[3]);
		String id = command.args[4];
		
		if(EtherealBags.addBag(target.getPlayer().getUniqueId(), id, size)){
			command.sender.sendMessage("Gave " + target.getName() + " an ethereal bag with ID " + id);
			Bukkit.getPluginManager().callEvent(
		    		new BagEtherealCreateEvent(target.getPlayer(), command.sender, id, 
		    				EtherealBags.formatBagId(target.getUniqueId(), id), EtherealBags.getBagSettings(target.getUniqueId(), id))
			);
		}else {
			command.sender.sendMessage("An ethereal bag with ID " + id + " already exists for " + target.getName());
		}

		return true;
	}
}
