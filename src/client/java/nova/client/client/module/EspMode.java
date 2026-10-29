
package nova.client.client.module;

// @name: EspMode
// @type: Java Enum
// @location: src/client/java/nova/client/client/module/EspMode.java

public enum EspMode {
    SHADER,
    BOX;

    private static EspMode current = SHADER;

    public static EspMode getCurrent() {
        return current;
    }

    public static void setCurrent(EspMode mode) {
        if (mode != null) {
            current = mode;
        }
    }

    public static void toggleMode() {
        current = current == SHADER ? BOX : SHADER;
    }
}
