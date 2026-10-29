
package nova.client.client.module;

// @name: ModuleManager
// @type: Client Java Class
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
        // COMBAT
        // =====================================
        CombatModules.register(this);

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

                if (mc.player.onGround()) {
                    mc.player.jumpFromGround();
                }
            }
        });

        register(new Module(
            "Step",
            "Automatically step over small obstacles",
            Category.MOVEMENT
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null) {
                    return;
                }

                mc.player.setMaxUpStep(1.0f);
            }

            @Override
            public void onDisable() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player != null) {
                    mc.player.setMaxUpStep(0.6f);
                }
            }
        });

        // =====================================
        // RENDER
        // =====================================

        register(new Module(
            "Fullbright",
            "Makes dark areas easier to see",
            Category.RENDER
        ) {});

        register(new Module(
            "Entity ESP",
            "Highlights entities through walls",
            Category.RENDER
        ) {});

        register(new Module(
            "Block ESP",
            "Highlights selected blocks",
            Category.RENDER
        ) {});

        register(new Module(
            "Tracers",
            "Draws lines toward entities",
            Category.RENDER
        ) {});

        register(new Module(
            "Name Tags",
            "Displays enhanced entity name tags",
            Category.RENDER
        ) {});

        register(new Module(
            "Freecam",
            "Move your camera independently",
            Category.RENDER
        ) {});

        register(new Module(
            "Minimap",
            "Displays a small map of your surroundings",
            Category.RENDER
        ) {});

        register(new Module(
            "Entity Radar",
            "Shows nearby entities on a radar",
            Category.RENDER
        ) {});

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

        register(new Module(
            "Auto Eat",
            "Automatically eat food when hungry",
            Category.PLAYER
        ) {

            @Override
            public void onTick() {

                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                if (mc.player.getFoodData()
                        .getFoodLevel() < 14) {

                    if (mc.player.getMainHandItem()
                            .getFoodProperties(mc.player) != null) {

                        mc.options.keyUse.setDown(true);
                    }
                }
            }

            @Override
            public void onDisable() {

                Minecraft.getInstance()
                    .options.keyUse.setDown(false);
            }
        });

        register(new Module(
            "Auto Fish",
            "Automatically use a fishing rod",
            Category.PLAYER
        ) {});

        // =====================================
        // MISC
        // =====================================

        register(new Module(
            "Anti AFK",
            "Prevents being marked as inactive",
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

                if (afkTicks >= 200) {

                    afkTicks = 0;

                    mc.player.swing(
                        InteractionHand.MAIN_HAND
                    );
                }
            }
        });

        register(new Module(
            "Coordinates",
            "Displays player coordinates",
            Category.MISC
        ) {});

        register(new Module(
            "FPS Counter",
            "Displays current FPS",
            Category.MISC
        ) {});

        register(new Module(
            "CPS Counter",
            "Displays clicks per second",
            Category.MISC
        ) {});

        register(new Module(
            "Macros",
            "Runs configured macros",
            Category.MISC
        ) {

            @Override
            public void onTick() {

                MacroEngine.tick();
            }
        });
    }

    // =========================================
    // MODULE MANAGEMENT
    // =========================================

    public void register(Module module) {

        if (module == null) {
            return;
        }

        modules.add(module);
    }

    public List<Module> getModules() {

        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(
        Category category
    ) {

        List<Module> result =
            new ArrayList<>();

        for (Module module : modules) {

            if (module.getCategory() == category) {
                result.add(module);
            }
        }

        return result;
    }

    public Module getByName(String name) {

        for (Module module : modules) {

            if (module.getName()
                    .equalsIgnoreCase(name)) {

                return module;
            }
        }

        return null;
    }

    public void tick() {

        for (Module module : modules) {

            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void disableAll() {

        for (Module module : modules) {

            if (module.isEnabled()) {
                module.setEnabled(false);
            }
        }
    }
}
