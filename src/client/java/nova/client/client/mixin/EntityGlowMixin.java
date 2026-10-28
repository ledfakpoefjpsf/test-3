
package nova.client.mixin;

// @name: EntityGlowMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/mixin/EntityGlowMixin.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.MobEspSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class EntityGlowMixin {

    @Inject(
        method = "shouldEntityAppearGlowing",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void nova$outlineEntity(
        Entity entity,
        CallbackInfoReturnable<Boolean> cir
    ) {

        Minecraft mc = (Minecraft) (Object) this;

        if (mc.player == null || entity == mc.player) {
            return;
        }

        if (entity instanceof Player) {

            Module playerEsp =
                NovaClient.MODULES.getByName("Player ESP");

            if (playerEsp != null && playerEsp.isEnabled()) {
                cir.setReturnValue(true);
            }

            return;
        }

        Module mobEsp =
            NovaClient.MODULES.getByName("Mob ESP");

        if (mobEsp != null
            && mobEsp.isEnabled()
            && MobEspSettings.shouldGlow(entity)) {

            cir.setReturnValue(true);
        }
    }
}
