
package nova.client.client.module;

// @name: EspOverlaySettings
// @type: Client Settings
// @location: src/client/java/nova/client/client/module/EspOverlaySettings.java

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EspOverlaySettings {

    private static boolean names = true;
    private static boolean health = true;
    private static boolean tracers = false;

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

    public static void setNames(boolean enabled) {
        names = enabled;
    }

    public static void setHealth(boolean enabled) {
        health = enabled;
    }

    public static void setTracers(boolean enabled) {
        tracers = enabled;
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

    public static String getName(Entity entity) {
        return entity.getDisplayName().getString();
    }

    public static float getHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Math.max(0, living.getHealth());
        }

        return 0;
    }

    public static float getMaxHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Math.max(1, living.getMaxHealth());
        }

        return 1;
    }

    public static float getHealthPercent(Entity entity) {
        return Math.min(
            1,
            getHealth(entity) / getMaxHealth(entity)
        );
    }

    public static String getHealthText(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return "";
        }

        return String.format(
            java.util.Locale.ROOT,
            "%.1f / %.1f HP",
            getHealth(entity),
            getMaxHealth(entity)
        );
    }
}
