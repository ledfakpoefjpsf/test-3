
package nova.client.client.module;

// @name: MobEspSettings
// @type: Java Class
// @location: src/client/java/nova/client/client/module/MobEspSettings.java

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

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

    private static final Set<Entity> glowing =
        new HashSet<>();

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

        for (String id : MOBS) {
            if (ids.contains(id)) {
                selected.add(id);
            }
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            clear();
            return;
        }

        Set<Entity> matches = new HashSet<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity)
                || entity instanceof Player) {
                continue;
            }

            Identifier identifier =
                BuiltInRegistries.ENTITY_TYPE.getKey(
                    entity.getType()
                );

            if (identifier == null) {
                continue;
            }

            if (selected.contains(identifier.getPath())) {
                matches.add(entity);

                if (!glowing.contains(entity)) {
                    entity.setGlowingTag(true);
                    glowing.add(entity);
                }
            }
        }

        for (Entity entity : Set.copyOf(glowing)) {
            if (!matches.contains(entity)) {
                entity.setGlowingTag(false);
                glowing.remove(entity);
            }
        }
    }

    public static void clear() {
        for (Entity entity : glowing) {
            entity.setGlowingTag(false);
        }

        glowing.clear();
    }
}
