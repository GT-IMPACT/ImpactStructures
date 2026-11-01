package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import com.rwtema.extrautils.gui.SlotItemContainer;
import com.rwtema.extrautils.item.ItemGoldenBag;

class GoldenBagTest {
    @Test
    void simulationPreservesNbtAndExtractionUpdatesOnlyMatchingSlots() throws Exception {
        Item material = new Item();
        TestItems.register(31004, "golden_bag_material", material);
        ItemGoldenBag bag = mock(ItemGoldenBag.class);
        TestItems.initializeMock(bag);
        ItemStack source = new ItemStack(bag);
        ExtraUtilitiesIntegration adapter = new ExtraUtilitiesIntegration();
        assertEquals(0, adapter.takeFromStack(s -> true, true, 1, (s, n) -> {}, source, null, null));
        assertNull(source.getTagCompound());
        NBTTagCompound contents = new NBTTagCompound();
        contents.setBoolean("enchanted", true);
        contents.setInteger("Color", 123);
        ItemStack wanted = new ItemStack(material, 6, 2);
        wanted.setTagCompound(new NBTTagCompound());
        wanted.getTagCompound().setString("variant", "wanted");
        contents.setTag("items_53", wanted.writeToNBT(new NBTTagCompound()));
        ItemStack other = wanted.copy();
        other.getTagCompound().setString("variant", "other");
        contents.setTag("items_0", other.writeToNBT(new NBTTagCompound()));
        source.setTagCompound(contents);
        InventoryBasic playerInventory = new InventoryBasic("test", false, 1);
        playerInventory.setInventorySlotContents(0, source);
        SlotItemContainer lastSlot = new SlotItemContainer(playerInventory, 53, 0, 0, 0);
        assertEquals(6, lastSlot.getStack().stackSize);
        NBTTagCompound before = (NBTTagCompound) contents.copy();
        assertEquals(6, adapter.takeFromStack(s -> ItemStack.areItemStackTagsEqual(s, wanted), true, 8,
                (s, n) -> {}, source, null, null));
        assertEquals(before, source.getTagCompound());
        assertEquals(4, adapter.takeFromStack(s -> ItemStack.areItemStackTagsEqual(s, wanted), false, 4,
                (s, n) -> {}, source, null, null));
        assertEquals(2, ItemStack.loadItemStackFromNBT(contents.getCompoundTag("items_53")).stackSize);
        assertEquals(2, lastSlot.getStack().stackSize);
        assertEquals(before.getTag("items_0"), contents.getTag("items_0"));
        assertEquals(before.getTag("enchanted"), contents.getTag("enchanted"));
        assertEquals(before.getTag("Color"), contents.getTag("Color"));
        assertEquals(2, adapter.takeFromStack(s -> ItemStack.areItemStackTagsEqual(s, wanted), false, 10,
                (s, n) -> {}, source, null, null));
        assertFalse(contents.hasKey("items_53"));
        assertNull(lastSlot.getStack());
    }
}
