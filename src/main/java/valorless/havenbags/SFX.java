package valorless.havenbags;

import org.bukkit.entity.Player;

import valorless.valorlessutils.ValorlessUtils.Utils;

public class SFX {
	public static void Play(String sound, float volume, float pitch, Player player) {
		//player.playSound(player.getLocation(), sound, volume, pitch);
		if(!Utils.IsStringNullOrEmpty(sound)) {
			player.playSound(player, org.bukkit.Sound.valueOf(sound.toUpperCase()), volume, pitch);
		}
	}
}
