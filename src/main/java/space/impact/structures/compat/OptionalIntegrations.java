package space.impact.structures.compat;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;

/** Internal bootstrap. No optional mod types may appear in this class. */
public final class OptionalIntegrations {

    private static final Logger LOG = LogManager.getLogger("Impact Structures/Integrations");
    private static final Entry[] ENTRIES = {
            new Entry("WctIntegration", new String[] { "appliedenergistics2", "ae2wct" },
                    "net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem",
                    "cofh.api.energy.IEnergyContainerItem"),
            new Entry("FluidCraftIntegration", new String[] { "appliedenergistics2", "ae2fc" },
                    "com.glodblock.github.common.item.ItemBaseWirelessTerminal"),
            new Entry("Ae2Integration", new String[] { "appliedenergistics2" }, "appeng.api.AEApi"),
            new Entry("BaublesIntegration", new String[] { "Baubles" }, "baubles.api.BaublesApi"),
            new Entry("ForestryIntegration", new String[] { "Forestry" },
                    "forestry.storage.inventory.ItemInventoryBackpack"),
            new Entry("BackpackIntegration", new String[] { "Backpack" },
                    "de.eydamos.backpack.saves.BackpackSave"),
            new Entry("ExtraUtilitiesIntegration", new String[] { "ExtraUtilities" },
                    "com.rwtema.extrautils.item.ItemGoldenBag")
    };
    private static boolean initialized;

    private OptionalIntegrations() {}

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        load(Loader::isModLoaded, OptionalIntegrations.class.getClassLoader());
    }

    static void load(Predicate<String> modLoaded, ClassLoader loader) {
        for (Entry entry : ENTRIES) {
            if (!entry.matches(modLoaded)) continue;
            try {
                for (String api : entry.apis) Class.forName(api, false, loader);
                Class.forName("space.impact.structures.compat." + entry.name, true, loader)
                        .getMethod("register").invoke(null);
                LOG.info("Enabled {}", entry.name);
            } catch (InvocationTargetException e) {
                LOG.error("Cannot enable " + entry.name, e.getCause());
            } catch (ReflectiveOperationException | LinkageError e) {
                LOG.error("Cannot enable " + entry.name + "; check the installed mod version", e);
            }
        }
    }

    private static final class Entry {
        final String name;
        final String[] mods;
        final String[] apis;

        Entry(String name, String[] mods, String... apis) {
            this.name = name;
            this.mods = mods;
            this.apis = apis;
        }

        boolean matches(Predicate<String> loaded) {
            for (String mod : mods) if (!loaded.test(mod)) return false;
            return true;
        }
    }
}
