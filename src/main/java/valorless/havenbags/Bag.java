package valorless.havenbags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import valorless.valorlessutils.ValorlessUtils.Utils;

public class Bag {
	/*public enum BagSize { 
		NINE(9),
		EIGHTEEN(18),
		TWENTYSEVEN(27),
		THIRTYSIX(36),
		FOURTYFIVE(45),
		FIFTYFOUR(54);
		
		public final int value;
		private BagSize(int value) {
			this.value = value;
		}
		
		public int value() {
			return this.value;
		}
	}*/
	
	public UUID uuid;
	public Player owner = null;
	public boolean canBind = true;
	public int size = 9;
	public List<ItemStack> content;
	
	public Bag(String uuid, Player owner, int size, boolean canBind) {
		if(!Utils.IsStringNullOrEmpty(uuid)) { this.uuid = UUID.fromString(uuid); }
		else { this.uuid = UUID.randomUUID(); }		
		this.owner = owner;
		this.canBind = canBind;
		this.size = size;
		this.content = new ArrayList<ItemStack>();
	}
	
	public void SetContent(Bag bag, List<ItemStack> content) {
		bag.content = content;
	}
}
