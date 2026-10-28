
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
import nova.client.client.module.Category;
import nova.client.client.module.Module;
import nova.client.client.module.MobEspSettings;

public class ClickGuiScreen extends Screen {

    private static final int WIDTH = 440;
    private static final int HEIGHT = 290;

    private static final int BACKGROUND = 0xFF101018;
    private static final int PANEL = 0xFF1B1B29;
    private static final int ACCENT = 0xFF9667FF;
    private static final int TEXT = 0xFFFFFFFF;

    private Category selected = Category.COMBAT;
    private boolean mobSettings = false;
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

        if (mobSettings) {
            buildMobSettings(left, top);
        } else {
            buildMainMenu(left, top);
        }
    }

    private void buildMainMenu(int left, int top) {

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

            boolean isMobEsp =
                module.getName().equals("Mob ESP");

            int buttonWidth = isMobEsp ? 155 : 260;

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
                .bounds(left + 160, y, buttonWidth, 27)
                .build()
            );

            if (isMobEsp) {
                addRenderableWidget(
                    Button.builder(
                        Component.literal("Settings"),
                        button -> {
                            mobSettings = true;
                            page = 0;
                            rebuild();
                        }
                    )
                    .bounds(left + 322, y, 98, 27)
                    .build()
                );
            }
        }

        addPageButtons(left, top, modules.size(), perPage);
    }

    private void buildMobSettings(int left, int top) {

        addRenderableWidget(
            Button.builder(
                Component.literal("< Back"),
                button -> {
                    mobSettings = false;
                    page = 0;
                    NovaClient.CONFIG.save();
                    rebuild();
                }
            )
            .bounds(left + 15, top + 45, 100, 25)
            .build()
        );

        addRenderableWidget(
            Button.builder(
                Component.literal("Select All"),
                button -> {
                    MobEspSettings.selectAll();
                    NovaClient.CONFIG.save();
                    rebuild();
                }
            )
            .bounds(left + 125, top + 45, 130, 25)
            .build()
        );

        addRenderableWidget(
            Button.builder(
                Component.literal("Select None"),
                button -> {
                    MobEspSettings.selectNone();
                    NovaClient.CONFIG.save();
                    rebuild();
                }
            )
            .bounds(left + 265, top + 45, 155, 25)
            .build()
        );

        String[] mobs = MobEspSettings.MOBS;

        int perPage = 12;
        int start = page * perPage;
        int end = Math.min(start + perPage, mobs.length);

        for (int i = start; i < end; i++) {
            String mob = mobs[i];

            int localIndex = i - start;
            int column = localIndex % 2;
            int row = localIndex / 2;

            int x = left + 15 + column * 207;
            int y = top + 83 + row * 27;

            addRenderableWidget(
                Button.builder(
                    Component.literal(
                        formatName(mob) +
                        (MobEspSettings.isSelected(mob)
                            ? " [ON]" : " [OFF]")
                    ),
                    button -> {
                        MobEspSettings.toggle(mob);
                        NovaClient.CONFIG.save();
                        rebuild();
                    }
                )
                .bounds(x, y, 198, 23)
                .build()
            );
        }

        addPageButtons(left, top, mobs.length, perPage);
    }

    private String formatName(String id) {
        String[] words = id.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(
                Character.toUpperCase(word.charAt(0))
            );
            result.append(word.substring(1));
        }

        return result.toString();
    }

    private void addPageButtons(
        int left,
        int top,
        int total,
        int perPage
    ) {
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
                    if ((page + 1) * perPage < total) {
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
        // Do not call renderBackground().
        // Prevents the double-blur crash.

        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;

        graphics.fill(
            left, top,
            left + WIDTH, top + HEIGHT,
            BACKGROUND
        );

        graphics.fill(
            left, top,
            left + WIDTH, top + 34,
            PANEL
        );

        graphics.fill(
            left, top + 33,
            left + WIDTH, top + 35,
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
            mobSettings ? "Mob ESP Settings"
                : selected.getDisplayName(),
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
