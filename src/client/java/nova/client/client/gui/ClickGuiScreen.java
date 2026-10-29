package nova.client.client.gui;

// @name: ClickGuiScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/ClickGuiScreen.java

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class ClickGuiScreen extends Screen {

    private static final int BG = 0xD00C0915;
    private static final int PANEL = 0xF0141020;
    private static final int BORDER = 0xFF65448E;
    private static final int HEADER = 0xFF422663;
    private static final int ROW = 0xFF211A30;
    private static final int ACTIVE = 0xFF432B61;
    private static final int PURPLE = 0xFFC084FC;
    private static final int WHITE = 0xFFF4EEFF;
    private static final int MUTED = 0xFFA99CB9;

    private static final int PANEL_W = 125;
    private static final int PANEL_H = 215;
    private static final int HEADER_H = 25;
    private static final int ROW_H = 19;

    private static final int SETTINGS_W = 390;
    private static final int SETTINGS_H = 370;

    private final List<Panel> panels = new ArrayList<>();

    private boolean lastLeft;
    private boolean lastRight;
    private boolean lastPageUp;
    private boolean lastPageDown;

    private Panel dragging;
    private boolean draggingSlider;

    private int dragOffsetX;
    private int dragOffsetY;

    private Module expandedModule;
    private int mobScroll;

    private int mouseX;
    private int mouseY;

    private long openedAt;

    private static final class Panel {

        final Category category;

        int x;
        int y;
        int scroll;

        boolean collapsed;

        Panel(Category category, int x, int y) {
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

        openedAt = System.currentTimeMillis();

        if (!panels.isEmpty()) {
            return;
        }

        Category[] categories = Category.values();

        int columns = width >= 850 ? 6 : 3;

        int totalWidth =
            columns * PANEL_W + (columns - 1) * 9;

        int startX = Math.max(
            8,
            (width - totalWidth) / 2
        );

        for (int i = 0; i < categories.length; i++) {

            panels.add(new Panel(
                categories[i],
                startX + (i % columns) * (PANEL_W + 9),
                57 + (i / columns) * (PANEL_H + 9)
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

        boolean pageUp = GLFW.glfwGetKey(
            window,
            GLFW.GLFW_KEY_PAGE_UP
        ) == GLFW.GLFW_PRESS;

        boolean pageDown = GLFW.glfwGetKey(
            window,
            GLFW.GLFW_KEY_PAGE_DOWN
        ) == GLFW.GLFW_PRESS;

        if (left && !lastLeft) {
            handleClick(mouseX, mouseY, false);
        }

        if (right && !lastRight) {
            handleClick(mouseX, mouseY, true);
        }

        if (draggingSlider && left) {
            updateTracerSlider(mouseX);
        }

        if (dragging != null && left) {

            dragging.x = Math.max(
                0,
                Math.min(
                    width - PANEL_W,
                    mouseX - dragOffsetX
                )
            );

            dragging.y = Math.max(
                43,
                Math.min(
                    height - HEADER_H,
                    mouseY - dragOffsetY
                )
            );
        }

        if (!left) {
            dragging = null;

            if (draggingSlider) {
                NovaClient.CONFIG.save();
            }

            draggingSlider = false;
        }

        if (pageUp && !lastPageUp) {
            scrollHovered(-1);
        }

        if (pageDown && !lastPageDown) {
            scrollHovered(1);
        }

        lastLeft = left;
        lastRight = right;
        lastPageUp = pageUp;
        lastPageDown = pageDown;
    }

    @Override
    public boolean mouseScrolled(
        double mx,
        double my,
        double horizontalAmount,
        double verticalAmount
    ) {

        if (verticalAmount == 0.0D) {
            return super.mouseScrolled(
                mx,
                my,
                horizontalAmount,
                verticalAmount
            );
        }

        mouseX = (int) mx;
        mouseY = (int) my;

        int direction = verticalAmount > 0 ? -1 : 1;

        int steps = Math.max(
            1,
            Math.min(
                6,
                (int) Math.ceil(Math.abs(verticalAmount))
            )
        );

        for (int i = 0; i < steps; i++) {
            scrollHovered(direction);
        }

        return true;
    }

    private void scrollHovered(int direction) {

        if (expandedModule != null) {

            if (expandedModule.getName()
                    .equalsIgnoreCase("Mob ESP")) {

                int max = Math.max(
                    0,
                    MobEspSettings.MOBS.length - 12
                );

                mobScroll = Math.max(
                    0,
                    Math.min(
                        max,
                        mobScroll + direction * 3
                    )
                );
            }

            return;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {

            Panel panel = panels.get(i);

            if (panel.collapsed) {
                continue;
            }

            if (!inside(
                mouseX,
                mouseY,
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

            int maxScroll = Math.max(
                0,
                count - visibleRows()
            );

            panel.scroll = Math.max(
                0,
                Math.min(
                    maxScroll,
                    panel.scroll + direction
                )
            );

            return;
        }
    }

    private int visibleRows() {
        return (PANEL_H - HEADER_H - 22) / ROW_H;
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

    private int settingsWidth() {
        return Math.min(SETTINGS_W, width - 20);
    }

    private int settingsX() {
        return (width - settingsWidth()) / 2;
    }

    private int settingsY() {
        return Math.max(
            45,
            (height - SETTINGS_H) / 2
        );
    }

    private int sliderX() {
        return settingsX() + 18;
    }

    private int sliderWidth() {
        return settingsWidth() - 36;
    }

    private int sliderY() {
        return settingsY() + 115;
    }

    private void updateTracerSlider(int mx) {

        float progress = (float) (
            (mx - sliderX())
                / (double) sliderWidth()
        );

        progress = Math.max(
            0.0F,
            Math.min(1.0F, progress)
        );

        float value = 1.0F + progress * 9.0F;

        value = Math.round(value * 2.0F) / 2.0F;

        EspOverlaySettings.setTracerThickness(value);
    }

    private void handleClick(
        int mx,
        int my,
        boolean rightClick
    ) {

        if (expandedModule != null) {
            handleSettingsClick(mx, my, rightClick);
            return;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {

            Panel panel = panels.get(i);

            if (inside(
                mx,
                my,
                panel.x,
                panel.y,
                PANEL_W,
                HEADER_H
            )) {

                if (rightClick) {

                    panel.collapsed = !panel.collapsed;

                } else {

                    dragging = panel;

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
                mx,
                my,
                panel.x,
                panel.y + HEADER_H,
                PANEL_W,
                PANEL_H - HEADER_H - 18
            )) {
                continue;
            }

            List<Module> modules =
                NovaClient.MODULES.getByCategory(
                    panel.category
                );

            int relativeY =
                my - panel.y - HEADER_H - 5;

            if (relativeY < 0) {
                return;
            }

            int row = relativeY / ROW_H;

            if (row < 0 || row >= visibleRows()) {
                return;
            }

            int index = panel.scroll + row;

            if (index < 0 || index >= modules.size()) {
                return;
            }

            Module module = modules.get(index);

            if (rightClick) {

                // ENTITY RADAR SETTINGS
                if (module.getName().equalsIgnoreCase(
                    "Entity Radar"
                )) {

                    Minecraft.getInstance().setScreen(
                        new RadarSettingsScreen(this)
                    );

                    return;
                }

                // ESP SETTINGS
                if (module.getName().equalsIgnoreCase(
                    "Mob ESP"
                ) || module.getName().equalsIgnoreCase(
                    "Player ESP"
                )) {

                    expandedModule = module;
                    mobScroll = 0;
                }

            } else {

                NovaClient.MODULES.toggleModule(module);
                NovaClient.CONFIG.save();
            }

            return;
        }
    }

    private void handleSettingsClick(
        int mx,
        int my,
        boolean rightClick
    ) {

        int x = settingsX();
        int y = settingsY();
        int w = settingsWidth();

        if (rightClick || inside(
            mx, my,
            x + 8, y + 7,
            55, 19
        )) {

            expandedModule = null;
            draggingSlider = false;
            return;
        }

        int buttonW = (w - 28) / 3;

        if (inside(
            mx, my,
            x + 8, y + 50,
            buttonW, 22
        )) {

            EspOverlaySettings.toggleNames();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            x + 12 + buttonW, y + 50,
            buttonW, 22
        )) {

            EspOverlaySettings.toggleHealth();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            x + 16 + buttonW * 2, y + 50,
            buttonW, 22
        )) {

            EspOverlaySettings.toggleTracers();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            sliderX() - 5,
            sliderY() - 8,
            sliderWidth() + 10,
            20
        )) {

            draggingSlider = true;
            updateTracerSlider(mx);
            return;
        }

        boolean mobSettings =
            expandedModule.getName().equalsIgnoreCase(
                "Mob ESP"
            );

        if (!mobSettings) {
            return;
        }

        if (inside(
            mx, my,
            x + w - 104, y + 7,
            43, 19
        )) {

            MobEspSettings.selectAll();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            x + w - 56, y + 7,
            47, 19
        )) {

            MobEspSettings.selectNone();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            x + 10, y + SETTINGS_H - 28,
            60, 19
        )) {

            mobScroll = Math.max(
                0,
                mobScroll - 6
            );

            return;
        }

        if (inside(
            mx, my,
            x + w - 70, y + SETTINGS_H - 28,
            60, 19
        )) {

            mobScroll = Math.min(
                Math.max(
                    0,
                    MobEspSettings.MOBS.length - 12
                ),
                mobScroll + 6
            );

            return;
        }

        int cellW = (w - 30) / 2;

        for (int i = 0; i < 12; i++) {

            int index = mobScroll + i;

            if (index >= MobEspSettings.MOBS.length) {
                break;
            }

            int column = i % 2;
            int row = i / 2;

            int cx =
                x + 10 + column * (cellW + 10);

            int cy =
                y + 145 + row * 31;

            if (inside(
                mx, my,
                cx, cy,
                cellW, 25
            )) {

                MobEspSettings.toggle(
                    MobEspSettings.MOBS[index]
                );

                NovaClient.CONFIG.save();
                return;
            }
        }
    }

    private void rect(
        GuiGraphics g,
        int x,
        int y,
        int w,
        int h,
        int color
    ) {

        g.fill(
            x,
            y,
            x + w,
            y + h,
            color
        );
    }

    private void outline(
        GuiGraphics g,
        int x,
        int y,
        int w,
        int h,
        int color
    ) {

        rect(g, x, y, w, 1, color);
        rect(g, x, y + h - 1, w, 1, color);
        rect(g, x, y, 1, h, color);
        rect(g, x + w - 1, y, 1, h, color);
    }

    private void text(
        GuiGraphics g,
        String value,
        int x,
        int y,
        int color
    ) {

        g.drawString(
            font,
            value,
            x,
            y,
            color,
            false
        );
    }

    private void button(
        GuiGraphics g,
        int x,
        int y,
        int w,
        int h,
        String label,
        boolean active
    ) {

        rect(
            g, x, y, w, h,
            active ? ACTIVE : ROW
        );

        outline(
            g, x, y, w, h,
            active ? PURPLE : BORDER
        );

        g.drawCenteredString(
            font,
            label,
            x + w / 2,
            y + (h - 8) / 2,
            active ? WHITE : MUTED
        );
    }

    @Override
    public void render(
        GuiGraphics g,
        int mx,
        int my,
        float delta
    ) {

        rect(g, 0, 0, width, height, BG);

        rect(
            g,
            0, 0,
            width, 40,
            0xFF171020
        );

        text(
            g,
            "NOVA NEBULA v3",
            16,
            14,
            WHITE
        );

        text(
            g,
            "LEFT CLICK: TOGGLE   RIGHT CLICK: SETTINGS",
            145,
            14,
            MUTED
        );

        int enabled = 0;

        for (Module module : NovaClient.MODULES.getModules()) {
            if (module.isEnabled()) {
                enabled++;
            }
        }

        String status =
            enabled + " ACTIVE / "
                + NovaClient.MODULES.getModules().size()
                + " MODULES";

        text(
            g,
            status,
            Math.max(10, width - font.width(status) - 16),
            14,
            PURPLE
        );

        for (Panel panel : panels) {
            drawPanel(g, panel, mx, my);
        }

        if (expandedModule != null) {
            drawEspSettings(g);
        }
    }

    private void drawPanel(
        GuiGraphics g,
        Panel panel,
        int mx,
        int my
    ) {

        int x = panel.x;
        int y = panel.y;

        int h = panel.collapsed
            ? HEADER_H
            : PANEL_H;

        rect(
            g,
            x, y,
            PANEL_W, h,
            PANEL
        );

        outline(
            g,
            x, y,
            PANEL_W, h,
            BORDER
        );

        rect(
            g,
            x + 1, y + 1,
            PANEL_W - 2,
            HEADER_H - 2,
            HEADER
        );

        text(
            g,
            panel.category.name(),
            x + 8,
            y + 8,
            WHITE
        );

        text(
            g,
            panel.collapsed ? "+" : "-",
            x + PANEL_W - 13,
            y + 8,
            PURPLE
        );

        if (panel.collapsed) {
            return;
        }

        List<Module> modules =
            NovaClient.MODULES.getByCategory(
                panel.category
            );

        int maxScroll = Math.max(
            0,
            modules.size() - visibleRows()
        );

        panel.scroll = Math.max(
            0,
            Math.min(maxScroll, panel.scroll)
        );

        for (int row = 0; row < visibleRows(); row++) {

            int index = panel.scroll + row;

            if (index >= modules.size()) {
                break;
            }

            Module module = modules.get(index);

            int ry =
                y + HEADER_H + 5 + row * ROW_H;

            boolean hover = inside(
                mx, my,
                x + 3, ry,
                PANEL_W - 6,
                ROW_H - 1
            );

            rect(
                g,
                x + 3,
                ry,
                PANEL_W - 6,
                ROW_H - 1,
                module.isEnabled()
                    ? ACTIVE
                    : hover
                        ? 0xFF302044
                        : ROW
            );

            if (module.isEnabled()) {

                rect(
                    g,
                    x + 3, ry,
                    2, ROW_H - 1,
                    PURPLE
                );
            }

            String name = module.getName();

            while (
                name.length() > 1
                && font.width(name) > PANEL_W - 28
            ) {

                name = name.substring(
                    0,
                    name.length() - 1
                );
            }

            text(
                g,
                name,
                x + 9,
                ry + 5,
                module.isEnabled()
                    ? WHITE
                    : MUTED
            );

            rect(
                g,
                x + PANEL_W - 13,
                ry + 6,
                5, 5,
                module.isEnabled()
                    ? PURPLE
                    : 0xFF484052
            );
        }

        rect(
            g,
            x + 1,
            y + PANEL_H - 18,
            PANEL_W - 2,
            17,
            0xFF1C142B
        );

        text(
            g,
            modules.size() + " MODULES",
            x + 8,
            y + PANEL_H - 14,
            MUTED
        );

        if (maxScroll > 0) {

            int trackX = x + PANEL_W - 4;
            int trackY = y + HEADER_H + 5;
            int trackH = visibleRows() * ROW_H;

            int thumbH = Math.max(
                14,
                trackH * visibleRows()
                    / modules.size()
            );

            int thumbY =
                trackY
                + (trackH - thumbH)
                    * panel.scroll / maxScroll;

            rect(
                g,
                trackX, trackY,
                2, trackH,
                0xFF38284D
            );

            rect(
                g,
                trackX, thumbY,
                2, thumbH,
                PURPLE
            );
        }
    }

    private void drawEspSettings(GuiGraphics g) {

        int x = settingsX();
        int y = settingsY();
        int w = settingsWidth();

        boolean mobSettings =
            expandedModule.getName().equalsIgnoreCase(
                "Mob ESP"
            );

        rect(
            g,
            0, 40,
            width, height - 40,
            0xB0000000
        );

        rect(
            g,
            x, y,
            w, SETTINGS_H,
            PANEL
        );

        outline(
            g,
            x, y,
            w, SETTINGS_H,
            BORDER
        );

        rect(
            g,
            x + 1, y + 1,
            w - 2, 32,
            HEADER
        );

        button(
            g,
            x + 8, y + 7,
            55, 19,
            "BACK",
            false
        );

        if (mobSettings) {

            button(
                g,
                x + w - 104, y + 7,
                43, 19,
                "ALL",
                false
            );

            button(
                g,
                x + w - 56, y + 7,
                47, 19,
                "NONE",
                false
            );
        }

        text(
            g,
            mobSettings
                ? "MOB ESP SETTINGS"
                : "PLAYER ESP SETTINGS",
            x + 10,
            y + 36,
            PURPLE
        );

        int buttonW = (w - 28) / 3;

        button(
            g,
            x + 8, y + 50,
            buttonW, 22,
            "Names: " + (
                EspOverlaySettings.showNames()
                    ? "ON" : "OFF"
            ),
            EspOverlaySettings.showNames()
        );

        button(
            g,
            x + 12 + buttonW, y + 50,
            buttonW, 22,
            "HP: " + (
                EspOverlaySettings.showHealth()
                    ? "ON" : "OFF"
            ),
            EspOverlaySettings.showHealth()
        );

        button(
            g,
            x + 16 + buttonW * 2, y + 50,
            buttonW, 22,
            "Tracers: " + (
                EspOverlaySettings.showTracers()
                    ? "ON" : "OFF"
            ),
            EspOverlaySettings.showTracers()
        );

        String thicknessText = String.format(
            Locale.ROOT,
            "Tracer Thickness: %.1f",
            EspOverlaySettings.getTracerThickness()
        );

        text(
            g,
            thicknessText,
            x + 18,
            y + 91,
            WHITE
        );

        int sx = sliderX();
        int sy = sliderY();
        int sw = sliderWidth();

        rect(
            g,
            sx, sy,
            sw, 6,
            0xFF393048
        );

        float progress =
            (EspOverlaySettings.getTracerThickness()
                - 1.0F) / 9.0F;

        int fillWidth = Math.round(sw * progress);

        rect(
            g,
            sx, sy,
            fillWidth, 6,
            PURPLE
        );

        rect(
            g,
            sx + fillWidth - 4,
            sy - 4,
            8, 14,
            WHITE
        );

        text(
            g,
            "1.0",
            sx,
            sy + 13,
            MUTED
        );

        text(
            g,
            "10.0",
            sx + sw - 25,
            sy + 13,
            MUTED
        );

        if (!mobSettings) {

            text(
                g,
                "These settings also apply to Mob ESP.",
                x + 18,
                y + 155,
                MUTED
            );

            return;
        }

        int cellW = (w - 30) / 2;

        for (int i = 0; i < 12; i++) {

            int index = mobScroll + i;

            if (index >= MobEspSettings.MOBS.length) {
                break;
            }

            String id = MobEspSettings.MOBS[index];

            int column = i % 2;
            int row = i / 2;

            int cx =
                x + 10 + column * (cellW + 10);

            int cy =
                y + 145 + row * 31;

            boolean selected =
                MobEspSettings.isSelected(id);

            rect(
                g,
                cx, cy,
                cellW, 25,
                selected ? ACTIVE : ROW
            );

            outline(
                g,
                cx, cy,
                cellW, 25,
                selected ? PURPLE : BORDER
            );

            text(
                g,
                (selected ? "[x] " : "[ ] ")
                    + id.replace('_', ' '),
                cx + 6,
                cy + 8,
                selected ? WHITE : MUTED
            );
        }

        button(
            g,
            x + 10,
            y + SETTINGS_H - 28,
            60, 19,
            "PREV",
            false
        );

        button(
            g,
            x + w - 70,
            y + SETTINGS_H - 28,
            60, 19,
            "NEXT",
            false
        );

        g.drawCenteredString(
            font,
            "Selected: "
                + MobEspSettings.getSelected().size(),
            x + w / 2,
            y + SETTINGS_H - 22,
            PURPLE
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
