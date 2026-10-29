
package nova.client.client.module;

// @name: EspOverlaySettings
// @type: Client Settings
// @location: src/client/java/nova/client/client/module/EspOverlaySettings.java

import java.util.Locale;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EspOverlaySettings {

    private static boolean names = true;
    private static boolean health = true;
    private static boolean tracers = true;

    private static float tracerThickness = 5.5F;

    private EspOverlaySettings() {}

    public static boolean showNames() {
        return names;
    }

    public static boolean showHealth() {
        return health;
    }

    public static boolean showTracers() {
        return tracers;
    }

    public static void toggleNames() {
        names = !names;
    }

    public static void toggleHealth() {
        health = !health;
    }

    public static void toggleTracers() {
        tracers = !tracers;
    }

    public static float getTracerThickness() {
        return tracerThickness;
    }

    public static void setTracerThickness(float value) {
        tracerThickness = Math.max(
            1.0F,
            Math.min(10.0F, value)
        );
    }

    public static String getName(Entity entity) {
        return entity.getDisplayName().getString();
    }

    public static float getHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Math.max(0.0F, living.getHealth());
        }
        return 0.0F;
    }

    public static float getMaxHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Math.max(1.0F, living.getMaxHealth());
        }
        return 1.0F;
    }

    public static float getHealthPercent(Entity entity) {
        return Math.max(
            0.0F,
            Math.min(
                1.0F,
                getHealth(entity) / getMaxHealth(entity)
            )
        );
    }

    public static String getHealthText(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return "";
        }

        return String.format(
            Locale.ROOT,
            "%.0f HP",
            getHealth(entity)
        );
    }
}
