package valorless.havenbags.datamodels;

import org.bukkit.entity.Player;

import valorless.havenbags.gui.BagGUI;

@Deprecated(forRemoval = true, since = "1.32.0.1888")
public class ActiveBag {
	public BagGUI gui;
	public String uuid;
	public Player player;
	
	public ActiveBag(BagGUI gui, String uuid, Player player) {
		this.gui = gui;
		this.uuid = uuid;
		this.player = player;
	}
}
