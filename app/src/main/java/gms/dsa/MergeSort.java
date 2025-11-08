package gms.dsa;

import java.util.Comparator;

public class MergeSort {

  // For DoublyLinkedList
  public static <T> void sort(DoublyLinkedList<T> list, Comparator<T> comp) {
    if (list == null || list.size() <= 1)
      return;
    list.head = mergeSort(list.head, comp);
    list.rebuildTailAndSize(); // update tail & size manually
  }

  private static <T> DoublyLinkedList.Node<T> mergeSort(DoublyLinkedList.Node<T> head, Comparator<T> comp) {
    if (head == null || head.next == null)
      return head;

    DoublyLinkedList.Node<T> mid = splitMiddle(head);
    DoublyLinkedList.Node<T> left = mergeSort(head, comp);
    DoublyLinkedList.Node<T> right = mergeSort(mid, comp);

    return mergeNodes(left, right, comp);
  }

  private static <T> DoublyLinkedList.Node<T> splitMiddle(DoublyLinkedList.Node<T> head) {
    DoublyLinkedList.Node<T> slow = head, fast = head;
    while (fast.next != null && fast.next.next != null) {
      slow = slow.next;
      fast = fast.next.next;
    }
    DoublyLinkedList.Node<T> mid = slow.next;
    slow.next = null;
    if (mid != null)
      mid.prev = null;
    return mid;
  }

  private static <T> DoublyLinkedList.Node<T> mergeNodes(
      DoublyLinkedList.Node<T> a,
      DoublyLinkedList.Node<T> b,
      Comparator<T> comp) {
    if (a == null)
      return b;
    if (b == null)
      return a;

    if (comp.compare(a.data, b.data) <= 0) {
      a.next = mergeNodes(a.next, b, comp);
      if (a.next != null)
        a.next.prev = a;
      a.prev = null;
      return a;
    } else {
      b.next = mergeNodes(a, b.next, comp);
      if (b.next != null)
        b.next.prev = b;
      b.prev = null;
      return b;
    }
  }
}
