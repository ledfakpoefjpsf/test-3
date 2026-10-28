
package nova.client.client.module;

// @name: ModuleManager
// @type: Client Module Registry
// @location: src/client/java/nova/client/client/module/ModuleManager.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void initialize() {

        // ====================================
        // MOVEMENT
        // ====================================

        register(new Module(
            "Auto Sprint",
            "Automatically sprint while walking forward",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                if (mc.options.keyUp.isDown()
                    && !mc.player.isCrouching()
                    && !mc.player.isUsingItem()) {

                    mc.player.setSprinting(true);
                }
            }
        });

        register(new Module(
            "Auto Walk",
            "Automatically hold the forward movement key",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player != null && mc.screen == null) {
                    mc.options.keyUp.setDown(true);
                }
            }

            @Override
            public void onDisable() {
                Minecraft mc = Minecraft.getInstance();

                mc.options.keyUp.setDown(false);
            }
        });

        register(new Module(
            "Auto Sneak",
            "Automatically hold the sneak key",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player != null && mc.screen == null) {
                    mc.options.keyShift.setDown(true);
                }
            }

            @Override
            public void onDisable() {
                Minecraft mc = Minecraft.getInstance();

                mc.options.keyShift.setDown(false);
            }
        });

        // ====================================
        // RENDER
        // ====================================

        register(new Module(
            "Player ESP",
            "Highlight other players",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                // Rendering fix comes later.
            }
        });

        register(new Module(
            "Mob ESP",
            "Highlight selected mobs",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                MobEspSettings.tick();
            }

            @Override
            public void onDisable() {
                MobEspSettings.clear();
            }
        });

        // ====================================
        // MISC
        // ====================================

        register(new Module(
            "Macro Recorder",
            "Record and replay movement macros",
            Category.MISC
        ) {});

    }

    // ====================================
    // MODULE REGISTRATION
    // ====================================

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
            .filter(module -> module.getCategory() == category)
            .toList();
    }

    public Module getByName(String name) {
        return modules.stream()
            .filter(module ->
                module.getName().equalsIgnoreCase(name)
            )
            .findFirst()
            .orElse(null);
    }

    // ====================================
    // MODULE TICK
    // ====================================

    public void tick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}
