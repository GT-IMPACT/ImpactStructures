package space.impact.structures.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;

final class TestItems {
    private TestItems() {}

    /** Register serialization fixtures without starting the Forge mod loader. */
    static void register(int id, String name, Item item) throws Exception {
        Method add = FMLControlledNamespacedRegistry.class.getDeclaredMethod(
                "addObjectRaw", int.class, String.class, Object.class);
        add.setAccessible(true);
        add.invoke(Item.itemRegistry, id, "impactapi_test:" + name, item);
    }

    @SuppressWarnings("unchecked")
    static void initializeMock(Item item) throws Exception {
        Field delegate = Item.class.getField("delegate");
        delegate.setAccessible(true);
        delegate.set(item, ((FMLControlledNamespacedRegistry<Item>) Item.itemRegistry).getDelegate(item, Item.class));
    }
}
