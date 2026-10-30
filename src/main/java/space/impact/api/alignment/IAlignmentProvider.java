package space.impact.api.alignment;

import javax.annotation.Nullable;

public interface IAlignmentProvider {

    @Nullable
    IAlignment getAlignment();
}
