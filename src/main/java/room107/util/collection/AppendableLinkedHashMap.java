package room107.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Extends LinkedHashMap<K, List<V>>. NOT thread-safe.
 * 
 * @author WangXiao
 */
public class AppendableLinkedHashMap<K, V> extends LinkedHashMap<K, List<V>> {

    /**
     * A read-only empty instance.
     */
    private static final AppendableLinkedHashMap<Object, Object> EMPTY = new AppendableLinkedHashMap<Object, Object>() {

        private static final long serialVersionUID = 2358471208066310887L;

        @Override
        public void clear() {
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Set<Entry<Object, List<Object>>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public List<Object> get(Object key) {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Set<Object> keySet() {
            return Collections.emptySet();
        }

        @Override
        public List<Object> put(Object key, List<Object> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends Object, ? extends List<Object>> m) {
        }

        @Override
        public List<Object> append(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Object> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Collection<List<Object>> values() {
            return Collections.emptySet();
        }

    };

    private static final long serialVersionUID = -8662200025248775622L;

    /**
     * @return a read-only empty instance
     * @throws UnsupportedOperationException
     *             when call any operation which may cause modification to the
     *             instance
     */
    @SuppressWarnings("unchecked")
    public static <K, V> AppendableLinkedHashMap<K, V> empty()
            throws UnsupportedOperationException {
        return (AppendableLinkedHashMap<K, V>) EMPTY;
    }

    public AppendableLinkedHashMap() {
        super();
    }

    public AppendableLinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public AppendableLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public AppendableLinkedHashMap(Map<? extends K, ? extends List<V>> m) {
        super(m);
    }

    /**
     * Append a value to the end of a list specified by a key.
     * 
     * @return the list which contains this value
     */
    public List<V> append(K key, V value) {
        List<V> result = get(key);
        if (result == null) {
            result = new ArrayList<V>();
            put(key, result);
        }
        result.add(value);
        return result;
    }

}
