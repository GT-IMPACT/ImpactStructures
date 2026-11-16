package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.common.Loader;

class MeFluidExtractionTest {

    private Field loaderField;
    private Object previousLoader;
    private final AtomicInteger stock = new AtomicInteger(4000);
    private final AtomicInteger power = new AtomicInteger(2);
    private final AtomicInteger actualExtractions = new AtomicInteger();
    private final FluidStack request = mock(FluidStack.class);
    private final PlayerSource actor = mock(PlayerSource.class);
    private IStorageHelper helper;
    private MeExtractor.Session session;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void prepareNetwork() throws Exception {
        loaderField = Loader.class.getDeclaredField("instance");
        loaderField.setAccessible(true);
        previousLoader = loaderField.get(null);
        Loader loader = mock(Loader.class);
        when(loader.getIndexedModList()).thenReturn(Collections.emptyMap());
        Field namedMods = Loader.class.getDeclaredField("namedMods");
        namedMods.setAccessible(true);
        namedMods.set(loader, Collections.emptyMap());
        loaderField.set(null, loader);

        request.amount = 1000;
        when(request.copy()).thenReturn(request);
        helper = mock(IStorageHelper.class);
        when(helper.createFluidStack(any())).thenAnswer(call -> aeFluid(((FluidStack) call.getArgument(0)).amount));
        IMEInventory<IAEFluidStack> fluids = mock(IMEInventory.class);
        when(fluids.extractItems(any(), any(), any())).thenAnswer(call -> {
            IAEFluidStack wanted = call.getArgument(0);
            int amount = (int) Math.min(stock.get(), wanted.getStackSize());
            if (call.getArgument(1) == Actionable.MODULATE) {
                stock.addAndGet(-amount);
                actualExtractions.incrementAndGet();
            }
            return amount > 0 ? aeFluid(amount) : null;
        });
        session = new MeExtractor.Session(null, fluids, (amount, action, multiplier) -> {
            int supplied = (int) Math.min(power.get(), multiplier.multiply(amount));
            if (action == Actionable.MODULATE) {
                power.addAndGet(-supplied);
            }
            return multiplier.divide(supplied);
        }, item -> true);
    }

    @AfterEach
    void restoreLoader() throws Exception {
        loaderField.set(null, previousLoader);
    }

    @Test
    void simulationPreservesFluidAndEnergyAndCommitConsumesOneBucket() {
        assertTrue(session.takeFluid(request, true, actor, helper));
        assertEquals(4000, stock.get());
        assertEquals(2, power.get());
        assertEquals(0, actualExtractions.get());
        assertTrue(session.takeFluid(request, false, actor, helper));
        assertEquals(3000, stock.get());
        assertEquals(1, power.get());
        assertEquals(1, actualExtractions.get());
    }

    @Test
    void insufficientFluidIsNotPartiallyConsumed() {
        stock.set(999);
        assertFalse(session.takeFluid(request, true, actor, helper));
        assertFalse(session.takeFluid(request, false, actor, helper));
        assertEquals(999, stock.get());
        assertEquals(2, power.get());
        assertEquals(0, actualExtractions.get());
    }

    @Test
    void emptyBatteryPreventsExtraction() {
        power.set(0);
        assertFalse(session.takeFluid(request, true, actor, helper));
        assertFalse(session.takeFluid(request, false, actor, helper));
        assertEquals(4000, stock.get());
        assertEquals(0, actualExtractions.get());
    }

    @Test
    void itemOnlySessionDoesNotSupplyFluids() {
        MeExtractor.Session portable = new MeExtractor.Session(null, (amount, action, multiplier) -> amount,
                item -> true);
        assertFalse(portable.takeFluid(request, false, actor, helper));
    }

    private static IAEFluidStack aeFluid(int amount) {
        AtomicInteger size = new AtomicInteger(amount);
        IAEFluidStack fluid = mock(IAEFluidStack.class);
        when(fluid.getStackSize()).thenAnswer(call -> (long) size.get());
        when(fluid.getAmountPerUnit()).thenReturn(1000);
        when(fluid.copy()).thenAnswer(call -> aeFluid(size.get()));
        when(fluid.setStackSize(anyLong())).thenAnswer(call -> {
            size.set(((Long) call.getArgument(0)).intValue());
            return fluid;
        });
        return fluid;
    }
}
