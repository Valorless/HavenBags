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
 * Migrates {@code config.yml} in-place to {@code config-version = 7}.
 *
 * <p>This converter is executed during plugin startup when an older
 * {@code config-version} is detected. It performs a small restructure of a
 * couple of legacy flat sections by converting them into structured sections.
 * Currently affected settings:</p>
 *
 * <ul>
 *   <li>{@code hardcore-bags} is converted into a section containing:
 *       {@code enabled}, {@code unbound}, {@code bound}, {@code unused}, and
 *       {@code used}.</li>
 *   <li>{@code protect-bags} is converted into a section containing:
 *       {@code enabled}, {@code unbound}, {@code bound}, {@code unused}, and
 *       {@code used}.</li>
 * </ul>
 *
 * <p><strong>Safety:</strong> Before any changes are written, a backup of the
 * original configuration file is created next to the file using the pattern
 * {@code <name>.backup-<build><ext>} (where {@code build} is taken from the
 * plugin version string). If the backup fails, the migration is aborted to
 * avoid destructive changes.</p>
 *
 * <p><strong>Note:</strong> Some setters are deferred by one tick using a Bukkit
 * task to avoid potential timing issues with the underlying configuration IO.
 * The migration still completes during startup.</p>
 */
public class CV7_ConfigRestructure {
	
	/**
	 * Checks the given configuration and performs a one-time migration to
	 * {@code config-version = 7} when required.
	 *
	 * <p>The migration will:</p>
	 * <ol>
	 *   <li>Create/update a backup copy of the existing config file.</li>
	 *   <li>Set {@code config-version} to {@code 7}.</li>
	 *   <li>If {@code hardcore-bags} exists, remove the legacy value and create the
	 *       new {@code hardcore-bags.*} keys with defaults.</li>
	 *   <li>If {@code protect-bags} exists, remove the legacy value and create the
	 *       new {@code protect-bags.*} keys with defaults.</li>
	 *   <li>Save the updated configuration.</li>
	 * </ol>
	 *
	 * <p>This method is intended for internal use during startup only and should
	 * not be called directly.</p>
	 *
	 * @param config configuration wrapper to migrate
	 */
	@DoNotCall("Internal Use Only")
	public static void check(@NotNull Config config) {
		if(config.getInt("config-version") < 7) {
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
    		config.set("config-version", 7);
    		config.saveConfig();

			config.set("hardcore-bags", null);
			config.set("hardcore-bags.enabled", false);
			config.set("hardcore-bags.unbound", true);
			config.set("hardcore-bags.bound", true);
			config.set("hardcore-bags.unused", true);
			config.set("hardcore-bags.used", true);
			
			config.set("protect-bags", null);
			config.set("protect-bags.enabled", true);
			config.set("protect-bags.unbound", true);
			config.set("protect-bags.bound", true);
			config.set("protect-bags.unused", true);
			config.set("protect-bags.used", true);
    		
			config.saveConfig();
			Log.info(Main.plugin, "Configuration update complete!");
    	}
	}
}