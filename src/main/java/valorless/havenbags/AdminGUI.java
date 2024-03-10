package valorless.havenbags;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

import valorless.havenbags.GUI.GUIAction;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.utils.Utils;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;
import valorless.valorlessutils.skulls.SkullCreator;

public class AdminGUI implements Listener {
	public enum GUIType { Main, Creation, Restoration, Player, Preview, PreviewPlayer, Deletion, DeletionPlayer, Confirmation }
	
	public JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	private Inventory inv;
	private Player player;
	private String target;
	private OfflinePlayer targetPlayer;
	private GUIType type;
	private List<ItemStack> content = new ArrayList<ItemStack>();
	private ItemStack selectedBag;
	private int page = 1;
	
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
		else if(type == GUIType.Restoration || type == GUIType.Preview || type == GUIType.Deletion) {
			content = PrepareBags();
			Open();
		}
		else if(type == GUIType.Player || type == GUIType.PreviewPlayer || type == GUIType.DeletionPlayer) {
			try {
				content = PreparePlayerBags(target);
			} catch (Exception e) {
				player.sendMessage(Lang.Get("prefix") + Lang.Get("too-many-bags"));
				player.closeInventory();
				e.printStackTrace();
				return;
			}
			Open();
		}else if(type == GUIType.Confirmation) {
			content = PrepareConfirmation();
			Open();
		}
	}
	
	void Open() {
		//Log.Debug(plugin, type.toString());
		
		if(type == GUIType.Main) {
			inv = Bukkit.createInventory(player, 9, Lang.Get("gui-main"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);
		}
		else if(type == GUIType.Creation) {
			inv = Bukkit.createInventory(player, 18, Lang.Get("gui-create"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);
		}
		else if(type == GUIType.Restoration) {
			page = 1;
			inv = GUI.CreatePage(player, Lang.Get("gui-restore"),
					page, content, 6);
			player.openInventory(inv);
			/*inv = Bukkit.createInventory(player, 54, Lang.Get("gui-restore"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);*/
		}
		else if(type == GUIType.Preview) {
			page = 1;
			inv = GUI.CreatePage(player, Lang.Get("gui-preview"),
					page, content, 6);
			player.openInventory(inv);
			/*inv = Bukkit.createInventory(player, 54, Lang.Get("gui-preview"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);*/
		}
		else if(type == GUIType.Deletion) {
			page = 1;
			inv = GUI.CreatePage(player, Lang.Get("gui-delete"),
					page, content, 6);
			player.openInventory(inv);
			/*inv = Bukkit.createInventory(player, 54, Lang.Get("gui-delete"));
			for(int i = 0; i < content.size(); i++) {
    			inv.setItem(i, content.get(i));
    		}
			player.openInventory(inv);*/
		}
		else if(type == GUIType.Player || type == GUIType.PreviewPlayer || type == GUIType.DeletionPlayer) {
			page = 1;
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
			if(type == GUIType.Player) {
				if(target.equalsIgnoreCase("ownerless")) {
					inv = GUI.CreatePage(player, Lang.Get("gui-restore"),
							page, content, 6);
				}else {
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
							page, content, 6);
				}
			}else if(type == GUIType.PreviewPlayer) {
				if(target.equalsIgnoreCase("ownerless")) {
					inv = GUI.CreatePage(player, Lang.Get("gui-preview"),
							page, content, 6);
				}else {
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
							page, content, 6);
				}
			}else if(type == GUIType.DeletionPlayer) {
				if(target.equalsIgnoreCase("ownerless")) {
					inv = GUI.CreatePage(player, Lang.Get("gui-delete"),
							page, content, 6);
				}else {
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
							page, content, 6);
				}
			}
			/*inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
					page, content, 6);*/
			player.openInventory(inv);
			//inv = Bukkit.createInventory(player, 54, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()));
			//for(int i = 0; i < content.size(); i++) {
    		//	inv.setItem(i, content.get(i));
    		//}
		}
		else if(type == GUIType.Confirmation) {
			inv = Bukkit.createInventory(player, 9, Lang.Get("gui-confirm"));
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
        
        if(e.getRawSlot() >= inv.getSize()) return;

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
        	else if(action.equalsIgnoreCase("preview")){
        		type = GUIType.Preview;
            	Reload();
        	}
        	else if(action.equalsIgnoreCase("delete")){
        		type = GUIType.Deletion;
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
        	GUIAction action = null;
        	try {
        		action = GUIAction.valueOf(NBT.GetString(clickedItem, "bag-action"));
        	} catch(Exception E) {}
        	
        	if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					Reload();
					return;
				}
				
				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Get("gui-restore"),
							page, content, 6);
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Get("gui-restore"),
							page, content, 6);
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
        	}
        	
        	String owner = clickedItem.getItemMeta().getDisplayName();
        	Log.Debug(plugin, "Changing Admin target to " + owner);
        	if(owner.equalsIgnoreCase("ownerless")) {
        		target = "ownerless";
        	}else {
        		target = Bukkit.getPlayer(owner).getUniqueId().toString();
        	}
        	type = GUIType.Player;
        	Reload();
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.Preview) {
        	GUIAction action = null;
        	try {
        		action = GUIAction.valueOf(NBT.GetString(clickedItem, "bag-action"));
        	} catch(Exception E) {}
        	
        	if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					Reload();
					return;
				}
				
				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Get("gui-preview"),
							page, content, 6);
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Get("gui-preview"),
							page, content, 6);
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
        	}
        	
        	String owner = clickedItem.getItemMeta().getDisplayName();
        	Log.Debug(plugin, "Changing Admin target to " + owner);
        	if(owner.equalsIgnoreCase("ownerless")) {
        		target = "ownerless";
        	}else {
        		target = Bukkit.getPlayer(owner).getUniqueId().toString();
        	}
        	type = GUIType.PreviewPlayer;
        	Reload();
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.Deletion) {
        	GUIAction action = null;
        	try {
        		action = GUIAction.valueOf(NBT.GetString(clickedItem, "bag-action"));
        	} catch(Exception E) {}
        	
        	if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Main;
					Reload();
					return;
				}
				
				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Get("gui-delete"),
							page, content, 6);
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
					inv = GUI.CreatePage(player, Lang.Get("gui-delete"),
							page, content, 6);
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
        	}
        	
        	String owner = clickedItem.getItemMeta().getDisplayName();
        	Log.Debug(plugin, "Changing Admin target to " + owner);
        	if(owner.equalsIgnoreCase("ownerless")) {
        		target = "ownerless";
        	}else {
        		target = Bukkit.getPlayer(owner).getUniqueId().toString();
        	}
        	type = GUIType.DeletionPlayer;
        	Reload();
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.Player) {
        	GUIAction action = null;
        	try {
        		action = GUIAction.valueOf(NBT.GetString(clickedItem, "bag-action"));
        	} catch(Exception E) {}
        	
        	if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Restoration;
					Reload();
					return;
				}
				
				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;
					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.CreatePage(player, Lang.Get("gui-restore"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;
					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.CreatePage(player, Lang.Get("gui-restore"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
        	}
        	
        	ItemStack giveItem = clickedItem.clone();
        	player.getInventory().addItem(giveItem);
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.PreviewPlayer) {
        	GUIAction action = null;
        	try {
        		action = GUIAction.valueOf(NBT.GetString(clickedItem, "bag-action"));
        	} catch(Exception E) {}
        	
        	if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Preview;
					Reload();
					return;
				}
				
				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.CreatePage(player, Lang.Get("gui-preview"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.CreatePage(player, Lang.Get("gui-preview"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
        	}
        	
        	String owner = NBT.GetString(clickedItem, "bag-owner");
        	String uuid = NBT.GetString(clickedItem, "bag-uuid");
        	int size = NBT.GetInt(clickedItem, "bag-size");

			String dirPath = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
			File dir = new File(dirPath);
			if(!dir.exists()) {
				return;
			}
			String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, uuid);
			File bagData;
			try {
				bagData = new File(path);
			} catch(Exception ex) {
				ex.printStackTrace();
				return;
			}
			if(!bagData.exists()) {
				return;
			}
			BagGUI gui = new BagGUI(Main.plugin, size, player, clickedItem, clickedItem.getItemMeta(), true);
			gui.OpenInventory(player);
        	e.setCancelled(true);
        	return;
        }
        
        if (type == GUIType.DeletionPlayer) {        	
        	GUIAction action = null;
        	try {
        		action = GUIAction.valueOf(NBT.GetString(clickedItem, "bag-action"));
        	} catch(Exception E) {}
        	
        	if(action != null) {
				if(action.equals(GUIAction.RETURN)){
					type = GUIType.Deletion;
					Reload();
					return;
				}
				
				if(action.equals(GUIAction.PREV_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page--;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.CreatePage(player, Lang.Get("gui-delete"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NEXT_PAGE)){
					List<Placeholder> placeholders = new ArrayList<Placeholder>();
					page++;

					if(target.equalsIgnoreCase("ownerless")) {
						inv = GUI.CreatePage(player, Lang.Get("gui-delete"),
								page, content, 6);
					}else {
						placeholders.add(new Placeholder("%player%", targetPlayer.getName()));
						inv = GUI.CreatePage(player, Lang.Parse(Lang.Get("bags-of"), placeholders, targetPlayer.getPlayer()),
								page, content, 6);
					}
					player.openInventory(inv);
				}
				
				if(action.equals(GUIAction.NONE)){
					e.setCancelled(true);
					return;
				}
        	}
        	
        	selectedBag = clickedItem;
    		type = GUIType.Confirmation;
        	Reload();
        	e.setCancelled(true);
        	return;
        }
        if (type == GUIType.Confirmation) {
        	String action = NBT.GetString(clickedItem, "bag-action");
        	if(action.equalsIgnoreCase("cancel")){
        		type = GUIType.DeletionPlayer;
            	Reload();
            	return;
        	}
        	
        	if(action.equalsIgnoreCase("confirm")){
        		String owner = NBT.GetString(selectedBag, "bag-owner");
            	String uuid = NBT.GetString(selectedBag, "bag-uuid");

    			String dirPath = String.format("%s/bags/%s/", Main.plugin.getDataFolder(), owner);
    			File dir = new File(dirPath);
    			if(!dir.exists()) {
    				return;
    			}
    			String path = String.format("%s/bags/%s/%s.json", Main.plugin.getDataFolder(), owner, uuid);
    			File bagData;
    			try {
    				bagData = new File(path);
    			} catch(Exception ex) {
    				ex.printStackTrace();
    				return;
    			}
    			if(!bagData.exists()) {
    				return;
    			}
    			
    			if(bagData.delete()) {
    				Log.Info(plugin, String.format("Bag '%s/%s' successfully deleted.", owner, uuid));
    			}else {
    				Log.Info(plugin, String.format("Could not delete bag '%s/%s'.", owner, uuid));
    			}
        		
        		type = GUIType.DeletionPlayer;
            	Reload();
            	return;
        	}
        	        	
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
		
		//Create
		String cresteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19";
		ItemStack createItem = SkullCreator.itemFromBase64(cresteTexture);
		ItemMeta createMeta = createItem.getItemMeta();
		createMeta.setDisplayName(Lang.Get("main-create"));
		List<String> c_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("main-create-lore")) {
			c_lore.add(Lang.Parse(line, targetPlayer));
		}
		//c_lore.add("§7Create bags easy.");
		createMeta.setLore(c_lore);
		createItem.setItemMeta(createMeta);
		NBT.SetString(createItem, "bag-action", "create");
		buttons.add(createItem);
		
		buttons.add(new ItemStack(Material.AIR));
		
		//Restore
		String restoreTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZGMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0=";
		ItemStack restoreItem = SkullCreator.itemFromBase64(restoreTexture);
		ItemMeta restoreMeta = restoreItem.getItemMeta();
		restoreMeta.setDisplayName(Lang.Get("main-restore"));
		List<String> r_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("main-restore-lore")) {
			r_lore.add(Lang.Parse(line, targetPlayer));
		}
		//r_lore.add("§7Restore bags of online players.");
		restoreMeta.setLore(r_lore);
		restoreItem.setItemMeta(restoreMeta);
		NBT.SetString(restoreItem, "bag-action", "restore");
		buttons.add(restoreItem);
		
		buttons.add(new ItemStack(Material.AIR));
		
		//Preview
		String previewTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZlM2JjYmE1N2M3YjdmOGQ0NjJiMzAwNTQzZDEzMmVjZWE5YmYyZWQ1ODdjYzlkOTk0YTM5NWFjOTU5MmVhYSJ9fX0=";
		ItemStack previewItem = SkullCreator.itemFromBase64(previewTexture);
		ItemMeta previewMeta = previewItem.getItemMeta();
		previewMeta.setDisplayName(Lang.Get("main-preview"));
		List<String> p_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("main-preview-lore")) {
			p_lore.add(Lang.Parse(line, targetPlayer));
		}
		/*p_lore.add("§7Preview bags of online players.");
		p_lore.add("§7You can take items from the preview,");
		p_lore.add("§7without affecting the real bag.");*/
		previewMeta.setLore(p_lore);
		previewItem.setItemMeta(previewMeta);
		NBT.SetString(previewItem, "bag-action", "preview");
		buttons.add(previewItem);

		buttons.add(new ItemStack(Material.AIR));
		
		//Deletion
		String deleteTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwZmQxMDE5OWU4ZTRmY2RhYmNhZTRmODVjODU5MTgxMjdhN2M1NTUzYWQyMzVmMDFjNTZkMThiYjk0NzBkMyJ9fX0=";
		ItemStack deleteItem = SkullCreator.itemFromBase64(deleteTexture);
		ItemMeta deleteMeta = deleteItem.getItemMeta();
		deleteMeta.setDisplayName(Lang.Get("main-delete"));
		List<String> d_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("main-delete-lore")) {
			d_lore.add(Lang.Parse(line, targetPlayer));
		}
		deleteMeta.setLore(d_lore);
		deleteItem.setItemMeta(deleteMeta);
		NBT.SetString(deleteItem, "bag-action", "delete");
		buttons.add(deleteItem);

		buttons.add(new ItemStack(Material.AIR));
		
		//Info
		String infoTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjcwNWZkOTRhMGM0MzE5MjdmYjRlNjM5YjBmY2ZiNDk3MTdlNDEyMjg1YTAyYjQzOWUwMTEyZGEyMmIyZTJlYyJ9fX0=";
		ItemStack infoItem = SkullCreator.itemFromBase64(infoTexture);
		ItemMeta infoMeta = infoItem.getItemMeta();
		infoMeta.setDisplayName(Lang.Get("main-info"));
		List<String> I_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("main-info-lore")) {
			I_lore.add(Lang.Parse(line, targetPlayer));
		}
		/*I_lore.add("§7The Admin GUI does not allow restoration of ownerless bags,");
		I_lore.add("§7you have to do those manually with §e/bags restore§7.");
		I_lore.add("");
		I_lore.add("§7If a player has over 53 bags,");
		I_lore.add("§7you also have to manually restore them.");
		I_lore.add("");
		I_lore.add("§7You can also restore bags of offline players,");
		I_lore.add("§7by using §e/bags gui restore <username>§7.");*/
		infoMeta.setLore(I_lore);
		infoItem.setItemMeta(infoMeta);
		buttons.add(infoItem);
		
		return buttons;
	}
    
    public void modifyMaxStack(ItemStack item, int amount) {
    	/*
    	try {
    		
    		Field f = ItemStack.class.getDeclaredField("maxStackSize");
    		f.setAccessible(true);
    		f.setInt(item, amount);
    	} catch (Exception e) {
    		e.printStackTrace();
    		return;
    	}
    	*/
    }

	ArrayList<ItemStack> PrepareTemplates() {
    	for(Field f : ItemStack.class.getDeclaredFields()) {
    		Log.Debug(plugin, f.getName() + " - " + f.getType());
    	}
		ArrayList<ItemStack> templates = new ArrayList<ItemStack>();
		
		//Bound
		for(int i = 1; i <= 6; i++) {
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
			String bagTexture = Main.config.GetString("bag-texture");
			ItemStack bagItem = new ItemStack(Material.AIR);
			
			modifyMaxStack(bagItem, 1);
			
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
			placeholders.add(new Placeholder("%size%", size));
        	lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
			//for (String l : Lang.lang.GetStringList("bag-size")) {
			//	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size), player));
			//}
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
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
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
			placeholders.add(new Placeholder("%size%", size));
        	lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, player));
			//for (String l : Lang.lang.GetStringList("bag-size")) {
			///	if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(String.format(l, size), player));
			//}
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
		returnMeta.setDisplayName(Lang.Get("return"));
		List<String> r_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("return-lore")) {
			r_lore.add(Lang.Parse(line, targetPlayer));
		}
		//r_lore.add("§7Go back.");
		returnMeta.setLore(r_lore);
		returnItem.setItemMeta(returnMeta);
		NBT.SetString(returnItem, "bag-action", "return");
		templates.add(returnItem);
		
		
		return templates;
	}
	
	List<ItemStack> PrepareBags() {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		//Collection<? extends Player> p = Bukkit.getOnlinePlayers();
		//List<Player> players = new ArrayList<>(p);
		
		String bagTexture = Main.config.GetString("bag-texture");
		ItemStack ownerless = SkullCreator.itemFromBase64(bagTexture);
		ItemMeta ownerlessmeta = ownerless.getItemMeta();
		ownerlessmeta.setDisplayName("Ownerless");
		ownerless.setItemMeta(ownerlessmeta);
		NBT.SetString(ownerless, "bag-owner", "ownerless");
		bags.add(ownerless);
		
		/*for(int i = 0; i < players.size(); i++) {
			ItemStack entry = SkullCreator.itemFromUuid(players.get(i).getUniqueId());
			ItemMeta meta = entry.getItemMeta();
			meta.setDisplayName(players.get(i).getName());
			entry.setItemMeta(meta);
			NBT.SetString(entry, "bag-owner", players.get(i).getUniqueId().toString());
			bags.add(entry);
		}*/
		
		
			
			
		for(Player p : Bukkit.getOnlinePlayers()){
			ItemStack entry = SkullCreator.itemFromUuid(p.getUniqueId());
			ItemMeta meta = entry.getItemMeta();
			meta.setDisplayName(p.getName());
			entry.setItemMeta(meta);
			NBT.SetString(entry, "bag-owner", p.getUniqueId().toString());
			bags.add(entry);
		}
		
		return bags;
	}
	
	List<ItemStack> PreparePlayerBags(String playeruuid) {
		List<ItemStack> bags = new ArrayList<ItemStack>();
		List<String> bagfiles = GetBags(playeruuid);
		
		//if(!playeruuid.equalsIgnoreCase("ownerless")) {
		for(String bagUUID : bagfiles){
			List<ItemStack> Content  = LoadContent(playeruuid, bagUUID);
			if (Content == null) continue;
			List<Placeholder> placeholders = new ArrayList<Placeholder>();
			
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
			if(playeruuid.equalsIgnoreCase("ownerless")) {
				NBT.SetBool(bagItem, "bag-canBind", false);
			}else {
				NBT.SetBool(bagItem, "bag-canBind", true);
			}
			
			ItemMeta bagMeta = bagItem.getItemMeta();
			if(Main.config.GetInt("bag-custom-model-data") != 0) {
				bagMeta.setCustomModelData(Main.config.GetInt("bag-custom-model-data"));
			}
			
			if(NBT.GetBool(bagItem, "bag-canBind")) {
				bagMeta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-bound-name"), targetPlayer));
			}else {
				bagMeta.setDisplayName(Lang.Parse(Lang.lang.GetString("bag-ownerless-used"), targetPlayer));
			}
			List<String> lore = new ArrayList<String>();
			for (String l : Lang.lang.GetStringList("bag-lore")) {
				if(!Utils.IsStringNullOrEmpty(l)) lore.add(Lang.Parse(l, targetPlayer));
			}
			if(NBT.GetBool(bagItem, "bag-canBind")) {
				placeholders.add(new Placeholder("%owner%", player.getName()));
	            lore.add(Lang.Parse(Lang.Get("bound-to"), placeholders, player));
	        }
			
            placeholders.add(new Placeholder("%size%", size));
            lore.add(Lang.Parse(Lang.Get("bag-size"), placeholders, targetPlayer));
			
			List<ItemStack> cont = new ArrayList<ItemStack>();
	        int a = 0;
			List<String> items = new ArrayList<String>();
	        for(int i = 0; i < Content.size(); i++) {
	    		cont.add(Content.get(i));
	    		if(Content.get(i) != null) {
	    			List<Placeholder> itemph = new ArrayList<Placeholder>();
	    			if(Content.get(i).getItemMeta().hasDisplayName()) {
    					itemph.add(new Placeholder("%item%", Content.get(i).getItemMeta().getDisplayName()));
    					itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
    					if(Content.get(i).getAmount() != 1) {
    						items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
    					} else {
    						items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
    					}
	    			}else {
    	    			itemph.add(new Placeholder("%item%", Main.translator.Translate(Content.get(i).getType().getTranslationKey())));
    	    			itemph.add(new Placeholder("%amount%", Content.get(i).getAmount()));
    	    			if(Content.get(i).getAmount() != 1) {
        					items.add(Lang.Parse(Lang.Get("bag-content-item-amount"), itemph, player));
        				} else {
        					items.add(Lang.Parse(Lang.Get("bag-content-item"), itemph, player));
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
		//}
		
		return bags;
	}
	
	ArrayList<ItemStack> PrepareConfirmation() {
		ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();
		
		//Cancel
		String cancelTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=";
		ItemStack cancelItem = SkullCreator.itemFromBase64(cancelTexture);
		ItemMeta cancelMeta = cancelItem.getItemMeta();
		cancelMeta.setDisplayName(Lang.Get("confirm-cancel"));
		List<String> c_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("confirm-cancel-lore")) {
			c_lore.add(Lang.Parse(line, targetPlayer));
		}
		cancelMeta.setLore(c_lore);
		cancelItem.setItemMeta(cancelMeta);
		NBT.SetString(cancelItem, "bag-action", "cancel");
		buttons.add(cancelItem);
		
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));

		buttons.add(selectedBag);
		
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));
		buttons.add(new ItemStack(Material.AIR));

		//Confirm
		String comfirmTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=";
		ItemStack confirmItem = SkullCreator.itemFromBase64(comfirmTexture);
		ItemMeta confirmMeta = confirmItem.getItemMeta();
		confirmMeta.setDisplayName(Lang.Get("confirm-confirm"));
		List<String> co_lore = new ArrayList<String>();
		for(String line : Lang.lang.GetStringList("confirm-confirm-lore")) {
			co_lore.add(Lang.Parse(line, targetPlayer));
		}
		confirmMeta.setLore(co_lore);
		confirmItem.setItemMeta(confirmMeta);
		NBT.SetString(confirmItem, "bag-action", "confirm");
		buttons.add(confirmItem);
		
		return buttons;
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
