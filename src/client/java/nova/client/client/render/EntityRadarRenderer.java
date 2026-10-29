package nova.client.client.render;

// @name: EntityRadarRenderer
// @type: Client HUD Renderer
// @location: src/client/java/nova/client/client/render/EntityRadarRenderer.java

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.RadarSettings;

public final class EntityRadarRenderer {

    private static final int CYAN = 0xFF43E5FF;
    private static final int RED = 0xFFFF526F;
    private static final int YELLOW = 0xFFFFD56D;
    private static final int PURPLE = 0xFFAD80FF;

    private EntityRadarRenderer() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(
            (graphics, tickCounter) -> render(graphics)
        );
    }

    private static void render(GuiGraphics g) {

        Minecraft mc = Minecraft.getInstance();

        Module module =
            NovaClient.MODULES.getByName("Entity Radar");

        if (module == null
            || !module.isEnabled()
            || mc.player == null
            || mc.level == null
            || mc.options.hideGui) {
            return;
        }

        int diameter = RadarSettings.size;
        int radius = diameter / 2;

        Module minimap =
            NovaClient.MODULES.getByName("Minimap");

        boolean showMap =
            minimap != null && minimap.isEnabled();

        int cx = g.guiWidth() - radius - 20;

        int cy = showMap
            ? 25 + 123 + radius + 35
            : radius + 35;

        cx = Math.max(radius + 12, cx);

        cy = Math.max(
            radius + 27,
            Math.min(g.guiHeight() - radius - 22, cy)
        );

        // Background
        g.fill(
            cx - radius - 9,
            cy - radius - 24,
            cx + radius + 9,
            cy + radius + 21,
            0xC00B0717
        );

        // Title
        g.drawString(
            mc.font,
            "ENTITY RADAR",
            cx - radius,
            cy - radius - 17,
            0xFFD7B7FF,
            true
        );

        // Radar circles
        drawCircle(
            g, cx, cy, radius, PURPLE
        );

        drawCircle(
            g, cx, cy, radius / 2, 0xFF5D4787
        );

        // Crosshair
        g.fill(
            cx - radius, cy,
            cx + radius, cy + 1,
            0x665E4B82
        );

        g.fill(
            cx, cy - radius,
            cx + 1, cy + radius,
            0x665E4B82
        );

        // Player center marker
        g.fill(
            cx - 3, cy - 3,
            cx + 4, cy + 4,
            0xFFDDACFF
        );

        double yaw =
            Math.toRadians(mc.player.getYRot());

        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        int count = 0;

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (entity == mc.player || entity.isRemoved()) {
                continue;
            }

            int color;

            if (entity instanceof Player) {

                if (!RadarSettings.players) {
                    continue;
                }

                color = CYAN;

            } else if (entity instanceof Monster) {

                if (!RadarSettings.hostile) {
                    continue;
                }

                color = RED;

            } else if (entity instanceof Mob) {

                if (!RadarSettings.passive) {
                    continue;
                }

                color = YELLOW;

            } else {
                continue;
            }

            double dx =
                entity.getX() - mc.player.getX();

            double dz =
                entity.getZ() - mc.player.getZ();

            double distance =
                Math.sqrt(dx * dx + dz * dz);

            if (distance > RadarSettings.range) {
                continue;
            }

            // Optional rotation
            double rx = RadarSettings.rotate
                ? dx * cos + dz * sin
                : dx;

            double rz = RadarSettings.rotate
                ? dx * sin - dz * cos
                : dz;

            double scale =
                (radius - 5.0) / RadarSettings.range;

            int dotX =
                cx + (int) Math.round(rx * scale);

            int dotY =
                cy + (int) Math.round(rz * scale);

            double offX = dotX - cx;
            double offY = dotY - cy;

            if (offX * offX + offY * offY
                > (radius - 3) * (radius - 3)) {
                continue;
            }

            // Dot outline
            g.fill(
                dotX - 2,
                dotY - 2,
                dotX + 3,
                dotY + 3,
                0xFF090612
            );

            // Entity dot
            g.fill(
                dotX - 1,
                dotY - 1,
                dotX + 2,
                dotY + 2,
                color
            );

            // Optional name and distance
            if (RadarSettings.names || RadarSettings.distance) {

                String label = RadarSettings.names
                    ? entity.getName().getString()
                    : "";

                if (RadarSettings.distance) {

                    label +=
                        (label.isEmpty() ? "" : " ")
                        + (int) Math.round(distance)
                        + "m";
                }

                if (label.length() > 22) {
                    label = label.substring(0, 22);
                }

                int labelX = Math.max(
                    2,
                    Math.min(
                        g.guiWidth() - mc.font.width(label) - 2,
                        dotX + 4
                    )
                );

                int labelY = Math.max(
                    2,
                    Math.min(
                        g.guiHeight() - 10,
                        dotY - 5
                    )
                );

                g.drawString(
                    mc.font,
                    label,
                    labelX,
                    labelY,
                    color,
                    true
                );
            }

            count++;
        }

        // Footer
        g.drawString(
            mc.font,
            "Targets: " + count
                + "  " + RadarSettings.range + "m",
            cx - radius,
            cy + radius + 7,
            0xFFC7B6E4,
            true
        );
    }

    private static void drawCircle(
        GuiGraphics g,
        int cx,
        int cy,
        int radius,
        int color
    ) {

        for (int angle = 0; angle < 360; angle += 2) {

            double radians = Math.toRadians(angle);

            int x = cx + (int) Math.round(
                Math.cos(radians) * radius
            );

            int y = cy + (int) Math.round(
                Math.sin(radians) * radius
            );

            g.fill(
                x, y,
                x + 2, y + 2,
                color
            );
        }
    }
}
