package hashmap;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private double loadFactor;
    private int size;

    /** Constructors */
    public MyHashMap() {
        this(16);
     }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] ret = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            ret[i] = createBucket();
        }
        return ret;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    /** Removes all of the mappings from this map. */
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        size = 0;
    }
    private Node getHelper(K key) {
        Collection<Node> bucket = buckets[Math.floorMod(key.hashCode(), buckets.length)];
        for (Node node : bucket) if (key.equals(node.key)) return node;
        return null;
        }

    /** Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key) {
        return getHelper(key) != null;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        Node node = getHelper(key);
        if (node != null) return node.value;
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    public int size() {
        return size;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    public void put(K key, V value) {
        Node node = getHelper(key);
        if (node != null) node.value = value;
        else {
            buckets[Math.floorMod(key.hashCode(), buckets.length)].add(createNode(key, value));
            size++;
        }
        if (size > buckets.length * loadFactor) {
            Collection<Node>[] tmp = createTable(buckets.length * 2);
            for (Collection<Node> bucket : buckets) {
                for (Node n: bucket) {
                    tmp[Math.floorMod(n.key.hashCode(), tmp.length)].add(n);
                }
            }
            buckets = tmp;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }
    private class KeyIterator implements Iterator<K> {
        Iterator<Node> iterator = buckets[0].iterator();
        int count = 0;
        int current = 0;
        @Override
        public boolean hasNext() {
            return count < size;
        }
        @Override
        public K next() {
            if (!hasNext()) return null;
            while (!iterator.hasNext()) {
                current++;
                iterator = buckets[current].iterator();
            }
            count++;
            return iterator.next().key;
        }
    }

    /** Returns a Set view of the keys contained in this map. */
    public Set<K> keySet() {
        return new KeySet();
    }
    private class KeySet implements Set<K> {

        @Override
        public boolean add(K arg0) {
            throw new UnsupportedOperationException("Unimplemented method 'add'");
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException("Unimplemented method 'addAll'");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Unimplemented method 'clear'");
        }

        @Override
        public boolean contains(Object o) {
            return containsKey((K) o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            boolean flag = true;
            for (Object o : c) if (!contains(o)) {
                flag = false;
                break;
            }
            return flag;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public Iterator<K> iterator() {
            return MyHashMap.this.iterator();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Unimplemented method 'remove'");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public Object[] toArray() {
            Object[] ret = new Object[size];
            int i = 0;
            for (Object k : MyHashMap.this) {
                ret[i] = k;
                i++;
            }
            return ret;
        }

        @Override
        public <T> T[] toArray(T[] arg0) {
            if (arg0.length < size) arg0 = (T[]) new Object[size];
            int i = 0;
            for (Object k : MyHashMap.this) {
                arg0[i] = (T) k;
                i++;
            }
            for (;i < arg0.length; i++) arg0[i] = null;
            return arg0;
        }
    }

    public V removeHelper(K key, V value, boolean matters) {
        Node node = getHelper(key);
        if (node != null && (!matters || node.value.equals(value))) {
            V ret = node.value;
            buckets[Math.floorMod(key.hashCode(), buckets.length)].remove(node);
            size--;
            return ret;
        }
        return null;
    }
    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    public V remove(K key) {
        return removeHelper(key, null, false);
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    public V remove(K key, V value) {
        return removeHelper(key, value, true);
    }
}
