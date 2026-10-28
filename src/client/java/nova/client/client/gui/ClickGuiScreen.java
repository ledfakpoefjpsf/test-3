
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

import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {

    // ========================================
    // NOVA NEBULA COLORS
    // ========================================

    private static final int BACKGROUND = 0xC8080711;
    private static final int PANEL_BG = 0xF013101E;
    private static final int PANEL_BORDER = 0xFF62428B;
    private static final int HEADER = 0xFF422663;
    private static final int HEADER_TOP = 0xFF8450D1;
    private static final int ROW = 0xFF1B1728;
    private static final int ROW_HOVER = 0xFF302044;
    private static final int ROW_ACTIVE = 0xFF39244F;
    private static final int PURPLE = 0xFFBC91FF;
    private static final int WHITE = 0xFFF4EEFF;
    private static final int MUTED = 0xFF998BAE;
    private static final int GREEN = 0xFFBA8CFF;

    private static final int PANEL_WIDTH = 125;
    private static final int PANEL_HEIGHT = 215;
    private static final int HEADER_HEIGHT = 25;
    private static final int ROW_HEIGHT = 19;
    private static final int GAP = 9;

    private static final long WAVE_STEP = 85L;
    private static final long WAVE_LENGTH = 360L;

    private final List<Panel> panels = new ArrayList<>();

    private long openedAt;

    private boolean lastLeft;
    private boolean lastRight;
    private boolean lastPageUp;
    private boolean lastPageDown;

    private Panel dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    private Module expandedModule;
    private int mobScroll;

    private int mouseX;
    private int mouseY;

    private static class Panel {

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
        super(Component.literal("Nova Nebula"));
    }

    // ========================================
    // INITIALIZATION
    // ========================================

    @Override
    protected void init() {

        openedAt = System.currentTimeMillis();

        if (panels.isEmpty()) {

            Category[] categories = Category.values();

            int columns = width >= 850 ? 6 : 3;

            int totalWidth =
                columns * PANEL_WIDTH
                + (columns - 1) * GAP;

            int startX = Math.max(
                8,
                (width - totalWidth) / 2
            );

            for (int i = 0; i < categories.length; i++) {

                int column = i % columns;
                int row = i / columns;

                panels.add(
                    new Panel(
                        categories[i],
                        startX + column
                            * (PANEL_WIDTH + GAP),
                        57 + row
                            * (PANEL_HEIGHT + GAP)
                    )
                );
            }
        }
    }

    // ========================================
    // MOUSE POLLING
    // ========================================

    @Override
    public void tick() {

        Minecraft mc = Minecraft.getInstance();

        if (mc.getWindow() == null) {
            return;
        }

        long handle = mc.getWindow().handle();

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
            handle,
            GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        boolean right = GLFW.glfwGetMouseButton(
            handle,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) == GLFW.GLFW_PRESS;

        boolean pageUp = GLFW.glfwGetKey(
            handle,
            GLFW.GLFW_KEY_PAGE_UP
        ) == GLFW.GLFW_PRESS;

        boolean pageDown = GLFW.glfwGetKey(
            handle,
            GLFW.GLFW_KEY_PAGE_DOWN
        ) == GLFW.GLFW_PRESS;

        if (left && !lastLeft) {
            handleClick(mouseX, mouseY, false);
        }

        if (right && !lastRight) {
            handleClick(mouseX, mouseY, true);
        }

        if (dragging != null && left) {

            dragging.x = mouseX - dragOffsetX;
            dragging.y = mouseY - dragOffsetY;

            dragging.x = Math.max(
                0,
                Math.min(
                    width - PANEL_WIDTH,
                    dragging.x
                )
            );

            dragging.y = Math.max(
                43,
                Math.min(
                    height - HEADER_HEIGHT,
                    dragging.y
                )
            );
        }

        if (!left) {
            dragging = null;
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

    // ========================================
    // CLICK HANDLING
    // ========================================

    private void handleClick(
        int mx,
        int my,
        boolean rightClick
    ) {

        // Replay wave animation

        if (!rightClick
            && inside(
                mx, my,
                width - 84, 12, 72, 19
            )) {

            openedAt = System.currentTimeMillis();
            return;
        }

        if (expandedModule != null) {
            handleSettingsClick(mx, my, rightClick);
            return;
        }

        // Check panels from front to back

        for (int i = panels.size() - 1; i >= 0; i--) {

            Panel panel = panels.get(i);

            if (!isPanelVisible(i)) {
                continue;
            }

            int x = panel.x;
            int y = panel.y;

            // Category header

            if (inside(
                mx, my,
                x, y,
                PANEL_WIDTH,
                HEADER_HEIGHT
            )) {

                if (rightClick) {

                    panel.collapsed = !panel.collapsed;

                } else {

                    dragging = panel;

                    dragOffsetX = mx - panel.x;
                    dragOffsetY = my - panel.y;

                    // Bring selected panel to front

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
                x,
                y + HEADER_HEIGHT,
                PANEL_WIDTH,
                PANEL_HEIGHT - HEADER_HEIGHT
            )) {
                continue;
            }

            List<Module> modules =
                NovaClient.MODULES.getByCategory(
                    panel.category
                );

            int relativeY =
                my - y - HEADER_HEIGHT - 5;

            if (relativeY < 0) {
                return;
            }

            int row = relativeY / ROW_HEIGHT;

            if (row >= visibleRows()) {
                return;
            }

            int index = panel.scroll + row;

            if (index < 0 || index >= modules.size()) {
                return;
            }

            Module module = modules.get(index);

            if (rightClick) {

                // Open Mob ESP settings

                if (module.getName()
                    .equalsIgnoreCase("Mob ESP")) {

                    expandedModule = module;
                    mobScroll = 0;
                }

            } else {

                // Toggle module and show notifications

                NovaClient.MODULES.toggleModule(module);

                // Save enabled states
                NovaClient.CONFIG.save();
            }

            return;
        }
    }

    // ========================================
    // SCROLLING
    // ========================================

    private void scrollHovered(int direction) {

        if (expandedModule != null) {

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

            return;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {

            Panel panel = panels.get(i);

            if (!inside(
                mouseX, mouseY,
                panel.x, panel.y,
                PANEL_WIDTH, PANEL_HEIGHT
            )) {
                continue;
            }

            int count =
                NovaClient.MODULES
                    .getByCategory(panel.category)
                    .size();

            int max = Math.max(
                0,
                count - visibleRows()
            );

            panel.scroll = Math.max(
                0,
                Math.min(
                    max,
                    panel.scroll + direction
                )
            );

            return;
        }
    }

    private int visibleRows() {
        return (PANEL_HEIGHT - HEADER_HEIGHT - 9)
            / ROW_HEIGHT;
    }

    // ========================================
    // MOB ESP SETTINGS
    // ========================================

    private void handleSettingsClick(
        int mx,
        int my,
        boolean rightClick
    ) {

        int w = Math.min(370, width - 20);
        int h = 295;

        int x = (width - w) / 2;
        int y = Math.max(
            45,
            (height - h) / 2
        );

        if (rightClick
            || inside(
                mx, my,
                x + 8, y + 7,
                55, 19
            )) {

            expandedModule = null;
            return;
        }

        if (inside(
            mx, my,
            x + w - 104,
            y + 7,
            43, 19
        )) {

            MobEspSettings.selectAll();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            x + w - 56,
            y + 7,
            47, 19
        )) {

            MobEspSettings.selectNone();
            NovaClient.CONFIG.save();
            return;
        }

        if (inside(
            mx, my,
            x + 10,
            y + h - 28,
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
            x + w - 70,
            y + h - 28,
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

            int cellX =
                x + 10 + column * (cellW + 10);

            int cellY =
                y + 46 + row * 32;

            if (inside(
                mx, my,
                cellX, cellY,
                cellW, 26
            )) {

                MobEspSettings.toggle(
                    MobEspSettings.MOBS[index]
                );

                NovaClient.CONFIG.save();
                return;
            }
        }
    }

    // ========================================
    // DRAW HELPERS
    // ========================================

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

    private void rectangle(
        GuiGraphics g,
        int x,
        int y,
        int w,
        int h,
        int color
    ) {

        g.fill(
            x, y,
            x + w, y + h,
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

        rectangle(g, x, y, w, 1, color);
        rectangle(g, x, y + h - 1, w, 1, color);
        rectangle(g, x, y, 1, h, color);
        rectangle(g, x + w - 1, y, 1, h, color);
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
            x, y,
            color,
            false
        );
    }

    private boolean isPanelVisible(int index) {

        long elapsed =
            System.currentTimeMillis() - openedAt;

        return elapsed >= index * WAVE_STEP;
    }

    // ========================================
    // RENDER MAIN GUI
    // ========================================

    @Override
    public void render(
        GuiGraphics g,
        int mx,
        int my,
        float delta
    ) {

        // Do not call renderBackground().
        // Prevents Minecraft's double-blur crash.

        rectangle(
            g,
            0, 0,
            width, height,
            BACKGROUND
        );

        rectangle(
            g,
            0, 0,
            width, 40,
            0xEE100B1D
        );

        rectangle(
            g,
            0, 39,
            width, 1,
            PURPLE
        );

        text(
            g,
            "NOVA NEBULA",
            13, 9,
            PURPLE
        );

        text(
            g,
            "FABRIC 1.21.11",
            13, 23,
            MUTED
        );

        drawSmallButton(
            g,
            width - 84,
            12,
            72,
            19,
            "REPLAY",
            inside(
                mx, my,
                width - 84, 12, 72, 19
            )
        );

        for (int i = 0; i < panels.size(); i++) {

            if (!isPanelVisible(i)) {
                continue;
            }

            drawPanel(
                g,
                panels.get(i),
                i,
                mx,
                my
            );
        }

        if (expandedModule != null) {
            drawMobSettings(g, mx, my);
        }

        text(
            g,
            "LMB Toggle  |  RMB Settings  |  Drag Headers",
            10,
            height - 15,
            MUTED
        );
    }

    // ========================================
    // DRAW CATEGORY PANEL
    // ========================================

    private void drawPanel(
        GuiGraphics g,
        Panel panel,
        int index,
        int mx,
        int my
    ) {

        long elapsed =
            System.currentTimeMillis() - openedAt;

        double progress = Math.max(
            0,
            Math.min(
                1,
                (elapsed - index * WAVE_STEP)
                    / (double) WAVE_LENGTH
            )
        );

        double eased =
            1 - Math.pow(1 - progress, 3);

        int waveOffset =
            (int) ((1 - eased) * 35);

        g.pose().pushMatrix();

        g.pose().translate(
            0,
            waveOffset
        );

        int x = panel.x;
        int y = panel.y;

        int h = panel.collapsed
            ? HEADER_HEIGHT
            : PANEL_HEIGHT;

        // Panel shadow

        rectangle(
            g,
            x + 3,
            y + 4,
            PANEL_WIDTH,
            h,
            0x55000000
        );

        // Panel background

        rectangle(
            g,
            x, y,
            PANEL_WIDTH, h,
            PANEL_BG
        );

        outline(
            g,
            x, y,
            PANEL_WIDTH, h,
            PANEL_BORDER
        );

        // Header

        rectangle(
            g,
            x + 1,
            y + 1,
            PANEL_WIDTH - 2,
            HEADER_HEIGHT - 2,
            HEADER
        );

        rectangle(
            g,
            x + 1,
            y + 1,
            PANEL_WIDTH - 2,
            2,
            HEADER_TOP
        );

        text(
            g,
            panel.category.getDisplayName()
                .toUpperCase(),
            x + 8,
            y + 9,
            WHITE
        );

        text(
            g,
            panel.collapsed ? "+" : "-",
            x + PANEL_WIDTH - 14,
            y + 9,
            PURPLE
        );

        if (!panel.collapsed) {

            List<Module> modules =
                NovaClient.MODULES.getByCategory(
                    panel.category
                );

            int rows = visibleRows();

            for (int row = 0; row < rows; row++) {

                int moduleIndex =
                    panel.scroll + row;

                if (moduleIndex >= modules.size()) {
                    break;
                }

                Module module =
                    modules.get(moduleIndex);

                int ry =
                    y + HEADER_HEIGHT + 5
                    + row * ROW_HEIGHT;

                boolean hover = inside(
                    mx, my,
                    x + 3, ry,
                    PANEL_WIDTH - 6,
                    ROW_HEIGHT - 1
                );

                int bg = module.isEnabled()
                    ? ROW_ACTIVE
                    : hover ? ROW_HOVER : ROW;

                rectangle(
                    g,
                    x + 3, ry,
                    PANEL_WIDTH - 6,
                    ROW_HEIGHT - 1,
                    bg
                );

                if (module.isEnabled()) {

                    rectangle(
                        g,
                        x + 3, ry,
                        2,
                        ROW_HEIGHT - 1,
                        PURPLE
                    );
                }

                String name = module.getName();

                int maxTextWidth =
                    PANEL_WIDTH - 28;

                if (font.width(name) > maxTextWidth) {

                    while (name.length() > 1
                        && font.width(name + "..")
                            > maxTextWidth) {

                        name = name.substring(
                            0,
                            name.length() - 1
                        );
                    }

                    name += "..";
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

                // Enabled indicator

                rectangle(
                    g,
                    x + PANEL_WIDTH - 13,
                    ry + 6,
                    5, 5,
                    module.isEnabled()
                        ? GREEN
                        : 0xFF484052
                );
            }

            // Footer

            rectangle(
                g,
                x + 1,
                y + PANEL_HEIGHT - 18,
                PANEL_WIDTH - 2,
                17,
                0xFF1C142B
            );

            text(
                g,
                modules.size() + " MODULES",
                x + 8,
                y + PANEL_HEIGHT - 14,
                MUTED
            );

            if (modules.size() > rows) {

                text(
                    g,
                    "^ v",
                    x + PANEL_WIDTH - 29,
                    y + PANEL_HEIGHT - 14,
                    PURPLE
                );
            }
        }

        g.pose().popMatrix();
    }

    // ========================================
    // SMALL BUTTON DRAWING
    // ========================================

    private void drawSmallButton(
        GuiGraphics g,
        int x,
        int y,
        int w,
        int h,
        String label,
        boolean hover
    ) {

        rectangle(
            g,
            x, y,
            w, h,
            hover ? ROW_HOVER : ROW
        );

        outline(
            g,
            x, y,
            w, h,
            PANEL_BORDER
        );

        g.drawCenteredString(
            font,
            label,
            x + w / 2,
            y + (h - 8) / 2,
            WHITE
        );
    }

    // ========================================
    // MOB ESP SETTINGS WINDOW
    // ========================================

    private void drawMobSettings(
        GuiGraphics g,
        int mx,
        int my
    ) {

        int w = Math.min(370, width - 20);
        int h = 295;

        int x = (width - w) / 2;
        int y = Math.max(
            45,
            (height - h) / 2
        );

        // Darken background

        rectangle(
            g,
            0, 40,
            width,
            height - 40,
            0xB0000000
        );

        // Settings panel

        rectangle(
            g,
            x, y,
            w, h,
            PANEL_BG
        );

        outline(
            g,
            x, y,
            w, h,
            PANEL_BORDER
        );

        rectangle(
            g,
            x + 1,
            y + 1,
            w - 2,
            32,
            HEADER
        );

        text(
            g,
            "MOB ESP SETTINGS",
            x + 10,
            y + 36,
            PURPLE
        );

        drawSmallButton(
            g,
            x + 8,
            y + 7,
            55,
            19,
            "BACK",
            false
        );

        drawSmallButton(
            g,
            x + w - 104,
            y + 7,
            43,
            19,
            "ALL",
            false
        );

        drawSmallButton(
            g,
            x + w - 56,
            y + 7,
            47,
            19,
            "NONE",
            false
        );

        int cellW = (w - 30) / 2;

        for (int i = 0; i < 12; i++) {

            int index = mobScroll + i;

            if (index >= MobEspSettings.MOBS.length) {
                break;
            }

            String id =
                MobEspSettings.MOBS[index];

            int column = i % 2;
            int row = i / 2;

            int cx =
                x + 10
                + column * (cellW + 10);

            int cy =
                y + 46
                + row * 32;

            boolean selected =
                MobEspSettings.isSelected(id);

            rectangle(
                g,
                cx, cy,
                cellW, 26,
                selected
                    ? ROW_ACTIVE
                    : ROW
            );

            outline(
                g,
                cx, cy,
                cellW, 26,
                selected
                    ? PURPLE
                    : PANEL_BORDER
            );

            String label =
                id.replace('_', ' ');

            text(
                g,
                (selected ? "[x] " : "[ ] ")
                    + label,
                cx + 6,
                cy + 9,
                selected
                    ? WHITE
                    : MUTED
            );
        }

        drawSmallButton(
            g,
            x + 10,
            y + h - 28,
            60,
            19,
            "PREV",
            false
        );

        drawSmallButton(
            g,
            x + w - 70,
            y + h - 28,
            60,
            19,
            "NEXT",
            false
        );

        g.drawCenteredString(
            font,
            "Selected: "
                + MobEspSettings.getSelected().size(),
            x + w / 2,
            y + h - 22,
            PURPLE
        );
    }

    // ========================================
    // SCREEN SETTINGS
    // ========================================

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
