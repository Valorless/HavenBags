package valorless.havenbags;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomModelData {
	static String Name = "§7[§aHaven§bTweaks§7]§r";

	public static void Main(Player player, int value) {
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();
		if(meta == null) {
			player.sendMessage(Name + " Hand cannot be empty!");
			return;
		}
		meta.setCustomModelData(value);
		item.setItemMeta(meta);
		if(meta.hasDisplayName()) {
			player.sendMessage(Name + String.format(" CustomModelData of item §e%s§r has been set to §e%s§r.", meta.getDisplayName(), value));
		}
		else
		{
			player.sendMessage(Name + String.format(" CustomModelData of item §e%s§r has been set to §e%s§r.", item.getType().toString(), value));
		}
	}
}
