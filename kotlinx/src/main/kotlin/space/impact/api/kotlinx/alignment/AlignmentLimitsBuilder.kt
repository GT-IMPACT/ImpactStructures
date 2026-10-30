package space.impact.api.kotlinx.alignment

import java.util.Optional
import net.minecraftforge.common.util.ForgeDirection
import space.impact.api.alignment.IAlignmentLimits
import space.impact.api.alignment.enumerable.ExtendedFacing
import space.impact.api.alignment.enumerable.Flip
import space.impact.api.alignment.enumerable.Rotation

public class AlignmentLimitsBuilder internal constructor(allowByDefault: Boolean) {

    private var delegate: IAlignmentLimits.Builder = if (allowByDefault) {
        IAlignmentLimits.Builder.allowAll()
    } else {
        IAlignmentLimits.Builder.denyAll()
    }

    public fun allow(direction: ForgeDirection) {
        require(direction != ForgeDirection.UNKNOWN)
        delegate.allow(direction)
    }

    public fun allow(rotation: Rotation) {
        delegate.allow(rotation)
    }

    public fun allow(flip: Flip) {
        delegate.allow(flip)
    }

    public fun allow(facing: ExtendedFacing) {
        delegate.allow(facing)
    }

    public fun deny(direction: ForgeDirection) {
        require(direction != ForgeDirection.UNKNOWN)
        delegate.deny(direction)
    }

    public fun deny(rotation: Rotation) {
        delegate.deny(rotation)
    }

    public fun deny(flip: Flip) {
        delegate.deny(flip)
    }

    public fun deny(facing: ExtendedFacing) {
        delegate.deny(facing)
    }

    public fun allowAll() {
        delegate = IAlignmentLimits.Builder.allowAll()
    }

    public fun denyAll() {
        delegate = IAlignmentLimits.Builder.denyAll()
    }

    public fun ensureDuplicates() {
        delegate.ensureDuplicates()
    }

    public fun ensureNoDuplicates(preferredFlip: Flip) {
        require(preferredFlip == Flip.HORIZONTAL || preferredFlip == Flip.VERTICAL)
        delegate.ensureNoDuplicates(preferredFlip)
    }

    /** Restrict the current set without re-enabling orientations denied earlier in this scope. */
    public fun filter(predicate: (ExtendedFacing) -> Boolean) {
        delegate.filter { orientation ->
            if (predicate(orientation)) {
                Optional.empty<Boolean>()
            } else {
                Optional.of(false)
            }
        }
    }

    internal fun build(): IAlignmentLimits {
        val allowed: IAlignmentLimits = delegate.build()
        val snapshot: IAlignmentLimits.Builder = IAlignmentLimits.Builder.denyAll()
        for (orientation: ExtendedFacing in ExtendedFacing.VALUES) {
            if (allowed.isNewExtendedFacingValid(orientation)) {
                snapshot.allow(orientation)
            }
        }
        return snapshot.build()
    }
}
