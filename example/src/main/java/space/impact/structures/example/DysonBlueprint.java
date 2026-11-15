package space.impact.structures.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.render.BlockPosition;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.StructureDefinition;
import space.impact.structures.structure.StructureUtility;

public final class DysonBlueprint {

    public static final BlockPosition OFFSET = new BlockPosition(47, 96, 0);
    private static final String[] CHANNELS = {"dyson_outer", "dyson_middle", "dyson_inner"};
    private static final int[] COLORS = {1, 4, 9};
    private static final Map<BlockPosition, Integer> cells = createCells();
    public static final IStructureDefinition<DysonController> STRUCTURE = createDefinition();

    private DysonBlueprint() {
    }

    public static String channel(int layer) {
        return CHANNELS[layer];
    }

    private static Map<BlockPosition, Integer> createCells() {
        Map<BlockPosition, Integer> blocks = new LinkedHashMap<>();
        for (int layer = 0; layer < DysonGeometry.LAYERS; layer++) {
            for (BlockPosition position : DysonGeometry.frame(layer)) {
                blocks.put(position, layer);
            }
        }
        return Collections.unmodifiableMap(blocks);
    }

    private static IStructureDefinition<DysonController> createDefinition() {
        char[][][] grid = new char[98][97][DysonGeometry.DIAMETER];
        for (char[][] slice : grid) {
            for (char[] row : slice) {
                Arrays.fill(row, ' ');
            }
        }
        for (Map.Entry<BlockPosition, Integer> entry : cells.entrySet()) {
            BlockPosition position = entry.getKey();
            grid[position.z][96 - position.y][47 - position.x] = (char) ('A' + entry.getValue());
        }
        grid[0][96][47] = '~';
        String[][] shape = new String[98][97];
        for (int c = 0; c < shape.length; c++) {
            for (int b = 0; b < shape[c].length; b++) {
                shape[c][b] = new String(grid[c][b]);
            }
        }
        StructureDefinition.Builder<DysonController> builder = StructureDefinition.builder();
        for (int layer = 0; layer < DysonGeometry.LAYERS; layer++) {
            builder.addElement(
                    (char) ('A' + layer),
                    StructureUtility.withChannel(
                            channel(layer),
                            StructureUtility.ofBlock(Blocks.stained_hardened_clay, COLORS[layer])
                    )
            );
        }
        return builder.addShape("main", shape).build();
    }

    public static boolean isLoaded(World world, BlockPosition origin) {
        return world.checkChunksExist(
                origin.x - 49,
                origin.y,
                origin.z,
                origin.x + 48,
                origin.y + 96,
                origin.z + 98
        );
    }

    public static boolean check(DysonController controller) {
        BlockPosition origin = controller.getStructureOrigin();
        return isLoaded(controller.getWorldObj(), origin) && STRUCTURE.check(
                controller,
                "main",
                controller.getWorldObj(),
                ExtendedFacing.DEFAULT,
                origin.x,
                origin.y,
                origin.z,
                OFFSET.x,
                OFFSET.y,
                OFFSET.z,
                false
        );
    }

    public static void place(World world, BlockPosition origin) {
        for (Map.Entry<BlockPosition, Integer> entry : cells.entrySet()) {
            BlockPosition position = entry.getKey();
            world.setBlock(
                    origin.x + position.x,
                    origin.y + position.y,
                    origin.z + position.z,
                    Blocks.stained_hardened_clay,
                    COLORS[entry.getValue()],
                    2
            );
        }
        world.setBlock(origin.x, origin.y, origin.z, AnimationExample.dysonController, 0, 3);
    }
}
