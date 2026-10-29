
package nova.client.client.render;

// @name: MinimapRenderer
// @type: Client HUD Renderer
// @location: src/client/java/nova/client/client/render/MinimapRenderer.java

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;

public final class MinimapRenderer {

    private static final int SIZE = 41;
    private static final int PIXEL = 3;
    private static final int MAP_SIZE = SIZE * PIXEL;

    private static final int RANGE_STEP = 2;

    private static final int[] COLORS = new int[SIZE * SIZE];

    private static int lastX = Integer.MIN_VALUE;
    private static int lastZ = Integer.MIN_VALUE;
    private static int lastTick = Integer.MIN_VALUE;

    private static int ticks = 0;

    private MinimapRenderer() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(
            (graphics, tickCounter) -> render(graphics)
        );
    }

    private static boolean enabled() {
        Module module =
            NovaClient.MODULES.getByName("Minimap");

        return module != null && module.isEnabled();
    }

    public static void tick() {
        ticks++;
    }

    private static void render(GuiGraphics graphics) {

        Minecraft mc = Minecraft.getInstance();

        if (!enabled()
            || mc.player == null
            || mc.level == null
            || mc.options.hideGui) {
            return;
        }

        int centerX = mc.player.getBlockX();
        int centerZ = mc.player.getBlockZ();

        if (centerX != lastX
            || centerZ != lastZ
            || ticks - lastTick >= 10) {

            updateTerrain(mc, centerX, centerZ);

            lastX = centerX;
            lastZ = centerZ;
            lastTick = ticks;
        }

        int x = graphics.guiWidth() - MAP_SIZE - 14;
        int y = 25;

        // Outer shadow.
        graphics.fill(
            x - 5, y - 18,
            x + MAP_SIZE + 5,
            y + MAP_SIZE + 23,
            0xB0090614
        );

        // Purple border.
        graphics.fill(
            x - 2, y - 2,
            x + MAP_SIZE + 2,
            y + MAP_SIZE + 2,
            0xFFAA75FF
        );

        // Background.
        graphics.fill(
            x, y,
            x + MAP_SIZE,
            y + MAP_SIZE,
            0xFF161120
        );

        // Terrain pixels.
        for (int z = 0; z < SIZE; z++) {
            for (int bx = 0; bx < SIZE; bx++) {

                int color = COLORS[z * SIZE + bx];

                int px = x + bx * PIXEL;
                int py = y + z * PIXEL;

                graphics.fill(
                    px, py,
                    px + PIXEL,
                    py + PIXEL,
                    color
                );
            }
        }

        int cx = x + MAP_SIZE / 2;
        int cy = y + MAP_SIZE / 2;

        // Actual player marker.
        graphics.fill(
            cx - 3, cy - 3,
            cx + 4, cy + 4,
            0xFF12091E
        );

        graphics.fill(
            cx - 2, cy - 2,
            cx + 3, cy + 3,
            0xFFDDBBFF
        );

        // Player facing indicator.
        double radians =
            Math.toRadians(mc.player.getYRot());

        int dx = (int) Math.round(
            -Math.sin(radians) * 9
        );

        int dz = (int) Math.round(
            Math.cos(radians) * 9
        );

        graphics.fill(
            cx + dx - 1,
            cy + dz - 1,
            cx + dx + 2,
            cy + dz + 2,
            0xFFFFFFFF
        );

        // Title.
        graphics.drawString(
            mc.font,
            "NOVA MINIMAP",
            x,
            y - 14,
            0xFFD7B7FF,
            true
        );

        // Compass.
        graphics.drawString(
            mc.font,
            "N",
            cx - 3,
            y + 3,
            0xFFFFDD88,
            true
        );

        // Coordinates.
        String coords =
            centerX + ", " + centerZ;

        graphics.drawString(
            mc.font,
            coords,
            x,
            y + MAP_SIZE + 7,
            0xFFCCCCDD,
            true
        );
    }

    private static void updateTerrain(
        Minecraft mc,
        int centerX,
        int centerZ
    ) {

        for (int z = 0; z < SIZE; z++) {
            for (int x = 0; x < SIZE; x++) {

                int worldX =
                    centerX + (x - SIZE / 2) * RANGE_STEP;

                int worldZ =
                    centerZ + (z - SIZE / 2) * RANGE_STEP;

                int color = 0xFF181320;

                BlockPos column =
                    new BlockPos(
                        worldX,
                        mc.player.getBlockY(),
                        worldZ
                    );

                if (mc.level.hasChunkAt(column)) {

                    int height = mc.level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        worldX,
                        worldZ
                    );

                    BlockPos surface =
                        new BlockPos(
                            worldX,
                            height - 1,
                            worldZ
                        );

                    BlockState state =
                        mc.level.getBlockState(surface);

                    color = terrainColor(state);

                    // Slight elevation shading.
                    int difference =
                        height - mc.player.getBlockY();

                    if (difference > 12) {
                        color = darken(color);
                    }
                }

                COLORS[z * SIZE + x] = color;
            }
        }
    }

    private static int terrainColor(BlockState state) {

        if (state.is(Blocks.GRASS_BLOCK)
            || state.is(Blocks.SHORT_GRASS)
            || state.is(Blocks.TALL_GRASS)) {
            return 0xFF4D9651;
        }

        if (state.is(Blocks.WATER)) {
            return 0xFF3164B8;
        }

        if (state.is(Blocks.LAVA)) {
            return 0xFFFF7133;
        }

        if (state.is(Blocks.SAND)
            || state.is(Blocks.RED_SAND)
            || state.is(Blocks.SANDSTONE)) {
            return 0xFFE2C17C;
        }

        if (state.is(Blocks.SNOW)
            || state.is(Blocks.SNOW_BLOCK)
            || state.is(Blocks.ICE)
            || state.is(Blocks.PACKED_ICE)) {
            return 0xFFE7F0FF;
        }

        if (state.is(Blocks.STONE)
            || state.is(Blocks.COBBLESTONE)
            || state.is(Blocks.DEEPSLATE)
            || state.is(Blocks.GRAVEL)) {
            return 0xFF777785;
        }

        if (state.is(Blocks.OAK_LEAVES)
            || state.is(Blocks.SPRUCE_LEAVES)
            || state.is(Blocks.BIRCH_LEAVES)
            || state.is(Blocks.JUNGLE_LEAVES)
            || state.is(Blocks.DARK_OAK_LEAVES)
            || state.is(Blocks.ACACIA_LEAVES)
            || state.is(Blocks.MANGROVE_LEAVES)) {
            return 0xFF287B40;
        }

        if (state.is(Blocks.OAK_LOG)
            || state.is(Blocks.SPRUCE_LOG)
            || state.is(Blocks.BIRCH_LOG)) {
            return 0xFF87633E;
        }

        if (state.is(Blocks.DIRT)
            || state.is(Blocks.COARSE_DIRT)
            || state.is(Blocks.ROOTED_DIRT)) {
            return 0xFF806044;
        }

        if (state.is(Blocks.NETHERRACK)) {
            return 0xFF833D43;
        }

        if (state.is(Blocks.END_STONE)) {
            return 0xFFE2E3A5;
        }

        return 0xFF817A8C;
    }

    private static int darken(int color) {

        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        r = (int) (r * 0.75);
        g = (int) (g * 0.75);
        b = (int) (b * 0.75);

        return 0xFF000000
            | (r << 16)
            | (g << 8)
            | b;
    }
}
