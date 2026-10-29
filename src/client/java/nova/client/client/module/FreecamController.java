
package nova.client.client.module;

// @name: FreecamController
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/FreecamController.java

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import org.lwjgl.glfw.GLFW;

public final class FreecamController {

    private static boolean active = false;

    private static Vec3 position = Vec3.ZERO;

    private static float yaw;
    private static float pitch;

    private static double speed = 0.45D;

    private static long lastUpdate;

    private FreecamController() {}

    public static boolean isActive() {
        return active;
    }

    public static Vec3 getPosition() {
        return position;
    }

    public static float getYaw() {
        return yaw;
    }

    public static float getPitch() {
        return pitch;
    }

    public static double getSpeed() {
        return speed;
    }

    public static void setSpeed(double value) {
        speed = Math.max(0.05D, Math.min(3.0D, value));
    }

    public static void enable() {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        position = mc.player.getEyePosition();

        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();

        lastUpdate = System.nanoTime();
        active = true;
    }

    public static void disable() {
        active = false;
        lastUpdate = 0L;
    }

    public static void tick() {

        if (!active) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            disable();
            return;
        }

        long now = System.nanoTime();

        double delta = Math.min(
            0.1D,
            (now - lastUpdate) / 1_000_000_000.0D
        );

        lastUpdate = now;

        if (mc.screen != null) {
            return;
        }

        long window = mc.getWindow().handle();

        double forward = 0.0D;
        double strafe = 0.0D;
        double vertical = 0.0D;

        if (pressed(window, GLFW.GLFW_KEY_W)) {
            forward += 1.0D;
        }

        if (pressed(window, GLFW.GLFW_KEY_S)) {
            forward -= 1.0D;
        }

        if (pressed(window, GLFW.GLFW_KEY_D)) {
            strafe += 1.0D;
        }

        if (pressed(window, GLFW.GLFW_KEY_A)) {
            strafe -= 1.0D;
        }

        if (pressed(window, GLFW.GLFW_KEY_SPACE)) {
            vertical += 1.0D;
        }

        if (pressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)) {
            vertical -= 1.0D;
        }

        double radians = Math.toRadians(yaw);

        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double dx = -sin * forward + cos * strafe;
        double dz = cos * forward + sin * strafe;

        double length = Math.sqrt(
            dx * dx + dz * dz + vertical * vertical
        );

        if (length > 0.0D) {

            double movement = speed * 20.0D * delta;

            position = position.add(
                dx / length * movement,
                vertical / length * movement,
                dz / length * movement
            );
        }
    }

    public static void rotate(
        double mouseDeltaX,
        double mouseDeltaY
    ) {

        if (!active) {
            return;
        }

        yaw += (float) mouseDeltaX;
        pitch += (float) mouseDeltaY;

        pitch = Math.max(
            -90.0F,
            Math.min(90.0F, pitch)
        );
    }

    private static boolean pressed(
        long window,
        int key
    ) {

        return GLFW.glfwGetKey(window, key)
            == GLFW.GLFW_PRESS;
    }
}
