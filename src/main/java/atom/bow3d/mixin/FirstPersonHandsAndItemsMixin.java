package atom.bow3d.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {

	@Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
	private void bow3d$cancelBowItemUsed(InteractionHand hand, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			net.minecraft.world.item.ItemStack held = mc.player.getItemInHand(hand);
			if (held.is(Items.BOW) || held.is(Items.CROSSBOW)) {
				ci.cancel();
			}
		}
	}

	@Inject(
		method = "shouldInstantlyReplaceVisibleItem",
		at = @At("HEAD"),
		cancellable = true
	)
	private void bow3d$instantReplaceCrossbow(
		net.minecraft.world.item.ItemStack currentlyVisibleItem,
		net.minecraft.world.item.ItemStack expectedItem,
		net.minecraft.client.player.LocalPlayer player,
		org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir
	) {
		if (currentlyVisibleItem.is(Items.CROSSBOW) && expectedItem.is(Items.CROSSBOW)) {
			cir.setReturnValue(true);
		}
	}
}
