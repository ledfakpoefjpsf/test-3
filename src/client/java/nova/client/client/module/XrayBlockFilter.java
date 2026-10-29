package nova.client.client.module;

// @name: XrayBlockFilter
// @type: Client Java Class
// @location: src/client/java/nova/client/client/module/XrayBlockFilter.java

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

        // BEDROCK
        if (XraySettings.bedrock
            && block == Blocks.BEDROCK) {
            return true;
        }

        // LAVA
        if (XraySettings.lava
            && !state.getFluidState().isEmpty()
            && state.getFluidState().is(
                net.minecraft.tags.FluidTags.LAVA
            )) {
            return true;
        }

        // WATER
        if (XraySettings.water
            && !state.getFluidState().isEmpty()
            && state.getFluidState().is(
                net.minecraft.tags.FluidTags.WATER
            )) {
            return true;
        }

        // DIAMOND
        if (XraySettings.diamond
            && (
                block == Blocks.DIAMOND_ORE
                || block == Blocks.DEEPSLATE_DIAMOND_ORE
            )) {
            return true;
        }

        // ANCIENT DEBRIS
        if (XraySettings.ancient
            && block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }

        // EMERALD
        if (XraySettings.emerald
            && (
                block == Blocks.EMERALD_ORE
                || block == Blocks.DEEPSLATE_EMERALD_ORE
            )) {
            return true;
        }

        // GOLD
        if (XraySettings.gold
            && (
                block == Blocks.GOLD_ORE
                || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.NETHER_GOLD_ORE
            )) {
            return true;
        }

        // IRON
        if (XraySettings.iron
            && (
                block == Blocks.IRON_ORE
                || block == Blocks.DEEPSLATE_IRON_ORE
            )) {
            return true;
        }

        // REDSTONE
        if (XraySettings.redstone
            && (
                block == Blocks.REDSTONE_ORE
                || block == Blocks.DEEPSLATE_REDSTONE_ORE
            )) {
            return true;
        }

        // LAPIS LAZULI
        if (XraySettings.lapis
            && (
                block == Blocks.LAPIS_ORE
                || block == Blocks.DEEPSLATE_LAPIS_ORE
            )) {
            return true;
        }

        // COPPER
        if (XraySettings.copper
            && (
                block == Blocks.COPPER_ORE
                || block == Blocks.DEEPSLATE_COPPER_ORE
            )) {
            return true;
        }

        // COAL
        if (XraySettings.coal
            && (
                block == Blocks.COAL_ORE
                || block == Blocks.DEEPSLATE_COAL_ORE
            )) {
            return true;
        }

        // NETHER QUARTZ
        if (XraySettings.quartz
            && block == Blocks.NETHER_QUARTZ_ORE) {
            return true;
        }

        return false;
    }

    public static boolean shouldHide(BlockState state) {
        return !shouldShow(state);
    }
}
