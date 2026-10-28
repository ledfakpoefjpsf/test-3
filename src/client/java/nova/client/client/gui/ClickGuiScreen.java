
package nova.client.client.gui;

// @name: ClickGuiScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/ClickGuiScreen.java

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.NovaClient;
import nova.client.client.module.Category;
import nova.client.client.module.Module;
import nova.client.client.module.MobEspSettings;

public class ClickGuiScreen extends Screen {

    private static final int BG = 0xE90D0A18;
    private static final int PANEL = 0xF01A1428;
    private static final int PANEL_BORDER = 0xFF65458F;
    private static final int PURPLE = 0xFFB78AFF;
    private static final int WHITE = 0xFFF3ECFF;
    private static final int MUTED = 0xFF9588AC;
    private static final int ACTIVE = 0xFF513179;

    private static final long WAVE_STEP_MS = 85;
    private static final long WAVE_DURATION_MS = 380;

    private long openedAt;
    private int builtPanels;

    private boolean settingsOpen;
    private int mobPage;

    private final boolean[] collapsed =
        new boolean[Category.values().length];

    public ClickGuiScreen() {
        super(Component.literal("Nova Nebula"));
    }

    private int columns() {
        return width >= 790 ? 6 : 3;
    }

    private int panelWidth() {
        if (columns() == 6) {
            return Math.max(
                108,
                Math.min(150, (width - 40) / 6 - 8)
            );
        }

        return Math.max(
            105,
            Math.min(185, (width - 30) / 3 - 8)
        );
    }

    private int gap() {
        return 8;
    }

    private int boardWidth() {
        return columns() * panelWidth()
            + (columns() - 1) * gap();
    }

    private int startX() {
        return (width - boardWidth()) / 2;
    }

    private int panelHeight() {
        if (columns() == 6) {
            return Math.min(245, height - 105);
        }

        return Math.min(
            175,
            (height - 100) / 2
        );
    }

    private int startY() {
        return 55;
    }

    private int px(int i) {
        return startX()
            + (i % columns()) * (panelWidth() + gap());
    }

    private int py(int i) {
        return startY()
            + (i / columns()) * (panelHeight() + 9);
    }

    // =========================================
    // INITIALIZE GUI
    // =========================================

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        builtPanels = -1;
        settingsOpen = false;

        rebuild();
    }

    // =========================================
    // WAVE OPEN ANIMATION
    // =========================================

    @Override
    public void tick() {

        int count = (int) Math.min(
            Category.values().length,
            Math.max(
                0,
                (System.currentTimeMillis()
                    - openedAt
                    - WAVE_DURATION_MS)
                    / WAVE_STEP_MS + 1
            )
        );

        if (count != builtPanels) {
            builtPanels = count;
            rebuild();
        }
    }

    // =========================================
    // BUILD GUI
    // =========================================

    private void rebuild() {

        clearWidgets();

        int right = width - 12;

        addRenderableWidget(
            Button.builder(
                Component.literal("Wave"),
                b -> {
                    openedAt = System.currentTimeMillis();
                    builtPanels = -1;
                    rebuild();
                }
            ).bounds(
                right - 40,
                13,
                40,
                19
            ).build()
        );

        if (settingsOpen) {
            buildMobSettings();
            return;
        }

        Category[] categories = Category.values();

        for (int i = 0; i < categories.length; i++) {

            if (i >= builtPanels) {
                continue;
            }

            final int index = i;

            int x = px(i);
            int y = py(i);
            int w = panelWidth();

            // Category collapse button

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        (collapsed[i] ? "+ " : "- ")
                            + categories[i]
                                .getDisplayName()
                                .toUpperCase()
                    ),
                    b -> {
                        collapsed[index] = !collapsed[index];
                        rebuild();
                    }
                ).bounds(
                    x + 3,
                    y + 3,
                    w - 6,
                    19
                ).build()
            );

            if (collapsed[i]) {
                continue;
            }

            List<Module> modules =
                NovaClient.MODULES.getByCategory(
                    categories[i]
                );

            int visible = 0;

            int maxRows = Math.max(
                1,
                (panelHeight() - 35) / 23
            );

            for (Module module : modules) {

                if (visible >= maxRows) {
                    break;
                }

                int rowY = y + 29 + visible * 23;

                // Module toggle

                addRenderableWidget(
                    Button.builder(
                        Component.literal(
                            (module.isEnabled()
                                ? "● "
                                : "○ ")
                                + module.getName()
                        ),
                        b -> {
                            module.toggle();

                            NovaClient.CONFIG.save();

                            rebuild();
                        }
                    ).bounds(
                        x + 5,
                        rowY,
                        w - 10,
                        20
                    ).build()
                );

                // Mob ESP settings

                if (module.getName()
                    .equalsIgnoreCase("Mob ESP")) {

                    addRenderableWidget(
                        Button.builder(
                            Component.literal("⚙"),
                            b -> {
                                settingsOpen = true;
                                mobPage = 0;
                                rebuild();
                            }
                        ).bounds(
                            x + w - 28,
                            rowY + 1,
                            20,
                            18
                        ).build()
                    );
                }

                visible++;
            }
        }
    }

    // =========================================
    // MOB ESP SETTINGS
    // =========================================

    private void buildMobSettings() {

        int w = Math.min(420, width - 30);

        int left = (width - w) / 2;

        int top = Math.max(
            45,
            (height - 275) / 2
        );

        // Back button

        addRenderableWidget(
            Button.builder(
                Component.literal("< Back to Nebula"),
                b -> {
                    settingsOpen = false;
                    rebuild();
                }
            ).bounds(
                left + 10,
                top + 10,
                135,
                21
            ).build()
        );

        // Select all mobs

        addRenderableWidget(
            Button.builder(
                Component.literal("All"),
                b -> {
                    MobEspSettings.selectAll();
                    NovaClient.CONFIG.save();
                    rebuild();
                }
            ).bounds(
                left + w - 105,
                top + 10,
                43,
                21
            ).build()
        );

        // Deselect all mobs

        addRenderableWidget(
            Button.builder(
                Component.literal("None"),
                b -> {
                    MobEspSettings.selectNone();
                    NovaClient.CONFIG.save();
                    rebuild();
                }
            ).bounds(
                left + w - 57,
                top + 10,
                47,
                21
            ).build()
        );

        int perPage = 12;

        int start = mobPage * perPage;

        int end = Math.min(
            MobEspSettings.MOBS.length,
            start + perPage
        );

        int itemW = (w - 32) / 2;

        for (int i = start; i < end; i++) {

            final String id = MobEspSettings.MOBS[i];

            int column = (i - start) % 2;
            int row = (i - start) / 2;

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        (MobEspSettings.isSelected(id)
                            ? "● "
                            : "○ ")
                            + id.replace('_', ' ')
                    ),
                    b -> {
                        MobEspSettings.toggle(id);
                        NovaClient.CONFIG.save();
                        rebuild();
                    }
                ).bounds(
                    left + 10
                        + column * (itemW + 12),
                    top + 46 + row * 28,
                    itemW,
                    23
                ).build()
            );
        }

        // Previous page

        addRenderableWidget(
            Button.builder(
                Component.literal("<"),
                b -> {
                    if (mobPage > 0) {
                        mobPage--;
                        rebuild();
                    }
                }
            ).bounds(
                left + 10,
                top + 224,
                42,
                20
            ).build()
        );

        // Next page

        addRenderableWidget(
            Button.builder(
                Component.literal(">"),
                b -> {
                    if ((mobPage + 1) * perPage
                        < MobEspSettings.MOBS.length) {

                        mobPage++;
                        rebuild();
                    }
                }
            ).bounds(
                left + w - 52,
                top + 224,
                42,
                20
            ).build()
        );
    }

    // =========================================
    // RENDER GUI
    // =========================================

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {

        // Do not call renderBackground().
        // Avoids Minecraft's double-blur crash.

        graphics.fill(
            0, 0, width, height, BG
        );

        // Top bar

        graphics.fill(
            0, 0, width, 42,
            0xED171023
        );

        graphics.fill(
            0, 40, width, 42,
            PURPLE
        );

        graphics.drawString(
            font,
            "✦ NOVA NEBULA",
            14,
            13,
            PURPLE
        );

        graphics.drawString(
            font,
            "FABRIC 1.21.11",
            14,
            27,
            MUTED
        );

        if (settingsOpen) {

            int w = Math.min(
                420,
                width - 30
            );

            int x = (width - w) / 2;

            int y = Math.max(
                45,
                (height - 275) / 2
            );

            graphics.fill(
                x - 1,
                y - 1,
                x + w + 1,
                y + 259,
                PANEL_BORDER
            );

            graphics.fill(
                x,
                y,
                x + w,
                y + 258,
                PANEL
            );

            graphics.drawString(
                font,
                "MOB ESP TARGETS",
                x + 11,
                y + 37,
                PURPLE
            );

            graphics.drawCenteredString(
                font,
                "Page " + (mobPage + 1)
                    + "/"
                    + ((MobEspSettings.MOBS.length + 11)
                        / 12),
                width / 2,
                y + 229,
                WHITE
            );

        } else {

            long elapsed =
                System.currentTimeMillis() - openedAt;

            for (int i = 0;
                 i < Category.values().length;
                 i++) {

                double progress = Math.min(
                    1.0,
                    Math.max(
                        0.0,
                        (elapsed - i * WAVE_STEP_MS)
                            / (double) WAVE_DURATION_MS
                    )
                );

                if (progress <= 0) {
                    continue;
                }

                double eased =
                    1 - Math.pow(1 - progress, 3);

                int slide = (int) (
                    (1 - eased) * 38
                );

                int x = px(i);
                int y = py(i) + slide;
                int w = panelWidth();

                int h = collapsed[i]
                    ? 27
                    : panelHeight();

                // Panel border

                graphics.fill(
                    x - 1,
                    y - 1,
                    x + w + 1,
                    y + h + 1,
                    PANEL_BORDER
                );

                // Panel background

                graphics.fill(
                    x,
                    y,
                    x + w,
                    y + h,
                    PANEL
                );

                // Purple category header

                graphics.fill(
                    x,
                    y,
                    x + w,
                    y + 26,
                    ACTIVE
                );
            }
        }

        graphics.drawString(
            font,
            "RIGHT SHIFT  •  NOVA CLIENT",
            12,
            height - 15,
            MUTED
        );

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
