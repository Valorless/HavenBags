package valorless.havenbags.hooks;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import valorless.havenbags.BagData;
import valorless.havenbags.BagData.Bag;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.BagListener;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.features.BagCarryLimit;

public class PlaceholderAPI extends PlaceholderExpansion{
	
	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String getAuthor() {
		return Main.plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return "havenbags";
	}

	@Override
	public String getVersion() {
		return Main.plugin.getDescription().getVersion();
	}

	/**
	 * This is the method called when a placeholder with our identifier is found and
	 * needs a value. <br>
	 * We specify the value identifier in this method. <br>
	 * Since version 2.9.1 can you use OfflinePlayers in your requests.
	 *
	 * @param player     An {@link org.bukkit.OfflinePlayer OfflinePlayer}.
	 * @param identifier A String containing the identifier/value.
	 *
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onRequest(OfflinePlayer player, @NotNull String identifier) {
				
		if(identifier.contains("bags_current")) {
			return "" + BagData.GetBags(player.getUniqueId().toString()).size();
		}
		
		if(identifier.contains("bags_max")) {
			if(player instanceof Player pl) return "" + BagListener.getPlayerBagLimit(pl);
			else return "N/A";
		}

		if(identifier.contains("bags_items_")) {
			if(player instanceof Player pl) {
				try {
					String[] split = identifier.split("_", 3);
					String material = split[2];
					int count = 0;

					for(String bag : BagData.GetBags(pl.getUniqueId().toString())) {
						Data data = BagData.GetBag(bag, null);
						count += HavenBags.countItems(data.getContent(), Material.valueOf(material.toUpperCase()));
					}

					return "" + count;
				}catch(Exception e) {
					return "0";
				}
			}
			else return "N/A";
		}

		if(identifier.contains("bags_inv_items_")) {
			if(player instanceof Player pl) {
				try {
					String[] split = identifier.split("_", 4);
					String material = split[3];
					int count = 0;

					for(Bag bag : HavenBags.GetBagsDataInInventory(pl)) {
						count += HavenBags.countItems(bag.content, Material.valueOf(material.toUpperCase()));
					}

					return "" + count;
				}catch(Exception e) {
					return "0";
				}
			}
			else return "N/A";
		}

		if(identifier.contains("player_has_bag")) {
			return (BagData.GetBags(player.getUniqueId().toString()).size() != 0) ? "true" : "false";
		}

		if(identifier.contains("carry_max")) {
			if(player instanceof Player pl) {
				return "" + BagCarryLimit.getBagCarryLimit(pl);
			}
			else return "N/A";
		}

		if(identifier.contains("carry")) {
			if(player instanceof Player pl) {
				return "" + HavenBags.GetBagsInInventory(pl);
			}
			else return "N/A";
		}

		if(identifier.contains("bags_slots_total")) {
			if(player instanceof Player pl) {
				try {
					int count = 0;

					for(Bag bag : HavenBags.GetBagsDataInInventory(pl)) {
						count += BagData.GetBag(HavenBags.GetBagUUID(bag.item), null).getSize();
					}

					return "" + count;
				}catch(Exception e) {
					return "0";
				}
			}
			else return "N/A";
		}

		if(identifier.contains("bags_slots_used")) {
			if(player instanceof Player pl) {
				try {
					int count = 0;

					for(Bag bag : HavenBags.GetBagsDataInInventory(pl)) {
						for(ItemStack item : bag.content) {
							if(item != null && item.getType() != Material.AIR) {
								count += 1;
							}
						}
					}

					return "" + count;
				}catch(Exception e) {
					return "0";
				}
			}
			else return "N/A";
		}

		if(identifier.contains("bags_slots_free")) {
			if(player instanceof Player pl) {
				try {
					int used = 0;

					for(Bag bag : HavenBags.GetBagsDataInInventory(pl)) {
						for(ItemStack item : bag.content) {
							if(item != null && item.getType() != Material.AIR) {
								used += 1;
							}
						}
					}

					return "" + (HavenBags.GetBagSlotsInInventory(pl) - used);
				}catch(Exception e) {
					return "0";
				}
			}
			else return "N/A";
		}

		if(identifier.contains("bags_others")) {
			if(player instanceof Player pl) {
				return "" + HavenBags.HasOthersBag(pl);
			}
			else return "N/A";
		}
		
		


		// We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
		// was provided
		return null;
	}

}
