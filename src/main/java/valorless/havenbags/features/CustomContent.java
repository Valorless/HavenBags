package valorless.havenbags.features;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import valorless.havenbags.Main;
import valorless.valorlessutils.config.Config;

public class CustomContent implements Listener {
	
	private static Inventory inv = Bukkit.createInventory(null, 54, "HavenBags CustomContent Editor");
	
	public static void open(@NotNull Player player) {
		player.openInventory(inv);
	}

	public static ItemStack[] getContent(){
		return inv.getContents();
	}
	
	public static void setContent(List<ItemStack> content) {
		for(int i = 0; i < content.size(); i++) {
			inv.setItem(i, content.get(i));
		}
	}
	
	public static List<ItemStack> load(String name){
		Config file = new Config(Main.plugin, String.format("/customcontent/%s.yml", name));
		List<ItemStack> content = new ArrayList<>();
		
		for(int i = 0; i < getContent().length; i++) {
			String index = ""+i;
			if(file.HasKey(index)) {
				content.add((ItemStack) file.Get(index));
			}
			else content.add(null);
		}
		return content;
	}
	
	public static void save(String name) {
		File path = new File(Main.plugin.getDataFolder() + "/customcontent");
		File path2 = new File(Main.plugin.getDataFolder() + String.format("/customcontent/%s.yml", name));
		
		if(!path.exists()) path.mkdir();
		if(!path2.exists())
			try {
				path2.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		Config file = new Config(Main.plugin, String.format("/customcontent/%s.yml", name));
		for(int i = 0; i < inv.getContents().length; i++) {
			String index = ""+i;
			file.GetFile().set(index, inv.getContents()[i]);
		}
		file.SaveConfig();
	}
}
