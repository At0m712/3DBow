package atom.bow3d.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

public record HasFlameProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<HasFlameProperty> MAP_CODEC = MapCodec.unit(new HasFlameProperty());

	@Override
	public boolean get(
		ItemStack itemStack,
		@Nullable ClientLevel level,
		@Nullable LivingEntity owner,
		int seed,
		ItemDisplayContext displayContext
	) {
		return hasFlame(itemStack);
	}

	public static boolean hasFlame(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		if (enchantments != null && !enchantments.isEmpty()) {
			for (Holder<Enchantment> holder : enchantments.keySet()) {
				if (holder.is(Enchantments.FLAME)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return MAP_CODEC;
	}
}
