package space.impact.structures.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;

import org.junit.jupiter.api.Test;

class HintObstructionTest {

    private static final Block AIR = new Block(Material.air) {
        @Override
        public boolean canCollideCheck(int metadata, boolean includeLiquid) {
            return false;
        }
    };
    private static final Block WALL = new Block(Material.rock) {};

    @Test
    void hoveringFromOutsideSurvivesMinecraftMutatingTheRayOrigin() throws ReflectiveOperationException {
        Vec3 from = Vec3.createVectorHelper(0.5, 0.5, -3);
        Vec3 to = Vec3.createVectorHelper(0.5, 0.5, 61);
        double obstruction = MarkerRayTrace.obstructionDistance(world(), from, to);

        assertEquals(4, from.zCoord, 1e-9);
        assertEquals(7, obstruction, 1e-9);
        double visibleHint = MarkerRayTrace.hintIntersection(0.5, 0.5, -3, 0, 0, 1, 0, 0, 0, null, 64);
        double hiddenHint = MarkerRayTrace.hintIntersection(0.5, 0.5, -3, 0, 0, 1, 0, 0, 6, null, 64);
        assertTrue(visibleHint > 0 && visibleHint < obstruction);
        assertTrue(hiddenHint > obstruction);
    }

    @Test
    void unobstructedRaysAllowHologramLabels() throws ReflectiveOperationException {
        assertEquals(Double.POSITIVE_INFINITY, MarkerRayTrace.obstructionDistance(world(),
                Vec3.createVectorHelper(0.5, 0.5, -3), Vec3.createVectorHelper(0.5, 0.5, -67)));
    }

    @Test
    void diagonalObstructionsUseDistanceFromTheOriginalEyePosition() throws ReflectiveOperationException {
        assertEquals(Math.sqrt(98), MarkerRayTrace.obstructionDistance(world(),
                Vec3.createVectorHelper(0.5, 0.5, -3), Vec3.createVectorHelper(64.5, 0.5, 61)), 1e-9);
    }

    private static World world() throws ReflectiveOperationException {
        // Only the block-query overrides and Minecraft's real DDA traversal are needed, with no game bootstrap.
        Class<?> allocatorClass = Class.forName("sun.misc.Unsafe");
        Field allocatorField = allocatorClass.getDeclaredField("theUnsafe");
        allocatorField.setAccessible(true);
        Object allocator = allocatorField.get(null);
        return RayTraceWorld.class.cast(allocatorClass.getMethod("allocateInstance", Class.class)
                .invoke(allocator, RayTraceWorld.class));
    }

    private static final class RayTraceWorld extends World {

        private RayTraceWorld() {
            super(null, "hologram-ray-test", (WorldProvider) null, (WorldSettings) null, null);
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            return z == 4 ? WALL : AIR;
        }

        @Override
        public int getBlockMetadata(int x, int y, int z) {
            return 0;
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return null;
        }

        @Override
        protected int func_152379_p() {
            return 0;
        }

        @Override
        public Entity getEntityByID(int id) {
            return null;
        }
    }
}
