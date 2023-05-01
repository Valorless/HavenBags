package valorless.havenbags;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.skulls.SkullCreator;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.ValorlessUtils.Tags;

public class BagGUI implements Listener {
	public JavaPlugin plugin;
	String Name = "§7[§aHaven§bBags§7]§r";
	private final Inventory inv;
	public ItemStack bagItem;
	public SkullMeta bagMeta;
	public List<ItemStack> content;
	public static Config config;
	public Player player;
	
	public class Items {
		public String name = "";
		public String item = "";
		public List<String> lore = new ArrayList<String>();
		public Boolean interactable = false;
		public String tag;
	}

    public BagGUI(JavaPlugin plugin, int size, Player player, ItemStack bagItem, SkullMeta bagMeta) {
    	this.plugin = plugin;
    	this.bagItem = bagItem;
    	this.bagMeta = bagMeta;
    	this.player = player;
    	
        inv = Bukkit.createInventory(player, size, bagMeta.getDisplayName());
        //this.content = JsonUtils.fromJson(Tags.Get(plugin, this.bagMeta.getPersistentDataContainer(), "content", PersistentDataType.STRING).toString());
		//player.sendMessage(content.toString());

        this.content = LoadContent();
        
        InitializeItems();
        //LoadContent();
    }
    
	public void InitializeItems() {
    	for(int i = 0; i < content.size(); i++) {
    		inv.setItem(i, content.get(i));
    	}
    }
	
	List<ItemStack> LoadContent() {
		String uuid = Tags.Get(plugin, this.bagMeta.getPersistentDataContainer(), "uuid", PersistentDataType.STRING).toString();
		String owner = Tags.Get(plugin, this.bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING).toString();
		if(owner != "ownerless") {
			owner = Bukkit.getPlayer(UUID.fromString(owner)).getName();
    	}
		//owner = Bukkit.getPlayer(UUID.fromString(owner)).getName();
		String path = String.format("%s/bags/%s/%s.json", plugin.getDataFolder(), owner, uuid);
		
		File bagData;
		try {
			bagData = new File(path);
		} catch(Exception e) {
			player.sendMessage(e.toString());
			e.printStackTrace();
			return null;
		}
        if(!bagData.exists()) {
        	//player.sendMessage(Name + "§c No bag found with that UUID.");
        	player.sendMessage(Lang.Get("bag-does-not-exist"));
        	return null;
        }
        String content = "";
		try {
			Path filePath = Path.of(path);
			content = Files.readString(filePath);
			return JsonUtils.fromJson(content);
		} catch (IOException e) {
			player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:LoadContent()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
			return null;
		}
		//return JsonUtils.fromJson(content);
	}

    public void OpenInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        
        if(Tags.Get(plugin, clickedItem.getItemMeta().getPersistentDataContainer(), "uuid", PersistentDataType.STRING) != null) {
        	e.getWhoClicked().closeInventory();
        	//e.getWhoClicked().sendMessage(Name + "§c Bags cannot be placed inside bags.");
        	e.getWhoClicked().sendMessage(Lang.Get("prefix") + Lang.Get("bag-in-bag-error"));
        	e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        List<ItemStack> cont = new ArrayList<ItemStack>();
        int a = 0;
        List<String> items = new ArrayList<String>();
        for(int i = 0; i < inv.getSize(); i++) {
    		cont.add(inv.getItem(i));
    		if(inv.getItem(i) != null) {
    			if(inv.getItem(i).getItemMeta().hasDisplayName()) {
    				if(inv.getItem(i).getAmount() != 1) {
    					//items.add("§7" + inv.getItem(i).getItemMeta().getDisplayName() + " §7x" + inv.getItem(i).getAmount());
    					items.add(Lang.Get("bag-content-item-amount", inv.getItem(i).getItemMeta().getDisplayName(), inv.getItem(i).getAmount()));
    				} else {
    					//items.add("§7" + inv.getItem(i).getItemMeta().getDisplayName());
    					items.add(Lang.Get("bag-content-item", inv.getItem(i).getItemMeta().getDisplayName()));
    				}
    			}else {
    				if(inv.getItem(i).getAmount() != 1) {
    					//items.add("§7" + FixMaterialName(inv.getItem(i).getType().name()) + " §7x" + inv.getItem(i).getAmount());
    					items.add(Lang.Get("bag-content-item-amount", FixMaterialName(inv.getItem(i).getType().name()), inv.getItem(i).getAmount()));
    				} else {
    					//items.add("§7" + FixMaterialName(inv.getItem(i).getType().name()));
    					items.add(Lang.Get("bag-content-item", FixMaterialName(inv.getItem(i).getType().name())));
    				}
    			}
    			a++;
    		}
    	}
        List<String> lore = new ArrayList<String>();
        if(Tags.Get(HavenBags.plugin, bagMeta.getPersistentDataContainer(), "canbind", PersistentDataType.STRING).toString() != "false") {
        	//lore.add(String.format("§7Bound to %s", e.getPlayer().getName()));
        	lore.add(Lang.Get("bound-to", e.getPlayer().getName()));
        }
        //lore.add("§7Size: " + inv.getSize());
        lore.add(Lang.Get("bag-size", inv.getSize()));
        if(a > 0) {
        	//lore.add("§7Content:");
        	lore.add(Lang.Get("bag-content-title"));
        	for(int k = 0; k < items.size(); k++) {
        		if(k < 5) {
        			lore.add("  " + items.get(k));
        		}
        	}
        	if(a > 5) {
        		//lore.add("  §7And more..");
        		lore.add(Lang.Get("bag-content-and-more"));
        	}
        }
        bagMeta.setLore(lore);
        
		//Tags.Set(plugin, bagMeta.getPersistentDataContainer(), "content", JsonUtils.toJson(cont), PersistentDataType.STRING);
		//bagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		//bagMeta.addEnchant(Enchantment.LUCK, 1, true);
		bagItem.setItemMeta(bagMeta);
		WriteToServer();
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
    
    void WriteToServer() {
    	String uuid = Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "uuid", PersistentDataType.STRING).toString();
    	String owner = Tags.Get(plugin, bagMeta.getPersistentDataContainer(), "owner", PersistentDataType.STRING).toString();
		if(owner != "ownerless") {
    		player = Bukkit.getPlayer(UUID.fromString(owner));
    	}
    	File bagData;
    	List<ItemStack> cont = new ArrayList<ItemStack>();
        for(int i = 0; i < inv.getSize(); i++) {
    		cont.add(inv.getItem(i));
    	}
    	if(owner != "ownerless") {
    		bagData = new File(plugin.getDataFolder() + "/bags/", player.getName() + "/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Info(plugin, String.format("Bag data for (%s) %s does not exist, creating new.", player.getName(), uuid));
            }
    	}else {
    		bagData = new File(plugin.getDataFolder() + "/bags/", owner + "/" + uuid + ".json");
    		if(!bagData.exists()) {
            	bagData.getParentFile().mkdirs();
                Log.Info(plugin, String.format("Bag data for (%s) %s does not exist, creating new.", owner, uuid));
            }
    	}
        
        FileWriter fw;
		try {
			fw = new FileWriter(bagData);
	        fw.write(JsonUtils.toPrettyJson(cont));
	        fw.flush();
	        fw.close();
		} catch (IOException e) {
			player.sendMessage(Name + "§c Something went wrong! \n§fPlayer tell the owner this: '§eHavenBags:BagGUI:WriteToServer()§f'. \nThank you! §4❤§r");
			e.printStackTrace();
		}
    }
}