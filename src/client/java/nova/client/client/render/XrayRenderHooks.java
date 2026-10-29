package nova.client.client.render;

// @name: XrayRenderHooks
// @type: Java Client Rendering Helper
// @location: src/client/java/nova/client/client/render/XrayRenderHooks.java

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.XrayBlockFilter;
import nova.client.client.module.XraySettings;

public final class XrayRenderHooks {

    private XrayRenderHooks() {
    }

    /**
     * Returns whether Nova X-Ray is currently enabled.
     */
    public static boolean isEnabled() {

        Module module =
            NovaClient.MODULES.getByName("X-Ray");

        return module != null && module.isEnabled();
    }

    /**
     * Converts the user's percentage opacity setting
     * into Minecraft's 0-255 alpha range.
     *
     * 0%   = fully invisible
     * 20%  = approximately 51 alpha
     * 100% = fully opaque
     */
    public static int getHiddenAlpha() {

        int percentage = Math.max(
            0,
            Math.min(100, XraySettings.hiddenOpacity)
        );

        return Math.round(
            percentage * 255.0F / 100.0F
        );
    }

    /**
     * Determines the desired alpha for a terrain block.
     *
     * -1 means preserve vanilla rendering.
     * 255 means fully visible.
     * 0-254 means render with custom transparency.
     */
    public static int getBlockAlpha(
        BlockState state,
        BlockPos pos
    ) {

        if (!isEnabled() || state == null) {
            return -1;
        }

        if (XrayBlockFilter.shouldShow(state)) {
            return 255;
        }

        return getHiddenAlpha();
    }

    /**
     * Determines desired alpha for a fluid.
     *
     * Enabled water/lava remain fully visible.
     * Other fluids use the hidden-block opacity.
     */
    public static int getFluidAlpha(
        FluidState state,
        BlockPos pos
    ) {

        if (!isEnabled() || state == null) {
            return -1;
        }

        if (state.isEmpty()) {
            return -1;
        }

        BlockState legacyState =
            state.createLegacyBlock();

        if (XrayBlockFilter.shouldShow(legacyState)) {
            return 255;
        }

        return getHiddenAlpha();
    }

    /**
     * Returns true when the selected block should
     * remain completely visible.
     */
    public static boolean isSelected(BlockState state) {

        return isEnabled()
            && state != null
            && XrayBlockFilter.shouldShow(state);
    }

    /**
     * Returns true when the block should use
     * the X-Ray transparency treatment.
     */
    public static boolean shouldFade(BlockState state) {

        return isEnabled()
            && state != null
            && !XrayBlockFilter.shouldShow(state);
    }

    /**
     * Requests a rebuild after a settings change.
     */
    public static void settingsChanged() {

        XrayChunkRefresh.requestRefresh();
    }
}
