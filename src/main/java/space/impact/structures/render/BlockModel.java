package space.impact.structures.render;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.block.Block;

/** Immutable, channel-independent geometry. Integer cells; continuous movement belongs to animations. */
public final class BlockModel {

    private final Map<BlockPosition, BlockAppearance> blocks;

    private BlockModel(Map<BlockPosition, BlockAppearance> blocks) {
        this.blocks = Collections.unmodifiableMap(new LinkedHashMap<>(blocks));
    }

    public Map<BlockPosition, BlockAppearance> getBlocks() {
        return blocks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<BlockPosition, BlockAppearance> blocks = new LinkedHashMap<>();

        public Builder block(int x, int y, int z, BlockAppearance appearance) {
            blocks.put(new BlockPosition(x, y, z), Objects.requireNonNull(appearance, "appearance"));
            return this;
        }

        public Builder block(
                int x,
                int y,
                int z,
                Block block,
                int metadata
        ) {
            return block(x, y, z, new BlockAppearance(block, metadata));
        }

        public BlockModel build() {
            if (blocks.isEmpty()) {
                throw new IllegalStateException("A render model must contain blocks");
            }
            return new BlockModel(blocks);
        }
    }
}
