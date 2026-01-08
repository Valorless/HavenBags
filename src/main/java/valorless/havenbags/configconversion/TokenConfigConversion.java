package valorless.havenbags.configconversion;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.annotations.NotNull;
import valorless.valorlessutils.ValorlessUtils.Log;
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
public class TokenConfigConversion {
	
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
		if(config.GetInt("config-version") < 5) {
    		Log.Warning(Main.plugin, "Old configuration found, updating configs!");
    		config.Set("config-version", 5);
    		config.SaveConfig();
    		
    		if(config.HasKey("skin-token.material")) {
				config.Set("token.skin.material", config.Get("skin-token.material"));
				config.Set("token.skin.custommodeldata", config.Get("skin-token.custommodeldata"));
				config.Set("token.skin.displayname", config.Get("skin-token.display-name"));
				config.Set("token.skin.lore", config.Get("skin-token.lore"));
				
				config.Set("skin-token.display-name", null);
				config.Set("skin-token.material", null);
				config.Set("skin-token.custommodeldata", null);
				config.Set("skin-token.lore", null);
				config.Set("skin-token", null);
				
				config.SaveConfig();
			}
			Log.Info(Main.plugin, "Configuration update complete!");
    	}
	}
}