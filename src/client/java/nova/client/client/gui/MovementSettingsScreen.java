
package nova.client.client.gui;

// @name: MovementSettingsScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/MovementSettingsScreen.java

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.module.MovementModules;

public final class MovementSettingsScreen extends Screen {

    private record Option(
        String key,
        String title,
        String[] values
    ) {}

    private final Screen parent;
    private final String module;
    private final List<Option> options = new ArrayList<>();

    private int page = 0;

    public MovementSettingsScreen(
        Screen parent,
        String module
    ) {
        super(Component.literal(module + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    private void add(
        String key,
        String title,
        String... values
    ) {
        options.add(new Option(key, title, values));
    }

    private void prepareOptions() {
        options.clear();

        switch (module) {
            case "Speed" -> {
                add("mode", "Mode",
                    "Walk", "Sprint", "Custom");
                add("multiplier", "Multiplier",
                    "1.1", "1.25", "1.5",
                    "2.0", "3.0", "4.0");
            }

            case "Fly" -> {
                add("speed", "Flight speed",
                    "0.5", "1.0", "1.5",
                    "2.0", "3.0", "5.0");
            }

            case "Long Jump" -> {
                add("boost", "Jump boost",
                    "1.25", "1.5", "2.0",
                    "2.5", "3.0");
            }

            case "High Jump" -> {
                add("height", "Jump height",
                    "0.45", "0.55", "0.65",
                    "0.8", "1.0", "1.2");
            }

            case "Bunny Hop" -> {
                add("boost", "Hop boost",
                    "1.0", "1.1", "1.15",
                    "1.25", "1.5");
            }

            case "Step" -> {
                add("height", "Step height",
                    "0.5", "1.0");
            }

            case "No Slow" -> {
                add("boost", "Movement boost",
                    "1.0", "1.15", "1.25",
                    "1.5", "2.0");
            }

            case "Spider" -> {
                add("speed", "Climb speed",
                    "0.1", "0.2", "0.3",
                    "0.4", "0.5");
            }

            case "Jesus" -> {
                add("lift", "Water lift",
                    "0.05", "0.1", "0.12",
                    "0.2", "0.3");
            }

            case "No Fall" -> {
                add("threshold", "Fall threshold",
                    "2.0", "3.0", "4.0",
                    "5.0", "6.0");
            }

            case "Fast Ladder" -> {
                add("speed", "Climb speed",
                    "0.2", "0.3", "0.4",
                    "0.6", "0.8");
            }

            case "Air Jump" -> {
                add("jumps", "Extra jumps",
                    "1", "2", "3", "4", "5");
                add("height", "Jump height",
                    "0.3", "0.42", "0.55",
                    "0.7", "0.9");
            }

            case "Elytra Boost" -> {
                add("boost", "Acceleration",
                    "0.01", "0.02", "0.04",
                    "0.06", "0.1", "0.2");
            }

            case "Parkour", "Safe Walk" -> {
                // These modules currently use automatic behavior.
            }
        }
    }

    @Override
    protected void init() {
        prepareOptions();
        buildButtons();
    }

    private String label(Option o) {
        return o.title + ": " + MovementModules.get(
            module, o.key, o.values[0]
        );
    }

    private void cycle(Option o) {
        String current = MovementModules.get(
            module, o.key, o.values[0]
        );

        int index = -1;

        for (int i = 0; i < o.values.length; i++) {
            if (o.values[i].equals(current)) {
                index = i;
                break;
            }
        }

        MovementModules.set(
            module,
            o.key,
            o.values[(index + 1) % o.values.length]
        );
    }

    private void buildButtons() {
        clearWidgets();

        int x = width / 2 - 145;
        int y = Math.max(40, height / 2 - 110);

        int start = page * 6;
        int end = Math.min(start + 6, options.size());

        for (int i = start; i < end; i++) {
            Option option = options.get(i);
            int yy = y + 34 + (i - start) * 26;

            addRenderableWidget(
                Button.builder(
                    Component.literal(label(option)),
                    button -> {
                        cycle(option);
                        button.setMessage(
                            Component.literal(label(option))
                        );
                    }
                ).bounds(x, yy, 290, 22).build()
            );
        }

        if (page > 0) {
            addRenderableWidget(
                Button.builder(
                    Component.literal("Previous"),
                    b -> {
                        page--;
                        buildButtons();
                    }
                ).bounds(
                    x, y + 202, 90, 20
                ).build()
            );
        }

        if ((page + 1) * 6 < options.size()) {
            addRenderableWidget(
                Button.builder(
                    Component.literal("Next"),
                    b -> {
                        page++;
                        buildButtons();
                    }
                ).bounds(
                    x + 200, y + 202, 90, 20
                ).build()
            );
        }

        addRenderableWidget(
            Button.builder(
                Component.literal("Back"),
                b -> onClose()
            ).bounds(
                width / 2 - 60,
                y + 235,
                120,
                22
            ).build()
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
            0, 0, width, height,
            0xE8091522
        );

        int x = width / 2 - 160;
        int y = Math.max(20, height / 2 - 140);

        g.fill(
            x, y,
            x + 320, y + 320,
            0xF5102638
        );

        g.fill(
            x, y,
            x + 320, y + 30,
            0xFF194663
        );

        g.drawCenteredString(
            font,
            "NOVA MOVEMENT / " + module,
            width / 2,
            y + 10,
            0xFF83C9ED
        );

        if (options.isEmpty()) {
            g.drawCenteredString(
                font,
                "Automatic movement module",
                width / 2,
                y + 115,
                0xFF8DADC2
            );
        }

        super.render(g, mx, my, delta);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
