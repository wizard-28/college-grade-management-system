package gms.dsa;

import java.util.List;

public class Stack<T> {
  private static final class Node<U> {
    U data;
    Node<U> next;

    Node(U d) {
      data = d;
    }
  }

  private Node<T> top;
  private int size;

  public boolean isEmpty() {
    return top == null;
  }

  public int size() {
    return size;
  }

  public void push(T v) {
    Node<T> n = new Node<>(v);
    n.next = top;
    top = n;
    size++;
  }

  public boolean pop() {
    if (isEmpty())
      return false;
    top = top.next;
    size--;
    return true;
  }

  public T peek() {
    if (isEmpty())
      throw new IllegalStateException("Stack underflow");
    return top.data;
  }

  // top -> bottom
  public List<T> toList() {
    List<T> out = new DoublyLinkedList<>();
    for (Node<T> cur = top; cur != null; cur = cur.next)
      out.add(cur.data);
    return out;
  }

}
