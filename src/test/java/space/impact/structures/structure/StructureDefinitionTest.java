package space.impact.structures.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import org.junit.jupiter.api.Test;

class StructureDefinitionTest {
    private static final IStructureElement<Object> ELEMENT = new IStructureElement<Object>() {
        public boolean check(Object context, World world, int x, int y, int z) { return true; }
        public boolean spawnHint(Object context, World world, int x, int y, int z, ItemStack trigger) { return true; }
        public boolean placeBlock(Object context, World world, int x, int y, int z, ItemStack trigger) { return true; }
    };

    @Test
    void occupancyRetainsControllerAndSkipsSpacesAcrossSlices() {
        IStructureDefinition<Object> definition = StructureDefinition.<Object>builder()
                .addShape("main", new String[][] {{"A ~", " A "}, {"  A"}})
                .addElement('A', ELEMENT).build();
        assertTrue(definition.isContainedInStructure("main", 0, 0, 0));
        assertFalse(definition.isContainedInStructure("main", 1, 0, 0));
        assertTrue(definition.isContainedInStructure("main", 2, 0, 0));
        assertTrue(definition.isContainedInStructure("main", 1, 1, 0));
        assertTrue(definition.isContainedInStructure("main", 2, 0, 1));
        assertFalse(definition.isContainedInStructure("main", 3, 0, 1));
    }

    @Test
    void missingBindingsAndPiecesFailExplicitly() {
        assertThrows(RuntimeException.class, () -> StructureDefinition.<Object>builder()
                .addShape("main", new String[][] {{"X"}}).build());
        IStructureDefinition<Object> definition = StructureDefinition.<Object>builder()
                .addShape("main", new String[][] {{"A"}}).addElement('A', ELEMENT).build();
        assertThrows(NoSuchElementException.class, () -> definition.getStructureFor("missing"));
    }

    @Test
    void explicitChannelBoundsOverrideInferredDefaults() {
        IStructureDefinition<Object> definition = StructureDefinition.<Object>builder()
                .addElement('A', StructureUtility.withChannel("coil tier", ELEMENT))
                .addShape("main", new String[][] {{"A"}}).addChannel("coil tier", 2, 5).build();
        IChannelDefinition channel = definition.getChannelDefinition("coil tier");
        assertEquals(2, channel.normalizeValue(1));
        assertEquals(5, channel.normalizeValue(9));
        assertEquals(3, channel.normalizeValue(3));
        assertFalse(channel.isValueValid(1));
        assertEquals(1, definition.getExplicitChannelDefinitions().size());
    }

    @Test
    void emptyBudgetCannotTouchWorldOrInventory() {
        IStructureDefinition<Object> definition = StructureDefinition.<Object>builder()
                .addShape("main", new String[][] {{"A"}}).addElement('A', ELEMENT).build();
        assertEquals(0, definition.survivalBuild(new Object(), null, "main", null, ExtendedFacing.DEFAULT,
                0, 0, 0, 0, 0, 0, 0, null, false));
    }

    @Test
    void missingPredicateBasedPlacementResourceRejectsWithoutTouchingWorld() {
        IStructureElement<Object> element = new IStructureElement<Object>() {
            public boolean check(Object context, World world, int x, int y, int z) { return false; }
            public boolean spawnHint(Object context, World world, int x, int y, int z, ItemStack trigger) { return true; }
            public boolean placeBlock(Object context, World world, int x, int y, int z, ItemStack trigger) { return true; }
            public BlocksToPlace getBlocksToPlace(Object context, World world, int x, int y, int z,
                    ItemStack trigger, AutoPlaceEnvironment environment) {
                return BlocksToPlace.create(stack -> true);
            }
        };
        IItemSource empty = (predicate, simulate, count) -> Collections.emptyMap();
        AutoPlaceEnvironment environment = AutoPlaceEnvironment.fromLegacy(empty, null, null);
        assertEquals(IStructureElement.PlaceResult.REJECT,
                element.survivalPlaceBlock(new Object(), null, 0, 0, 0, null, environment));
    }

    @Test
    void notAirRequestsRealBlockItemsRatherThanArbitraryInventoryItems() {
        IStructureElement.BlocksToPlace candidates = StructureUtility.notAir()
                .getBlocksToPlace(new Object(), null, 0, 0, 0, null, null);
        assertFalse(candidates.getPredicate().test(new ItemStack(new Item())));
        Block block = new Block(Material.rock) {};
        assertTrue(candidates.getPredicate().test(new ItemStack(new ItemBlock(block))));
    }
}
