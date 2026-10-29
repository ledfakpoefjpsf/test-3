package nova.client.client.gui;

// @name: RadarSettingsScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/RadarSettingsScreen.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.NovaClient;
import nova.client.client.module.RadarSettings;

import org.lwjgl.glfw.GLFW;

public final class RadarSettingsScreen extends Screen {

    private final Screen parent;
    private boolean previousLeft;
    private int dragging = -1;
    private int mouseX;
    private int mouseY;

    public RadarSettingsScreen(Screen parent) {
        super(Component.literal("Nova Entity Radar Settings"));
        this.parent = parent;
    }

    private int panelX() {
        return (width - 330) / 2;
    }

    private int panelY() {
        return Math.max(12, (height - 330) / 2);
    }

    @Override
    public void tick() {

        Minecraft mc = Minecraft.getInstance();

        if (mc.getWindow() == null) {
            return;
        }

        long window = mc.getWindow().handle();

        mouseX = (int) (
            mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth()
        );

        mouseY = (int) (
            mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight()
        );

        boolean left = GLFW.glfwGetMouseButton(
            window,
            GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        if (left && !previousLeft) {
            click(mouseX, mouseY);
        }

        if (left && dragging >= 0) {
            updateSlider(mouseX, dragging);
        }

        if (!left && previousLeft && dragging >= 0) {
            dragging = -1;
            NovaClient.CONFIG.save();
        }

        previousLeft = left;
    }

    private boolean inside(
        int mx, int my,
        int x, int y,
        int w, int h
    ) {
        return mx >= x
            && my >= y
            && mx < x + w
            && my < y + h;
    }

    private void click(int mx, int my) {

        int x = panelX();
        int y = panelY();

        // Back button
        if (inside(mx, my, x + 263, y + 9, 57, 20)) {
            onClose();
            return;
        }

        // Detection range slider
        if (inside(mx, my, x + 15, y + 57, 300, 24)) {
            dragging = 0;
            updateSlider(mx, 0);
            return;
        }

        // Radar size slider
        if (inside(mx, my, x + 15, y + 109, 300, 24)) {
            dragging = 1;
            updateSlider(mx, 1);
            return;
        }

        // Toggle settings
        for (int i = 0; i < 6; i++) {

            int ry = y + 151 + i * 25;

            if (!inside(mx, my, x + 15, ry, 300, 22)) {
                continue;
            }

            switch (i) {

                case 0 ->
                    RadarSettings.players = !RadarSettings.players;

                case 1 ->
                    RadarSettings.hostile = !RadarSettings.hostile;

                case 2 ->
                    RadarSettings.passive = !RadarSettings.passive;

                case 3 ->
                    RadarSettings.rotate = !RadarSettings.rotate;

                case 4 ->
                    RadarSettings.names = !RadarSettings.names;

                case 5 ->
                    RadarSettings.distance = !RadarSettings.distance;

                default -> {
                }
            }

            NovaClient.CONFIG.save();
            return;
        }
    }

    private void updateSlider(int mx, int which) {

        double fraction = Math.max(
            0,
            Math.min(
                1,
                (mx - panelX() - 20) / 290.0
            )
        );

        if (which == 0) {

            RadarSettings.setRange(
                16 + (int) Math.round(fraction * 240)
            );

        } else {

            RadarSettings.setSize(
                80 + (int) Math.round(fraction * 120)
            );
        }
    }

    @Override
    public void onClose() {

        NovaClient.CONFIG.save();

        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(
        GuiGraphics g,
        int mx,
        int my,
        float delta
    ) {

        g.fill(
            0, 0, width, height,
            0xD00B0717
        );

        int x = panelX();
        int y = panelY();

        // Panel border
        g.fill(
            x - 1, y - 1,
            x + 331, y + 331,
            0xFFAB78F8
        );

        // Panel background
        g.fill(
            x, y,
            x + 330, y + 330,
            0xFF171020
        );

        // Header
        g.fill(
            x, y,
            x + 330, y + 36,
            0xFF422663
        );

        g.drawString(
            font,
            "NOVA  /  ENTITY RADAR",
            x + 13,
            y + 13,
            0xFFF3E8FF,
            true
        );

        g.drawString(
            font,
            "BACK",
            x + 276,
            y + 14,
            0xFFFFFFFF,
            true
        );

        drawSlider(
            g, x, y + 46,
            "Detection Range",
            RadarSettings.range,
            16, 256,
            " blocks"
        );

        drawSlider(
            g, x, y + 98,
            "Radar Size",
            RadarSettings.size,
            80, 200,
            " px"
        );

        drawToggle(
            g, x, y + 151,
            "Show Players",
            RadarSettings.players,
            0xFF43E5FF
        );

        drawToggle(
            g, x, y + 176,
            "Show Hostile Mobs",
            RadarSettings.hostile,
            0xFFFF526F
        );

        drawToggle(
            g, x, y + 201,
            "Show Other Mobs",
            RadarSettings.passive,
            0xFFFFD56D
        );

        drawToggle(
            g, x, y + 226,
            "Rotate With Player",
            RadarSettings.rotate,
            0xFFC084FC
        );

        drawToggle(
            g, x, y + 251,
            "Show Entity Names",
            RadarSettings.names,
            0xFFC084FC
        );

        drawToggle(
            g, x, y + 276,
            "Show Distance",
            RadarSettings.distance,
            0xFFC084FC
        );

        g.drawString(
            font,
            "Saved automatically  |  ESC to return",
            x + 15,
            y + 310,
            0xFFA99CB9,
            false
        );
    }

    private void drawSlider(
        GuiGraphics g,
        int x,
        int y,
        String name,
        int value,
        int min,
        int max,
        String suffix
    ) {

        g.drawString(
            font,
            name + ": " + value + suffix,
            x + 15,
            y,
            0xFFEBD8FF,
            true
        );

        g.fill(
            x + 20, y + 20,
            x + 310, y + 25,
            0xFF38264B
        );

        int knob = x + 20
            + (int) Math.round(
                (value - min) * 290.0 / (max - min)
            );

        g.fill(
            x + 20, y + 20,
            knob, y + 25,
            0xFFB37AFF
        );

        g.fill(
            knob - 3, y + 16,
            knob + 4, y + 29,
            0xFFF0D6FF
        );
    }

    private void drawToggle(
        GuiGraphics g,
        int x,
        int y,
        String name,
        boolean enabled,
        int color
    ) {

        g.fill(
            x + 15, y,
            x + 315, y + 22,
            0xFF251A33
        );

        g.drawString(
            font,
            name,
            x + 24,
            y + 7,
            color,
            true
        );

        g.fill(
            x + 269, y + 4,
            x + 307, y + 18,
            enabled ? 0xFF6E40AA : 0xFF40364A
        );

        g.drawString(
            font,
            enabled ? "ON" : "OFF",
            x + 279,
            y + 7,
            enabled ? 0xFFFFFFFF : 0xFFB6A9C5,
            false
        );
    }
}
