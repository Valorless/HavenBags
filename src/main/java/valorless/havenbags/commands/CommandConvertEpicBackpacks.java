package valorless.havenbags.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.havenbags.BagData;
import valorless.havenbags.Lang;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.utils.HeadCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.utils.Utils;

public class CommandConvertEpicBackpacks {
	
	static String prefix = "§7[§aHaven§bBags§7]§r ";

	public static boolean Run(HBCommand command) {
		Config config = new Config(Main.plugin, "epicbackpacks/players.yml");
		try {
			Log.Info(Main.plugin, "Attempting to convert EpicBackpacks data to HavenBags.");
			command.sender.sendMessage(prefix + "Attempting to convert EpicBackpacks data to HavenBags.");
			
			/*
			Get the data directly from the EpicBackpack's Datafolder/backpacks.
			Files in /backpacks are names player's UUIDs.
			Get json item stacks from slots inside files.
			Example:
				'22':
				  ==: org.bukkit.inventory.ItemStack
				  v: 3700
				  type: DIAMOND
			*/

			Plugin ebp = Bukkit.getPluginManager().getPlugin("EpicBackpacks");
			List<String> files = getFiles(new File(ebp.getDataFolder(), "backpacks"));
			
			for(String file : files) {
				Config data = new Config((JavaPlugin) ebp, String.format("backpacks/%s.yml", file));
				OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(file));
				List<ItemStack> cont = new ArrayList<>(54);
				for(int i = 0; i < 54; i++) { cont.add(null); } // Placeholder slots
				for(int i = 0; i < 54; i++) {
					if(data.HasKey(String.valueOf(i))) {
						ItemStack item = data.GetFile().getConfig().getItemStack(String.valueOf(i));
						//Log.Debug(Main.plugin, item.toString());
						cont.set(i,item);
					}
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

			Log.Info(Main.plugin, "Successfully converted EpicBackpacks data to HavenBags.");
			command.sender.sendMessage(prefix + "§aSuccessfully converted EpicBackpacks data to HavenBags.");
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
		bagTexture = Main.config.GetString("bag.texture");
		
		String uuid = UUID.randomUUID().toString();
		
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		int size = 6;
		if(Main.config.GetString("bag.type").equalsIgnoreCase("HEAD")){
			if(Main.config.GetBool("bag-textures.enabled")) {
				for(int s = 9; s <= 54; s += 9) {
					if(size*9 == s) {
						bagItem = HeadCreator.itemFromBase64(Main.config.GetString("bag-textures.size-" + size*9));
					}
				}
			}else {
				bagItem = HeadCreator.itemFromBase64(bagTexture);
			}
		} else if(Main.config.GetString("bag.type").equalsIgnoreCase("ITEM")) {
			bagItem = new ItemStack(Main.config.GetMaterial("bag.material"));
		}
		ItemMeta bagMeta = bagItem.getItemMeta();
		if (bagMeta == null) {
			PDC.SetString(bagItem, "uuid", uuid);
			bagMeta = bagItem.getItemMeta();
        }
		if(Main.config.GetInt("bag.modeldata") != 0) {
			bagMeta.setCustomModelData(Main.config.GetInt("bag.modeldata"));
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
		//PDC.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
		PDC.SetString(bagItem, "uuid", uuid);
		PDC.SetString(bagItem, "owner", owner.getUniqueId().toString());
		PDC.SetInteger(bagItem, "size", size*9);
		PDC.SetBoolean(bagItem, "binding", true);
		//Bukkit.getPlayer(command.sender.getName()).getInventory().addItem(bagItem);
		//Log.Debug(Main.plugin, "[DI-139] " + String.format("Bag created: %s %s %s %s", "null", "null", size*9, "true"));
		
		return bagItem;
	}
	
	public static List<String> getFiles(File folder) {
        List<String> fileNames = new ArrayList<>();

        // Check if the folder exists and is indeed a directory
        if (folder.exists() && folder.isDirectory()) {
            // List all files in the folder
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml")); // Only filter .yml files
            
            if (files != null) {
                for (File file : files) {
                    // Get the file name without .yml extension
                    String fileName = file.getName().replace(".yml", "");
                    fileNames.add(fileName); // Add to list
                }
            }
        } else {
            System.out.println("Folder does not exist or is not a directory.");
        }

        return fileNames;
    }
}
