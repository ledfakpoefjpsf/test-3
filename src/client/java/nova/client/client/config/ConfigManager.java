
package nova.client.client.config;

// @name: ConfigManager
// @type: Java Class
// @location: src/client/java/nova/client/client/config/ConfigManager.java

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;

import nova.client.client.module.Module;
import nova.client.client.module.ModuleManager;
import nova.client.client.module.MobEspSettings;

public class ConfigManager {

    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private final ModuleManager manager;

    private final Path path = FabricLoader
        .getInstance()
        .getConfigDir()
        .resolve("novaclient.json");

    public ConfigManager(ModuleManager manager) {
        this.manager = manager;
    }

    // =========================================
    // SAVE CONFIGURATION
    // =========================================

    public void save() {
        try {
            JsonObject root = new JsonObject();

            // Save all module states and keybinds
            for (Module module : manager.getModules()) {

                JsonObject data = new JsonObject();

                data.addProperty(
                    "enabled",
                    module.isEnabled()
                );

                data.addProperty(
                    "keybind",
                    module.getKeybind()
                );

                root.add(module.getName(), data);
            }

            // Save selected Mob ESP entities
            JsonArray mobSelections = new JsonArray();

            for (String id : MobEspSettings.getSelected()) {
                mobSelections.add(id);
            }

            root.add("mobEspSelections", mobSelections);

            // Create config folder if missing
            Files.createDirectories(path.getParent());

            // Write configuration
            try (Writer writer =
                     Files.newBufferedWriter(path)) {

                gson.toJson(root, writer);
            }

            System.out.println(
                "[Nova Client] Configuration saved!"
            );

        } catch (Exception e) {

            System.err.println(
                "[Nova Client] Failed to save configuration!"
            );

            e.printStackTrace();
        }
    }

    // =========================================
    // LOAD CONFIGURATION
    // =========================================

    public void load() {

        if (!Files.exists(path)) {
            System.out.println(
                "[Nova Client] No configuration found. Using defaults."
            );
            return;
        }

        try (Reader reader =
                 Files.newBufferedReader(path)) {

            JsonObject root =
                gson.fromJson(reader, JsonObject.class);

            if (root == null) {
                return;
            }

            // Load Mob ESP selections
            if (root.has("mobEspSelections")
                && root.get("mobEspSelections").isJsonArray()) {

                Set<String> ids = new HashSet<>();

                JsonArray savedMobs =
                    root.getAsJsonArray("mobEspSelections");

                for (JsonElement element : savedMobs) {
                    if (element.isJsonPrimitive()
                        && element.getAsJsonPrimitive().isString()) {

                        ids.add(element.getAsString());
                    }
                }

                MobEspSettings.setSelected(ids);
            }

            // Load all module states and keybinds
            for (Module module : manager.getModules()) {

                if (!root.has(module.getName())) {
                    continue;
                }

                JsonElement entry =
                    root.get(module.getName());

                if (!entry.isJsonObject()) {
                    continue;
                }

                JsonObject data = entry.getAsJsonObject();

                if (data.has("keybind")
                    && data.get("keybind").isJsonPrimitive()) {

                    module.setKeybind(
                        data.get("keybind").getAsInt()
                    );
                }

                if (data.has("enabled")
                    && data.get("enabled").isJsonPrimitive()) {

                    module.setEnabled(
                        data.get("enabled").getAsBoolean()
                    );
                }
            }

            System.out.println(
                "[Nova Client] Configuration loaded!"
            );

        } catch (Exception e) {

            System.err.println(
                "[Nova Client] Failed to load configuration!"
            );

            e.printStackTrace();
        }
    }
}
