package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }
    /**
     * Returns the maximum element in the deque as governed by the previously given Comparator. 
     * If the MaxArrayDeque is empty, simply return null.
     * @return the maxiumum element, null if empty.
     */
    public T max() {
        return max(comparator);
    }
    /**
     * Returns the maximum element in the deque as governed by the parameter Comparator c. 
     * If the MaxArrayDeque is empty, simply return null.
     * @param c the comparator used to determine the maxiumum element.
     * @return
     */
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T m = get(0);
        for (T item : this) {
            if (c.compare(item, m) > 0) {
                m = item;
            }
        }
        return m;
    }
}
