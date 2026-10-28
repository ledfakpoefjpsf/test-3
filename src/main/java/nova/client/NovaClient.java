
package nova.client.client;

// @name: NovaClient
// @type: Client Mod Initializer
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

public final class NovaClient implements ClientModInitializer {

    // =====================================
    // NOVA CLIENT SYSTEMS
    // =====================================

    public static final ModuleManager MODULES =
        new ModuleManager();

    public static final ConfigManager CONFIG =
        new ConfigManager(MODULES);

    private static boolean guiKeyWasDown = false;

    // =====================================
    // CLIENT INITIALIZATION
    // =====================================

    @Override
    public void onInitializeClient() {

        System.out.println(
            "[Nova Client] Starting Nova Nebula v3..."
        );

        // Register all modules
        MODULES.initialize();

        // Load saved module settings
        CONFIG.load();

        // Initialize HUD rendering
        NovaHud.initialize();

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Update enabled modules
            MODULES.tick();

            // Update macro recorder
            handleMacro(client);

            // Handle ClickGUI key
            handleGuiKey(client);
        });

        // Save settings when Minecraft closes
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                CONFIG::save,
                "NovaClient-ConfigSave"
            )
        );

        System.out.println(
            "[Nova Client] Nova Nebula v3 initialized!"
        );
    }

    // =====================================
    // MACRO SYSTEM
    // =====================================

    private static void handleMacro(Minecraft client) {

        Module macro =
            MODULES.getByName("Macro Recorder");

        if (macro != null && macro.isEnabled()) {

            MacroEngine.tick(client);

        } else {

            MacroEngine.stop(client);
        }
    }

    // =====================================
    // CLICK GUI KEY HANDLING
    // =====================================

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

        // Only toggle once per key press
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
