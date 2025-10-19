package valorless.havenbags.datamodels;

public class EtherealBagSettings {
	/** Filter for auto-pickup functionality. Use "null" for no filter. */
	public String autoPickup = "null";
	/** If true, items will be drawn towards the player when they are nearby. */
	public Boolean magnet = false;
	/** If true, items will be automatically sorted when the bag updates. */
	public Boolean autoSort = false;
	
	public EtherealBagSettings() {}
	
	public String toString() {
		return "EtherealBagSettings[autoPickup=" + autoPickup + ", magnet=" + magnet + ", autoSort=" + autoSort + "]";
	}
}
