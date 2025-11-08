package gms.dsa;

import java.util.Comparator;

public class MergeSort {

  private static <T> DoublyLinkedList.Node<T> mergeNodes(DoublyLinkedList.Node<T> a, DoublyLinkedList.Node<T> b,
      Comparator<T> comp) {
    DoublyLinkedList.Node<T> head = null;
    DoublyLinkedList.Node<T> tail = null;

    while (a != null && b != null) {
      DoublyLinkedList.Node<T> next = (comp.compare(a.data, b.data) <= 0) ? a : b;
      if (next == a)
        a = a.next;
      else
        b = b.next;

      if (head == null) {
        head = tail = next;
        next.prev = null;
      } else {
        tail.next = next;
        next.prev = tail;
        tail = next;
      }
    }

    DoublyLinkedList.Node<T> remaining = (a != null) ? a : b;
    if (tail != null) {
      tail.next = remaining;
      if (remaining != null)
        remaining.prev = tail;
    } else {
      head = remaining;
    }

    return head;
  }

  private static <T> DoublyLinkedList.Node<T> split(DoublyLinkedList.Node<T> head, int halfLen) {
    if (head == null)
      return null;

    DoublyLinkedList.Node<T> current = head;
    for (int i = 1; i < halfLen && current.next != null; i++) {
      current = current.next;
    }

    if (current.next == null)
      return null;

    DoublyLinkedList.Node<T> secondHead = current.next;
    current.next = null;
    secondHead.prev = null;

    return secondHead;
  }

  public static <T> void sort(DoublyLinkedList<T> list, Comparator<T> comp) {
    if (list == null || list.head == null || list.head.next == null) {
      return;
    }

    int n = list.size();

    for (int subListSize = 1; subListSize < n; subListSize *= 2) {

      DoublyLinkedList.Node<T> newHead = null;
      DoublyLinkedList.Node<T> tail = null;
      DoublyLinkedList.Node<T> current = list.head;

      while (current != null) {

        DoublyLinkedList.Node<T> left = current;
        DoublyLinkedList.Node<T> right = split(left, subListSize);

        DoublyLinkedList.Node<T> nextStart = split(right, subListSize);

        DoublyLinkedList.Node<T> merged = mergeNodes(left, right, comp);

        if (newHead == null) {
          newHead = merged;
        } else {
          tail.next = merged;
          merged.prev = tail;
        }

        DoublyLinkedList.Node<T> temp = merged;
        while (temp != null && temp.next != null) {
          temp = temp.next;
        }
        tail = temp;

        current = nextStart;
      }

      list.head = newHead;
    }

    list.rebuildTailAndSize();
  }
}
