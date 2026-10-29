package nova.client.client.module;

// @name: XraySettings
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/XraySettings.java

import com.google.gson.JsonObject;

public final class XraySettings {

    public static boolean bedrock = true;
    public static boolean lava = true;
    public static boolean water = true;

    public static boolean diamond = true;
    public static boolean ancient = true;
    public static boolean emerald = true;
    public static boolean gold = true;
    public static boolean iron = true;
    public static boolean redstone = true;
    public static boolean lapis = true;
    public static boolean copper = true;
    public static boolean coal = true;
    public static boolean quartz = true;

    // Percent opacity of ordinary terrain.
    public static int hiddenOpacity = 20;

    // Radius around the actual player.
    public static int range = 128;

    private XraySettings() {
    }

    public static void setHiddenOpacity(int value) {
        hiddenOpacity = Math.max(0, Math.min(60, value));
    }

    public static void setRange(int value) {
        range = Math.max(16, Math.min(128, value));
    }

    public static void reset() {
        bedrock = true;
        lava = true;
        water = true;

        diamond = true;
        ancient = true;
        emerald = true;
        gold = true;
        iron = true;
        redstone = true;
        lapis = true;
        copper = true;
        coal = true;
        quartz = true;

        hiddenOpacity = 20;
        range = 128;
    }

    public static JsonObject save() {

        JsonObject json = new JsonObject();

        json.addProperty("bedrock", bedrock);
        json.addProperty("lava", lava);
        json.addProperty("water", water);

        json.addProperty("diamond", diamond);
        json.addProperty("ancient", ancient);
        json.addProperty("emerald", emerald);
        json.addProperty("gold", gold);
        json.addProperty("iron", iron);
        json.addProperty("redstone", redstone);
        json.addProperty("lapis", lapis);
        json.addProperty("copper", copper);
        json.addProperty("coal", coal);
        json.addProperty("quartz", quartz);

        json.addProperty("hiddenOpacity", hiddenOpacity);
        json.addProperty("range", range);

        return json;
    }

    public static void load(JsonObject json) {

        if (json == null) {
            return;
        }

        bedrock = bool(json, "bedrock", bedrock);
        lava = bool(json, "lava", lava);
        water = bool(json, "water", water);

        diamond = bool(json, "diamond", diamond);
        ancient = bool(json, "ancient", ancient);
        emerald = bool(json, "emerald", emerald);
        gold = bool(json, "gold", gold);
        iron = bool(json, "iron", iron);
        redstone = bool(json, "redstone", redstone);
        lapis = bool(json, "lapis", lapis);
        copper = bool(json, "copper", copper);
        coal = bool(json, "coal", coal);
        quartz = bool(json, "quartz", quartz);

        if (json.has("hiddenOpacity")) {
            setHiddenOpacity(json.get("hiddenOpacity").getAsInt());
        }

        if (json.has("range")) {
            setRange(json.get("range").getAsInt());
        }
    }

    private static boolean bool(
        JsonObject json,
        String key,
        boolean fallback
    ) {

        if (!json.has(key)) {
            return fallback;
        }

        try {
            return json.get(key).getAsBoolean();
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
