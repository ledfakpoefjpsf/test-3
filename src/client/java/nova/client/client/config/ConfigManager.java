
package nova.client.client.config;

// @name: ConfigManager
// @type: Java Class
// @location: src/client/java/nova/client/client/config/ConfigManager.java

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

import nova.client.client.module.Module;
import nova.client.client.module.ModuleManager;

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

    public void save() {
        try {
            JsonObject root = new JsonObject();

            for (Module module : manager.getModules()) {
                JsonObject data = new JsonObject();

                data.addProperty("enabled", module.isEnabled());
                data.addProperty("keybind", module.getKeybind());

                root.add(module.getName(), data);
            }

            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(root, writer);
            }

        } catch (Exception e) {
            System.err.println("[Nova Client] Config save failed");
            e.printStackTrace();
        }
    }

    public void load() {
        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {

            JsonObject root = gson.fromJson(reader, JsonObject.class);

            if (root == null) {
                return;
            }

            for (Module module : manager.getModules()) {

                if (!root.has(module.getName())) {
                    continue;
                }

                JsonObject data =
                    root.getAsJsonObject(module.getName());

                if (data.has("keybind")) {
                    module.setKeybind(
                        data.get("keybind").getAsInt()
                    );
                }

                if (data.has("enabled")) {
                    module.setEnabled(
                        data.get("enabled").getAsBoolean()
                    );
                }
            }

        } catch (Exception e) {
            System.err.println("[Nova Client] Config load failed");
            e.printStackTrace();
        }
    }
}
