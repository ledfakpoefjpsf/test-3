package nova.client.client;

// @name: NovaClient
// @type: Client Mod Initializer
// @location: src/client/java/nova/client/client/NovaClient.java

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;

import nova.client.client.config.ConfigManager;
import nova.client.client.gui.ClickGuiScreen;
import nova.client.client.gui.NovaHud;
import nova.client.client.macro.MacroEngine;
import nova.client.client.module.Category;
import nova.client.client.module.Module;
import nova.client.client.module.ModuleManager;
import nova.client.client.render.EntityRadarRenderer;
import nova.client.client.render.EspRenderer;
import nova.client.client.render.MinimapRenderer;
import nova.client.client.render.XrayRenderer;

import org.lwjgl.glfw.GLFW;

public final class NovaClient implements ClientModInitializer {

    public static final ModuleManager MODULES =
        new ModuleManager();

    public static final ConfigManager CONFIG =
        new ConfigManager(MODULES);

    private static boolean guiKeyWasDown = false;

    @Override
    public void onInitializeClient() {

        System.out.println(
            "[Nova Client] Starting Nova Nebula v3..."
        );

        // Register existing modules.
        MODULES.initialize();

        // Minimap.
        MODULES.register(new Module(
            "Minimap",
            "Top-down terrain minimap",
            Category.RENDER
        ) {});

        // Entity Radar.
        MODULES.register(new Module(
            "Entity Radar",
            "Nearby players and mobs",
            Category.RENDER
        ) {});

        // X-Ray.
        MODULES.register(new Module(
            "X-Ray",
            "Highlights ores, bedrock and fluids",
            Category.RENDER
        ) {
            @Override
            public void onDisable() {
                XrayRenderer.clear();
            }
        });

        // Load saved module and settings states.
        CONFIG.load();

        // Initialize existing HUD systems.
        NovaHud.initialize();
        MinimapRenderer.initialize();
        EntityRadarRenderer.initialize();

        // Client updates.
        ClientTickEvents.END_CLIENT_TICK.register(
            client -> {

                MODULES.tick();

                MinimapRenderer.tick();
                XrayRenderer.tick();

                handleMacro(client);
                handleGuiKey(client);
            }
        );

        // Preserve ESP rendering and add X-Ray.
        WorldRenderEvents.END_EXTRACTION.register(
            context -> {

                Minecraft mc = Minecraft.getInstance();

                EspRenderer.render(mc);
                XrayRenderer.render(mc);
            }
        );

        // Save when the game closes.
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                CONFIG::save,
                "NovaClient-ConfigSave"
            )
        );

        System.out.println(
            "[Nova Client] Initialized!"
        );
    }

    private static void handleMacro(Minecraft client) {

        Module macro =
            MODULES.getByName("Macro Recorder");

        if (macro != null && macro.isEnabled()) {
            MacroEngine.tick(client);
        } else {
            MacroEngine.stop(client);
        }
    }

    private static void handleGuiKey(Minecraft client) {

        if (client == null || client.getWindow() == null) {
            return;
        }

        long window = client.getWindow().handle();

        boolean keyDown = GLFW.glfwGetKey(
            window,
            GLFW.GLFW_KEY_RIGHT_SHIFT
        ) == GLFW.GLFW_PRESS;

        if (keyDown && !guiKeyWasDown) {

            if (client.screen instanceof ClickGuiScreen) {

                client.setScreen(null);

            } else if (client.screen == null) {

                client.setScreen(
                    new ClickGuiScreen()
                );
            }
        }

        guiKeyWasDown = keyDown;
    }
}
