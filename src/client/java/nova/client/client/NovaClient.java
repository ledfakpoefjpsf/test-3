
package nova.client;

// @name: NovaClient
// @type: Fabric Client Entrypoint
// @location: src/client/java/nova/client/

import nova.client.config.ConfigManager;
import nova.client.gui.ClickGuiScreen;
import nova.client.module.ModuleManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public class NovaClient implements ClientModInitializer {

    public static final String NAME = "Nova Client";
    public static final String VERSION = "1.0.0";

    public static final ModuleManager MODULES =
        new ModuleManager();

    public static final ConfigManager CONFIG =
        new ConfigManager(MODULES);

    private boolean guiKeyWasDown = false;

    @Override
    public void onInitializeClient() {

        MODULES.initialize();
        CONFIG.load();

        ClientTickEvents.END_CLIENT_TICK.register(
            client -> {
                MODULES.tick();
                handleGuiKey(client);
            }
        );

        Runtime.getRuntime().addShutdownHook(
            new Thread(CONFIG::save)
        );

        System.out.println(
            "[NovaClient] Loaded version " + VERSION
        );
    }

    private void handleGuiKey(Minecraft client) {

        if (client.getWindow() == null) {
            return;
        }

        long window = client.getWindow().getWindow();

        boolean down = GLFW.glfwGetKey(
            window,
            GLFW.GLFW_KEY_RIGHT_SHIFT
        ) == GLFW.GLFW_PRESS;

        if (down && !guiKeyWasDown) {

            if (client.screen instanceof ClickGuiScreen) {
                client.setScreen(null);
            } else if (client.screen == null) {
                client.setScreen(new ClickGuiScreen());
            }
        }

        guiKeyWasDown = down;
    }
}
