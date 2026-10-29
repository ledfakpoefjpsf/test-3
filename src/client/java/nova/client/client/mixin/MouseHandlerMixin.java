
package nova.client.client.mixin;

// @name: MouseHandlerMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/MouseHandlerMixin.java

import net.minecraft.client.MouseHandler;

import nova.client.client.module.FreecamController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(
        method = "turnPlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nova$freecamMouse(
        double accumulatedDelta,
        CallbackInfo ci
    ) {

        if (!FreecamController.isActive()) {
            return;
        }

        // Prevent the normal player rotation while
        // Freecam is enabled.
        ci.cancel();
    }
}
