package nova.client.client.render;

// @name: XrayRenderer
// @type: Client Renderer
// @location: src/client/java/nova/client/client/render/XrayRenderer.java

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import nova.client.client.NovaClient;
import nova.client.client.module.Module;
import nova.client.client.module.XraySettings;
import nova.client.client.module.XrayBlockFilter;

import java.util.ArrayList;
import java.util.List;

public final class XrayRenderer {

    private static final int GRAY = 0xFF888888;
    private static final int ORANGE = 0xFFFF6500;
    private static final int BLUE = 0xFF2196F3;
    private static final int CYAN = 0xFF00FFFF;
    private static final int PURPLE = 0xFFB070FF;
    private static final int GREEN = 0xFF00FF66;
    private static final int GOLD = 0xFFFFD700;
    private static final int WHITE = 0xFFE6E6E6;
    private static final int RED = 0xFFFF3333;
    private static final int LAPIS = 0xFF3366FF;
    private static final int COPPER = 0xFFCC7733;
    private static final int COAL = 0xFF777777;

    private static final List<Marker> MARKERS =
        new ArrayList<>();

    private static int scanIndex = 0;
    private static int lastRange = -1;
    private static int lastX = Integer.MIN_VALUE;
    private static int lastY = Integer.MIN_VALUE;
    private static int lastZ = Integer.MIN_VALUE;

    private static final int MAX_MARKERS = 6000;
    private static final int BLOCKS_PER_TICK = 12000;

    private record Marker(BlockPos pos, int color) {}

    private XrayRenderer() {}

    public static void clear() {
        MARKERS.clear();
        scanIndex = 0;
        lastRange = -1;
        lastX = Integer.MIN_VALUE;
        lastY = Integer.MIN_VALUE;
        lastZ = Integer.MIN_VALUE;
    }

    public static void tick() {

        Minecraft mc = Minecraft.getInstance();

        Module module =
            NovaClient.MODULES.getByName("X-Ray");

        if (module == null
            || !module.isEnabled()
            || mc.player == null
            || mc.level == null) {

            clear();
            return;
        }

        int px = mc.player.getBlockX();
        int py = mc.player.getBlockY();
        int pz = mc.player.getBlockZ();

        int range = XraySettings.range;

        if (px != lastX
            || py != lastY
            || pz != lastZ
            || range != lastRange) {

            MARKERS.clear();
            scanIndex = 0;

            lastX = px;
            lastY = py;
            lastZ = pz;
            lastRange = range;
        }

        int diameter = range * 2 + 1;
        long area = (long) diameter * diameter;
        long total = area * diameter;

        int scanned = 0;
        BlockPos.MutableBlockPos pos =
            new BlockPos.MutableBlockPos();

        while (scanned < BLOCKS_PER_TICK
            && scanIndex < total
            && MARKERS.size() < MAX_MARKERS) {

            long index = scanIndex++;

            int dx = (int) (index % diameter) - range;
            int dz = (int) ((index / diameter) % diameter) - range;
            int dy = (int) (index / area) - range;

            scanned++;

            // Keep the scan inside a sphere.
            if ((long) dx * dx
                + (long) dy * dy
                + (long) dz * dz
                > (long) range * range) {
                continue;
            }

            pos.set(px + dx, py + dy, pz + dz);

            if (pos.getY() < mc.level.getMinY()
                || pos.getY() >= mc.level.getMaxY()) {
                continue;
            }

            if (!mc.level.hasChunkAt(pos)) {
                continue;
            }

            BlockState state = mc.level.getBlockState(pos);

            if (!XrayBlockFilter.shouldShow(state)) {
                continue;
            }

            int color = colorFor(state);

            MARKERS.add(
                new Marker(pos.immutable(), color)
            );
        }
    }

    public static void render(Minecraft mc) {

        if (mc == null
            || mc.player == null
            || mc.level == null) {
            return;
        }

        Module module =
            NovaClient.MODULES.getByName("X-Ray");

        if (module == null || !module.isEnabled()) {
            return;
        }

        double rangeSquared =
            (double) XraySettings.range * XraySettings.range;

        Vec3 player = mc.player.position();

        for (Marker marker : MARKERS) {

            BlockPos pos = marker.pos();

            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;

            double dx = cx - player.x;
            double dy = cy - player.y;
            double dz = cz - player.z;

            if (dx * dx + dy * dy + dz * dz
                > rangeSquared) {
                continue;
            }

            drawBox(pos, marker.color());
        }
    }

    private static int colorFor(BlockState state) {

        Block block = state.getBlock();

        if (block == Blocks.BEDROCK) return GRAY;

        if (state.getFluidState().is(
            net.minecraft.tags.FluidTags.LAVA
        )) return ORANGE;

        if (state.getFluidState().is(
            net.minecraft.tags.FluidTags.WATER
        )) return BLUE;

        if (block == Blocks.DIAMOND_ORE
            || block == Blocks.DEEPSLATE_DIAMOND_ORE)
            return CYAN;

        if (block == Blocks.ANCIENT_DEBRIS)
            return PURPLE;

        if (block == Blocks.EMERALD_ORE
            || block == Blocks.DEEPSLATE_EMERALD_ORE)
            return GREEN;

        if (block == Blocks.GOLD_ORE
            || block == Blocks.DEEPSLATE_GOLD_ORE
            || block == Blocks.NETHER_GOLD_ORE)
            return GOLD;

        if (block == Blocks.REDSTONE_ORE
            || block == Blocks.DEEPSLATE_REDSTONE_ORE)
            return RED;

        if (block == Blocks.LAPIS_ORE
            || block == Blocks.DEEPSLATE_LAPIS_ORE)
            return LAPIS;

        if (block == Blocks.COPPER_ORE
            || block == Blocks.DEEPSLATE_COPPER_ORE)
            return COPPER;

        if (block == Blocks.COAL_ORE
            || block == Blocks.DEEPSLATE_COAL_ORE)
            return COAL;

        if (block == Blocks.NETHER_QUARTZ_ORE)
            return WHITE;

        return WHITE;
    }

    private static void drawBox(BlockPos pos, int color) {

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        Vec3 a = new Vec3(x, y, z);
        Vec3 b = new Vec3(x + 1, y, z);
        Vec3 c = new Vec3(x + 1, y, z + 1);
        Vec3 d = new Vec3(x, y, z + 1);

        Vec3 e = new Vec3(x, y + 1, z);
        Vec3 f = new Vec3(x + 1, y + 1, z);
        Vec3 g = new Vec3(x + 1, y + 1, z + 1);
        Vec3 h = new Vec3(x, y + 1, z + 1);

        line(a, b, color);
        line(b, c, color);
        line(c, d, color);
        line(d, a, color);

        line(e, f, color);
        line(f, g, color);
        line(g, h, color);
        line(h, e, color);

        line(a, e, color);
        line(b, f, color);
        line(c, g, color);
        line(d, h, color);
    }

    private static void line(
        Vec3 start,
        Vec3 end,
        int color
    ) {
        Gizmos.line(
            start,
            end,
            color,
            1.5F
        ).setAlwaysOnTop();
    }
}
