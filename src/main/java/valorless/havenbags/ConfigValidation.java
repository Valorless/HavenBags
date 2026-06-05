package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;

import valorless.havenbags.features.AutoPickup;
import valorless.valorlessutils.logging.Log;

public class ConfigValidation {

	/**
	 * validates and initializes various components of the application.<br>
	 * This method sequentially calls:<br>
	 * - {@link #Filters()} to process or validate the filters.<br>
	 */
	public static void validate() {
		Filters();
	}
	private static void Filters() {
		AutoPickup.filter.addValidationEntry("allow-specific", false);
		
		Log.debug(Main.plugin, "[DI-6] Validating filters.yml");
		AutoPickup.filter.validate();
	}
	

}
