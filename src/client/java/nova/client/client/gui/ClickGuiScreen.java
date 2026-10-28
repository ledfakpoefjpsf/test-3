
package nova.client.client.gui;

// @name: ClickGuiScreen
// @type: Client GUI Screen
// @location: src/client/java/nova/client/client/gui/ClickGuiScreen.java

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import nova.client.client.NovaClient;
import nova.client.module.Category;
import nova.client.module.Module;

public class ClickGuiScreen extends Screen {

    private static final int WIDTH = 440;
    private static final int HEIGHT = 290;

    private static final int BACKGROUND = 0xFF101018;
    private static final int PANEL = 0xFF1B1B29;
    private static final int ACCENT = 0xFF9667FF;
    private static final int TEXT = 0xFFFFFFFF;

    private Category selected = Category.COMBAT;
    private int page = 0;

    public ClickGuiScreen() {
        super(Component.literal("Nova Client"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {

        clearWidgets();

        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;

        Category[] categories = Category.values();

        for (int i = 0; i < categories.length; i++) {

            Category category = categories[i];

            int y = top + 48 + i * 35;

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        (selected == category ? "> " : "") +
                        category.getDisplayName()
                    ),
                    button -> {
                        selected = category;
                        page = 0;
                        rebuild();
                    }
                )
                .bounds(left + 15, y, 125, 27)
                .build()
            );
        }

        List<Module> modules =
                NovaClient.MODULES.getByCategory(selected);

        int perPage = 6;
        int start = page * perPage;
        int end = Math.min(start + perPage, modules.size());

        for (int i = start; i < end; i++) {

            Module module = modules.get(i);
            int y = top + 48 + (i - start) * 35;

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        module.getName() +
                        (module.isEnabled() ? " [ON]" : " [OFF]")
                    ),
                    button -> {
                        module.toggle();
                        NovaClient.CONFIG.save();
                        rebuild();
                    }
                )
                .bounds(left + 160, y, 260, 27)
                .build()
            );
        }

        addRenderableWidget(
            Button.builder(
                Component.literal("< Previous"),
                button -> {
                    if (page > 0) {
                        page--;
                        rebuild();
                    }
                }
            )
            .bounds(left + 160, top + 255, 110, 22)
            .build()
        );

        addRenderableWidget(
            Button.builder(
                Component.literal("Next >"),
                button -> {
                    if ((page + 1) * perPage < modules.size()) {
                        page++;
                        rebuild();
                    }
                }
            )
            .bounds(left + 310, top + 255, 110, 22)
            .build()
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        // Minecraft 1.21.11:
        // Do not call renderBackground() here.
        // The screen rendering pipeline already handles
        // background effects, and requesting another blur
        // can crash with:
        // "Can only blur once per frame"

        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;

        graphics.fill(
            left,
            top,
            left + WIDTH,
            top + HEIGHT,
            BACKGROUND
        );

        graphics.fill(
            left,
            top,
            left + WIDTH,
            top + 34,
            PANEL
        );

        graphics.fill(
            left,
            top + 33,
            left + WIDTH,
            top + 35,
            ACCENT
        );

        graphics.drawString(
            font,
            "NOVA CLIENT",
            left + 15,
            top + 12,
            TEXT
        );

        graphics.drawString(
            font,
            selected.getDisplayName(),
            left + 160,
            top + 12,
            ACCENT
        );

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
