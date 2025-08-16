package valorless.havenbags.datamodels;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import valorless.havenbags.HavenBags;
import valorless.havenbags.Main;
import valorless.havenbags.hooks.Essentials;
import valorless.havenbags.hooks.EssentialsHook;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.tags.TagType;
import valorless.valorlessutils.tags.Tags;

//@SuppressWarnings("deprecation")
public class BagArmorStand {

    private final Plugin plugin;
    private final Player trackedPlayer;

    private ArmorStand armorStand;
    private boolean despawn = false;

    public BagArmorStand(Player trackedPlayer, Plugin plugin) {
        this.plugin = plugin;
        this.trackedPlayer = trackedPlayer;

        spawn();
        startUpdating();
    }

	private void spawn() {
        Location spawnLoc = getOffsetLocation();
        armorStand = (ArmorStand) trackedPlayer.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        //armorStand.setSmall(true);
        armorStand.setCustomName("§r");
        //armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setInvulnerable(true);
        armorStand.setCollidable(false);

        if(Server.VersionHigherOrEqualTo(Version.v1_19)) {
        	Attribute scaleAttribute = Attribute.valueOf("GENERIC_SCALE");
        	if (scaleAttribute == null) {
        		scaleAttribute = Attribute.valueOf("SCALE");
        	}
        	if (scaleAttribute != null) {
        		AttributeInstance instance = armorStand.getAttribute(scaleAttribute);
        		if (instance != null) {
        			instance.setBaseValue(Main.config.GetDouble("back-bag.scale"));
        		}
        	}
        }
        
        Tags.Set(Main.plugin, armorStand.getPersistentDataContainer(), "HavenBags", "back-bag", TagType.STRING);
        Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
        	if(!Main.config.GetBool("back-bag.show-own")) trackedPlayer.hideEntity(plugin, armorStand);
        	// Was still showing for some reason
		}, 1L);
        
    }

    private void startUpdating() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!trackedPlayer.isOnline() || despawn) {
                    if (armorStand != null && !armorStand.isDead()) {
                        armorStand.remove();
                    }
                    cancel();
                    return;
                }

                updateLocationAndRotation();
                updateScale();
                updateHelmetFromPlayerBag();
                
                //Bukkit.getLogger().info("Torso yaw: " + VisualPack.getTorsoYaw(trackedPlayer));
                //Bukkit.getLogger().info("Player yaw: " + trackedPlayer.getLocation().getYaw());
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private Location getOffsetLocation() {
        Location base = trackedPlayer.getLocation();

        Vector forward = base.getDirection().clone();
        forward.setY(0);
        forward.normalize();

        Vector right = forward.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        
        Vector offset = new Vector(Main.config.GetDouble("back-bag.offset.position.x"), 
				Main.config.GetDouble("back-bag.offset.position.y"), 
				Main.config.GetDouble("back-bag.offset.position.z"));

        Vector adjusted = forward.multiply(offset.getZ())
                                .add(right.multiply(offset.getX()))
                                .add(new Vector(0, offset.getY(), 0));

        return base.clone().add(adjusted);
    }

    private void updateLocationAndRotation() {
        if (armorStand == null || armorStand.isDead()) return;

        Location loc = getOffsetLocation();

        float combinedYaw = normalizeYaw(trackedPlayer.getLocation().getYaw() + Main.config.GetDouble("back-bag.offset.rotation").floatValue());
        loc.setYaw(combinedYaw);
        loc.setPitch(Main.config.GetDouble("back-bag.offset.pitch").floatValue());

        armorStand.teleport(loc);
        armorStand.setHeadPose(armorStand.getHeadPose().setX((float) Math.toRadians(Main.config.GetDouble("back-bag.offset.pitch").floatValue())));
    }
    
    private void updateScale() {
        if (armorStand == null || armorStand.isDead()) return;

        if(Server.VersionHigherOrEqualTo(Version.v1_19)) {
        	Attribute scaleAttribute = Attribute.valueOf("GENERIC_SCALE");
        	if (scaleAttribute == null) {
        		scaleAttribute = Attribute.valueOf("SCALE");
        	}
        	if (scaleAttribute != null) {
        		AttributeInstance playerScale = trackedPlayer.getAttribute(scaleAttribute);
                AttributeInstance standScale = armorStand.getAttribute(scaleAttribute);

                if (playerScale != null && standScale != null) {
                    double playerValue = playerScale.getValue();
                    standScale.setBaseValue(Main.config.GetDouble("back-bag.scale") * playerValue);
                }
        	}
        }
    }

    public void updateHelmetFromPlayerBag() {
        if (armorStand == null || armorStand.isDead()) return;
        if(isHoldingBag()) {
        	armorStand.getEquipment().setHelmet(new ItemStack(Material.AIR));
        	return;
        }

        ItemStack helmet = (!HavenBags.GetBagsDataInInventory(trackedPlayer).isEmpty()) ?
                HavenBags.GetBagsDataInInventory(trackedPlayer).get(0).item.clone() : new ItemStack(Material.AIR);
        
        if(trackedPlayer.getGameMode() == GameMode.SPECTATOR) helmet = new ItemStack(Material.AIR);
        if(trackedPlayer.isDead()) helmet = new ItemStack(Material.AIR);
        if(trackedPlayer.isGliding()) helmet = new ItemStack(Material.AIR);
        if(trackedPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) helmet = new ItemStack(Material.AIR);
        if(trackedPlayer.isInvisible()) helmet = new ItemStack(Material.AIR);
        if(EssentialsHook.isHooked()) {
        	if(Essentials.isVanished(trackedPlayer)) helmet = new ItemStack(Material.AIR);
        }
        if(isVanished(trackedPlayer)) helmet = new ItemStack(Material.AIR);
        

        //armorStand.setHelmet(helmet);
        armorStand.getEquipment().setHelmet(helmet);
    }

    public void despawn() {
        despawn = true;
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }

    private float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }
    
    public boolean isHoldingBag() {
    	String back = HavenBags.GetBagUUID((!HavenBags.GetBagsDataInInventory(trackedPlayer).isEmpty()) ?
                HavenBags.GetBagsDataInInventory(trackedPlayer).get(0).item.clone() : new ItemStack(Material.AIR));
    	String hand = HavenBags.GetBagUUID(trackedPlayer.getInventory().getItemInMainHand());
    	if (back == null || hand == null) return false;
    	return back.equalsIgnoreCase(hand);
    }
    
    //PremiumVanish
    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

	public ArmorStand getStand() {
		return armorStand;
	}
}
