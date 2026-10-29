
package nova.client.client.render;

// @name: CombatOverlayRenderer
// @type: Client World Renderer
// @location: src/client/java/nova/client/client/render/CombatOverlayRenderer.java

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.phys.*;

import nova.client.client.NovaClient;
import nova.client.client.module.CombatSettings;

public final class CombatOverlayRenderer {

    private CombatOverlayRenderer() {}

    public static void render(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        var hit = NovaClient.MODULES.getByName("Hitboxes");
        var proj = NovaClient.MODULES.getByName("Projectile ESP");

        boolean boxes =
            hit != null && hit.isEnabled();

        boolean projectiles =
            proj != null && proj.isEnabled();

        if (!boxes && !projectiles) {
            return;
        }

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || e.isRemoved()) {
                continue;
            }

            // HITBOX OUTLINES
            if (boxes
                && e instanceof LivingEntity living
                && living.isAlive()
                && (e instanceof Player
                    ? CombatSettings.bool(
                        "Hitboxes", "players", true
                    )
                    : CombatSettings.bool(
                        "Hitboxes", "mobs", true
                    ))
                && mc.player.distanceTo(e)
                    <= CombatSettings.number(
                        "Hitboxes", "range", 16
                    )) {

                draw(
                    e.getBoundingBox().inflate(
                        CombatSettings.number(
                            "Hitboxes", "size", 0.5
                        )
                    ),
                    0xFF83C9ED
                );
            }

            // PROJECTILE ESP
            if (projectiles
                && e instanceof Projectile
                && mc.player.distanceTo(e)
                    <= CombatSettings.number(
                        "Projectile ESP", "range", 32
                    )) {

                String type = e.getType()
                    .toString().toLowerCase();

                boolean arrow = type.contains("arrow");
                boolean trident = type.contains("trident");

                if ((arrow && !CombatSettings.bool(
                        "Projectile ESP", "arrows", true
                    ))
                    || (trident && !CombatSettings.bool(
                        "Projectile ESP", "tridents", true
                    ))
                    || (!arrow && !trident
                        && !CombatSettings.bool(
                            "Projectile ESP", "other", true
                        ))) {
                    continue;
                }

                int color = switch (
                    CombatSettings.get(
                        "Projectile ESP", "color", "Cyan"
                    )
                ) {
                    case "Purple" -> 0xFFB58AFF;
                    case "Green" -> 0xFF55FF99;
                    case "Red" -> 0xFFFF6677;
                    case "White" -> 0xFFFFFFFF;
                    default -> 0xFF83C9ED;
                };

                draw(
                    e.getBoundingBox().inflate(0.15),
                    color
                );
            }
        }
    }

    private static void draw(AABB b, int color) {
        double[] xs = {b.minX, b.maxX};
        double[] ys = {b.minY, b.maxY};
        double[] zs = {b.minZ, b.maxZ};

        for (double x : xs) {
            for (double y : ys) {
                Gizmos.line(
                    new Vec3(x, y, b.minZ),
                    new Vec3(x, y, b.maxZ),
                    color
                );
            }
        }

        for (double x : xs) {
            for (double z : zs) {
                Gizmos.line(
                    new Vec3(x, b.minY, z),
                    new Vec3(x, b.maxY, z),
                    color
                );
            }
        }

        for (double y : ys) {
            for (double z : zs) {
                Gizmos.line(
                    new Vec3(b.minX, y, z),
                    new Vec3(b.maxX, y, z),
                    color
                );
            }
        }
    }
}
