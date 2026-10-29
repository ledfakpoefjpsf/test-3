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
     * Requests a terrain rebuild when X-Ray settings change.
     */
    public static void requestRefresh() {
        refreshRequested = true;
    }

    /**
     * Runs once per client tick.
     */
    public static void tick(Minecraft client) {

        if (client == null) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
        }

        if (!refreshRequested || cooldown > 0) {
            return;
        }

        if (client.level == null) {
            return;
        }

        if (client.levelRenderer == null) {
            return;
        }

        refreshRequested = false;

        // Force Minecraft to rebuild terrain geometry.
        client.levelRenderer.allChanged();

        cooldown = 10;

        System.out.println(
            "[Nova X-Ray] Terrain refresh requested."
        );
    }

    public static boolean isRefreshPending() {
        return refreshRequested;
    }

    public static void reset() {
        refreshRequested = false;
        cooldown = 0;
    }
}
