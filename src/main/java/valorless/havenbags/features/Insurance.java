package valorless.havenbags.features;

import java.util.HashMap;

import org.bukkit.entity.Player;

import valorless.havenbags.Main;
import valorless.havenbags.hooks.Eco;
import valorless.havenbags.hooks.EssentialsHook;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.json.JsonUtils;

public class Insurance {
	public enum InsuranceType {
		ADD,
		PERCENT;
		
		public static InsuranceType fromString(String str) {
			for (InsuranceType type : InsuranceType.values()) {
				if (type.name().equalsIgnoreCase(str)) {
					return type;
				}
			}
			Log.Error(Main.plugin, "Invalid insurance type in config: " + str + ". Valid types are: ADD, PERCENT"
					+ "\nDefaulting to ADD.");
			return ADD; // or throw an exception if you prefer
		}
	}
	
	private final Config data;
	private HashMap<Player, Double> playerInsuranceCosts = new HashMap<>();
	private HashMap<Player, Long> lastClaimTimes = new HashMap<>();
	private final long cooldownMillis;
	private final long resetTimeMillis;
	private final double defaultCost;
	private final double incrementValue;
	
	private static Insurance instance;
	public static Insurance getInstance() {
		return instance;
	}
	
	private InsuranceType type;
	
	public Insurance () {
		if(Main.config.GetBool("insurance.enabled") == false ) {
			data = null;
			type = null;
			cooldownMillis = 0;
			resetTimeMillis = 0;
			defaultCost = 0;
			incrementValue = 0;
			return;
		}
		if(!EssentialsHook.isHooked()) throw new IllegalStateException("Essentials must be hooked to use insurance feature.");
		instance = this;
		this.data = new Config("insurance");
		this.type = InsuranceType.fromString(data.GetString("type"));
		this.defaultCost = data.GetDouble("default-cost");
		this.incrementValue = data.GetDouble("increment-value");
		this.cooldownMillis = data.GetInt("cooldown-seconds") * 1000;
		this.resetTimeMillis = data.GetInt("reset-time-seconds") * 1000;
		loadData();
	}
	
	public static void shutdown() {
		if (instance != null) {
			instance.saveData();
		}
	}
	
	private void loadData() {
		playerInsuranceCosts.clear();
		playerInsuranceCosts = data.HasKey("playerInsuranceCosts") ? JsonUtils.fromJson(data.GetString("playerInsuranceCosts")) : new HashMap<>();
		lastClaimTimes.clear();
		lastClaimTimes = data.HasKey("lastClaimTimes") ? JsonUtils.fromJson(data.GetString("lastClaimTimes")) : new HashMap<>();
	}
	
	private void saveData() {
		data.Set("playerInsuranceCosts", !playerInsuranceCosts.isEmpty() ? JsonUtils.toJson(playerInsuranceCosts) : null);
		data.Set("lastClaimTimes", !lastClaimTimes.isEmpty() ? JsonUtils.toJson(lastClaimTimes) : null);
		data.SaveConfig();
	}
	
	public double getCurrentInsuranceCost(Player player) {
		if(shouldReset(player)) {
			return defaultCost;
		}
		return playerInsuranceCosts.getOrDefault(player, defaultCost);
	}
	
	public double getDefaultCost() {
		return defaultCost;
	}
	
	public boolean canClaim(Player player) {
		long currentTime = System.currentTimeMillis();
		long lastClaimTime = lastClaimTimes.getOrDefault(player, 0L);
		return (currentTime - lastClaimTime) >= cooldownMillis;
	}
	
	public boolean shouldReset(Player player) {
		if(resetTimeMillis <= 0) return false; // Reset disabled
		long currentTime = System.currentTimeMillis();
		long lastClaimTime = lastClaimTimes.getOrDefault(player, 0L);
		return (currentTime - lastClaimTime) >= resetTimeMillis;
	}
	
	public boolean claimInsurance(Player player) {		
		double currentCost = getCurrentInsuranceCost(player);
		if(!Eco.canAfford(player, currentCost)) return false; // Player cannot afford the insurance cost
		
		double newCost = currentCost;
		switch (type) {
			case ADD:
				newCost += incrementValue;
				break;
			case PERCENT:
				newCost *= (1 + incrementValue);
				break;
		}
		playerInsuranceCosts.put(player, newCost);
		lastClaimTimes.put(player, System.currentTimeMillis());
		
		Eco.takeMoney(player, currentCost);
		return true;
	}
	
	public void resetInsurance(Player player) {
		playerInsuranceCosts.put(player, defaultCost);
		lastClaimTimes.put(player, System.currentTimeMillis());
	}

}
