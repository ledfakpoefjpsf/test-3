
package nova.client.client;

// @name: NovaClient
// @type: Client Initializer
// @location: src/client/java/nova/client/client/NovaClient.java

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import nova.client.client.gui.ClickGuiScreen;
import nova.client.client.gui.NovaHud;
import nova.client.client.macro.MacroEngine;
import nova.client.client.module.Module;
import nova.client.client.module.ModuleManager;
import nova.client.client.config.ConfigManager;

import org.lwjgl.glfw.GLFW;

public class NovaClient implements ClientModInitializer {

    public static final ModuleManager MODULES =
        new ModuleManager();

    public static final ConfigManager CONFIG =
        new ConfigManager(MODULES);

    private static boolean guiKeyWasDown = false;

    @Override
    public void onInitializeClient() {

        MODULES.initialize();

        CONFIG.load();

        // Register the Coordinates and FPS HUD.
        NovaHud.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            MODULES.tick();

            Module macro =
                MODULES.getByName("Macro Recorder");

            if (macro != null && macro.isEnabled()) {
                MacroEngine.tick(client);
            } else {
                MacroEngine.stop(client);
            }

            handleGuiKey(client);
        });

        Runtime.getRuntime().addShutdownHook(
            new Thread(CONFIG::save)
        );

        System.out.println("[Nova Client] Initialized!");
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
