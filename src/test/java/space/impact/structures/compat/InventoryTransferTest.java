package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

class InventoryTransferTest {
    @Test
    void simulationIsReadOnlyAndRealExtractionIsLimitedAndFiltered() {
        InventoryBasic inventory = new InventoryBasic("test", false, 3);
        Item item = new Item();
        ItemStack first = new ItemStack(item, 4, 1);
        first.setTagCompound(new NBTTagCompound());
        first.getTagCompound().setString("variant", "wanted");
        inventory.setInventorySlotContents(0, first);
        inventory.setInventorySlotContents(1, new ItemStack(item, 32, 2));
        inventory.setInventorySlotContents(2, first.copy());
        AtomicInteger collected = new AtomicInteger();
        assertEquals(6, InventoryTransfer.take(inventory, s -> s.getItemDamage() == 1, true, 6,
                (s, count) -> collected.addAndGet(count)));
        assertEquals(6, collected.get());
        assertEquals(4, first.stackSize);
        assertEquals("wanted", first.getTagCompound().getString("variant"));
        assertEquals(6, InventoryTransfer.take(inventory, s -> s.getItemDamage() == 1, false, 6, (s, n) -> {}));
        assertNull(inventory.getStackInSlot(0));
        assertEquals(32, inventory.getStackInSlot(1).stackSize);
        assertEquals(2, inventory.getStackInSlot(2).stackSize);
    }
}
