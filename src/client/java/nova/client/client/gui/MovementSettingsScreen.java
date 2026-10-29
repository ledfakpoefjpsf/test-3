
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
        String fallback,
        String[] values
    ) {}

    private final Screen parent;
    private final String module;
    private final List<Option> options = new ArrayList<>();

    private int page = 0;

    public MovementSettingsScreen(Screen parent, String module) {
        super(Component.literal(module + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    private void add(
        String key, String title, String fallback,
        String... values
    ) {
        options.add(new Option(key, title, fallback, values));
    }

    private void prepare() {
        options.clear();

        switch (module) {
            case "Speed" -> {
                add("mode", "Mode", "Custom",
                    "Walk", "Sprint", "Custom");
                add("speed", "Speed", "0.3",
                    "0.15", "0.2", "0.3", "0.4", "0.6", "0.8", "1.0");
                add("groundOnly", "Ground only", "false",
                    "false", "true");
            }

            case "Fly" -> {
                add("speed", "Horizontal speed", "0.4",
                    "0.2", "0.4", "0.6", "0.8", "1.0", "1.5");
                add("vertical", "Vertical speed", "0.35",
                    "0.15", "0.25", "0.35", "0.5", "0.8");
            }

            case "Long Jump" -> {
                add("speed", "Forward speed", "0.75",
                    "0.3", "0.5", "0.75", "1.0", "1.3");
                add("height", "Jump height", "0.42",
                    "0.42", "0.5", "0.6", "0.8");
            }

            case "High Jump" -> add(
                "height", "Jump height", "0.7",
                "0.42", "0.55", "0.7", "0.9", "1.1", "1.4"
            );

            case "Bunny Hop" -> add(
                "speed", "Hop speed", "0.3",
                "0.2", "0.3", "0.4", "0.5", "0.7"
            );

            case "Step" -> add(
                "height", "Jump height", "0.42",
                "0.42", "0.55", "0.7", "0.9"
            );

            case "No Slow" -> add(
                "speed", "Using-item speed", "0.22",
                "0.1", "0.15", "0.22", "0.3", "0.4"
            );

            case "Spider" -> add(
                "speed", "Wall climb speed", "0.25",
                "0.1", "0.2", "0.25", "0.4", "0.6"
            );

            case "Jesus" -> {
                add("lift", "Water lift", "0.15",
                    "0.05", "0.1", "0.15", "0.25", "0.4");
                add("speed", "Water speed", "0.2",
                    "0.1", "0.2", "0.3", "0.5");
            }

            case "Parkour" -> add(
                "distance", "Edge detection", "0.8",
                "0.5", "0.65", "0.8", "1.0", "1.2"
            );

            case "Safe Walk" -> add(
                "distance", "Edge detection", "0.65",
                "0.4", "0.5", "0.65", "0.8", "1.0"
            );

            case "No Fall" -> {
                add("threshold", "Fall threshold", "3.0",
                    "2.0", "3.0", "4.0", "5.0", "8.0");
                add("fallSpeed", "Fall speed limit", "0.25",
                    "0.1", "0.2", "0.25", "0.3", "0.4");
            }

            case "Fast Ladder" -> add(
                "speed", "Climb speed", "0.35",
                "0.2", "0.35", "0.5", "0.7", "1.0"
            );

            case "Air Jump" -> {
                add("jumps", "Extra jumps", "2",
                    "1", "2", "3", "4", "5");
                add("height", "Jump height", "0.5",
                    "0.3", "0.42", "0.5", "0.7", "0.9");
            }

            case "Elytra Boost" -> {
                add("boost", "Acceleration", "0.06",
                    "0.02", "0.04", "0.06", "0.1", "0.2");
                add("maxSpeed", "Maximum speed", "2.0",
                    "1.0", "1.5", "2.0", "3.0", "5.0");
            }

            case "Auto Sprint" -> {
                add("forwardOnly", "Forward only", "true",
                    "true", "false");
                add("requireHunger", "Require hunger", "true",
                    "true", "false");
            }

            case "Auto Walk" -> add(
                "stopSneaking", "Stop while sneaking", "false",
                "false", "true"
            );

            case "Auto Sneak" -> add(
                "groundOnly", "Ground only", "false",
                "false", "true"
            );

            case "Auto Jump" -> add(
                "movingOnly", "Moving only", "true",
                "true", "false"
            );
        }
    }

    private String current(Option option) {
        return MovementModules.get(
            module, option.key(), option.fallback()
        );
    }

    private void cycle(Option option) {
        String value = current(option);
        String[] values = option.values();

        int index = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                index = i;
                break;
            }
        }

        MovementModules.set(
            module, option.key(),
            values[(index + 1) % values.length]
        );
    }

    private void rebuild() {
        clearWidgets();

        int x = width / 2 - 140;
        int top = Math.max(45, height / 2 - 95);

        int start = page * 5;
        int end = Math.min(start + 5, options.size());

        for (int i = start; i < end; i++) {
            Option option = options.get(i);
            int y = top + (i - start) * 28;

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        option.title() + ": " + current(option)
                    ),
                    button -> {
                        cycle(option);
                        button.setMessage(Component.literal(
                            option.title() + ": " + current(option)
                        ));
                    }
                ).bounds(x, y, 280, 23).build()
            );
        }

        int bottom = top + 155;

        if (page > 0) {
            addRenderableWidget(
                Button.builder(
                    Component.literal("Previous"),
                    b -> {
                        page--;
                        rebuild();
                    }
                ).bounds(x, bottom, 85, 22).build()
            );
        }

        if ((page + 1) * 5 < options.size()) {
            addRenderableWidget(
                Button.builder(
                    Component.literal("Next"),
                    b -> {
                        page++;
                        rebuild();
                    }
                ).bounds(x + 195, bottom, 85, 22).build()
            );
        }

        addRenderableWidget(
            Button.builder(
                Component.literal("Back"),
                b -> onClose()
            ).bounds(
                width / 2 - 60,
                bottom + 30,
                120, 23
            ).build()
        );
    }

    @Override
    protected void init() {
        prepare();
        rebuild();
    }

    @Override
    public void render(
        GuiGraphics g, int mx, int my, float delta
    ) {
        g.fill(0, 0, width, height, 0xE8091522);

        int x = width / 2 - 155;
        int y = Math.max(12, height / 2 - 135);

        g.fill(x, y, x + 310, y + 285, 0xF5102638);
        g.fill(x, y, x + 310, y + 30, 0xFF194663);

        g.drawCenteredString(
            font,
            "NOVA MOVEMENT - " + module,
            width / 2,
            y + 10,
            0xFF83C9ED
        );

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
