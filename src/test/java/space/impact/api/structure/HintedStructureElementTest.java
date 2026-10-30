package space.impact.api.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import org.junit.jupiter.api.Test;

import space.impact.api.hint.HintColor;

class HintedStructureElementTest {

    @Test
    void decorationPreservesChecksPlacementSurvivalChannelsAndNavigation() {
        RecordingElement original = new RecordingElement();
        IStructureElement<Object> decorated = StructureUtility.withHint(original, HintColor.BLUE);
        Object context = new Object();

        assertTrue(decorated.check(context, null, 1, 2, 3));
        assertFalse(decorated.couldBeValid(context, null, 1, 2, 3, null));
        assertTrue(decorated.placeBlock(context, null, 1, 2, 3, null));
        assertSame(original.channels, decorated.getChannels());
        assertSame(original.description, decorated.getDescription(context));
        assertSame(original.candidates, decorated.getBlocksToPlace(context, null, 1, 2, 3, null, null));
        assertEquals(IStructureElement.PlaceResult.STOP,
                decorated.survivalPlaceBlock(context, null, 1, 2, 3, null, (AutoPlaceEnvironment) null));
        assertEquals(IStructureElement.PlaceResult.REJECT,
                decorated.survivalPlaceBlock(context, null, 1, 2, 3, null, null, null, null));
        assertEquals(2, decorated.getStepA());
        assertEquals(-1, decorated.getStepB());
        assertEquals(3, decorated.getStepC());
        assertTrue(decorated.resetA());
        assertFalse(decorated.resetB());
        assertTrue(decorated.resetC());
        assertTrue(decorated.isNavigating());
    }

    @Test
    void decoratingDoesNotRunCallbacksAndKeepsTheOriginalSpawnResult() {
        RecordingElement original = new RecordingElement();
        IStructureElement<Object> decorated = original.withHint(HintColor.WHITE);
        assertEquals(0, original.hintCalls);
        assertFalse(decorated.spawnHint(new Object(), null, 1, 2, 3, null));
        assertEquals(1, original.hintCalls);
    }

    @Test
    void elementAndFrameColorAreRequired() {
        assertThrows(NullPointerException.class, () -> StructureUtility.withHint(null, HintColor.BLUE));
        assertThrows(NullPointerException.class, () -> StructureUtility.withHint(new RecordingElement(), null));
    }

    private static final class RecordingElement implements IStructureElement<Object> {
        private final Collection<String> channels = Collections.singletonList("casing");
        private final List<String> description = Collections.singletonList("test.casing");
        private final BlocksToPlace candidates = BlocksToPlace.create(stack -> true);
        private int hintCalls;

        @Override
        public boolean check(Object context, World world, int x, int y, int z) { return true; }

        @Override
        public boolean couldBeValid(Object context, World world, int x, int y, int z, ItemStack trigger) { return false; }

        @Override
        public boolean placeBlock(Object context, World world, int x, int y, int z, ItemStack trigger) { return true; }

        @Override
        public boolean spawnHint(Object context, World world, int x, int y, int z, ItemStack trigger) {
            hintCalls++;
            return false;
        }

        @Override
        public Collection<String> getChannels() { return channels; }

        @Override
        public List<String> getDescription(Object context) { return description; }

        @Override
        public BlocksToPlace getBlocksToPlace(Object context, World world, int x, int y, int z, ItemStack trigger,
                AutoPlaceEnvironment environment) { return candidates; }

        @Override
        public PlaceResult survivalPlaceBlock(Object context, World world, int x, int y, int z, ItemStack trigger,
                AutoPlaceEnvironment environment) { return PlaceResult.STOP; }

        @Override
        @Deprecated
        public PlaceResult survivalPlaceBlock(Object context, World world, int x, int y, int z, ItemStack trigger,
                IItemSource source, EntityPlayerMP actor, Consumer<IChatComponent> chatter) { return PlaceResult.REJECT; }

        @Override
        public int getStepA() { return 2; }

        @Override
        public int getStepB() { return -1; }

        @Override
        public int getStepC() { return 3; }

        @Override
        public boolean resetA() { return true; }

        @Override
        public boolean resetB() { return false; }

        @Override
        public boolean resetC() { return true; }

        @Override
        public boolean isNavigating() { return true; }
    }
}
