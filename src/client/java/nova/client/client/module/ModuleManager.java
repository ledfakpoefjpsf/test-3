
package nova.client.module;

// @name: ModuleManager
// @type: Java Class
// @location: src/client/java/nova/client/module/

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules =
            new ArrayList<>();

    public void initialize() {

        // COMBAT
        register(new Module(
            "Target HUD",
            "Display information about a target",
            Category.COMBAT
        ) {});

        register(new Module(
            "Hit Color",
            "Customize entity hit effects",
            Category.COMBAT
        ) {});

        // MOVEMENT
        register(new Module(
            "Sprint",
            "Automatic sprint controls",
            Category.MOVEMENT
        ) {});

        register(new Module(
            "Toggle Sneak",
            "Toggle sneaking",
            Category.MOVEMENT
        ) {});

        // RENDER
        register(new Module(
            "Fullbright",
            "Increase game brightness",
            Category.RENDER
        ) {});

        register(new Module(
            "FPS Display",
            "Display current FPS",
            Category.RENDER
        ) {});

        register(new Module(
            "Coordinates",
            "Display player coordinates",
            Category.RENDER
        ) {});

        register(new Module(
            "Armor HUD",
            "Display equipped armor",
            Category.RENDER
        ) {});

        register(new Module(
            "Keystrokes",
            "Display movement keys",
            Category.RENDER
        ) {});

        // PLAYER
        register(new Module(
            "Auto Tool",
            "Automatically select tools",
            Category.PLAYER
        ) {});

        // WORLD
        register(new Module(
            "Waypoints",
            "Save locations",
            Category.WORLD
        ) {});

        // MISC
        register(new Module(
            "Notifications",
            "Display client notifications",
            Category.MISC
        ) {});
    }

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
            .filter(m -> m.getCategory() == category)
            .toList();
    }

    public Module getByName(String name) {
        return modules.stream()
            .filter(m -> m.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public void tick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}

