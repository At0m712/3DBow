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

public class CrossbowProjectileRenderer {

	private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

	public static final RenderType SHAFT_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/arrow_shaft.png")
	);
	public static final RenderType TIP_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/arrow_tip.png")
	);
	public static final RenderType FLETCHING_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/arrow_fletching.png")
	);
	public static final RenderType FIREWORK_RENDER_TYPE = RenderTypes.entityCutout(
		Identifier.fromNamespaceAndPath("bow3d", "textures/item/firework_rocket.png")
	);

	public record FaceQuad(
		Direction dir,
		float minU, float minV, float maxU, float maxV,
		float minX, float minY, float minZ,
		float maxX, float maxY, float maxZ
	) {}

	private static final FaceQuad[] ARROW_SHAFT_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 1F, 5F, 2F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.UP, 4F, 0F, 5F, 1F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.SOUTH, 2F, 0F, 3F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.WEST, 3F, 0F, 4F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.EAST, 1F, 0F, 2F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 10.46304F, 14.90166F)
	};

	private static final FaceQuad[] ARROW_TIP_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 0F, 1.7F, 0.2F, 2F, 7.32233F, 9.66304F, 14.60166F, 7.52233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.UP, 0F, 0F, 0.2F, 0.3F, 7.32233F, 9.66304F, 14.60166F, 7.52233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.NORTH, 1.8F, 1.5F, 2F, 2.3F, 7.32233F, 9.66304F, 14.60166F, 7.52233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.SOUTH, 0F, 1.5F, 0.2F, 2.3F, 7.32233F, 9.66304F, 14.60166F, 7.52233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.WEST, 0F, 1.5F, 0.3F, 2.3F, 7.32233F, 9.66304F, 14.60166F, 7.52233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.EAST, 1.7F, 1.5F, 2F, 2.3F, 7.32233F, 9.66304F, 14.60166F, 7.52233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.DOWN, 0F, 1.7F, 0.2F, 2F, 6.82233F, 9.66304F, 14.60166F, 7.02233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.UP, 0F, 0F, 0.2F, 0.3F, 6.82233F, 9.66304F, 14.60166F, 7.02233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.NORTH, 1.8F, 1.5F, 2F, 2.3F, 6.82233F, 9.66304F, 14.60166F, 7.02233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.SOUTH, 0F, 1.5F, 0.2F, 2.3F, 6.82233F, 9.66304F, 14.60166F, 7.02233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.WEST, 0F, 1.5F, 0.3F, 2.3F, 6.82233F, 9.66304F, 14.60166F, 7.02233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.EAST, 1.7F, 1.5F, 2F, 2.3F, 6.82233F, 9.66304F, 14.60166F, 7.02233F, 10.46304F, 14.90166F),
		new FaceQuad(Direction.DOWN, 0F, 1.8F, 0.3F, 2F, 7.02233F, 9.66304F, 14.40166F, 7.32233F, 10.46304F, 14.60166F),
		new FaceQuad(Direction.UP, 0F, 0F, 0.3F, 0.2F, 7.02233F, 9.66304F, 14.40166F, 7.32233F, 10.46304F, 14.60166F),
		new FaceQuad(Direction.NORTH, 1.7F, 1.5F, 2F, 2.3F, 7.02233F, 9.66304F, 14.40166F, 7.32233F, 10.46304F, 14.60166F),
		new FaceQuad(Direction.SOUTH, 0F, 1.5F, 0.3F, 2.3F, 7.02233F, 9.66304F, 14.40166F, 7.32233F, 10.46304F, 14.60166F),
		new FaceQuad(Direction.WEST, 0F, 1.5F, 0.2F, 2.3F, 7.02233F, 9.66304F, 14.40166F, 7.32233F, 10.46304F, 14.60166F),
		new FaceQuad(Direction.EAST, 1.8F, 1.5F, 2F, 2.3F, 7.02233F, 9.66304F, 14.40166F, 7.32233F, 10.46304F, 14.60166F),
		new FaceQuad(Direction.DOWN, 0F, 1.8F, 0.3F, 2F, 7.02233F, 9.66304F, 14.90166F, 7.32233F, 10.46304F, 15.10166F),
		new FaceQuad(Direction.UP, 0F, 0F, 0.3F, 0.2F, 7.02233F, 9.66304F, 14.90166F, 7.32233F, 10.46304F, 15.10166F),
		new FaceQuad(Direction.NORTH, 1.7F, 1.5F, 2F, 2.3F, 7.02233F, 9.66304F, 14.90166F, 7.32233F, 10.46304F, 15.10166F),
		new FaceQuad(Direction.SOUTH, 0F, 1.5F, 0.3F, 2.3F, 7.02233F, 9.66304F, 14.90166F, 7.32233F, 10.46304F, 15.10166F),
		new FaceQuad(Direction.WEST, 0F, 1.5F, 0.2F, 2.3F, 7.02233F, 9.66304F, 14.90166F, 7.32233F, 10.46304F, 15.10166F),
		new FaceQuad(Direction.EAST, 1.8F, 1.5F, 2F, 2.3F, 7.02233F, 9.66304F, 14.90166F, 7.32233F, 10.46304F, 15.10166F),
		new FaceQuad(Direction.DOWN, 0F, 1.7F, 0.3F, 2F, 7.02233F, 10.46304F, 14.60166F, 7.32233F, 10.96304F, 14.90166F),
		new FaceQuad(Direction.UP, 0F, 0F, 0.3F, 0.3F, 7.02233F, 10.46304F, 14.60166F, 7.32233F, 10.96304F, 14.90166F),
		new FaceQuad(Direction.NORTH, 1.7F, 1.5F, 2F, 2F, 7.02233F, 10.46304F, 14.60166F, 7.32233F, 10.96304F, 14.90166F),
		new FaceQuad(Direction.SOUTH, 0F, 1.5F, 0.3F, 2F, 7.02233F, 10.46304F, 14.60166F, 7.32233F, 10.96304F, 14.90166F),
		new FaceQuad(Direction.WEST, 0F, 1.5F, 0.3F, 2F, 7.02233F, 10.46304F, 14.60166F, 7.32233F, 10.96304F, 14.90166F),
		new FaceQuad(Direction.EAST, 1.7F, 1.5F, 2F, 2F, 7.02233F, 10.46304F, 14.60166F, 7.32233F, 10.96304F, 14.90166F)
	};

	private static final FaceQuad[] ARROW_FLETCHING_G1_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F)
	};

	private static final FaceQuad[] ARROW_FLETCHING_G2_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F)
	};

	private static final FaceQuad[] FW_SHAFT_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 1F, 5F, 2F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 9.66304F, 14.90166F),
		new FaceQuad(Direction.UP, 4F, 0F, 5F, 1F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 9.66304F, 14.90166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 9.66304F, 14.90166F),
		new FaceQuad(Direction.SOUTH, 2F, 0F, 3F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 9.66304F, 14.90166F),
		new FaceQuad(Direction.WEST, 3F, 0F, 4F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 9.66304F, 14.90166F),
		new FaceQuad(Direction.EAST, 1F, 0F, 2F, 9F, 7.02233F, 1.46304F, 14.60166F, 7.32233F, 9.66304F, 14.90166F)
	};

	private static final FaceQuad[] FW_BODY_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 0F, 8F, 4F, 6.7723F, 9.66304F, 14.30166F, 7.5723F, 12.46304F, 15.10166F),
		new FaceQuad(Direction.UP, 0F, 0F, 4F, 4F, 6.7723F, 9.66304F, 14.30166F, 7.5723F, 12.46304F, 15.10166F),
		new FaceQuad(Direction.NORTH, 0F, 4F, 4F, 16F, 6.7723F, 9.66304F, 14.30166F, 7.5723F, 12.46304F, 15.10166F),
		new FaceQuad(Direction.SOUTH, 8F, 4F, 12F, 16F, 6.7723F, 9.66304F, 14.30166F, 7.5723F, 12.46304F, 15.10166F),
		new FaceQuad(Direction.WEST, 12F, 4F, 16F, 16F, 6.7723F, 9.66304F, 14.30166F, 7.5723F, 12.46304F, 15.10166F),
		new FaceQuad(Direction.EAST, 4F, 4F, 8F, 16F, 6.7723F, 9.66304F, 14.30166F, 7.5723F, 12.46304F, 15.10166F)
	};

	private static final FaceQuad[] FW_CONE_2_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 0F, 8F, 4F, 6.3723F, 9.66304F, 16.70166F, 7.9723F, 10.06304F, 18.30166F),
		new FaceQuad(Direction.UP, 0F, 0F, 4F, 4F, 6.3723F, 9.66304F, 16.70166F, 7.9723F, 10.06304F, 18.30166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 4F, 4F, 6.3723F, 9.66304F, 16.70166F, 7.9723F, 10.06304F, 18.30166F),
		new FaceQuad(Direction.SOUTH, 8F, 0F, 12F, 4F, 6.3723F, 9.66304F, 16.70166F, 7.9723F, 10.06304F, 18.30166F),
		new FaceQuad(Direction.WEST, 12F, 0F, 16F, 4F, 6.3723F, 9.66304F, 16.70166F, 7.9723F, 10.06304F, 18.30166F),
		new FaceQuad(Direction.EAST, 4F, 0F, 8F, 4F, 6.3723F, 9.66304F, 16.70166F, 7.9723F, 10.06304F, 18.30166F)
	};

	private static final FaceQuad[] FW_CONE_3_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 0F, 8F, 4F, 6.5723F, 9.66304F, 17.30166F, 7.7723F, 10.06304F, 18.50166F),
		new FaceQuad(Direction.UP, 0F, 0F, 4F, 4F, 6.5723F, 9.66304F, 17.30166F, 7.7723F, 10.06304F, 18.50166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 4F, 4F, 6.5723F, 9.66304F, 17.30166F, 7.7723F, 10.06304F, 18.50166F),
		new FaceQuad(Direction.SOUTH, 8F, 0F, 12F, 4F, 6.5723F, 9.66304F, 17.30166F, 7.7723F, 10.06304F, 18.50166F),
		new FaceQuad(Direction.WEST, 12F, 0F, 16F, 4F, 6.5723F, 9.66304F, 17.30166F, 7.7723F, 10.06304F, 18.50166F),
		new FaceQuad(Direction.EAST, 4F, 0F, 8F, 4F, 6.5723F, 9.66304F, 17.30166F, 7.7723F, 10.06304F, 18.50166F)
	};

	private static final FaceQuad[] FW_CONE_4_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 0F, 8F, 4F, 6.7723F, 9.66304F, 17.90166F, 7.5723F, 10.06304F, 18.70166F),
		new FaceQuad(Direction.UP, 0F, 0F, 4F, 4F, 6.7723F, 9.66304F, 17.90166F, 7.5723F, 10.06304F, 18.70166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 4F, 4F, 6.7723F, 9.66304F, 17.90166F, 7.5723F, 10.06304F, 18.70166F),
		new FaceQuad(Direction.SOUTH, 8F, 0F, 12F, 4F, 6.7723F, 9.66304F, 17.90166F, 7.5723F, 10.06304F, 18.70166F),
		new FaceQuad(Direction.WEST, 12F, 0F, 16F, 4F, 6.7723F, 9.66304F, 17.90166F, 7.5723F, 10.06304F, 18.70166F),
		new FaceQuad(Direction.EAST, 4F, 0F, 8F, 4F, 6.7723F, 9.66304F, 17.90166F, 7.5723F, 10.06304F, 18.70166F)
	};

	private static final FaceQuad[] FW_CONE_5_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 4F, 0F, 8F, 4F, 6.9723F, 9.66304F, 18.50166F, 7.3723F, 10.06304F, 18.90166F),
		new FaceQuad(Direction.UP, 0F, 0F, 4F, 4F, 6.9723F, 9.66304F, 18.50166F, 7.3723F, 10.06304F, 18.90166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 4F, 4F, 6.9723F, 9.66304F, 18.50166F, 7.3723F, 10.06304F, 18.90166F),
		new FaceQuad(Direction.SOUTH, 8F, 0F, 12F, 4F, 6.9723F, 9.66304F, 18.50166F, 7.3723F, 10.06304F, 18.90166F),
		new FaceQuad(Direction.WEST, 12F, 0F, 16F, 4F, 6.9723F, 9.66304F, 18.50166F, 7.3723F, 10.06304F, 18.90166F),
		new FaceQuad(Direction.EAST, 4F, 0F, 8F, 4F, 6.9723F, 9.66304F, 18.50166F, 7.3723F, 10.06304F, 18.90166F)
	};

	private static final FaceQuad[] FW_FLETCHING_G1_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 7.30936F, 1.57601F, 14.70166F, 7.60936F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 6.72233F, 1.57601F, 14.70166F, 7.02233F, 2.57601F, 14.80166F)
	};

	private static final FaceQuad[] FW_FLETCHING_G2_FACES = new FaceQuad[] {
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 7.26318F, 1.57603F, 14.70547F, 7.56318F, 2.57603F, 14.80547F),
		new FaceQuad(Direction.DOWN, 0F, 0F, 1F, 0F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.UP, 0F, 0F, 1F, 0F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.NORTH, 0F, 0F, 1F, 1F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.SOUTH, 0F, 1F, 1F, 2F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.WEST, 0F, 0F, 0F, 1F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F),
		new FaceQuad(Direction.EAST, 0F, 0F, 0F, 1F, 6.69316F, 1.57664F, 14.70547F, 6.99316F, 2.57664F, 14.80547F)
	};

	public static void renderLoadingProjectile(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		int tintColor,
		float pull,
		boolean isFirework
	) {
		if (pull < 0.0F || pull >= 0.33F) {
			return;
		}

		float t = pull / 0.33F;
		t = Mth.clamp(t, 0.0F, 1.0F);

		// Animation de pose de la fleche / fusee dans l'arbalete :
		// La main droite amene le projectile depuis le haut-droit, le descend et le place dans la rainure
		float ease = t * t * (3.0F - 2.0F * t);

		float offsetX = Mth.lerp(ease, 0.22F, 0.0F);
		float offsetY = Mth.lerp(ease, 0.14F, 0.0F);
		float offsetZ = Mth.lerp(ease, 0.05F, 0.0F);

		float rotX = Mth.lerp(ease, -14.0F, 0.0F);
		float rotY = Mth.lerp(ease, -16.0F, 0.0F);
		float rotZ = Mth.lerp(ease, 10.0F, 0.0F);

		poseStack.pushPose();

		// Deplacement d'animation dans l'espace de la main et de la camera
		poseStack.translate(offsetX, offsetY, offsetZ);
		if (rotX != 0.0F) poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
		if (rotY != 0.0F) poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
		if (rotZ != 0.0F) poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

		// Transformation standard firstperson_righthand de crossbow.json :
		// translation: [-5.5, 5.5, -1.8], rotation: [-180, -5.5, -180], scale: [1.4, 1.4, 1.4]
		poseStack.translate(-5.5F * 0.0625F, 5.5F * 0.0625F, -1.8F * 0.0625F);
		poseStack.mulPose(new Quaternionf().rotationXYZ(
			-180.0F * DEG_TO_RAD,
			-5.5F * DEG_TO_RAD,
			-180.0F * DEG_TO_RAD
		));
		poseStack.scale(1.4F, 1.4F, 1.4F);
		poseStack.translate(-0.5F, -0.5F, -0.5F);

		if (isFirework) {
			renderFireworkGeometry(poseStack, submitNodeCollector, lightCoords);
		} else {
			renderArrowGeometry(poseStack, submitNodeCollector, lightCoords, tintColor);
		}

		poseStack.popPose();
	}

	private static void renderArrowGeometry(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		int tintColor
	) {
		float originX = 7.16584F / 16.0F;
		float originY = 6.39351F / 16.0F;
		float originZ = 14.74899F / 16.0F;

		// Groupe 1 : Manche, pointe et empennages G1 (rotation ZYX: 90, 0, 90)
		poseStack.pushPose();
		poseStack.translate(originX, originY, originZ);
		poseStack.mulPose(new Quaternionf().rotationZYX(
			90.0F * DEG_TO_RAD,
			0.0F,
			90.0F * DEG_TO_RAD
		));
		poseStack.translate(-originX, -originY, -originZ);

		submitNodeCollector.submitCustomGeometry(poseStack, SHAFT_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, ARROW_SHAFT_FACES);
		});

		submitNodeCollector.submitCustomGeometry(poseStack, TIP_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, tintColor, ARROW_TIP_FACES);
		});

		submitNodeCollector.submitCustomGeometry(poseStack, FLETCHING_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, ARROW_FLETCHING_G1_FACES);
		});

		poseStack.popPose();

		// Groupe 2 : Empennages G2 (rotation ZYX: 180, -0.5, 90)
		poseStack.pushPose();
		poseStack.translate(originX, originY, originZ);
		poseStack.mulPose(new Quaternionf().rotationZYX(
			180.0F * DEG_TO_RAD,
			-0.5F * DEG_TO_RAD,
			90.0F * DEG_TO_RAD
		));
		poseStack.translate(-originX, -originY, -originZ);

		submitNodeCollector.submitCustomGeometry(poseStack, FLETCHING_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, ARROW_FLETCHING_G2_FACES);
		});

		poseStack.popPose();
	}

	private static void renderFireworkGeometry(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords
	) {
		float originX = 7.16584F / 16.0F;
		float originY = 6.39351F / 16.0F;
		float originZ = 14.74899F / 16.0F;

		// Groupe 1 : Manche, corps cylindrique et empennages G1 (rotation ZYX: 90, 0, 90)
		poseStack.pushPose();
		poseStack.translate(originX, originY, originZ);
		poseStack.mulPose(new Quaternionf().rotationZYX(
			90.0F * DEG_TO_RAD,
			0.0F,
			90.0F * DEG_TO_RAD
		));
		poseStack.translate(-originX, -originY, -originZ);

		submitNodeCollector.submitCustomGeometry(poseStack, SHAFT_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, FW_SHAFT_FACES);
		});

		submitNodeCollector.submitCustomGeometry(poseStack, FIREWORK_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, FW_BODY_FACES);
		});

		submitNodeCollector.submitCustomGeometry(poseStack, FLETCHING_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, FW_FLETCHING_G1_FACES);
		});

		poseStack.popPose();

		// Groupe 2 : Empennages G2 (rotation ZYX: 180, -0.5, 90)
		poseStack.pushPose();
		poseStack.translate(originX, originY, originZ);
		poseStack.mulPose(new Quaternionf().rotationZYX(
			180.0F * DEG_TO_RAD,
			-0.5F * DEG_TO_RAD,
			90.0F * DEG_TO_RAD
		));
		poseStack.translate(-originX, -originY, -originZ);

		submitNodeCollector.submitCustomGeometry(poseStack, FLETCHING_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, FW_FLETCHING_G2_FACES);
		});

		poseStack.popPose();

		// Cône de la pointe du feu d'artifice (4 sections superposées)
		renderConeElement(poseStack, submitNodeCollector, lightCoords, FW_CONE_2_FACES, 17.54899F);
		renderConeElement(poseStack, submitNodeCollector, lightCoords, FW_CONE_3_FACES, 17.94899F);
		renderConeElement(poseStack, submitNodeCollector, lightCoords, FW_CONE_4_FACES, 18.34899F);
		renderConeElement(poseStack, submitNodeCollector, lightCoords, FW_CONE_5_FACES, 18.74899F);
	}

	private static void renderConeElement(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		FaceQuad[] quads,
		float originZ
	) {
		float originX = 7.21581F / 16.0F;
		float originY = 6.39351F / 16.0F;
		float oz = originZ / 16.0F;

		poseStack.pushPose();
		poseStack.translate(originX, originY, oz);
		poseStack.mulPose(new Quaternionf().rotationZYX(
			90.0F * DEG_TO_RAD,
			0.0F,
			90.0F * DEG_TO_RAD
		));
		poseStack.translate(-originX, -originY, -oz);

		submitNodeCollector.submitCustomGeometry(poseStack, FIREWORK_RENDER_TYPE, (pose, buffer) -> {
			renderQuads(pose, buffer, lightCoords, 0xFFFFFFFF, quads);
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
