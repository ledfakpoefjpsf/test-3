
package nova.client.client.module;

// @name: MovementModules
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/MovementModules.java

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class MovementModules {

    private static final Properties SETTINGS = new Properties();

    private MovementModules() {}

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config/nova/movement.properties");
    }

    public static void load() {
        try {
            SETTINGS.clear();
            if (Files.exists(file())) {
                try (var in = Files.newInputStream(file())) {
                    SETTINGS.load(in);
                }
            }
        } catch (Exception e) {
            System.err.println("[Nova Movement] " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            try (var out = Files.newOutputStream(file())) {
                SETTINGS.store(out, "Nova Movement Settings");
            }
        } catch (Exception e) {
            System.err.println("[Nova Movement] " + e);
        }
    }

    public static String get(String module, String key, String fallback) {
        return SETTINGS.getProperty(module + "." + key, fallback);
    }

    public static double number(String module, String key, double fallback) {
        try {
            return Double.parseDouble(get(module, key, "" + fallback));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean bool(String module, String key, boolean fallback) {
        return Boolean.parseBoolean(get(module, key, "" + fallback));
    }

    public static void set(String module, String key, String value) {
        SETTINGS.setProperty(module + "." + key, value);
        save();
    }

    private static Minecraft ready() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.level != null
            && mc.screen == null ? mc : null;
    }

    private static boolean moving(Minecraft mc) {
        return mc.player.input != null
            && mc.player.input.getMoveVector().lengthSquared() > 0.001F;
    }

    // Convert player movement input to a horizontal world direction.
    private static Vec3 direction(Minecraft mc) {
        Vec2 input = mc.player.input.getMoveVector();

        double forward = input.y;
        double sideways = input.x;
        double yaw = Math.toRadians(mc.player.getYRot());

        double x = -Math.sin(yaw) * forward
            + Math.cos(yaw) * sideways;
        double z = Math.cos(yaw) * forward
            + Math.sin(yaw) * sideways;

        double length = Math.sqrt(x * x + z * z);
        if (length < 0.001) return Vec3.ZERO;

        return new Vec3(x / length, 0, z / length);
    }

    private static void setHorizontal(Minecraft mc, double speed) {
        Vec3 dir = direction(mc);
        if (dir.lengthSqr() < 0.001) return;

        Vec3 v = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(
            dir.x * speed, v.y, dir.z * speed
        );
    }

    private static void setVertical(Minecraft mc, double speed) {
        Vec3 v = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(v.x, speed, v.z);
    }

    private static boolean edgeAhead(Minecraft mc, double distance) {
        Vec3 dir = direction(mc);
        if (dir.lengthSqr() < 0.001) return false;

        Vec3 ahead = mc.player.position()
            .add(dir.scale(distance));

        BlockPos below = BlockPos.containing(
            ahead.x,
            mc.player.getBoundingBox().minY - 0.15,
            ahead.z
        );

        return mc.level.getBlockState(below)
            .getCollisionShape(mc.level, below).isEmpty();
    }

    private static void register(
        ModuleManager manager,
        String name,
        String description,
        java.util.function.Consumer<Minecraft> action
    ) {
        manager.register(new Module(
            name, description, Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc != null) action.accept(mc);
            }
        });
    }

    public static void register(ModuleManager manager) {
        load();

        // SPEED
        register(manager, "Speed", "Adjust movement speed", mc -> {
            if (!moving(mc)) return;

            String mode = get("Speed", "mode", "Custom");

            if (mode.equals("Sprint")) {
                mc.player.setSprinting(true);
            } else if (mode.equals("Custom")) {
                if (!bool("Speed", "groundOnly", false)
                    || mc.player.onGround()) {
                    setHorizontal(mc, number("Speed", "speed", 0.3));
                }
            }
        });

        // FLY
        manager.register(new Module(
            "Fly", "Directional client-side flight",
            Category.MOVEMENT
        ) {
            private boolean oldNoGravity;
            private boolean saved;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!saved) {
                    oldNoGravity = mc.player.isNoGravity();
                    saved = true;
                }

                mc.player.setNoGravity(true);

                double speed = number("Fly", "speed", 0.4);

                if (moving(mc)) {
                    setHorizontal(mc, speed);
                } else {
                    Vec3 v = mc.player.getDeltaMovement();
                    mc.player.setDeltaMovement(0, v.y, 0);
                }

                double vertical = 0;

                if (mc.options.keyJump.isDown()) {
                    vertical = number("Fly", "vertical", 0.35);
                } else if (mc.options.keyShift.isDown()) {
                    vertical = -number("Fly", "vertical", 0.35);
                }

                setVertical(mc, vertical);
                mc.player.fallDistance = 0;
            }

            @Override
            public void onDisable() {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && saved) {
                    mc.player.setNoGravity(oldNoGravity);
                }
                saved = false;
            }
        });

        // LONG JUMP
        manager.register(new Module(
            "Long Jump", "Boost horizontal jump distance",
            Category.MOVEMENT
        ) {
            private boolean used;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!mc.options.keyJump.isDown()) used = false;

                if (!used && moving(mc)
                    && mc.player.onGround()
                    && mc.options.keyJump.isDown()) {

                    mc.player.jumpFromGround();
                    setHorizontal(mc, number("Long Jump", "speed", 0.75));
                    setVertical(mc, number("Long Jump", "height", 0.42));
                    used = true;
                }

                if (used && !mc.player.onGround() && moving(mc)) {
                    setHorizontal(mc, number("Long Jump", "speed", 0.75));
                }
            }

            @Override
            public void onDisable() {
                used = false;
            }
        });

        // HIGH JUMP
        manager.register(new Module(
            "High Jump", "Increase jump height",
            Category.MOVEMENT
        ) {
            private boolean used;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!mc.options.keyJump.isDown()) used = false;

                if (!used && mc.player.onGround()
                    && mc.options.keyJump.isDown()) {
                    mc.player.jumpFromGround();
                    setVertical(mc, number("High Jump", "height", 0.7));
                    used = true;
                }
            }

            @Override
            public void onDisable() {
                used = false;
            }
        });

        // BUNNY HOP
        register(manager, "Bunny Hop", "Automatically chain jumps", mc -> {
            if (moving(mc) && mc.player.onGround()) {
                mc.player.jumpFromGround();
                setHorizontal(mc, number("Bunny Hop", "speed", 0.3));
            }
        });

        // STEP - jump-based step assistance, not collision-height modification
        register(manager, "Step", "Climb obstacles automatically", mc -> {
            if (moving(mc) && mc.player.onGround()
                && mc.player.horizontalCollision) {
                mc.player.jumpFromGround();
                setVertical(mc, number("Step", "height", 0.42));
            }
        });

        // NO SLOW - client movement compensation
        register(manager, "No Slow", "Compensate for item-use slowdown", mc -> {
            if (moving(mc) && mc.player.isUsingItem()) {
                setHorizontal(mc, number("No Slow", "speed", 0.22));
            }
        });

        // SPIDER
        register(manager, "Spider", "Climb vertical walls", mc -> {
            if (moving(mc) && mc.player.horizontalCollision) {
                setVertical(mc, number("Spider", "speed", 0.25));
            }
        });

        // JESUS - water surface lift, not true solid-water collision
        register(manager, "Jesus", "Stay near the water surface", mc -> {
            if (mc.player.isInWater()
                && !mc.options.keyShift.isDown()) {
                setVertical(mc, number("Jesus", "lift", 0.15));
                if (moving(mc)) {
                    setHorizontal(mc, number("Jesus", "speed", 0.2));
                }
            }
        });

        // PARKOUR - jump before gaps
        manager.register(new Module(
            "Parkour", "Automatically jump near ledges",
            Category.MOVEMENT
        ) {
            private int cooldown;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;
                if (cooldown > 0) cooldown--;

                if (cooldown == 0 && moving(mc)
                    && mc.player.onGround()
                    && edgeAhead(mc, number("Parkour", "distance", 0.8))) {
                    mc.player.jumpFromGround();
                    cooldown = 8;
                }
            }

            @Override
            public void onDisable() {
                cooldown = 0;
            }
        });

        // SAFE WALK - sneak only near edges
        manager.register(new Module(
            "Safe Walk", "Avoid walking off ledges",
            Category.MOVEMENT
        ) {
            private boolean ownsSneak;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) {
                    release();
                    return;
                }

                boolean edge = moving(mc)
                    && mc.player.onGround()
                    && edgeAhead(mc, number("Safe Walk", "distance", 0.65));

                if (edge) {
                    if (!mc.options.keyShift.isDown()) {
                        mc.options.keyShift.setDown(true);
                        ownsSneak = true;
                    }
                } else {
                    release();
                }
            }

            private void release() {
                if (ownsSneak) {
                    Minecraft.getInstance()
                        .options.keyShift.setDown(false);
                    ownsSneak = false;
                }
            }

            @Override
            public void onDisable() {
                release();
            }
        });

        // NO FALL - reduces local falling speed only
        register(manager, "No Fall", "Limit downward falling speed", mc -> {
            if (mc.player.fallDistance
                > number("No Fall", "threshold", 3.0)
                && mc.player.getDeltaMovement().y < -0.3) {
                setVertical(mc, -number("No Fall", "fallSpeed", 0.25));
            }
        });

        // FAST LADDER
        register(manager, "Fast Ladder", "Climb ladders faster", mc -> {
            if (mc.player.onClimbable()
                && mc.options.keyUp.isDown()) {
                setVertical(mc, number("Fast Ladder", "speed", 0.35));
            }
        });

        // AIR JUMP
        manager.register(new Module(
            "Air Jump", "Jump multiple times in midair",
            Category.MOVEMENT
        ) {
            private int jumps;
            private boolean previous;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (mc.player.onGround()) jumps = 0;

                boolean pressed = mc.options.keyJump.isDown();

                if (pressed && !previous && !mc.player.onGround()
                    && jumps < number("Air Jump", "jumps", 2)) {
                    setVertical(mc, number("Air Jump", "height", 0.5));
                    jumps++;
                }

                previous = pressed;
            }

            @Override
            public void onDisable() {
                jumps = 0;
                previous = false;
            }
        });

        // ELYTRA BOOST
        register(manager, "Elytra Boost", "Accelerate while gliding", mc -> {
            if (!mc.player.isFallFlying()) return;

            if (mc.options.keyUp.isDown()) {
                double boost = number("Elytra Boost", "boost", 0.06);
                double max = number("Elytra Boost", "maxSpeed", 2.0);

                Vec3 velocity = mc.player.getDeltaMovement()
                    .add(mc.player.getLookAngle().scale(boost));

                if (velocity.length() > max) {
                    velocity = velocity.normalize().scale(max);
                }

                mc.player.setDeltaMovement(velocity);
            }
        });
    }
}
