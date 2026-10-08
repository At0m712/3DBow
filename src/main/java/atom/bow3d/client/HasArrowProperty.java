package atom.bow3d.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record HasArrowProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<HasArrowProperty> MAP_CODEC = MapCodec.unit(new HasArrowProperty());

	@Override
	public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
		if (owner instanceof Player player) {
			if (player.isUsingItem()) {
				BowShotTracker.cancelReload();
			}
			if (owner == Minecraft.getInstance().player && BowShotTracker.isReloading()) {
				return false;
			}
			ItemStack projectile = player.getProjectile(itemStack);
			return !projectile.isEmpty() || player.getAbilities().instabuild;
		}
		return false;
	}

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return MAP_CODEC;
	}
}
