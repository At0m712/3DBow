package atom.bow3d;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreeDBow implements ModInitializer {
	public static final String MOD_ID = "bow3d";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier BOW_WOOD_CREAK_ID = id("bow_wood_creak");
	public static final SoundEvent BOW_WOOD_CREAK = SoundEvent.createVariableRangeEvent(BOW_WOOD_CREAK_ID);

	public static final Identifier BOW_STRING_STRETCH_ID = id("bow_string_stretch");
	public static final SoundEvent BOW_STRING_STRETCH = SoundEvent.createVariableRangeEvent(BOW_STRING_STRETCH_ID);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, BOW_WOOD_CREAK_ID, BOW_WOOD_CREAK);
		Registry.register(BuiltInRegistries.SOUND_EVENT, BOW_STRING_STRETCH_ID, BOW_STRING_STRETCH);

		LOGGER.info("3D Bow initialized with custom sounds!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
