package atom.bow3d.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrossbowArmRenderer {

	public static void renderCrossbowArms(
		PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState state,
		InteractionHand hand,
		ItemStack itemStack,
		float partialTicks,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		Minecraft minecraft
	) {
		if (!itemStack.is(Items.CROSSBOW) || hand != InteractionHand.MAIN_HAND) {
			return;
		}

		AvatarRenderState avatarRenderState = playerState.avatarRenderState;
		if (avatarRenderState == null || avatarRenderState.isInvisible) {
			return;
		}

		boolean isUsing = state.useItemRemainingTicks > 0 && (
			(avatarRenderState.isUsingItem && (avatarRenderState.useItemHand == hand || avatarRenderState.useItemHand == null))
			|| (minecraft.player != null && minecraft.player.isUsingItem())
		);

		boolean isCharged = CrossbowItem.isCharged(itemStack);

		int chargeDuration = state.mainHandChargeDuration > 0 ? state.mainHandChargeDuration : 25;
		int useDuration = (hand == InteractionHand.MAIN_HAND) ? state.mainHandUseDuration : state.offHandUseDuration;
		float timeHeld = isUsing ? ((float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F)) : 0.0F;
		float pull = isUsing ? Mth.clamp(timeHeld / (float) chargeDuration, 0.0F, 1.0F) : 0.0F;

		AvatarRenderer<?> avatarRenderer = minecraft.getEntityRenderDispatcher().getRenderer(avatarRenderState);
		Identifier skinTexture = avatarRenderState.skin.body().texturePath();

		// ==========================================
		// 1. Bras gauche : tient l'arbalète par en-dessous (soutient le fût)
		// ==========================================
		poseStack.pushPose();
		float pushProgress = isUsing ? (pull * pull * (3.0F - 2.0F * pull)) : 0.0F;
		float leftX = Mth.lerp(pushProgress, -0.38F, -0.395F);
		float leftY = Mth.lerp(pushProgress, -0.12F, -0.11F);
		float leftZ = Mth.lerp(pushProgress, -0.30F, -0.32F);
		float leftRx = Mth.lerp(pushProgress, -38.0F, -36.0F);
		float leftRy = Mth.lerp(pushProgress, 28.0F, 26.0F);
		float leftRz = Mth.lerp(pushProgress, -25.0F, -22.0F);

		poseStack.translate(leftX, leftY, leftZ);
		poseStack.rotateDegrees(Axis.XP, leftRx);
		poseStack.rotateDegrees(Axis.YP, leftRy);
		poseStack.rotateDegrees(Axis.ZP, leftRz);
		poseStack.translate(-0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showLeftSleeve);
		poseStack.popPose();

		// ==========================================
		// 2. Bras droit : tient la flèche, la place, et tire la corde
		// ==========================================
		poseStack.pushPose();
		float targetX;
		float targetY;
		float targetZ;
		float rx;
		float ry;
		float rz;

		if (isUsing) {
			if (pull < 0.28F) {
				// Phase 1 : tient la flèche et la pose sur le rail de guidage
				float p = pull / 0.28F;
				float ease = p * p * (3.0F - 2.0F * p);
				targetX = Mth.lerp(ease, 0.04F, -0.16F);
				targetY = Mth.lerp(ease, 0.05F, -0.02F);
				targetZ = Mth.lerp(ease, -0.10F, -0.20F);
				rx = Mth.lerp(ease, -42.0F, -26.0F);
				ry = Mth.lerp(ease, -24.0F, -34.0F);
				rz = Mth.lerp(ease, 34.0F, 40.0F);
			} else {
				// Phase 2 : attrape la corde et la tire fermement vers l'arrière jusqu'à la noix
				float p = (pull - 0.28F) / 0.72F;
				float ease = p * p * (3.0F - 2.0F * p);
				targetX = Mth.lerp(ease, -0.16F, -0.20F);
				targetY = Mth.lerp(ease, -0.02F, -0.07F);
				targetZ = Mth.lerp(ease, -0.20F, 0.16F);
				rx = Mth.lerp(ease, -26.0F, -20.0F);
				ry = Mth.lerp(ease, -34.0F, -36.0F);
				rz = Mth.lerp(ease, 40.0F, 46.0F);
			}
		} else if (isCharged) {
			// Arbalète chargée : la main droite tient la poignée / gâchette à l'arrière, prête à tirer
			targetX = -0.16F;
			targetY = -0.12F;
			targetZ = 0.18F;
			rx = -22.0F;
			ry = -25.0F;
			rz = 32.0F;
		} else {
			// Au repos (non chargée) : main droite sur la poignée arrière
			targetX = -0.16F;
			targetY = -0.12F;
			targetZ = 0.18F;
			rx = -22.0F;
			ry = -25.0F;
			rz = 32.0F;
		}

		poseStack.translate(targetX, targetY, targetZ);
		poseStack.rotateDegrees(Axis.XP, rx);
		poseStack.rotateDegrees(Axis.YP, ry);
		poseStack.rotateDegrees(Axis.ZP, rz);
		poseStack.translate(0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showRightSleeve);
		poseStack.popPose();
	}
}
