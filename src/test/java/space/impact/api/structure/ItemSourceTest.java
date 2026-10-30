package space.impact.api.structure;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;
import java.util.Collections;

class ItemSourceTest {
    @Test
    void takeOneNormalizesExternalSourceKeysWithoutMutatingThem() {
        ItemStack original = new ItemStack(new Item(), 32, 4);
        IItemSource source = (predicate, simulate, count) -> Collections.singletonMap(original, 1);
        assertEquals(1, source.takeOne(stack -> true, true).stackSize);
        assertEquals(32, original.stackSize);
    }

    @Test
    void simulationAndInsufficientRequestsDoNotRemoveResources() {
        Item item = new Item();
        InventoryBasic inventory = new InventoryBasic("test", false, 2);
        inventory.setInventorySlotContents(0, new ItemStack(item, 5, 2));
        IItemSource source = IItemSource.fromInventory(inventory);
        assertFalse(source.takeAll(new ItemStack(item, 8, 2), false));
        assertEquals(5, inventory.getStackInSlot(0).stackSize);
        assertTrue(source.takeAll(new ItemStack(item, 4, 2), true));
        assertEquals(5, inventory.getStackInSlot(0).stackSize);
        assertTrue(source.takeAll(new ItemStack(item, 4, 2), false));
        assertEquals(1, inventory.getStackInSlot(0).stackSize);
    }

    @Test
    void nbtMismatchDoesNotConsumeSimilarItem() {
        Item item = new Item();
        InventoryBasic inventory = new InventoryBasic("test", false, 1);
        ItemStack stored = new ItemStack(item, 3, 2);
        stored.setTagCompound(new NBTTagCompound());
        stored.getTagCompound().setString("variant", "one");
        inventory.setInventorySlotContents(0, stored);
        ItemStack wanted = stored.copy();
        wanted.stackSize = 1;
        wanted.getTagCompound().setString("variant", "two");
        assertFalse(IItemSource.fromInventory(inventory).takeOne(wanted, false));
        assertEquals(3, inventory.getStackInSlot(0).stackSize);
    }
}
