package space.impact.structures.render;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import net.minecraft.world.World;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.animation.Vector3;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.IStructureElement;
import space.impact.structures.structure.IStructureWalker;

/** Optional bridge from structural channels to otherwise independent render models. */
public final class StructureRenderBinding {

    private final BlockModel model;
    private final Map<BlockPosition, BlockAppearance> sources;

    private StructureRenderBinding(BlockModel model, Map<BlockPosition, BlockAppearance> sources) {
        this.model = model;
        this.sources = Collections.unmodifiableMap(sources);
    }

    /**
     * Captures actual blocks. Coordinates in the model are world-axis offsets from origin, preserving block facing.
     * An unloaded source aborts capture: a partial model must never hide a complete channel.
     */
    public static <T> StructureRenderBinding capture(
            IStructureDefinition<T> definition,
            String piece,
            String channel,
            World world,
            BlockPosition origin,
            ExtendedFacing facing,
            BlockPosition structureOffset,
            UnaryOperator<BlockAppearance> appearance
    ) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(appearance, "appearance");
        Map<BlockPosition, BlockAppearance> sources = new LinkedHashMap<>();
        BlockModel.Builder model = BlockModel.builder();
        definition.iterate(piece, world, facing, origin.x, origin.y, origin.z,
                structureOffset.x, structureOffset.y, structureOffset.z,
                new IStructureWalker<T>() {

                    @Override
                    public boolean visit(
                            IStructureElement<T> element,
                            World sourceWorld,
                            int x,
                            int y,
                            int z,
                            int a,
                            int b,
                            int c
                    ) {
                        if (!element.getChannels().contains(channel)) {
                            return true;
                        }
                        if (sourceWorld.isAirBlock(x, y, z)) {
                            throw new IllegalStateException("Cannot capture air in channel " + channel);
                        }
                        BlockAppearance source = new BlockAppearance(sourceWorld.getBlock(x, y, z),
                                sourceWorld.getBlockMetadata(x, y, z));
                        sources.put(new BlockPosition(x, y, z), source);
                        model.block(x - origin.x, y - origin.y, z - origin.z, appearance.apply(source));
                        return true;
                    }

                    @Override
                    public boolean blockNotLoaded(
                            IStructureElement<T> element,
                            World sourceWorld,
                            int x,
                            int y,
                            int z,
                            int a,
                            int b,
                            int c
                    ) {
                        if (element.getChannels().contains(channel)) {
                            throw new IllegalStateException("Channel source is not loaded: " + channel);
                        }
                        return true;
                    }
                });
        return new StructureRenderBinding(model.build(), sources);
    }

    public BlockModel getModel() {
        return model;
    }

    public Map<BlockPosition, BlockAppearance> getSources() {
        return sources;
    }

    /** Converts an ABC axis or pivot offset into the captured model's coordinate system. */
    public static Vector3 worldOffset(ExtendedFacing facing, Vector3 abc) {
        double[] output = new double[3];
        facing.getWorldOffset(new double[] {abc.x, abc.y, abc.z}, output);
        return new Vector3(output[0], output[1], output[2]);
    }
}
