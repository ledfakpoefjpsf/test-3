package nova.client.client.mixin;

// @name: XrayBlockStateMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/XrayBlockStateMixin.java

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import nova.client.client.render.XrayRenderHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class XrayBlockStateMixin {

    // Hide blocks that are not selected in X-Ray settings.
    @Inject(
        method = "getRenderShape",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private void nova$xrayRenderShape(
        CallbackInfoReturnable<RenderShape> cir
    ) {

        BlockState state = (BlockState) (Object) this;

        if (XrayRenderHooks.shouldFade(state)) {

            cir.setReturnValue(RenderShape.INVISIBLE);
        }
    }

    // Prevent selected ores from having their faces
    // hidden by neighboring blocks.
    @Inject(
        method = "skipRendering",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void nova$xrayFaceCulling(
        BlockState neighbor,
        Direction direction,
        CallbackInfoReturnable<Boolean> cir
    ) {

        BlockState state = (BlockState) (Object) this;

        if (XrayRenderHooks.isSelected(state)) {

            cir.setReturnValue(false);
        }
    }
}
