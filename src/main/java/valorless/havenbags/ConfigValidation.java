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

		AutoPickup.filter.addValidationEntry("gui.reset-filter.icon", "BARRIER",
				List.of("Item shown in the filter selection GUI for the \"no filter\" option.",
						"Supports Nexo items with \"nexo:item-id\" format."));
		AutoPickup.filter.addValidationEntry("gui.reset-filter.displayname", "&cReset Filter");
		AutoPickup.filter.addValidationEntry("gui.reset-filter.lore", List.of("&7Remove the current filter from the bag."));

		Log.debug(Main.plugin, "[DI-6] Validating filters.yml");
		AutoPickup.filter.validate();
	}
	

}
