
package nova.client.client.mixin;

// @name: CameraMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/CameraMixin.java

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

import nova.client.client.module.FreecamController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(
        float yaw,
        float pitch
    );

    @Inject(
        method = "setup",
        at = @At("TAIL")
    )
    private void nova$applyFreecam(
        CallbackInfo ci
    ) {

        if (!FreecamController.isActive()) {
            return;
        }

        setPosition(
            FreecamController.getPosition()
        );

        setRotation(
            FreecamController.getYaw(),
            FreecamController.getPitch()
        );
    }
}
