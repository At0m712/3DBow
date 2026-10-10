package atom.bow3d.mixin;

import atom.bow3d.client.BowArmRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(
		method = "renderArmWithItem",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;")
	)
	private ItemUseAnimation cancelVanillaBowAnimation(ItemStack instance) {
		ItemUseAnimation anim = instance.getUseAnimation();
		if (anim == ItemUseAnimation.BOW) {
			return ItemUseAnimation.NONE;
		}
		return anim;
	}

	@ModifyVariable(
		method = "renderArmWithItem",
		at = @At("HEAD"),
		argsOnly = true,
		ordinal = 3
	)
	private float bow3d$cancelBowInverseArmHeight(
		float equipProgress,
		AbstractClientPlayer player,
		float partialTicks,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack itemStack
	) {
		if (itemStack.is(Items.BOW)) {
			if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
				return 0.0F;
			}
		}
		return equipProgress;
	}

	@Inject(
		method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
		)
	)
	private void bow3d$pushBowForwardOnDraw(
		AbstractClientPlayer player,
		float partialTicks,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack itemStack,
		float equipProgress,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CallbackInfo ci
	) {
		if (itemStack.is(Items.BOW)) {
			boolean isUsing = player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand;
			if (isUsing) {
				float timeHeld = (float) itemStack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
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
		method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
			shift = At.Shift.AFTER
		)
	)
	private void renderFirstPersonBowArms(
		AbstractClientPlayer player,
		float partialTicks,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack itemStack,
		float equipProgress,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		CallbackInfo ci
	) {
		BowArmRenderer.renderBowArms(
			player,
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
