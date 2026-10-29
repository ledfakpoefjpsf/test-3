
package nova.client.client.mixin;

// @name: MouseHandlerMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/MouseHandlerMixin.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import nova.client.client.module.FreecamController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(
        method = "turnPlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nova$freecamMouse(
        double movementTime,
        CallbackInfo ci
    ) {

        if (!FreecamController.isActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null) {
            double sensitivity =
                mc.options.sensitivity().get() * 0.6D + 0.2D;

            double multiplier =
                sensitivity * sensitivity * sensitivity * 8.0D;

            FreecamController.rotate(
                accumulatedDX * multiplier * 0.15D,
                accumulatedDY * multiplier * 0.15D
            );
        }

        accumulatedDX = 0.0D;
        accumulatedDY = 0.0D;

        ci.cancel();
    }

    @Inject(
        method = "onScroll",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nova$freecamScroll(
        long window,
        double horizontal,
        double vertical,
        CallbackInfo ci
    ) {

        if (!FreecamController.isActive()) {
            return;
        }

        if (Minecraft.getInstance().screen != null) {
            return;
        }

        FreecamController.adjustSpeed(vertical);
        ci.cancel();
    }
}
