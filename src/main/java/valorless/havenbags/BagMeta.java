package valorless.havenbags;

import org.bukkit.inventory.ItemStack;

import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.file.YamlFile;
import valorless.valorlessutils.nbt.NBT;

public class BagMeta {
	
	private final String uuid;
	private Config meta;
	private YamlFile metaFile;
	
	public BagMeta(ItemStack bag) {
		this.uuid = NBT.GetString(bag, "bag-uuid");
		String path = String.format("bags/%s/%s.meta", NBT.GetString(bag, "bag-owner"), uuid);
		this.meta = new Config(Main.plugin, path);
		this.meta.SaveConfig();
		this.metaFile = meta.GetFile();
	}
	
	public boolean HasBagMeta() {
		if(metaFile == null) return false;
		if(metaFile.fileExists()) return true;
		else return false;
	}
	
	public boolean Has(String key) {
		return meta.HasKey(key);
	}
	
	public Object Get(String key) throws Exception{
		if(HasBagMeta() == false) throw new NullPointerException(String.format("%s does not contain BagMeta.", uuid));
		if(Has(key) == false) throw new NullPointerException(String.format("%s BagMeta does not contain '%s'", uuid, key));
		return meta.Get(key);
	}
	
	public <T> void Set(String key, T value) throws Exception {
		if(value == null) throw new NullPointerException("Value cannot be null!");
		if(HasBagMeta() == false) throw new NullPointerException(String.format("%s does not contain BagMeta.", uuid));
		meta.Set(key, value);
		meta.SaveConfig();
	}
}
