package space.impact.structures.kotlinx.alignment

import net.minecraftforge.common.util.ForgeDirection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.structures.alignment.IAlignmentLimits
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.alignment.enumerable.Flip
import space.impact.structures.alignment.enumerable.Rotation

internal class AlignmentExtensionsTest {

    @Test
    internal fun defaultsAndNamedArgumentsPreserveCoreOrientationIndices() {
        assertEquals(
            ExtendedFacing.NORTH_NORMAL_NONE,
            facing(),
        )
        assertEquals(
            ExtendedFacing.UP_CLOCKWISE_HORIZONTAL,
            facing(
                direction = ForgeDirection.UP,
                rotation = Rotation.CLOCKWISE,
                flip = Flip.HORIZONTAL,
            ),
        )
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            facing(direction = ForgeDirection.UNKNOWN)
        }
    }

    @Test
    internal fun limitsDefaultToDeniedAndFilterOnlyNarrowsAllowedStates() {
        val limits: IAlignmentLimits = alignmentLimits {
            allow(ForgeDirection.NORTH)
            filter { orientation ->
                orientation.rotation == Rotation.NORMAL
            }
        }
        assertTrue(limits.isNewExtendedFacingValid(ExtendedFacing.NORTH_NORMAL_NONE))
        assertFalse(limits.isNewExtendedFacingValid(ExtendedFacing.NORTH_CLOCKWISE_NONE))
        assertFalse(limits.isNewExtendedFacingValid(ExtendedFacing.SOUTH_NORMAL_NONE))
    }

    @Test
    internal fun escapedBuilderCannotMutatePublishedLimits() {
        var captured: AlignmentLimitsBuilder? = null
        val limits: IAlignmentLimits = alignmentLimits {
            allow(ExtendedFacing.NORTH_NORMAL_NONE)
            captured = this
        }
        captured?.allowAll()
        assertFalse(limits.isNewExtendedFacingValid(ExtendedFacing.SOUTH_NORMAL_NONE))
        assertTrue(limits.isNewExtendedFacingValid(ExtendedFacing.NORTH_NORMAL_NONE))
    }
}
