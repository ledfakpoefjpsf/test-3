package nova.client.client.module;

// @name: RadarSettings
// @type: Client Settings Class
// @location: src/client/java/nova/client/client/module/RadarSettings.java

public final class RadarSettings {

    public static int range = 64;
    public static int size = 110;

    public static boolean players = true;
    public static boolean hostile = true;
    public static boolean passive = true;
    public static boolean rotate = true;
    public static boolean names = true;
    public static boolean distance = true;

    private RadarSettings() {
    }

    public static void reset() {
        range = 64;
        size = 110;
        players = true;
        hostile = true;
        passive = true;
        rotate = true;
        names = true;
        distance = true;
    }

    public static void setRange(int value) {
        range = Math.max(16, Math.min(256, value));
    }

    public static void setSize(int value) {
        size = Math.max(80, Math.min(200, value));
    }
}
