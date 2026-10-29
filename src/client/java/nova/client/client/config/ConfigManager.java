package nova.client.client.config;

// @name: ConfigManager
// @type: Client Configuration Manager
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
import nova.client.client.module.RadarSettings;
import nova.client.client.module.XraySettings;

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

            // Module enabled states and keybinds.
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

            // Mob ESP selections.
            JsonArray mobs = new JsonArray();

            for (String id : MobEspSettings.getSelected()) {
                mobs.add(id);
            }

            root.add("mobEspSelections", mobs);

            // Entity Radar settings.
            JsonObject radar = new JsonObject();

            radar.addProperty(
                "range",
                RadarSettings.range
            );

            radar.addProperty(
                "size",
                RadarSettings.size
            );

            radar.addProperty(
                "players",
                RadarSettings.players
            );

            radar.addProperty(
                "hostile",
                RadarSettings.hostile
            );

            radar.addProperty(
                "passive",
                RadarSettings.passive
            );

            radar.addProperty(
                "rotate",
                RadarSettings.rotate
            );

            radar.addProperty(
                "names",
                RadarSettings.names
            );

            radar.addProperty(
                "distance",
                RadarSettings.distance
            );

            root.add("entityRadarSettings", radar);

            // X-Ray settings.
            root.add(
                "xraySettings",
                XraySettings.save()
            );

            // Write config.
            Files.createDirectories(path.getParent());

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

            // Entity Radar.
            if (root.has("entityRadarSettings")
                && root.get("entityRadarSettings").isJsonObject()) {

                JsonObject radar =
                    root.getAsJsonObject("entityRadarSettings");

                if (radar.has("range")) {
                    RadarSettings.setRange(
                        radar.get("range").getAsInt()
                    );
                }

                if (radar.has("size")) {
                    RadarSettings.setSize(
                        radar.get("size").getAsInt()
                    );
                }

                if (radar.has("players")) {
                    RadarSettings.players =
                        radar.get("players").getAsBoolean();
                }

                if (radar.has("hostile")) {
                    RadarSettings.hostile =
                        radar.get("hostile").getAsBoolean();
                }

                if (radar.has("passive")) {
                    RadarSettings.passive =
                        radar.get("passive").getAsBoolean();
                }

                if (radar.has("rotate")) {
                    RadarSettings.rotate =
                        radar.get("rotate").getAsBoolean();
                }

                if (radar.has("names")) {
                    RadarSettings.names =
                        radar.get("names").getAsBoolean();
                }

                if (radar.has("distance")) {
                    RadarSettings.distance =
                        radar.get("distance").getAsBoolean();
                }
            }

            // X-Ray.
            if (root.has("xraySettings")
                && root.get("xraySettings").isJsonObject()) {

                XraySettings.load(
                    root.getAsJsonObject("xraySettings")
                );
            }

            // Mob ESP selections.
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

            // Module states and keybinds.
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
