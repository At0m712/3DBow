package atom.bow3d.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class Arrow3DRenderer {

	public static final RenderType SHAFT_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/arrow_shaft.png")
	);
	public static final RenderType TIP_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/arrow_tip.png")
	);
	public static final RenderType FLETCHING_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/arrow_fletching.png")
	);

	public record FaceQuad(
		Direction dir,
		float minU, float minV, float maxU, float maxV,
		float minX, float minY, float minZ,
		float maxX, float maxY, float maxZ
	) {}

	private static final FaceQuad[] SHAFT_FACES = new FaceQuad[] {
		// Element 0: Shaft
		new FaceQuad(Direction.DOWN,  4.0F, 1.0F, 5.0F, 2.0F, 2.875F, 5.56203F, 8.5F, 3.175F, 14.56203F, 8.8F),
		new FaceQuad(Direction.UP,    4.0F, 0.0F, 5.0F, 1.0F, 2.875F, 5.56203F, 8.5F, 3.175F, 14.56203F, 8.8F),
		new FaceQuad(Direction.NORTH, 0.0F, 0.0F, 1.0F, 9.0F, 2.875F, 5.56203F, 8.5F, 3.175F, 14.56203F, 8.8F),
		new FaceQuad(Direction.SOUTH, 2.0F, 0.0F, 3.0F, 9.0F, 2.875F, 5.56203F, 8.5F, 3.175F, 14.56203F, 8.8F),
		new FaceQuad(Direction.WEST,  3.0F, 0.0F, 4.0F, 9.0F, 2.875F, 5.56203F, 8.5F, 3.175F, 14.56203F, 8.8F),
		new FaceQuad(Direction.EAST,  1.0F, 0.0F, 2.0F, 9.0F, 2.875F, 5.56203F, 8.5F, 3.175F, 14.56203F, 8.8F)
	};

	private static final FaceQuad[] TIP_FACES = new FaceQuad[] {
		// Element 1: Wing 1
		new FaceQuad(Direction.DOWN,  0.0F, 1.7F, 0.2F, 2.0F, 3.175F, 13.76203F, 8.5F, 3.375F, 14.56203F, 8.8F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 0.2F, 0.3F, 3.175F, 13.76203F, 8.5F, 3.375F, 14.56203F, 8.8F),
		new FaceQuad(Direction.NORTH, 1.8F, 1.5F, 2.0F, 2.3F, 3.175F, 13.76203F, 8.5F, 3.375F, 14.56203F, 8.8F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.5F, 0.2F, 2.3F, 3.175F, 13.76203F, 8.5F, 3.375F, 14.56203F, 8.8F),
		new FaceQuad(Direction.WEST,  0.0F, 1.5F, 0.3F, 2.3F, 3.175F, 13.76203F, 8.5F, 3.375F, 14.56203F, 8.8F),
		new FaceQuad(Direction.EAST,  1.7F, 1.5F, 2.0F, 2.3F, 3.175F, 13.76203F, 8.5F, 3.375F, 14.56203F, 8.8F),

		// Element 2: Wing 2
		new FaceQuad(Direction.DOWN,  0.0F, 1.7F, 0.2F, 2.0F, 2.675F, 13.76203F, 8.5F, 2.875F, 14.56203F, 8.8F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 0.2F, 0.3F, 2.675F, 13.76203F, 8.5F, 2.875F, 14.56203F, 8.8F),
		new FaceQuad(Direction.NORTH, 1.8F, 1.5F, 2.0F, 2.3F, 2.675F, 13.76203F, 8.5F, 2.875F, 14.56203F, 8.8F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.5F, 0.2F, 2.3F, 2.675F, 13.76203F, 8.5F, 2.875F, 14.56203F, 8.8F),
		new FaceQuad(Direction.WEST,  0.0F, 1.5F, 0.3F, 2.3F, 2.675F, 13.76203F, 8.5F, 2.875F, 14.56203F, 8.8F),
		new FaceQuad(Direction.EAST,  1.7F, 1.5F, 2.0F, 2.3F, 2.675F, 13.76203F, 8.5F, 2.875F, 14.56203F, 8.8F),

		// Element 3: Wing 3
		new FaceQuad(Direction.DOWN,  0.0F, 1.8F, 0.3F, 2.0F, 2.875F, 13.76203F, 8.3F, 3.175F, 14.56203F, 8.5F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 0.3F, 0.2F, 2.875F, 13.76203F, 8.3F, 3.175F, 14.56203F, 8.5F),
		new FaceQuad(Direction.NORTH, 1.7F, 1.5F, 2.0F, 2.3F, 2.875F, 13.76203F, 8.3F, 3.175F, 14.56203F, 8.5F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.5F, 0.3F, 2.3F, 2.875F, 13.76203F, 8.3F, 3.175F, 14.56203F, 8.5F),
		new FaceQuad(Direction.WEST,  0.0F, 1.5F, 0.2F, 2.3F, 2.875F, 13.76203F, 8.3F, 3.175F, 14.56203F, 8.5F),
		new FaceQuad(Direction.EAST,  1.8F, 1.5F, 2.0F, 2.3F, 2.875F, 13.76203F, 8.3F, 3.175F, 14.56203F, 8.5F),

		// Element 4: Wing 4
		new FaceQuad(Direction.DOWN,  0.0F, 1.8F, 0.3F, 2.0F, 2.875F, 13.76203F, 8.8F, 3.175F, 14.56203F, 9.0F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 0.3F, 0.2F, 2.875F, 13.76203F, 8.8F, 3.175F, 14.56203F, 9.0F),
		new FaceQuad(Direction.NORTH, 1.7F, 1.5F, 2.0F, 2.3F, 2.875F, 13.76203F, 8.8F, 3.175F, 14.56203F, 9.0F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.5F, 0.3F, 2.3F, 2.875F, 13.76203F, 8.8F, 3.175F, 14.56203F, 9.0F),
		new FaceQuad(Direction.WEST,  0.0F, 1.5F, 0.2F, 2.3F, 2.875F, 13.76203F, 8.8F, 3.175F, 14.56203F, 9.0F),
		new FaceQuad(Direction.EAST,  1.8F, 1.5F, 2.0F, 2.3F, 2.875F, 13.76203F, 8.8F, 3.175F, 14.56203F, 9.0F),

		// Element 5: Tip point
		new FaceQuad(Direction.DOWN,  0.0F, 1.7F, 0.3F, 2.0F, 2.875F, 14.56203F, 8.5F, 3.175F, 15.06203F, 8.8F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 0.3F, 0.3F, 2.875F, 14.56203F, 8.5F, 3.175F, 15.06203F, 8.8F),
		new FaceQuad(Direction.NORTH, 1.7F, 1.5F, 2.0F, 2.0F, 2.875F, 14.56203F, 8.5F, 3.175F, 15.06203F, 8.8F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.5F, 0.3F, 2.0F, 2.875F, 14.56203F, 8.5F, 3.175F, 15.06203F, 8.8F),
		new FaceQuad(Direction.WEST,  0.0F, 1.5F, 0.3F, 2.0F, 2.875F, 14.56203F, 8.5F, 3.175F, 15.06203F, 8.8F),
		new FaceQuad(Direction.EAST,  1.7F, 1.5F, 2.0F, 2.0F, 2.875F, 14.56203F, 8.5F, 3.175F, 15.06203F, 8.8F)
	};

	private static final FaceQuad[] FLETCHING_G1_FACES = new FaceQuad[] {
		// Element 6: Fletching 1
		new FaceQuad(Direction.DOWN,  0.0F, 0.0F, 1.0F, 0.0F, 3.16203F, 5.675F, 8.6F, 3.46203F, 6.675F, 8.7F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 1.0F, 0.0F, 3.16203F, 5.675F, 8.6F, 3.46203F, 6.675F, 8.7F),
		new FaceQuad(Direction.NORTH, 0.0F, 0.0F, 1.0F, 1.0F, 3.16203F, 5.675F, 8.6F, 3.46203F, 6.675F, 8.7F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.0F, 1.0F, 2.0F, 3.16203F, 5.675F, 8.6F, 3.46203F, 6.675F, 8.7F),
		new FaceQuad(Direction.WEST,  0.0F, 0.0F, 0.0F, 1.0F, 3.16203F, 5.675F, 8.6F, 3.46203F, 6.675F, 8.7F),
		new FaceQuad(Direction.EAST,  0.0F, 0.0F, 0.0F, 1.0F, 3.16203F, 5.675F, 8.6F, 3.46203F, 6.675F, 8.7F),

		// Element 7: Fletching 2
		new FaceQuad(Direction.DOWN,  0.0F, 0.0F, 1.0F, 0.0F, 2.575F, 5.675F, 8.6F, 2.875F, 6.675F, 8.7F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 1.0F, 0.0F, 2.575F, 5.675F, 8.6F, 2.875F, 6.675F, 8.7F),
		new FaceQuad(Direction.NORTH, 0.0F, 0.0F, 1.0F, 1.0F, 2.575F, 5.675F, 8.6F, 2.875F, 6.675F, 8.7F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.0F, 1.0F, 2.0F, 2.575F, 5.675F, 8.6F, 2.875F, 6.675F, 8.7F),
		new FaceQuad(Direction.WEST,  0.0F, 0.0F, 0.0F, 1.0F, 2.575F, 5.675F, 8.6F, 2.875F, 6.675F, 8.7F),
		new FaceQuad(Direction.EAST,  0.0F, 0.0F, 0.0F, 1.0F, 2.575F, 5.675F, 8.6F, 2.875F, 6.675F, 8.7F)
	};

	private static final FaceQuad[] FLETCHING_G2_FACES = new FaceQuad[] {
		// Element 8: Fletching 3
		new FaceQuad(Direction.DOWN,  0.0F, 0.0F, 1.0F, 0.0F, 3.11852F, 5.675F, 8.60648F, 3.41852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 1.0F, 0.0F, 3.11852F, 5.675F, 8.60648F, 3.41852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.NORTH, 0.0F, 0.0F, 1.0F, 1.0F, 3.11852F, 5.675F, 8.60648F, 3.41852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.0F, 1.0F, 2.0F, 3.11852F, 5.675F, 8.60648F, 3.41852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.WEST,  0.0F, 0.0F, 0.0F, 1.0F, 3.11852F, 5.675F, 8.60648F, 3.41852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.EAST,  0.0F, 0.0F, 0.0F, 1.0F, 3.11852F, 5.675F, 8.60648F, 3.41852F, 6.675F, 8.70648F),

		// Element 9: Fletching 4
		new FaceQuad(Direction.DOWN,  0.0F, 0.0F, 1.0F, 0.0F, 2.61852F, 5.675F, 8.60648F, 2.91852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.UP,    0.0F, 0.0F, 1.0F, 0.0F, 2.61852F, 5.675F, 8.60648F, 2.91852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.NORTH, 0.0F, 0.0F, 1.0F, 1.0F, 2.61852F, 5.675F, 8.60648F, 2.91852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.SOUTH, 0.0F, 1.0F, 1.0F, 2.0F, 2.61852F, 5.675F, 8.60648F, 2.91852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.WEST,  0.0F, 0.0F, 0.0F, 1.0F, 2.61852F, 5.675F, 8.60648F, 2.91852F, 6.675F, 8.70648F),
		new FaceQuad(Direction.EAST,  0.0F, 0.0F, 0.0F, 1.0F, 2.61852F, 5.675F, 8.60648F, 2.91852F, 6.675F, 8.70648F)
	};

	public static void renderReloadArrow(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		int tintColor,
		float reloadProg
	) {
		if (reloadProg < 0.35F || reloadProg >= 1.0F) {
			return;
		}

		float t = (reloadProg - 0.35F) / 0.65F;
		t = Mth.clamp(t, 0.0F, 1.0F);

		// Animation en 3 temps passant PAR-DESSUS l'arc:
		// 1. t in [0.0, 0.55]: Montee rapide au-dessus de l'arc (lift & clearance par dessus l'arc)
		// 2. t in [0.55, 0.82]: Passage par-dessus et descente sur le repose-fleche
		// 3. t in [0.82, 1.0]: Glissement vers l'arriere et enclenchement ("encochage") sur la corde
		float offsetX;
		float offsetY;
		float offsetZ;
		float rotX;
		float rotY;
		float rotZ;

		if (t < 0.55F) {
			float p = t / 0.55F;
			float ease = Mth.sin(p * (float) (Math.PI / 2.0));

			offsetX = Mth.lerp(ease, 0.435F, 0.180F);
			offsetY = Mth.lerp(ease, -0.412F, 0.220F);
			offsetZ = Mth.lerp(ease, 0.220F, 0.040F);

			rotX = Mth.lerp(ease, -35.0F, 14.0F);
			rotY = Mth.lerp(ease, 20.0F, 6.0F);
			rotZ = Mth.lerp(ease, -25.0F, 6.0F);
		} else if (t < 0.82F) {
			float p = (t - 0.55F) / 0.27F;
			float ease = 0.5F - 0.5F * Mth.cos(p * (float) Math.PI);

			offsetX = Mth.lerp(ease, 0.180F, 0.015F);
			offsetY = Mth.lerp(ease, 0.220F, 0.020F);
			offsetZ = Mth.lerp(ease, 0.040F, -0.065F);

			rotX = Mth.lerp(ease, 14.0F, 0.0F);
			rotY = Mth.lerp(ease, 6.0F, 0.0F);
			rotZ = Mth.lerp(ease, 6.0F, 0.0F);
		} else {
			float notch = (t - 0.82F) / 0.18F;
			float notchEase = notch * notch * (3.0F - 2.0F * notch);

			offsetX = Mth.lerp(notchEase, 0.015F, 0.0F);
			offsetY = Mth.lerp(notchEase, 0.020F, 0.0F);
			offsetZ = Mth.lerp(notchEase, -0.065F, 0.0F);

			rotX = 0.0F;
			rotY = 0.0F;
			rotZ = 0.0F;
		}

		poseStack.pushPose();

		// Deplacement d'animation dans l'espace de la main et de l'arc
		poseStack.translate(offsetX, offsetY, offsetZ);
		if (rotX != 0.0F) poseStack.rotateDegrees(Axis.XP, rotX);
		if (rotY != 0.0F) poseStack.rotateDegrees(Axis.YP, rotY);
		if (rotZ != 0.0F) poseStack.rotateDegrees(Axis.ZP, rotZ);

		// Transformation standard firstperson_righthand de bow.json
		// translation: [-10.75, 1, -1.25], rotation: [-90, -20, -95], scale: [2, 2, 2]
		poseStack.translate(-10.75F * 0.0625F, 1.0F * 0.0625F, -1.25F * 0.0625F);
		poseStack.rotate(new Quaternionf().rotationXYZ(
			-90.0F * (float) (Math.PI / 180.0),
			-20.0F * (float) (Math.PI / 180.0),
			-95.0F * (float) (Math.PI / 180.0)
		));
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.translate(-0.5F, -0.5F, -0.5F);

		renderArrowGeometry(poseStack, submitNodeCollector, lightCoords, tintColor);

		poseStack.popPose();
	}

	private static void renderArrowGeometry(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		int tintColor
	) {
		float originX = 3.01852F / 16.0F;
		float originY = 10.49352F / 16.0F;
		float originZ = 8.65F / 16.0F;

		// Groupe 1 : Manche, pointe et 2 empennages (rotation ZYX: 90, 0, 12)
		poseStack.pushPose();
		poseStack.translate(originX, originY, originZ);
		poseStack.rotate(new Quaternionf().rotationZYX(
			90.0F * (float) (Math.PI / 180.0),
			0.0F,
			12.0F * (float) (Math.PI / 180.0)
		));
		poseStack.translate(-originX, -originY, -originZ);

		submitNodeCollector.submitCustomGeometry(poseStack, SHAFT_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, SHAFT_FACES);
		});

		submitNodeCollector.submitCustomGeometry(poseStack, TIP_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, tintColor, TIP_FACES);
		});

		submitNodeCollector.submitCustomGeometry(poseStack, FLETCHING_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, FLETCHING_G1_FACES);
		});

		poseStack.popPose();

		// Groupe 2 : Les 2 autres empennages a 90 degres (rotation ZYX: 180, 78, 90)
		poseStack.pushPose();
		poseStack.translate(originX, originY, originZ);
		poseStack.rotate(new Quaternionf().rotationZYX(
			180.0F * (float) (Math.PI / 180.0),
			78.0F * (float) (Math.PI / 180.0),
			90.0F * (float) (Math.PI / 180.0)
		));
		poseStack.translate(-originX, -originY, -originZ);

		submitNodeCollector.submitCustomGeometry(poseStack, FLETCHING_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, FLETCHING_G2_FACES);
		});

		poseStack.popPose();
	}

	private static void renderQuads(
		PoseStack.Pose pose,
		VertexConsumer consumer,
		int lightCoords,
		int color,
		FaceQuad[] quads
	) {
		for (FaceQuad q : quads) {
			submitFace(pose, consumer, lightCoords, color, q);
		}
	}

	private static void submitFace(
		PoseStack.Pose pose,
		VertexConsumer consumer,
		int lightCoords,
		int color,
		FaceQuad q
	) {
		float u0 = q.minU() / 16.0F;
		float v0 = q.minV() / 16.0F;
		float u1 = q.maxU() / 16.0F;
		float v1 = q.maxV() / 16.0F;

		float minX = q.minX() / 16.0F;
		float minY = q.minY() / 16.0F;
		float minZ = q.minZ() / 16.0F;
		float maxX = q.maxX() / 16.0F;
		float maxY = q.maxY() / 16.0F;
		float maxZ = q.maxZ() / 16.0F;

		float x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3;
		float nx = q.dir().getStepX();
		float ny = q.dir().getStepY();
		float nz = q.dir().getStepZ();

		switch (q.dir()) {
			case DOWN -> {
				x0 = minX; y0 = minY; z0 = maxZ;
				x1 = minX; y1 = minY; z1 = minZ;
				x2 = maxX; y2 = minY; z2 = minZ;
				x3 = maxX; y3 = minY; z3 = maxZ;
			}
			case UP -> {
				x0 = minX; y0 = maxY; z0 = minZ;
				x1 = minX; y1 = maxY; z1 = maxZ;
				x2 = maxX; y2 = maxY; z2 = maxZ;
				x3 = maxX; y3 = maxY; z3 = minZ;
			}
			case NORTH -> {
				x0 = maxX; y0 = maxY; z0 = minZ;
				x1 = maxX; y1 = minY; z1 = minZ;
				x2 = minX; y2 = minY; z2 = minZ;
				x3 = minX; y3 = maxY; z3 = minZ;
			}
			case SOUTH -> {
				x0 = minX; y0 = maxY; z0 = maxZ;
				x1 = minX; y1 = minY; z1 = maxZ;
				x2 = maxX; y2 = minY; z2 = maxZ;
				x3 = maxX; y3 = maxY; z3 = maxZ;
			}
			case WEST -> {
				x0 = minX; y0 = maxY; z0 = minZ;
				x1 = minX; y1 = minY; z1 = minZ;
				x2 = minX; y2 = minY; z2 = maxZ;
				x3 = minX; y3 = maxY; z3 = maxZ;
			}
			case EAST -> {
				x0 = maxX; y0 = maxY; z0 = maxZ;
				x1 = maxX; y1 = minY; z1 = maxZ;
				x2 = maxX; y2 = minY; z2 = minZ;
				x3 = maxX; y3 = maxY; z3 = minZ;
			}
			default -> { return; }
		}

		consumer.addVertex(pose, x0, y0, z0).setColor(color).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(pose, nx, ny, nz);
		consumer.addVertex(pose, x1, y1, z1).setColor(color).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(pose, nx, ny, nz);
		consumer.addVertex(pose, x2, y2, z2).setColor(color).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(pose, nx, ny, nz);
		consumer.addVertex(pose, x3, y3, z3).setColor(color).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(pose, nx, ny, nz);
	}
}