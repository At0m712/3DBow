package atom.bow3d.mixin;

import atom.bow3d.client.BowShotTracker;
import atom.bow3d.client.BowSoundTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

	@Inject(
		method = "releaseUsing",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/BowItem;draw(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/List;"
		)
	)
	private void onBowShotFired(ItemStack itemStack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfoReturnable<Boolean> cir) {
		if (level.isClientSide() && livingEntity == Minecraft.getInstance().player) {
			BowShotTracker.onShotFired();
			BowSoundTracker.reset();
		}
	}

	@Inject(method = "use", at = @At("HEAD"))
	private void onBowUse(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
		if (level.isClientSide() && player == Minecraft.getInstance().player) {
			BowShotTracker.cancelReload();
			BowSoundTracker.reset();
		}
	}
}
