package space.impact.structures.render;

import java.util.Objects;
import net.minecraft.block.Block;

/** A block and metadata used for rendering only; never placed into the world. */
public final class BlockAppearance {

    public final Block block;
    public final int metadata;

    public BlockAppearance(Block block, int metadata) {
        this.block = Objects.requireNonNull(block, "block");
        if (metadata < 0 || metadata > 15) {
            throw new IllegalArgumentException("Metadata must be in 0..15");
        }
        this.metadata = metadata;
    }
}
