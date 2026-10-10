package atom.bow3d.mixin;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

	@Redirect(
		method = "prepareMainSubmit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;itemGlintRenderType()Lnet/minecraft/client/renderer/rendertype/RenderType;"
		)
	)
	private RenderType bow3d$suppressAmmunitionGlint(BakedQuad.MaterialInfo material) {
		Identifier spriteName = material.sprite().contents().name();
		if (bow3d$isGlintImmune(spriteName)) {
			return material.itemRenderType();
		}
		return material.itemGlintRenderType();
	}

	@Redirect(
		method = "prepareMainSubmit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;itemGlintSpecialRenderType()Lnet/minecraft/client/renderer/rendertype/RenderType;"
		)
	)
	private RenderType bow3d$suppressAmmunitionSpecialGlint(BakedQuad.MaterialInfo material) {
		Identifier spriteName = material.sprite().contents().name();
		if (bow3d$isGlintImmune(spriteName)) {
			return material.itemRenderType();
		}
		return material.itemGlintSpecialRenderType();
	}

	private static boolean bow3d$isGlintImmune(Identifier name) {
		if (name == null) {
			return false;
		}
		String path = name.getPath();
		return path.contains("arrow") || path.contains("fire") || path.contains("firework");
	}
}
