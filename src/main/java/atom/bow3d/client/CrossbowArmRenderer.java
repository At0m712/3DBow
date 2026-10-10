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

		// =========================================================================
		// 1. Bras gauche : sous le fût de l'arbalète pour la soutenir
		// =========================================================================
		poseStack.pushPose();
		float pushProgress = isUsing ? (pull * pull * (3.0F - 2.0F * pull)) : 0.0F;
		float leftX = Mth.lerp(pushProgress, -0.400F, -0.415F);
		float leftY = Mth.lerp(pushProgress, -0.020F, -0.005F);
		float leftZ = Mth.lerp(pushProgress, -0.470F, -0.490F);
		float leftRx = Mth.lerp(pushProgress, -43.0F, -41.0F);
		float leftRy = Mth.lerp(pushProgress, 18.0F, 16.0F);
		float leftRz = Mth.lerp(pushProgress, -36.0F, -32.0F);

		poseStack.translate(leftX, leftY, leftZ);
		poseStack.rotateDegrees(Axis.XP, leftRx);
		poseStack.rotateDegrees(Axis.YP, leftRy);
		poseStack.rotateDegrees(Axis.ZP, leftRz);
		// Décalage pour aligner le creux de la paume directement sous l'arbalète
		poseStack.translate(-0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showLeftSleeve);
		poseStack.popPose();

		// Rendu de la flèche / fusée en 3D pendant la phase de pose sur l'arbalète
		if (isUsing && pull < 0.33F) {
			boolean isFirework = false;
			if (minecraft.player != null) {
				ItemStack proj = minecraft.player.getProjectile(itemStack);
				isFirework = proj.is(Items.FIREWORK_ROCKET);
			}
			int tintColor = new ArrowTintSource().calculate(itemStack, null, minecraft.player);
			CrossbowProjectileRenderer.renderLoadingProjectile(
				poseStack,
				submitNodeCollector,
				lightCoords,
				tintColor,
				pull,
				isFirework
			);
		}

		// =========================================================================
		// 2. Bras droit : positionné AU-DESSUS sur le rail (pose flèche & tire corde)
		// =========================================================================
		if (isCharged) {
			return; // Quand l'arbalète est chargée, la main droite disparaît !
		}

		poseStack.pushPose();
		float targetX;
		float targetY;
		float targetZ;
		float rx;
		float ry;
		float rz;

		if (isUsing) {
			if (pull < 0.33F) {
				// Phase 1 : la main droite vient d'au-dessus avec la flèche et la dépose sur le rail
				float p = pull / 0.33F;
				float ease = p * p * (3.0F - 2.0F * p);
				targetX = Mth.lerp(ease, 0.10F, -0.16F);
				targetY = Mth.lerp(ease, 0.26F, 0.14F);
				targetZ = Mth.lerp(ease, -0.14F, -0.28F);
				rx = Mth.lerp(ease, -35.0F, -25.0F);
				ry = Mth.lerp(ease, -20.0F, -32.0F);
				rz = Mth.lerp(ease, 32.0F, 38.0F);
			} else {
				// Phase 2 : la main droite attrape la corde sur le dessus du rail et la tire en arrière
				float p = (pull - 0.33F) / 0.67F;
				float ease = p * p * (3.0F - 2.0F * p);
				targetX = Mth.lerp(ease, -0.16F, -0.19F);
				targetY = Mth.lerp(ease, 0.14F, 0.11F);
				targetZ = Mth.lerp(ease, -0.28F, 0.12F);
				rx = Mth.lerp(ease, -25.0F, -18.0F);
				ry = Mth.lerp(ease, -32.0F, -32.0F);
				rz = Mth.lerp(ease, 38.0F, 40.0F);
			}
		} else {
			// Au repos (non chargée) : main droite sur la poignée arrière
			targetX = -0.17F;
			targetY = 0.11F;
			targetZ = 0.12F;
			rx = -18.0F;
			ry = -28.0F;
			rz = 36.0F;
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
