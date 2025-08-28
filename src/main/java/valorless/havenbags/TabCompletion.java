package valorless.havenbags;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import valorless.havenbags.HavenBags.BagState;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.features.AutoPickup;
import valorless.havenbags.features.BagEffects;
import valorless.havenbags.features.CustomBags;

public class TabCompletion implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			List<String> subCommands = new ArrayList<>();
			subCommands.add("help");
			/*if (sender.hasPermission("havenbags.open")) {
				subCommands.add("open");
			}*/
			if (sender.hasPermission("havenbags.reload")) {
				subCommands.add("reload");
			}
			if (sender.hasPermission("havenbags.create")) {
				subCommands.add("create");
			}
			if (sender.hasPermission("havenbags.give")) {
				subCommands.add("give");
			}
			if (sender.hasPermission("havenbags.restore")) {
				//subCommands.add("restore");
			}
			if (sender.hasPermission("havenbags.preview")) {
				//subCommands.add("preview");
			}
			if (sender.hasPermission("havenbags.rename")) {
				subCommands.add("rename");
			}
			if (sender.hasPermission("havenbags.info")) {
				subCommands.add("info");
				subCommands.add("rawinfo");
			}
			if (sender.hasPermission("havenbags.gui")) {
				subCommands.add("gui");
			}else if(Main.config.GetBool("player-gui.enabled")){
				subCommands.add("gui");
			}
			if (sender.hasPermission("havenbags.empty")) {
				subCommands.add("empty");
			}
			if (sender.hasPermission("havenbags.autopickup")) {
				subCommands.add("autopickup");
			}
			if (sender.hasPermission("havenbags.weight")) {
				subCommands.add("weight");
			}
			if (sender.hasPermission("havenbags.trust")) {
				subCommands.add("trust");
			}
			if (sender.hasPermission("havenbags.trust")) {
				subCommands.add("untrust");
			}
			if (sender.hasPermission("havenbags.texture")) {
				subCommands.add("texture");
			}
			if (sender.hasPermission("havenbags.modeldata")) {
				subCommands.add("modeldata");
				subCommands.add("itemmodel");
			}
			if (sender.hasPermission("havenbags.token")) {
				subCommands.add("token");
			}
			if (sender.hasPermission("havenbags.database")) {
				subCommands.add("convertdatabase");
			}
			if (sender.hasPermission("havenbags.autosort")) {
				subCommands.add("autosort");
			}
			if (sender.hasPermission("havenbags.magnet")) {
				subCommands.add("magnet");
			}
			if (sender.hasPermission("havenbags.refill")) {
				subCommands.add("refill");
			}
			if (sender.hasPermission("havenbags.effects")) {
				subCommands.add("effect");
			}
			if(Main.plugins.GetBool("mods.HavenBagsPreview.enable-command")) {
				subCommands.add("mod");
			}
			

			if (sender.isOp()) {
				subCommands.add("customcontent");
				subCommands.add("pluginreload");
			}

			StringUtil.copyPartialMatches(args[0], subCommands, completions);
		}
		else if(args.length == 2) {
			String cmd = args[1];
			if (args[0].equalsIgnoreCase("create") && sender.hasPermission("havenbags.create")) {
				// /bags create <size>
				List<String> sizes = new ArrayList<String>();
				sizes.add("ownerless");
				sizes.add("1");
				sizes.add("2");
				sizes.add("3");
				sizes.add("4");
				sizes.add("5");
				sizes.add("6");
				StringUtil.copyPartialMatches(cmd, sizes, completions);
			}
			if (args[0].equalsIgnoreCase("give") && sender.hasPermission("havenbags.give")) {
				// /bags give <player>
				List<String> playerNames = getOnlinePlayerNames();
				StringUtil.copyPartialMatches(cmd, playerNames, completions);
			}
			if (args[0].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.restore")) {
				// /bags restore <player>
				//List<String> playerNames = getOnlinePlayerNames();
				//playerNames.add("ownerless");
				List<String> playerNames = GetBagOwners();
				StringUtil.copyPartialMatches(cmd, playerNames, completions);
			}
			if (args[0].equalsIgnoreCase("preview") && sender.hasPermission("havenbags.preview")) {
				// /bags preview <player>
				//List<String> playerNames = getOnlinePlayerNames();
				//playerNames.add("ownerless");
				List<String> playerNames = GetBagOwners();
				StringUtil.copyPartialMatches(cmd, playerNames, completions);
			}
			if (args[0].equalsIgnoreCase("gui") && sender.hasPermission("havenbags.gui")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("create");
				cmds.add("restore");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("autopickup") && sender.hasPermission("havenbags.autopickup")) {
				List<String> filters = AutoPickup.GetFilterNames((Player)sender);
				filters.add("none");
				StringUtil.copyPartialMatches(cmd, filters, completions);
			}
			if (args[0].equalsIgnoreCase("trust") && sender.hasPermission("havenbags.trust")) {
				StringUtil.copyPartialMatches(cmd, getOnlinePlayerNames(), completions);
			}
			if (args[0].equalsIgnoreCase("untrust") && sender.hasPermission("havenbags.trust")) {
				Player player = (Player)sender;
				ItemStack item = player.getInventory().getItemInMainHand();
				if(item != null) {
					if(HavenBags.IsBag(item)) {
						if(HavenBags.BagState(item) == BagState.Used) {
							Data data = BagData.GetBag(HavenBags.GetBagUUID(item), null);
							List<String> list = data.getTrusted();
							StringUtil.copyPartialMatches(cmd, list, completions);
						}
					}
				}
			}
			if (args[0].equalsIgnoreCase("texture") && sender.hasPermission("havenbags.texture")) {
				StringUtil.copyPartialMatches(cmd, getTextures(), completions);
			}
			if (args[0].equalsIgnoreCase("token") && sender.hasPermission("havenbags.token")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("texture");
				cmds.add("custommodeldata");
				cmds.add("itemmodel");
				cmds.add("effect");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("convertdatabase") && sender.hasPermission("havenbags.database")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("FILES");
				cmds.add("MYSQL");
				cmds.add("MYSQLPLUS");
				cmds.add("SQLITE");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("customcontent") && sender.hasPermission("havenbags.editor")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("edit");
				cmds.add("load");
				cmds.add("save");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("autosort") && sender.hasPermission("havenbags.autosort")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("on");
				cmds.add("off");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("magnet") && sender.hasPermission("havenbags.magnet")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("on");
				cmds.add("off");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("refill") && sender.hasPermission("havenbags.refill")) {
				List<String> cmds = new ArrayList<String>();
				cmds.add("on");
				cmds.add("off");
				StringUtil.copyPartialMatches(cmd, cmds, completions);
			}
			if (args[0].equalsIgnoreCase("effect") && sender.hasPermission("havenbags.effects")) {
				List<String> effects = BagEffects.getEffectNames();
				effects.add("none");
				StringUtil.copyPartialMatches(cmd, effects, completions);
			}
			/*
			if (args[0].equalsIgnoreCase("open") && sender.hasPermission("havenbags.open")) {
				Player player = (Player)sender;
				String owner = player.getUniqueId().toString();
				List<String> bags = GetBags(owner);
				StringUtil.copyPartialMatches(cmd, bags, completions);
			}
			*/
		}
		else if(args.length == 3) {
			String cmd = args[2];
			if (args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("ownerless") && sender.hasPermission("havenbags.create")) {
				// /bags create ownerless <size>
				List<String> sizes = new ArrayList<String>();
				sizes.add("1");
				sizes.add("2");
				sizes.add("3");
				sizes.add("4");
				sizes.add("5");
				sizes.add("6");
				StringUtil.copyPartialMatches(cmd, sizes, completions);
			}
			if (args[0].equalsIgnoreCase("give") && sender.hasPermission("havenbags.give")) {
				// /bags give <player> <size>
				List<String> sizes = new ArrayList<String>();
				sizes.add("ownerless");
				sizes.add("1");
				sizes.add("2");
				sizes.add("3");
				sizes.add("4");
				sizes.add("5");
				sizes.add("6");
				sizes.addAll(CustomBags.List());
				StringUtil.copyPartialMatches(cmd, sizes, completions);
			}
			if (args[0].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.restore")) {
				// /bags restore <player> <uuid>
				List<String> bags = GetBags(args[1]);
				StringUtil.copyPartialMatches(cmd, bags, completions);
			}
			if (args[0].equalsIgnoreCase("preview") && sender.hasPermission("havenbags.preview")) {
				// /bags restore <player> <uuid>
				List<String> bags = GetBags(args[1]);
				StringUtil.copyPartialMatches(cmd, bags, completions);
			}
			if (args[0].equalsIgnoreCase("gui") && args[1].equalsIgnoreCase("restore") && sender.hasPermission("havenbags.gui")) {
				List<String> playerNames = getOnlinePlayerNames();
				StringUtil.copyPartialMatches(cmd, playerNames, completions);
			}
			if (args[0].equalsIgnoreCase("token") && args[1].equalsIgnoreCase("texture") && sender.hasPermission("havenbags.token")) {
				StringUtil.copyPartialMatches(cmd, getTextures(), completions);
			}
			
			//What??
			if (args[0].equalsIgnoreCase("modeldata") && sender.hasPermission("havenbags.modeldata") && sender.hasPermission("havenbags.token")) {
				List<String> materialNames = Arrays.stream(Material.values())
				        .map(material -> material.name().toLowerCase())
				        .collect(Collectors.toList());
				StringUtil.copyPartialMatches(cmd, materialNames, completions);
			}
			if (args[0].equalsIgnoreCase("itemmodel") && sender.hasPermission("havenbags.modeldata") && sender.hasPermission("havenbags.token")) {
				List<String> materialNames = Arrays.stream(Material.values())
				        .map(material -> material.name().toLowerCase())
				        .collect(Collectors.toList());
				StringUtil.copyPartialMatches(cmd, materialNames, completions);
			}
			
			
			if (args[0].equalsIgnoreCase("token") && args[1].equalsIgnoreCase("effect") && sender.hasPermission("havenbags.effects") && sender.hasPermission("havenbags.token")) {
				StringUtil.copyPartialMatches(cmd, BagEffects.getEffectNames(), completions);
			}

			if (args[0].equalsIgnoreCase("customcontent") && sender.hasPermission("havenbags.editor")) {
				if(args[1].equalsIgnoreCase("load")) {
					StringUtil.copyPartialMatches(cmd, ListCustomContent(), completions);
				}
				if(args[1].equalsIgnoreCase("save")) {
					List<String> cmds = new ArrayList<String>();
					cmds.add("<FileName>");
					StringUtil.copyPartialMatches(cmd, cmds, completions);
				}
			}
		} 
		else if(args.length == 4) {
			String cmd = args[3];
			if (args[0].equalsIgnoreCase("give") && args[2].equalsIgnoreCase("ownerless") && sender.hasPermission("havenbags.give")) {
				// /bags give <player> ownerless <size>
				List<String> sizes = new ArrayList<String>();
				sizes.add("1");
				sizes.add("2");
				sizes.add("3");
				sizes.add("4");
				sizes.add("5");
				sizes.add("6");
				StringUtil.copyPartialMatches(cmd, sizes, completions);
			}
			if (args[0].equalsIgnoreCase("token") && sender.hasPermission("havenbags.token")) {
				StringUtil.copyPartialMatches(cmd, getOnlinePlayerNames(), completions);
			}
		} 
		else {
			
		}
		return completions;
	}

	private List<String> getOnlinePlayerNames() {
		List<String> playerNames = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			playerNames.add(player.getName());
		}
		return playerNames;
	}
	
	private List<String> getTextures() {
		List<String> textures = new ArrayList<>();
		for (Object texture : Main.textures.GetConfigurationSection("textures").getKeys(false).toArray()) {
			textures.add(texture.toString());
		}
		return textures;
	}
	
	public List<String> GetBags(String player){
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), player)).listFiles())
					.filter(file -> !file.isDirectory())
					.filter(file -> !file.getName().contains(".json"))
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < bags.size(); i++) {
				bags.set(i, bags.get(i).replace(".yml", ""));
			}
			return bags;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	public List<String> GetBagOwners(){
		try {
			List<String> bagOwners = Stream.of(new File(String.format("%s/bags/", Main.plugin.getDataFolder())).listFiles())
					.filter(file -> file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
			return bagOwners;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	public List<String> ListCustomContent(){
		try {
			List<String> files = Stream.of(new File(String.format("%s/customcontent/", Main.plugin.getDataFolder())).listFiles())
					.filter(file -> !file.isDirectory())
					.filter(file -> file.getName().contains(".yml"))
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < files.size(); i++) {
				files.set(i, files.get(i).replace(".yml", ""));
			}
			return files;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	
}