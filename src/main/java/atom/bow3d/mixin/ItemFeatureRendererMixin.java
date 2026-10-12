package atom.bow3d.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

	@Redirect(
		method = "prepareFoilSubmit",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBakedQuad(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V"
		)
	)
	private void bow3d$suppressAmmunitionGlint(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
		Identifier spriteName = quad.materialInfo().sprite().contents().name();
		if (bow3d$isGlintImmune(spriteName)) {
			return;
		}
		consumer.putBakedQuad(pose, quad, instance);
	}

	@Redirect(
		method = "prepareMainSubmit",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBakedQuad(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V"
		)
	)
	private void bow3d$putBakedQuadUnshadedFire(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
		Identifier spriteName = quad.materialInfo().sprite().contents().name();
		if (bow3d$isBowFlame(spriteName)) {
			Matrix4f matrix = pose.pose();
			int light = 15728880; // Full bright (maximum block + sky light)
			float nx = 0.0F;
			float ny = 1.0F; // View-space upward normal -> 100% diffuse illumination regardless of aiming pitch
			float nz = 0.0F;

			for (int vertex = 0; vertex < 4; vertex++) {
				Vector3fc position = quad.position(vertex);
				long packedUv = quad.packedUV(vertex);
				int vertexColor = instance.getColor(vertex);
				Vector3f pos = matrix.transformPosition(position, new Vector3f());
				float u = UVPair.unpackU(packedUv);
				float v = UVPair.unpackV(packedUv);
				consumer.addVertex(pos.x(), pos.y(), pos.z(), vertexColor, u, v, instance.overlayCoords(), light, nx, ny, nz);
			}
		} else {
			consumer.putBakedQuad(pose, quad, instance);
		}
	}

	private static boolean bow3d$isBowFlame(Identifier name) {
		if (name == null) {
			return false;
		}
		return "bow3d".equals(name.getNamespace()) && name.getPath().contains("fire_0");
	}

	private static boolean bow3d$isGlintImmune(Identifier name) {
		if (name == null) {
			return false;
		}
		if ("bow3d".equals(name.getNamespace())) {
			String path = name.getPath();
			return path.contains("arrow") || path.contains("fire") || path.contains("firework");
		}
		String path = name.getPath();
		return path.equals("item/arrow") || path.equals("item/spectral_arrow")
			|| path.equals("item/tipped_arrow_head") || path.equals("item/tipped_arrow_base");
	}
}
