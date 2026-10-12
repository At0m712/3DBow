package atom.bow3d.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.mojang.blaze3d.vertex.VertexMultiConsumer$Double")
public interface DoubleVertexConsumerAccessor {
	@Accessor("first")
	VertexConsumer getFirst();
}
