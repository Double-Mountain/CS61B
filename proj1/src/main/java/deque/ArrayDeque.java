package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
   public static final int MIN_LENGTH = 8;
   private int size = 0;
   private T[] items;
   private int first = 0;
   private class dequeIterator implements Iterator<T> {
      int count = 0;
      @Override
      public boolean hasNext() {
         return count < size;
      }
      @Override
      public T next() {
         T _item = items[(first + count) % items.length];
         count++;
         return _item;
      }
   }

   public ArrayDeque() {
      items = (T[]) new Object[MIN_LENGTH];
   }

   private void resize(int _size) {
      T[] newItems = (T[]) new Object[_size];
      for (int i = 0; i < size; i++) {
         newItems[i] = items[(first + i) % items.length];
      }
      first = 0;
      items = newItems;
   }

   /** 
    * Adds an item of type T to the front of the deque. You can assume that item is never null.
    * @param item the first item.
    */
   @Override
   public void addFirst(T item) {
      if (size == items.length) {
         resize(size * 2);
      }
      first = (first + items.length - 1) % items.length;
      items[first] = item;
      size++;
   }

   /**
    * Adds an item of type T to the back of the deque. You can assume that item is never null.
    * @param item the last item.
    */
   @Override
   public void addLast(T item) {
      if (size == items.length) {
         resize(size * 2);
      }
      items[(first + size) % items.length] = item;
      size++;
   }

   /**
    * Returns the number of items in the deque.
    * @return the size of the deque.
    */
   @Override
   public int size() {
      return size;
   }

   /**
    * Prints the items in the deque from first to last, separated by a space. 
    * Once all the items have been printed, print out a new line.
    */
   @Override
   public void printDeque() {
      for (int i = 0; i < size; i++) {
         System.out.print(items[(first + i) % items.length]);
         if (i != size - 1) {
            System.out.print(" ");
         }
      }
      System.out.println("");
   }

   /**
    * Removes and returns the item at the front of the deque. If no such item exists, returns null.
    * @return the first item, null if it doesn't exist.
    */
   @Override
   public T removeFirst() {
      if (isEmpty()) {
         return null;
      }
      T _item = items[first];
      items[first] = null;
      first = (first + 1) % items.length;
      size--;
      if (size < 0.25 * items.length && items.length > MIN_LENGTH) {
         resize(items.length / 2);
      }
      return _item;
   }

   /**
    * Removes and returns the item at the back of the deque. If no such item exists, returns null.
    * @return the last item, null if it doesn't exist.
    */
   @Override
   public T removeLast() {
      if (isEmpty()) {
         return null;
      }
      T _item = items[(first + size + items.length- 1) % items.length];
      items[(first + size + items.length - 1) % items.length] = null;
      size--; 
      if (size < 0.25 * items.length && items.length > MIN_LENGTH) {
         resize(items.length / 2);
      }
      return _item;
   }

   /**
    * Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth. 
    * If no such item exists, returns null. Must not alter the deque!
    * @param index the index of the item.
    * @return the item at the given index, null if it doesn't exist;
    */
   @Override
   public T get(int index) {
      return items[(first + index) % items.length];
   }

   @Override
   public Iterator<T> iterator() {
      return new dequeIterator();
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof Deque)) {
         return false;
      }
      Deque<T> a = (Deque<T>) o;
      if (size != a.size()) {
         return false;
      }
      int current = 0;
      for (T i : (Iterable<T>) a) {
         if (i != get(current)) {
            return false;
         }
         current++;
      }
      return true;
   }
}
