package gms.dsa;

import java.util.Comparator;
import java.util.List;

public class MergeSort {

  public static <T> void sort(List<T> list, Comparator<T> comp) {
    if (list == null || list.size() <= 1)
      return;
    mergeSort(list, 0, list.size() - 1, comp);
  }

  private static <T> void mergeSort(List<T> list, int left, int right, Comparator<T> comp) {
    if (left >= right)
      return;
    int mid = left + (right - left) / 2;
    mergeSort(list, left, mid, comp);
    mergeSort(list, mid + 1, right, comp);
    merge(list, left, mid, right, comp);
  }

  @SuppressWarnings("unchecked")
  private static <T> void merge(List<T> list, int left, int mid, int right, Comparator<T> comp) {
    int n1 = mid - left + 1;
    int n2 = right - mid;

    Object[] leftArr = new Object[n1];
    Object[] rightArr = new Object[n2];

    for (int i = 0; i < n1; i++)
      leftArr[i] = list.get(left + i);
    for (int i = 0; i < n2; i++)
      rightArr[i] = list.get(mid + 1 + i);

    int i = 0, j = 0, k = left;
    while (i < n1 && j < n2) {
      T a = (T) leftArr[i];
      T b = (T) rightArr[j];
      if (comp.compare(a, b) <= 0) {
        list.set(k++, a);
        i++;
      } else {
        list.set(k++, b);
        j++;
      }
    }
    while (i < n1)
      list.set(k++, (T) leftArr[i++]);
    while (j < n2)
      list.set(k++, (T) rightArr[j++]);
  }
}
