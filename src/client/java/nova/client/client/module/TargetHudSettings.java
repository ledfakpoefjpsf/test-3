
package nova.client.client.module;

// @name: TargetHudSettings
// @type: Client Settings Class
// @location: src/client/java/nova/client/client/module/TargetHudSettings.java

public final class TargetHudSettings {

    private static int x = -1;
    private static int y = -1;

    private static long holdMillis = 1800L;

    private TargetHudSettings() {}

    public static int getX(int screenWidth, int panelWidth) {
        if (x < 0) {
            return (screenWidth - panelWidth) / 2;
        }
        return Math.max(0, Math.min(x, screenWidth - panelWidth));
    }

    public static int getY(int screenHeight, int panelHeight) {
        if (y < 0) {
            return Math.max(10, screenHeight / 2 + 32);
        }
        return Math.max(0, Math.min(y, screenHeight - panelHeight));
    }

    public static void setPosition(int newX, int newY) {
        x = Math.max(0, newX);
        y = Math.max(0, newY);
    }

    public static long getHoldMillis() {
        return holdMillis;
    }

    public static void setHoldMillis(long value) {
        holdMillis = Math.max(0L, Math.min(10000L, value));
    }
}
