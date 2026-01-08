package valorless.havenbags.configconversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;

/**
 * Handles migration of HavenBags per-bag data from legacy JSON files to the
 * current YAML-based format when an outdated {@code config-version} is
 * detected. When invoked against a configuration with version lower than 4,
 * this converter will:
 * <ul>
 *   <li>Update {@code config-version} to {@code 4} and persist the change.</li>
 *   <li>Enumerate bag owners and scan {@code <dataFolder>/bags/<owner>} for bag
 *       data files that are not YAML.</li>
 *   <li>Deserialize each legacy JSON file into a list of {@link ItemStack}
 *       entries and write a new YAML file with structured metadata and content.</li>
 *   <li>Log conversion progress and a final summary of converted and failed
 *       files. Legacy files are intentionally not deleted.</li>
 * </ul>
 *
 * Notes:
 * <ul>
 *   <li>Performs filesystem I/O and may take noticeable time on large data
 *       sets.</li>
 *   <li>Not thread-safe; intended to run during plugin startup before other
 *       components access the data directory.</li>
 * </ul>
 */
public class DataConversion {
	
	/**
	 * Checks the provided configuration and, if {@code config-version < 4},
	 * converts each owner's legacy bag JSON files into YAML files containing bag
	 * metadata and serialized content. The configuration is updated to version
	 * {@code 4} and saved prior to performing file conversions.
	 *
	 * Side effects:
	 * <ul>
	 *   <li>Updates and saves the supplied configuration.</li>
	 *   <li>Creates or overwrites YAML files under
	 *       {@code <dataFolder>/bags/<owner>/<bag>.yml}.</li>
	 *   <li>Emits log messages for progress, warnings, and errors.</li>
	 *   <li>Does not remove legacy JSON files; manual cleanup may be required.</li>
	 * </ul>
	 *
	 * Thread-safety: not thread-safe. Call during initialization only.
	 *
	 * @param config non-null plugin configuration used to read/write
	 *               {@code config-version} and defaults (e.g., {@code bag-texture}).
	 * @throws InvalidConfigurationException if a YAML configuration cannot be
	 *         initialized or persisted during conversion.
	 */
	@DoNotCall("Internal Use Only")
	public static void check(@NotNull Config config) throws InvalidConfigurationException {
		if(config.GetInt("config-version") < 4) {
    		Log.Warning(Main.plugin, "Old data storage found, updating bag data!");
    		//Log.Error(plugin, "Debugging. Old data files are not removed.");
    		Log.Error(Main.plugin, "Old data files are not removed, in case of failure.");
    		config.Set("config-version", 4);
    		config.SaveConfig();
    		int converted = 0;
    		int failed = 0;
    		
    		List<String> owners	= BagData.GetBagOwners();
    		for(String owner : owners) {
    			List<String> bags = GetBags(owner);
    			for(String bag : bags) {
    				bag = bag.replace(".json", "");
    				String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, bag);
    				Path conf = Paths.get(String.format("%s/bags/%s/%s.yml", Main.plugin.getDataFolder(), owner, bag));
    				try {
    					List<ItemStack> cont = JsonUtils.fromJson(Files.readString(Paths.get(path)));
    	    			@SuppressWarnings("unused")
						List<String> lines = Arrays.asList(JsonUtils.toPrettyJson(cont));

    	    			try {
    	    				//Files.write(conf, lines, StandardCharsets.UTF_8);
    	    				//Files.createFile(conf);
    	    				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(conf.toString()), StandardCharsets.UTF_8);
    	    				//writer.write(String.format("uuid: %s", bag));
    	    				writer.close();
    	    				
    	    			}catch(IOException e){
    	    				//e.printStackTrace();
    	    			}finally {
    	    				try {
    	    					Config bagData = new Config(Main.plugin, String.format("/bags/%s/%s.yml", owner, bag));
    	    					bagData.Set("uuid", bag);
    	    					bagData.Set("owner", owner);
    	    					bagData.Set("creator", "null");
    	    					bagData.Set("size", cont.size());
    	    					bagData.Set("texture", config.Get("bag-texture"));
    	    					bagData.Set("custommodeldata", 0);
    	    					bagData.Set("trusted", new ArrayList<String>());
    	    					bagData.Set("auto-pickup", "null");
    	    					bagData.Set("weight-max", 0);
    	    					bagData.Set("content", JsonUtils.toJson(cont).replace("'", "◊"));
    	    					bagData.SaveConfig();
    	    					converted++;
    	    				}catch(Exception E) {
    	    					//E.printStackTrace();
    	    					// Error: Top level is not a Map.
    	    					// Unsure why this is thrown, but the file is converted successfully without issues..
    	    					//Log.Error(plugin, String.format("Something went wrong while converting %s!.", String.format("/bags/%s/%s", owner, bag)));
    	    				}
    	    			}
    				} catch(Exception e) {
    					failed++;
    		    		//config.Set("config-version", 3);
    		    		//config.SaveConfig();
    					e.printStackTrace();
    					Log.Error(Main.plugin, String.format("Failed to convert %s, may require manual update.", String.format("/bags/%s/%s.json", owner, bag)));
    				}
    			}
    		}
    		Log.Info(Main.plugin, String.format("Converted %s Data Files!", converted));
    		Log.Info(Main.plugin, String.format("Failed: %s.", failed));
    	}
	}
    
    /**
     * Lists the bag data files for a given player under
     * {@code <dataFolder>/bags/<player>/}. Only non-directory files that are not
     * YAML ({@code .yml}) are returned. File names are returned as observed on
     * disk and may still include other extensions (e.g., {@code .json}).
     *
     * @param player the owner/directory name; must not be {@code null}
     * @return a list of file names for the player's bags; never {@code null}
     *         (an empty list is returned on error)
     */
    static List<String> GetBags(@NotNull String player){
		Log.Debug(Main.plugin, "[DI-21] " + player);
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), player)).listFiles())
					.filter(file -> !file.isDirectory())
					.filter(file -> !file.getName().contains(".yml"))
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < bags.size(); i++) {
				//Log.Debug(Main.plugin, bags.get(i));
				bags.set(i, bags.get(i).replace(".yml", ""));
			}
			return bags;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
}
