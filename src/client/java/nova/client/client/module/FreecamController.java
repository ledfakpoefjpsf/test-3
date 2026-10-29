
package nova.client.client.module;

// @name: FreecamController
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/FreecamController.java
//
// Freecam movement adapted from Meteor Client:
// https://github.com/MeteorDevelopment/meteor-client
// Copyright (c) Meteor Development
// Meteor Client is licensed under GPL-3.0.

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.phys.Vec3;

import org.lwjgl.glfw.GLFW;

public final class FreecamController {

    private static boolean active = false;

    private static Vec3 position = Vec3.ZERO;
    private static Vec3 previousPosition = Vec3.ZERO;

    private static float yaw;
    private static float pitch;

    private static float previousYaw;
    private static float previousPitch;

    private static double speed = 1.0D;

    private static CameraType oldPerspective;

    private static long lastTickTime = System.nanoTime();

    private FreecamController() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void enable() {
        Minecraft mc = Minecraft.getInstance();

        if (active || mc.player == null || mc.level == null) {
            return;
        }

        oldPerspective = mc.options.getCameraType();

        Vec3 start = mc.gameRenderer.getMainCamera().getPosition();

        position = start;
        previousPosition = start;

        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();

        if (oldPerspective == CameraType.THIRD_PERSON_FRONT) {
            yaw += 180.0F;
            pitch = -pitch;
        }

        previousYaw = yaw;
        previousPitch = pitch;

        mc.options.setCameraType(CameraType.FIRST_PERSON);

        active = true;
        lastTickTime = System.nanoTime();

        clearMovementKeys(mc);
    }

    public static void disable() {
        if (!active) {
            return;
        }

        active = false;

        Minecraft mc = Minecraft.getInstance();

        if (oldPerspective != null) {
            mc.options.setCameraType(oldPerspective);
        }

        oldPerspective = null;

        clearMovementKeys(mc);
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

        if (mc.screen != null) {
            previousPosition = position;
            previousYaw = yaw;
            previousPitch = pitch;
            lastTickTime = System.nanoTime();
            clearMovementKeys(mc);
            return;
        }

        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }

        long window = mc.getWindow().handle();

        boolean forward = down(window, GLFW.GLFW_KEY_W);
        boolean backward = down(window, GLFW.GLFW_KEY_S);
        boolean left = down(window, GLFW.GLFW_KEY_A);
        boolean right = down(window, GLFW.GLFW_KEY_D);
        boolean up = down(window, GLFW.GLFW_KEY_SPACE);
        boolean down = down(window, GLFW.GLFW_KEY_LEFT_SHIFT);
        boolean sprint = down(window, GLFW.GLFW_KEY_LEFT_CONTROL);

        previousPosition = position;
        previousYaw = yaw;
        previousPitch = pitch;

        double movementSpeed = speed * (sprint ? 1.0D : 0.5D);

        Vec3 forwardVector = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 rightVector = Vec3.directionFromRotation(0.0F, yaw + 90.0F);

        double x = 0.0D;
        double y = 0.0D;
        double z = 0.0D;

        int forwardInput = (forward ? 1 : 0) - (backward ? 1 : 0);
        int rightInput = (right ? 1 : 0) - (left ? 1 : 0);

        if (forwardInput != 0) {
            x += forwardVector.x * forwardInput;
            z += forwardVector.z * forwardInput;
        }

        if (rightInput != 0) {
            x += rightVector.x * rightInput;
            z += rightVector.z * rightInput;
        }

        double horizontalLength = Math.sqrt(x * x + z * z);

        if (horizontalLength > 1.0D) {
            x /= horizontalLength;
            z /= horizontalLength;
        }

        if (up) {
            y += 1.0D;
        }

        if (down) {
            y -= 1.0D;
        }

        position = position.add(
            x * movementSpeed,
            y * movementSpeed,
            z * movementSpeed
        );

        lastTickTime = System.nanoTime();

        clearMovementKeys(mc);
    }

    private static boolean down(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public static void clearMovementKeys(Minecraft mc) {
        if (mc == null) {
            return;
        }

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(false);
        mc.options.keySprint.setDown(false);
    }

    public static void rotate(double deltaX, double deltaY) {
        if (!active) {
            return;
        }

        previousYaw = yaw;
        previousPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
    }

    public static void adjustSpeed(double scrollAmount) {
        if (!active) {
            return;
        }

        speed += scrollAmount * 0.15D * speed;
        speed = Math.max(0.1D, Math.min(10.0D, speed));
    }

    private static float partialTick() {
        double elapsed = (System.nanoTime() - lastTickTime) / 50_000_000.0D;
        return (float) Math.max(0.0D, Math.min(1.0D, elapsed));
    }

    public static Vec3 getPosition() {
        float delta = partialTick();

        return new Vec3(
            previousPosition.x + (position.x - previousPosition.x) * delta,
            previousPosition.y + (position.y - previousPosition.y) * delta,
            previousPosition.z + (position.z - previousPosition.z) * delta
        );
    }

    public static float getYaw() {
        float delta = partialTick();
        return previousYaw + (yaw - previousYaw) * delta;
    }

    public static float getPitch() {
        float delta = partialTick();
        return previousPitch + (pitch - previousPitch) * delta;
    }
}
