
package nova.client.client.module;

// @name: ModuleManager
// @type: Client Module Registry
// @location: src/client/java/nova/client/client/module/ModuleManager.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    private int afkTicks = 0;

    public void initialize() {

        if (!modules.isEmpty()) {
            return;
        }

        // ====================================
        // MOVEMENT
        // ====================================

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

        register(new Module(
            "Auto Jump",
            "Automatically jump while moving on the ground",
            Category.MOVEMENT
        ) {
            private boolean jumpedLastTick = false;

            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    jumpedLastTick = false;
                    return;
                }

                boolean moving =
                    mc.options.keyUp.isDown()
                    || mc.options.keyDown.isDown()
                    || mc.options.keyLeft.isDown()
                    || mc.options.keyRight.isDown();

                boolean shouldJump =
                    moving
                    && mc.player.onGround()
                    && !mc.player.isCrouching()
                    && !mc.player.isUsingItem();

                if (shouldJump && !jumpedLastTick) {
                    mc.player.jumpFromGround();
                }

                jumpedLastTick = shouldJump;
            }

            @Override
            public void onDisable() {
                jumpedLastTick = false;
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
            // Existing placeholder.
            // Proper ESP rendering comes later.
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
            "Display XYZ coordinates",
            Category.RENDER
        ) {});

        register(new Module(
            "FPS HUD",
            "Display current FPS",
            Category.RENDER
        ) {});

        register(new Module(
            "FOV Changer",
            "Change your normal FOV to 100 degrees",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                VisualModules.tickFov();
            }

            @Override
            public void onDisable() {
                VisualModules.tickFov();
            }
        });

        register(new Module(
            "Zoom",
            "Hold C to zoom in while enabled",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                VisualModules.tickFov();
            }

            @Override
            public void onDisable() {
                VisualModules.tickFov();
            }
        });

        register(new Module(
            "Fullbright",
            "Brighten dark areas using client night vision",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                VisualModules.tickFullbright();
            }

            @Override
            public void onDisable() {
                VisualModules.disableFullbright();
            }
        });

        register(new Module(
            "No Fog",
            "Toggle Minecraft's built-in fog rendering",
            Category.RENDER
        ) {
            @Override
            public void onEnable() {
                VisualModules.enableNoFog();
            }

            @Override
            public void onDisable() {
                VisualModules.disableNoFog();
            }
        });

        // ====================================
        // PLAYER
        // ====================================

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

        // ====================================
        // MISC
        // ====================================

        register(new Module(
            "Macro Recorder",
            "Record and replay movement macros",
            Category.MISC
        ) {});

        register(new Module(
            "Anti AFK",
            "Periodically swing your hand",
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

                if (afkTicks >= 600) {
                    afkTicks = 0;
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }

            @Override
            public void onDisable() {
                afkTicks = 0;
            }
        });

        register(new Module(
            "Notifications",
            "Show feedback when modules toggle",
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
    // TOGGLE + NOTIFICATIONS
    // ====================================

    public void toggleModule(Module module) {

        if (module == null) {
            return;
        }

        module.toggle();

        Module notifications = getByName("Notifications");

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

    // ====================================
    // MODULE TICK
    // ====================================

    public void tick() {

        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }

        // Restore original FOV when both FOV modules
        // are disabled.
        VisualModules.tickFov();
    }
}
