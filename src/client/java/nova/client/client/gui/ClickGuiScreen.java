
package nova.client.gui;

// @name: ClickGuiScreen
// @type: Java Class
// @location: src/client/java/nova/client/gui/

import nova.client.NovaClient;
import nova.client.module.Category;
import nova.client.module.Module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClickGuiScreen extends Screen {

    private Category selected = Category.RENDER;
    private int page = 0;

    private static final int WIDTH = 440;
    private static final int HEIGHT = 290;

    private static final int BG = 0xFF101018;
    private static final int PANEL = 0xFF1B1B29;
    private static final int ACCENT = 0xFF9667FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF9999AA;

    public ClickGuiScreen() {
        super(Component.literal("Nova Client"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        int index = 0;

        for (Category category : Category.values()) {

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        category.getDisplayName()
                    ),
                    button -> {
                        selected = category;
                        page = 0;
                        rebuild();
                    }
                ).bounds(
                    x + 12,
                    y + 48 + index * 32,
                    115,
                    25
                ).build()
            );

            index++;
        }

        List<Module> modules =
            NovaClient.MODULES.getByCategory(selected);

        int pageSize = 6;
        int start = page * pageSize;
        int end = Math.min(
            start + pageSize,
            modules.size()
        );

        for (int i = start; i < end; i++) {

            Module module = modules.get(i);
            int row = i - start;

            addRenderableWidget(
                Button.builder(
                    getModuleLabel(module),
                    button -> {
                        module.toggle();
                        NovaClient.CONFIG.save();
                        rebuild();
                    }
                ).bounds(
                    x + 145,
                    y + 48 + row * 32,
                    280,
                    25
                ).build()
            );
        }

        if (page > 0) {
            addRenderableWidget(
                Button.builder(
                    Component.literal("Previous"),
                    button -> {
                        page--;
                        rebuild();
                    }
                ).bounds(
                    x + 145, y + 252, 110, 22
                ).build()
            );
        }

        if (end < modules.size()) {
            addRenderableWidget(
                Button.builder(
                    Component.literal("Next"),
                    button -> {
                        page++;
                        rebuild();
                    }
                ).bounds(
                    x + 315, y + 252, 110, 22
                ).build()
            );
        }
    }

    private Component getModuleLabel(Module module) {
        return Component.literal(
            module.getName() +
            (module.isEnabled() ? "  [ON]" : "  [OFF]")
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta) {

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        graphics.fill(
            x, y,
            x + WIDTH, y + HEIGHT,
            BG
        );

        graphics.fill(
            x, y,
            x + WIDTH, y + 4,
            ACCENT
        );

        graphics.fill(
            x + 6, y + 40,
            x + 135, y + 278,
            PANEL
        );

        graphics.drawString(
            font,
            "NOVA CLIENT",
            x + 12,
            y + 17,
            ACCENT,
            true
        );

        graphics.drawString(
            font,
            "v1.0",
            x + 390,
            y + 17,
            GRAY,
            false
        );

        graphics.drawString(
            font,
            selected.getDisplayName(),
            x + 145,
            y + 25,
            WHITE,
            true
        );

        graphics.drawString(
            font,
            "RIGHT SHIFT | ESC TO CLOSE",
            x + 145,
            y + 280,
            GRAY,
            false
        );

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

