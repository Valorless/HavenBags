package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.features.CustomBags;
import valorless.havenbags.items.BagItemFactory;
import valorless.valorlessutils.logging.Log;

public class CommandGive {

	final static String Name = "§7[§aHaven§bBags§7]§r";
	static String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";

	public static boolean Run(HBCommand command) {

		ItemStack bagItem = new ItemStack(Material.DIRT);
		bagTexture = Main.config.getString("bag.texture");
		Player receiver = Bukkit.getPlayer(command.args[1]);
		int size;

		if(command.args[2].equalsIgnoreCase("ethereal")) {
			return CommandGiveEthereal.Run(command);
		}

		try {
			size = Integer.parseInt(command.args[2]);
		}catch(Exception e) {
			try {
				size = Integer.parseInt(command.args[3]);
			}catch(Exception E) {
				try {
					CustomBags.Give(receiver, command.args[2]);
					return true;
				}catch(Exception ex) {
					ex.printStackTrace();
					return false;
				}
			}
		}

        List<Placeholder> placeholders = new ArrayList<Placeholder>();
        if(command.args[2].equalsIgnoreCase("ownerless")) {
            if (command.args.length >= 3){
                //size = Utils.Clamp(Integer.parseInt(command.args[3]), 1, 6);
                size = Integer.parseInt(command.args[3]);
                int slots = HavenBags.findClosestNine(size);

                bagItem = BagItemFactory.createBagItem(false, slots, receiver);
                receiver.getInventory().addItem(bagItem);
                placeholders.add(new Placeholder("%name%", Lang.Get("bag-ownerless-unused")));
                receiver.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-given"), placeholders));
                Log.debug(Main.plugin, "[DI-140] " + String.format("Bag created: %s %s %s %s (ownerless)", "null", "null", size, "false"));
                //sender.sendMessage(JsonUtils.toJson(bagItem));
            }else {
                command.sender.sendMessage(Lang.Get("prefix") + Lang.Get("bag-ownerless-no-size"));
            }
        }
        else {
            try{
                //size = Utils.Clamp(Integer.parseInt(command.args[2]), 1, 6);
                size = Integer.parseInt(command.args[2]);
                int slots = HavenBags.findClosestNine(size);

                bagItem = BagItemFactory.createBagItem(false, slots, receiver);

                receiver.getInventory().addItem(bagItem);
                placeholders.add(new Placeholder("%name%", Lang.Get("bag-unbound-name")));
                receiver.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("bag-given"), placeholders));
                //receiver.sendMessage(Lang.Get("prefix") + Lang.Get("bag-given", Lang.Get("bag-unbound-name")));
                //sender.sendMessage(JsonUtils.toJson(bagItem));
                Log.debug(Main.plugin, "[DI-141] " + String.format("Bag created: %s %s %s %s", "null", "null", size, "true"));
            }
            catch (NumberFormatException ex){
                ex.printStackTrace();
                placeholders.add(new Placeholder("%value%", command.args[2]));
                command.sender.sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("number-conversion-error"), placeholders));
            }
        }
        return true;
	}
}
