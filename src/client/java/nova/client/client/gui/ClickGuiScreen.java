
package nova.client.client.gui;

// @name: ClickGuiScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/ClickGuiScreen.java

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.NovaClient;
import nova.client.client.module.Category;
import nova.client.client.module.Module;
import nova.client.client.module.MobEspSettings;
import nova.client.client.module.EspOverlaySettings;

import org.lwjgl.glfw.GLFW;

public final class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 145;
    private static final int PANEL_H = 245;
    private static final int HEADER_H = 27;
    private static final int ROW_H = 21;

    private static final int BACKGROUND = 0xE8091522;
    private static final int PANEL = 0xF5102638;
    private static final int HEADER = 0xFF194663;
    private static final int ROW = 0xFF102435;
    private static final int ACTIVE = 0xFF205579;
    private static final int BORDER = 0xFF274A63;
    private static final int PURPLE = 0xFF83C9ED;
    private static final int WHITE = 0xFFE0F3FF;
    private static final int MUTED = 0xFF8DADC2;

    private final List<CategoryPanel> panels =
        new ArrayList<>();

    private CategoryPanel draggingPanel;

    private int dragOffsetX;
    private int dragOffsetY;
    private int mouseX;
    private int mouseY;

    private boolean previousLeft;
    private boolean previousRight;

    private Module espSettingsModule;
    private int mobScroll;

    private static final class CategoryPanel {
        final Category category;
        int x;
        int y;
        int scroll;
        boolean collapsed;

        CategoryPanel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
    }

    public ClickGuiScreen() {
        super(Component.literal("Nova Nebula v3"));
    }

    @Override
    protected void init() {
        if (!panels.isEmpty()) {
            return;
        }

        Category[] categories = Category.values();

        int columns = Math.max(
            1,
            Math.min(
                categories.length,
                (width - 20) / (PANEL_W + 10)
            )
        );

        int totalWidth =
            columns * PANEL_W + (columns - 1) * 10;

        int startX = Math.max(
            8,
            (width - totalWidth) / 2
        );

        for (int i = 0; i < categories.length; i++) {
            panels.add(new CategoryPanel(
                categories[i],
                startX + (i % columns) * (PANEL_W + 10),
                53 + (i / columns) * (PANEL_H + 10)
            ));
        }
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

        boolean right = GLFW.glfwGetMouseButton(
            window,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) == GLFW.GLFW_PRESS;

        if (left && !previousLeft) {
            handleClick(mouseX, mouseY, false);
        }

        if (right && !previousRight) {
            handleClick(mouseX, mouseY, true);
        }

        if (left && draggingPanel != null) {
            draggingPanel.x = Math.max(
                0,
                Math.min(
                    width - PANEL_W,
                    mouseX - dragOffsetX
                )
            );

            draggingPanel.y = Math.max(
                40,
                Math.min(
                    height - HEADER_H,
                    mouseY - dragOffsetY
                )
            );
        }

        if (!left) {
            draggingPanel = null;
        }

        previousLeft = left;
        previousRight = right;
    }

    private boolean inside(
        int mx,
        int my,
        int x,
        int y,
        int w,
        int h
    ) {
        return mx >= x
            && mx < x + w
            && my >= y
            && my < y + h;
    }

    private int visibleRows() {
        return (PANEL_H - HEADER_H - 20) / ROW_H;
    }

    private void handleClick(
        int mx,
        int my,
        boolean rightClick
    ) {
        if (espSettingsModule != null) {
            handleEspClick(mx, my, rightClick);
            return;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = panels.get(i);

            if (inside(
                mx, my,
                panel.x, panel.y,
                PANEL_W, HEADER_H
            )) {
                if (rightClick) {
                    panel.collapsed = !panel.collapsed;
                } else {
                    draggingPanel = panel;
                    dragOffsetX = mx - panel.x;
                    dragOffsetY = my - panel.y;

                    panels.remove(i);
                    panels.add(panel);
                }

                return;
            }

            if (panel.collapsed) {
                continue;
            }

            if (!inside(
                mx, my,
                panel.x,
                panel.y + HEADER_H,
                PANEL_W,
                PANEL_H - HEADER_H - 15
            )) {
                continue;
            }

            int relativeY =
                my - panel.y - HEADER_H - 4;

            if (relativeY < 0) {
                return;
            }

            int row = relativeY / ROW_H;

            if (row >= visibleRows()) {
                return;
            }

            List<Module> modules =
                NovaClient.MODULES.getByCategory(
                    panel.category
                );

            int index = panel.scroll + row;

            if (index >= modules.size()) {
                return;
            }

            Module module = modules.get(index);

            if (rightClick) {
                String name = module.getName();

                // MACRO SETTINGS
                if (name.equalsIgnoreCase("Macro Recorder")) {
                    Minecraft.getInstance().setScreen(
                        new MacroSettingsScreen(this)
                    );
                    return;
                }

                // X-RAY SETTINGS
                if (name.equalsIgnoreCase("X-Ray")) {
                    Minecraft.getInstance().setScreen(
                        new XraySettingsScreen(this)
                    );
                    return;
                }

                // RADAR SETTINGS
                if (name.equalsIgnoreCase("Entity Radar")) {
                    Minecraft.getInstance().setScreen(
                        new RadarSettingsScreen(this)
                    );
                    return;
                }

                // ESP SETTINGS
                if (name.equalsIgnoreCase("Mob ESP")
                    || name.equalsIgnoreCase("Player ESP")) {
                    espSettingsModule = module;
                    mobScroll = 0;
                }
            } else {
                NovaClient.MODULES.toggleModule(module);
                NovaClient.CONFIG.save();
            }

            return;
        }
    }

    private int settingsX() {
        return (width - 360) / 2;
    }

    private int settingsY() {
        return Math.max(40, (height - 360) / 2);
    }

    private void handleEspClick(
        int mx,
        int my,
        boolean rightClick
    ) {
        int x = settingsX();
        int y = settingsY();

        if (rightClick
            || inside(mx, my, x + 10, y + 10, 65, 22)) {
            espSettingsModule = null;
            return;
        }

        if (inside(mx, my, x + 10, y + 50, 105, 25)) {
            EspOverlaySettings.toggleNames();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(mx, my, x + 127, y + 50, 105, 25)) {
            EspOverlaySettings.toggleHealth();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(mx, my, x + 244, y + 50, 105, 25)) {
            EspOverlaySettings.toggleTracers();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(mx, my, x + 15, y + 104, 330, 22)) {
            float progress = Math.max(
                0.0F,
                Math.min(
                    1.0F,
                    (mx - x - 15) / 330.0F
                )
            );

            float thickness =
                1.0F + Math.round(progress * 18) * 0.5F;

            EspOverlaySettings.setTracerThickness(
                thickness
            );

            NovaClient.CONFIG.save();
            return;
        }

        if (!espSettingsModule.getName()
            .equalsIgnoreCase("Mob ESP")) {
            return;
        }

        if (inside(mx, my, x + 255, y + 10, 45, 22)) {
            MobEspSettings.selectAll();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(mx, my, x + 305, y + 10, 45, 22)) {
            MobEspSettings.selectNone();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(mx, my, x + 10, y + 328, 60, 20)) {
            mobScroll = Math.max(0, mobScroll - 6);
            return;
        }

        if (inside(mx, my, x + 290, y + 328, 60, 20)) {
            mobScroll = Math.min(
                Math.max(
                    0,
                    MobEspSettings.MOBS.length - 12
                ),
                mobScroll + 6
            );
            return;
        }

        for (int i = 0; i < 12; i++) {
            int index = mobScroll + i;

            if (index >= MobEspSettings.MOBS.length) {
                break;
            }

            int bx = x + 10 + (i % 2) * 175;
            int by = y + 139 + (i / 2) * 30;

            if (inside(mx, my, bx, by, 165, 25)) {
                MobEspSettings.toggle(
                    MobEspSettings.MOBS[index]
                );

                NovaClient.CONFIG.save();
                return;
            }
        }
    }

    @Override
    public boolean mouseScrolled(
        double mx,
        double my,
        double horizontalAmount,
        double verticalAmount
    ) {
        int direction = verticalAmount > 0 ? -1 : 1;

        if (espSettingsModule != null) {
            if (espSettingsModule.getName()
                .equalsIgnoreCase("Mob ESP")) {

                mobScroll = Math.max(
                    0,
                    Math.min(
                        Math.max(
                            0,
                            MobEspSettings.MOBS.length - 12
                        ),
                        mobScroll + direction * 3
                    )
                );
            }

            return true;
        }

        for (CategoryPanel panel : panels) {
            if (panel.collapsed) {
                continue;
            }

            if (!inside(
                (int) mx,
                (int) my,
                panel.x,
                panel.y,
                PANEL_W,
                PANEL_H
            )) {
                continue;
            }

            int count = NovaClient.MODULES
                .getByCategory(panel.category)
                .size();

            panel.scroll = Math.max(
                0,
                Math.min(
                    Math.max(
                        0,
                        count - visibleRows()
                    ),
                    panel.scroll + direction
                )
            );

            return true;
        }

        return super.mouseScrolled(
            mx,
            my,
            horizontalAmount,
            verticalAmount
        );
    }

    @Override
    public void render(
        GuiGraphics g,
        int mx,
        int my,
        float delta
    ) {
        g.fill(
            0, 0,
            width, height,
            BACKGROUND
        );

        g.fill(
            0, 0,
            width, 39,
            PANEL
        );

        g.drawString(
            font,
            "NOVA NEBULA v3",
            12, 14,
            WHITE,
            true
        );

        g.drawString(
            font,
            "LEFT: TOGGLE  |  RIGHT: SETTINGS",
            145, 14,
            MUTED,
            false
        );

        for (CategoryPanel panel : panels) {
            drawPanel(g, panel);
        }

        if (espSettingsModule != null) {
            drawEspSettings(g);
        }
    }

    private void drawPanel(
        GuiGraphics g,
        CategoryPanel panel
    ) {
        int x = panel.x;
        int y = panel.y;

        int panelHeight =
            panel.collapsed ? HEADER_H : PANEL_H;

        g.fill(
            x, y,
            x + PANEL_W,
            y + panelHeight,
            PANEL
        );

        g.fill(
            x, y,
            x + PANEL_W,
            y + HEADER_H,
            HEADER
        );

        g.drawString(
            font,
            panel.category.name(),
            x + 8,
            y + 9,
            WHITE,
            true
        );

        g.drawString(
            font,
            panel.collapsed ? "+" : "-",
            x + PANEL_W - 13,
            y + 9,
            PURPLE,
            true
        );

        if (panel.collapsed) {
            return;
        }

        List<Module> modules =
            NovaClient.MODULES.getByCategory(
                panel.category
            );

        for (int row = 0; row < visibleRows(); row++) {
            int index = panel.scroll + row;

            if (index >= modules.size()) {
                break;
            }

            Module module = modules.get(index);

            int ry =
                y + HEADER_H + 4 + row * ROW_H;

            g.fill(
                x + 4,
                ry,
                x + PANEL_W - 4,
                ry + ROW_H - 2,
                module.isEnabled() ? ACTIVE : ROW
            );

            String name = module.getName();

            while (name.length() > 1
                && font.width(name) > PANEL_W - 25) {
                name = name.substring(
                    0,
                    name.length() - 1
                );
            }

            g.drawString(
                font,
                name,
                x + 10,
                ry + 6,
                module.isEnabled() ? WHITE : MUTED,
                false
            );

            g.fill(
                x + PANEL_W - 13,
                ry + 7,
                x + PANEL_W - 8,
                ry + 12,
                module.isEnabled()
                    ? PURPLE
                    : 0xFF51425F
            );
        }

        g.drawString(
            font,
            modules.size() + " modules",
            x + 8,
            y + PANEL_H - 13,
            MUTED,
            false
        );
    }

    private void drawEspSettings(GuiGraphics g) {
        int x = settingsX();
        int y = settingsY();

        g.fill(
            0, 39,
            width, height,
            0xC0000000
        );

        g.fill(
            x - 1,
            y - 1,
            x + 361,
            y + 361,
            BORDER
        );

        g.fill(
            x, y,
            x + 360,
            y + 360,
            PANEL
        );

        g.fill(
            x, y,
            x + 360,
            y + 36,
            HEADER
        );

        drawButton(
            g,
            x + 10,
            y + 10,
            65,
            22,
            "BACK",
            false
        );

        boolean mob =
            espSettingsModule.getName()
                .equalsIgnoreCase("Mob ESP");

        if (mob) {
            drawButton(
                g, x + 255, y + 10,
                45, 22,
                "ALL", false
            );

            drawButton(
                g, x + 305, y + 10,
                45, 22,
                "NONE", false
            );
        }

        drawButton(
            g, x + 10, y + 50,
            105, 25,
            "Names",
            EspOverlaySettings.showNames()
        );

        drawButton(
            g, x + 127, y + 50,
            105, 25,
            "Health",
            EspOverlaySettings.showHealth()
        );

        drawButton(
            g, x + 244, y + 50,
            105, 25,
            "Tracers",
            EspOverlaySettings.showTracers()
        );

        g.drawString(
            font,
            "Tracer Thickness: "
                + EspOverlaySettings.getTracerThickness(),
            x + 15,
            y + 90,
            WHITE,
            false
        );

        g.fill(
            x + 15,
            y + 110,
            x + 345,
            y + 117,
            ROW
        );

        int sliderFill = (int) (
            (EspOverlaySettings.getTracerThickness() - 1)
                / 9.0F * 330
        );

        g.fill(
            x + 15,
            y + 110,
            x + 15 + sliderFill,
            y + 117,
            PURPLE
        );

        if (!mob) {
            return;
        }

        for (int i = 0; i < 12; i++) {
            int index = mobScroll + i;

            if (index >= MobEspSettings.MOBS.length) {
                break;
            }

            String id =
                MobEspSettings.MOBS[index];

            drawButton(
                g,
                x + 10 + (i % 2) * 175,
                y + 139 + (i / 2) * 30,
                165,
                25,
                id.replace('_', ' '),
                MobEspSettings.isSelected(id)
            );
        }

        drawButton(
            g, x + 10, y + 328,
            60, 20,
            "PREV", false
        );

        drawButton(
            g, x + 290, y + 328,
            60, 20,
            "NEXT", false
        );
    }

    private void drawButton(
        GuiGraphics g,
        int x,
        int y,
        int w,
        int h,
        String label,
        boolean enabled
    ) {
        g.fill(
            x, y,
            x + w,
            y + h,
            enabled ? ACTIVE : ROW
        );

        g.drawCenteredString(
            font,
            label,
            x + w / 2,
            y + (h - 8) / 2,
            enabled ? WHITE : MUTED
        );
    }

    @Override
    public void onClose() {
        NovaClient.CONFIG.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
