
package nova.client.client.module;

// @name: ModuleManager
// @type: Client Module Registry
// @location: src/client/java/nova/client/client/module/ModuleManager.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    private int afkTicks = 0;

    public void initialize() {

        // ================================
        // MOVEMENT
        // ================================

        register(new Module(
            "Auto Sprint",
            "Automatically sprint while moving forward",
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
            "Automatically walk forward",
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
                Minecraft.getInstance()
                    .options.keyUp.setDown(false);
            }
        });

        register(new Module(
            "Auto Sneak",
            "Automatically hold sneak",
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
                Minecraft.getInstance()
                    .options.keyShift.setDown(false);
            }
        });

        // ================================
        // RENDER
        // ================================

        register(new Module(
            "Player ESP",
            "Highlight players",
            Category.RENDER
        ) {
            // ESP renderer will be implemented later.
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

        register(new Module(
            "Coordinates HUD",
            "Display your XYZ coordinates",
            Category.RENDER
        ) {});

        register(new Module(
            "FPS HUD",
            "Display current FPS",
            Category.RENDER
        ) {});

        // ================================
        // PLAYER
        // ================================

        register(new Module(
            "Auto Respawn",
            "Automatically respawn after death",
            Category.PLAYER
        ) {
            private boolean handledDeath = false;

            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null || mc.level == null) {
                    handledDeath = false;
                    return;
                }

                if (!mc.player.isDeadOrDying()) {
                    handledDeath = false;
                    return;
                }

                if (!handledDeath) {
                    mc.player.respawn();
                    handledDeath = true;
                }
            }

            @Override
            public void onDisable() {
                handledDeath = false;
            }
        });

        // ================================
        // MISC
        // ================================

        register(new Module(
            "Macro Recorder",
            "Record and replay movement",
            Category.MISC
        ) {});

        register(new Module(
            "Anti AFK",
            "Periodically swing your hand while idle",
            Category.MISC
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    afkTicks = 0;
                    return;
                }

                afkTicks++;

                // Approximately once every 30 seconds.
                if (afkTicks >= 600) {
                    afkTicks = 0;

                    mc.player.swing(
                        net.minecraft.world.InteractionHand.MAIN_HAND
                    );
                }
            }

            @Override
            public void onDisable() {
                afkTicks = 0;
            }
        });

        register(new Module(
            "Notifications",
            "Show feedback when toggling modules",
            Category.MISC
        ) {});
    }

    // ================================
    // MODULE REGISTRATION
    // ================================

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

    public void toggleModule(Module module) {
        module.toggle();

        Module notifications =
            getByName("Notifications");

        Minecraft mc = Minecraft.getInstance();

        if (notifications != null
            && notifications.isEnabled()
            && mc.player != null) {

            mc.player.displayClientMessage(
                Component.literal(
                    "[Nova] " + module.getName()
                    + ": "
                    + (module.isEnabled() ? "ON" : "OFF")
                ),
                true
            );
        }
    }

    // ================================
    // MODULE TICK
    // ================================

    public void tick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}
