
package nova.client.client.module;

// @name: CombatModules
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/CombatModules.java

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;

public final class CombatModules {

    private CombatModules() {}

    public static void register(ModuleManager manager) {

        manager.register(new Module(
            "Sword Aimbot",
            "Configurable sword targeting",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.getMainHandItem()
                        .getItem().toString()
                        .contains("sword")) {
                    aim(
                        mc,
                        target(mc, "Sword Aimbot"),
                        "Sword Aimbot",
                        false
                    );
                }
            }
        });

        manager.register(new Module(
            "Bow Aimbot",
            "Snap or smooth predictive bow targeting",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.isUsingItem()
                    && mc.player.getUseItem().is(Items.BOW)) {
                    aim(
                        mc,
                        target(mc, "Bow Aimbot"),
                        "Bow Aimbot",
                        true
                    );
                }
            }
        });

        manager.register(new Module(
            "Aim Assist",
            "Configurable target tracking",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null) {
                    aim(
                        mc,
                        target(mc, "Aim Assist"),
                        "Aim Assist",
                        false
                    );
                }
            }
        });

        manager.register(new Module(
            "TriggerBot",
            "Attack target under crosshair",
            Category.COMBAT
        ) {
            private int ticks;

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) return;

                if (++ticks < (int) CombatSettings.number(
                    "TriggerBot", "delay", 2
                )) {
                    return;
                }

                if (mc.hitResult instanceof EntityHitResult hit
                    && hit.getEntity() instanceof LivingEntity living
                    && valid(mc, living, "TriggerBot")
                    && mc.player.distanceTo(living)
                        <= CombatSettings.number(
                            "TriggerBot", "range", 4.5
                        )
                    && mc.player.getAttackStrengthScale(0)
                        >= 0.95f) {

                    mc.gameMode.attack(mc.player, living);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    ticks = 0;
                }
            }
        });

        manager.register(new Module(
            "Auto Crystal",
            "Break nearby end crystals",
            Category.COMBAT
        ) {
            private int ticks;

            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null || ++ticks < 5) return;

                ticks = 0;

                for (Entity e : mc.level.entitiesForRendering()) {
                    if (e.getType() == EntityType.END_CRYSTAL
                        && mc.player.distanceTo(e)
                            < CombatSettings.number(
                                "Auto Crystal", "range", 4
                            )) {

                        mc.gameMode.attack(mc.player, e);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        break;
                    }
                }
            }
        });

        manager.register(new Module(
            "Auto Totem",
            "Select hotbar totem below health threshold",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null
                    || mc.player.getHealth()
                        > CombatSettings.number(
                            "Auto Totem", "health", 10
                        )
                    || mc.player.getOffhandItem()
                        .is(Items.TOTEM_OF_UNDYING)) {
                    return;
                }

                for (int i = 0; i < 9; i++) {
                    if (mc.player.getInventory()
                        .getItem(i)
                        .is(Items.TOTEM_OF_UNDYING)) {

                        mc.player.getInventory()
                            .setSelectedSlot(i);
                        break;
                    }
                }
            }
        });

        manager.register(new Module(
            "Auto Shield",
            "Raise offhand shield near targets",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.getOffhandItem()
                        .is(Items.SHIELD)) {

                    mc.options.keyUse.setDown(
                        target(mc, "Auto Shield") != null
                    );
                }
            }

            @Override
            public void onDisable() {
                Minecraft.getInstance()
                    .options.keyUse.setDown(false);
            }
        });

        manager.register(new Module(
            "Target Strafe",
            "Strafe near selected targets",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null) return;

                boolean found =
                    target(mc, "Target Strafe") != null;

                boolean left = CombatSettings.bool(
                    "Target Strafe", "left", false
                );

                mc.options.keyLeft.setDown(found && left);
                mc.options.keyRight.setDown(found && !left);
            }

            @Override
            public void onDisable() {
                Minecraft mc = Minecraft.getInstance();

                mc.options.keyLeft.setDown(false);
                mc.options.keyRight.setDown(false);
            }
        });

        manager.register(new Module(
            "Hitboxes",
            "Render adjustable client-side target boxes",
            Category.COMBAT
        ) {});

        manager.register(new Module(
            "Projectile ESP",
            "Render outlines around nearby projectiles",
            Category.COMBAT
        ) {});

        manager.register(new Module(
            "Auto Weapon",
            "Select sword when target nearby",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc == null
                    || target(mc, "Auto Weapon") == null) {
                    return;
                }

                for (int i = 0; i < 9; i++) {
                    if (mc.player.getInventory()
                        .getItem(i)
                        .getItem().toString()
                        .contains("sword")) {

                        mc.player.getInventory()
                            .setSelectedSlot(i);
                        break;
                    }
                }
            }
        });

        manager.register(new Module(
            "Criticals Helper",
            "Jump when ready to strike",
            Category.COMBAT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = ready();

                if (mc != null
                    && mc.player.onGround()
                    && target(mc, "Criticals Helper") != null
                    && mc.player.getAttackStrengthScale(0)
                        > 0.95f) {

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
        LivingEntity e,
        String module
    ) {
        if (e == mc.player || !e.isAlive()) {
            return false;
        }

        if (e instanceof Player p
            && (p.isSpectator() || p.isCreative())) {
            return false;
        }

        if (CombatSettings.bool(module, "los", true)
            && !mc.player.hasLineOfSight(e)) {
            return false;
        }

        return e instanceof Player
            ? CombatSettings.bool(module, "players", true)
            : CombatSettings.bool(module, "mobs", true);
    }

    private static LivingEntity target(
        Minecraft mc,
        String module
    ) {
        double range = CombatSettings.number(
            module,
            "range",
            module.equals("Bow Aimbot") ? 48 : 4.5
        );

        String priority = CombatSettings.get(
            module, "priority", "Closest"
        );

        LivingEntity best = null;
        double score = 0;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living)
                || !valid(mc, living, module)) {
                continue;
            }

            double d = mc.player.distanceToSqr(living);

            if (d > range * range) {
                continue;
            }

            double s = switch (priority) {
                case "Furthest" -> d;
                case "Lowest HP" -> -living.getHealth();
                case "Highest HP" -> living.getHealth();
                default -> -d;
            };

            if (best == null || s > score) {
                best = living;
                score = s;
            }
        }

        return best;
    }

    private static void aim(
        Minecraft mc,
        LivingEntity e,
        String module,
        boolean bow
    ) {
        if (e == null) return;

        Vec3 pos = e.position().add(
            0, e.getBbHeight() * 0.75, 0
        );

        if (bow && CombatSettings.bool(
            module, "prediction", true
        )) {
            double distance = mc.player.distanceTo(e);

            double lead = Math.min(
                18, distance / 2.5
            ) * CombatSettings.number(
                module, "lead", 1
            );

            pos = pos.add(
                e.getDeltaMovement().scale(lead)
            );
        }

        Vec3 delta = pos.subtract(
            mc.player.getEyePosition()
        );

        double h = Math.sqrt(
            delta.x * delta.x + delta.z * delta.z
        );

        float yaw = (float) (
            Math.toDegrees(
                Math.atan2(delta.z, delta.x)
            ) - 90
        );

        float pitch = (float) -Math.toDegrees(
            Math.atan2(delta.y, h)
        );

        boolean snap = CombatSettings.get(
            module,
            "mode",
            bow ? "Snap" : "Smooth"
        ).equals("Snap");

        float speed = (float) CombatSettings.number(
            module,
            "speed",
            bow ? 8 : 5
        );

        mc.player.setYRot(
            snap
                ? yaw
                : mc.player.getYRot()
                    + Mth.clamp(
                        Mth.wrapDegrees(
                            yaw - mc.player.getYRot()
                        ),
                        -speed,
                        speed
                    )
        );

        mc.player.setXRot(
            snap
                ? pitch
                : mc.player.getXRot()
                    + Mth.clamp(
                        pitch - mc.player.getXRot(),
                        -speed,
                        speed
                    )
        );
    }
}
