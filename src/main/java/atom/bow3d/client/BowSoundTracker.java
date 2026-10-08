package atom.bow3d.client;

import atom.bow3d.ThreeDBow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;

public class BowSoundTracker {
	private static boolean playedWoodCreak = false;
	private static boolean playedStringStretch = false;
	private static boolean playedFullCreak = false;

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			reset();
			return;
		}

		if (player.isUsingItem() && player.getUseItem().is(Items.BOW)) {
			int ticks = player.getTicksUsingItem();

			if (ticks >= 2 && !playedWoodCreak) {
				playedWoodCreak = true;
				playSound(client, ThreeDBow.BOW_WOOD_CREAK, 0.70F, 1.25F);
			}

			if (ticks >= 8 && !playedStringStretch) {
				playedStringStretch = true;
				playSound(client, ThreeDBow.BOW_STRING_STRETCH, 0.60F, 1.15F);
			}

			if (ticks >= 18 && !playedFullCreak) {
				playedFullCreak = true;
				playSound(client, ThreeDBow.BOW_WOOD_CREAK, 0.50F, 1.35F);
			}
		} else {
			reset();
		}
	}

	private static void playSound(Minecraft client, SoundEvent sound, float volume, float pitch) {
		client.getSoundManager().play(
			new SimpleSoundInstance(
				sound.location(),
				SoundSource.PLAYERS,
				volume,
				pitch,
				RandomSource.create(),
				false,
				0,
				SoundInstance.Attenuation.NONE,
				0.0, 0.0, 0.0,
				true
			)
		);
	}

	public static void reset() {
		playedWoodCreak = false;
		playedStringStretch = false;
		playedFullCreak = false;
	}
}
