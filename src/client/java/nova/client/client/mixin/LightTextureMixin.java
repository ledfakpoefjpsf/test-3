
package nova.client.mixin;

// @name: LightTextureMixin
// @type: Client Lightmap Mixin
// @location: src/client/java/nova/client/mixin/LightTextureMixin.java

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

    @ModifyExpressionValue(
        method = "updateLightTexture",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"
        ),
        require = 0
    )
    private Object nova$fullbrightGamma(Object original) {

        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.player == null) {
            return original;
        }

        Module fullbright =
            NovaClient.MODULES.getByName("Fullbright");

        if (fullbright != null
            && fullbright.isEnabled()
            && original instanceof Double) {

            return 16.0D;
        }

        return original;
    }
}
