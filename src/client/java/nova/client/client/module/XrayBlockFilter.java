
package nova.client.client.module;

// @name: XrayBlockFilter
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/XrayBlockFilter.java
//
// Adapted from Meteor Client's Xray.java.
// Original project: https://github.com/MeteorDevelopment/meteor-client
// Meteor Client is licensed under GPL-3.0.

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class XrayBlockFilter {

    private XrayBlockFilter() {
    }

    public static boolean shouldShow(BlockState state) {

        if (state == null) {
            return false;
        }

        Block block = state.getBlock();

        if (XraySettings.bedrock
                && block == Blocks.BEDROCK) {
            return true;
        }

        if (XraySettings.lava
                && state.getFluidState().is(FluidTags.LAVA)) {
            return true;
        }

        if (XraySettings.water
                && state.getFluidState().is(FluidTags.WATER)) {
            return true;
        }

        if (XraySettings.diamond
                && (block == Blocks.DIAMOND_ORE
                || block == Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return true;
        }

        if (XraySettings.ancient
                && block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }

        if (XraySettings.emerald
                && (block == Blocks.EMERALD_ORE
                || block == Blocks.DEEPSLATE_EMERALD_ORE)) {
            return true;
        }

        if (XraySettings.gold
                && (block == Blocks.GOLD_ORE
                || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.NETHER_GOLD_ORE)) {
            return true;
        }

        if (XraySettings.iron
                && (block == Blocks.IRON_ORE
                || block == Blocks.DEEPSLATE_IRON_ORE)) {
            return true;
        }

        if (XraySettings.redstone
                && (block == Blocks.REDSTONE_ORE
                || block == Blocks.DEEPSLATE_REDSTONE_ORE)) {
            return true;
        }

        if (XraySettings.lapis
                && (block == Blocks.LAPIS_ORE
                || block == Blocks.DEEPSLATE_LAPIS_ORE)) {
            return true;
        }

        if (XraySettings.copper
                && (block == Blocks.COPPER_ORE
                || block == Blocks.DEEPSLATE_COPPER_ORE)) {
            return true;
        }

        if (XraySettings.coal
                && (block == Blocks.COAL_ORE
                || block == Blocks.DEEPSLATE_COAL_ORE)) {
            return true;
        }

        if (XraySettings.quartz
                && block == Blocks.NETHER_QUARTZ_ORE) {
            return true;
        }

        return false;
    }

    public static boolean shouldHide(BlockState state) {
        return !shouldShow(state);
    }

    /**
     * Meteor-style face-culling decision.
     *
     * Called by a rendering mixin when Minecraft
     * decides whether a block face should render.
     */
    public static boolean modifyDrawSide(
            BlockState state,
            BlockGetter world,
            BlockPos pos,
            Direction direction,
            boolean vanillaResult) {

        if (state == null || world == null
                || pos == null || direction == null) {
            return vanillaResult;
        }

        if (vanillaResult || !shouldShow(state)) {
            return vanillaResult;
        }

        BlockPos adjacentPos = pos.relative(direction);

        BlockState adjacentState =
                world.getBlockState(adjacentPos);

        // If the neighboring block will be faded,
        // the selected ore's face must remain visible.
        if (shouldHide(adjacentState)) {
            return true;
        }

        // Preserve vanilla's decision when two selected
        // blocks touch each other.
        return vanillaResult;
    }
}
