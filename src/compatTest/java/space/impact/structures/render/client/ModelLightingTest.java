package space.impact.structures.render.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import space.impact.structures.animation.Animations;
import space.impact.structures.render.BlockModel;
import space.impact.structures.render.BlockPosition;
import space.impact.structures.render.RenderPart;

class ModelLightingTest {

    private final Block block = new Block(Material.rock) {
    };
    private final BlockPosition origin = new BlockPosition(100, 80, -40);

    @Test
    void stableLightKeepsGeometryAndNeighbourLightChangesInvalidateIt() {
        World world = world();
        ModelBlockAccess access = access(world);
        // This face/AO sample is outside the sole model block and must still be tracked.
        when(world.getLightBrightnessForSkyBlocks(101, 80, -40, 0)).thenReturn(0x00F00000);
        for (int repeat = 0; repeat < 10; repeat++) {
            assertEquals(0x00F00000, access.getLightBrightnessForSkyBlocks(1, 0, 0, 0));
        }
        clearInvocations(world);
        assertFalse(access.hasLightingChanged());
        verify(world, times(1)).getLightBrightnessForSkyBlocks(101, 80, -40, 0);
        when(world.getLightBrightnessForSkyBlocks(101, 80, -40, 0)).thenReturn(0x00E00000);
        assertTrue(access.hasLightingChanged());
        // Checking must not erase the difference before the mesh is rebuilt.
        assertTrue(access.hasLightingChanged());
    }

    @Test
    void distinctMinimumBrightnessQueriesAreRetained() {
        World world = world();
        ModelBlockAccess access = access(world);
        when(world.getLightBrightnessForSkyBlocks(100, 80, -40, 0)).thenReturn(0x00F00000);
        when(world.getLightBrightnessForSkyBlocks(100, 80, -40, 15)).thenReturn(0x00F000F0);
        access.getLightBrightnessForSkyBlocks(0, 0, 0, 0);
        access.getLightBrightnessForSkyBlocks(0, 0, 0, 15);
        assertFalse(access.hasLightingChanged());
        when(world.getLightBrightnessForSkyBlocks(100, 80, -40, 0)).thenReturn(0x00F00010);
        assertTrue(access.hasLightingChanged());
    }

    @Test
    void loadingPreviouslyMissingLightSamplesInvalidatesFallbackBrightness() {
        World world = world();
        ModelBlockAccess access = access(world);
        when(world.blockExists(100, 255, -40)).thenReturn(false);
        assertEquals(0x00F000F0, access.getLightBrightnessForSkyBlocks(0, 300, 0, 0));
        assertFalse(access.hasLightingChanged());
        when(world.blockExists(100, 255, -40)).thenReturn(true);
        when(world.getLightBrightnessForSkyBlocks(100, 255, -40, 0)).thenReturn(0x00F00000);
        assertTrue(access.hasLightingChanged());
    }

    @Test
    void textureNotificationsScaleWithMaterialsInsteadOfBlockCount() {
        BlockModel.Builder builder = BlockModel.builder();
        for (int x = 0; x < 1000; x++) {
            builder.block(x, 0, 0, block, x % 3);
        }
        RenderPart part = new RenderPart(builder.build(), Animations.STILL);
        RenderHandle handle = new RenderHandle(world(), origin, 0, new RenderPart[] {part, part});
        assertEquals(3, handle.appearances.size());
        handle.geometryDirty = false;
        handle.invalidateGeometry();
        assertTrue(handle.geometryDirty);
    }

    private ModelBlockAccess access(World world) {
        return new ModelBlockAccess(BlockModel.builder().block(0, 0, 0, block, 0).build(), world, origin);
    }

    private World world() {
        World world = mock(World.class);
        when(world.getHeight()).thenReturn(256);
        when(world.blockExists(anyInt(), anyInt(), anyInt())).thenReturn(true);
        return world;
    }
}
