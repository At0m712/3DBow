package atom.bow3d.mixin;

import atom.bow3d.client.BowArmRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(
		method = "submitArmWithItem",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;")
	)
	private ItemUseAnimation cancelVanillaBowAnimation(ItemStack instance) {
		ItemUseAnimation anim = instance.getUseAnimation();
		if (anim == ItemUseAnimation.BOW) {
			return ItemUseAnimation.NONE;
		}
		return anim;
	}


	@org.spongepowered.asm.mixin.injection.ModifyVariable(
		method = "submitArmWithItem",
		at = @At("HEAD"),
		argsOnly = true,
		ordinal = 3
	)
	private float bow3d$cancelBowInverseArmHeight(
		float inverseArmHeight,
		PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState state,
		float partialTicks,
		float xRot,
		InteractionHand hand,
		float attack,
		ItemStack itemStack
	) {
		if (itemStack.is(net.minecraft.world.item.Items.BOW)) {
			net.minecraft.client.renderer.entity.state.AvatarRenderState avatar = playerState.avatarRenderState;
			if (avatar != null && avatar.isUsingItem && state.useItemRemainingTicks > 0) {
				return 0.0F;
			}
		}
		return inverseArmHeight;
	}

	@Inject(
		method = "submitArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
		)
	)
	private void bow3d$pushBowForwardOnDraw(
		PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState state,
		float partialTicks,
		float xRot,
		InteractionHand hand,
		float attack,
		ItemStack itemStack,
		float inverseArmHeight,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CallbackInfo ci
	) {
		if (itemStack.is(Items.BOW)) {
			AvatarRenderState avatar = playerState.avatarRenderState;
			boolean isUsing = state.useItemRemainingTicks > 0 && (
				(avatar != null && avatar.isUsingItem && (avatar.useItemHand == hand || avatar.useItemHand == null))
				|| (this.minecraft.player != null && this.minecraft.player.isUsingItem())
			);
			if (isUsing) {
				int useDuration = (hand == InteractionHand.MAIN_HAND) ? state.mainHandUseDuration : state.offHandUseDuration;
				float timeHeld = (float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F);
				float pull = Mth.clamp(timeHeld / 20.0F, 0.0F, 1.0F);
				float pushProgress = pull * pull * (3.0F - 2.0F * pull);

				poseStack.translate(
					-0.012F * pushProgress,
					0.008F * pushProgress,
					-0.075F * pushProgress
				);
			}
		}
	}

	@Inject(
		method = "submitArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V",
			shift = At.Shift.AFTER
		)
	)
	private void renderFirstPersonBowArms(
		PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState state,
		float partialTicks,
		float xRot,
		InteractionHand hand,
		float attack,
		ItemStack itemStack,
		float inverseArmHeight,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CallbackInfo ci
	) {
		BowArmRenderer.renderBowArms(
			playerState,
			state,
			hand,
			itemStack,
			partialTicks,
			poseStack,
			submitNodeCollector,
			lightCoords,
			this.minecraft
		);
	}
}
