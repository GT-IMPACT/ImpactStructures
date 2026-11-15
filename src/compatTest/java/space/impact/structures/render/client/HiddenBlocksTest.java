package space.impact.structures.render.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import space.impact.structures.render.BlockAppearance;
import space.impact.structures.render.BlockModel;
import space.impact.structures.render.BlockPosition;

class HiddenBlocksTest {

    @AfterEach
    void clear() {
        HiddenBlocks.replace(null, Collections.emptyMap());
    }

    @Test
    void hideRestoreRebuildsNeighboursAndNeverHidesTheIndependentModel() {
        World world = mock(World.class);
        Block block = new Block(Material.iron) {};
        BlockPosition position = new BlockPosition(-17, 80, -33);
        BlockAppearance source = new BlockAppearance(block, 3);
        when(world.getBlock(-17, 80, -33)).thenReturn(block);
        when(world.getBlockMetadata(-17, 80, -33)).thenReturn(3);
        HiddenBlocks.replace(world, Collections.singletonMap(position, source));
        assertTrue(HiddenBlocks.contains(world, -17, 80, -33));
        verify(world).markBlockRangeForRenderUpdate(-18, 79, -34, -16, 81, -32);
        ModelBlockAccess model = new ModelBlockAccess(BlockModel.builder().block(-17, 80, -33, source).build(),
                world, new BlockPosition(0, 0, 0));
        assertFalse(HiddenBlocks.contains(model, -17, 80, -33));
        when(world.getBlockMetadata(-17, 80, -33)).thenReturn(4);
        assertFalse(HiddenBlocks.contains(world, -17, 80, -33));
        HiddenBlocks.replace(world, Collections.emptyMap());
        assertFalse(HiddenBlocks.contains(world, -17, 80, -33));
    }
}
