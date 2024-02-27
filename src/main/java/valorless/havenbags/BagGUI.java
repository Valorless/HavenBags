package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
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

import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.sound.SFX;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;

public class BagGUI implements Listener {
	public JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	private Inventory inv;
	public ItemStack bagItem;
	public ItemMeta bagMeta;
	public List<ItemStack> content;
	public static Config config;
	public Player player;
	public String bagOwner;
	String bag = "";
	boolean preview;
	
    public BagGUI(JavaPlugin plugin, int size, Player player, ItemStack bagItem, ItemMeta bagMeta, boolean... preview) {
    	if(preview.length != 0) this.preview = preview[0];
    	if(!this.preview) Main.activeBags.add(new ActiveBag(this, NBT.GetString(bagItem, "bag-uuid")));
    	
    	try {
    		// Try get owner's name on the server.
    		this.bag = NBT.GetString(bagItem, "bag-owner") + "/" + NBT.GetString(bagItem, "bag-uuid");
    	} catch (Exception e) {
    		// Otherwise use MojangAPI
    		if(!NBT.GetString(bagItem, "bag-owner").equalsIgnoreCase("ownerless")) {
    			this.bag = NBT.GetString(bagItem, "bag-owner") + "/" + NBT.GetString(bagItem, "bag-uuid");
    		} else {
    			this.bagOwner = "ownerless" + "/" + NBT.GetString(bagItem, "bag-uuid").toString();
    		}
    	}
    	Log.Debug(plugin, "Attempting to create and open bag " + bag);
    	this.plugin = plugin;
    	this.bagItem = bagItem;
    	this.bagMeta = bagMeta;
    	this.player = player;
    	try {
    		// Try get owner's name on the server.
    		this.bagOwner = NBT.GetString(bagItem, "bag-owner");
    	} catch (Exception e) {
    		// Otherwise use MojangAPI
    		if(!NBT.GetString(bagItem, "bag-owner").equalsIgnoreCase("ownerless")) {
    			this.bagOwner = NBT.GetString(bagItem, "bag-owner");
    		} else {
    			this.bagOwner = "ownerless";
    		}
    	}
    	
    	if(HavenBags.DoesBagExist(NBT.GetString(bagItem, "bag-uuid"), NBT.GetString(bagItem, "bag-owner"), player) == false) return;

    	if(!this.preview) CheckInstances(); // Check for multiple of the same bags
    	
    	if(!Utils.IsStringNullOrEmpty(Lang.Get("bag-inventory-title"))) {
    		inv = Bukkit.createInventory(player, size, Lang.Get("bag-inventory-title"));
    	} else {
    		inv = Bukkit.createInventory(player, size, bagMeta.getDisplayName());
    	}
    	
        if(Bukkit.getPluginManager().getPlugin("ChestSort") != null) {
        	try {
        		de.jeff_media.chestsort.api.ChestSortAPI.setSortable(inv);   
        	}catch (Exception e) {
        		Log.Error(plugin, "Failed to get ChestSort's API. Is it up to date?)");
        	}
        }
        //this.content = JsonUtils.fromJson(Tags.Get(plugin, this.bagMeta.getPersistentDataContainer(), "content", PersistentDataType.STRING).toString());
		//player.sendMessage(content.toString());

        this.content = LoadContent();
        
        InitializeItems();
        //LoadContent();
        
        if(!this.preview) HavenBags.BagHashes.Add(inv.hashCode());
    }
    
    void CheckInstances() {
    	List<BagGUI> thisUUID = new ArrayList<BagGUI>();
    	for (ActiveBag openBag : Main.activeBags) {
    		Log.Debug(plugin, "Open Bag: " + openBag.uuid + " - " + NBT.GetString(bagItem, "bag-uuid"));
    		if(openBag.uuid.equalsIgnoreCase(NBT.GetString(bagItem, "bag-uuid"))) {
    			thisUUID.add(openBag.gui);
    		}
    	}
    	//Log.Debug(plugin, "" + thisUUID.size());
    	if(thisUUID.size() > 1) {
    		Log.Warning(plugin, "Multiple instances of the same bag is opened by: " + thisUUID.get(0).player.getName() + " & " + thisUUID.get(1).player.getName());
    		Log.Warning(plugin, "They might be trying to dupe items. Forcing their bags to close.");
    		for (BagGUI openBag : thisUUID) {
    			openBag.player.closeInventory();
        	}
    		player.closeInventory();
    	}
    }
    
	public void InitializeItems() {
		try {
			Log.Debug(plugin, "Attempting to initialize bag items");
    		for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
		} catch (Exception e) {
			if(e.toString().contains("because \"this.content\" is null")) {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				//Log.Error(plugin, String.format("Failed to load content of bag '%s'. Is the file empty?", bag));
				String errorMessage = 
						ChatColor.RED + "\n################################\n" +
						"THIS IS A CUSTOM ERROR THROWN BY THE PLUGIN, NOT THE SERVER\n" +
						"\n" +
						"Failed to load content of bag\n" +
						ChatColor.GOLD + "'%s.json'\n" +
						ChatColor.RED + "Please check if the file is empty or missing before reporting any errors.\n" +
						"\n" +
						"################################\n";
				console.sendMessage(String.format(errorMessage, bag));
				for (ActiveBag openBag : Main.activeBags) {
		    		Log.Debug(plugin, "Open Bag: " + openBag.uuid + " - " + NBT.GetString(bagItem, "bag-uuid"));
		    		if(openBag.uuid == NBT.GetString(bagItem, "bag-uuid")) {
		    			Main.activeBags.remove(openBag);
		    		}
		    	}
				throw(new NullPointerException(""));
			} else {
				e.printStackTrace();
			}
		}
    }
	
	List<ItemStack> LoadContent() {
		Log.Debug(plugin, "Attempting to load bag content");

    	String uuid = NBT.GetString(this.bagItem, "bag-uuid");
    	String owner = NBT.GetString(this.bagItem, "bag-owner");
		if(owner != "ownerless") {
			owner = bagOwner;
    	}
				
		return HavenBags.LoadBagContentFromServer(uuid, owner, player);
	}

    public void OpenInventory(final HumanEntity ent) {
    	try {
    		ent.openInventory(inv);
    	} catch (Exception e) {
    		
    	}
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        if(e.getRawSlot() == -999) return;
        Log.Debug(Main.plugin, e.getRawSlot() + "");
        
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
        Log.Debug(plugin, "clicked: " + e.getCurrentItem());
        Log.Debug(plugin, "cursor: " + e.getCursor());
        
      //Check Weight When cursor isnt air, clicked item is null. Therefore we run this before the null check.
        if(cursorItem != null) {
        if(e.getRawSlot() < inv.getSize() && !cursorItem.getType().equals(Material.AIR)) {
            Log.Debug(Main.plugin, "within");
        	if(Main.weight.GetBool("enabled") == false) return;
            Log.Debug(Main.plugin, "enabled");
            List<ItemStack> cont = new ArrayList<ItemStack>();
            for(int i = 0; i < inv.getSize(); i++) {
        		cont.add(inv.getItem(i));
        	}
            //Log.Debug(Main.plugin, HavenBags.CanCarry(clickedItem, bagItem, cont) + "");
        	if(!HavenBags.CanCarry(cursorItem, bagItem, cont)) { 
        		e.setCancelled(true);
            	//e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Parse(Main.weight.GetString("bag-cant-carry")));
            	List<Placeholder> placeholders = new ArrayList<Placeholder>();
            	placeholders.add(new Placeholder("%item%", Main.translator.Translate(cursorItem.getTranslationKey())));
            	placeholders.add(new Placeholder("%weight%",  HavenBags.ItemWeight(cursorItem) + ""));
            	placeholders.add(new Placeholder("%remaining%", (NBT.GetDouble(bagItem, "bag-weight-limit") - HavenBags.GetWeight(cont)) + ""));
            	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.ParseCustomPlaceholders(Main.weight.GetString("bag-cant-carry"), placeholders));
        		return;
        	}
        }
        }
        
        if(clickedItem == null) return;
        
        //if (Main.config.GetBool("bags-in-bags") == true) return;
        
        if(HavenBags.IsBag(clickedItem) && !Main.config.GetBool("bags-in-bags")) {
        	//e.getWhoClicked().closeInventory();
        	//e.getWhoClicked().sendMessage(Name + "§c Bags cannot be placed inside bags.");
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-bag-error"));
        	e.setCancelled(true);
        }
        
        if(e.getRawSlot() > inv.getSize() && e.isShiftClick()) {
            Log.Debug(Main.plugin, "within");
        	if(Main.weight.GetBool("enabled") == false) return;
            Log.Debug(Main.plugin, "enabled");
            List<ItemStack> cont = new ArrayList<ItemStack>();
            for(int i = 0; i < inv.getSize(); i++) {
        		cont.add(inv.getItem(i));
        	}
            //Log.Debug(Main.plugin, HavenBags.CanCarry(clickedItem, bagItem, cont) + "");
        	if(!HavenBags.CanCarry(clickedItem, bagItem, cont)) { 
        		e.setCancelled(true);
            	//e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Parse(Main.weight.GetString("bag-cant-carry")));
            	List<Placeholder> placeholders = new ArrayList<Placeholder>();
            	placeholders.add(new Placeholder("%item%", Main.translator.Translate(clickedItem.getTranslationKey())));
            	placeholders.add(new Placeholder("%weight%",  HavenBags.ItemWeight(clickedItem) + ""));
            	placeholders.add(new Placeholder("%remaining%", (NBT.GetDouble(bagItem, "bag-weight-limit") - HavenBags.GetWeight(cont)) + ""));
            	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.ParseCustomPlaceholders(Main.weight.GetString("bag-cant-carry"), placeholders));
        		return;
        	}
        }
        
        
        
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        if(!preview) Close(false);
    }
    
    // Move into HavenBags.java to make it public and specific.
    public boolean IsOpen() {
    	for (ActiveBag openBag : Main.activeBags) {
    		if(openBag.uuid.equalsIgnoreCase(NBT.GetString(bagItem, "bag-uuid"))) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public void Close(boolean forced) {
    	if(forced) {
    		Log.Warning(plugin, String.format("%s forcefully closed! Attempting to save it and return it to %s!", bag, player.getName()));
    		player.closeInventory();
    	}
    	
    	if(!HavenBags.IsBagOpen(bagItem)) return;

		SFX.Play(Main.config.GetString("close-sound"), 
				Main.config.GetFloat("close-volume").floatValue(), 
				Main.config.GetFloat("close-pitch").floatValue(), player);
    	
        Log.Debug(plugin, "Bag closed, attempting to save bag. (" + bag + ")");
    	
        List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < inv.getSize(); i++) {
    		cont.add(inv.getItem(i));
    	}
        
        if(Main.weight.GetBool("enabled")) {
        	HavenBags.HasWeightLimit(bagItem);
        	NBT.SetDouble(bagItem, "bag-weight", HavenBags.GetWeight(cont));
        }
        
        HavenBags.UpdateBagItem(bagItem, cont, player);
		GivePlayerBagBack();
		HavenBags.WriteBagToServer(bagItem, cont, player);
		try {
			for (int i = 0; i < Main.activeBags.size(); i++) {
    			Log.Debug(plugin, "Open Bag: " + Main.activeBags.get(i).uuid + " - " + NBT.GetString(bagItem, "bag-uuid"));
    			if(Main.activeBags.get(i).uuid == NBT.GetString(bagItem, "bag-uuid")) {
    				Main.activeBags.remove(i);
    			}
    		}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Log.Debug(plugin, "Remaining Open Bags: " + Main.activeBags.size());
		
		//UpdateTimestamp();
    }
    
    /*void UpdateTimestamp() {
    	Log.Debug(plugin, "Updating timestamp for " + bag);
    	Main.timeTable.Set(
    		String.format("%s/%s", NBT.GetString(bagItem, "bag-owner"), NBT.GetString(bagItem, "bag-uuid")),
    			System.currentTimeMillis() / 1000L);
    	Main.timeTable.SaveConfig();
    }*/
    
    void GivePlayerBagBack() {
    	HavenBags.ReturnBag(bagItem, player);
    }
    
    String FixMaterialName(String string) {
    	string = string.replace('_', ' ');
        char[] charArray = string.toCharArray();
        boolean foundSpace = true;
        for(int i = 0; i < charArray.length; i++) {
        	charArray[i] = Character.toLowerCase(charArray[i]);
        	if(Character.isLetter(charArray[i])) {
        		if(foundSpace) {
        			charArray[i] = Character.toUpperCase(charArray[i]);
        			foundSpace = false;
        		}
        	}
        	else {
        		foundSpace = true;
        	}
        }
        string = String.valueOf(charArray);
    	return string;
    }
    
    /*
    void WriteToServer() {
    	String uuid = NBT.GetString(bagItem, "bag-uuid");
    	String owner = NBT.GetString(bagItem, "bag-owner");
    	Log.Debug(plugin, "Attempting to write bag " + bag + " onto server");
    	
		if(owner != "ownerless") {
    		//player = Bukkit.getPlayer(UUID.fromString(owner));
			//player = bagOwner;
    	}
    	File bagData;
    	List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < inv.getSize(); i++) {
    		cont.add(inv.getItem(i));
    	}
    	if(owner != "ownerless") {
    		bagData = new File(plugin.getDataFolder() + "/bags/", bagOwner + "/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Debug(plugin, String.format("Bag data for (%s) %s does not exist, creating new.", bagOwner, uuid));
            }
    	}else {
    		bagData = new File(plugin.getDataFolder() + "/bags/", bagOwner + "/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Debug(plugin, String.format("Bag data for (%s) %s does not exist, creating new.", bagOwner, uuid));
            }
    	}
    	
    	Path path = Paths.get(plugin.getDataFolder() + "/bags/", bagOwner + "/" + uuid + ".json");
    	List<String> lines = Arrays.asList(JsonUtils.toPrettyJson(cont));
    	try {
    		Files.write(path, lines, StandardCharsets.UTF_8);
    	}catch(IOException e){
			player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
    	}
    	
    }
    */
}
