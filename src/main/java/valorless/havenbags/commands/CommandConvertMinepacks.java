package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import valorless.havenbags.BagData;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.utils.Utils;

public class CommandConvertMinepacks {
	
	static String prefix = "§7[§aHaven§bBags§7]§r ";

	public static boolean Run(HBCommand command) {
		Config config = new Config(Main.plugin, "minepacks/players.yml");
		try {
			Log.Info(Main.plugin, "Attempting to convert Minepacks data to HavenBags.");
			command.sender.sendMessage(prefix + "Attempting to convert Minepacks data to HavenBags.");
			OfflinePlayer[] players = Bukkit.getOfflinePlayers();
			//OfflinePlayer alynie = Bukkit.getOfflinePlayer(UUID.fromString("84c1800c-ecbd-4f45-8ebb-4d251a196726"));
			
			for(OfflinePlayer player : players) {
				ItemStack[] content;
				if(MinepacksPlugin.getInstance().getBackpackCachedOnly(player) == null) {
					// Player has no Minepack, skipping
					continue;
				}else {
					content = MinepacksPlugin.getInstance().getBackpackCachedOnly(player).getInventory().getContents();
				}
				if(content == null) continue; // Casual bonus check.
				Log.Info(Main.plugin, player.getName());
				List<ItemStack> cont = new ArrayList<>();
				for(ItemStack item : content) {
					cont.add(item);
				}
				
				String uuid = UUID.randomUUID().toString();
				BagData.CreateBag(uuid, 
						player.getUniqueId().toString(), 
						cont, 
						null, 
						PlaceholderBag(player));
				config.Set("players." + player.getUniqueId().toString(), uuid);
			}
			config.SaveConfig();

			Log.Info(Main.plugin, "Successfully converted Minepacks data to HavenBags.");
			command.sender.sendMessage(prefix + "§aSuccessfully converted Minepacks data to HavenBags.");
			command.sender.sendMessage("Players will get their new bag, the next time they log on.");
			return true;
		}catch(Exception e) {
			Log.Error(Main.plugin, "Something failed during convertion:");
			command.sender.sendMessage(prefix + "§cSomething failed during convertion, please check the console.");
			e.printStackTrace();
			return true;
		}
	}
	
	
	static ItemStack PlaceholderBag(OfflinePlayer owner) {
		ItemStack bagItem = new ItemStack(Material.DIRT);
		String bagTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
		bagTexture = Main.config.GetString("bag-texture");
		
		String uuid = UUID.randomUUID().toString();
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		int size = 6;
		if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
			if(Main.config.GetBool("bag-textures.enabled")) {
				for(int s = 9; s <= 54; s += 9) {
					if(size*9 == s) {
						bagItem = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-" + size*9));
					}
				}
			}else {
				bagItem = HeadCreator.itemFromBase64(bagTexture);
			}
		} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
		}
		ItemMeta bagMeta = bagItem.getItemMeta();
		if (bagMeta == null) {
			NBT.SetString(bagItem, "bag-uuid", uuid);
			bagMeta = bagItem.getItemMeta();
        }
		if(Main.config.GetInt("bag-custom-model-data") != 0) {
			bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
		}
		if(Main.config.GetBool("bag-custom-model-datas.enabled")) {
			for(int s = 9; s <= 54; s += 9) {
				if(size*9 == s) {
					bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-datas.size-" + s));
				}
			}
		}
		bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
		List<String> lore = new ArrayList<String>();
		for (String l : Lang.lang.GetStringList("bag-lore")) {
			if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, null));
		}
		placeholders.add(new Placeholder("%size%", size*9));
        lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders));
		//for (String l : Lang.lang.GetStringList("bag-size")) {
		//	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size*9), (Player)sender));
		//}
		bagMeta.setLore(lore);
		bagItem.setItemMeta(bagMeta);
		//NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
		NBT.SetString(bagItem, "bag-uuid", uuid);
		NBT.SetString(bagItem, "bag-owner", owner.getUniqueId().toString());
		NBT.SetInt(bagItem, "bag-size", size*9);
		NBT.SetBool(bagItem, "bag-canBind", true);
		//Bukkit.getPlayer(command.sender.getName()).getInventory().addItem(bagItem);
		//Log.Debug(Main.plugin, "[DI-139] " + String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
		
		return bagItem;
	}
}
