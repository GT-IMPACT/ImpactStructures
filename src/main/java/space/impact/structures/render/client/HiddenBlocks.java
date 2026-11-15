package space.impact.structures.render.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import space.impact.structures.render.BlockAppearance;
import space.impact.structures.render.BlockPosition;

/** Immutable publication is safe for Angelica's chunk builder threads. Mutations run on the client thread. */
public final class HiddenBlocks {

    private static volatile Map<Long, BlockAppearance> hidden = Collections.emptyMap();

    private HiddenBlocks() {
    }

    public static boolean contains(IBlockAccess access, int x, int y, int z) {
        if (access == null || access instanceof ModelBlockAccess) {
            return false;
        }
        BlockAppearance source = hidden.get(BlockPosition.key(x, y, z));
        return source != null && access.getBlock(x, y, z) == source.block
                && access.getBlockMetadata(x, y, z) == source.metadata;
    }

    static void replace(World world, Map<BlockPosition, BlockAppearance> sources) {
        Map<Long, BlockAppearance> previous = hidden;
        Map<Long, BlockAppearance> next = new HashMap<>();
        for (Map.Entry<BlockPosition, BlockAppearance> entry : sources.entrySet()) {
            BlockPosition position = entry.getKey();
            next.put(BlockPosition.key(position.x, position.y, position.z), entry.getValue());
        }
        hidden = Collections.unmodifiableMap(next);
        if (world == null) {
            return;
        }
        for (Long key : previous.keySet()) {
            if (!next.containsKey(key)) {
                rebuild(world, key);
            }
        }
        for (Long key : next.keySet()) {
            if (!previous.containsKey(key)) {
                rebuild(world, key);
            }
        }
    }

    private static void rebuild(World world, long key) {
        int x = (int) (key >> 38);
        int y = (int) (key & 0xFFF);
        int z = (int) (key << 26 >> 38);
        // Adjacent sections must expose the faces previously occluded by these blocks.
        world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }
}
