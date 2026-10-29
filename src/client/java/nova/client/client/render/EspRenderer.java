
package nova.client.client.render;

// @name: EspRenderer
// @type: Client Renderer
// @location: src/client/java/nova/client/client/render/EspRenderer.java

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.MobEspSettings;
import nova.client.client.module.EspOverlaySettings;

public final class EspRenderer {

    private static final int PLAYER_COLOR = 0xFF00FFFF;
    private static final int MOB_COLOR = 0xFFFF4444;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BAR_BACKGROUND = 0xFF303030;

    private static final GizmoStyle PLAYER_STYLE =
        GizmoStyle.stroke(PLAYER_COLOR, 2.5F);

    private static final GizmoStyle MOB_STYLE =
        GizmoStyle.stroke(MOB_COLOR, 2.5F);

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

        Vec3 tracerStart = mc.player.getEyePosition();

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (entity == mc.player || entity.isRemoved()) {
                continue;
            }

            boolean isPlayer = entity instanceof Player;

            if (isPlayer) {
                if (!showPlayers) {
                    continue;
                }
            } else {
                if (!showMobs
                        || !MobEspSettings.shouldGlow(entity)) {
                    continue;
                }
            }

            int color = isPlayer ? PLAYER_COLOR : MOB_COLOR;

            GizmoStyle style =
                isPlayer ? PLAYER_STYLE : MOB_STYLE;

            AABB box = entity.getBoundingBox().inflate(0.05);

            // 3D bounding box
            Gizmos.cuboid(box, style).setAlwaysOnTop();

            // Floating entity name
            if (EspOverlaySettings.showNames()) {

                Vec3 namePos = new Vec3(
                    (box.minX + box.maxX) / 2.0,
                    box.maxY + 0.48,
                    (box.minZ + box.maxZ) / 2.0
                );

                Gizmos.billboardText(
                    EspOverlaySettings.getName(entity),
                    namePos,
                    TextGizmo.Style
                        .forColorAndCentered(WHITE)
                        .withScale(0.8F)
                ).setAlwaysOnTop();
            }

            // Health text and bar
            if (EspOverlaySettings.showHealth()
                    && entity instanceof LivingEntity) {

                float percent =
                    EspOverlaySettings.getHealthPercent(entity);

                int hpColor = healthColor(percent);

                Vec3 hpTextPos = new Vec3(
                    (box.minX + box.maxX) / 2.0,
                    box.maxY + 0.25,
                    (box.minZ + box.maxZ) / 2.0
                );

                Gizmos.billboardText(
                    EspOverlaySettings.getHealthText(entity),
                    hpTextPos,
                    TextGizmo.Style
                        .forColorAndCentered(hpColor)
                        .withScale(0.65F)
                ).setAlwaysOnTop();

                // Horizontal health bar above the box
                double barY = box.maxY + 0.12;
                double barZ = box.minZ;
                double left = box.minX;
                double right = box.maxX;
                double filled = left
                    + (right - left) * percent;

                Gizmos.line(
                    new Vec3(left, barY, barZ),
                    new Vec3(right, barY, barZ),
                    BAR_BACKGROUND,
                    6.0F
                ).setAlwaysOnTop();

                if (percent > 0.0F) {
                    Gizmos.line(
                        new Vec3(left, barY, barZ),
                        new Vec3(filled, barY, barZ),
                        hpColor,
                        4.0F
                    ).setAlwaysOnTop();
                }
            }

            // Tracer from your viewpoint to the entity
            if (EspOverlaySettings.showTracers()) {

                Vec3 target = new Vec3(
                    (box.minX + box.maxX) / 2.0,
                    (box.minY + box.maxY) / 2.0,
                    (box.minZ + box.maxZ) / 2.0
                );

                Gizmos.line(
                    tracerStart,
                    target,
                    color,
                    1.5F
                ).setAlwaysOnTop();
            }
        }
    }

    private static int healthColor(float percent) {

        percent = Math.max(
            0.0F,
            Math.min(1.0F, percent)
        );

        int red = (int) (255.0F * (1.0F - percent));
        int green = (int) (255.0F * percent);

        return 0xFF000000 | (red << 16) | (green << 8);
    }
}
