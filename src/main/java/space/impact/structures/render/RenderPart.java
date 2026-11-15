package space.impact.structures.render;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import space.impact.structures.animation.IAnimation;

/** One independently animated group. Build multiple parts for independently moving blocks or layers. */
public final class RenderPart {

    public final BlockModel model;
    public final IAnimation animation;
    public final Map<BlockPosition, BlockAppearance> hiddenSources;

    public RenderPart(BlockModel model, IAnimation animation) {
        this(model, animation, Collections.emptyMap());
    }

    public RenderPart(StructureRenderBinding binding, IAnimation animation, boolean hideOriginals) {
        this(binding.getModel(), animation, hideOriginals ? binding.getSources() : Collections.emptyMap());
    }

    private RenderPart(BlockModel model, IAnimation animation, Map<BlockPosition, BlockAppearance> hiddenSources) {
        this.model = Objects.requireNonNull(model, "model");
        this.animation = Objects.requireNonNull(animation, "animation");
        this.hiddenSources = hiddenSources;
    }
}
