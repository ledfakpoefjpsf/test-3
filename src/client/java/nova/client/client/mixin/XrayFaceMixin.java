package nova.client.client.mixin;

// @name: XrayFaceMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/XrayFaceMixin.java

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import nova.client.client.render.XrayRenderHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nova Nebula X-Ray face-rendering hook.
 *
 * Allows selected ores to render faces that vanilla
 * Minecraft normally hides against neighboring stone.
 *
 * Does not change terrain opacity or RenderShape.
 */
@Mixin(Block.class)
public abstract class XrayFaceMixin {

    @Inject(
        method = "shouldRenderFace",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void nova$renderXrayFaces(
        BlockState state,
        BlockGetter world,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        CallbackInfoReturnable<Boolean> cir
    ) {

        // Preserve vanilla rendering when X-Ray is off.
        if (!XrayRenderHooks.isEnabled()) {
            return;
        }

        if (state == null || world == null) {
            return;
        }

        // Only override face culling for selected blocks.
        if (!XrayRenderHooks.isSelected(state)) {
            return;
        }

        BlockState neighbor = world.getBlockState(
            neighborPos
        );

        // Show ore faces touching ordinary terrain.
        if (XrayRenderHooks.shouldFade(neighbor)) {
            cir.setReturnValue(true);
        }
    }
}
