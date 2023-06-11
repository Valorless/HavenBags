package valorless.havenbags;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import valorless.valorlessutils.ValorlessUtils.Log;

public class TabCompletion implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			List<String> subCommands = new ArrayList<>();
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
				subCommands.add("restore");
			}
			if (sender.hasPermission("havenbags.preview")) {
				subCommands.add("preview");
			}
			if (sender.hasPermission("havenbags.rename")) {
				subCommands.add("rename");
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
	
	public List<String> GetBags(String player){
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", HavenBags.plugin.getDataFolder(), player)).listFiles())
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < bags.size(); i++) {
				bags.set(i, bags.get(i).replace(".json", ""));
			}
			return bags;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	public List<String> GetBagOwners(){
		try {
			List<String> bagOwners = Stream.of(new File(String.format("%s/bags/", HavenBags.plugin.getDataFolder())).listFiles())
					.filter(file -> file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
			return bagOwners;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
}