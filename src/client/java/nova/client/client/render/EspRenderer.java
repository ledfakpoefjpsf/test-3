
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

    private static final int PURPLE = 0xFFC084FC;
    private static final int CYAN = 0xFF42D9FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DARK = 0xFF292638;

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
                if (!showPlayers) continue;
            } else {
                if (!showMobs
                        || !MobEspSettings.shouldGlow(entity)) {
                    continue;
                }
            }

            AABB box = entity.getBoundingBox().inflate(0.05);

            int color = isPlayer ? CYAN : PURPLE;

            // Nova Competitive corner brackets
            drawCorners(box, color);

            // Compact nameplate
            if (EspOverlaySettings.showNames()) {

                Vec3 namePosition = new Vec3(
                    box.getCenter().x,
                    box.maxY + 0.35,
                    box.getCenter().z
                );

                Gizmos.billboardText(
                    EspOverlaySettings.getName(entity),
                    namePosition,
                    TextGizmo.Style
                        .forColorAndCentered(WHITE)
                        .withScale(0.8F)
                ).setAlwaysOnTop();
            }

            // Vertical health bar and HP text
            if (EspOverlaySettings.showHealth()
                    && entity instanceof LivingEntity) {

                float percent =
                    EspOverlaySettings.getHealthPercent(entity);

                int healthColor = healthColor(percent);

                double barX = box.minX - 0.12;
                double barZ = box.minZ;
                double bottom = box.minY;
                double top = box.maxY;

                Gizmos.line(
                    new Vec3(barX, bottom, barZ),
                    new Vec3(barX, top, barZ),
                    DARK,
                    6.0F
                ).setAlwaysOnTop();

                if (percent > 0.0F) {
                    Gizmos.line(
                        new Vec3(barX, bottom, barZ),
                        new Vec3(
                            barX,
                            bottom + (top - bottom) * percent,
                            barZ
                        ),
                        healthColor,
                        4.0F
                    ).setAlwaysOnTop();
                }

                Vec3 hpPosition = new Vec3(
                    box.getCenter().x,
                    box.maxY + (
                        EspOverlaySettings.showNames()
                            ? 0.18 : 0.35
                    ),
                    box.getCenter().z
                );

                Gizmos.billboardText(
                    EspOverlaySettings.getHealthText(entity),
                    hpPosition,
                    TextGizmo.Style
                        .forColorAndCentered(healthColor)
                        .withScale(0.7F)
                ).setAlwaysOnTop();
            }

            // Adjustable tracer
            if (EspOverlaySettings.showTracers()) {

                Vec3 target = box.getCenter();

                Gizmos.line(
                    tracerStart,
                    target,
                    color,
                    EspOverlaySettings.getTracerThickness()
                ).setAlwaysOnTop();
            }
        }
    }

    private static void drawCorners(AABB box, int color) {

        double minX = box.minX;
        double maxX = box.maxX;
        double minY = box.minY;
        double maxY = box.maxY;
        double minZ = box.minZ;
        double maxZ = box.maxZ;

        double dx = Math.min(
            (maxX - minX) * 0.28,
            0.25
        );

        double dy = Math.min(
            (maxY - minY) * 0.20,
            0.35
        );

        double dz = Math.min(
            (maxZ - minZ) * 0.28,
            0.25
        );

        // Eight corners, each with three short edges
        for (int xi = 0; xi < 2; xi++) {
            for (int yi = 0; yi < 2; yi++) {
                for (int zi = 0; zi < 2; zi++) {

                    double x = xi == 0 ? minX : maxX;
                    double y = yi == 0 ? minY : maxY;
                    double z = zi == 0 ? minZ : maxZ;

                    double xDirection = xi == 0 ? 1 : -1;
                    double yDirection = yi == 0 ? 1 : -1;
                    double zDirection = zi == 0 ? 1 : -1;

                    Vec3 corner = new Vec3(x, y, z);

                    Gizmos.line(
                        corner,
                        new Vec3(
                            x + xDirection * dx,
                            y,
                            z
                        ),
                        color,
                        2.5F
                    ).setAlwaysOnTop();

                    Gizmos.line(
                        corner,
                        new Vec3(
                            x,
                            y + yDirection * dy,
                            z
                        ),
                        color,
                        2.5F
                    ).setAlwaysOnTop();

                    Gizmos.line(
                        corner,
                        new Vec3(
                            x,
                            y,
                            z + zDirection * dz
                        ),
                        color,
                        2.5F
                    ).setAlwaysOnTop();
                }
            }
        }
    }

    private static int healthColor(float percent) {

        percent = Math.max(
            0.0F,
            Math.min(1.0F, percent)
        );

        int red = (int) (255 * (1.0F - percent));
        int green = (int) (255 * percent);

        return 0xFF000000 | (red << 16) | (green << 8);
    }
}
