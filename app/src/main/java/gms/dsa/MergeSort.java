package gms.dsa;

import java.util.Comparator;

public class MergeSort {

  // public static <T> void sort(List<T> list, Comparator<T> comp) {
  // if (list == null || list.size() <= 1)
  // return;
  // mergeSort(list, 0, list.size() - 1, comp);
  // }
  //
  // private static <T> void mergeSort(List<T> list, int left, int right,
  // Comparator<T> comp) {
  // if (left >= right)
  // return;
  // int mid = left + (right - left) / 2;
  // mergeSort(list, left, mid, comp);
  // mergeSort(list, mid + 1, right, comp);
  // merge(list, left, mid, right, comp);
  // }
  //
  // @SuppressWarnings("unchecked")
  // private static <T> void merge(List<T> list, int left, int mid, int right,
  // Comparator<T> comp) {
  // int n1 = mid - left + 1;
  // int n2 = right - mid;
  //
  // Object[] leftArr = new Object[n1];
  // Object[] rightArr = new Object[n2];
  //
  // for (int i = 0; i < n1; i++)
  // leftArr[i] = list.get(left + i);
  // for (int i = 0; i < n2; i++)
  // rightArr[i] = list.get(mid + 1 + i);
  //
  // int i = 0, j = 0, k = left;
  // while (i < n1 && j < n2) {
  // T a = (T) leftArr[i];
  // T b = (T) rightArr[j];
  // if (comp.compare(a, b) <= 0) {
  // list.set(k++, a);
  // i++;
  // } else {
  // list.set(k++, b);
  // j++;
  // }
  // }
  // while (i < n1)
  // list.set(k++, (T) leftArr[i++]);
  // while (j < n2)
  // list.set(k++, (T) rightArr[j++]);
  // }
  //
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
