
package nova.client.client.macro;

// @name: MacroEngine
// @type: Java Class
// @location: src/client/java/nova/client/client/macro/MacroEngine.java

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

public final class MacroEngine {

    private static final int[] KEYS = {
        GLFW.GLFW_KEY_W,
        GLFW.GLFW_KEY_A,
        GLFW.GLFW_KEY_S,
        GLFW.GLFW_KEY_D,
        GLFW.GLFW_KEY_SPACE,
        GLFW.GLFW_KEY_LEFT_SHIFT,
        GLFW.GLFW_KEY_LEFT_CONTROL
    };

    private record Frame(
        boolean[] keys,
        boolean leftMouse,
        boolean rightMouse
    ) {}

    private static final List<Frame> frames =
        new ArrayList<>();

    private static boolean recording = false;
    private static boolean playing = false;

    private static boolean oldF6 = false;
    private static boolean oldF7 = false;
    private static boolean oldF8 = false;

    private static int playbackIndex = 0;

    private MacroEngine() {}

    public static void tick(Minecraft mc) {

        if (mc.getWindow() == null) {
            return;
        }

        long window = mc.getWindow().handle();

        boolean f6 = isDown(window, GLFW.GLFW_KEY_F6);
        boolean f7 = isDown(window, GLFW.GLFW_KEY_F7);
        boolean f8 = isDown(window, GLFW.GLFW_KEY_F8);

        if (f8 && !oldF8) {
            stop(mc);
        }

        if (mc.player != null && mc.screen == null) {

            if (f6 && !oldF6) {

                if (recording) {
                    recording = false;
                    System.out.println(
                        "[Nova Macro] Recording saved: " +
                        frames.size() + " ticks"
                    );
                } else {
                    stop(mc);
                    frames.clear();
                    recording = true;

                    System.out.println(
                        "[Nova Macro] Recording started"
                    );
                }
            }

            if (f7 && !oldF7 && !frames.isEmpty()) {
                stop(mc);
                playing = true;
                playbackIndex = 0;

                System.out.println(
                    "[Nova Macro] Playback started"
                );
            }

            if (recording) {
                record(window);
            }

            if (playing) {
                if (playbackIndex < frames.size()) {
                    apply(mc, frames.get(playbackIndex));
                    playbackIndex++;
                } else {
                    stop(mc);
                }
            }
        } else if (recording || playing) {
            stop(mc);
        }

        oldF6 = f6;
        oldF7 = f7;
        oldF8 = f8;
    }

    private static boolean isDown(long window, int key) {
        return GLFW.glfwGetKey(window, key)
            == GLFW.GLFW_PRESS;
    }

    private static void record(long window) {

        boolean[] keys = new boolean[KEYS.length];

        for (int i = 0; i < KEYS.length; i++) {
            keys[i] = isDown(window, KEYS[i]);
        }

        boolean leftMouse =
            GLFW.glfwGetMouseButton(
                window,
                GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == GLFW.GLFW_PRESS;

        boolean rightMouse =
            GLFW.glfwGetMouseButton(
                window,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT
            ) == GLFW.GLFW_PRESS;

        frames.add(new Frame(
            keys,
            leftMouse,
            rightMouse
        ));

        if (frames.size() >= 12000) {
            recording = false;
            System.out.println(
                "[Nova Macro] Maximum recording length reached"
            );
        }
    }

    private static void apply(Minecraft mc, Frame frame) {

        KeyMapping[] mappings = {
            mc.options.keyUp,
            mc.options.keyLeft,
            mc.options.keyDown,
            mc.options.keyRight,
            mc.options.keyJump,
            mc.options.keyShift,
            mc.options.keySprint
        };

        for (int i = 0; i < mappings.length; i++) {
            mappings[i].setDown(frame.keys()[i]);
        }

        mc.options.keyAttack.setDown(frame.leftMouse());
        mc.options.keyUse.setDown(frame.rightMouse());
    }

    public static void stop(Minecraft mc) {

        recording = false;

        if (playing) {
            playing = false;

            mc.options.keyUp.setDown(false);
            mc.options.keyLeft.setDown(false);
            mc.options.keyDown.setDown(false);
            mc.options.keyRight.setDown(false);
            mc.options.keyJump.setDown(false);
            mc.options.keyShift.setDown(false);
            mc.options.keySprint.setDown(false);
            mc.options.keyAttack.setDown(false);
            mc.options.keyUse.setDown(false);

            System.out.println(
                "[Nova Macro] Playback stopped"
            );
        }
    }
}
