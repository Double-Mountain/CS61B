package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
   private int size = 0;

   private class Node {
      private T item;
      private Node prev;
      private Node next;
      private Node(T _item) {
         item = _item;
      }
   }

   private class dequeIterator implements Iterator<T> {
      private Node nextNode = sentinel.next;
      @Override
      public boolean hasNext() {
         return nextNode == sentinel;
      }
      @Override
      public T next() {
         T _item = nextNode.item;
         nextNode = nextNode.next;
         return _item;
      }
   }

   private Node sentinel;

   public LinkedListDeque() {
     sentinel = new Node(null);
     sentinel.prev = sentinel.next = sentinel;
   }

   /** 
    * Adds an item of type T to the front of the deque. You can assume that item is never null.
    * @param item the first item.
    */
   @Override
   public void addFirst(T item) {
      Node tmp = new Node(item);
      tmp.prev = sentinel;
      tmp.next = sentinel.next;
      sentinel.next.prev = tmp;
      sentinel.next = tmp;
      size++;
   }

   /**
    * Adds an item of type T to the back of the deque. You can assume that item is never null.
    * @param item the last item.
    */
   @Override
   public void addLast(T item) {
      Node tmp = new Node(item);
      tmp.prev = sentinel.prev;
      tmp.next = sentinel;
      sentinel.prev.next = tmp;
      sentinel.prev = tmp;
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
      Node current = sentinel;
      while (true) {
         if (current.next != sentinel) {
            current = current.next;
            System.out.print(current.item);
            System.out.print(" ");
         } else {
            System.out.println("");
            break;
         }
      }
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
      Node tmp = sentinel.next;
      sentinel.next = tmp.next;
      tmp.next.prev = sentinel;
      size--;
      return tmp.item;
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
      Node tmp = sentinel.prev;
      sentinel.prev = tmp.prev;
      tmp.prev.next = sentinel;
      size--;
      return tmp.item;
   }

   /**
    * Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth. 
    * If no such item exists, returns null. Must not alter the deque!
    * @param index the index of the item.
    * @return the item at the given index, null if it doesn't exist;
    */
   @Override
   public T get(int index) {
      if (size > index + 1) {
         return null;
      }
      Node current = sentinel.next;
      for (int i = 0; i < index; i++) {
         current = current.next;
      }
      return current.item;
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
      Deque<T> d = (Deque<T>) o;
      if (size != d.size()) {
         return false;
      }
      Node current = sentinel.next;
      for (T i : (Iterable<T>) d) {
         if (i != current.item) {
            return false;
         }
         current = current.next;
      }
      return true;
   }

   /**
    * Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth. 
    * If no such item exists, returns null. Must not alter the deque!
    * @param index the index of the item.
    * @return the item at the given index, null if it doesn't exist;
    */
   public T getRecursive(int index) {
      if (size > index + 1) {
         return null;
      }
      return getRecursive(sentinel.next, index);
   }
   private T getRecursive(Node start, int index) {
      if (index == 0) {
         return start.item;
      }
      return getRecursive(start.next, index - 1);
   }
}
