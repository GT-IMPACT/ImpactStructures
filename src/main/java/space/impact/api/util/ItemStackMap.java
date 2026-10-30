package space.impact.api.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Item lookup by item, metadata and optionally NBT, independently of stack size.
 * Stored keys are copied: changing an inventory stack never corrupts a lookup.
 * Wildcard registrations take precedence, matching the source registry's lookup semantics.
 */
public final class ItemStackMap<V> extends AbstractMap<ItemStack, V> {

    public static final NBTTagCompound WILDCARD_TAG = new NBTTagCompound();
    static { WILDCARD_TAG.setBoolean("*", true); }

    private final boolean nbtSensitive;
    private final Map<Key, Stored<V>> values = new LinkedHashMap<>();

    public ItemStackMap() { this(false); }

    public ItemStackMap(boolean nbtSensitive) { this.nbtSensitive = nbtSensitive; }

    @Override
    public V put(ItemStack stack, V value) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(stack.getItem(), "stack item");
        Objects.requireNonNull(value, "value");
        Key key = new Key(stack, nbtSensitive);
        Stored<V> previous = values.put(key, new Stored<>(stack, value));
        return previous == null ? null : previous.value;
    }

    @Override
    public V get(Object object) {
        Stored<V> found = find(object);
        return found == null ? null : found.value;
    }

    @Override
    public boolean containsKey(Object object) { return find(object) != null; }

    private Stored<V> find(Object object) {
        if (!(object instanceof ItemStack)) return null;
        ItemStack stack = (ItemStack) object;
        if (stack.getItem() == null) return null;
        Key query = new Key(stack, nbtSensitive);
        NBTTagCompound anyTag = nbtSensitive ? WILDCARD_TAG : null;
        Stored<V> found = values.get(new Key(query.item, OreDictionary.WILDCARD_VALUE, anyTag));
        if (found != null) return found;
        found = values.get(new Key(query.item, query.damage, anyTag));
        if (found != null) return found;
        if (nbtSensitive) {
            found = values.get(new Key(query.item, OreDictionary.WILDCARD_VALUE, query.tag));
            if (found != null) return found;
        }
        return values.get(query);
    }

    /** Removes an exact registered key; looking up a wildcard does not remove it accidentally. */
    @Override
    public V remove(Object object) {
        if (!(object instanceof ItemStack) || ((ItemStack) object).getItem() == null) return null;
        Stored<V> removed = values.remove(new Key((ItemStack) object, nbtSensitive));
        return removed == null ? null : removed.value;
    }

    @Override
    public void clear() { values.clear(); }

    @Override
    public int size() { return values.size(); }

    /** Mutation operations address the registered key, without resolving fallback mappings. */
    @Override
    public V computeIfAbsent(ItemStack stack, Function<? super ItemStack, ? extends V> function) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(function, "function");
        Stored<V> existing = values.get(new Key(stack, nbtSensitive));
        if (existing != null) return existing.value;
        V computed = function.apply(stack);
        if (computed != null) put(stack, computed);
        return computed;
    }

    @Override
    public V merge(ItemStack stack, V value, BiFunction<? super V, ? super V, ? extends V> function) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(function, "function");
        Stored<V> existing = values.get(new Key(stack, nbtSensitive));
        V updated = existing == null ? value : function.apply(existing.value, value);
        if (updated == null) remove(stack); else put(stack, updated);
        return updated;
    }

    @Override
    public Set<Map.Entry<ItemStack, V>> entrySet() {
        return new AbstractSet<Map.Entry<ItemStack, V>>() {
            @Override
            public int size() { return values.size(); }

            @Override
            public void clear() { values.clear(); }

            @Override
            public Iterator<Map.Entry<ItemStack, V>> iterator() {
                final Iterator<Stored<V>> iterator = values.values().iterator();
                return new Iterator<Map.Entry<ItemStack, V>>() {
                    @Override
                    public boolean hasNext() { return iterator.hasNext(); }

                    @Override
                    public Map.Entry<ItemStack, V> next() {
                        final Stored<V> stored = iterator.next();
                        return new SimpleEntry<ItemStack, V>(stored.stack.copy(), stored.value) {
                            @Override
                            public V setValue(V value) {
                                Objects.requireNonNull(value, "value");
                                V previous = stored.value;
                                stored.value = value;
                                super.setValue(value);
                                return previous;
                            }
                        };
                    }

                    @Override
                    public void remove() { iterator.remove(); }
                };
            }
        };
    }

    private static final class Stored<V> {
        final ItemStack stack;
        V value;

        Stored(ItemStack stack, V value) {
            this.stack = stack.copy();
            this.stack.stackSize = 1;
            this.value = value;
        }
    }

    private static final class Key {
        final Item item;
        final int damage;
        final NBTTagCompound tag;

        Key(ItemStack stack, boolean nbtSensitive) {
            item = stack.getItem();
            damage = MiscUtils.getRawItemDamage(stack);
            NBTTagCompound original = nbtSensitive ? stack.getTagCompound() : null;
            tag = original == null ? null : original.getBoolean("*")
                    ? (NBTTagCompound) WILDCARD_TAG.copy() : (NBTTagCompound) original.copy();
        }

        private Key(Item item, int damage, NBTTagCompound tag) {
            this.item = item;
            this.damage = damage;
            this.tag = tag;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Key)) return false;
            Key other = (Key) object;
            return item == other.item && damage == other.damage && Objects.equals(tag, other.tag);
        }

        @Override
        public int hashCode() { return 31 * (31 * System.identityHashCode(item) + damage) + Objects.hashCode(tag); }
    }
}
