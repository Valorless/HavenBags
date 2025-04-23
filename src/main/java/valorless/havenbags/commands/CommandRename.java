package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandRename {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
    	placeholders.add(new Placeholder("%bag-content-title%", null));
		ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
		Data data = null;
    	if(HavenBags.IsBag(hand)){
    		if(BagData.BagExists(HavenBags.GetBagUUID(hand))) {
    			data = BagData.GetBag(HavenBags.GetBagUUID(hand), null);
    		}
    	}
    		
		if (command.args.length >= 2){ // New Name
			ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			//player.sendMessage("has meta: " + hand.hasItemMeta());
			if(meta == null) return true;
			
			if(NBT.Has(hand, "bag-uuid")) {
				String owner = NBT.GetString(hand, "bag-owner");
				if (command.sender.hasPermission("havenbags.bypass")) {
					//Continue.
				} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(command.sender.getName()).getUniqueId().toString())) {
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-cannot-use"), (OfflinePlayer)command.sender));
					return true;
				}
				String rename = "";
				for(int i = 1; i < command.args.length; i++) { rename = rename + " " + command.args[i]; }
				rename = rename.substring(1);
				meta.setDisplayName(Lang.Parse(rename, (OfflinePlayer)command.sender));
				placeholders.add(new Placeholder("%name%", rename));
				command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-rename"), placeholders, (OfflinePlayer)command.sender));
				hand.setItemMeta(meta);
				if(data != null) data.setName(rename);
			} else {
				command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-rename"));
			}
		}else {
			ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			//player.sendMessage("has meta: " + hand.hasItemMeta());
			if(meta == null) return true;
			
			if(NBT.Has(hand, "bag-uuid")) {
				String owner = NBT.GetString(hand, "bag-owner");
				if(!owner.equalsIgnoreCase("ownerless")) {
					if (command.sender.hasPermission("havenbags.bypass")) {
						//Continue.
					} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(command.sender.getName()).getUniqueId().toString())) {
						command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-cannot-use"), (OfflinePlayer)command.sender));
						return true;
					}
				
					UUID uuid = UUID.fromString(owner);
					try {
						placeholders.add(new Placeholder("%player%", Bukkit.getPlayer(uuid).getName()));
						meta.setDisplayName(Lang.Parse(Lang.Get("bag-bound-name"), placeholders, (OfflinePlayer)command.sender));
					} catch (Exception e) {
						placeholders.add(new Placeholder("%player%", UUIDFetcher.getName(uuid)));
						meta.setDisplayName(Lang.Parse(Lang.Get("bag-bound-name"),  placeholders, (OfflinePlayer)command.sender));
					}
					hand.setItemMeta(meta);
					if(data != null) data.setName(null);
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-rename-reset"), (OfflinePlayer)command.sender));
				}
				else {

					try {
						meta.setDisplayName(Lang.Parse(Lang.Get("bag-ownerless-used"), placeholders, (OfflinePlayer)command.sender));
					} catch (Exception e) {
						meta.setDisplayName(Lang.Parse(Lang.Get("bag-ownerless-used"),  placeholders, (OfflinePlayer)command.sender));
					}
					hand.setItemMeta(meta);
					if(data != null) data.setName(null);
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-rename-reset"), (OfflinePlayer)command.sender));
				}
			} else {
				command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-cannot-rename"), (OfflinePlayer)command.sender));
			}
		}
		return true;
	}
}
