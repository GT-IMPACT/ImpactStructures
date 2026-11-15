package space.impact.structures.example;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.render.BlockAppearance;
import space.impact.structures.render.BlockPosition;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.StructureDefinition;
import space.impact.structures.structure.StructureUtility;

/** Original block-built demo inspired by the silhouette of Satisfactory's space elevator. */
public final class ElevatorBlueprint {

    public static final String CARRIAGE = "carriage";
    public static final int RADIUS = 9;
    public static final int HEIGHT = 65;
    public static final BlockPosition CONTROLLER_POSITION = new BlockPosition(0, 1, -8);
    public static final BlockPosition OFFSET = new BlockPosition(RADIUS, HEIGHT - 1, RADIUS);
    private static final Map<BlockPosition, Character> cells = createCells();
    private static final Map<Character, BlockAppearance> palette = createPalette();
    public static final IStructureDefinition<ElevatorController> STRUCTURE = createDefinition();

    private ElevatorBlueprint() {
    }

    private static Map<BlockPosition, Character> createCells() {
        Map<BlockPosition, Character> blocks = new LinkedHashMap<>();
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                if (Math.abs(x) + Math.abs(z) <= 12) {
                    blocks.put(new BlockPosition(x, 0, z), (Math.abs(x) == 8 || Math.abs(z) == 8) ? 'O' : 'B');
                }
            }
        }
        for (int y = 1; y <= 20; y++) {
            int distance = 7 - y / 8;
            for (int signX : new int[] {-1, 1}) {
                for (int signZ : new int[] {-1, 1}) {
                    blocks.put(new BlockPosition(signX * distance, y, signZ * distance), y % 5 == 0 ? 'L' : 'O');
                    blocks.put(new BlockPosition(signX * (distance + 1), y, signZ * distance), 'B');
                }
            }
        }
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                int radius = x * x + z * z;
                if (radius >= 25 && radius <= 40) {
                    blocks.put(new BlockPosition(x, 21, z), 'O');
                    blocks.put(new BlockPosition(x, 22, z), 'B');
                }
            }
        }
        for (int y = 1; y < HEIGHT; y++) {
            blocks.put(new BlockPosition(0, y, 0), 'T');
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                blocks.put(new BlockPosition(x, 4, z), 'P');
                if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                    for (int y = 5; y <= 8; y++) {
                        blocks.put(new BlockPosition(x, y, z), 'P');
                    }
                }
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    blocks.put(new BlockPosition(x, 9, z), 'P');
                }
            }
        }
        blocks.put(CONTROLLER_POSITION, '~');
        return Collections.unmodifiableMap(blocks);
    }

    private static Map<Character, BlockAppearance> createPalette() {
        Map<Character, BlockAppearance> blocks = new LinkedHashMap<>();
        blocks.put('B', new BlockAppearance(Blocks.stained_hardened_clay, 7));
        blocks.put('O', new BlockAppearance(Blocks.stained_hardened_clay, 1));
        blocks.put('L', new BlockAppearance(Blocks.glowstone, 0));
        blocks.put('T', new BlockAppearance(Blocks.iron_bars, 0));
        blocks.put('P', new BlockAppearance(Blocks.iron_block, 0));
        return blocks;
    }

    private static IStructureDefinition<ElevatorController> createDefinition() {
        String[][] shape = new String[RADIUS * 2 + 1][HEIGHT];
        for (int c = 0; c < shape.length; c++) {
            for (int b = 0; b < HEIGHT; b++) {
                StringBuilder row = new StringBuilder();
                for (int a = 0; a < shape.length; a++) {
                    Character cell = cells.get(new BlockPosition(RADIUS - a, HEIGHT - 1 - b, c - RADIUS));
                    row.append(cell == null ? ' ' : cell);
                }
                shape[c][b] = row.toString();
            }
        }
        StructureDefinition.Builder<ElevatorController> builder = StructureDefinition.builder();
        for (Map.Entry<Character, BlockAppearance> entry : palette.entrySet()) {
            BlockAppearance appearance = entry.getValue();
            if (entry.getKey() == 'P') {
                builder.addElement('P', StructureUtility.withChannel(CARRIAGE,
                        StructureUtility.ofBlock(appearance.block, appearance.metadata)));
            } else {
                builder.addElement(entry.getKey(), StructureUtility.ofBlock(appearance.block, appearance.metadata));
            }
        }
        return builder.addShape("main", shape).build();
    }

    public static boolean check(ElevatorController controller) {
        BlockPosition origin = controller.getStructureOrigin();
        return STRUCTURE.check(controller, "main", controller.getWorldObj(), ExtendedFacing.DEFAULT,
                origin.x, origin.y, origin.z,
                OFFSET.x, OFFSET.y, OFFSET.z, false);
    }

    public static Map<BlockPosition, Character> getCells() {
        return cells;
    }

    public static void place(World world, BlockPosition origin) {
        for (Map.Entry<BlockPosition, Character> entry : cells.entrySet()) {
            BlockPosition position = entry.getKey();
            BlockAppearance appearance = palette.get(entry.getValue());
            if (appearance != null) {
                world.setBlock(origin.x + position.x, origin.y + position.y, origin.z + position.z,
                        appearance.block, appearance.metadata, 3);
            }
        }
        world.setBlock(origin.x + CONTROLLER_POSITION.x, origin.y + CONTROLLER_POSITION.y,
                origin.z + CONTROLLER_POSITION.z, AnimationExample.controller, 0, 3);
    }
}
