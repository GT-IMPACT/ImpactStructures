package space.impact.structures.alignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import space.impact.structures.alignment.enumerable.ExtendedFacing;

class AlignmentLimitsTest {
    @Test
    void denyAllRejectsEveryOrientation() {
        IAlignmentLimits limits = IAlignmentLimits.Builder.denyAll().build();
        for (ExtendedFacing facing : ExtendedFacing.VALUES) {
            assertFalse(limits.isNewExtendedFacingValid(facing));
        }
    }

    @Test
    void allowOnlyDoesNotLeakOtherOrientations() {
        ExtendedFacing chosen = ExtendedFacing.NORTH_NORMAL_NONE;
        IAlignmentLimits limits = IAlignmentLimits.allowOnly(chosen);
        for (ExtendedFacing facing : ExtendedFacing.VALUES) {
            assertEquals(facing == chosen, limits.isNewExtendedFacingValid(facing));
        }
    }
}
