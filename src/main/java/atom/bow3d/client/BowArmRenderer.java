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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BowArmRenderer {

	public static void renderBowArms(
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
		if (!itemStack.is(Items.BOW) || hand != InteractionHand.MAIN_HAND) {
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

		int useDuration = (hand == InteractionHand.MAIN_HAND) ? state.mainHandUseDuration : state.offHandUseDuration;
		float timeHeld = isUsing ? ((float) useDuration - (state.useItemRemainingTicks - partialTicks + 1.0F)) : 0.0F;
		float pull = isUsing ? Mth.clamp(timeHeld / 20.0F, 0.0F, 1.0F) : 0.0F;

		AvatarRenderer<?> avatarRenderer = minecraft.getEntityRenderDispatcher().getRenderer(avatarRenderState);
		Identifier skinTexture = avatarRenderState.skin.body().texturePath();

		// 1. Bras gauche : tient l'arc par la poignée, tendant légèrement le bras vers l'avant lors du bandage
		poseStack.pushPose();
		float pushProgress = isUsing ? (pull * pull * (3.0F - 2.0F * pull)) : 0.0F;
		float leftRx = Mth.lerp(pushProgress, -46.0F, -42.0F);
		float leftRy = Mth.lerp(pushProgress, 20.0F, 18.0F);
		float leftRz = Mth.lerp(pushProgress, -41.0F, -36.0F);

		poseStack.translate(-0.535F, 0.038F, -0.445F);
		poseStack.rotateDegrees(Axis.XP, leftRx);
		poseStack.rotateDegrees(Axis.YP, leftRy);
		poseStack.rotateDegrees(Axis.ZP, leftRz);
		// Décalage pour aligner le centre de rotation sur la paume de la main gauche
		poseStack.translate(-0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showLeftSleeve);
		poseStack.popPose();

		boolean isLocalPlayer = minecraft.player != null;
		boolean hasArrows = isLocalPlayer && (!minecraft.player.getProjectile(itemStack).isEmpty() || minecraft.player.getAbilities().instabuild);

		if (isUsing && BowShotTracker.isReloading()) {
			BowShotTracker.cancelReload();
		}

		// Rendu de la flèche 3D extraite de bow.json pendant le rechargement (uniquement si le joueur n'est pas en train de bander l'arc)
		if (hasArrows && !isUsing && BowShotTracker.isReloading()) {
			float reloadProg = BowShotTracker.getReloadProgress();
			int tintColor = new ArrowTintSource().calculate(itemStack, null, minecraft.player);
			Arrow3DRenderer.renderReloadArrow(poseStack, submitNodeCollector, lightCoords, tintColor, reloadProg);
		}

		// 2. Bras droit : gère la visée, le tir et le rechargement avec encochage réaliste
		poseStack.pushPose();
		float targetX;
		float targetY;
		float targetZ;
		float rx;
		float ry;
		float rz;

		if (isUsing) {
			// Tension de la corde lors de la visée
			targetX = Mth.lerp(pull, -0.355F, -0.390F);
			targetY = Mth.lerp(pull, 0.092F, 0.080F);
			targetZ = Mth.lerp(pull, 0.0F, 0.350F);
			rx = -15.0F;
			ry = Mth.lerp(pull, -40.0F, -30.0F);
			rz = Mth.lerp(pull, 40.0F, 45.0F);
		} else if (hasArrows && BowShotTracker.isReloading()) {
			// Animation de rechargement réaliste après un tir : aller chercher la flèche, la poser et l'encocher
			float reloadProg = BowShotTracker.getReloadProgress();
			if (reloadProg < 0.35F) {
				// Phase 1 : la main descend vers le carquois / la ceinture pour saisir une flèche
				float t = reloadProg / 0.35F;
				targetX = Mth.lerp(t, -0.355F, 0.08F);
				targetY = Mth.lerp(t, 0.092F, -0.32F);
				targetZ = Mth.lerp(t, 0.0F, 0.22F);
				rx = Mth.lerp(t, -15.0F, -55.0F);
				ry = Mth.lerp(t, -40.0F, -20.0F);
				rz = Mth.lerp(t, 40.0F, 15.0F);
			} else {
				// Phase 2 : la main remonte avec la flèche par-dessus l'arc, la pose et l'encoche
				float t = (reloadProg - 0.35F) / 0.65F;
				t = Mth.clamp(t, 0.0F, 1.0F);
				if (t < 0.55F) {
					// 1. Montée de la main au-dessus de l'arc (lift par-dessus)
					float p = t / 0.55F;
					float ease = Mth.sin(p * (float) (Math.PI / 2.0));
					targetX = Mth.lerp(ease, 0.08F, 0.00F);
					targetY = Mth.lerp(ease, -0.32F, 0.26F);
					targetZ = Mth.lerp(ease, 0.22F, 0.04F);
					rx = Mth.lerp(ease, -55.0F, -25.0F);
					ry = Mth.lerp(ease, -20.0F, -32.0F);
					rz = Mth.lerp(ease, 15.0F, 30.0F);
				} else if (t < 0.82F) {
					// 2. Passage par-dessus et descente vers le repose-flèche
					float p = (t - 0.55F) / 0.27F;
					float ease = 0.5F - 0.5F * Mth.cos(p * (float) Math.PI);
					targetX = Mth.lerp(ease, 0.00F, -0.34F);
					targetY = Mth.lerp(ease, 0.26F, 0.11F);
					targetZ = Mth.lerp(ease, 0.04F, -0.065F);
					rx = Mth.lerp(ease, -25.0F, -20.0F);
					ry = Mth.lerp(ease, -32.0F, -38.0F);
					rz = Mth.lerp(ease, 30.0F, 38.0F);
				} else {
					// 3. Encochage : la main recule pour fixer fermement l'encoche sur la corde
					float notch = (t - 0.82F) / 0.18F;
					float notchEase = notch * notch * (3.0F - 2.0F * notch);
					targetX = Mth.lerp(notchEase, -0.34F, -0.355F);
					targetY = Mth.lerp(notchEase, 0.11F, 0.092F);
					targetZ = Mth.lerp(notchEase, -0.065F, 0.0F);
					rx = Mth.lerp(notchEase, -20.0F, -15.0F);
					ry = Mth.lerp(notchEase, -38.0F, -40.0F);
					rz = Mth.lerp(notchEase, 38.0F, 40.0F);
				}
			}
		} else {
			// Au repos : main droite sur la corde (remontée et reculée sur la corde)
			targetX = -0.355F;
			targetY = 0.092F;
			targetZ = 0.0F;
			rx = -15.0F;
			ry = -40.0F;
			rz = 40.0F;
		}

		poseStack.translate(targetX, targetY, targetZ);
		poseStack.rotateDegrees(Axis.XP, rx);
		poseStack.rotateDegrees(Axis.YP, ry);
		poseStack.rotateDegrees(Axis.ZP, rz);
		// Décalage pour aligner le centre de rotation sur la paume de la main droite
		poseStack.translate(0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, avatarRenderState.showRightSleeve);
		poseStack.popPose();
	}
}
