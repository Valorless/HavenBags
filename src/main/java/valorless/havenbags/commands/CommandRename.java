package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Database;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Bag;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandRename {

	public static boolean run(HBCommand command, String permission) {
		if(!command.sender.hasPermission(permission)) return false;

		List<Placeholder> placeholders = new ArrayList<Placeholder>();
    	placeholders.add(new Placeholder("%bag-content-title%", null));
		ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
		Bag data = null;
    	if(HavenBags.isBag(hand)){
    		if(Database.bagExists(HavenBags.getBagUUID(hand))) {
    			data = Database.getBag(HavenBags.getBagUUID(hand), null);
    		}
    	}
    		
		if (command.args.length >= 2){ // New Name
			ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			//player.sendMessage("has meta: " + hand.hasItemMeta());
			if(meta == null) return true;
			
			if(PDC.has(hand, "uuid")) {
				String owner = PDC.getString(hand, "owner");
				if (command.sender.hasPermission("havenbags.bypass")) {
					//Continue.
				} else if("ownerless".equalsIgnoreCase(owner)) {
					//Continue.
				} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(command.sender.getName()).getUniqueId().toString())) {
					command.sender.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("bag-cannot-use"), (OfflinePlayer)command.sender));
					return true;
				}
				String rename = "";
				for(int i = 1; i < command.args.length; i++) { rename = rename + " " + command.args[i]; }
				rename = rename.substring(1);
				meta.setDisplayName(Lang.parse(rename, (OfflinePlayer)command.sender));
				placeholders.add(new Placeholder("%name%", rename));
				command.sender.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("bag-rename"), placeholders, (OfflinePlayer)command.sender));
				hand.setItemMeta(meta);
				if(data != null) data.setName(rename);
			} else {
				command.sender.sendMessage(Lang.get("prefix") + Lang.get("bag-cannot-rename"));
			}
		}else {
			ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			//player.sendMessage("has meta: " + hand.hasItemMeta());
			if(meta == null) return true;
			
			if(PDC.has(hand, "uuid")) {
				String owner = PDC.getString(hand, "owner");
				if(!owner.equalsIgnoreCase("ownerless")) {
					if (command.sender.hasPermission("havenbags.bypass")) {
						//Continue.
					} else if("ownerless".equalsIgnoreCase(owner)) {
						//Continue.
					} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(command.sender.getName()).getUniqueId().toString())) {
						command.sender.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("bag-cannot-use"), (OfflinePlayer)command.sender));
						return true;
					}
				
					UUID uuid = UUID.fromString(owner);
					try {
						placeholders.add(new Placeholder("%player%", Bukkit.getPlayer(uuid).getName()));
						meta.setDisplayName(Lang.parse(Lang.get("bag-bound-name"), placeholders, (OfflinePlayer)command.sender));
					} catch (Exception e) {
						placeholders.add(new Placeholder("%player%", UUIDFetcher.getName(uuid)));
						meta.setDisplayName(Lang.parse(Lang.get("bag-bound-name"),  placeholders, (OfflinePlayer)command.sender));
					}
					hand.setItemMeta(meta);
					if(data != null) data.setName(null);
					command.sender.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("bag-rename-reset"), (OfflinePlayer)command.sender));
				}
				else {

					try {
						meta.setDisplayName(Lang.parse(Lang.get("bag-ownerless-used"), placeholders, (OfflinePlayer)command.sender));
					} catch (Exception e) {
						meta.setDisplayName(Lang.parse(Lang.get("bag-ownerless-used"),  placeholders, (OfflinePlayer)command.sender));
					}
					hand.setItemMeta(meta);
					if(data != null) data.setName(null);
					command.sender.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("bag-rename-reset"), (OfflinePlayer)command.sender));
				}
			} else {
				command.sender.sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("bag-cannot-rename"), (OfflinePlayer)command.sender));
			}
		}
		return true;
	}
}
