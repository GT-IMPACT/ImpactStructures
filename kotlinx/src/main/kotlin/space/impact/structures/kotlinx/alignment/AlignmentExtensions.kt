package space.impact.structures.kotlinx.alignment

import net.minecraftforge.common.util.ForgeDirection
import space.impact.structures.alignment.IAlignmentLimits
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.alignment.enumerable.Flip
import space.impact.structures.alignment.enumerable.Rotation

public fun facing(
    direction: ForgeDirection = ForgeDirection.NORTH,
    rotation: Rotation = Rotation.NORMAL,
    flip: Flip = Flip.NONE,
): ExtendedFacing {
    require(value = direction != ForgeDirection.UNKNOWN) {
        "An orientation requires one of the six block directions"
    }
    return ExtendedFacing.of(
        direction,
        rotation,
        flip,
    )
}

public fun alignmentLimits(
    allowByDefault: Boolean = false,
    configure: AlignmentLimitsBuilder.() -> Unit,
): IAlignmentLimits {
    val builder: AlignmentLimitsBuilder = AlignmentLimitsBuilder(allowByDefault)
    builder.configure()
    return builder.build()
}
