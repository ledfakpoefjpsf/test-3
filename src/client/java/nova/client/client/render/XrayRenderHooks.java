
package nova.client.client.render;

// @name: XrayRenderHooks
// @type: Client Rendering Helper
// @location: src/client/java/nova/client/client/render/XrayRenderHooks.java
//
// Adapted from Meteor Client's Xray.java.
// Original project: https://github.com/MeteorDevelopment/meteor-client
// Meteor Client is licensed under GPL-3.0.

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.XrayBlockFilter;
import nova.client.client.module.XraySettings;

public final class XrayRenderHooks {

    private XrayRenderHooks() {
    }

    public static boolean isEnabled() {

        Module module =
                NovaClient.MODULES.getByName("X-Ray");

        return module != null && module.isEnabled();
    }

    /**
     * Converts Nova's 0-100 percentage into
     * Meteor's 0-255 alpha range.
     */
    public static int getHiddenAlpha() {

        int percent = Math.max(
                0,
                Math.min(100, XraySettings.hiddenOpacity)
        );

        return Math.round(percent * 255.0F / 100.0F);
    }

    /**
     * -1 = use vanilla alpha
     * 0-255 = override block alpha
     */
    public static int getBlockAlpha(
            BlockState state,
            BlockPos pos) {

        if (!isEnabled() || state == null) {
            return -1;
        }

        if (XrayBlockFilter.shouldShow(state)) {
            return -1;
        }

        return getHiddenAlpha();
    }

    /**
     * Fluid alpha calculation.
     */
    public static int getFluidAlpha(
            FluidState state,
            BlockPos pos) {

        if (!isEnabled() || state == null
                || state.isEmpty()) {
            return -1;
        }

        if (XrayBlockFilter.shouldShow(
                state.createLegacyBlock())) {
            return -1;
        }

        return getHiddenAlpha();
    }

    /**
     * Meteor-style adjustment for hidden ore faces.
     */
    public static boolean modifyDrawSide(
            BlockState state,
            BlockGetter world,
            BlockPos pos,
            Direction direction,
            boolean vanillaResult) {

        if (!isEnabled()) {
            return vanillaResult;
        }

        return XrayBlockFilter.modifyDrawSide(
                state,
                world,
                pos,
                direction,
                vanillaResult
        );
    }

    public static boolean isSelected(BlockState state) {

        return isEnabled()
                && XrayBlockFilter.shouldShow(state);
    }

    public static boolean shouldFade(BlockState state) {

        return isEnabled()
                && XrayBlockFilter.shouldHide(state);
    }
}
