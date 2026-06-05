package bstmap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private class Node {
        private K key;
        private V value;
        private Node left;
        private Node right;
        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    private int size = 0;
    private Node root = null;
    /** Removes all of the mappings from this map. */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    private Node search(Node root, K key) {
        if (root == null) return null;
        else if (key.compareTo(root.key) > 0) return search(root.right, key);
        else if (key.compareTo(root.key) < 0) return search(root.left, key);
        else return root;
    }
    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        return search(root, key) != null;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        Node tmp = search(root, key);
        if (tmp == null) return null;
        return tmp.value;
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new Node(key, value);
            size++;
        }
        else put(root, key, value);
    }
    private void put(Node root, K key, V value) {
        if (key.compareTo(root.key) > 0) {
            if (root.right == null) {
                root.right = new Node(key, value);
                size++;
            }
            else put(root.right, key, value);
        }
        else if (key.compareTo(root.key) < 0) {
            if (root.left == null) {
                root.left = new Node(key, value);
                size++;
            }
            else put(root.left, key, value);
        }
        else root.value = value;
    }
    private class NodeIterator implements Iterator<Node> {
        private class StackNode {
            Node node;
            boolean processed;
            StackNode next;
            public StackNode(Node n, boolean p) {
                node = n;
                processed = p;
            }
        }
        private class Stack {
            StackNode sentinel = new StackNode(null, false);
            public Stack() {
                if (root != null) {
                    sentinel.next = new StackNode(root, false);
                }
            }
            private void push(Node n, boolean p) {
                StackNode tmp = new StackNode(n, p);
                tmp.next = sentinel.next;
                sentinel.next = tmp;
            }
            private boolean isEmpty() {
                return sentinel.next == null;
            }
            private StackNode pop() {
                if (isEmpty()) return null;
                StackNode ret = sentinel.next;
                sentinel.next = ret.next;
                return ret;
            }
        }
        Stack stack = new Stack();
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }
        @Override
        public Node next() {
            StackNode curr = stack.pop();
            if (!curr.processed) {
                if (curr.node.right != null) stack.push(curr.node.right, false);
                return curr.node;
            } else {
                stack.push(curr.node, true);
                if (curr.node.left != null) stack.push(curr.node.left, false);
                return next();
            }
        }
    }
    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    private class KeyIterator implements Iterator<K> {
        private NodeIterator nodeIterator = new NodeIterator();
        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }
        @Override
        public K next() {
            return nodeIterator.next().key;
        }
    }

    private class KeySet implements Set<K> {
        @Override
        public boolean add(K arg0) {
            throw new UnsupportedOperationException("Unimplemented method 'addAll'");
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
            for (Object k : c) if (!containsKey((K) k)) {
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
            return new KeyIterator();
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
            for (K k : this) {
                ret[i] = k;
                i++;
            }
            return ret;
        }

        @Override
        public <T> T[] toArray(T[] arg0) {
            int i = 0;
            T[] ret;
            if (arg0.length >= size) ret = arg0;
            else ret = (T[]) new Object[size];
            for (K k : this) {
                ret[i] = (T) k;
                i++;
            }
            for (; i < ret.length; i++) {
                ret[i] = null;
            }
            return ret;
        }
    }
    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    Node removeLeftest(Node current, Node parent, boolean isRight) {
        if (current == null || parent == null) return null;
        if (current.left != null) return removeLeftest(current.left, current, false);
        removeHelper(current.key, null, current, parent, isRight, false);
        return current;
    }
    private V removeHelper(K key, V value, Node current, Node parent, boolean isRight, boolean valueMatters) {
        if (current == null) return null;
        else if (current.key.equals(key) && (!valueMatters || valueMatters && current.value.equals(value))) {
            if (current.left == null) {
                if (parent == null) root = current.right;
                else if (isRight) parent.right = current.right;
                else parent.left = current.right;
            } else if (current.right == null) {
                if (parent == null) root = current.left;
                else if (isRight) parent.right = current.left;
                else parent.left = current.left;
            } else {
                Node tmp = removeLeftest(current.right, current, true);
                current.key = tmp.key;
                current.value = tmp.value;
            }
            size--;
            return current.value;
        }
        else if (key.compareTo(current.key) < 0) return removeHelper(key, value, current.left, current, false, valueMatters);
        else return removeHelper(key, value, current.right, current, true, valueMatters);
    }
    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        return removeHelper(key, null, root, null, false, false);
    }
    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        return removeHelper(key, value, root, null, false, true);
    }

    public void printInOrder() {
        NodeIterator nodeIterator = new NodeIterator();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            System.out.print(node.key);
            System.out.print(' ');
            System.out.println(node.value);
        }
    }
}
