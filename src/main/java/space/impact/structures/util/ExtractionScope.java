package space.impact.structures.util;

import java.util.HashSet;
import java.util.Set;

/** Internal, thread-confined state for a single inventory query; never cached between calls. */
public final class ExtractionScope implements AutoCloseable {
    private static final ThreadLocal<Set<String>> ACTIVE = new ThreadLocal<>();
    private final boolean owner;

    private ExtractionScope() {
        owner = ACTIVE.get() == null;
        if (owner) ACTIVE.set(new HashSet<>());
    }

    public static ExtractionScope open() {
        return new ExtractionScope();
    }

    public static boolean claim(String key) {
        Set<String> visited = ACTIVE.get();
        return visited == null || visited.add(key);
    }

    @Override
    public void close() {
        if (owner) ACTIVE.remove();
    }
}
