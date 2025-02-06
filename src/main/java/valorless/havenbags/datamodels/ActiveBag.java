package valorless.havenbags.datamodels;

import valorless.havenbags.BagGUI;

public class ActiveBag {
	public BagGUI gui;
	public String uuid;
	
	public ActiveBag(BagGUI gui, String uuid) {
		this.gui = gui;
		this.uuid = uuid;
	}
}
