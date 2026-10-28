
package nova.client.client.module;

// @name: VisualModules
// @type: Client Visual Feature Controller
// @location: src/client/java/nova/client/client/module/VisualModules.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;

import nova.client.client.NovaClient;

import org.lwjgl.glfw.GLFW;

public final class VisualModules {

    private static final int CHANGED_FOV = 100;
    private static final int ZOOM_FOV = 30;

    private static Integer originalFov = null;
    private static boolean fogChanged = false;

    private VisualModules() {}

    private static boolean enabled(String name) {
        Module module = NovaClient.MODULES.getByName(name);
        return module != null && module.isEnabled();
    }

    public static void tickFov() {

        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.options == null) {
            return;
        }

        boolean fovChanger = enabled("FOV Changer");
        boolean zoom = enabled("Zoom");

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

    // Fullbright is now rendered by LightTextureMixin.
    // These methods remain so ModuleManager still compiles.

    public static void tickFullbright() {}

    public static void disableFullbright() {}

    public static void enableNoFog() {

        if (fogChanged) {
            return;
        }

        boolean fogNowEnabled = FogRenderer.toggleFog();

        if (!fogNowEnabled) {
            fogChanged = true;
        } else {
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
