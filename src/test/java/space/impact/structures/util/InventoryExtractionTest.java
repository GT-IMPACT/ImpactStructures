package space.impact.structures.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class InventoryExtractionTest {
    @Test
    void stackSimulationDoesNotMarkInventoryDirty() {
        DirtyInventory inventory = new DirtyInventory();
        inventory.setInventorySlotContents(0, new ItemStack(new Item(), 5, 0));
        inventory.dirty = 0;
        InventoryUtility.ItemStackExtractor extractor = InventoryUtility.newItemStackProvider(stack -> inventory);
        assertEquals(2, extractor.takeFromStack(s -> true, true, 2, (s, n) -> {}, null, null, null));
        assertEquals(0, inventory.dirty);
        assertEquals(5, inventory.getStackInSlot(0).stackSize);
        assertEquals(2, extractor.takeFromStack(s -> true, false, 2, (s, n) -> {}, null, null, null));
        assertEquals(1, inventory.dirty);
        assertEquals(3, inventory.getStackInSlot(0).stackSize);
    }

    @Test
    void oneQueryDoesNotCountTheSameExternalInventoryTwiceAndNextQueryStartsFresh() {
        Item bag = new Item();
        Item material = new Item();
        InventoryUtility.registerStackExtractor("test-unique-source", new InventoryUtility.ItemStackExtractor() {
            public boolean isAPIImplemented(APIType type) { return type == APIType.MAIN; }
            public boolean isValidSource(ItemStack stack, EntityPlayerMP player) {
                return stack.getItem() == bag;
            }
            public int takeFromStack(Predicate<ItemStack> predicate, boolean simulate, int count,
                    InventoryUtility.ItemStackCounter store, ItemStack source, ItemStack filter,
                    EntityPlayerMP player) {
                if (!ExtractionScope.claim("same-backpack-uid")) return 0;
                int found = Math.min(count, 3);
                store.add(new ItemStack(material), found);
                return found;
            }
        });
        for (int i = 0; i < 2; i++) {
            Map<ItemStack, Integer> result = InventoryUtility.takeFromInventory(
                    Arrays.asList(new ItemStack(bag), new ItemStack(bag)), s -> s.getItem() == material, true, 6, true);
            assertEquals(3, result.values().stream().mapToInt(Integer::intValue).sum());
        }
    }

    @Test
    void nestedScopeAndExceptionsDoNotLeakVisitedInventories() {
        assertThrows(IllegalStateException.class, () -> {
            try (ExtractionScope outer = ExtractionScope.open()) {
                assertTrue(ExtractionScope.claim("uid"));
                try (ExtractionScope inner = ExtractionScope.open()) {
                    assertFalse(ExtractionScope.claim("uid"));
                }
                assertFalse(ExtractionScope.claim("uid"));
                throw new IllegalStateException();
            }
        });
        try (ExtractionScope next = ExtractionScope.open()) {
            assertTrue(ExtractionScope.claim("uid"));
        }
    }

    private static final class DirtyInventory extends InventoryBasic {
        int dirty;
        DirtyInventory() { super("test", false, 1); }
        @Override public void markDirty() { dirty++; }
    }
}
