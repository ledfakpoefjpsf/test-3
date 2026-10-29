
package nova.client.client.macro;

// @name: MacroEngine
// @type: Client Java Class
// @location: src/client/java/nova/client/client/macro/MacroEngine.java

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
        boolean attack,
        boolean use
    ) {}

    private static final List<Frame> frames =
        new ArrayList<>();

    private static boolean recording = false;
    private static boolean playing = false;
    private static boolean mining = false;
    private static boolean looping = false;

    private static boolean oldX = false;
    private static boolean oldF7 = false;
    private static boolean oldF8 = false;

    private static int playbackIndex = 0;
    private static final int MAX_FRAMES = 12000;

    private static String selected = "default";

    private MacroEngine() {}

    private static Path directory() {
        return Minecraft.getInstance()
            .gameDirectory.toPath()
            .resolve("config/nova/macros");
    }

    private static String safe(String name) {
        if (name == null || name.isBlank()) {
            return "default";
        }

        String cleaned = name.replaceAll(
            "[^a-zA-Z0-9_-]", "_"
        );

        if (cleaned.isEmpty()) {
            return "default";
        }

        return cleaned.substring(
            0, Math.min(40, cleaned.length())
        );
    }

    private static Path file(String name) {
        return directory().resolve(
            safe(name) + ".nmacro"
        );
    }

    public static String selected() {
        return selected;
    }

    public static void select(String name) {
        selected = safe(name);
    }

    public static boolean isRecording() {
        return recording;
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static boolean isMining() {
        return mining;
    }

    public static boolean isLooping() {
        return looping;
    }

    public static void setLooping(boolean value) {
        looping = value;
    }

    public static int frameCount() {
        return frames.size();
    }

    public static List<String> savedNames() {
        try {
            Files.createDirectories(directory());

            try (var stream = Files.list(directory())) {
                return stream
                    .filter(p -> p.getFileName()
                        .toString().endsWith(".nmacro"))
                    .map(p -> p.getFileName()
                        .toString()
                        .replaceFirst("\\.nmacro$", ""))
                    .sorted()
                    .toList();
            }
        } catch (IOException e) {
            System.err.println(
                "[Nova Macro] List failed: " + e
            );
            return List.of();
        }
    }

    public static boolean save(String name) {
        if (frames.isEmpty()) {
            return false;
        }

        try {
            Files.createDirectories(directory());

            try (DataOutputStream out =
                new DataOutputStream(
                    Files.newOutputStream(file(name))
                )) {

                out.writeInt(0x4E4D4143);
                out.writeInt(1);
                out.writeInt(frames.size());

                for (Frame frame : frames) {
                    int bits = 0;

                    for (int i = 0; i < KEYS.length; i++) {
                        if (frame.keys[i]) {
                            bits |= 1 << i;
                        }
                    }

                    if (frame.attack) {
                        bits |= 1 << 7;
                    }

                    if (frame.use) {
                        bits |= 1 << 8;
                    }

                    out.writeShort(bits);
                }
            }

            select(name);
            System.out.println(
                "[Nova Macro] Saved: " + selected
            );
            return true;

        } catch (IOException e) {
            System.err.println(
                "[Nova Macro] Save failed: " + e
            );
            return false;
        }
    }

    public static boolean load(String name) {
        try (DataInputStream in =
            new DataInputStream(
                Files.newInputStream(file(name))
            )) {

            if (in.readInt() != 0x4E4D4143 ||
                in.readInt() != 1) {
                return false;
            }

            int count = in.readInt();

            if (count < 0 || count > MAX_FRAMES) {
                return false;
            }

            List<Frame> loaded =
                new ArrayList<>();

            for (int n = 0; n < count; n++) {
                int bits = in.readUnsignedShort();
                boolean[] keys =
                    new boolean[KEYS.length];

                for (int i = 0; i < KEYS.length; i++) {
                    keys[i] =
                        (bits & (1 << i)) != 0;
                }

                loaded.add(new Frame(
                    keys,
                    (bits & 128) != 0,
                    (bits & 256) != 0
                ));
            }

            stop(Minecraft.getInstance());
            frames.clear();
            frames.addAll(loaded);
            select(name);

            return true;

        } catch (IOException e) {
            System.err.println(
                "[Nova Macro] Load failed: " + e
            );
            return false;
        }
    }

    public static boolean delete(String name) {
        try {
            return Files.deleteIfExists(file(name));
        } catch (IOException e) {
            return false;
        }
    }

    public static void startRecording(Minecraft mc) {
        stop(mc);
        frames.clear();
        playbackIndex = 0;
        recording = true;

        System.out.println(
            "[Nova Macro] Recording started: " + selected
        );
    }

    public static void finishRecording() {
        if (!recording) {
            return;
        }

        recording = false;

        boolean saved = save(selected);

        System.out.println(
            saved
                ? "[Nova Macro] Recording finished and saved"
                : "[Nova Macro] Nothing recorded"
        );
    }

    public static void play(Minecraft mc) {
        stop(mc);

        if (frames.isEmpty()) {
            load(selected);
        }

        if (!frames.isEmpty()) {
            playing = true;
            playbackIndex = 0;
        }
    }

    public static void startAutoMine(Minecraft mc) {
        stop(mc);
        mining = true;
    }

    public static void stop(Minecraft mc) {
        boolean controlled = playing || mining;

        recording = false;
        playing = false;
        mining = false;
        playbackIndex = 0;

        if (controlled && mc != null) {
            release(mc);
        }
    }

    private static KeyMapping[] mappings(Minecraft mc) {
        return new KeyMapping[] {
            mc.options.keyUp,
            mc.options.keyLeft,
            mc.options.keyDown,
            mc.options.keyRight,
            mc.options.keyJump,
            mc.options.keyShift,
            mc.options.keySprint
        };
    }

    private static void release(Minecraft mc) {
        for (KeyMapping key : mappings(mc)) {
            key.setDown(false);
        }

        mc.options.keyAttack.setDown(false);
        mc.options.keyUse.setDown(false);
    }

    private static boolean down(long window, int key) {
        return GLFW.glfwGetKey(window, key)
            == GLFW.GLFW_PRESS;
    }

    // =====================================
    // MAIN MACRO TICK
    // =====================================

    public static void tick(Minecraft mc) {
        if (mc.getWindow() == null) {
            return;
        }

        long window = mc.getWindow().handle();

        boolean x = down(
            window, GLFW.GLFW_KEY_X
        );

        boolean f7 = down(
            window, GLFW.GLFW_KEY_F7
        );

        boolean f8 = down(
            window, GLFW.GLFW_KEY_F8
        );

        boolean inWorld =
            mc.player != null &&
            mc.level != null &&
            mc.screen == null;

        // F8 is the emergency stop.
        if (f8 && !oldF8) {
            stop(mc);
        }

        if (!inWorld) {
            // Finish and save if a menu opens
            // during recording.
            if (recording) {
                finishRecording();
            }

            if (playing || mining) {
                stop(mc);
            }
        } else {

            // =================================
            // X: START / FINISH + SAVE
            // =================================

            if (x && !oldX) {
                if (recording) {
                    finishRecording();
                } else {
                    startRecording(mc);
                }
            }

            // F7: PLAY
            if (f7 && !oldF7) {
                play(mc);
            }

            // =================================
            // RECORD INPUTS
            // =================================

            if (recording) {
                boolean[] keys =
                    new boolean[KEYS.length];

                for (int i = 0; i < KEYS.length; i++) {
                    keys[i] = down(window, KEYS[i]);
                }

                boolean attack =
                    GLFW.glfwGetMouseButton(
                        window,
                        GLFW.GLFW_MOUSE_BUTTON_LEFT
                    ) == GLFW.GLFW_PRESS;

                boolean use =
                    GLFW.glfwGetMouseButton(
                        window,
                        GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    ) == GLFW.GLFW_PRESS;

                frames.add(new Frame(
                    keys, attack, use
                ));

                if (frames.size() >= MAX_FRAMES) {
                    finishRecording();
                }
            }

            // =================================
            // AUTO MINE
            // =================================

            if (mining) {
                mc.options.keyAttack.setDown(true);
            }

            // =================================
            // PLAYBACK
            // =================================

            if (playing) {

                if (playbackIndex >= frames.size()) {
                    if (looping && !frames.isEmpty()) {
                        playbackIndex = 0;
                    } else {
                        stop(mc);
                    }
                }

                if (playing) {
                    Frame frame =
                        frames.get(playbackIndex++);

                    KeyMapping[] keys = mappings(mc);

                    for (int i = 0; i < keys.length; i++) {
                        keys[i].setDown(frame.keys[i]);
                    }

                    mc.options.keyAttack.setDown(
                        frame.attack
                    );

                    mc.options.keyUse.setDown(
                        frame.use
                    );
                }
            }
        }

        oldX = x;
        oldF7 = f7;
        oldF8 = f8;
    }
}
