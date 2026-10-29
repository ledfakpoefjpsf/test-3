package nova.client.client.gui;

// @name: XraySettingsScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/XraySettingsScreen.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.NovaClient;
import nova.client.client.module.XraySettings;
import nova.client.client.render.XrayRenderer;

public final class XraySettingsScreen extends Screen {

    private static final int PANEL = 0xF0171024;
    private static final int HEADER = 0xFF422663;
    private static final int ACTIVE = 0xFF674091;
    private static final int INACTIVE = 0xFF30253C;
    private static final int WHITE = 0xFFF4EAFF;
    private static final int PURPLE = 0xFFC084FC;

    private final Screen parent;

    public XraySettingsScreen(Screen parent) {
        super(Component.literal("Nova X-Ray Settings"));
        this.parent = parent;
    }

    private int left() {
        return (width - 350) / 2;
    }

    private int top() {
        return Math.max(12, (height - 445) / 2);
    }

    @Override
    protected void init() {

        int x = left();
        int y = top();

        addRenderableWidget(
            net.minecraft.client.gui.components.Button
                .builder(
                    Component.literal("Back"),
                    button -> onClose()
                )
                .bounds(x + 275, y + 8, 65, 20)
                .build()
        );

        String[] names = {
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

        for (int i = 0; i < names.length; i++) {

            final int index = i;

            int col = i % 2;
            int row = i / 2;

            int bx = x + 10 + col * 170;
            int by = y + 43 + row * 33;

            addRenderableWidget(
                net.minecraft.client.gui.components.Button
                    .builder(
                        Component.literal(
                            buttonLabel(index)
                        ),
                        button -> {

                            toggle(index);

                            button.setMessage(
                                Component.literal(
                                    buttonLabel(index)
                                )
                            );

                            save();
                        }
                    )
                    .bounds(bx, by, 160, 26)
                    .build()
            );
        }

        addRenderableWidget(
            new net.minecraft.client.gui.components.AbstractSliderButton(
                x + 12,
                y + 291,
                326,
                20,
                Component.empty(),
                (XraySettings.range - 16) / 112.0
            ) {

                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(
                        "Scan Range: "
                            + XraySettings.range
                            + " blocks"
                    ));
                }

                @Override
                protected void applyValue() {

                    XraySettings.setRange(
                        16 + (int) Math.round(
                            value * 112
                        )
                    );

                    XrayRenderer.clear();
                    save();
                }
            }
        );

        addRenderableWidget(
            new net.minecraft.client.gui.components.AbstractSliderButton(
                x + 12,
                y + 327,
                326,
                20,
                Component.empty(),
                XraySettings.hiddenOpacity / 60.0
            ) {

                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(
                        "Hidden Terrain Opacity: "
                            + XraySettings.hiddenOpacity
                            + "%"
                    ));
                }

                @Override
                protected void applyValue() {

                    XraySettings.setHiddenOpacity(
                        (int) Math.round(value * 60)
                    );

                    save();
                }
            }
        );

        addRenderableWidget(
            net.minecraft.client.gui.components.Button
                .builder(
                    Component.literal("Reset Defaults"),
                    button -> {

                        XraySettings.reset();
                        XrayRenderer.clear();
                        save();

                        Minecraft.getInstance().setScreen(
                            new XraySettingsScreen(parent)
                        );
                    }
                )
                .bounds(
                    x + 12,
                    y + 365,
                    326,
                    22
                )
                .build()
        );
    }

    private String buttonLabel(int index) {

        String[] names = {
            "Bedrock", "Lava", "Water",
            "Diamond", "Ancient Debris",
            "Emerald", "Gold", "Iron",
            "Redstone", "Lapis", "Copper",
            "Coal", "Nether Quartz"
        };

        return names[index] + ": "
            + (enabled(index) ? "ON" : "OFF");
    }

    private boolean enabled(int index) {

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

    private void toggle(int index) {

        switch (index) {
            case 0 -> XraySettings.bedrock ^= true;
            case 1 -> XraySettings.lava ^= true;
            case 2 -> XraySettings.water ^= true;
            case 3 -> XraySettings.diamond ^= true;
            case 4 -> XraySettings.ancient ^= true;
            case 5 -> XraySettings.emerald ^= true;
            case 6 -> XraySettings.gold ^= true;
            case 7 -> XraySettings.iron ^= true;
            case 8 -> XraySettings.redstone ^= true;
            case 9 -> XraySettings.lapis ^= true;
            case 10 -> XraySettings.copper ^= true;
            case 11 -> XraySettings.coal ^= true;
            case 12 -> XraySettings.quartz ^= true;
            default -> {}
        }

        XrayRenderer.clear();
    }

    private void save() {
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
            0, 0, width, height,
            0xD0090612
        );

        int x = left();
        int y = top();

        graphics.fill(
            x - 1, y - 1,
            x + 351, y + 446,
            PURPLE
        );

        graphics.fill(
            x, y,
            x + 350, y + 445,
            PANEL
        );

        graphics.fill(
            x, y,
            x + 350, y + 34,
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
            "Block filters",
            x + 12,
            y + 275,
            PURPLE,
            false
        );

        graphics.drawString(
            font,
            "Outlines are visible through terrain.",
            x + 12,
            y + 405,
            WHITE,
            false
        );

        graphics.drawString(
            font,
            "Opacity requires transparent-terrain rendering.",
            x + 12,
            y + 419,
            0xFFB6A7C7,
            false
        );

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
