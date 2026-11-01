package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.Loader;

class MeExtractionTest {
    @Test
    @SuppressWarnings("unchecked")
    void exactExtractionHonorsPowerStockAndViewFilterInBothModes() throws Exception {
        Field loaderInstance = Loader.class.getDeclaredField("instance");
        loaderInstance.setAccessible(true);
        Object previous = loaderInstance.get(null);
        Loader loader = mock(Loader.class);
        when(loader.getIndexedModList()).thenReturn(Collections.emptyMap());
        Field namedMods = Loader.class.getDeclaredField("namedMods");
        namedMods.setAccessible(true);
        namedMods.set(loader, Collections.emptyMap());
        loaderInstance.set(null, loader);
        try {
            Item item = new Item();
            AtomicInteger stock = new AtomicInteger(4);
            AtomicInteger power = new AtomicInteger(2);
            AtomicInteger received = new AtomicInteger();
            IMEInventory<IAEItemStack> inventory = mock(IMEInventory.class);
            when(inventory.extractItems(any(), any(), any())).thenAnswer(call -> {
                IAEItemStack request = call.getArgument(0);
                int amount = (int) Math.min(request.getStackSize(), stock.get());
                if (call.getArgument(1) == Actionable.MODULATE) stock.addAndGet(-amount);
                return amount == 0 ? null : aeStack(item, amount);
            });
            IStorageHelper helper = mock(IStorageHelper.class);
            when(helper.createItemStack(any())).thenAnswer(call -> aeStack(item, ((ItemStack) call.getArgument(0)).stackSize));
            MeExtractor.Session session = new MeExtractor.Session(inventory, (amount, action, multiplier) -> {
                int supplied = (int) Math.min(power.get(), multiplier.multiply(amount));
                if (action == Actionable.MODULATE) power.addAndGet(-supplied);
                return multiplier.divide(supplied);
            }, stack -> true);
            ItemStack wanted = new ItemStack(item, 6, 0);
            PlayerSource actor = mock(PlayerSource.class);
            assertEquals(2, session.take(s -> true, wanted, true, 6, (s, n) -> received.addAndGet(n), actor, helper));
            assertEquals(2, received.get());
            assertEquals(4, stock.get());
            assertEquals(2, power.get());
            assertEquals(2, session.take(s -> true, wanted, false, 6, (s, n) -> {}, actor, helper));
            assertEquals(2, stock.get());
            assertEquals(0, power.get());
            assertEquals(0, session.take(s -> true, wanted, true, 6, (s, n) -> {}, actor, helper));
            MeExtractor.Session hidden = new MeExtractor.Session(inventory, (amount, action, multiplier) -> amount,
                    stack -> false);
            assertEquals(0, hidden.take(s -> true, wanted, false, 6, (s, n) -> fail(), actor, helper));
            assertEquals(2, stock.get());
        } finally {
            loaderInstance.set(null, previous);
        }
    }

    private static IAEItemStack aeStack(Item item, int count) {
        AtomicInteger size = new AtomicInteger(count);
        IAEItemStack stack = mock(IAEItemStack.class);
        when(stack.getStackSize()).thenAnswer(call -> (long) size.get());
        when(stack.getAmountPerUnit()).thenReturn(1);
        when(stack.getItemStack()).thenAnswer(call -> new ItemStack(item, size.get(), 0));
        when(stack.copy()).thenAnswer(call -> aeStack(item, size.get()));
        when(stack.setStackSize(anyLong())).thenAnswer(call -> {
            size.set(((Long) call.getArgument(0)).intValue());
            return stack;
        });
        return stack;
    }
}
