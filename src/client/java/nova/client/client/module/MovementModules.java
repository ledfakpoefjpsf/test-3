
package nova.client.client.module;

// @name: MovementModules
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/MovementModules.java

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class MovementModules {

    private static final Properties SETTINGS = new Properties();

    private MovementModules() {}

    // ========================================
    // SETTINGS STORAGE
    // ========================================

    private static Path file() {
        return Minecraft.getInstance()
            .gameDirectory.toPath()
            .resolve("config/nova/movement.properties");
    }

    public static void load() {
        try {
            if (Files.exists(file())) {
                try (var input = Files.newInputStream(file())) {
                    SETTINGS.load(input);
                }
            }
        } catch (Exception e) {
            System.err.println(
                "[Nova Movement] Load: " + e
            );
        }
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());

            try (var output = Files.newOutputStream(file())) {
                SETTINGS.store(
                    output,
                    "Nova Movement Settings"
                );
            }
        } catch (Exception e) {
            System.err.println(
                "[Nova Movement] Save: " + e
            );
        }
    }

    public static String get(
        String module,
        String key,
        String fallback
    ) {
        return SETTINGS.getProperty(
            module + "." + key,
            fallback
        );
    }

    public static double number(
        String module,
        String key,
        double fallback
    ) {
        try {
            return Double.parseDouble(
                get(
                    module,
                    key,
                    Double.toString(fallback)
                )
            );
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean bool(
        String module,
        String key,
        boolean fallback
    ) {
        return Boolean.parseBoolean(
            get(
                module,
                key,
                Boolean.toString(fallback)
            )
        );
    }

    public static void set(
        String module,
        String key,
        String value
    ) {
        SETTINGS.setProperty(
            module + "." + key,
            value
        );

        save();
    }

    // ========================================
    // MOVEMENT HELPERS
    // ========================================

    private static Minecraft ready() {
        Minecraft mc = Minecraft.getInstance();

        return mc.player != null
            && mc.level != null
            && mc.screen == null
            ? mc : null;
    }

    // FIXED: Minecraft 1.21.11 Vec2 uses
    // lengthSquared(), not lengthSqr().
    private static boolean moving(Minecraft mc) {
        if (mc.player == null
            || mc.player.input == null) {
            return false;
        }

        var movement =
            mc.player.input.getMoveVector();

        return movement.lengthSquared() > 0.001F;
    }

    private static void horizontal(
        Minecraft mc,
        double multiplier
    ) {
        Vec3 velocity =
            mc.player.getDeltaMovement();

        double x = velocity.x;
        double z = velocity.z;

        double length = Math.sqrt(
            x * x + z * z
        );

        if (length < 0.001) {
            return;
        }

        double desired = Math.min(
            1.2,
            length * multiplier
        );

        mc.player.setDeltaMovement(
            x / length * desired,
            velocity.y,
            z / length * desired
        );
    }

    private static void vertical(
        Minecraft mc,
        double amount
    ) {
        Vec3 velocity =
            mc.player.getDeltaMovement();

        mc.player.setDeltaMovement(
            velocity.x,
            amount,
            velocity.z
        );
    }

    // ========================================
    // REGISTER MOVEMENT MODULES
    // ========================================

    public static void register(
        ModuleManager manager
    ) {
        load();

        // ====================================
        // 1. SPEED
        // ====================================

        manager.register(new Module(
            "Speed",
            "Adjust horizontal movement speed",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null || !moving(mc)) {
                    return;
                }

                String mode = get(
                    "Speed",
                    "mode",
                    "Sprint"
                );

                if (mode.equals("Sprint")) {
                    mc.player.setSprinting(true);

                } else if (mode.equals("Custom")) {
                    horizontal(
                        mc,
                        number(
                            "Speed",
                            "multiplier",
                            1.5
                        )
                    );
                }
            }
        });

        // ====================================
        // 2. FLY
        // ====================================

        manager.register(new Module(
            "Fly",
            "Client-side flight movement",
            Category.MOVEMENT
        ) {
            private boolean oldFlying;
            private boolean oldMayfly;
            private float oldSpeed;

            @Override
            public void onEnable() {
                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null) {
                    return;
                }

                var abilities =
                    mc.player.getAbilities();

                oldFlying = abilities.flying;
                oldMayfly = abilities.mayfly;
                oldSpeed =
                    abilities.getFlyingSpeed();

                abilities.mayfly = true;
                abilities.flying = true;
            }

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                var abilities =
                    mc.player.getAbilities();

                abilities.mayfly = true;
                abilities.flying = true;

                abilities.setFlyingSpeed(
                    (float) (
                        number(
                            "Fly",
                            "speed",
                            1.0
                        ) * 0.05
                    )
                );
            }

            @Override
            public void onDisable() {
                Minecraft mc =
                    Minecraft.getInstance();

                if (mc.player == null) {
                    return;
                }

                var abilities =
                    mc.player.getAbilities();

                abilities.mayfly = oldMayfly;
                abilities.flying = oldFlying;

                abilities.setFlyingSpeed(
                    oldSpeed
                );
            }
        });

        // ====================================
        // 3. LONG JUMP
        // ====================================

        manager.register(new Module(
            "Long Jump",
            "Boost forward while jumping",
            Category.MOVEMENT
        ) {
            private boolean activated;

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                if (mc.player.onGround()) {
                    activated = false;
                }

                if (!activated
                    && mc.options.keyJump.isDown()
                    && moving(mc)
                    && mc.player.onGround()) {

                    mc.player.jumpFromGround();

                    horizontal(
                        mc,
                        number(
                            "Long Jump",
                            "boost",
                            2.0
                        )
                    );

                    activated = true;
                }
            }

            @Override
            public void onDisable() {
                activated = false;
            }
        });

        // ====================================
        // 4. HIGH JUMP
        // ====================================

        manager.register(new Module(
            "High Jump",
            "Increase jump height",
            Category.MOVEMENT
        ) {
            private boolean used;

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                if (!mc.options.keyJump.isDown()) {
                    used = false;
                }

                if (!used
                    && mc.player.onGround()
                    && mc.options.keyJump.isDown()) {

                    mc.player.jumpFromGround();

                    vertical(
                        mc,
                        number(
                            "High Jump",
                            "height",
                            0.65
                        )
                    );

                    used = true;
                }
            }

            @Override
            public void onDisable() {
                used = false;
            }
        });

        // ====================================
        // 5. BUNNY HOP
        // ====================================

        manager.register(new Module(
            "Bunny Hop",
            "Automatically chain jumps",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && moving(mc)
                    && mc.player.onGround()) {

                    mc.player.jumpFromGround();

                    horizontal(
                        mc,
                        number(
                            "Bunny Hop",
                            "boost",
                            1.15
                        )
                    );
                }
            }
        });

        // ====================================
        // 6. STEP
        // ====================================

        manager.register(new Module(
            "Step",
            "Assist climbing small obstacles",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null
                    || !moving(mc)
                    || !mc.player.onGround()
                    || !mc.player.horizontalCollision) {
                    return;
                }

                double height = number(
                    "Step",
                    "height",
                    1.0
                );

                if (height >= 1.0) {
                    mc.player.jumpFromGround();
                }
            }
        });

        // ====================================
        // 7. NO SLOW
        // ====================================

        manager.register(new Module(
            "No Slow",
            "Movement boost while using items",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.isUsingItem()
                    && moving(mc)) {

                    horizontal(
                        mc,
                        number(
                            "No Slow",
                            "boost",
                            1.25
                        )
                    );
                }
            }
        });

        // ====================================
        // 8. SPIDER
        // ====================================

        manager.register(new Module(
            "Spider",
            "Climb against walls",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.horizontalCollision
                    && moving(mc)) {

                    vertical(
                        mc,
                        number(
                            "Spider",
                            "speed",
                            0.2
                        )
                    );
                }
            }
        });

        // ====================================
        // 9. JESUS
        // ====================================

        manager.register(new Module(
            "Jesus",
            "Assist movement across water",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.isInWater()
                    && !mc.options.keyShift.isDown()) {

                    vertical(
                        mc,
                        number(
                            "Jesus",
                            "lift",
                            0.12
                        )
                    );
                }
            }
        });

        // ====================================
        // 10. PARKOUR
        // ====================================

        manager.register(new Module(
            "Parkour",
            "Automatically jump over obstacles",
            Category.MOVEMENT
        ) {
            private int cooldown;

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                if (cooldown > 0) {
                    cooldown--;
                }

                if (cooldown == 0
                    && mc.player.onGround()
                    && moving(mc)
                    && mc.player.horizontalCollision) {

                    mc.player.jumpFromGround();
                    cooldown = 8;
                }
            }

            @Override
            public void onDisable() {
                cooldown = 0;
            }
        });

        // ====================================
        // 11. SAFE WALK
        // ====================================

        manager.register(new Module(
            "Safe Walk",
            "Sneak while moving on the ground",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                if (mc.player.onGround()
                    && moving(mc)) {

                    mc.options.keyShift.setDown(true);

                } else {
                    mc.options.keyShift.setDown(false);
                }
            }

            @Override
            public void onDisable() {
                Minecraft.getInstance()
                    .options.keyShift.setDown(false);
            }
        });

        // ====================================
        // 12. NO FALL
        // ====================================

        manager.register(new Module(
            "No Fall",
            "Client-side fall assistance",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                if (mc.player.fallDistance
                    > number(
                        "No Fall",
                        "threshold",
                        3.0
                    )
                    && mc.player.getDeltaMovement().y
                        < -0.4) {

                    // Client-side velocity assistance.
                    // Server fall damage still applies.
                    vertical(mc, -0.35);
                }
            }
        });

        // ====================================
        // 13. FAST LADDER
        // ====================================

        manager.register(new Module(
            "Fast Ladder",
            "Climb ladders faster",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.onClimbable()
                    && mc.options.keyUp.isDown()) {

                    vertical(
                        mc,
                        number(
                            "Fast Ladder",
                            "speed",
                            0.3
                        )
                    );
                }
            }
        });

        // ====================================
        // 14. AIR JUMP
        // ====================================

        manager.register(new Module(
            "Air Jump",
            "Perform extra midair jumps",
            Category.MOVEMENT
        ) {
            private int jumps;
            private boolean previousJump;

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) {
                    return;
                }

                if (mc.player.onGround()) {
                    jumps = 0;
                }

                boolean down =
                    mc.options.keyJump.isDown();

                if (down
                    && !previousJump
                    && !mc.player.onGround()
                    && jumps < (int) number(
                        "Air Jump",
                        "jumps",
                        1
                    )) {

                    vertical(
                        mc,
                        number(
                            "Air Jump",
                            "height",
                            0.42
                        )
                    );

                    jumps++;
                }

                previousJump = down;
            }

            @Override
            public void onDisable() {
                jumps = 0;
                previousJump = false;
            }
        });

        // ====================================
        // 15. ELYTRA BOOST
        // ====================================

        manager.register(new Module(
            "Elytra Boost",
            "Accelerate while gliding",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null
                    || !mc.player.isFallFlying()
                    || !mc.player.getItemBySlot(
                        EquipmentSlot.CHEST
                    ).is(Items.ELYTRA)) {
                    return;
                }

                if (mc.options.keyUp.isDown()) {

                    Vec3 look =
                        mc.player.getLookAngle();

                    double boost = number(
                        "Elytra Boost",
                        "boost",
                        0.04
                    );

                    mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement()
                            .add(look.scale(boost))
                    );
                }
            }
        });
    }
}
