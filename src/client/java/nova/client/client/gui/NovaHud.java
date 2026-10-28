
package nova.client.client.gui;

// @name: NovaHud
// @type: Client HUD Renderer
// @location: src/client/java/nova/client/client/gui/NovaHud.java

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;

public final class NovaHud {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int PURPLE = 0xFFBD92FF;
    private static final int MUTED = 0xFFB4A5C9;
    private static final int BACKGROUND = 0xA5100B1B;

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

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        int x = 8;
        int y = 8;

        if (enabled("Coordinates HUD")) {

            String coordinates = String.format(
                java.util.Locale.ROOT,
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
    }

    private static void drawLine(
        GuiGraphics graphics,
        Minecraft mc,
        String message,
        int x,
        int y
    ) {

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
        int y
    ) {

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
