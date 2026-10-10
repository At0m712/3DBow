package atom.bow3d.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.jspecify.annotations.Nullable;

public record LoadingFireworkProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<LoadingFireworkProperty> MAP_CODEC = MapCodec.unit(new LoadingFireworkProperty());

	@Override
	public boolean get(
		ItemStack itemStack,
		@Nullable ClientLevel level,
		@Nullable LivingEntity owner,
		int seed,
		ItemDisplayContext displayContext
	) {
		ChargedProjectiles projectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
		if (projectiles != null && !projectiles.isEmpty()) {
			return projectiles.contains(Items.FIREWORK_ROCKET);
		}
		if (owner != null) {
			ItemStack projectile = owner.getProjectile(itemStack);
			return projectile.is(Items.FIREWORK_ROCKET);
		}
		return false;
	}

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return MAP_CODEC;
	}
}
