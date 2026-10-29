package nova.client.client.gui;

// @name: XraySettingsScreen
// @type: Java Client GUI
// @location: src/client/java/nova/client/client/gui/XraySettingsScreen.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.NovaClient;
import nova.client.client.module.XraySettings;
import nova.client.client.render.XrayChunkRefresh;

public final class XraySettingsScreen extends Screen {

    private static final int PANEL_WIDTH = 350;
    private static final int PANEL_HEIGHT = 445;

    private static final int PANEL = 0xF0171024;
    private static final int HEADER = 0xFF422663;
    private static final int WHITE = 0xFFF4EAFF;
    private static final int PURPLE = 0xFFC084FC;
    private static final int MUTED = 0xFFB6A7C7;

    private static final String[] BLOCK_NAMES = {
        "Bedrock",
        "Lava",
        "Water",
        "Diamond",
        "Ancient Debris",
        "Emerald",
        "Gold",
        "Iron",
        "Redstone",
        "Lapis",
        "Copper",
        "Coal",
        "Nether Quartz"
    };

    private final Screen parent;

    public XraySettingsScreen(Screen parent) {
        super(Component.literal("Nova X-Ray Settings"));
        this.parent = parent;
    }

    private int left() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return Math.max(
            12,
            (height - PANEL_HEIGHT) / 2
        );
    }

    @Override
    protected void init() {

        int x = left();
        int y = top();

        addRenderableWidget(
            Button.builder(
                Component.literal("Back"),
                button -> onClose()
            )
            .bounds(x + 275, y + 8, 65, 20)
            .build()
        );

        // Individual block selection buttons.
        for (int i = 0; i < BLOCK_NAMES.length; i++) {

            final int index = i;

            int column = i % 2;
            int row = i / 2;

            int bx = x + 10 + column * 170;
            int by = y + 43 + row * 33;

            addRenderableWidget(
                Button.builder(
                    Component.literal(buttonLabel(index)),
                    button -> {

                        toggleBlock(index);

                        button.setMessage(
                            Component.literal(
                                buttonLabel(index)
                            )
                        );

                        settingsChanged();
                    }
                )
                .bounds(bx, by, 160, 26)
                .build()
            );
        }

        // Scan range slider.
        addRenderableWidget(
            new AbstractSliderButton(
                x + 12,
                y + 291,
                326,
                20,
                Component.empty(),
                (XraySettings.range - 16) / 112.0
            ) {

                @Override
                protected void updateMessage() {

                    setMessage(
                        Component.literal(
                            "Scan Range: "
                            + XraySettings.range
                            + " blocks"
                        )
                    );
                }

                @Override
                protected void applyValue() {

                    XraySettings.setRange(
                        16 + (int) Math.round(
                            value * 112.0
                        )
                    );

                    settingsChanged();
                }
            }
        );

        // Terrain opacity slider.
        addRenderableWidget(
            new AbstractSliderButton(
                x + 12,
                y + 327,
                326,
                20,
                Component.empty(),
                XraySettings.hiddenOpacity / 60.0
            ) {

                @Override
                protected void updateMessage() {

                    setMessage(
                        Component.literal(
                            "Hidden Terrain Opacity: "
                            + XraySettings.hiddenOpacity
                            + "%"
                        )
                    );
                }

                @Override
                protected void applyValue() {

                    XraySettings.setHiddenOpacity(
                        (int) Math.round(
                            value * 60.0
                        )
                    );

                    settingsChanged();
                }
            }
        );

        // Restore original selections.
        addRenderableWidget(
            Button.builder(
                Component.literal("Reset Defaults"),
                button -> {

                    XraySettings.reset();

                    settingsChanged();

                    Minecraft.getInstance().setScreen(
                        new XraySettingsScreen(parent)
                    );
                }
            )
            .bounds(x + 12, y + 365, 326, 22)
            .build()
        );
    }

    private String buttonLabel(int index) {

        if (index < 0 || index >= BLOCK_NAMES.length) {
            return "Unknown";
        }

        return BLOCK_NAMES[index]
            + ": "
            + (isEnabled(index) ? "ON" : "OFF");
    }

    private boolean isEnabled(int index) {

        return switch (index) {

            case 0 -> XraySettings.bedrock;
            case 1 -> XraySettings.lava;
            case 2 -> XraySettings.water;
            case 3 -> XraySettings.diamond;
            case 4 -> XraySettings.ancient;
            case 5 -> XraySettings.emerald;
            case 6 -> XraySettings.gold;
            case 7 -> XraySettings.iron;
            case 8 -> XraySettings.redstone;
            case 9 -> XraySettings.lapis;
            case 10 -> XraySettings.copper;
            case 11 -> XraySettings.coal;
            case 12 -> XraySettings.quartz;

            default -> false;
        };
    }

    private void toggleBlock(int index) {

        switch (index) {

            case 0 ->
                XraySettings.bedrock = !XraySettings.bedrock;

            case 1 ->
                XraySettings.lava = !XraySettings.lava;

            case 2 ->
                XraySettings.water = !XraySettings.water;

            case 3 ->
                XraySettings.diamond = !XraySettings.diamond;

            case 4 ->
                XraySettings.ancient = !XraySettings.ancient;

            case 5 ->
                XraySettings.emerald = !XraySettings.emerald;

            case 6 ->
                XraySettings.gold = !XraySettings.gold;

            case 7 ->
                XraySettings.iron = !XraySettings.iron;

            case 8 ->
                XraySettings.redstone = !XraySettings.redstone;

            case 9 ->
                XraySettings.lapis = !XraySettings.lapis;

            case 10 ->
                XraySettings.copper = !XraySettings.copper;

            case 11 ->
                XraySettings.coal = !XraySettings.coal;

            case 12 ->
                XraySettings.quartz = !XraySettings.quartz;

            default -> {
            }
        }
    }

    private void settingsChanged() {

        // Request the terrain to be rebuilt using
        // the current X-Ray settings.
        XrayChunkRefresh.requestRefresh();

        // Persist block filters and slider values.
        NovaClient.CONFIG.save();
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {

        graphics.fill(
            0,
            0,
            width,
            height,
            0xD0090612
        );

        int x = left();
        int y = top();

        // Outer border.
        graphics.fill(
            x - 1,
            y - 1,
            x + PANEL_WIDTH + 1,
            y + PANEL_HEIGHT + 1,
            PURPLE
        );

        // Main background.
        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + PANEL_HEIGHT,
            PANEL
        );

        // Header.
        graphics.fill(
            x,
            y,
            x + PANEL_WIDTH,
            y + 34,
            HEADER
        );

        graphics.drawString(
            font,
            "NOVA / X-RAY",
            x + 12,
            y + 13,
            WHITE,
            true
        );

        graphics.drawString(
            font,
            "Block Filters",
            x + 12,
            y + 275,
            PURPLE,
            false
        );

        graphics.drawString(
            font,
            "Selected blocks stay visible.",
            x + 12,
            y + 405,
            WHITE,
            false
        );

        graphics.drawString(
            font,
            "Opacity requires the terrain X-Ray renderer.",
            x + 12,
            y + 419,
            MUTED,
            false
        );

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );
    }

    @Override
    public void onClose() {

        NovaClient.CONFIG.save();

        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
