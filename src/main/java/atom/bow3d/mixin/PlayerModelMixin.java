package atom.bow3d.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {

	public PlayerModelMixin(ModelPart root) {
		super(root);
	}


	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void bow3d$onSetupAnimThirdPerson(AvatarRenderState state, CallbackInfo ci) {
		boolean isBow = (state.leftHandItemStack != null && state.leftHandItemStack.is(Items.BOW))
			|| (state.rightHandItemStack != null && state.rightHandItemStack.is(Items.BOW));

		if (!isBow) {
			return;
		}

		boolean isAiming = state.isUsingItem
			|| state.rightArmPose == ArmPose.BOW_AND_ARROW
			|| state.leftArmPose == ArmPose.BOW_AND_ARROW;

		if (!isAiming) {
			return;
		}

		float pull = Mth.clamp(state.ticksUsingItem / 20.0F, 0.0F, 1.0F);


		this.leftArm.xRot = this.head.xRot - 1.5707964F;
		this.leftArm.yRot = this.head.yRot + 0.45F;
		this.leftArm.zRot = 0.35F;
		this.leftArm.z = -pull * 1.5F;
		this.leftArm.x = 5.0F - pull * 0.3F;


		this.rightArm.xRot = this.head.xRot - Mth.lerp(pull, 1.25F, 0.76F);
		this.rightArm.yRot = this.head.yRot - Mth.lerp(pull, 0.60F, 0.95F);
		this.rightArm.zRot = -Mth.lerp(pull, 0.40F, 0.85F);
		this.rightArm.z = Mth.lerp(pull, -0.5F, 1.5F);
		this.rightArm.x = -5.0F + Mth.lerp(pull, 0.0F, 0.8F);
	}
}
