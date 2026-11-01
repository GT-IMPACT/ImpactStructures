package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import forestry.api.storage.IBackpackDefinition;
import forestry.storage.items.ItemBackpack;

class ForestryInventoryTest {
    @Test
    void queryingAnUninitializedBackpackDoesNotCreateNbt() throws Exception {
        ItemBackpack item = mock(ItemBackpack.class);
        TestItems.initializeMock(item);
        when(item.getBackpackSize()).thenReturn(15);
        when(item.getDefinition()).thenReturn(mock(IBackpackDefinition.class));
        ItemStack source = new ItemStack(item);
        assertEquals(0, new ForestryIntegration().takeFromStack(s -> true, true, 10, (s, n) -> {}, source, null, null));
        assertNull(source.getTagCompound());
    }

    @Test
    void simulationPreservesNbtAndExtractionSurvivesReopening() throws Exception {
        Item material = new Item();
        TestItems.register(31001, "forestry_material", material);
        ItemBackpack item = mock(ItemBackpack.class);
        TestItems.initializeMock(item);
        when(item.getBackpackSize()).thenReturn(15);
        when(item.getDefinition()).thenReturn(mock(IBackpackDefinition.class));
        ItemStack source = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("UID", "existing-uid");
        NBTTagCompound slots = new NBTTagCompound();
        NBTTagCompound contents = new NBTTagCompound();
        new ItemStack(material, 6, 3).writeToNBT(contents);
        slots.setTag("0", contents);
        tag.setTag("Slots", slots);
        source.setTagCompound(tag);
        NBTTagCompound before = (NBTTagCompound) tag.copy();
        ForestryIntegration extractor = new ForestryIntegration();
        assertEquals(4, extractor.takeFromStack(s -> s.getItem() == material, true, 4, (s, n) -> {}, source, null, null));
        assertEquals(before, source.getTagCompound());
        assertEquals(4, extractor.takeFromStack(s -> s.getItem() == material, false, 4, (s, n) -> {}, source, null, null));
        assertEquals(2, extractor.takeFromStack(s -> s.getItem() == material, true, 20, (s, n) -> {}, source, null, null));
        assertEquals("existing-uid", source.getTagCompound().getString("UID"));
    }
}
