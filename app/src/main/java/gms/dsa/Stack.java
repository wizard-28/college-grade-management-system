package gms.dsa;

import java.util.List;

public class Stack<T> {
  private final DoublyLinkedList<T> list = new DoublyLinkedList<>();

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public int size() {
    return list.size();
  }

  public void push(T v) {
    list.add(v);
  }

  public boolean pop() {
    if (isEmpty())
      return false;
    list.remove(list.size() - 1);
    return true;
  }

  public T peek() {
    if (isEmpty())
      throw new IllegalStateException("Stack underflow");
    return list.get(list.size() - 1);
  }

  // // top -> bottom
  public List<T> toList() {
    return list;
  }

  public void display() {
    if (isEmpty()) {
      System.out.print("(none)");
    }
    for (DoublyLinkedList.Node<T> cur = list.tail; cur != null; cur = cur.prev) {
      System.out.print(cur.data);
      if (cur.prev != null) {
        System.out.print(", ");
      }
    }
  }

  public void emptyTraversal() {
    if (isEmpty()) {
      System.out.print("(none)");
    }
    for (T t : list) {
    }
  }

}
