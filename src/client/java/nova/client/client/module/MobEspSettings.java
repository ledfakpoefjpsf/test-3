
package nova.client.client.module;

// @name: MobEspSettings
// @type: Client Mob ESP Settings
// @location: src/client/java/nova/client/client/module/MobEspSettings.java

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class MobEspSettings {

    public static final String[] MOBS = {
        "zombie", "husk", "drowned",
        "skeleton", "stray", "bogged",
        "creeper", "spider", "cave_spider",
        "enderman", "witch", "slime",
        "magma_cube", "phantom", "blaze",
        "ghast", "piglin", "zombified_piglin",
        "piglin_brute", "hoglin", "zoglin",
        "wither_skeleton", "guardian",
        "elder_guardian", "shulker",
        "silverfish", "endermite",
        "pillager", "vindicator", "evoker",
        "ravager", "breeze", "warden",
        "cow", "pig", "sheep", "chicken",
        "horse", "wolf", "cat", "fox",
        "rabbit", "bee", "goat",
        "frog", "turtle", "axolotl",
        "villager", "iron_golem",
        "snow_golem", "allay",
        "wither", "ender_dragon"
    };

    private static final Set<String> selected =
        new HashSet<>(Arrays.asList(
            "zombie",
            "skeleton",
            "creeper",
            "enderman",
            "wither",
            "ender_dragon"
        ));

    private MobEspSettings() {}

    public static boolean isSelected(String id) {
        return selected.contains(id);
    }

    public static void toggle(String id) {
        if (!selected.add(id)) {
            selected.remove(id);
        }
    }

    public static void selectAll() {
        selected.addAll(Arrays.asList(MOBS));
    }

    public static void selectNone() {
        selected.clear();
    }

    public static Set<String> getSelected() {
        return Set.copyOf(selected);
    }

    public static void setSelected(Set<String> ids) {
        selected.clear();

        if (ids == null) {
            return;
        }

        for (String id : MOBS) {
            if (ids.contains(id)) {
                selected.add(id);
            }
        }
    }

    public static boolean shouldGlow(Entity entity) {

        if (!(entity instanceof LivingEntity)
            || entity instanceof Player) {
            return false;
        }

        Identifier identifier =
            BuiltInRegistries.ENTITY_TYPE.getKey(
                entity.getType()
            );

        return identifier != null
            && selected.contains(identifier.getPath());
    }

    // Kept for compatibility with ModuleManager.
    // Actual ESP is handled by EntityGlowMixin.

    public static void tick() {}

    public static void clear() {}
}
