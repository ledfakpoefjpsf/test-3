
package nova.client.client.module;

// @name: VisualModules
// @type: Client Visual Feature Controller
// @location: src/client/java/nova/client/client/module/VisualModules.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import nova.client.client.NovaClient;

import org.lwjgl.glfw.GLFW;

public final class VisualModules {

    private static final int CHANGED_FOV = 100;
    private static final int ZOOM_FOV = 30;

    private static Integer originalFov = null;

    private static boolean nightVisionApplied = false;
    private static boolean fogChanged = false;

    private VisualModules() {}

    // ====================================
    // MODULE STATE
    // ====================================

    private static boolean enabled(String name) {
        Module module = NovaClient.MODULES.getByName(name);
        return module != null && module.isEnabled();
    }

    // ====================================
    // FOV CHANGER + ZOOM
    // ====================================

    public static void tickFov() {

        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.options == null) {
            return;
        }

        boolean fovChanger = enabled("FOV Changer");
        boolean zoom = enabled("Zoom");

        // Neither module enabled: restore original FOV.
        if (!fovChanger && !zoom) {

            if (originalFov != null) {
                mc.options.fov().set(originalFov);
                originalFov = null;
            }

            return;
        }

        if (originalFov == null) {
            originalFov = mc.options.fov().get();
        }

        int target = fovChanger
            ? CHANGED_FOV
            : originalFov;

        // Zoom only activates while C is held.
        if (zoom
            && mc.player != null
            && mc.screen == null
            && mc.getWindow() != null) {

            long window = mc.getWindow().handle();

            boolean holdingC =
                GLFW.glfwGetKey(
                    window,
                    GLFW.GLFW_KEY_C
                ) == GLFW.GLFW_PRESS;

            if (holdingC) {
                target = ZOOM_FOV;
            }
        }

        if (mc.options.fov().get() != target) {
            mc.options.fov().set(target);
        }
    }

    // ====================================
    // FULLBRIGHT
    // ====================================

    public static void tickFullbright() {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            nightVisionApplied = false;
            return;
        }

        MobEffectInstance current =
            mc.player.getEffect(MobEffects.NIGHT_VISION);

        // Do not replace a legitimate Night Vision effect.
        if (current != null && !nightVisionApplied) {
            return;
        }

        // Refresh before the effect expires.
        if (current == null || current.getDuration() < 100) {

            mc.player.addEffect(
                new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    400,
                    0,
                    true,
                    false,
                    false
                )
            );

            nightVisionApplied = true;
        }
    }

    public static void disableFullbright() {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && nightVisionApplied) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }

        nightVisionApplied = false;
    }

    // ====================================
    // NO FOG
    // ====================================

    public static void enableNoFog() {

        if (fogChanged) {
            return;
        }

        // toggleFog() returns the new fog-enabled state.
        // Only record a change if fog was turned off.
        boolean fogNowEnabled = FogRenderer.toggleFog();

        if (!fogNowEnabled) {
            fogChanged = true;
        } else {
            // Fog was already disabled. Restore that state.
            FogRenderer.toggleFog();
        }
    }

    public static void disableNoFog() {

        if (fogChanged) {
            FogRenderer.toggleFog();
            fogChanged = false;
        }
    }
}
