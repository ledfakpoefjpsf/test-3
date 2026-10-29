
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

public final class EntityRadarRenderer {

    private static final int RADIUS = 55;
    private static final int DETECTION_RANGE = 64;

    private static final int PLAYER_COLOR = 0xFF43E5FF;
    private static final int HOSTILE_COLOR = 0xFFFF526F;
    private static final int MOB_COLOR = 0xFFFFD56D;

    private static final int PURPLE = 0xFFAD80FF;

    private EntityRadarRenderer() {
    }

    public static void initialize() {

        HudRenderCallback.EVENT.register(
            (graphics, tickCounter) -> render(graphics)
        );
    }

    private static boolean enabled() {

        Module module =
            NovaClient.MODULES.getByName("Entity Radar");

        return module != null && module.isEnabled();
    }

    private static void render(GuiGraphics graphics) {

        Minecraft mc = Minecraft.getInstance();

        if (!enabled()
            || mc.player == null
            || mc.level == null
            || mc.options.hideGui) {
            return;
        }

        // Place radar below the minimap when both are enabled.
        Module minimap =
            NovaClient.MODULES.getByName("Minimap");

        boolean minimapEnabled =
            minimap != null && minimap.isEnabled();

        int cx = graphics.guiWidth() - RADIUS - 20;

        int cy = minimapEnabled
            ? 25 + 123 + 55 + 35
            : RADIUS + 35;

        // Background panel.
        graphics.fill(
            cx - RADIUS - 9,
            cy - RADIUS - 24,
            cx + RADIUS + 9,
            cy + RADIUS + 21,
            0xB00B0717
        );

        graphics.drawString(
            mc.font,
            "ENTITY RADAR",
            cx - RADIUS,
            cy - RADIUS - 17,
            0xFFD7B7FF,
            true
        );

        // Circular radar grid.
        drawCircle(
            graphics,
            cx,
            cy,
            RADIUS,
            PURPLE
        );

        drawCircle(
            graphics,
            cx,
            cy,
            RADIUS / 2,
            0xFF5D4787
        );

        graphics.fill(
            cx - RADIUS,
            cy,
            cx + RADIUS,
            cy + 1,
            0x665E4B82
        );

        graphics.fill(
            cx,
            cy - RADIUS,
            cx + 1,
            cy + RADIUS,
            0x665E4B82
        );

        // Player center marker.
        graphics.fill(
            cx - 3,
            cy - 3,
            cx + 4,
            cy + 4,
            0xFFDDACFF
        );

        double playerYaw =
            Math.toRadians(mc.player.getYRot());

        double cos = Math.cos(playerYaw);
        double sin = Math.sin(playerYaw);

        int count = 0;

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (entity == mc.player
                || entity.isRemoved()) {
                continue;
            }

            int color;

            if (entity instanceof Player) {
                color = PLAYER_COLOR;

            } else if (entity instanceof Monster) {
                color = HOSTILE_COLOR;

            } else if (entity instanceof Mob) {
                color = MOB_COLOR;

            } else {
                continue;
            }

            double dx =
                entity.getX() - mc.player.getX();

            double dz =
                entity.getZ() - mc.player.getZ();

            double distanceSquared =
                dx * dx + dz * dz;

            if (distanceSquared
                > DETECTION_RANGE * DETECTION_RANGE) {
                continue;
            }

            // Rotate radar so forward is up.
            double rotatedX =
                dx * cos + dz * sin;

            double rotatedZ =
                -dx * sin + dz * cos;

            double scale =
                (RADIUS - 5.0) / DETECTION_RANGE;

            int dotX =
                cx + (int) Math.round(rotatedX * scale);

            int dotY =
                cy + (int) Math.round(rotatedZ * scale);

            double radarDX = dotX - cx;
            double radarDY = dotY - cy;

            if (radarDX * radarDX
                + radarDY * radarDY
                > (RADIUS - 3) * (RADIUS - 3)) {
                continue;
            }

            graphics.fill(
                dotX - 2,
                dotY - 2,
                dotX + 3,
                dotY + 3,
                0xFF090612
            );

            graphics.fill(
                dotX - 1,
                dotY - 1,
                dotX + 2,
                dotY + 2,
                color
            );

            count++;
        }

        graphics.drawString(
            mc.font,
            "Targets: " + count,
            cx - RADIUS,
            cy + RADIUS + 7,
            0xFFC7B6E4,
            true
        );
    }

    private static void drawCircle(
        GuiGraphics graphics,
        int cx,
        int cy,
        int radius,
        int color
    ) {

        // Draw a circular outline using small rectangles.
        for (int angle = 0; angle < 360; angle += 2) {

            double radians = Math.toRadians(angle);

            int x = cx + (int) Math.round(
                Math.cos(radians) * radius
            );

            int y = cy + (int) Math.round(
                Math.sin(radians) * radius
            );

            graphics.fill(
                x,
                y,
                x + 2,
                y + 2,
                color
            );
        }
    }
}
