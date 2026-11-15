package space.impact.structures.render.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import space.impact.structures.render.BlockAppearance;
import space.impact.structures.render.BlockModel;
import space.impact.structures.render.BlockPosition;

/** Isolated neighbours expose every outside face of a moving group, including formerly buried faces. */
final class ModelBlockAccess implements IBlockAccess {

    private final Map<Long, BlockAppearance> blocks = new HashMap<>();
    private final World world;
    private final BlockPosition origin;

    ModelBlockAccess(BlockModel model, World world, BlockPosition origin) {
        this.world = world;
        this.origin = origin;
        for (Map.Entry<BlockPosition, BlockAppearance> entry : model.getBlocks().entrySet()) {
            BlockPosition position = entry.getKey();
            blocks.put(BlockPosition.key(position.x, position.y, position.z), entry.getValue());
        }
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        BlockAppearance appearance = blocks.get(BlockPosition.key(x, y, z));
        return appearance == null ? Blocks.air : appearance.block;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        BlockAppearance appearance = blocks.get(BlockPosition.key(x, y, z));
        return appearance == null ? 0 : appearance.metadata;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return null;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minimum) {
        int worldY = Math.max(0, Math.min(world.getHeight() - 1, origin.y + y));
        if (!world.blockExists(origin.x + x, worldY, origin.z + z)) {
            return 0x00F000F0;
        }
        return world.getLightBrightnessForSkyBlocks(origin.x + x, worldY, origin.z + z, minimum);
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int direction) {
        return 0;
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return getBlock(x, y, z) == Blocks.air;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        if (!world.blockExists(origin.x + x, origin.y, origin.z + z)) {
            return BiomeGenBase.plains;
        }
        return world.getBiomeGenForCoords(origin.x + x, origin.z + z);
    }

    @Override
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean defaultValue) {
        return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
    }
}
