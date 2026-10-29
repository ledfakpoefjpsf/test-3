
package nova.client.client.module;

// @name: ModuleManager
// @type: Java Class
// @location: src/client/java/nova/client/client/module/ModuleManager.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import nova.client.client.macro.MacroEngine;

public final class ModuleManager {

    private final List<Module> modules =
        new ArrayList<>();

    private int afkTicks = 0;

    // =========================================
    // INITIALIZE ALL MODULES
    // =========================================

    public void initialize() {

        modules.clear();

        // =====================================
        // MOVEMENT
        // =====================================

        register(new Module(
            "Auto Sprint",
            "Automatically sprint while moving forward",
            Category.MOVEMENT
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                if (mc.player.input != null
                    && mc.player.input.getMoveVector().y > 0) {

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

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                mc.options.keyUp.setDown(true);
            }

            @Override
            public void onDisable() {

                Minecraft mc =
                    Minecraft.getInstance();

                mc.options.keyUp.setDown(false);
            }
        });

        register(new Module(
            "Auto Sneak",
            "Automatically sneak",
            Category.MOVEMENT
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                mc.options.keyShift.setDown(true);
            }

            @Override
            public void onDisable() {

                Minecraft mc =
                    Minecraft.getInstance();

                mc.options.keyShift.setDown(false);
            }
        });

        register(new Module(
            "Auto Jump",
            "Automatically jump while moving",
            Category.MOVEMENT
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                if (mc.player.onGround()
                    && mc.player.input != null
                    && mc.player.input.getMoveVector().lengthSquared() > 0) {

                    mc.player.jumpFromGround();
                }
            }
        });

        // =====================================
        // RENDER
        // =====================================

        register(new Module(
            "Player ESP",
            "Highlight other players with glowing outlines",
            Category.RENDER
        ) {});

        register(new Module(
            "Mob ESP",
            "Highlight selected mobs with glowing outlines",
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

        // =====================================
        // FREECAM
        // =====================================

        register(new Module(
            "Freecam",
            "Fly the camera without moving your player",
            Category.RENDER
        ) {

            @Override
            public void onEnable() {
                FreecamController.enable();
            }

            @Override
            public void onTick() {
                FreecamController.tick();
            }

            @Override
            public void onDisable() {
                FreecamController.disable();
            }
        });

        register(new Module(
            "Coordinates HUD",
            "Display your current coordinates",
            Category.RENDER
        ) {});

        register(new Module(
            "FPS HUD",
            "Display your current FPS",
            Category.RENDER
        ) {});

        register(new Module(
            "Armor HUD",
            "Display equipped armor and durability",
            Category.RENDER
        ) {});

        register(new Module(
            "FOV Changer",
            "Change your field of view",
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
            "Hold C to zoom in",
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
            "Brighten dark areas using the lightmap",
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
            "Toggle Minecraft's fog rendering",
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

        // =====================================
        // PLAYER
        // =====================================

        register(new Module(
            "Auto Respawn",
            "Automatically respawn after dying",
            Category.PLAYER
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player != null
                    && mc.player.isDeadOrDying()) {

                    mc.player.respawn();
                }
            }
        });

        // =====================================
        // MISC
        // =====================================

        register(new Module(
            "Macro Recorder",
            "Record and replay movement macros",
            Category.MISC
        ) {

            @Override
            public void onDisable() {

                MacroEngine.stop(
                    Minecraft.getInstance()
                );
            }
        });

        register(new Module(
            "Anti AFK",
            "Periodically swing to prevent inactivity",
            Category.MISC
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                afkTicks++;

                if (afkTicks >= 600) {

                    mc.player.swing(
                        InteractionHand.MAIN_HAND
                    );

                    afkTicks = 0;
                }
            }

            @Override
            public void onDisable() {
                afkTicks = 0;
            }
        });

        register(new Module(
            "Notifications",
            "Show module toggle notifications",
            Category.MISC
        ) {});

        System.out.println(
            "[Nova Client] Registered "
                + modules.size()
                + " modules."
        );
    }

    // =========================================
    // REGISTER MODULE
    // =========================================

    public void register(Module module) {

        if (module == null) {
            return;
        }

        modules.add(module);
    }

    // =========================================
    // TICK ENABLED MODULES
    // =========================================

    public void tick() {

        for (Module module : modules) {

            if (module.isEnabled()) {
                module.onTick();
            }
        }

        VisualModules.tickFov();
    }

    // =========================================
    // GET MODULE BY NAME
    // =========================================

    public Module getByName(String name) {

        if (name == null) {
            return null;
        }

        for (Module module : modules) {

            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }

        return null;
    }

    // =========================================
    // GET MODULES BY CATEGORY
    // =========================================

    public List<Module> getByCategory(Category category) {

        List<Module> result =
            new ArrayList<>();

        for (Module module : modules) {

            if (module.getCategory() == category) {
                result.add(module);
            }
        }

        return result;
    }

    // =========================================
    // GET ALL MODULES
    // =========================================

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getAll() {
        return getModules();
    }

    // =========================================
    // TOGGLE MODULE
    // =========================================

    public void toggleModule(Module module) {

        if (module == null) {
            return;
        }

        module.toggle();

        Minecraft mc =
            Minecraft.getInstance();

        Module notifications =
            getByName("Notifications");

        if (mc.player != null
            && notifications != null
            && notifications.isEnabled()) {

            String status =
                module.isEnabled() ? "ON" : "OFF";

            mc.player.displayClientMessage(
                Component.literal(
                    "[Nova] "
                        + module.getName()
                        + ": "
                        + status
                ),
                true
            );
        }
    }
}
