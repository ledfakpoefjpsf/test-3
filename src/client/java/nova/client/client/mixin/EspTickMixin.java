
package nova.client.client.mixin;

// @name: EspTickMixin
// @type: Client Mixin
// @location: src/client/java/nova/client/client/mixin/EspTickMixin.java

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import nova.client.client.NovaClient;
import nova.client.client.module.MobEspSettings;
import nova.client.client.module.Module;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class EspTickMixin {

    private final Map<Entity, Boolean> nova$originalGlow =
        new IdentityHashMap<>();

    private ClientLevel nova$lastLevel;

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    private void nova$updateEsp(CallbackInfo ci) {

        Minecraft mc = (Minecraft) (Object) this;

        if (mc.level != nova$lastLevel) {
            nova$originalGlow.clear();
            nova$lastLevel = mc.level;
        }

        if (mc.level == null || mc.player == null) {
            return;
        }

        Module playerEsp =
            NovaClient.MODULES.getByName("Player ESP");

        Module mobEsp =
            NovaClient.MODULES.getByName("Mob ESP");

        boolean playersEnabled =
            playerEsp != null && playerEsp.isEnabled();

        boolean mobsEnabled =
            mobEsp != null && mobEsp.isEnabled();

        Iterator<Map.Entry<Entity, Boolean>> iterator =
            nova$originalGlow.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<Entity, Boolean> entry =
                iterator.next();

            Entity entity = entry.getKey();

            boolean shouldGlow =
                nova$shouldGlow(
                    entity,
                    mc.player,
                    playersEnabled,
                    mobsEnabled
                );

            if (!shouldGlow || entity.isRemoved()) {

                if (!entity.isRemoved()) {
                    entity.setGlowingTag(entry.getValue());
                }

                iterator.remove();
            }
        }

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (!nova$shouldGlow(
                entity,
                mc.player,
                playersEnabled,
                mobsEnabled
            )) {
                continue;
            }

            if (!nova$originalGlow.containsKey(entity)) {
                nova$originalGlow.put(
                    entity,
                    entity.hasGlowingTag()
                );
            }

            entity.setGlowingTag(true);
        }
    }

    private boolean nova$shouldGlow(
        Entity entity,
        Player localPlayer,
        boolean playersEnabled,
        boolean mobsEnabled
    ) {

        if (entity == localPlayer) {
            return false;
        }

        if (entity instanceof Player) {
            return playersEnabled;
        }

        return mobsEnabled
            && MobEspSettings.shouldGlow(entity);
    }
}
