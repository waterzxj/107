package room107.util;

import java.util.Map;

import lombok.AllArgsConstructor;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.Validate;

/**
 * @author WangXiao
 */
public class CacheUtils {

    @SuppressWarnings("unchecked")
    private static final Map<Object, CacheEntry> GLOBAL_CACHE = new LRUMap(
            10240);

    @AllArgsConstructor
    public static class CacheEntry {

        private Object value;

        private long time;

    }

    public static interface ICacheEvaler<K, V> {

        V eval(K key);

    }

    /**
     * Thread-safe.
     */
    @SuppressWarnings("unchecked")
    public synchronized static <K, V> V getValue(K key, int ttl,
            ICacheEvaler<K, V> evaler) {
        Validate.notNull(key);
        Validate.notNull(evaler);
        CacheEntry entry = GLOBAL_CACHE.get(key);
        if (entry == null) { // first time
            V result = evaler.eval(key);
            GLOBAL_CACHE.put(key,
                    new CacheEntry(result, System.currentTimeMillis()));
            return result;
        } else {
            long time = System.currentTimeMillis();
            if ((time - entry.time) / 1000 > ttl) {
                entry.value = evaler.eval(key);
                entry.time = time;
            }
            return (V) entry.value;
        }
    }
}
