package space.impact.structures.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import org.junit.jupiter.api.Test;

class ItemStackMapTest {
    @Test
    void ignoresSizeAndDefendsAgainstInventoryStackMutation() {
        Item item = new Item();
        ItemStack original = new ItemStack(item, 12, 3);
        original.setTagCompound(tag("original"));
        ItemStackMap<String> map = new ItemStackMap<>(true);
        map.put(original, "value");
        original.stackSize = 1;
        original.setItemDamage(9);
        original.getTagCompound().setString("mode", "changed");
        ItemStack lookup = new ItemStack(item, 64, 3);
        lookup.setTagCompound(tag("original"));
        assertEquals("value", map.get(lookup));
        assertNull(map.get(original));
    }

    @Test
    void distinguishesNbtWhenRequestedIncludingExplicitEmptyTag() {
        Item item = new Item();
        ItemStack first = new ItemStack(item, 1, 2);
        first.setTagCompound(tag("a"));
        ItemStack second = new ItemStack(item, 1, 2);
        second.setTagCompound(tag("b"));
        ItemStackMap<String> sensitive = new ItemStackMap<>(true);
        sensitive.put(first, "a");
        sensitive.put(second, "b");
        assertEquals(2, sensitive.size());
        assertEquals("a", sensitive.get(first));
        assertEquals("b", sensitive.get(second));
        ItemStackMap<String> insensitive = new ItemStackMap<>();
        insensitive.put(first, "a");
        assertEquals("a", insensitive.get(second));
        ItemStack empty = new ItemStack(item, 1, 7);
        empty.setTagCompound(new NBTTagCompound());
        sensitive.put(empty, "empty");
        assertEquals("empty", sensitive.get(empty));
        assertNull(sensitive.get(new ItemStack(item, 4, 7)));
    }

    @Test
    void broadWildcardEntriesOverrideSpecificRegistrations() {
        Item item = new Item();
        ItemStackMap<String> map = new ItemStackMap<>(true);
        ItemStack any = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
        any.setTagCompound((NBTTagCompound) ItemStackMap.WILDCARD_TAG.copy());
        map.put(any, "fallback");
        ItemStack exactMetadata = new ItemStack(item, 1, 4);
        exactMetadata.setTagCompound((NBTTagCompound) ItemStackMap.WILDCARD_TAG.copy());
        map.put(exactMetadata, "metadata");
        ItemStack exact = new ItemStack(item, 1, 4);
        exact.setTagCompound(tag("exact"));
        map.put(exact, "exact");
        assertEquals("fallback", map.get(exact));
        map.remove(any);
        assertEquals("metadata", map.get(exact));
        ItemStack otherTag = new ItemStack(item, 1, 4);
        otherTag.setTagCompound(tag("other"));
        assertEquals("metadata", map.get(otherTag));
        assertNull(map.get(new ItemStack(item, 1, 6)));
        map.remove(exactMetadata);
        assertEquals("exact", map.get(exact));
        assertNull(map.get(new ItemStack(new Item(), 1, 4)));
    }

    @Test
    void entryKeysAreCopiesAndIteratorSupportsRemoval() {
        Item item = new Item();
        ItemStackMap<Integer> map = new ItemStackMap<>(true);
        ItemStack key = new ItemStack(item, 1, 3);
        map.put(key, 2);
        Map.Entry<ItemStack, Integer> entry = map.entrySet().iterator().next();
        entry.getKey().setItemDamage(8);
        entry.setValue(7);
        assertEquals(Integer.valueOf(7), map.get(key));
        java.util.Iterator<Map.Entry<ItemStack, Integer>> iterator = map.entrySet().iterator();
        iterator.next();
        iterator.remove();
        assertTrue(map.isEmpty());
    }

    @Test
    void computeAndMergeModifyExactRegistrationInsteadOfWildcardFallback() {
        Item item = new Item();
        ItemStack wildcard = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
        ItemStack exact = new ItemStack(item, 1, 4);
        ItemStackMap<Integer> map = new ItemStackMap<>();
        map.put(wildcard, 10);
        assertEquals(Integer.valueOf(3), map.computeIfAbsent(exact, stack -> 3));
        assertEquals(Integer.valueOf(7), map.merge(exact, 4, Integer::sum));
        assertEquals(Integer.valueOf(10), map.get(exact));
        map.remove(wildcard);
        assertEquals(Integer.valueOf(7), map.get(exact));
    }

    @Test
    void metadataWildcardStillMatchesNbtUntilExplicitNbtWildcardIsSet() {
        Item item = new Item();
        ItemStackMap<String> map = new ItemStackMap<>(true);
        ItemStack wildcardMetadata = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
        wildcardMetadata.setTagCompound(tag("one"));
        map.put(wildcardMetadata, "one");
        ItemStack one = new ItemStack(item, 8, 7);
        one.setTagCompound(tag("one"));
        ItemStack two = new ItemStack(item, 8, 7);
        two.setTagCompound(tag("two"));
        assertEquals("one", map.get(one));
        assertNull(map.get(two));
        assertNull(map.get(new ItemStack(item, 1, 7)));
        wildcardMetadata.getTagCompound().setBoolean("*", true);
        map.put(wildcardMetadata, "any");
        assertEquals("any", map.get(two));
        assertEquals("any", map.get(new ItemStack(item, 1, 7)));
    }

    private static NBTTagCompound tag(String mode) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("mode", mode);
        return tag;
    }
}
