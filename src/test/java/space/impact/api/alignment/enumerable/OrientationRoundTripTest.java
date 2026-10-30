package space.impact.api.alignment.enumerable;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraftforge.common.util.ForgeDirection;
import org.junit.jupiter.api.Test;

class OrientationRoundTripTest {
    @Test
    void allNinetySixSavedIndicesKeepTheirMeaning() {
        assertEquals(96, ExtendedFacing.VALUES.length);
        ForgeDirection[] directions = {ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH,
                ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};
        Rotation[] rotations = {Rotation.NORMAL, Rotation.CLOCKWISE, Rotation.UPSIDE_DOWN, Rotation.COUNTER_CLOCKWISE};
        Flip[] flips = {Flip.NONE, Flip.HORIZONTAL, Flip.VERTICAL, Flip.BOTH};
        int index = 0;
        for (ForgeDirection direction : directions) for (Rotation rotation : rotations) for (Flip flip : flips) {
            ExtendedFacing facing = ExtendedFacing.byIndex(index);
            assertEquals(direction, facing.getDirection());
            assertEquals(rotation, facing.getRotation());
            assertEquals(flip, facing.getFlip());
            assertSame(facing, ExtendedFacing.of(direction, rotation, flip));
            assertEquals(index++, facing.getIndex());
        }
    }

    @Test
    void coordinateTransformsRoundTripForEveryOrientation() {
        int[][] offsets = {{0, 0, 0}, {2, -3, 7}, {-11, 5, -9}};
        for (ExtendedFacing facing : ExtendedFacing.VALUES) for (int[] offset : offsets) {
            int[] world = new int[3];
            int[] recovered = new int[3];
            facing.getWorldOffset(offset, world);
            facing.getOffsetABC(world, recovered);
            assertArrayEquals(offset, recovered, facing.name());
        }
    }
}
