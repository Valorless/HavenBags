package valorless.havenbags.configconversion;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// Not Ready for use, placeholder for future config restructures

public class ConfigRestructure {
	
	@DoNotCall("Internal Use Only")
	public static void check(@NotNull Config config) {
		if(config.GetInt("config-version") < 6) {
    		Log.Warning(Main.plugin, "Old configuration found, updating configs!");
    		
    		// Create a backup of the current config before making changes
            try {
                File original = config.GetFile().getFile();
                File parent = original.getParentFile();
                String name = original.getName();
                String base = name;
                String ext = "";
                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    base = name.substring(0, dot);
                    ext = name.substring(dot); // includes dot
                }
                String backupName = base + ".backup-" + Main.build + ext;
                File backup = new File(parent, backupName);
                Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Log.Info(Main.plugin, "Backed up config to: " + backup.getName());
            } catch (IOException ex) {
                Log.Error(Main.plugin, "Failed to backup config before restructure: " + ex.getMessage());
                return; // Abort restructure if backup fails
            }
    		
            // Start restructuring
    		config.Set("config-version", 6);
    		config.SaveConfig();
    		
    		if(config.HasKey("auto-save-interval")) {
				config.Set("auto-save.interval", config.Get("auto-save-interval"));
				config.Set("auto-save-interval", null);
			}
    		
    		if(config.HasKey("auto-save-message")) {
				config.Set("auto-save.message", config.Get("auto-save-message"));
				config.Set("auto-save-message", null);
			}
    		
    		if(config.HasKey("bag-type")) {
				config.Set("bag.type", config.Get("bag-type"));
				config.Set("bag.texture", config.Get("bag-texture"));
				config.Set("bag.material", config.Get("bag-material"));
				config.Set("bag.modeldata", config.Get("bag-custom-model-data"));
				config.Set("bag.itemmodel", config.Get("bag-item-model"));
				config.Set("bag-type", null);
				config.Set("bag-texture", null);
				config.Set("bag-material", null);
				config.Set("bag-custom-model-data", null);
				config.Set("bag-item-model", null);
			}
    		
    		if(config.HasKey("open-sound")) {
				config.Set("sound.open.key", config.Get("open-sound"));
				config.Set("sound.open.volume", config.Get("open-volume"));
				config.Set("sound.open.pitch", config.Get("open-pitch"));
				config.Set("open-sound", null);
				config.Set("open-volume", null);
				config.Set("open-pitch", null);
			}
    		
    		if(config.HasKey("close-sound")) {
				config.Set("sound.close.key", config.Get("close-sound"));
				config.Set("sound.close.volume", config.Get("close-volume"));
				config.Set("sound.close.pitch", config.Get("close-pitch"));
				config.Set("close-sound", null);
				config.Set("close-volume", null);
				config.Set("close-pitch", null);
			}
    		
    		if(config.HasKey("inventory-full-sound")) {
				config.Set("sound.inventory-full.key", config.Get("inventory-full-sound"));
				config.Set("sound.inventory-full.volume", config.Get("inventory-full-volume"));
				config.Set("sound.inventory-full.pitch", config.Get("inventory-full-pitch"));
				config.Set("inventory-full-sound", null);
				config.Set("inventory-full-volume", null);
				config.Set("inventory-full-pitch", null);
			}
    		
    		if(config.HasKey("auto-pickup")) {
    			boolean autoPickup = config.GetBool("auto-pickup");
				config.Set("auto-pickup", null);
				config.Set("auto-pickup.enabled", autoPickup);
				config.Set("auto-pickup.sound.key", config.Get("auto-pickup-sound"));
				config.Set("auto-pickup.sound.volume", config.Get("auto-pickup-volume"));
				config.Set("auto-pickup.sound.pitch.min", config.Get("auto-pickup-pitch-min"));
				config.Set("auto-pickup.sound.pitch.max", config.Get("auto-pickup-pitch-max"));
				config.Set("auto-pickup.inventory.enabled", config.Get("auto-pickup-inventory.enabled"));
				config.Set("auto-pickup.inventory.events.onBlockBreak", config.Get("auto-pickup-inventory.events.onBlockBreak"));
				config.Set("auto-pickup.inventory.events.onItemPickup", config.Get("auto-pickup-inventory.events.onItemPickup"));
				
				config.Set("auto-pickup-sound", null);
				config.Set("auto-pickup-volume", null);
				config.Set("auto-pickup-pitch-min", null);
				config.Set("auto-pickup-pitch-max", null);
				config.Set("auto-pickup-inventory.enabled", null);
				config.Set("auto-pickup-inventory.events.onBlockBreak", null);
				config.Set("auto-pickup-inventory.events.onItemPickup", null);
			}
			
			config.SaveConfig();
			Log.Info(Main.plugin, "Configuration update complete!");
    	}
	}
}