
package nova.client.client.gui;

// @name: NovaHud
// @type: Client HUD Renderer
// @location: src/client/java/nova/client/client/gui/NovaHud.java

import java.util.Locale;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.TargetHudSettings;

public final class NovaHud {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int PURPLE = 0xFFBD92FF;
    private static final int MUTED = 0xFFB4A5C9;
    private static final int BACKGROUND = 0xA5100B1B;
    private static final int DARK = 0xFF261B37;
    private static final int GREEN = 0xFF78E8A2;

    private static final int TARGET_WIDTH = 186;
    private static final int TARGET_HEIGHT = 65;

    private static LivingEntity lastTarget;
    private static long lastTargetTime;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private NovaHud() {}

    public static void initialize() {
        HudRenderCallback.EVENT.register(
            (graphics, tickCounter) -> render(graphics)
        );
    }

    private static boolean enabled(String name) {
        Module module = NovaClient.MODULES.getByName(name);
        return module != null && module.isEnabled();
    }

    private static void render(GuiGraphics graphics) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null
                || mc.options.hideGui) {
            lastTarget = null;
            return;
        }

        int x = 8;
        int y = 8;

        if (enabled("Coordinates HUD")) {

            String coordinates = String.format(
                Locale.ROOT,
                "XYZ: %.1f / %.1f / %.1f",
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ()
            );

            drawLine(graphics, mc, coordinates, x, y);
            y += 17;
        }

        if (enabled("FPS HUD")) {

            drawLine(
                graphics,
                mc,
                "FPS: " + mc.getFps(),
                x,
                y
            );

            y += 17;
        }

        if (enabled("Armor HUD")) {
            drawArmor(graphics, mc, x, y + 3);
        }

        if (enabled("Target HUD")) {
            drawTargetHud(graphics, mc);
        } else {
            lastTarget = null;
        }
    }

    private static void drawTargetHud(
            GuiGraphics graphics,
            Minecraft mc) {

        LivingEntity target = findTarget(mc);

        if (target == null) {
            return;
        }

        int x = TargetHudSettings.getX(
            mc.getWindow().getGuiScaledWidth(),
            TARGET_WIDTH
        );

        int y = TargetHudSettings.getY(
            mc.getWindow().getGuiScaledHeight(),
            TARGET_HEIGHT
        );

        float health = Math.max(0.0F, target.getHealth());
        float maxHealth = Math.max(1.0F, target.getMaxHealth());

        float percent = Math.max(
            0.0F,
            Math.min(1.0F, health / maxHealth)
        );

        double distance = mc.player.distanceTo(target);

        // Background and border
        graphics.fill(
            x, y,
            x + TARGET_WIDTH,
            y + TARGET_HEIGHT,
            BACKGROUND
        );

        graphics.fill(
            x, y,
            x + 3,
            y + TARGET_HEIGHT,
            PURPLE
        );

        graphics.fill(
            x + 3, y,
            x + TARGET_WIDTH,
            y + 2,
            PURPLE
        );

        // Header
        graphics.drawString(
            mc.font,
            "NOVA  /  TARGET",
            x + 11,
            y + 7,
            PURPLE,
            false
        );

        // Target name, truncated if necessary
        String name = target.getDisplayName().getString();

        if (mc.font.width(name) > TARGET_WIDTH - 22) {
            name = mc.font.plainSubstrByWidth(
                name,
                TARGET_WIDTH - 35
            ) + "...";
        }

        graphics.drawString(
            mc.font,
            name,
            x + 11,
            y + 21,
            WHITE,
            false
        );

        String stats = String.format(
            Locale.ROOT,
            "%.1f / %.1f HP   |   %.1fm",
            health,
            maxHealth,
            distance
        );

        graphics.drawString(
            mc.font,
            stats,
            x + 11,
            y + 35,
            MUTED,
            false
        );

        // Health bar background
        int barX = x + 11;
        int barY = y + 51;
        int barWidth = TARGET_WIDTH - 22;

        graphics.fill(
            barX,
            barY,
            barX + barWidth,
            barY + 7,
            DARK
        );

        // Health bar foreground
        int fillWidth = Math.round(barWidth * percent);

        if (fillWidth > 0) {
            graphics.fill(
                barX,
                barY,
                barX + fillWidth,
                barY + 7,
                healthColor(percent)
            );
        }

        // Small percentage label
        String percentage = Math.round(percent * 100.0F) + "%";

        graphics.drawString(
            mc.font,
            percentage,
            x + TARGET_WIDTH - mc.font.width(percentage) - 10,
            y + 7,
            GREEN,
            false
        );
    }

    private static LivingEntity findTarget(Minecraft mc) {

        long now = System.nanoTime() / 1_000_000L;

        if (mc.hitResult instanceof EntityHitResult hit) {

            Entity entity = hit.getEntity();

            if (entity instanceof LivingEntity living
                    && entity != mc.player
                    && living.isAlive()) {

                lastTarget = living;
                lastTargetTime = now;

                return living;
            }
        }

        if (lastTarget != null
                && lastTarget.isAlive()
                && !lastTarget.isRemoved()
                && lastTarget.level() == mc.level
                && now - lastTargetTime
                    <= TargetHudSettings.getHoldMillis()) {

            return lastTarget;
        }

        lastTarget = null;
        return null;
    }

    private static int healthColor(float percent) {

        int red = (int) (255 * (1.0F - percent));
        int green = (int) (255 * percent);

        return 0xFF000000
            | (red << 16)
            | (green << 8);
    }

    private static void drawLine(
            GuiGraphics graphics,
            Minecraft mc,
            String message,
            int x,
            int y) {

        int width = mc.font.width(message) + 12;

        graphics.fill(
            x, y,
            x + width,
            y + 15,
            BACKGROUND
        );

        graphics.fill(
            x, y,
            x + 2,
            y + 15,
            PURPLE
        );

        graphics.drawString(
            mc.font,
            message,
            x + 6,
            y + 4,
            WHITE,
            false
        );
    }

    private static void drawArmor(
            GuiGraphics graphics,
            Minecraft mc,
            int x,
            int y) {

        graphics.fill(
            x, y,
            x + 148,
            y + 91,
            BACKGROUND
        );

        graphics.fill(
            x, y,
            x + 2,
            y + 91,
            PURPLE
        );

        graphics.drawString(
            mc.font,
            "ARMOR",
            x + 7,
            y + 5,
            PURPLE,
            false
        );

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {

            ItemStack stack =
                mc.player.getItemBySlot(ARMOR_SLOTS[i]);

            int rowY = y + 19 + i * 17;

            if (stack.isEmpty()) {

                graphics.drawString(
                    mc.font,
                    "Empty",
                    x + 26,
                    rowY + 4,
                    MUTED,
                    false
                );

                continue;
            }

            graphics.renderItem(
                stack,
                x + 7,
                rowY
            );

            String durability;

            if (stack.isDamageableItem()) {

                int remaining =
                    stack.getMaxDamage()
                    - stack.getDamageValue();

                durability = remaining
                    + "/" + stack.getMaxDamage();

            } else {
                durability = "Equipped";
            }

            graphics.drawString(
                mc.font,
                durability,
                x + 27,
                rowY + 4,
                WHITE,
                false
            );
        }
    }
}
