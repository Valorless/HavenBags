package valorless.havenbags.configconversion;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Performs in-place restructuring of the HavenBags configuration when an older
 * {@code config-version} is detected. This converter currently upgrades
 * configurations to version {@code 6} by moving keys to new hierarchical
 * paths and removing deprecated entries.
 * <p>
 * Safety: Before any change is applied, a backup of the original configuration
 * file is created alongside the file using the pattern
 * {@code <name>.backup-<build><ext>} so administrators can revert if needed.
 * </p>
 * <p>
 * Usage: This class is invoked internally during plugin startup and should not
 * be called directly from user code.
 * </p>
 */
public class CV6_ConfigRestructure {
	
	/**
	 * Checks the provided config and, if needed, performs a one-shot migration to
	 * {@code config-version = 6}. The migration:
	 * <ul>
	 *   <li>Creates a backup copy of the existing config file.</li>
	 *   <li>Renames legacy flat keys to new nested keys (e.g.,
	 *       {@code auto-save-interval} to {@code auto-save.interval}).</li>
	 *   <li>Groups sound settings under {@code sound.*} and auto-pickup settings
	 *       under {@code auto-pickup.*} with structured subkeys.</li>
	 *   <li>Removes deprecated keys after transferring their values.</li>
	 *   <li>Saves the updated configuration and logs progress.</li>
	 * </ul>
	 * If the backup fails, the restructure is aborted to avoid destructive changes.
	 *
	 * @param config the configuration to migrate; must not be null
	 */
	@DoNotCall("Internal Use Only")
	public static void check(@NotNull Config config) {
		if(config.getInt("config-version") < 6) {
    		Log.warning(Main.plugin, "Old configuration found, updating configs!");
    		
    		String ver = Main.plugin.getDescription().getVersion();
    		//Log.Debug(plugin, ver);
    		String[] split = ver.split("[.]");
    		//int major = Integer.valueOf(split[0]);
    		//int minor = Integer.valueOf(split[1]);
    		//int hotfix = Integer.valueOf(split[2]);
    		int build = Integer.parseInt(split[3]);
    		
    		// Create a backup of the current config before making changes
            try {
                File original = config.getFile().getFile();
                File parent = original.getParentFile();
                String name = original.getName();
                String base = name;
                String ext = "";
                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    base = name.substring(0, dot);
                    ext = name.substring(dot); // includes dot
                }
                String backupName = base + ".backup-" + build + ext;
                File backup = new File(parent, backupName);
                Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Log.info(Main.plugin, "Backed up config to: " + backup.getName());
            } catch (IOException ex) {
                Log.error(Main.plugin, "Failed to backup config before restructure: " + ex.getMessage());
                return; // Abort restructure if backup fails
            }
    		
            // Start restructuring
    		config.set("config-version", 6);
    		config.saveConfig();
    		
    		if(config.hasKey("auto-save-interval")) {
				config.set("auto-save.interval", config.get("auto-save-interval"));
				config.set("auto-save-interval", null);
			}
    		
    		if(config.hasKey("auto-save-message")) {
				config.set("auto-save.message", config.get("auto-save-message"));
				config.set("auto-save-message", null);
			}
    		
    		if(config.hasKey("bag-type")) {
				config.set("bag.type", config.get("bag-type"));
				config.set("bag.texture", config.get("bag-texture"));
				config.set("bag.material", config.get("bag-material"));
				config.set("bag.modeldata", config.get("bag-custom-model-data"));
				config.set("bag.itemmodel", config.get("bag-item-model"));
				config.set("bag-type", null);
				config.set("bag-texture", null);
				config.set("bag-material", null);
				config.set("bag-custom-model-data", null);
				config.set("bag-item-model", null);
			}
    		
    		if(config.hasKey("open-sound")) {
				config.set("sound.open.key", config.get("open-sound"));
				config.set("sound.open.volume", config.get("open-volume"));
				config.set("sound.open.pitch", config.get("open-pitch"));
				config.set("open-sound", null);
				config.set("open-volume", null);
				config.set("open-pitch", null);
			}
    		
    		if(config.hasKey("close-sound")) {
				config.set("sound.close.key", config.get("close-sound"));
				config.set("sound.close.volume", config.get("close-volume"));
				config.set("sound.close.pitch", config.get("close-pitch"));
				config.set("close-sound", null);
				config.set("close-volume", null);
				config.set("close-pitch", null);
			}
    		
    		if(config.hasKey("inventory-full-sound")) {
				config.set("sound.inventory-full.key", config.get("inventory-full-sound"));
				config.set("sound.inventory-full.volume", config.get("inventory-full-volume"));
				config.set("sound.inventory-full.pitch", config.get("inventory-full-pitch"));
				config.set("inventory-full-sound", null);
				config.set("inventory-full-volume", null);
				config.set("inventory-full-pitch", null);
			}
    		
    		if(config.hasKey("auto-pickup")) {
    			boolean autoPickup = config.getBool("auto-pickup"); //<-- not working, sets false
    			new org.bukkit.scheduler.BukkitRunnable() {
    			    @Override
    			    public void run() {
    					config.set("auto-pickup", null);
    					config.set("auto-pickup.enabled", autoPickup);
    					config.set("auto-pickup.sound.key", config.get("auto-pickup-sound"));
    					config.set("auto-pickup.sound.volume", config.get("auto-pickup-volume"));
    					config.set("auto-pickup.sound.pitch.min", config.get("auto-pickup-pitch-min"));
    					config.set("auto-pickup.sound.pitch.max", config.get("auto-pickup-pitch-max"));
    					config.set("auto-pickup.inventory.enabled", config.get("auto-pickup-inventory.enabled"));
    					config.set("auto-pickup.inventory.events.onBlockBreak", config.get("auto-pickup-inventory.events.onBlockBreak"));
    					config.set("auto-pickup.inventory.events.onItemPickup", config.get("auto-pickup-inventory.events.onItemPickup"));
    					
    					config.set("auto-pickup-sound", null);
    					config.set("auto-pickup-volume", null);
    					config.set("auto-pickup-pitch-min", null);
    					config.set("auto-pickup-pitch-max", null);
    					config.set("auto-pickup-inventory.enabled", null);
    					config.set("auto-pickup-inventory.events.onBlockBreak", null);
    					config.set("auto-pickup-inventory.events.onItemPickup", null);
    					config.set("auto-pickup-inventory.events", null);
    					config.set("auto-pickup-inventory", null);
    			    }
    			}.runTaskLater(Main.plugin, 1L);
			}
    		
    		if(config.hasKey("inventory-lock")) {
    			boolean locked = config.getBool("inventory-lock"); //<-- not working, sets false
    			new org.bukkit.scheduler.BukkitRunnable() {
    			    @Override
    			    public void run() {
    					config.set("inventory-lock", null);
    					config.set("inventory-lock.enabled", locked);
    					config.set("inventory-lock.unbound", false);
    					config.set("inventory-lock.bound", true);
    					config.set("inventory-lock.unused", false);
    					config.set("inventory-lock.used", true);
    			    }
    			}.runTaskLater(Main.plugin, 1L);
			}
    		
			config.saveConfig();
			Log.info(Main.plugin, "Configuration update complete!");
    	}
	}
}