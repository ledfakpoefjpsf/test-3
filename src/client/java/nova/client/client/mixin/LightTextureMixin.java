
package nova.client.client.mixin;

// @name: LightTextureMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/LightTextureMixin.java

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.renderer.LightTexture;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

    @ModifyExpressionValue(
        method = "updateLightTexture(F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"
        ),
        require = 1
    )
    private Object nova$fullbrightGamma(Object original) {

        Module fullbright =
            NovaClient.MODULES.getByName("Fullbright");

        if (fullbright == null || !fullbright.isEnabled()) {
            return original;
        }

        if (original instanceof Double) {
            return 16.0D;
        }

        return original;
    }
}
