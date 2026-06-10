package valorless.havenbags.configconversion;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.config.Config;

/**
 * Migrates legacy token configuration keys to the current schema when an
 * outdated {@code config-version} is detected. If the version is lower than
 * {@code 5}, this converter will:
 * <ul>
 *   <li>Set {@code config-version} to {@code 5} and save the configuration.</li>
 *   <li>Move legacy {@code skin-token.*} keys to the new {@code token.skin.*}
 *       namespace (material, custommodeldata, displayname, lore).</li>
 *   <li>Remove the migrated legacy keys from the configuration.</li>
 * </ul>
 *
 * Invocation is intended for internal use during plugin initialization.
 */
public class CV5_TokenConfigConversion {
	
	/**
	 * Checks the provided configuration and, if {@code config-version < 5},
	 * updates the configuration structure by moving {@code skin-token.*} keys
	 * into {@code token.skin.*}, removes the legacy keys, and persists the
	 * changes. The configuration's {@code config-version} is set to {@code 5}
	 * before migration.
	 *
	 * Side effects:
	 * <ul>
	 *   <li>Updates and saves the supplied configuration.</li>
	 *   <li>Renames configuration keys under the token settings.</li>
	 *   <li>Emits log messages describing the migration.</li>
	 * </ul>
	 *
	 * Thread-safety: not thread-safe. Call during initialization only.
	 *
	 * @param config the non-null plugin configuration to inspect and migrate
	 */
	@DoNotCall("Internal Use Only")
	public static void check(@NotNull Config config) {
		if(config.getInt("config-version") < 5) {
    		Log.warning(Main.plugin, "Old configuration found, updating configs!");
    		config.set("config-version", 5);
    		config.saveConfig();
    		
    		if(config.hasKey("skin-token.material")) {
				config.set("token.skin.material", config.get("skin-token.material"));
				config.set("token.skin.custommodeldata", config.get("skin-token.custommodeldata"));
				config.set("token.skin.displayname", config.get("skin-token.display-name"));
				config.set("token.skin.lore", config.get("skin-token.lore"));
				
				config.set("skin-token.display-name", null);
				config.set("skin-token.material", null);
				config.set("skin-token.custommodeldata", null);
				config.set("skin-token.lore", null);
				config.set("skin-token", null);
				
				config.saveConfig();
			}
			Log.info(Main.plugin, "Configuration update complete!");
    	}
	}
}