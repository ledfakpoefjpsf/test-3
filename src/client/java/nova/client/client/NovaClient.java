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
import nova.client.client.render.XrayChunkRefresh;

import org.lwjgl.glfw.GLFW;

public final class NovaClient implements ClientModInitializer {

    public static final ModuleManager MODULES =
        new ModuleManager();

    public static final ConfigManager CONFIG =
        new ConfigManager(MODULES);

    private static boolean guiKeyWasDown = false;

    private static boolean lastXrayState = false;

    @Override
    public void onInitializeClient() {

        System.out.println(
            "[Nova Client] Starting Nova Nebula v3..."
        );

        // Register every existing built-in module.
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
            "Highlights selected blocks through terrain",
            Category.RENDER
        ) {

            @Override
            public void onEnable() {

                XrayChunkRefresh.requestRefresh();

                System.out.println(
                    "[Nova X-Ray] Enabled"
                );
            }

            @Override
            public void onDisable() {

                XrayChunkRefresh.requestRefresh();

                System.out.println(
                    "[Nova X-Ray] Disabled"
                );
            }
        });

        // Load module states and settings.
        CONFIG.load();

        // Initialize existing HUD and radar systems.
        NovaHud.initialize();
        MinimapRenderer.initialize();
        EntityRadarRenderer.initialize();

        // Client tick.
        ClientTickEvents.END_CLIENT_TICK.register(
            client -> {

                MODULES.tick();

                MinimapRenderer.tick();

                XrayChunkRefresh.tick(client);

                checkXrayState();

                handleMacro(client);

                handleGuiKey(client);
            }
        );

        // Preserve existing ESP rendering.
        WorldRenderEvents.END_EXTRACTION.register(
            context -> {

                Minecraft mc = Minecraft.getInstance();

                EspRenderer.render(mc);
            }
        );

        // Save settings on shutdown.
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

    public static boolean isXrayEnabled() {

        Module xray = MODULES.getByName("X-Ray");

        return xray != null && xray.isEnabled();
    }

    private static void checkXrayState() {

        boolean enabled = isXrayEnabled();

        if (enabled != lastXrayState) {

            lastXrayState = enabled;

            XrayChunkRefresh.requestRefresh();
        }
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
