package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class OptionalIntegrationsTest {
    @Test
    void absentModsNeverResolveOptionalClasses() {
        List<String> attempted = new ArrayList<>();
        OptionalIntegrations.load(id -> false, rejectingLoader(attempted));
        assertTrue(attempted.isEmpty());
    }

    @Test
    void addonsRequireAe2AndFluidCraftDoesNotRequireWct() {
        List<String> attempted = new ArrayList<>();
        OptionalIntegrations.load(id -> id.equals("ae2wct") || id.equals("ae2fc"), rejectingLoader(attempted));
        assertTrue(attempted.isEmpty());
        OptionalIntegrations.load(id -> id.equals("appliedenergistics2") || id.equals("ae2fc"),
                rejectingLoader(attempted));
        assertEquals(Arrays.asList("com.glodblock.github.common.item.ItemBaseWirelessTerminal", "appeng.api.AEApi"),
                attempted);
    }

    @Test
    void missingApiInOneModDoesNotPreventOtherIntegrations() {
        List<String> attempted = new ArrayList<>();
        OptionalIntegrations.load(id -> true, rejectingLoader(attempted));
        assertEquals(7, attempted.size());
        assertEquals("de.eydamos.backpack.saves.BackpackSave", attempted.get(5));
        assertEquals("com.rwtema.extrautils.item.ItemGoldenBag", attempted.get(6));
    }

    private static ClassLoader rejectingLoader(List<String> attempted) {
        return new ClassLoader(null) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                attempted.add(name);
                throw new ClassNotFoundException(name);
            }
        };
    }
}
