package atom.bow3d.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrossbowArmRenderer {

	public static void renderCrossbowArms(
		AbstractClientPlayer player,
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

		if (player == null || player.isInvisible()) {
			return;
		}

		boolean isUsing = player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand;

		int chargeDuration = CrossbowItem.getChargeDuration(itemStack, player);
		if (chargeDuration <= 0) {
			chargeDuration = 25;
		}
		float timeHeld = isUsing ? ((float) itemStack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F)) : 0.0F;
		float pull = isUsing ? Mth.clamp(timeHeld / (float) chargeDuration, 0.0F, 1.0F) : 0.0F;

		AvatarRenderer<?> avatarRenderer = (AvatarRenderer<?>) (Object) minecraft.getEntityRenderDispatcher().getRenderer(player);
		Identifier skinTexture = player.getSkin().body().texturePath();
		boolean showLeftSleeve = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
		boolean showRightSleeve = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);

		// =========================================================================
		// 1. Bras gauche : fixe sous le fût de l'arbalète pour la soutenir
		// =========================================================================
		poseStack.pushPose();
		float leftX = -0.345F;
		float leftY = -0.055F;
		float leftZ = -0.470F;
		float leftRx = -43.0F;
		float leftRy = 17.0F;
		float leftRz = -35.0F;

		poseStack.translate(leftX, leftY, leftZ);
		poseStack.mulPose(Axis.XP.rotationDegrees(leftRx));
		poseStack.mulPose(Axis.YP.rotationDegrees(leftRy));
		poseStack.mulPose(Axis.ZP.rotationDegrees(leftRz));
		// Décalage pour aligner le creux de la paume directement sous l'arbalète
		poseStack.translate(-0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, showLeftSleeve);
		poseStack.popPose();

		// Rendu de la flèche / fusée en 3D pendant la phase de pose sur l'arbalète
		if (isUsing && pull < 0.33F) {
			boolean isFirework = false;
			ItemStack proj = player.getProjectile(itemStack);
			isFirework = proj.is(Items.FIREWORK_ROCKET);
			int tintColor = new ArrowTintSource().calculate(itemStack, null, player);
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
		// 2. Bras droit : apparaît UNIQUEMENT pendant le chargement (pose flèche & tire corde)
		// =========================================================================
		if (!isUsing) {
			return; // Pas de main droite au repos ou lorsque l'arbalète est chargée
		}

		poseStack.pushPose();
		float targetX;
		float targetY;
		float targetZ;
		float rx;
		float ry;
		float rz;

		if (pull < 0.33F) {
			// Phase 1 : la main droite vient d'au-dessus avec la flèche et la dépose sur le rail
			float p = pull / 0.33F;
			float ease = p * p * (3.0F - 2.0F * p);
			targetX = Mth.lerp(ease, 0.08F, -0.16F);
			targetY = Mth.lerp(ease, 0.26F, 0.14F);
			targetZ = Mth.lerp(ease, -0.22F, -0.36F);
			rx = Mth.lerp(ease, -35.0F, -25.0F);
			ry = Mth.lerp(ease, -20.0F, -32.0F);
			rz = Mth.lerp(ease, 32.0F, 38.0F);
		} else {
			// Phase 2 : la main droite attrape la corde sur le dessus du rail et la tire en arrière
			float p = (pull - 0.33F) / 0.67F;
			float ease = p * p * (3.0F - 2.0F * p);
			targetX = Mth.lerp(ease, -0.16F, -0.18F);
			targetY = Mth.lerp(ease, 0.14F, 0.11F);
			targetZ = Mth.lerp(ease, -0.36F, 0.04F);
			rx = Mth.lerp(ease, -25.0F, -18.0F);
			ry = Mth.lerp(ease, -32.0F, -32.0F);
			rz = Mth.lerp(ease, 38.0F, 40.0F);
		}

		poseStack.translate(targetX, targetY, targetZ);
		poseStack.mulPose(Axis.XP.rotationDegrees(rx));
		poseStack.mulPose(Axis.YP.rotationDegrees(ry));
		poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
		poseStack.translate(0.4308F, -0.6785F, 0.0F);
		avatarRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, showRightSleeve);
		poseStack.popPose();
	}
}
