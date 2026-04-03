package valorless.havenbags.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatMessageType;
import valorless.havenbags.BagData;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Message;
import valorless.havenbags.enums.BagState;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.havenbags.HavenBags;
import valorless.havenbags.Lang;
import valorless.havenbags.utils.TextFeatures;
import valorless.valorlessutils.utils.Utils;

public class CommandInfo {

	final static String Name = "§7[§aHaven§bBags§7]§r";

	public static boolean Run(HBCommand command) {

		if(command.sender instanceof Player sender) {
			ItemStack hand = sender.getInventory().getItemInMainHand();
			ItemMeta meta = sender.getInventory().getItemInMainHand().getItemMeta();

			if(HavenBags.IsBag(hand) && BagState.getState(hand) == BagState.USED) {
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
				String limit = String.valueOf(PDC.GetDouble(hand, "weight-limit").intValue());
				List<String> lore = meta.getLore();

				String _lore = "";
				for(int i = 0; i < lore.size(); i++) { _lore = _lore + "\n    " + lore.get(i); }

				command.sender.sendMessage("§6## HavenBag Bag Information ##");

				Message message = new Message(ChatMessageType.CHAT, 
						"  §fUUID: §e" + uuid + " §7(§eClick to copy§7)"
						);
				message.SetHoverText(Lang.Parse("Click to copy.", sender));
				message.addCopyToClipboardEvent(uuid);
				message.Send(sender);

				if(!Utils.IsStringNullOrEmpty(data.getTexture())) { 
					Message texture = new Message(ChatMessageType.CHAT, 
							"  §fTexture: ... §7(§eClick to copy§7)"
							);
					texture.SetHoverText(Lang.Parse( "Base64: " + data.getTexture(), sender));
					texture.addCopyToClipboardEvent(data.getTexture());
					texture.Send(sender);
				}

				List<String> infoList = new ArrayList<String>();

				String info = "";
				if(!Utils.IsStringNullOrEmpty(owner)) { 
					if (canBind) {
						try {
							Message m_owner = new Message(ChatMessageType.CHAT, 
									String.format("  §fOwner: §e%s (%s)", Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(), owner) + " §7(§eClick to copy§7)"
									);
							m_owner.SetHoverText(Lang.Parse("Click to copy.", sender));
							m_owner.addCopyToClipboardEvent(owner);
							m_owner.Send(sender);
							//infoList.add(String.format("  §fOwner: §e%s (%s)", Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(), owner)); 
						} catch(Exception e) {
							infoList.add("  §fOwner: §4Error");
						}
					}else {
						infoList.add("  §fOwner: §e" + owner);
					}
				}
				if(!Utils.IsStringNullOrEmpty(creator) && !creator.equalsIgnoreCase("null")) {
					try {
						info = info + String.format("\n  §fCreator: §e%s (%s)", Bukkit.getOfflinePlayer(UUID.fromString(creator)).getName(), creator);
					} catch(Exception e) {
						infoList.add("  §fCreator: §4Error");
					}
				}else {
					infoList.add("  §fCreator: §enull");
				}
				if(canBind != null) { infoList.add("  §fCanBind: §e" + canBind.toString()); }
				if(size != null) { infoList.add("  §fSize: §e" + size.toString()); }
				//if(filter != null) { infoList.add("  §fFilter: §e" + filter; }
				if(!Utils.IsStringNullOrEmpty(filter)) {
					infoList.add("  §fFilter: §e" + filter);
				}else {
					infoList.add("  §fFilter: §enone");
				}

				if(!data.getTrusted().isEmpty()) {
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
					infoList.add("  §fTrusted: §e" + trusted);
				}else {
					infoList.add("  §fTrusted: §enone");
				}
				if(weight != null) { infoList.add("  §fWeight: §e" + weight); }
				if(limit != null) { infoList.add("  §fWeight Limit: §e" + limit); }
				if(data.getModeldata() != null && data.getModeldata() != 0) { 
					infoList.add("  §fCustom Model Data: " + data.getModeldata());
				}
				if(!Utils.IsStringNullOrEmpty(data.getItemmodel())) { 
					infoList.add("  §fItemModel: " + data.getItemmodel());
				}
				if(data.getBlacklist() != null && !data.getBlacklist().isEmpty()) {
					infoList.add("  §fBlacklist:");
					for(String blacked : data.getBlacklist()) {
						infoList.add("    §f" + blacked);
					}
					infoList.add("  §fWhitelist:" + data.isWhitelist());
				}
				
				//if(lore != null) { infoList.add("  §fLore:§r" + _lore); }
				if(data.getTooltipStyle() != null) { infoList.add("  §fTooltip Style: " + data.getTooltipStyle()); }

				sender.sendMessage(String.join("\n", infoList));
				return true;
			}
		}
		return true;
	}
}
