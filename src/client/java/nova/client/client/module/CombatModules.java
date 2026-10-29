
package nova.client.client.module;

// @name: CombatModules
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/CombatModules.java

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

/**
 * Nova Nebula v3.0 Combat Modules.
 * All modules start disabled by default.
 */
public final class CombatModules {

    private CombatModules() {}

    private static final double RANGE = 4.5;
    private static final float AIM_STEP = 5.0f;

    public static void register(ModuleManager manager) {

        // Sword Aimbot
        manager.register(new Module(
            "Sword Aimbot",
            "Smoothly face nearby targets while holding a sword",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!mc.player.getMainHandItem()
                        .getItem().toString().contains("sword")) {
                    return;
                }

                aim(mc, nearest(mc, RANGE), AIM_STEP);
            }
        });

        // Bow Aimbot
        manager.register(new Module(
            "Bow Aimbot",
            "Lead nearby moving targets while drawing a bow",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!mc.player.isUsingItem()
                    || !mc.player.getUseItem().is(Items.BOW)) {
                    return;
                }

                LivingEntity target = nearest(mc, 48);

                if (target != null) {
                    aimAt(
                        mc,
                        target.position()
                            .add(target.getDeltaMovement().scale(5))
                            .add(0, target.getBbHeight() * .75, 0),
                        2.5f
                    );
                }
            }
        });

        // TriggerBot
        manager.register(new Module(
            "TriggerBot",
            "Attack targets under your crosshair",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!(mc.hitResult instanceof EntityHitResult hit)) {
                    return;
                }

                if (!(hit.getEntity() instanceof LivingEntity target)
                    || !valid(mc, target)
                    || mc.player.distanceTo(target) > RANGE) {
                    return;
                }

                if (mc.player.getAttackStrengthScale(0) >= .95f) {
                    mc.gameMode.attack(mc.player, target);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        });

        // Auto Crystal
        manager.register(new Module(
            "Auto Crystal",
            "Break nearby end crystals",
            Category.COMBAT
        ) {
            private int delay;

            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null || ++delay < 5) return;

                if (!mc.player.getMainHandItem()
                        .is(Items.END_CRYSTAL)) {
                    return;
                }

                delay = 0;

                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity.getType()
                            == net.minecraft.world.entity.EntityType.END_CRYSTAL
                        && mc.player.distanceTo(entity) < 4) {

                        mc.gameMode.attack(mc.player, entity);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        break;
                    }
                }
            }
        });

        // Auto Totem
        manager.register(new Module(
            "Auto Totem",
            "Select a hotbar totem when health is low",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (mc.player.getHealth() > 10
                    || mc.player.getOffhandItem()
                        .is(Items.TOTEM_OF_UNDYING)) {
                    return;
                }

                for (int slot = 0; slot < 9; slot++) {
                    if (mc.player.getInventory()
                            .getItem(slot)
                            .is(Items.TOTEM_OF_UNDYING)) {

                        mc.player.getInventory().setSelectedSlot(slot);
                        break;
                    }
                }
            }
        });

        // Auto Shield
        manager.register(new Module(
            "Auto Shield",
            "Raise shield near a target",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();
                if (mc == null) return;

                if (!mc.player.getOffhandItem().is(Items.SHIELD)) {
                    return;
                }

                if (nearest(mc, 5) != null) {
                    mc.options.keyUse.setDown(true);
                }
            }

            @Override
            public void onDisable() {
                Minecraft.getInstance()
                    .options.keyUse.setDown(false);
            }
        });

        // Target Strafe
        manager.register(new Module(
            "Target Strafe",
            "Strafe around nearby targets",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null && nearest(mc, 4) != null) {
                    mc.options.keyRight.setDown(true);
                }
            }

            @Override
            public void onDisable() {
                Minecraft.getInstance()
                    .options.keyRight.setDown(false);
            }
        });

        // Aim Assist
        manager.register(new Module(
            "Aim Assist",
            "Gently track nearby targets",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null) {
                    aim(mc, nearest(mc, RANGE), 1.5f);
                }
            }
        });

        // Hitboxes (placeholder)
        manager.register(new Module(
            "Hitboxes",
            "Target reach indicator (not implemented)",
            Category.COMBAT
        ) {});

        // Auto Weapon
        manager.register(new Module(
            "Auto Weapon",
            "Select a sword near targets",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null || nearest(mc, RANGE) == null) {
                    return;
                }

                for (int slot = 0; slot < 9; slot++) {
                    if (mc.player.getInventory()
                            .getItem(slot)
                            .getItem()
                            .toString()
                            .contains("sword")) {

                        mc.player.getInventory()
                            .setSelectedSlot(slot);
                        return;
                    }
                }
            }
        });

        // Projectile ESP (placeholder)
        manager.register(new Module(
            "Projectile ESP",
            "Projectile highlighting (not implemented)",
            Category.COMBAT
        ) {});

        // Criticals Helper
        manager.register(new Module(
            "Criticals Helper",
            "Jump when a target is in reach",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.onGround()
                    && nearest(mc, 3) != null
                    && mc.player.getAttackStrengthScale(0) > .95f) {

                    mc.player.jumpFromGround();
                }
            }
        });
    }

    private static Minecraft ready() {
        Minecraft mc = Minecraft.getInstance();

        return mc.player != null
            && mc.level != null
            && mc.gameMode != null
            && mc.screen == null
            ? mc : null;
    }

    private static boolean valid(
        Minecraft mc,
        LivingEntity entity
    ) {
        return entity != mc.player
            && !(entity instanceof Player player
                && (player.isSpectator() || player.isCreative()))
            && entity.isAlive()
            && mc.player.hasLineOfSight(entity);
    }

    private static LivingEntity nearest(
        Minecraft mc,
        double range
    ) {
        LivingEntity best = null;
        double bestDistance = range * range;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)
                || !valid(mc, living)) {
                continue;
            }

            double distance = mc.player.distanceToSqr(living);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = living;
            }
        }

        return best;
    }

    private static void aim(
        Minecraft mc,
        LivingEntity target,
        float speed
    ) {
        if (target != null) {
            aimAt(
                mc,
                target.position()
                    .add(0, target.getBbHeight() * .75, 0),
                speed
            );
        }
    }

    private static void aimAt(
        Minecraft mc,
        Vec3 position,
        float speed
    ) {
        Vec3 delta = position.subtract(
            mc.player.getEyePosition()
        );

        double horizontal = Math.sqrt(
            delta.x * delta.x + delta.z * delta.z
        );

        float yaw = (float) (
            Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90
        );

        float pitch = (float) -Math.toDegrees(
            Math.atan2(delta.y, horizontal)
        );

        mc.player.setYRot(
            mc.player.getYRot()
                + Mth.clamp(
                    Mth.wrapDegrees(yaw - mc.player.getYRot()),
                    -speed,
                    speed
                )
        );

        mc.player.setXRot(
            mc.player.getXRot()
                + Mth.clamp(
                    pitch - mc.player.getXRot(),
                    -speed,
                    speed
                )
        );
    }
}
