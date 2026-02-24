package valorless.havenbags.commands.fun;

import java.util.List;
import java.util.Random;

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

public class CommandExplode {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		Player player = (Player)command.sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(HavenBags.IsBag(item)) {
			if(BagData.IsBagOpen(item)) {
				Log.Warning(Main.plugin, "Due to a recent bug, this player may be attempting to exploit the empty command while the bag is open: " + player.getName());
				return true;
			}
			if(HavenBags.IsOwner(item, player)) {
				Explode(item, player);
			}else {
				player.sendMessage(Lang.Get("prefix") + Lang.Get("bag-cannot-use"));
			}
		}
		return true;
	}
	
	public static void Explode(ItemStack bag, Player player) {
		Random random = new Random();
		String uuid = PDC.GetString(bag, "uuid");
		List<ItemStack> content = BagData.GetBag(uuid, bag).getContent();
		Sound sound = Sound.parse("ENTITY_GENERIC_EXPLODE:1.0:1.0");
		sound.play(player);
		for(int i = 0; i < content.size(); i++) {
			try {
				if(PDC.Has(content.get(i), "locked")) continue;
				Item dropped = player.getWorld().dropItem(player.getLocation(), content.get(i));
				dropped.setPickupDelay(100);
				
		        double x = (random.nextDouble() - 0.5) * 0.6; // -0.3 to 0.3
		        double y = random.nextDouble() * 0.7; // Small upward boost (0 to 0.5)
		        double z = (random.nextDouble() - 0.5) * 0.6; // -0.3 to 0.3

		        dropped.setVelocity(new Vector(x, y, z));
				
				content.set(i, null);
			} catch (Exception e) {
				continue;
			}
		}
		BagData.UpdateBag(uuid, content);
		HavenBags.UpdateBagItem(bag, player);
	}
}
