
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

    private final List<Module> modules = new ArrayList<>();

    private int afkTicks = 0;

    // ========================================
    // INITIALIZE ALL MODULES
    // ========================================

    public void initialize() {

        modules.clear();

        // ========================================
        // COMBAT
        // ========================================

        CombatModules.register(this);

        // ========================================
        // MOVEMENT
        // ========================================

        // Registers Speed, Fly, Long Jump,
        // High Jump, Bunny Hop, Step, No Slow,
        // Spider, Jesus, Parkour, Safe Walk,
        // No Fall, Fast Ladder, Air Jump,
        // and Elytra Boost.

        MovementModules.register(this);

        // ========================================
        // AUTO SPRINT
        // ========================================

        register(new Module(
            "Auto Sprint",
            "Automatically sprint while moving",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null
                    || mc.screen != null
                    || mc.player.input == null) {
                    return;
                }

                boolean forwardOnly = MovementModules.bool(
                    "Auto Sprint",
                    "forwardOnly",
                    true
                );

                boolean requireHunger = MovementModules.bool(
                    "Auto Sprint",
                    "requireHunger",
                    true
                );

                var input = mc.player.input.getMoveVector();

                boolean moving = forwardOnly
                    ? input.y > 0
                    : input.lengthSquared() > 0.001F;

                boolean enoughHunger = !requireHunger
                    || mc.player.getFoodData()
                        .getFoodLevel() > 6;

                if (moving && enoughHunger) {
                    mc.player.setSprinting(true);
                }
            }
        });

        // ========================================
        // AUTO WALK
        // ========================================

        register(new Module(
            "Auto Walk",
            "Automatically walk forward",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null
                    || mc.screen != null) {
                    return;
                }

                boolean stopSneaking = MovementModules.bool(
                    "Auto Walk",
                    "stopSneaking",
                    false
                );

                mc.options.keyUp.setDown(
                    !stopSneaking
                        || !mc.options.keyShift.isDown()
                );
            }

            @Override
            public void onDisable() {

                Minecraft.getInstance()
                    .options.keyUp.setDown(false);
            }
        });

        // ========================================
        // AUTO SNEAK
        // ========================================

        register(new Module(
            "Auto Sneak",
            "Automatically sneak",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null
                    || mc.screen != null) {
                    return;
                }

                boolean groundOnly = MovementModules.bool(
                    "Auto Sneak",
                    "groundOnly",
                    false
                );

                mc.options.keyShift.setDown(
                    !groundOnly
                        || mc.player.onGround()
                );
            }

            @Override
            public void onDisable() {

                Minecraft.getInstance()
                    .options.keyShift.setDown(false);
            }
        });

        // ========================================
        // AUTO JUMP
        // ========================================

        register(new Module(
            "Auto Jump",
            "Automatically jump while moving",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null
                    || mc.screen != null) {
                    return;
                }

                boolean movingOnly = MovementModules.bool(
                    "Auto Jump",
                    "movingOnly",
                    true
                );

                boolean moving = mc.player.input != null
                    && mc.player.input.getMoveVector()
                        .lengthSquared() > 0.001F;

                if (mc.player.onGround()
                    && (!movingOnly || moving)) {

                    mc.player.jumpFromGround();
                }
            }
        });

        // ========================================
        // RENDER
        // ========================================

        register(new Module(
            "Player ESP",
            "Highlight other players",
            Category.RENDER
        ) {});

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
            "Freecam",
            "Move the camera independently",
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
            "Display player coordinates",
            Category.RENDER
        ) {});

        register(new Module(
            "FPS HUD",
            "Display FPS",
            Category.RENDER
        ) {});

        register(new Module(
            "Armor HUD",
            "Display armor and durability",
            Category.RENDER
        ) {});

        register(new Module(
            "FOV Changer",
            "Customize field of view",
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
            "Hold C to zoom",
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
            "Brighten dark areas",
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
            "Disable fog rendering",
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

        // ========================================
        // PLAYER
        // ========================================

        register(new Module(
            "Auto Respawn",
            "Automatically respawn after death",
            Category.PLAYER
        ) {
            @Override
            public void onTick() {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player != null
                    && mc.player.isDeadOrDying()) {

                    mc.player.respawn();
                }
            }
        });

        // ========================================
        // MISC
        // ========================================

        register(new Module(
            "Macro Recorder",
            "Record, save and replay macros",
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
            "Perform periodic anti-AFK actions",
            Category.MISC
        ) {
            @Override
            public void onTick() {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null
                    || mc.screen != null) {
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
            "Display module toggle notifications",
            Category.MISC
        ) {});

        // ========================================
        // FINISHED
        // ========================================

        System.out.println(
            "[Nova Client] Registered "
                + modules.size()
                + " modules."
        );
    }

    // ========================================
    // REGISTER
    // ========================================

    public void register(Module module) {

        if (module == null) {
            return;
        }

        // Prevent duplicate registrations.
        if (getByName(module.getName()) != null) {
            System.out.println(
                "[Nova Client] Skipped duplicate: "
                    + module.getName()
            );
            return;
        }

        modules.add(module);
    }

    // ========================================
    // TICK
    // ========================================

    public void tick() {

        for (Module module : modules) {

            if (module.isEnabled()) {
                module.onTick();
            }
        }

        VisualModules.tickFov();
    }

    // ========================================
    // GET BY NAME
    // ========================================

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

    // ========================================
    // GET BY CATEGORY
    // ========================================

    public List<Module> getByCategory(
        Category category
    ) {

        List<Module> result = new ArrayList<>();

        for (Module module : modules) {

            if (module.getCategory() == category) {
                result.add(module);
            }
        }

        return result;
    }

    // ========================================
    // GET ALL MODULES
    // ========================================

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getAll() {
        return getModules();
    }

    // ========================================
    // TOGGLE MODULE
    // ========================================

    public void toggleModule(Module module) {

        if (module == null) {
            return;
        }

        module.toggle();

        Minecraft mc = Minecraft.getInstance();

        Module notifications =
            getByName("Notifications");

        if (mc.player != null
            && notifications != null
            && notifications.isEnabled()) {

            String status = module.isEnabled()
                ? "ON"
                : "OFF";

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
