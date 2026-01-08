package valorless.havenbags.configconversion;

import java.io.File;
import java.io.FilenameFilter;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.uuid.UUIDFetcher;

public class BagConversion {

	@DoNotCall("Internal Use Only")
	public static void check(Config config) {
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
