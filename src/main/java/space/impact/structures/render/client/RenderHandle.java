package space.impact.structures.render.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.world.World;
import space.impact.structures.animation.AnimationClock;
import space.impact.structures.render.BlockAppearance;
import space.impact.structures.render.BlockPosition;
import space.impact.structures.render.RenderPart;

/** Client-thread lifetime of a scene. Close when its controller unloads, changes state or is destroyed. */
public final class RenderHandle implements AutoCloseable {

    final World world;
    final BlockPosition origin;
    final List<RenderPart> parts;
    final int[] displayLists;
    private final AnimationClock playback;
    private boolean closed;
    boolean ready;
    long meshTick = Long.MIN_VALUE;

    RenderHandle(World world, BlockPosition origin, double startTick, RenderPart[] parts) {
        if (!Double.isFinite(startTick) || parts.length == 0) {
            throw new IllegalArgumentException("A scene requires a finite start tick and at least one part");
        }
        this.world = world;
        this.origin = origin;
        this.playback = new AnimationClock(startTick);
        this.parts = Collections.unmodifiableList(Arrays.asList(parts.clone()));
        this.displayLists = new int[parts.length];
        for (RenderPart part : parts) {
            for (BlockAppearance appearance : part.model.getBlocks().values()) {
                if (appearance.block.hasTileEntity(appearance.metadata) || appearance.block.getRenderType() < 0) {
                    throw new IllegalArgumentException("TileEntity/special-only models need an explicit block replacement");
                }
            }
        }
    }

    public boolean isClosed() {
        return closed;
    }

    /** Use the server's world tick epoch to synchronize playback, including players joining mid-animation. */
    public void setStartTick(double startTick) {
        if (!Double.isFinite(startTick)) {
            throw new IllegalArgumentException("Start tick must be finite");
        }
        playback.restart(startTick);
    }

    public AnimationClock getPlayback() {
        return playback;
    }

    double seconds(float partialTicks) {
        return playback.secondsAt(world.getTotalWorldTime() + (double) partialTicks);
    }

    /** Rebuild after external texture/appearance changes. Resource reload is handled automatically. */
    public void invalidateGeometry() {
        meshTick = Long.MIN_VALUE;
    }

    boolean sourcesValid() {
        if (!world.blockExists(origin.x, origin.y, origin.z)) {
            return false;
        }
        for (RenderPart part : parts) {
            for (Map.Entry<BlockPosition, BlockAppearance> entry : part.hiddenSources.entrySet()) {
                BlockPosition position = entry.getKey();
                BlockAppearance source = entry.getValue();
                if (!world.blockExists(position.x, position.y, position.z)
                        || world.getBlock(position.x, position.y, position.z) != source.block
                        || world.getBlockMetadata(position.x, position.y, position.z) != source.metadata) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            StructureRenderer.INSTANCE.refreshHiddenBlocks();
        }
    }
}
