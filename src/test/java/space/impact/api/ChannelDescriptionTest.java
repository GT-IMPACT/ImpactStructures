package space.impact.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.Test;

class ChannelDescriptionTest {
    @Test
    void choosesTheSameFirstChannelForPickingAndDragAndDropRegardlessOfRegistrationOrder() {
        ItemStack indicator = new ItemStack(new Item(), 1, 0);
        ChannelDescription.item("test-z-channel", 7, indicator);
        ChannelDescription.item("test-a-channel", 2, indicator);
        ArrayList<Map.Entry<String, Integer>> channels = new ArrayList<>(ChannelDescription.iterate(indicator));
        assertEquals("test-a-channel", channels.get(0).getKey());
        assertEquals(Integer.valueOf(2), channels.get(0).getValue());
        assertEquals("test-z-channel", channels.get(1).getKey());
    }

    @Test
    void updatesIndicatorMappingAndIgnoresStackCount() {
        Item item = new Item();
        ChannelDescription.item("test-tier", 2, new ItemStack(item, 1, 3));
        ChannelDescription.item("test-tier", 4, new ItemStack(item, 64, 3));
        Map.Entry<String, Integer> mapping = ChannelDescription.iterate(new ItemStack(item, 7, 3)).iterator().next();
        assertEquals(Integer.valueOf(4), mapping.getValue());
    }

    @Test
    void validatesBindingsOutsideDebugMode() {
        assertFalse(ChannelDescription.has(""));
        assertThrows(IllegalArgumentException.class, () -> ChannelDescription.get("UPPERCASE"));
        assertThrows(IllegalArgumentException.class, () -> ChannelDescription.item("test-invalid", 0, new ItemStack(new Item())));
        assertThrows(IllegalArgumentException.class, () -> ChannelDescription.item("test-invalid", 1, null));
    }
}
