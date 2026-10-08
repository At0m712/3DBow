package atom.bow3d.client;

import com.mojang.serialization.MapCodec;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

public record ArrowTintSource() implements ItemTintSource {
	public static final MapCodec<ArrowTintSource> MAP_CODEC = MapCodec.unit(new ArrowTintSource());
	public static final int WHITE = 0xFFFFFFFF;
	public static final int SPECTRAL_COLOR = 0xFFFFDD00;

	@Override
	public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
		LivingEntity entity = owner != null ? owner : Minecraft.getInstance().player;
		if (entity instanceof Player player) {
			ItemStack projectile = player.getProjectile(itemStack);
			if (projectile.is(Items.SPECTRAL_ARROW)) {
				return SPECTRAL_COLOR;
			}
			PotionContents potion = projectile.get(DataComponents.POTION_CONTENTS);
			if (potion != null) {
				OptionalInt effectColor = PotionContents.getColorOptional(potion.getAllEffects());
				if (effectColor.isPresent()) {
					return ARGB.opaque(effectColor.getAsInt());
				}
				if (potion.customColor().isPresent()) {
					return ARGB.opaque(potion.customColor().get());
				}
				int defaultColor = potion.getColor();
				if (defaultColor != -1) {
					return ARGB.opaque(defaultColor);
				}
			}
		}
		return WHITE;
	}

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return MAP_CODEC;
	}
}
