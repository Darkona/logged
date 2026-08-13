package io.github.darkona.logged.internals;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link LinkedHashMap} in access-order mode that evicts the least recently
 * used entry once {@code capacity} is exceeded. Not thread-safe on its own;
 * wrap with {@link java.util.Collections#synchronizedMap(Map)} for concurrent use.
 */
public final class BoundedLruMap<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    public BoundedLruMap(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
