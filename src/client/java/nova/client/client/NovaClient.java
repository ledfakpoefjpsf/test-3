
package nova.client.client;

// @name: NovaClient
// @type: Client Mod Initializer
// @location: src/client/java/nova/client/client/NovaClient.java

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;

import nova.client.client.gui.ClickGuiScreen;
import nova.client.client.gui.NovaHud;
import nova.client.client.macro.MacroEngine;
import nova.client.client.module.Module;
import nova.client.client.module.ModuleManager;
import nova.client.client.config.ConfigManager;
import nova.client.client.render.EspRenderer;

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

        MODULES.initialize();
        CONFIG.load();
        NovaHud.initialize();

        // Keep gameplay systems on client ticks.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            MODULES.tick();
            handleMacro(client);
            handleGuiKey(client);

        });

        // NEW: Submit ESP visuals during frame extraction.
        // Do not also call EspRenderer.tick() above.
        WorldRenderEvents.END_EXTRACTION.register(context -> {

            EspRenderer.render(Minecraft.getInstance());

        });

        Runtime.getRuntime().addShutdownHook(
            new Thread(
                CONFIG::save,
                "NovaClient-ConfigSave"
            )
        );

        System.out.println("[Nova Client] Initialized!");
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

        boolean keyDown =
            GLFW.glfwGetKey(
                window,
                GLFW.GLFW_KEY_RIGHT_SHIFT
            ) == GLFW.GLFW_PRESS;

        if (keyDown && !guiKeyWasDown) {

            if (client.screen instanceof ClickGuiScreen) {
                client.setScreen(null);
            } else if (client.screen == null) {
                client.setScreen(new ClickGuiScreen());
            }
        }

        guiKeyWasDown = keyDown;
    }
}
