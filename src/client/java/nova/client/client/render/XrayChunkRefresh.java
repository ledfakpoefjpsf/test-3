package nova.client.client.render;

// @name: XrayChunkRefresh
// @type: Client Java Class
// @location: src/client/java/nova/client/client/render/XrayChunkRefresh.java

import net.minecraft.client.Minecraft;

public final class XrayChunkRefresh {

    private static boolean refreshRequested = false;

    private static int cooldown = 0;

    private XrayChunkRefresh() {
    }

    /**
     * Requests a rebuild of the currently loaded terrain.
     *
     * Call this when:
     * - X-Ray is enabled
     * - X-Ray is disabled
     * - An ore filter changes
     * - Bedrock, lava or water visibility changes
     * - Terrain-rendering settings change
     */
    public static void requestRefresh() {

        refreshRequested = true;
    }

    /**
     * Processes refresh requests on the Minecraft client thread.
     */
    public static void tick(Minecraft client) {

        if (client == null) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
        }

        if (!refreshRequested) {
            return;
        }

        if (cooldown > 0) {
            return;
        }

        if (client.level == null) {
            return;
        }

        if (client.levelRenderer == null) {
            return;
        }

        refreshRequested = false;

        // Rebuild loaded terrain using the current
        // block-rendering rules.
        client.levelRenderer.allChanged();

        // Prevent repeated full rebuilds every tick.
        cooldown = 10;

        System.out.println(
            "[Nova X-Ray] Terrain refresh requested."
        );
    }

    /**
     * Returns whether a refresh is waiting.
     */
    public static boolean isRefreshPending() {

        return refreshRequested;
    }

    /**
     * Resets internal refresh state.
     */
    public static void reset() {

        refreshRequested = false;
        cooldown = 0;
    }
}
