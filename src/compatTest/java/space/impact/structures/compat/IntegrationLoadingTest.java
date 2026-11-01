package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class IntegrationLoadingTest {
    @Test
    void independentModCombinationsRegisterOnlyTheirOwnAdapters() throws Exception {
        check(Collections.emptySet());
        check(set("appliedenergistics2"));
        check(set("appliedenergistics2", "ae2wct"));
        check(set("appliedenergistics2", "ae2fc"));
        check(set("Baubles"));
        check(set("Forestry"));
        check(set("Backpack"));
        check(set("ExtraUtilities"));
        check(set("appliedenergistics2", "ae2wct", "ae2fc", "Baubles", "Forestry", "Backpack", "ExtraUtilities"));
    }

    private void check(Set<String> mods) throws Exception {
        URL classes = OptionalIntegrations.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader isolated = new URLClassLoader(new URL[] { classes }, getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                String mod = name.startsWith("appeng.") ? "appliedenergistics2"
                        : name.startsWith("net.p455w0rd.") ? "ae2wct"
                        : name.startsWith("com.glodblock.") ? "ae2fc"
                        : name.startsWith("baubles.") ? "Baubles"
                        : name.startsWith("forestry.") ? "Forestry"
                        : name.startsWith("de.eydamos.") ? "Backpack"
                        : name.startsWith("com.rwtema.extrautils.") ? "ExtraUtilities" : null;
                if (mod != null && !mods.contains(mod)) throw new ClassNotFoundException(name);
                if (name.startsWith("space.impact.structures.")) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) loaded = findClass(name);
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
                return super.loadClass(name, resolve);
            }
        }) {
            Class<?> bootstrap = isolated.loadClass(OptionalIntegrations.class.getName());
            Method load = bootstrap.getDeclaredMethod("load", Predicate.class, ClassLoader.class);
            load.setAccessible(true);
            load.invoke(null, (Predicate<String>) mods::contains, isolated);
            Class<?> utility = isolated.loadClass("space.impact.structures.util.InventoryUtility");
            utility.getMethod("init").invoke(null);
            Class<?> registry = isolated.loadClass("space.impact.structures.SortedRegistry");
            Object extractors = registry.getMethod("getRegistry", String.class).invoke(null, "stackExtractors");
            Object providers = registry.getMethod("getRegistry", String.class).invoke(null, "inventoryProviders");
            Method contains = registry.getMethod("containsKey", String.class);
            assertEquals(mods.contains("appliedenergistics2"), contains.invoke(extractors, "1000-ae2-wireless"), mods.toString());
            assertEquals(mods.contains("ae2wct"), contains.invoke(extractors, "0990-ae2wct"), mods.toString());
            assertEquals(mods.contains("ae2fc"), contains.invoke(extractors, "0991-ae2fc"), mods.toString());
            assertEquals(mods.contains("Forestry"), contains.invoke(extractors, "1000-forestry-backpack"), mods.toString());
            assertEquals(mods.contains("Backpack"), contains.invoke(extractors, "1000-backpack"), mods.toString());
            assertEquals(mods.contains("ExtraUtilities"),
                    contains.invoke(extractors, "1000-extrautilities-golden-bag"), mods.toString());
            assertEquals(mods.contains("Backpack"), contains.invoke(providers, "5002-equipped-backpack"), mods.toString());
            assertEquals(mods.contains("Baubles"), contains.invoke(providers, "5001-baubles"), mods.toString());
        }
    }

    private static Set<String> set(String... values) { return new HashSet<>(Arrays.asList(values)); }
}
