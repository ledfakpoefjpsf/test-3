
package nova.client.client.gui;

// @name: MacroSettingsScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/MacroSettingsScreen.java

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.macro.MacroEngine;

public final class MacroSettingsScreen extends Screen {

    private final Screen parent;
    private EditBox name;

    private String message =
        "X: Record/Save | F7: Play | F8: Stop";

    private int page = 0;

    public MacroSettingsScreen(Screen parent) {
        super(Component.literal(
            "Nova Macro Settings"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {

        int x = width / 2 - 145;
        int y = Math.max(20, height / 2 - 125);

        name = new EditBox(
            font,
            x + 10,
            y + 34,
            270,
            20,
            Component.literal("Macro name")
        );

        name.setMaxLength(40);
        name.setValue(MacroEngine.selected());

        addRenderableWidget(name);

        // START RECORDING
        addRenderableWidget(
            Button.builder(
                Component.literal("Start Recording"),
                b -> {
                    MacroEngine.select(name.getValue());

                    Minecraft mc =
                        Minecraft.getInstance();

                    mc.setScreen(null);
                    MacroEngine.startRecording(mc);
                }
            ).bounds(
                x + 10, y + 62, 130, 20
            ).build()
        );

        // FINISH AND SAVE
        addRenderableWidget(
            Button.builder(
                Component.literal("Finish + Save"),
                b -> {
                    MacroEngine.select(name.getValue());

                    if (MacroEngine.isRecording()) {
                        MacroEngine.finishRecording();
                        message = "Recording saved!";
                    } else {
                        message = "No recording in progress";
                    }
                }
            ).bounds(
                x + 150, y + 62, 130, 20
            ).build()
        );

        // LOAD
        addRenderableWidget(
            Button.builder(
                Component.literal("Load"),
                b -> {
                    boolean loaded =
                        MacroEngine.load(name.getValue());

                    message = loaded
                        ? "Macro loaded!"
                        : "Macro not found";
                }
            ).bounds(
                x + 10, y + 88, 86, 20
            ).build()
        );

        // PLAY
        addRenderableWidget(
            Button.builder(
                Component.literal("Play"),
                b -> {
                    if (!MacroEngine.load(name.getValue())
                        && MacroEngine.frameCount() == 0) {
                        message = "No macro loaded";
                        return;
                    }

                    Minecraft mc =
                        Minecraft.getInstance();

                    mc.setScreen(null);
                    MacroEngine.play(mc);
                }
            ).bounds(
                x + 102, y + 88, 86, 20
            ).build()
        );

        // STOP
        addRenderableWidget(
            Button.builder(
                Component.literal("Stop"),
                b -> {
                    MacroEngine.stop(
                        Minecraft.getInstance()
                    );

                    message = "Stopped";
                }
            ).bounds(
                x + 194, y + 88, 86, 20
            ).build()
        );

        // AUTO MINE
        addRenderableWidget(
            Button.builder(
                Component.literal("Auto Mine"),
                b -> {
                    Minecraft mc =
                        Minecraft.getInstance();

                    mc.setScreen(null);
                    MacroEngine.startAutoMine(mc);
                }
            ).bounds(
                x + 10, y + 114, 130, 20
            ).build()
        );

        // LOOP TOGGLE
        addRenderableWidget(
            Button.builder(
                Component.literal(
                    "Loop: " +
                    (MacroEngine.isLooping()
                        ? "ON" : "OFF")
                ),
                b -> {
                    MacroEngine.setLooping(
                        !MacroEngine.isLooping()
                    );

                    b.setMessage(
                        Component.literal(
                            "Loop: " +
                            (MacroEngine.isLooping()
                                ? "ON" : "OFF")
                        )
                    );
                }
            ).bounds(
                x + 150, y + 114, 130, 20
            ).build()
        );

        // DELETE
        addRenderableWidget(
            Button.builder(
                Component.literal("Delete Saved"),
                b -> {
                    boolean deleted =
                        MacroEngine.delete(name.getValue());

                    message = deleted
                        ? "Macro deleted"
                        : "No saved macro found";
                }
            ).bounds(
                x + 10, y + 140, 130, 20
            ).build()
        );

        // BACK
        addRenderableWidget(
            Button.builder(
                Component.literal("Back"),
                b -> onClose()
            ).bounds(
                x + 150, y + 140, 130, 20
            ).build()
        );

        // PREVIOUS PAGE
        addRenderableWidget(
            Button.builder(
                Component.literal("<"),
                b -> page = Math.max(0, page - 1)
            ).bounds(
                x + 10, y + 205, 35, 20
            ).build()
        );

        // NEXT PAGE
        addRenderableWidget(
            Button.builder(
                Component.literal(">"),
                b -> {
                    int count =
                        MacroEngine.savedNames().size();

                    if ((page + 1) * 2 < count) {
                        page++;
                    }
                }
            ).bounds(
                x + 245, y + 205, 35, 20
            ).build()
        );
    }

    @Override
    public void render(
        GuiGraphics g,
        int mx,
        int my,
        float partial
    ) {

        int x = width / 2 - 145;
        int y = Math.max(20, height / 2 - 125);

        // Dark Arctic Blue background
        g.fill(
            0, 0, width, height,
            0xD8091522
        );

        // Main panel
        g.fill(
            x, y,
            x + 290, y + 250,
            0xFF102638
        );

        // Header
        g.fill(
            x, y,
            x + 290, y + 25,
            0xFF194663
        );

        g.drawCenteredString(
            font,
            "NOVA / MACRO SETTINGS",
            width / 2,
            y + 8,
            0xFFE0F3FF
        );

        g.drawString(
            font,
            "Macro Name",
            x + 10,
            y + 27,
            0xFF83C9ED
        );

        List<String> saved =
            MacroEngine.savedNames();

        g.drawString(
            font,
            "Saved Macros (click to select):",
            x + 10,
            y + 171,
            0xFFE0F3FF
        );

        for (int i = 0; i < 2; i++) {
            int index = page * 2 + i;

            if (index < saved.size()) {
                g.drawString(
                    font,
                    saved.get(index),
                    x + 12,
                    y + 184 + i * 11,
                    0xFF83C9ED
                );
            }
        }

        String status = MacroEngine.isRecording()
            ? "RECORDING - Press X to Save"
            : MacroEngine.isPlaying()
                ? "PLAYING MACRO"
                : MacroEngine.isMining()
                    ? "AUTO MINE ACTIVE"
                    : message;

        g.drawCenteredString(
            font,
            status,
            width / 2,
            y + 233,
            0xFF8DADC2
        );

        super.render(g, mx, my, partial);
    }

    @Override
    public boolean mouseClicked(
        double mx,
        double my,
        int button
    ) {

        int x = width / 2 - 145;
        int y = Math.max(20, height / 2 - 125);

        if (button == 0
            && mx >= x + 10
            && mx <= x + 280
            && my >= y + 181
            && my < y + 205) {

            int index =
                page * 2 +
                ((int) my - (y + 181)) / 11;

            List<String> saved =
                MacroEngine.savedNames();

            if (index >= 0 && index < saved.size()) {
                name.setValue(saved.get(index));
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
