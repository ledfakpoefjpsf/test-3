
package nova.client.client;

// @name: NovaClient
// @type: Fabric Client Initializer
// @location: src/client/java/nova/client/client/NovaClient.java

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.Minecraft;

import nova.client.config.ConfigManager;
import nova.client.gui.ClickGuiScreen;
import nova.client.module.ModuleManager;

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            MODULES.tick();

            handleGuiKey(client);
        });

        Runtime.getRuntime().addShutdownHook(
                new Thread(CONFIG::save)
        );

        System.out.println(
                "[Nova Client] Initialized successfully!"
        );
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

                client.setScreen(new ClickGuiScreen());
            }
        }

        guiKeyWasDown = keyDown;
    }
}
