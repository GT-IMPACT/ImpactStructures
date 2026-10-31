package space.impact.structures.alignment;

import net.minecraftforge.common.util.ForgeDirection;

import space.impact.structures.alignment.enumerable.Flip;
import space.impact.structures.alignment.enumerable.Rotation;

class AlignmentLimits implements IAlignmentLimits {

    protected final boolean[] validStates;

    AlignmentLimits(boolean[] validStates) {
        this.validStates = validStates;
    }

    @Override
    public boolean isNewExtendedFacingValid(ForgeDirection direction, Rotation rotation, Flip flip) {
        return validStates[IAlignment.getAlignmentIndex(direction, rotation, flip)];
    }
}
