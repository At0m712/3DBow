package atom.bow3d.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

	@Redirect(
		method = "renderQuadList",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"
		)
	)
	private static void bow3d$processQuad(
		VertexConsumer consumer,
		PoseStack.Pose pose,
		BakedQuad quad,
		float red,
		float green,
		float blue,
		float alpha,
		int lightCoords,
		int overlayCoords
	) {
		Identifier spriteName = quad.sprite().contents().name();
		VertexConsumer targetConsumer = consumer;
		if (bow3d$isGlintImmune(spriteName) && consumer instanceof DoubleVertexConsumerAccessor accessor) {
			targetConsumer = accessor.getFirst();
		}

		if (bow3d$isBowFlame(spriteName)) {
			Matrix4f matrix = pose.pose();
			int light = 15728880; // Full bright
			float nx = 0.0F;
			float ny = 1.0F; // Upward view normal -> 100% diffuse illumination regardless of aiming pitch
			float nz = 0.0F;

			int color = ((int) (alpha * 255.0F) << 24)
				| ((int) (red * 255.0F) << 16)
				| ((int) (green * 255.0F) << 8)
				| (int) (blue * 255.0F);

			for (int vertex = 0; vertex < 4; vertex++) {
				Vector3fc position = quad.position(vertex);
				long packedUv = quad.packedUV(vertex);
				Vector3f pos = matrix.transformPosition(position, new Vector3f());
				float u = UVPair.unpackU(packedUv);
				float v = UVPair.unpackV(packedUv);
				targetConsumer.addVertex(pos.x(), pos.y(), pos.z(), color, u, v, overlayCoords, light, nx, ny, nz);
			}
		} else {
			targetConsumer.putBulkData(pose, quad, red, green, blue, alpha, lightCoords, overlayCoords);
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
