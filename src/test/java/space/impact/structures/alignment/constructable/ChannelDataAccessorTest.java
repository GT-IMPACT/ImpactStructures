package space.impact.structures.alignment.constructable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

class ChannelDataAccessorTest {
    private ItemStack stack() {
        ItemStack result = new ItemStack(new Item(), 3, 9);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("owner", "unchanged");
        result.setTagCompound(tag);
        return result;
    }

    @Test
    void roundTripsChannelsThroughCompressedNbtWithoutChangingOtherData() throws Exception {
        ItemStack projector = stack();
        ChannelDataAccessor.setChannelData(projector, "coil tier", 7);
        ChannelDataAccessor.setChannelData(projector, "glass", 2);
        ByteArrayOutputStream saved = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(projector.getTagCompound(), saved);
        ItemStack loaded = stack();
        loaded.setTagCompound(CompressedStreamTools.readCompressed(new ByteArrayInputStream(saved.toByteArray())));
        assertEquals(7, ChannelDataAccessor.getChannelData(loaded, "coil tier"));
        assertEquals(2, ChannelDataAccessor.countChannelData(loaded));
        assertEquals("unchanged", loaded.getTagCompound().getString("owner"));
        Map<String, Integer> entries = ChannelDataAccessor.iterateChannelData(loaded)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(Integer.valueOf(2), entries.get("glass"));
    }

    @Test
    void switchesMasterValueWithoutChangingOriginalCountOrDamage() {
        ItemStack projector = stack();
        ChannelDataAccessor.setChannelData(projector, "coil tier", 7);
        ItemStack selected = ChannelDataAccessor.withChannel(projector, "coil tier");
        assertNotSame(projector, selected);
        assertEquals(7, selected.stackSize);
        assertEquals(3, projector.stackSize);
        assertEquals(9, selected.getItemDamage());
        assertEquals(3, ChannelDataAccessor.getChannelData(projector, "absent"));
    }

    @Test
    void unsetAndWipePreserveOtherNbt() {
        ItemStack projector = stack();
        ChannelDataAccessor.setChannelData(projector, "a", 1);
        ChannelDataAccessor.setChannelData(projector, "b", 2);
        ChannelDataAccessor.unsetChannelData(projector, "a");
        assertFalse(ChannelDataAccessor.hasSubChannel(projector, "a"));
        assertTrue(ChannelDataAccessor.hasSubChannel(projector, "b"));
        ChannelDataAccessor.wipeChannelData(projector);
        assertFalse(ChannelDataAccessor.hasSubChannel(projector));
        assertEquals("unchanged", projector.getTagCompound().getString("owner"));
    }

    @Test
    void rejectsInvalidNamesAndValuesWithoutDependingOnDebugMode() {
        ItemStack projector = stack();
        assertThrows(IllegalArgumentException.class, () -> ChannelDataAccessor.setChannelData(projector, "", 1));
        assertThrows(IllegalArgumentException.class, () -> ChannelDataAccessor.setChannelData(projector, "TIER", 1));
        assertThrows(IllegalArgumentException.class, () -> ChannelDataAccessor.setChannelData(projector, "tier", 0));
        assertThrows(IllegalArgumentException.class, () -> ChannelDataAccessor.setChannelData(projector, "tier", -1));
    }
}
