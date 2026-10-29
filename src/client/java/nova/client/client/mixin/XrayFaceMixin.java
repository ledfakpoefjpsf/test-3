
package nova.client.client.mixin;

// @name: XrayFaceMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/XrayFaceMixin.java

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import nova.client.client.render.XrayRenderHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nova Nebula X-Ray face visibility.
 *
 * Minecraft 1.21.11 Mojang mappings.
 *
 * Makes selected ore faces visible when touching
 * ordinary terrain while X-Ray is enabled.
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
        BlockState neighborState,
        Direction direction,
        CallbackInfoReturnable<Boolean> cir
    ) {

        if (!XrayRenderHooks.isEnabled()) {
            return;
        }

        if (state == null || neighborState == null) {
            return;
        }

        if (!XrayRenderHooks.isSelected(state)) {
            return;
        }

        // Force ore faces to render when they touch
        // ordinary terrain that X-Ray will fade.
        if (XrayRenderHooks.shouldFade(neighborState)) {
            cir.setReturnValue(true);
        }
    }
}
