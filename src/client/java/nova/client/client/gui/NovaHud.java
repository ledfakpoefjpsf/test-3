
package nova.client.client.gui;

// @name: NovaHud
// @type: Client HUD Renderer
// @location: src/client/java/nova/client/client/gui/NovaHud.java

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;

public final class NovaHud {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int PURPLE = 0xFFBD92FF;
    private static final int BACKGROUND = 0xA5100B1B;

    private NovaHud() {}

    public static void initialize() {
        HudRenderCallback.EVENT.register(
            (graphics, tickCounter) -> render(graphics)
        );
    }

    private static boolean enabled(String name) {
        Module module = NovaClient.MODULES.getByName(name);
        return module != null && module.isEnabled();
    }

    private static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        int x = 8;
        int y = 8;

        if (enabled("Coordinates HUD")) {

            String coordinates = String.format(
                java.util.Locale.ROOT,
                "XYZ: %.1f / %.1f / %.1f",
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ()
            );

            drawLine(graphics, mc, coordinates, x, y);
            y += 17;
        }

        if (enabled("FPS HUD")) {

            String fps =
                "FPS: " + mc.getFps();

            drawLine(graphics, mc, fps, x, y);
        }
    }

    private static void drawLine(
        GuiGraphics graphics,
        Minecraft mc,
        String message,
        int x,
        int y
    ) {

        int width = mc.font.width(message) + 12;

        graphics.fill(
            x,
            y,
            x + width,
            y + 15,
            BACKGROUND
        );

        graphics.fill(
            x,
            y,
            x + 2,
            y + 15,
            PURPLE
        );

        graphics.drawString(
            mc.font,
            message,
            x + 6,
            y + 4,
            WHITE,
            false
        );
    }
}
