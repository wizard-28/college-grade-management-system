package gms.dsa;

import java.util.*;

public class DoublyLinkedList<T> implements List<T> {

  public static class Node<T> {
    public T data;
    public Node<T> prev, next;

    public Node(T data) {
      this.data = data;
    }
  }

  public Node<T> head; // First node
  public Node<T> tail; // Last node
  private int size = 0;

  public DoublyLinkedList() {
  }

  public DoublyLinkedList(Collection<? extends T> c) {
    this(); // initialize empty list first
    for (T item : c) {
      this.add(item); // append each element
    }
  }

  // ===== Basic Helpers =====

  public void rebuildTailAndSize() {
    tail = head;
    size = 0;
    while (tail != null) {
      size++;
      if (tail.next == null)
        break;
      tail = tail.next;
    }
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
  }

  private Node<T> getNode(int index) {
    checkIndex(index);
    if (index < size / 2) {
      Node<T> cur = head;
      for (int i = 0; i < index; i++)
        cur = cur.next;
      return cur;
    } else {
      Node<T> cur = tail;
      for (int i = size - 1; i > index; i--)
        cur = cur.prev;
      return cur;
    }
  }

  // ====== List<T> Implementation ======

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean contains(Object o) {
    for (T val : this) {
      if (Objects.equals(val, o))
        return true;
    }
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      Node<T> cur = head;

      @Override
      public boolean hasNext() {
        return cur != null;
      }

      @Override
      public T next() {
        if (!hasNext())
          throw new NoSuchElementException();
        T val = cur.data;
        cur = cur.next;
        return val;
      }
    };
  }

  @Override
  public T get(int index) {
    return getNode(index).data;
  }

  @Override
  public T set(int index, T element) {
    Node<T> n = getNode(index);
    T old = n.data;
    n.data = element;
    return old;
  }

  @Override
  public boolean add(T element) {
    Node<T> n = new Node<>(element);
    if (size == 0) {
      head = tail = n;
    } else {
      tail.next = n;
      n.prev = tail;
      tail = n;
    }
    size++;
    return true;
  }

  @Override
  public void add(int index, T element) {
    if (index == size) {
      add(element);
      return;
    }
    checkIndex(index);
    Node<T> cur = getNode(index);
    Node<T> n = new Node<>(element);
    Node<T> prev = cur.prev;

    n.next = cur;
    cur.prev = n;

    if (prev == null)
      head = n;
    else {
      prev.next = n;
      n.prev = prev;
    }
    size++;
  }

  @Override
  public T remove(int index) {
    checkIndex(index);
    Node<T> cur = getNode(index);
    T val = cur.data;
    Node<T> prev = cur.prev;
    Node<T> next = cur.next;

    if (prev == null)
      head = next;
    else
      prev.next = next;

    if (next == null)
      tail = prev;
    else
      next.prev = prev;

    size--;
    return val;
  }

  @Override
  public boolean remove(Object o) {
    Node<T> cur = head;
    while (cur != null) {
      if (Objects.equals(cur.data, o)) {
        Node<T> prev = cur.prev;
        Node<T> next = cur.next;

        if (prev == null)
          head = next;
        else
          prev.next = next;
        if (next == null)
          tail = prev;
        else
          next.prev = prev;

        size--;
        return true;
      }
      cur = cur.next;
    }
    return false;
  }

  @Override
  public void clear() {
    head = tail = null;
    size = 0;
  }

  @Override
  public int indexOf(Object o) {
    int i = 0;
    for (T val : this) {
      if (Objects.equals(val, o))
        return i;
      i++;
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    int i = size - 1;
    Node<T> cur = tail;
    while (cur != null) {
      if (Objects.equals(cur.data, o))
        return i;
      cur = cur.prev;
      i--;
    }
    return -1;
  }

  // Not implemented as it's not used in the codebase
  @Override
  public ListIterator<T> listIterator() {
    throw new UnsupportedOperationException();
  }

  // Not implemented as it's not used in the codebase
  @Override
  public ListIterator<T> listIterator(int index) {
    throw new UnsupportedOperationException();
  }

  // Not implemented as it's not used in the codebase
  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    Object[] arr = new Object[size];
    int i = 0;
    for (T val : this)
      arr[i++] = val;
    return arr;
  }

  @Override
  public <E> E[] toArray(E[] a) {
    if (a.length < size)
      a = (E[]) new Object[size];
    int i = 0;
    for (T val : this)
      a[i++] = (E) val;
    return a;
  }

  // Not implemented as it's not used in the codebase
  @Override
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  // Not implemented as it's not used in the codebase
  @Override
  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  // Not implemented as it's not used in the codebase
  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  // Not implemented as it's not used in the codebase
  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  // Not implemented as it's not used in the codebase
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }
}
