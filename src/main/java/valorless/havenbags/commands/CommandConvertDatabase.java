package valorless.havenbags.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.havenbags.enums.DatabaseType;
import valorless.valorlessutils.ValorlessUtils.Log;

public class CommandConvertDatabase {
	
	static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		try {
			DatabaseType type = DatabaseType.get(command.args[1].toUpperCase());
			if(type == null) {
				return false;
			}
			
			BagData.ChangeDatabase(type);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
        		public void run() {
        			BagData.SaveData(false, true);
        		}
    		}, 1);
			
			Main.config.Set("save-type", type.toString().toLowerCase());
			Main.config.SaveConfig();
			
			if(!(command.sender instanceof Player)) { 
				Log.Info(Main.plugin, String.format("Database type changed to %s!", type.toString()));
			}else {
				command.sender.sendMessage(Name + String.format(" Database type changed to %s", type.toString()));
			}
			
			return true;
		}catch(Exception e) {
			if(!(command.sender instanceof Player)) { 
				Log.Error(Main.plugin, "Something failed during the change.");
			}else {
				command.sender.sendMessage(Name + " Something failed during the change, please check the console.");
			}
			e.printStackTrace();
			return true;
		}
	}
}
