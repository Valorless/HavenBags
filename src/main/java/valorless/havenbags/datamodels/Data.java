package valorless.havenbags.datamodels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import valorless.havenbags.annotations.DoNotCall;
import valorless.havenbags.gui.BagGUI;

/**
 * Data model representing a HavenBag's persistent and runtime state.
 * Includes identity (uuid/owner/creator), visual configuration (material, name,
 * texture, custom model), inventory contents, feature flags (autopickup, weight,
 * autosort, magnet, refill, whitelist/blacklist), and transient GUI/viewer state.
 * Mutator methods generally mark the instance as changed for persistence.
 */
public class Data {
    /** Unique identifier of the bag */
	String uuid;
	/** Owner UUID of the bag */
	String owner;
	/** UUID of the player who created the bag (may differ from owner) */
	String creator;
	/** Inventory size (number of slots) */
	int size;
	/** Base64 texture string for custom head/icon */
	String texture;
	/*/ Material used for bag display */
	private Material material;
	/** Custom name of the bag */
	private String name = "";
	/** Custom model data integer */
	int modeldata;
	/** Item model identifier string */
	String itemmodel;
	/** List of trusted player UUIDs */
	List<String> trusted;
	/** Autopickup mode string */
	String autopickup;
	/** Current weight of the bag */
	double weight;
	/** Maximum weight capacity of the bag */
	double weightMax;
	/** List of ItemStacks representing bag contents */
	List<ItemStack> content = new ArrayList<ItemStack>();
	/** Auto-sort enabled flag */
	private boolean autosort;
	/** Blacklist entries */
	private List<String> blacklist;
	/** Whitelist mode flag */
	private boolean whitelist;
	/** Ignore global blacklist flag */
	private boolean ignoreglobalblacklist;
	/** Magnet feature enabled flag */
	private boolean magnet;
	/** Refill feature enabled flag */
	private boolean refill;
	/** Effect identifier string */
	private String effect;
	
	/** Indicates if the data has changed since last save */
	boolean changed = false;
	/** Indicates if the bag is currently open */
	boolean isOpen = false;

	/** Current viewer of the bag */
	Player viewer = null;
	/** Associated GUI instance for the bag */
	BagGUI gui;
	
	/**
	 * Construct a Data object with mandatory uuid and owner.
	 * @param uuid bag UUID
	 * @param owner owner UUID
	 */
	public Data(@NotNull String uuid, @NotNull String owner) {
		this.setUuid(uuid); this.setOwner(owner);
	}
	/**
	 * Construct a Data object with uuid, owner and material.
	 * @param uuid bag UUID
	 * @param owner owner UUID
	 * @param material display material
	 */
	public Data(@NotNull String uuid, @NotNull String owner, @NotNull Material material) {
		this.setUuid(uuid); this.setOwner(owner); this.setMaterial(material);
	}

	/**
	 * Get the bag UUID.
	 * @return uuid string
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Set the bag UUID.
	 * @param uuid new UUID
	 */
	@DoNotCall("Should not be changed after creation, may cause database issues.")
	public void setUuid(@NotNull String uuid) {
		this.changed = true;
		this.uuid = uuid;
	}

	/** @return owner UUID */
	public String getOwner() {
		return owner;
	}

	/**
	 * Set owner UUID.
	 * @param owner owner UUID
	 */
	@DoNotCall("Should not be changed after creation, may cause database issues.")
	public void setOwner(@NotNull String owner) {
		this.changed = true;
		this.owner = owner;
	}
	/** @return creator UUID */
	public String getCreator() {
		return creator;
	}

	/**
	 * Set creator UUID.
	 * @param creator creator UUID
	 */
	public void setCreator(String creator) {
		this.changed = true;
		this.creator = creator;
	}

	/** @return inventory size */
	public int getSize() {
		return size;
	}

	/**
	 * Set inventory size.
	 * @param size slot count
	 */
	public void setSize(int size) {
		this.changed = true;
		this.size = size;
	}
	/**
	 * Replace content list.
	 * @param content new items list
	 */
	public void setContent(List<ItemStack> content){
		this.changed = true;
		this.content = content;
		//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
	}

	/**
	 * Current bag contents.
	 * @return mutable list of ItemStacks
	 */
	public List<ItemStack> getContent(){
		return content;
		//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
	}
	/** @return head/texture base64 */
	public String getTexture() {
		return texture;
	}
	
	/**
	 * Set head/texture base64.
	 * @param base64 texture string
	 */
	public void setTexture(String base64) {
		this.changed = true;
		this.texture = base64;
	}

	/** @return custom model data int */
	public int getModeldata() {
		return modeldata;
	}

	/**
	 * Set custom model data.
	 * @param modeldata model data value
	 */
	public void setModeldata(int modeldata) {
		this.changed = true;
		this.modeldata = modeldata;
	}

	/** @return item model key */
	public String getItemmodel() {
		return itemmodel;
	}

	/**
	 * Set item model key.
	 * @param itemmodel model identifier
	 */
	public void setItemmodel(String itemmodel) {
		this.changed = true;
		this.itemmodel = itemmodel;
	}

	/** @return trusted player UUIDs */
	public List<String> getTrusted() {
		return trusted;
	}
	
	/**
	 * Check if player UUID is trusted.
	 * @param uuid player UUID
	 * @return true if trusted
	 */
	public boolean isPlayerTrusted(String uuid) {
		if(trusted.isEmpty()) return false;
		for(int i = 0; i < trusted.size(); i++) {
			if(trusted.get(i).equalsIgnoreCase(uuid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set trusted UUID list.
	 * @param trusted list of UUIDs
	 */
	public void setTrusted(List<String> trusted) {
		this.changed = true;
		this.trusted = trusted;
	}
	
	/**
	 * Add a trusted player UUID.
	 * @param player player UUID
	 */
	public void addTrusted(String player) {
		if(!trusted.contains(player)) {
			trusted.add(player);
			setTrusted(trusted);
			setChanged(true);
		}
	}
	
	/**
	 * Remove a trusted player UUID.
	 * @param player player UUID
	 */
	public void removeTrusted(String player) {
		if(trusted.size() == 0) return;
		for(int i = 0; i < trusted.size(); i++) {
			if(trusted.get(i).equalsIgnoreCase(player)) {
				trusted.remove(i);
				setTrusted(trusted);
				setChanged(true);
			}
		}
	}

	/** @return autopickup mode string */
	public String getAutopickup() {
		return autopickup;
	}

	/**
	 * Set autopickup mode.
	 * @param autopickup mode string
	 */
	public void setAutopickup(String autopickup) {
		this.changed = true;
		this.autopickup = autopickup;
	}

	/** @return current weight */
	public double getWeight() {
		return weight;
	}

	/**
	 * Set current weight.
	 * @param weight weight value
	 */
	public void setWeight(double weight) {
		this.changed = true;
		this.weight = weight;
	}

	/** @return max weight */
	public double getWeightMax() {
		return weightMax;
	}

	/**
	 * Set max weight.
	 * @param weightMax limit
	 */
	public void setWeightMax(double weightMax) {
		this.changed = true;
		this.weightMax = weightMax;
	}

	/** @return GUI instance or null */
	public BagGUI getGui() {
		return gui;
	}

	/**
	 * Assign GUI instance.
	 * @param gui BagGUI
	 */
	public void setGui(BagGUI gui) {
		this.changed = true;
		this.gui = gui;
	}

	/** @return current viewer */
	public Player getViewer() {
		return viewer;
	}

	/**
	 * Set current viewer.
	 * @param viewer player viewing
	 */
	public void setViewer(Player viewer) {
		this.changed = true;
		this.viewer = viewer;
	}

	/** @return true if data changed since last save */
	public boolean isChanged() {
		return changed;
	}

	/** @return true if bag open */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * Set open state.
	 * @param open open flag
	 */
	public void setOpen(boolean open) {
		this.changed = true;
		this.isOpen = open;
	}

	/** @return true if auto-sort enabled */
	public boolean hasAutoSort() {
		return autosort;
	}

	/**
	 * Enable/disable auto-sort.
	 * @param autosort flag
	 */
	public void setAutoSort(boolean autosort) {
		this.changed = true;
		this.autosort = autosort;
	}

	/** @return material used for display */
	public Material getMaterial() {
		return material;
	}

	/**
	 * Set display material.
	 * @param material Material
	 */
	public void setMaterial(Material material) {
		this.changed = true;
		this.material = material;
	}

	/**
	 * Set display material from name (case-insensitive, "null" clears).
	 * @param material material name or "null"
	 */
	public void setMaterial(String material) {
		this.changed = true;
		if(material.equalsIgnoreCase("null")) {
			this.material = null;
			return;
		}
		this.material = Material.valueOf(material.toUpperCase());
	}

	/** @return custom name */
	public String getName() {
		return name;
	}

	/**
	 * Set custom name.
	 * @param name name string
	 */
	public void setName(String name) {
		this.changed = true;
		this.name = name;
	}

	/** @return blacklist entries */
	public List<String> getBlacklist() {
		return blacklist;
	}

	/**
	 * Set blacklist entries.
	 * @param blacklist list
	 */
	public void setBlacklist(List<String> blacklist) {
		this.changed = true;
		this.blacklist = blacklist;
	}

	/** @return true if whitelist mode */
	public boolean isWhitelist() {
		return whitelist;
	}

	/**
	 * Set whitelist mode.
	 * @param whitelist flag
	 */
	public void setWhitelist(boolean whitelist) {
		this.changed = true;
		this.whitelist = whitelist;
	}

	/** @return true if ignoring global blacklist */
	public boolean isIngoreGlobalBlacklist() {
		return ignoreglobalblacklist;
	}

	/**
	 * Set ignore global blacklist flag.
	 * @param useglobalblacklist flag
	 */
	public void setIgnoreGlobalBlacklist(boolean useglobalblacklist) {
		this.changed = true;
		this.ignoreglobalblacklist = useglobalblacklist;
	}

	/**
	 * Mark this data to be saved.
	 * 
	 * @param changed true/false
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/** @return true if magnet feature active */
	public boolean hasMagnet() {
		return magnet;
	}

	/**
	 * Enable/disable magnet.
	 * @param magnet flag
	 */
	public void setMagnet(boolean magnet) {
		this.changed = true;
		this.magnet = magnet;
	}

	/** @return true if refill feature active */
	public boolean hasRefill() {
		return refill;
	}

	/**
	 * Enable/disable refill.
	 * @param refill flag
	 */
	public void setRefill(boolean refill) {
		this.refill = refill;
		this.changed = true;
	}

	/** @return effect string */
	public String getEffect() {
		return effect;
	}

	/**
	 * Set effect string.
	 * @param effect effect id
	 */
	public void setEffect(String effect) {
		this.effect = effect;
		this.changed = true;
	}
	
	/**
	 * String representation for debugging/logging.
	 * @return readable summary
	 */
	@Override
	public String toString() {
	    return "Bag{" +
	            "uuid='" + uuid + '\'' +
	            ", owner='" + owner + '\'' +
	            ", creator='" + creator + '\'' +
	            ", size=" + size +
	            ", texture='" + texture + '\'' +
	            ", material=" + (material != null ? material.name() : "null") +
	            ", name='" + name + '\'' +
	            ", modeldata=" + modeldata +
	            ", itemmodel='" + itemmodel + '\'' +
	            ", trusted=" + trusted +
	            ", autopickup='" + autopickup + '\'' +
	            ", weight=" + weight +
	            ", weightMax=" + weightMax +
	            ", autosort=" + autosort +
	            ", blacklist=" + blacklist +
	            ", whitelist=" + whitelist +
	            ", ignoreglobalblacklist=" + ignoreglobalblacklist +
	            ", magnet=" + magnet +
	            ", refill=" + refill +
	            ", effect=" + effect +
	            ", changed=" + changed +
	            ", isOpen=" + isOpen +
	            ", viewer=" + (viewer != null ? viewer.getName() : "null") +
	            '}';
	}
	
	
	/**
	 * Creates a detached clone for read-only or manipulation without affecting original.
	 * Runtime fields (viewer/gui/open/changed) are reset.
	 * @return deep copy of this Data
	 */
	public Data clone() {
	    Data copy = new Data(this.uuid, this.owner);

	    copy.setCreator(this.creator);
	    copy.setSize(this.size);
	    copy.setTexture(this.texture);
	    copy.setMaterial(this.material);
	    copy.setName(this.name);
	    copy.setModeldata(this.modeldata);
	    copy.setItemmodel(this.itemmodel);

	    // Deep copy trusted list
	    if (this.trusted != null) {
	        copy.setTrusted(new ArrayList<>(this.trusted));
	    }

	    copy.setAutopickup(this.autopickup);
	    copy.setWeight(this.weight);
	    copy.setWeightMax(this.weightMax);
	    copy.setAutoSort(this.autosort);

	    // Deep copy blacklist
	    if (this.blacklist != null) {
	        copy.setBlacklist(new ArrayList<>(this.blacklist));
	    }

	    copy.setWhitelist(this.whitelist);
	    copy.setIgnoreGlobalBlacklist(this.ignoreglobalblacklist);
	    copy.setMagnet(this.magnet);
	    copy.setRefill(this.refill);
	    copy.setEffect(this.effect);

	    // Deep copy ItemStacks
	    if (this.content != null) {
	        List<ItemStack> newContent = new ArrayList<>();
	        for (ItemStack item : this.content) {
	            newContent.add(item != null ? item.clone() : null);
	        }
	        copy.setContent(newContent);
	    }

	    // Do NOT copy runtime-only stuff (viewer, GUI, open state)
	    copy.setOpen(false);
	    copy.setGui(null);
	    copy.setViewer(null);

	    // Reset changed state for the clone
	    copy.setChanged(false);

	    return copy;
	}

}