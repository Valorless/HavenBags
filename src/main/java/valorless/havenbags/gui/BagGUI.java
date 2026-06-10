package valorless.havenbags.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import valorless.havenbags.*;
import valorless.havenbags.BagData.UpdateSource;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.datamodels.Sound;
import valorless.havenbags.enums.DatabaseType;
import valorless.havenbags.events.BagCloseEvent;
import valorless.havenbags.events.BagOpenEvent;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.logging.Log;
import valorless.valorlessutils.utils.Utils;

/**
 * BagGUI class handles the creation and management of bag inventories in the HavenBags plugin.
 * It allows players to open, view, and interact with their bags, while also managing the content
 * and ensuring that multiple instances of the same bag are not opened simultaneously.
 */
public class BagGUI implements Listener {
	public JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	private Inventory inv;
	public ItemStack bagItem;
	public final String uuid;
	public ItemMeta bagMeta;
	public List<ItemStack> content;
	//public static Config config;
	public Player player;
	public String bagOwner;
	String bag = "";
	boolean preview;
	public int size;
	boolean ready = false;
	
    public BagGUI(JavaPlugin plugin, int size, Player player, ItemStack bagItem, ItemMeta bagMeta, boolean... preview) {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
    	if(preview.length != 0) this.preview = preview[0];
    	
    	ready = false;
    	
    	try {
    		// Try get owner's name on the server.
    		this.bag = PDC.getString(bagItem, "owner") + "/" + PDC.getString(bagItem, "uuid");
    	} catch (Exception e) {
    		// Otherwise use MojangAPI
    		if(!PDC.getString(bagItem, "owner").equalsIgnoreCase("ownerless")) {
    			this.bag = PDC.getString(bagItem, "owner") + "/" + PDC.getString(bagItem, "uuid");
    		} else {
    			this.bagOwner = "ownerless" + "/" + PDC.getString(bagItem, "uuid");
    		}
    	}
    	Log.debug(plugin, "[DI-33] " + "Attempting to create and open bag " + bag);
    	this.plugin = plugin;
    	this.bagItem = bagItem;
    	this.uuid = PDC.getString(bagItem, "uuid");
    	this.bagMeta = bagMeta;
    	this.player = player;
    	this.size = HavenBags.findClosestNine(size);
    	try {
    		// Try get owner's name on the server.
    		this.bagOwner = PDC.getString(bagItem, "owner");
    	} catch (Exception e) {
    		// Otherwise use MojangAPI
    		if(!PDC.getString(bagItem, "owner").equalsIgnoreCase("ownerless")) {
    			this.bagOwner = PDC.getString(bagItem, "owner");
    		} else {
    			this.bagOwner = "ownerless";
    		}
    	}
    	
    	//if(BagData.Contains(PDC.GetString(bagItem, "bag-uuid"))) return;

    	if(!this.preview) checkInstances(); // Check for multiple of the same bags
    	
    	if(Lang.lang.getBool("per-size-title")) {
    		for(int i = 9; i <= 54; i += 9) {
				if(this.size != i) continue;
				String title = Lang.parse(Lang.get("bag-inventory-title-" + String.valueOf(i)), player);
    			inv = Bukkit.createInventory(player, this.size, title.replace("%name%", bagMeta.getDisplayName()));
			}
    		if(inv == null) {
				if(!Utils.IsStringNullOrEmpty(Lang.get("bag-inventory-title"))) {
					inv = Bukkit.createInventory(player, this.size, Lang.parse(Lang.get("bag-inventory-title"), player));
				} else {
					inv = Bukkit.createInventory(player, this.size, bagMeta.getDisplayName());
				}
			}
    	}else {
    		if(!Utils.IsStringNullOrEmpty(Lang.get("bag-inventory-title"))) {
    			inv = Bukkit.createInventory(player, this.size, Lang.parse(Lang.get("bag-inventory-title"), player));
    		} else {
    			inv = Bukkit.createInventory(player, this.size, bagMeta.getDisplayName());
    		}
    	}
        //this.content = JsonUtils.fromJson(Tags.Get(plugin, this.bagMeta.getPersistentDataContainer(), "content", PersistentDataType.STRING).toString());
		//player.sendMessage(content.toString());
		
		BagData.markBagOpen(uuid, bagItem, player, this);

		if(BagData.getDatabase() == DatabaseType.MYSQLPLUS) {
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				try {
		        	this.content = loadContent();
		        	ready = true;
		        }catch(Exception e) {
		        	e.printStackTrace();
		        	inv = null;
		        	bagItem.setAmount(0);
		        	player.sendMessage(Lang.parse(Lang.get("bag-does-not-exist"), player));
		        	return;
		        }
			});
		}else {
			try {
	        	this.content = loadContent();
	        	ready = true;
	        }catch(Exception e) {
	        	e.printStackTrace();
	        	inv = null;
	        	bagItem.setAmount(0);
	        	player.sendMessage(Lang.parse(Lang.get("bag-does-not-exist"), player));
	        	return;
	        }
		}

		BukkitRunnable task = new BukkitRunnable() {
		    @Override
		    public void run() {
		    	if(ready) {
		    		if(content == null) {
		    			player.sendMessage(Lang.parse(Lang.get("prefix") + Lang.get("bag-already-open"), null));
		    			this.cancel();
		    			return;
		    		}
		    		
		    		initializeItems();
		    	
					openInventory(player);
					Bukkit.getPluginManager().callEvent(new BagOpenEvent(inv, player, bagItem, BagData.getBag(uuid, null)));
					this.cancel();
		    	}
		    }
		};

		task.runTaskTimer(Main.plugin, 1, 1);
		
    	
        
        //LoadContent();
        
        if(!this.preview) HavenBags.BagHashes.add(inv.hashCode());
    	//if(!this.preview) Main.activeBags.add(new ActiveBag(this, PDC.GetString(bagItem, "bag-uuid"), player));
    }
    
    void checkInstances() {
    	List<BagGUI> thisUUID = new ArrayList<BagGUI>();
    	for (Data openBag : BagData.getOpenBags()) {
    		Log.debug(plugin, "[DI-34] " + "Open Bag: " + openBag.getUuid() + " - " + PDC.getString(bagItem, "uuid"));
    		if(openBag.getUuid().equalsIgnoreCase(PDC.getString(bagItem, "uuid"))) {
    			thisUUID.add(openBag.getGui());
    		}
    	}
    	//Log.Debug(plugin, "" + thisUUID.size());
    	if(thisUUID.size() > 1) {
    		Log.warning(plugin, "Multiple instances of the same bag is opened by: " + thisUUID.get(0).player.getName() + " & " + thisUUID.get(1).player.getName());
    		Log.warning(plugin, "They might be trying to dupe items. Forcing their bags to close.");
    		for (BagGUI openBag : thisUUID) {
    			//openBag.player.closeInventory();
    			openBag.close(true);
        	}
    		close(true);
    		//player.closeInventory();
    	}
    }
    
	public void initializeItems() {
		try {
			Log.debug(plugin, "[DI-35] " + "Attempting to initialize bag items");
    		for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
    		//if(Main.weight.GetBool("enabled")) {
    		//	inv.setContents(HavenBags.ShowWeight(inv.getContents()));
    		//	player.getInventory().setContents(HavenBags.ShowWeight(player.getInventory().getContents()));
    		//}
		} catch (Exception e) {
			if(e.toString().contains("because \"this.content\" is null")) {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				//Log.Error(plugin, String.format("Failed to load content of bag '%s'. Is the file empty?", bag));
				String errorMessage = 
						ChatColor.RED + "\n################################\n" +
						"THIS IS A CUSTOM ERROR THROWN BY THE PLUGIN, NOT THE SERVER\n" +
						"\n" +
						"Failed to load content of bag\n" +
						ChatColor.GOLD + "'%s.yml'\n" +
						ChatColor.RED + "Please check if the file is empty or missing before reporting any errors.\n" +
						"\n" +
						"################################\n";
				console.sendMessage(String.format(errorMessage, bag));
				for (Data openBag : BagData.getOpenBags()) {
		    		Log.debug(plugin, "[DI-36] " + "Open Bag: " + openBag.getUuid() + " - " + PDC.getString(bagItem, "uuid"));
		    		if(openBag.getUuid() == PDC.getString(bagItem, "uuid")) {
		    			close(true);
		    		}
		    	}
				throw(new NullPointerException(""));
			} else {
				e.printStackTrace();
			}
		}
    }
	
	List<ItemStack> loadContent() {
		Log.debug(plugin, "[DI-37] " + "Attempting to load bag content");

    	String uuid = PDC.getString(this.bagItem, "uuid");
    	String owner = PDC.getString(this.bagItem, "owner");
		if(!Objects.equals(owner, "ownerless")) {
			owner = bagOwner;
    	}
				
		//return HavenBags.LoadBagContentFromServer(uuid, owner, player);
		
		if(BagData.getDatabase() == DatabaseType.MYSQLPLUS) {
			Data data = BagData.getMysql().loadBag(uuid);
			if(data.isOpen()) return null;
			List<ItemStack> content = data.getContent();
			data.setViewer(player); //Extra just to be sure
	    	return content;
		}else {
			return BagData.getBag(uuid, this.bagItem, UpdateSource.PLAYER).getContent();
		}
	}

    public void openInventory(final HumanEntity ent) {
    	try {
    		ent.openInventory(inv);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        if(e.getRawSlot() == -999) return;
        if(e.getHotbarButton() != -1) {
        	Log.debug(plugin, "[DI-38] " + "" + e.getHotbarButton());
        	e.setCancelled(true);
        	return;
        }
        Log.debug(Main.plugin, "[DI-39] " + e.getRawSlot() + "");
        
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
        
        for(ItemStack item : player.getInventory().getContents()) {
			if(HavenBags.isBag(item)) {
				if(uuid.equalsIgnoreCase(HavenBags.getBagUUID(item))) {
					bagItem = item;
				}
			}
		}
        
        //Log.Debug(plugin, "clicked: " + e.getCurrentItem());
        //Log.Debug(plugin, "cursor: " + e.getCursor());
        
      //Check Weight When cursor isnt air, clicked item is null. Therefore we run this before the null check.
        if(cursorItem != null) {        	
        	if(e.getRawSlot() < inv.getSize() && !cursorItem.getType().equals(Material.AIR)) {
        		Log.debug(Main.plugin, "[DI-40] " + "within");
        		if(Main.weight.getBool("enabled")) {
        			Log.debug(Main.plugin, "[DI-41] " + "enabled");
        			List<ItemStack> cont = new ArrayList<ItemStack>();
        			for(int i = 0; i < inv.getSize(); i++) {
        				cont.add(inv.getItem(i));
        			}
        			//Log.Debug(Main.plugin, HavenBags.CanCarry(clickedItem, bagItem, cont) + "");
        			if(!HavenBags.canCarry(cursorItem, bagItem, cont)) {
        				e.setCancelled(true);
        				//e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Parse(Main.weight.GetString("bag-cant-carry")));
        				List<Placeholder> placeholders = new ArrayList<Placeholder>();
        				placeholders.add(new Placeholder("%item%", Main.translator.Translate(cursorItem.getTranslationKey())));
        				placeholders.add(new Placeholder("%weight%",  HavenBags.itemWeight(cursorItem) + ""));
        				placeholders.add(new Placeholder("%remaining%", (PDC.getDouble(bagItem, "weight-limit") - HavenBags.getWeight(cont)) + ""));
        				e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.parse(Main.weight.getString("bag-cant-carry"), placeholders, player));
        				return;
        			}
        		}
        	}
        }
        
        if(clickedItem != null && clickedItem.isSimilar(bagItem)) {
        	e.setCancelled(true);
        	return;
        }
        
        if(PDC.has(clickedItem, "locked")) {
			e.setCancelled(true);
			return;
		}
    	
    	boolean holdingBag = HavenBags.isBag(cursorItem);
    	boolean clickedBag = HavenBags.isBag(clickedItem);
    	
    	if(e.getRawSlot() < inv.getSize() && holdingBag) {
    		if(uuid.equalsIgnoreCase(HavenBags.getBagUUID(cursorItem))) {
    			e.setCancelled(true);
    		}
    	}
    	
		if(e.getRawSlot() < inv.getSize() && holdingBag && !Main.config.getBool("bags-in-bags")){
        	//e.getWhoClicked().closeInventory();
        	//e.getWhoClicked().sendMessage(Name + "§c Bags cannot be placed inside bags.");
        	e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.get("bag-in-bag-error"));
        	e.setCancelled(true);
        }
		else if(e.getRawSlot() > inv.getSize() && clickedBag && e.isShiftClick() && !Main.config.getBool("bags-in-bags")){
            e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.get("bag-in-bag-error"));
            e.setCancelled(true);
        }
        
		Data data = BagData.getBag(uuid, null);
        
        //if (Main.config.GetBool("bags-in-bags") == true) return;
    	if(e.getRawSlot() < inv.getSize() && HavenBags.isItemBlacklisted(cursorItem, data)) {
        	e.setCancelled(true);
    		e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("item-blacklisted"), player));
    		return;
    	}
		else if(e.getRawSlot() > inv.getSize() && e.isShiftClick() && HavenBags.isItemBlacklisted(clickedItem, data)){
            e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.parse(Lang.get("item-blacklisted"), player));
            e.setCancelled(true);
        }
    	
    	if(clickedItem == null) return;
        if(e.getRawSlot() > inv.getSize() && e.isShiftClick()) {
            Log.debug(Main.plugin, "[DI-42] " + "within");
        	if(Main.weight.getBool("enabled") == false) return;
            Log.debug(Main.plugin, "[DI-43] " + "enabled");
            List<ItemStack> cont = new ArrayList<ItemStack>();
            for(int i = 0; i < inv.getSize(); i++) {
        		cont.add(inv.getItem(i));
        	}
            //Log.Debug(Main.plugin, HavenBags.CanCarry(clickedItem, bagItem, cont) + "");
        	if(!HavenBags.canCarry(clickedItem, bagItem, cont)) {
        		e.setCancelled(true);
            	//e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Parse(Main.weight.GetString("bag-cant-carry")));
            	List<Placeholder> placeholders = new ArrayList<Placeholder>();
            	placeholders.add(new Placeholder("%item%", Main.translator.Translate(clickedItem.getTranslationKey())));
            	placeholders.add(new Placeholder("%weight%",  HavenBags.itemWeight(clickedItem) + ""));
            	placeholders.add(new Placeholder("%remaining%", (PDC.getDouble(bagItem, "weight-limit") - HavenBags.getWeight(cont)) + ""));
            	e.getWhoClicked().sendMessage(Lang.get("prefix") + Lang.parse(Main.weight.getString("bag-cant-carry"), placeholders, player));
        		return;
        	}
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inv)) return;
        // Cancel the event to prevent splitting stacks by dragging
    	if(Main.weight.getBool("enabled") == false) return;
        event.setCancelled(true);
        //event.getWhoClicked().sendMessage("Splitting stacks by dragging is not allowed!");
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        if(!preview) close(false);
    }
    
    public boolean close(boolean forced) {
    	if(forced) {
    		Log.warning(plugin, String.format("%s forcefully closed! Attempting to save it and return it to %s!", bag, player.getName()));
    		player.closeInventory();
    		//return;
    	}
    	
    	for(ItemStack item : player.getInventory().getContents()) {
			if(HavenBags.isBag(item)) {
				if(uuid.equalsIgnoreCase(HavenBags.getBagUUID(item))) {
					bagItem = item;
				}
			}
		}
    	
    	//if(!HavenBags.IsBagOpen(bagItem)) return;
    	if(!BagData.isBagOpen(uuid, bagItem)) return false;

    	Sound sound = new Sound(Main.config.getString("sound.close.key"),
    			Main.config.getDouble("sound.close.volume"),
    			Main.config.getDouble("sound.close.pitch"));
    	sound.play(player);
    	
		//SFX.Play(Main.config.GetString("sound.close.key"), 
		//		Main.config.GetDouble("sound.close.volume").floatValue(), 
		//		Main.config.GetDouble("sound.close.pitch").floatValue(), player);
    	
        Log.debug(plugin, "[DI-44] " + "Bag closed, attempting to save bag. (" + bag + ")");

		//if(Main.weight.GetBool("enabled")) {
		//	inv.setContents(HavenBags.HideWeight(inv.getContents()));
        //	player.getInventory().setContents(HavenBags.HideWeight(player.getInventory().getContents()));
		//}
    	
        List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < inv.getSize(); i++) {
    		cont.add(inv.getItem(i));
    	}
        
        if(Main.weight.getBool("enabled")) {
        	HavenBags.hasWeightLimit(bagItem);
        	PDC.setDouble(bagItem, "weight", HavenBags.getWeight(cont));
        }
        
        //HavenBags.UpdateBagItem(bagItem, cont, player);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
            @Override
            public void run(){
            	HavenBags.updateBagItem(bagItem, player);
            }
        }, 1L);
		//GivePlayerBagBack();
		try {
			BagData.updateBag(bagItem, cont);
		}catch(Exception e) {
			BagData.updateBag(uuid, cont);
			Log.error(Main.plugin, String.format("Failed to update bag data completely for %s (Viewer: %s)", uuid, player.getName()));
		}
        
		//HavenBags.WriteBagToServer(bagItem, cont, player);
		/*try {
			for (int i = 0; i < Main.activeBags.size(); i++) {
    			Log.Debug(plugin, "[DI-45] " + "Open Bag: " + Main.activeBags.get(i).uuid + " - " + PDC.GetString(bagItem, "bag-uuid"));
    			if(Main.activeBags.get(i).uuid == PDC.GetString(bagItem, "bag-uuid")) {
    				Main.activeBags.remove(i);
    			}
    		}
		} catch(Exception e) {
			e.printStackTrace();
		}*/
		BagData.markBagClosed(uuid);
		//player.sendMessage("Bag closed");
		Log.debug(plugin, "[DI-46] " + "Remaining Open Bags: " + BagData.getOpenBags().size());
        //Unregister this GUI from listening to event.
    	Log.debug(Main.plugin, "[BagGUI][DI-263] Unregistering listener for " + player.getName());
		HandlerList.unregisterAll(this);
		
		Bukkit.getPluginManager().callEvent(new BagCloseEvent(inv, player, bagItem, BagData.getBag(uuid, null), forced));
		
		//UpdateTimestamp();
		return true;
    }
    
    /*void UpdateTimestamp() {
    	Log.Debug(plugin, "Updating timestamp for " + bag);
    	Main.timeTable.Set(
    		String.format("%s/%s", PDC.GetString(bagItem, "bag-owner"), PDC.GetString(bagItem, "bag-uuid")),
    			System.currentTimeMillis() / 1000L);
    	Main.timeTable.SaveConfig();
    }*/
    
    // Obsolete, removed feature.
    /*void GivePlayerBagBack() {
    	if(!Main.config.GetBool("keep-bags")){ 
    		HavenBags.ReturnBag(bagItem, player);
    	}
    }*/
    
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();
        ItemStack item = droppedItem.getItemStack();
        if(HavenBags.isBag(item)) {
        	if(BagData.isBagOpen(item)) {
        		event.setCancelled(true);
        	}
        }
    }

    /*
    void WriteToServer() {
    	String uuid = PDC.GetString(bagItem, "bag-uuid");
    	String owner = PDC.GetString(bagItem, "bag-owner");
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
