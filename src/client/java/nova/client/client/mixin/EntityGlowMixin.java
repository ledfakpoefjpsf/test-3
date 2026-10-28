
package nova.client.mixin;

// @name: EntityGlowMixin
// @type: Client Rendering Mixin
// @location: src/client/java/nova/client/mixin/EntityGlowMixin.java

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import nova.client.client.NovaClient;
import nova.client.client.module.MobEspSettings;
import nova.client.client.module.Module;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(
        method = "isCurrentlyGlowing",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nova$applyEsp(
        CallbackInfoReturnable<Boolean> cir
    ) {

        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.player == null) {
            return;
        }

        Entity entity = (Entity) (Object) this;

        if (entity == mc.player) {
            return;
        }

        Module playerEsp =
            NovaClient.MODULES.getByName("Player ESP");

        if (entity instanceof Player) {

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
