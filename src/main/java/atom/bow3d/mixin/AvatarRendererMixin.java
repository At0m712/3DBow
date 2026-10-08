package atom.bow3d.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
		at = @At("TAIL")
	)
	private void bow3d$onExtractRenderState(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (avatar == mc.player && mc.options.getCameraType().isFirstPerson()) {
			return;
		}

		if (state.rightHandItemStack != null && state.rightHandItemStack.is(Items.BOW)) {
			boolean isAiming = state.isUsingItem
				|| state.rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW
				|| state.leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW;
			boolean offhandEmpty = state.leftHandItemStack == null || state.leftHandItemStack.isEmpty();

			if (isAiming || offhandEmpty) {
				ItemStack bowStack = state.rightHandItemStack;
				mc.getItemModelResolver().updateForLiving(
					state.leftHandItemState,
					bowStack,
					ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
					avatar
				);
				state.leftHandItemStack = bowStack;
				state.rightHandItemState.clear();
				state.rightHandItemStack = ItemStack.EMPTY;

				if (isAiming) {
					state.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
					state.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
				} else {
					state.leftArmPose = HumanoidModel.ArmPose.ITEM;
					state.rightArmPose = HumanoidModel.ArmPose.EMPTY;
				}
			}
		}
	}
}
