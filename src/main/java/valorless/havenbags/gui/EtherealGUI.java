package valorless.havenbags.gui;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import valorless.valorlessutils.sound.SFX;
import valorless.havenbags.*;
import valorless.havenbags.database.EtherealBags;
import valorless.havenbags.datamodels.EtherealBagSettings;
import valorless.havenbags.features.AutoSorter;
import valorless.valorlessutils.ValorlessUtils.Log;


public class EtherealGUI implements Listener {
	private String key;
	private Inventory inv;
	public final String bagId;
	public List<ItemStack> content;
	public Player player;
	public int size;
	
	public EtherealBagSettings settings;
	
    public EtherealGUI(Player player, String bagId, Player viewer) {
		this.key = EtherealBags.formatBagId(player.getUniqueId(), bagId);
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.plugin);
		
		//Log.Info(Main.plugin, player.getName());
		//Log.Info(Main.plugin, bagId);
    	
    	Log.Debug(Main.plugin, "[EtherealGUI][DI-294] " + "Attempting to create and open ethereal bag " + this.key);
    	this.bagId = bagId;
    	this.player = player;
    	this.content = EtherealBags.getBagContentsOrNull(player.getUniqueId(), bagId);
    	if(this.content == null) {
    		viewer.sendMessage(Lang.Parse(Lang.Get("bag-does-not-exist"), viewer));
			HandlerList.unregisterAll(this);
			return;
		}
    	this.size = this.content.size();
    	this.settings = EtherealBags.getBagSettings(player.getUniqueId(), bagId);
    	Log.Debug(Main.plugin, "[EtherealGUI][DI-302] " + "Ethereal bag settings: " + settings.toString());
    	
    	EtherealBags.openGUIs.add(this);
    	
    	inv = Bukkit.createInventory(player, size, bagId);

    	InitializeItems();
    	
		//OpenInventory(player);
		
		SFX.Play(Main.config.GetString("sound.open.key"), 
				Main.config.GetDouble("sound.open.volume").floatValue(), 
				Main.config.GetDouble("sound.open.pitch").floatValue(), player);
    }
    
	public void InitializeItems() {
		Log.Debug(Main.plugin, "[EtherealGUI][DI-295] " + "Attempting to initialize ethereal bag items");
		for(int i = 0; i < content.size(); i++) {
			inv.setItem(i, content.get(i));
		}
    }

    public void OpenInventory(final HumanEntity ent) {
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
        	e.setCancelled(true);
        	return;
        }
        
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
    	
    	boolean holdingBag = HavenBags.IsBag(cursorItem);	
    	boolean clickedBag = HavenBags.IsBag(clickedItem);
    	
		if(e.getRawSlot() < inv.getSize() && holdingBag && !Main.config.GetBool("bags-in-bags")){
        	//e.getWhoClicked().closeInventory();
        	//e.getWhoClicked().sendMessage(Name + "§c Bags cannot be placed inside bags.");
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-bag-error"));
        	e.setCancelled(true);
        }
		else if(e.getRawSlot() > inv.getSize() && clickedBag && e.isShiftClick() && !Main.config.GetBool("bags-in-bags")){
            e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-bag-error"));
            e.setCancelled(true);
        }
        
        
        //if (Main.config.GetBool("bags-in-bags") == true) return;
    	if(e.getRawSlot() < inv.getSize() && HavenBags.IsItemBlacklisted(cursorItem)) {
        	e.setCancelled(true);
    		e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Parse(Lang.Get("item-blacklisted"), player));
    		return;
    	}
		else if(e.getRawSlot() > inv.getSize() && e.isShiftClick() && HavenBags.IsItemBlacklisted(clickedItem)){
            e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-bag-error"));
            e.setCancelled(true);
        }
    	
    	if(clickedItem == null) return;
    }
    
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        Close(false);
    }
    
    public void Close(boolean forced) {
    	if(forced) {
    		Log.Warning(Main.plugin, String.format("[EtherealGUI][DI-296] %s forcefully closed!", key));
    		player.closeInventory();
    	}

		SFX.Play(Main.config.GetString("sound.close.key"), 
				Main.config.GetDouble("sound.close.volume").floatValue(), 
				Main.config.GetDouble("sound.close.pitch").floatValue(), player);
    	
        Log.Debug(Main.plugin, "[EtherealGUI][DI-297] " + "Bag closed, attempting to save ethereal bag. (" + key + ")");
        
        if(settings.autoSort) {
			Log.Debug(Main.plugin, "[EtherealGUI][DI-301] " + "Auto-sorting enabled, sorting bag contents. (" + key + ")");
			List<ItemStack> sorted = AutoSorter.SortInventory(Arrays.asList(inv.getContents()));
			inv.setContents(sorted.toArray(new ItemStack[0]));
		}
        
        if(!EtherealBags.updateBagContents(player.getUniqueId(), bagId, Arrays.asList(inv.getContents()))) {
			Log.Error(Main.plugin, "[EtherealGUI][DI-298] " + "Failed to save ethereal bag contents! (" + key + ")");
		} else {
			Log.Debug(Main.plugin, "[EtherealGUI][DI-299] " + "Successfully saved ethereal bag contents. (" + key + ")");
		}
		
        //Unregister this GUI from listening to event.
    	Log.Debug(Main.plugin, "[EtherealGUI][DI-300] Unregistering listener for " + player.getName());
		HandlerList.unregisterAll(this);
		
		EtherealBags.openGUIs.remove(this);
    }
    
}
