package valorless.havenbags.datamodels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import valorless.havenbags.BagGUI;

public class Data {
	String uuid;
	String owner;
	String creator;
	int size;
	String texture;
	private Material material;
	private String name = "";
	int modeldata;
	String itemmodel;
	List<String> trusted;
	String autopickup;
	double weight;
	double weightMax;
	List<ItemStack> content = new ArrayList<ItemStack>();
	private boolean autosort;
	private List<String> blacklist;
	private boolean whitelist;
	private boolean ignoreglobalblacklist;
	private boolean magnet;
	private boolean refill;
	
	boolean changed = false;
	boolean isOpen = false;

	Player viewer = null;
	BagGUI gui;
	
	public Data(@NotNull String uuid, @NotNull String owner) {
		this.setUuid(uuid); this.setOwner(owner);
	}
	
	public Data(@NotNull String uuid, @NotNull String owner, @NotNull Material material) {
		this.setUuid(uuid); this.setOwner(owner); this.setMaterial(material);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(@NotNull String uuid) {
		this.changed = true;
		this.uuid = uuid;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(@NotNull String owner) {
		this.changed = true;
		this.owner = owner;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.changed = true;
		this.creator = creator;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.changed = true;
		this.size = size;
	}
	
	public void setContent(List<ItemStack> content){
		this.changed = true;
		this.content = content;
		//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
	}

	public List<ItemStack> getContent(){
		return content;
		//return JsonUtils.fromJson(data.GetString("content").replace("◊","'"));
	}
	
	public String getTexture() {
		return texture;
	}
	
	public void setTexture(String base64) {
		this.changed = true;
		this.texture = base64;
	}

	/*public Config getDataFile() {
		return dataFile;
	}

	public void SetDataFile(@NotNull Config data) {
		this.dataFile = data;
	}*/

	public int getModeldata() {
		return modeldata;
	}

	public void setModeldata(int modeldata) {
		this.changed = true;
		this.modeldata = modeldata;
	}

	public String getItemmodel() {
		return itemmodel;
	}

	public void setItemmodel(String itemmodel) {
		this.changed = true;
		this.itemmodel = itemmodel;
	}

	public List<String> getTrusted() {
		return trusted;
	}
	
	public boolean isPlayerTrusted(String uuid) {
		if(trusted.isEmpty()) return false;
		for(int i = 0; i < trusted.size(); i++) {
			if(trusted.get(i).equalsIgnoreCase(uuid)) {
				return true;
			}
		}
		return false;
	}

	public void setTrusted(List<String> trusted) {
		this.changed = true;
		this.trusted = trusted;
	}

	public String getAutopickup() {
		return autopickup;
	}

	public void setAutopickup(String autopickup) {
		this.changed = true;
		this.autopickup = autopickup;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.changed = true;
		this.weight = weight;
	}

	public double getWeightMax() {
		return weightMax;
	}

	public void setWeightMax(double weightMax) {
		this.changed = true;
		this.weightMax = weightMax;
	}

	public BagGUI getGui() {
		return gui;
	}

	public void setGui(BagGUI gui) {
		this.changed = true;
		this.gui = gui;
	}

	public Player getViewer() {
		return viewer;
	}

	public void setViewer(Player viewer) {
		this.changed = true;
		this.viewer = viewer;
	}

	public boolean isChanged() {
		return changed;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean open) {
		this.changed = true;
		this.isOpen = open;
	}

	public boolean hasAutoSort() {
		return autosort;
	}

	public void setAutoSort(boolean autosort) {
		this.changed = true;
		this.autosort = autosort;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.changed = true;
		this.material = material;
	}

	public void setMaterial(String material) {
		this.changed = true;
		if(material.equalsIgnoreCase("null")) {
			this.material = null;
			return;
		}
		this.material = Material.valueOf(material.toUpperCase());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.changed = true;
		this.name = name;
	}

	public List<String> getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(List<String> blacklist) {
		this.changed = true;
		this.blacklist = blacklist;
	}

	public boolean isWhitelist() {
		return whitelist;
	}

	public void setWhitelist(boolean whitelist) {
		this.changed = true;
		this.whitelist = whitelist;
	}

	public boolean isIngoreGlobalBlacklist() {
		return ignoreglobalblacklist;
	}

	public void setIgnoreGlobalBlacklist(boolean useglobalblacklist) {
		this.changed = true;
		this.ignoreglobalblacklist = useglobalblacklist;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean hasMagnet() {
		return magnet;
	}

	public void setMagnet(boolean magnet) {
		this.changed = true;
		this.magnet = magnet;
	}

	public boolean hasRefill() {
		return refill;
	}

	public void setRefill(boolean refill) {
		this.refill = refill;
	}
}