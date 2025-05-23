package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.BagData;
import valorless.havenbags.HavenBags.BagState;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.HavenBags;
import valorless.havenbags.utils.TextFeatures;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.utils.Utils;

public class CommandInfo {
	
	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {
		
		ItemStack hand = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand();
		ItemMeta meta = Bukkit.getPlayer(command.sender.getName()).getInventory().getItemInMainHand().getItemMeta();
			
		if(HavenBags.IsBag(hand) && HavenBags.BagState(hand) == BagState.Used) {
			Data data = BagData.GetBag(HavenBags.GetBagUUID(hand), hand);
			
			String uuid = data.getUuid();
			String owner = data.getOwner();
			String creator = data.getCreator();
			Boolean canBind = (!owner.equalsIgnoreCase("ownerless")) ? true : false;
			Integer size = data.getSize();
			String filter = data.getAutopickup();
			List<String> trust = new ArrayList<String>();
			String trusted = "";
			String weight = "";
			if(uuid.equalsIgnoreCase("null")) {
				weight = "0.0";
			}else {
				weight = TextFeatures.LimitDecimal(String.valueOf(HavenBags.GetWeight(hand)),2);
			}
			String limit = String.valueOf(NBT.GetDouble(hand, "bag-weight-limit").intValue());
			List<String> lore = meta.getLore();
			
			String _lore = "";
			for(int i = 0; i < lore.size(); i++) { _lore = _lore + "\n    " + lore.get(i); }
			
			String info = "§6## HavenBag Bag Information ##";
			
			if(!Utils.IsStringNullOrEmpty(uuid)) { info = info + "\n  §fUUID: §e" + uuid; }
			if(!Utils.IsStringNullOrEmpty(owner)) { 
				if (canBind) {
					try {
						info = info + String.format("\n  §fOwner: §e%s (%s)", Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(), owner); 
					} catch(Exception e) {
						info = info + "\n  §fOwner: §4Error";
					}
				}else {
					info = info + "\n  §fOwner: §e" + owner;
				}
			}
			if(!Utils.IsStringNullOrEmpty(creator) && !creator.equalsIgnoreCase("null")) {
				try {
					info = info + String.format("\n  §fCreator: §e%s (%s)", Bukkit.getOfflinePlayer(UUID.fromString(creator)).getName(), creator);
				} catch(Exception e) {
					info = info + "\n  §fCreator: §4Error";
				}
			}else {
				info = info + "\n  §fCreator: §enull";
			}
			if(canBind != null) { info = info + "\n  §fCanBind: §e" + canBind.toString(); }
			if(size != null) { info = info + "\n  §fSize: §e" + size.toString(); }
			//if(filter != null) { info = info + "\n  §fFilter: §e" + filter; }
			if(!Utils.IsStringNullOrEmpty(filter)) {
				info = info + "\n  §fFilter: §e" + filter;
			}else {
				info = info + "\n  §fFilter: §enone";
			}
			
			if(NBT.Has(hand, "bag-trust")) {
				trust = data.getTrusted();
			}
			if(!trust.isEmpty()) {
				for(int i = 0; i < trust.size(); i++) { 
					if(i != 0) {
						trusted = trusted + ", " + trust.get(i); 
					}else {
						trusted = trust.get(i); 
					}
				}
				info = info + "\n  §fTrusted: §e" + trusted;
			}else {
				info = info + "\n  §fTrusted: §enone";
			}
			if(weight != null) { info = info + "\n  §fWeight: §e" + weight; }
			if(limit != null) { info = info + "\n  §fWeight Limit: §e" + limit; }
			if(lore != null) { info = info + "\n  §fLore:§r" + _lore; }
			
			command.sender.sendMessage(info);
			return true;
		}
		return true;
	}
}
