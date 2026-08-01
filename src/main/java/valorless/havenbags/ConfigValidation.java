package valorless.havenbags;

import java.util.List;

import valorless.havenbags.features.AutoPickup;
import valorless.valorlessutils.logging.Log;

public class ConfigValidation {

	/**
	 * validates and initializes various components of the application.<br>
	 * This method sequentially calls:<br>
	 * - {@link #filters()} to process or validate the filters.<br>
	 */
	public static void validate() {
		filters();
	}
	private static void filters() {
		AutoPickup.filter.addValidationEntry("allow-specific", false);

		AutoPickup.filter.addValidationEntry("gui.reset-filter.icon", "BARRIER",
				List.of("Item shown in the filter selection GUI for the \"no filter\" option.",
						"Supports Nexo items with \"nexo:item-id\" format."));
		AutoPickup.filter.addValidationEntry("gui.reset-filter.displayname", "&cReset Filter");
		AutoPickup.filter.addValidationEntry("gui.reset-filter.lore", List.of("&7Remove the current filter from the bag."));

		if(!AutoPickup.filter.hasKey("custom-item-tags")) {
			AutoPickup.filter.addValidationEntry("custom-item-tags.example_tag", List.of(
					"DIRT", "DIORITE", "#minecraft:planks", "nexo:example_item"
			), List.of(
					"would be used as '#example_tag'",
					"Accepts vanilla tags as well, so you can use '#minecraft:planks' for example.",
					"All vanilla tags: https://minecraft.wiki/w/Item_tag_(Java_Edition)"
			));
		}

		Log.debug(Main.plugin, "[DI-6] Validating filters.yml");
		AutoPickup.filter.validate();
	}
	

}
