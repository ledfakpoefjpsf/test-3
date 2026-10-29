
package nova.client.client.render;

// @name: EspRenderer
// @type: Client Renderer
// @location: src/client/java/nova/client/client/render/EspRenderer.java

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.MobEspSettings;

public final class EspRenderer {

    private static final GizmoStyle PLAYER_STYLE =
        GizmoStyle.stroke(0xFF00FFFF, 2.5F);

    private static final GizmoStyle MOB_STYLE =
        GizmoStyle.stroke(0xFFFF4444, 2.5F);

    private EspRenderer() {}

    public static void tick(Minecraft mc) {

        if (mc == null || mc.level == null
                || mc.player == null) {
            return;
        }

        Module playerEsp =
            NovaClient.MODULES.getByName("Player ESP");

        Module mobEsp =
            NovaClient.MODULES.getByName("Mob ESP");

        boolean showPlayers =
            playerEsp != null && playerEsp.isEnabled();

        boolean showMobs =
            mobEsp != null && mobEsp.isEnabled();

        if (!showPlayers && !showMobs) {
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (entity == mc.player || entity.isRemoved()) {
                continue;
            }

            if (entity instanceof Player) {

                if (!showPlayers) {
                    continue;
                }

                Gizmos.cuboid(
                    entity.getBoundingBox().inflate(0.05),
                    PLAYER_STYLE
                ).setAlwaysOnTop();

            } else if (
                showMobs &&
                MobEspSettings.shouldGlow(entity)
            ) {

                Gizmos.cuboid(
                    entity.getBoundingBox().inflate(0.05),
                    MOB_STYLE
                ).setAlwaysOnTop();
            }
        }
    }
}
