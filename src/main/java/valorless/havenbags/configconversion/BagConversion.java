package valorless.havenbags.configconversion;

import java.io.File;
import java.io.FilenameFilter;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.uuid.UUIDFetcher;

/**
 * Performs migration of legacy HavenBags player bag directories to the current
 * format. When a configuration with an outdated {@code config-version} is
 * detected, this converter will:
 * <ul>
 *   <li>Update {@code config-version} to {@code 2} and persist the change.</li>
 *   <li>Scan the plugin's {@code /bags} data directory.</li>
 *   <li>Rename each player-named directory to the player's UUID using
 *       {@link UUIDFetcher#getUUID(String)}. The {@code ownerless} directory is
 *       skipped.</li>
 *   <li>Log progress, warnings, and errors during the conversion.</li>
 * </ul>
 *
 * Notes and caveats:
 * <ul>
 *   <li>This operation performs filesystem I/O and may be slow on large data
 *       sets.</li>
 *   <li>Directory renames may fail on some platforms; failures are logged and
 *       may require manual intervention.</li>
 *   <li>Invocation is intended for internal startup-time checks only.</li>
 * </ul>
 */
public class BagConversion {

	/**
	 * Checks the provided configuration for an outdated {@code config-version}
	 * and, if necessary, migrates bag directories from player-name based folders
	 * to UUID-based folders. The configuration's version is upgraded to {@code 2}
	 * and saved before directory migrations begin.
	 *
	 * Side effects:
	 * <ul>
	 *   <li>Updates and saves the supplied configuration.</li>
	 *   <li>Renames directories under {@code <dataFolder>/bags} where applicable.</li>
	 *   <li>Emits log messages for progress, warnings, and errors.</li>
	 * </ul>
	 *
	 * Thread-safety: not thread-safe. Intended to be called during plugin
	 * initialization before concurrent access to the data folder occurs.
	 *
	 * @param config the plugin configuration holding the {@code config-version}
	 *               key; must be non-null and writable
	 */
	@DoNotCall("Internal Use Only")
	public static void check(@NotNull Config config) {
		if(config.GetInt("config-version") < 2) {
			Log.Warning(Main.plugin, "Old configuration found, updating bag data!");
			config.Set("config-version", 2);
			config.SaveConfig();

			File file = new File(String.format("%s/bags", Main.plugin.getDataFolder()));
			String[] directories = file.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});

			for(String folder : directories) {
				if(folder.equalsIgnoreCase("ownerless")) continue;
				try {
					File f = new File(String.format("%s/bags/%s", Main.plugin.getDataFolder(), folder));
					File to = new File(String.format("%s/bags/%s", Main.plugin.getDataFolder(), UUIDFetcher.getUUID(folder)));
					f.renameTo(to);
					Log.Warning(Main.plugin, String.format("%s => %s", 
							String.format("/bags/%s", folder), 
							String.format("/bags/%s", UUIDFetcher.getUUID(folder))
							));
				} catch(Exception e) {
					Log.Error(Main.plugin, String.format("Failed to convert %s, may require manual update.", String.format("/bags/%s", folder)));
				}
			}
		}	
	}
}