package space.impact.structures.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.UnaryOperator;
import java.lang.reflect.Field;
import java.util.HashMap;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import space.impact.structures.Main;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.StructureDefinition;
import space.impact.structures.structure.StructureUtility;

class StructureRenderBindingTest {

    @BeforeAll
    static void initializeForgeBootstrap() throws Exception {
        Field instance = Loader.class.getDeclaredField("instance");
        instance.setAccessible(true);
        Object previous = instance.get(null);
        Loader loader = mock(Loader.class);
        ModContainer mod = mock(ModContainer.class);
        when(mod.getModId()).thenReturn("impactapi");
        when(loader.activeModContainer()).thenReturn(mod);
        instance.set(null, loader);
        if (Launch.blackboard == null) {
            Launch.blackboard = new HashMap<>();
        }
        Launch.blackboard.put("fml.deobfuscatedEnvironment", false);
        try {
            Main.DEBUG_MODE = false;
        } finally {
            instance.set(null, previous);
        }
    }

    private final Block source = new Block(Material.iron) {};
    private final Block replacement = new Block(Material.rock) {};
    private final BlockPosition origin = new BlockPosition(100, 80, -40);
    private final IStructureDefinition<Object> definition = StructureDefinition.<Object>builder()
            .addElement('A', StructureUtility.withChannel("moving", StructureUtility.ofBlock(source, 2)))
            .addElement('B', StructureUtility.ofBlock(source, 2))
            .addShape("main", new String[][] {{"~AB", " A "}})
            .build();

    @Test
    void captureUsesChannelMembershipAndPreservesActualMetadataForEveryFacing() {
        World world = world();
        for (ExtendedFacing facing : ExtendedFacing.values()) {
            StructureRenderBinding binding = capture(world, facing, UnaryOperator.identity());
            assertEquals(2, binding.getSources().size());
            for (int[] abc : new int[][] {{1, 0, 0}, {1, 1, 0}}) {
                int[] xyz = new int[3];
                facing.getWorldOffset(abc, xyz);
                BlockAppearance block = binding.getModel().getBlocks().get(new BlockPosition(xyz[0], xyz[1], xyz[2]));
                assertSame(source, block.block);
                assertEquals(7, block.metadata);
                assertEquals(7, binding.getSources().get(new BlockPosition(
                        origin.x + xyz[0], origin.y + xyz[1], origin.z + xyz[2])).metadata);
            }
        }
    }

    @Test
    void visualReplacementDoesNotReplaceSourceIdentityOrMutateTheWorld() {
        StructureRenderBinding binding = capture(world(), ExtendedFacing.DEFAULT,
                block -> new BlockAppearance(replacement, 5));
        assertSame(replacement, binding.getModel().getBlocks().values().iterator().next().block);
        assertSame(source, binding.getSources().values().iterator().next().block);
        assertThrows(UnsupportedOperationException.class, () -> binding.getSources().clear());
        assertThrows(UnsupportedOperationException.class, () -> binding.getModel().getBlocks().clear());
    }

    @Test
    void unloadedSourceCannotProduceAPartialHiddenChannel() {
        World world = world();
        when(world.blockExists(origin.x - 1, origin.y - 1, origin.z)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> capture(world, ExtendedFacing.DEFAULT, UnaryOperator.identity()));
    }

    private World world() {
        World world = mock(World.class);
        when(world.blockExists(anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(world.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(source);
        when(world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenReturn(7);
        return world;
    }

    private StructureRenderBinding capture(World world, ExtendedFacing facing, UnaryOperator<BlockAppearance> mapper) {
        return StructureRenderBinding.capture(definition, "main", "moving", world, origin, facing,
                new BlockPosition(0, 0, 0), mapper);
    }
}
