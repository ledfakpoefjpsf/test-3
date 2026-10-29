
package nova.client.client.gui;

// @name: CombatSettingsScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/CombatSettingsScreen.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nova.client.client.module.CombatSettings;

import java.util.*;

public final class CombatSettingsScreen extends Screen {

    private record Option(
        String key,
        String label,
        String[] values
    ) {}

    private final Screen parent;
    private final String module;
    private final List<Option> options = new ArrayList<>();

    public CombatSettingsScreen(
        Screen parent,
        String module
    ) {
        super(Component.literal(module + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    private void add(
        String key,
        String label,
        String... values
    ) {
        options.add(new Option(key, label, values));
    }

    @Override
    protected void init() {
        options.clear();

        if (List.of(
            "Bow Aimbot",
            "Sword Aimbot",
            "Aim Assist"
        ).contains(module)) {
            add("mode", "Aiming", "Snap", "Smooth");
            add(
                "speed", "Smooth speed",
                "2", "5", "8", "12", "20", "40"
            );
        }

        if (List.of(
            "Bow Aimbot",
            "Sword Aimbot",
            "Aim Assist",
            "Auto Shield",
            "Target Strafe",
            "Auto Weapon",
            "Criticals Helper"
        ).contains(module)) {
            add(
                "priority", "Priority",
                "Closest", "Furthest",
                "Lowest HP", "Highest HP"
            );
            add(
                "range", "Range",
                "3", "4.5", "6",
                "12", "24", "48", "64"
            );
            add("players", "Target players", "true", "false");
            add("mobs", "Target mobs", "true", "false");
            add("los", "Line of sight", "true", "false");
        }

        if (module.equals("Bow Aimbot")) {
            add("prediction", "Prediction", "true", "false");
            add(
                "lead", "Prediction lead",
                "0.5", "1", "1.5", "2"
            );
        }

        if (module.equals("TriggerBot")) {
            add(
                "delay", "Attack delay ticks",
                "1", "2", "3", "5", "10"
            );
            add("range", "Range", "3", "4.5", "6");
            add("players", "Players", "true", "false");
            add("mobs", "Mobs", "true", "false");
            add("los", "Line of sight", "true", "false");
        }

        if (module.equals("Auto Totem")) {
            add(
                "health", "Health threshold",
                "4", "6", "8", "10",
                "12", "16", "20"
            );
        }

        if (module.equals("Target Strafe")) {
            add("left", "Direction left", "false", "true");
        }

        if (module.equals("Auto Crystal")) {
            add(
                "range", "Crystal range",
                "3", "4", "5", "6"
            );
        }

        if (module.equals("Hitboxes")) {
            add(
                "size", "Outline expansion",
                "0", "0.25", "0.5",
                "1", "1.5", "2"
            );
            add(
                "range", "Render range",
                "8", "16", "32", "48"
            );
            add("players", "Players", "true", "false");
            add("mobs", "Mobs", "true", "false");
        }

        if (module.equals("Projectile ESP")) {
            add(
                "range", "Render range",
                "16", "32", "64", "96"
            );
            add("arrows", "Arrows", "true", "false");
            add("tridents", "Tridents", "true", "false");
            add("other", "Other projectiles", "true", "false");
            add(
                "color", "Outline color",
                "Cyan", "Purple", "Green",
                "Red", "White"
            );
        }

        int x = width / 2 - 145;
        int y = Math.max(22, height / 2 - 125);

        for (int i = 0; i < options.size(); i++) {
            Option o = options.get(i);
            int yy = y + 36 + i * 23;

            addRenderableWidget(
                Button.builder(
                    Component.literal(label(o)),
                    b -> {
                        CombatSettings.cycle(
                            module, o.key, o.values
                        );
                        b.setMessage(
                            Component.literal(label(o))
                        );
                    }
                ).bounds(
                    x + 10, yy, 270, 20
                ).build()
            );
        }

        addRenderableWidget(
            Button.builder(
                Component.literal("Back"),
                b -> onClose()
            ).bounds(
                x + 85,
                y + 42 + options.size() * 23,
                120, 20
            ).build()
        );
    }

    private String label(Option o) {
        return o.label + ": " +
            CombatSettings.get(
                module, o.key, o.values[0]
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

        g.drawCenteredString(
            font,
            "NOVA COMBAT / " + module,
            width / 2,
            Math.max(8, height / 2 - 143),
            0xFF83C9ED
        );

        super.render(g, mx, my, delta);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
