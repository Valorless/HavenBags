package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.Placeholder;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class CommandRename {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		if(command != null) {
			for(String arg : command.args) {
				Log.Debug(Main.plugin, arg);
			}
		}
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
    	placeholders.add(new Placeholder("%bag-content-title%", null));
		if (command.args.length >= 2){ // New Name
			ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
			ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			//player.sendMessage("has meta: " + hand.hasItemMeta());
			if(meta == null) return false;
			
			if(NBT.Has(hand, "bag-uuid")) {
				String owner = NBT.GetString(hand, "bag-owner");
				if (command.sender.hasPermission("havenbags.bypass")) {
					//Continue.
				} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(command.sender.getName()).getUniqueId().toString())) {
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-cannot-use"), (OfflinePlayer)command.sender));
					return false;
				}
				String rename = "";
				for(int i = 1; i < command.args.length; i++) { rename = rename + " " + command.args[i]; }
				rename = rename.substring(1);
				meta.setDisplayName(Lang.Parse(rename, (OfflinePlayer)command.sender));
				placeholders.add(new Placeholder("%name%", rename));
				command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-rename"), placeholders, (OfflinePlayer)command.sender));
				hand.setItemMeta(meta);
			} else {
				command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-rename"));
			}
		}else {
			ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
			ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			//player.sendMessage("has meta: " + hand.hasItemMeta());
			if(meta == null) return false;
			
			if(NBT.Has(hand, "bag-uuid")) {
				String owner = NBT.GetString(hand, "bag-owner");
				if(!owner.equalsIgnoreCase("ownerless")) {
					if (command.sender.hasPermission("havenbags.bypass")) {
						//Continue.
					} else if (!owner.equalsIgnoreCase(Bukkit.getPlayer(command.sender.getName()).getUniqueId().toString())) {
						command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-cannot-use"), (OfflinePlayer)command.sender));
						return false;
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
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-rename-reset"), (OfflinePlayer)command.sender));
				}
				else {

					try {
						meta.setDisplayName(Lang.Parse(Lang.Get("bag-ownerless-used"), placeholders, (OfflinePlayer)command.sender));
					} catch (Exception e) {
						meta.setDisplayName(Lang.Parse(Lang.Get("bag-ownerless-used"),  placeholders, (OfflinePlayer)command.sender));
					}
					hand.setItemMeta(meta);
					command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-rename-reset"), (OfflinePlayer)command.sender));
				}
			} else {
				command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-cannot-rename"), (OfflinePlayer)command.sender));
			}
		}
		return false;
	}
}
