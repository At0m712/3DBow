package atom.bow3d;

import atom.bow3d.client.ArrowTintSource;
import atom.bow3d.client.HasArrowProperty;
import atom.bow3d.mixin.ItemTintSourcesAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.resources.Identifier;

import atom.bow3d.client.BowSoundTracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ThreeDBowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConditionalItemModelProperties.ID_MAPPER.put(
			Identifier.fromNamespaceAndPath(ThreeDBow.MOD_ID, "has_arrow"),
			HasArrowProperty.MAP_CODEC
		);

		ItemTintSourcesAccessor.getIdMapper().put(
			Identifier.fromNamespaceAndPath(ThreeDBow.MOD_ID, "arrow_tint"),
			ArrowTintSource.MAP_CODEC
		);

		ClientTickEvents.END_CLIENT_TICK.register(BowSoundTracker::tick);

		ThreeDBow.LOGGER.info("3D Bow client initialized with HasArrowProperty, ArrowTintSource, and BowSoundTracker!");
	}
}
