package valorless.havenbags;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;

public class AdminGUI implements Listener {
	public enum GUIType { Main, Creation, Restoration, Player }
	
	public JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	private Inventory inv;
	private Player player;
	private String target;
	private OfflinePlayer targetPlayer;
	private GUIType type;
	private List<ItemStack> content = new ArrayList<ItemStack>();
	
	public AdminGUI(GUIType type, Player player) {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		this.plugin = Main.plugin;
		this.type = type;
		this.player = player;
		this.target = player.getUniqueId().toString();
		this.targetPlayer = player;
		
		Log.Debug(plugin, type.toString());
		
		try {
			PrepareContent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AdminGUI(GUIType type, Player player, OfflinePlayer target) {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		this.plugin = Main.plugin;
		this.type = type;
		this.player = player;
		this.target = target.getUniqueId().toString();
		this.targetPlayer = target;
		
		Log.Debug(plugin, type.toString());
		
		try {
			PrepareContent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void PrepareContent() {
		if(type == GUIType.Main) {
			content = PrepareMain();
			Open();
		} 
		else if(type == GUIType.Creation) {
			content = PrepareTemplates();
			Open();
		} 
		else if(type == GUIType.Restoration) {
			content = PrepareBags();
			Open();
		}
		else if(type == GUIType.Player) {
			try {
				content = PreparePlayerBags(target);
			} catch (Exception e) {
				player.sendMessage(Lang.Get("prefix") + "§cThis player has over 53 bags.\nPlease restore their bags through §e/bags restore§c!");
				player.closeInventory();
				e.printStackTrace();
				return;
			}
			Open();
		}
	}
	
	void Open() {
		//Log.Debug(plugin, type.toString());
		
		if(type == GUIType.Main) {
			inv = Bukkit.createInventory(player, 9, "§aHaven§bBags §rGUI");
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);
		}
		else if(type == GUIType.Creation) {
			inv = Bukkit.createInventory(player, 18, "§aHaven§bBags §rCreation GUI");
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);
		}
		else if(type == GUIType.Restoration) {
			inv = Bukkit.createInventory(player, 54, "§aHaven§bBags §rRestoration GUI");
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);
		}
		else if(type == GUIType.Player) {
			inv = Bukkit.createInventory(player, 54, Lang.Get("bags-of", targetPlayer.getName()));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);
		}
	}
	
	public void OpenInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }
	
	@EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        //player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        ItemStack clickedItem = e.getCurrentItem();        
        if(clickedItem == null) return;
        
        if (type == GUIType.Main) {
        	String action = NBT.GetString(clickedItem, "bag-action");
        	if(action.equalsIgnoreCase("create")){
        		type = GUIType.Creation;
            	Reload();
        	}
        	else if(action.equalsIgnoreCase("restore")){
        		type = GUIType.Restoration;
            	Reload();
        	}
        	else if(action.equalsIgnoreCase("return")){
            	type = GUIType.Main;
                Reload();
                return;
            }
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.Creation) {
        	String action = NBT.GetString(clickedItem, "bag-action");
        	if(action.equalsIgnoreCase("return")){
        		type = GUIType.Main;
            	Reload();
            	return;
        	}
        	
        	ItemStack giveItem = clickedItem.clone();
			NBT.SetString(giveItem, "bag-uuid", UUID.randomUUID().toString());
        	player.getInventory().addItem(giveItem);
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.Restoration) {
        	String action = NBT.GetString(clickedItem, "bag-action");
        	if(action.equalsIgnoreCase("return")){
        		type = GUIType.Main;
            	Reload();
            	return;
        	}
        	
        	String owner = clickedItem.getItemMeta().getDisplayName();
        	Log.Debug(plugin, "Changing Admin target to " + owner);
        	target = Bukkit.getPlayer(owner).getUniqueId().toString();
        	type = GUIType.Player;
        	Reload();
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.Player) {
        	String action = NBT.GetString(clickedItem, "bag-action");
        	if(action.equalsIgnoreCase("return")){
        		type = GUIType.Restoration;
            	Reload();
            	return;
        	}
        	
        	ItemStack giveItem = clickedItem.clone();
        	player.getInventory().addItem(giveItem);
        	e.setCancelled(true);
        	return;
        }
    }
    
    void Reload() {
    	try {
    		PrepareContent();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
	
	// Utils
    
    ArrayList<ItemStack> PrepareMain() {
		ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();
		
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		
		//Create
		String cresteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19";
		ItemStack createItem = SkullCreator.itemFromBase64(cresteTexture);
		ItemMeta createMeta = createItem.getItemMeta();
		createMeta.setDisplayName("§aBag Creation");
		List<String> c_lore = new ArrayList<String>();
		c_lore.add("§7Create bags easy.");
		createMeta.setLore(c_lore);
		createItem.setItemMeta(createMeta);
		NBT.SetString(createItem, "bag-action", "create");
		buttons.add(createItem);
		
		buttons.add(new ItemStack(Material.AIR));
		
		//Restore
		String restoreTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
		ItemStack restoreItem = SkullCreator.itemFromBase64(restoreTexture);
		ItemMeta restoreMeta = restoreItem.getItemMeta();
		restoreMeta.setDisplayName("§bBag Restoration");
		List<String> r_lore = new ArrayList<String>();
		r_lore.add("§7Restore bags of online players.");
		restoreMeta.setLore(r_lore);
		restoreItem.setItemMeta(restoreMeta);
		NBT.SetString(restoreItem, "bag-action", "restore");
		buttons.add(restoreItem);

		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		
		//Info
		String infoTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjcwNWZkOTRhMGM0MzE5MjdmYjRlNjM5YjBmY2ZiNDk3MTdlNDEyMjg1YTAyYjQzOWUwMTEyZGEyMmIyZTJlYyJ9fX0=";
		ItemStack infoItem = SkullCreator.itemFromBase64(infoTexture);
		ItemMeta infoMeta = infoItem.getItemMeta();
		infoMeta.setDisplayName("§eInformation");
		List<String> I_lore = new ArrayList<String>();
		I_lore.add("§7The admin GUI does not allow restoration of ownerless bags,");
		I_lore.add("§7you have to do those manually with §e/bags restore§7.");
		I_lore.add("");
		I_lore.add("§7If a player has over 53 bags,");
		I_lore.add("§7you also have to manually restore them.");
		I_lore.add("");
		I_lore.add("§7You can also restore bags of offline players,");
		I_lore.add("§7by using §e/bags gui restore <username>§7.");
		infoMeta.setLore(I_lore);
		infoItem.setItemMeta(infoMeta);
		buttons.add(infoItem);
		
		return buttons;
	}

	ArrayList<ItemStack> PrepareTemplates() {
		ArrayList<ItemStack> templates = new ArrayList<ItemStack>();
		
		//Bound
		for(int i = 1; i <= 6; i++) {
			String bagTexture = Main.config.GetString("bag-texture");
			ItemStack bagItem = new ItemStack(Material.AIR);
			int size = i*9;
			
			if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
				bagItem = SkullCreator.itemFromBase64(bagTexture);
			} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
				bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
			} else {
				player.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
				player.closeInventory();
			}
			ItemMeta bagMeta = bagItem.getItemMeta();
			if(Main.config.GetInt("bag-custom-model-data") != 0) {
				bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
			}
			bagMeta.setDisplayName(Lang.Get("bag-unbound-name"));
			List<String> lore = new ArrayList<String>();
			for (String l : Lang.lang.GetStringList("bag-lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
			}
			for (String l : Lang.lang.GetStringList("bag-size")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size), player));
			}
			bagMeta.setLore(lore);
			bagItem.setItemMeta(bagMeta);
			NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
			NBT.SetString(bagItem, "bag-owner", "null");
			NBT.SetInt(bagItem, "bag-size", size);
			NBT.SetBool(bagItem, "bag-canBind", true);
			templates.add(bagItem);
		}
		
		//3 air spaces to make a new line.
		templates.add(new ItemStack(Material.AIR));
		templates.add(new ItemStack(Material.AIR));
		templates.add(new ItemStack(Material.AIR));
		
		//Ownerless
		for(int i = 1; i <= 6; i++) {
			String bagTexture = Main.config.GetString("bag-texture");
			ItemStack bagItem = new ItemStack(Material.AIR);
			int size = i*9;
			
			if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
				bagItem = SkullCreator.itemFromBase64(bagTexture);
			} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
				bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
			} else {
				player.sendMessage(Lang.Get("prefix") + "&cbag-type must be either HEAD or ITEM.");
				player.closeInventory();
			}
			ItemMeta bagMeta = bagItem.getItemMeta();
			if(Main.config.GetInt("bag-custom-model-data") != 0) {
				bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
			}
			bagMeta.setDisplayName(Lang.Get("bag-ownerless-unused"));
			List<String> lore = new ArrayList<String>();
			for (String l : Lang.lang.GetStringList("bag-lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, player));
			}
			for (String l : Lang.lang.GetStringList("bag-size")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size), player));
			}
			bagMeta.setLore(lore);
			bagItem.setItemMeta(bagMeta);
			NBT.SetString(bagItem, "bag-uuid", UUID.randomUUID().toString());
			NBT.SetString(bagItem, "bag-owner", "null");
			NBT.SetInt(bagItem, "bag-size", size);
			NBT.SetBool(bagItem, "bag-canBind", false);
			templates.add(bagItem);
		}

		templates.add(new ItemStack(Material.AIR));
		templates.add(new ItemStack(Material.AIR));
		
		String returnTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NjFhZDFmNWM3NmU5NzM1OGM0NDRmZTBlODNhMzk1NjRlNmI0ODEwOTE3MDk4NGE4NGVjYTVkY2NkNDI0In19fQ==";
		ItemStack returnItem = SkullCreator.itemFromBase64(returnTexture);
		ItemMeta returnMeta = returnItem.getItemMeta();
		returnMeta.setDisplayName("§eReturn");
		List<String> r_lore = new ArrayList<String>();
		r_lore.add("§7Go back.");
		returnMeta.setLore(r_lore);
		returnItem.setItemMeta(returnMeta);
		NBT.SetString(returnItem, "bag-action", "return");
		templates.add(returnItem);
		
		
		return templates;
	}
	
	List<ItemStack> PrepareBags() {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		Collection<? extends Player> p = Bukkit.getOnlinePlayers();
		List<Player> players = new ArrayList<>(p);
		
		if(players.size() <= 54) {
			for(int i = 0; i < players.size(); i++) {
				ItemStack entry = SkullCreator.itemFromUuid(players.get(i).getUniqueId());
				ItemMeta meta = entry.getItemMeta();
				meta.setDisplayName(players.get(i).getName());
				entry.setItemMeta(meta);
				NBT.SetString(entry, "bag-owner", players.get(i).getUniqueId().toString());
				bags.add(entry);
			}
		}
		else {
			for(int i = 0; i < 54; i++) {
				ItemStack entry = SkullCreator.itemFromUuid(players.get(i).getUniqueId());
				ItemMeta meta = entry.getItemMeta();
				meta.setDisplayName(players.get(i).getName());
				entry.setItemMeta(meta);
				NBT.SetString(entry, "bag-owner", players.get(i).getUniqueId().toString());
				bags.add(entry);
			}
		}
		
		while(bags.size() < 54) {
			bags.add(new ItemStack(Material.AIR));
		}
		
		String returnTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NjFhZDFmNWM3NmU5NzM1OGM0NDRmZTBlODNhMzk1NjRlNmI0ODEwOTE3MDk4NGE4NGVjYTVkY2NkNDI0In19fQ==";
		ItemStack returnItem = SkullCreator.itemFromBase64(returnTexture);
		ItemMeta returnMeta = returnItem.getItemMeta();
		returnMeta.setDisplayName("§eReturn");
		List<String> r_lore = new ArrayList<String>();
		r_lore.add("§7Go back.");
		returnMeta.setLore(r_lore);
		returnItem.setItemMeta(returnMeta);
		NBT.SetString(returnItem, "bag-action", "return");
		bags.set(53, returnItem);
		
		
			
			
		/*for(Player p : Bukkit.getOnlinePlayers()){
			ItemStack entry = SkullCreator.itemFromUuid(p.getUniqueId());
			ItemMeta meta = entry.getItemMeta();
			meta.setDisplayName(p.getName());
			entry.setItemMeta(meta);
			NBT.SetString(entry, "bag-owner", p.getUniqueId().toString());
			bags.add(entry);
		}*/
		//ItemStack entry = SkullCreator.itemFromBase64(Main.config.GetString("bag-texture"));
		//ItemMeta meta = entry.getItemMeta();
		//meta.setDisplayName("Ownerless");
		//entry.setItemMeta(meta);
		//NBT.SetString(entry, "bag-owner", "ownerless");
		//bags.add(entry);
		
		return bags;
	}
	
	List<ItemStack> PreparePlayerBags(String playeruuid) {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		List<String> bagfiles = GetBags(playeruuid);
		
		if(!playeruuid.equalsIgnoreCase("ownerless")) {
		for(String bagUUID : bagfiles){
			List<ItemStack> Content  = LoadContent(playeruuid, bagUUID);
			if (Content == null) continue;
			
			String bagTexture = Main.config.GetString("bag-texture");
			ItemStack bagItem = new ItemStack(Material.AIR);
			int size = Content.size();
			
			if(Main.config.GetString("bag-type").equalsIgnoreCase("HEAD")){
				bagItem = SkullCreator.itemFromBase64(bagTexture);
			} else if(Main.config.GetString("bag-type").equalsIgnoreCase("ITEM")) {
				bagItem = new ItemStack(Main.config.GetMaterial("bag-material"));
			}
			
			NBT.SetString(bagItem, "bag-uuid", bagUUID);
			NBT.SetString(bagItem, "bag-owner", playeruuid);
			NBT.SetInt(bagItem, "bag-size", size);
			NBT.SetBool(bagItem, "bag-canBind", true);
			
			ItemMeta bagMeta = bagItem.getItemMeta();
			if(Main.config.GetInt("bag-custom-model-data") != 0) {
				bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
			}
			
			bagMeta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-bound-name"), targetPlayer.getName()));
			List<String> lore = new ArrayList<String>();
			for (String l : Lang.lang.GetStringList("bag-lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, target));
			}
			if(NBT.GetBool(bagItem, "bag-canBind")) {
	            for (String l : Lang.lang.GetStringList("bound-to")) {
	            	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, targetPlayer.getName())));
	            }
	        }
	        //l
			for (String l : Lang.lang.GetStringList("bag-size")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size), targetPlayer.getName()));
			}
			
			List<ItemStack> cont = new ArrayList<ItemStack>();
	        int a = 0;
			List<String> items = new ArrayList<String>();
	        for(int i = 0; i < Content.size(); i++) {
	    		cont.add(Content.get(i));
	    		if(Content.get(i) != null) {
	    			if(Content.get(i).getItemMeta().hasDisplayName()) {
	    				if(Content.get(i).getAmount() != 1) {
	    					items.add(Lang.Get("bag-content-item-amount", Content.get(i).getItemMeta().getDisplayName(), Content.get(i).getAmount()));
	    				} else {
	    					items.add(Lang.Get("bag-content-item", Content.get(i).getItemMeta().getDisplayName()));
	    				}
	    			}else {
	    				if(Content.get(i).getAmount() != 1) {
	    					items.add(Lang.Get("bag-content-item-amount", Main.translator.Translate(Content.get(i).getType().getTranslationKey()), Content.get(i).getAmount()));
	    				} else {
	    					items.add(Lang.Get("bag-content-item", Main.translator.Translate(Content.get(i).getType().getTranslationKey())));
	    				}
	    			}
	    			a++;
	    		}
	    	}
	        if(a > 0 && Lang.lang.GetBool("show-bag-content")) {
	        	lore.add(Lang.Get("bag-content-title"));
	        	for(int k = 0; k < items.size(); k++) {
	        		if(k < Lang.lang.GetInt("bag-content-preview-size")) {
	        			lore.add("  " + items.get(k));
	        		}
	        	}
	        	if(a > Lang.lang.GetInt("bag-content-preview-size")) {
	        		lore.add(Lang.Get("bag-content-and-more"));
	        	}
	        }
			bagMeta.setLore(lore);
			bagItem.setItemMeta(bagMeta);
			
			bags.add(bagItem);
		}
		}
		
		while(bags.size() < 54) {
			bags.add(new ItemStack(Material.AIR));
		}
		
		String returnTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NjFhZDFmNWM3NmU5NzM1OGM0NDRmZTBlODNhMzk1NjRlNmI0ODEwOTE3MDk4NGE4NGVjYTVkY2NkNDI0In19fQ==";
		ItemStack returnItem = SkullCreator.itemFromBase64(returnTexture);
		ItemMeta returnMeta = returnItem.getItemMeta();
		returnMeta.setDisplayName("§eReturn");
		List<String> r_lore = new ArrayList<String>();
		r_lore.add("§7Go back.");
		returnMeta.setLore(r_lore);
		returnItem.setItemMeta(returnMeta);
		NBT.SetString(returnItem, "bag-action", "return");
		bags.set(53, returnItem);
		
		return bags;
	}
	
	List<String> GetBagOwners(){
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
	
	List<String> GetBags(String player){
		try {
			List<String> bags = Stream.of(new File(String.format("%s/bags/%s/", Main.plugin.getDataFolder(), player)).listFiles())
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
	
	List<ItemStack> LoadContent(String owner, String uuid) {
		String path = String.format("%s/bags/%s/%s.json", plugin.getDataFolder(), owner, uuid);
		
		File bagData;
		try {
			bagData = new File(path);
		} catch(Exception e) {
			player.sendMessage(e.toString());
			e.printStackTrace();
			return new ArrayList<ItemStack>();
		}
        if(!bagData.exists()) {
        //	return new ArrayList<ItemStack>();
        }
        String content = "";
		try {
			Path filePath = Path.of(path);
			content = Files.readString(filePath);
			return JsonUtils.fromJson(content);
		} catch (IOException e) {
			player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:LoadContent()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
			return new ArrayList<ItemStack>();
		}
	}
}
