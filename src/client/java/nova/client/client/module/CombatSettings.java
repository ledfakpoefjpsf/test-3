
package nova.client.client.module;

// @name: CombatSettings
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/CombatSettings.java

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;

public final class CombatSettings {

    private static final Properties p = new Properties();

    private CombatSettings() {}

    private static Path path() {
        return Minecraft.getInstance()
            .gameDirectory.toPath()
            .resolve("config/nova/combat.properties");
    }

    static {
        load();
    }

    public static void load() {
        try {
            if (Files.exists(path())) {
                try (var in = Files.newInputStream(path())) {
                    p.load(in);
                }
            }
        } catch (Exception e) {
            System.err.println(
                "[Nova] Combat settings load: " + e
            );
        }
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());

            try (var out = Files.newOutputStream(path())) {
                p.store(out, "Nova combat settings");
            }
        } catch (Exception e) {
            System.err.println(
                "[Nova] Combat settings save: " + e
            );
        }
    }

    public static String get(
        String module,
        String key,
        String fallback
    ) {
        return p.getProperty(
            module + "." + key,
            fallback
        );
    }

    public static double number(
        String module,
        String key,
        double fallback
    ) {
        try {
            return Double.parseDouble(
                get(module, key, "" + fallback)
            );
        } catch (Exception e) {
            return fallback;
        }
    }

    public static boolean bool(
        String module,
        String key,
        boolean fallback
    ) {
        return Boolean.parseBoolean(
            get(module, key, "" + fallback)
        );
    }

    public static void set(
        String module,
        String key,
        String value
    ) {
        p.setProperty(module + "." + key, value);
        save();
    }

    public static void cycle(
        String module,
        String key,
        String... choices
    ) {
        String current = get(
            module, key, choices[0]
        );

        int i = Arrays.asList(choices)
            .indexOf(current);

        set(
            module,
            key,
            choices[(i + 1) % choices.length]
        );
    }
}
