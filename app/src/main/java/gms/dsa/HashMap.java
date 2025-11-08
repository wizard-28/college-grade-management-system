package gms.dsa;

import java.util.*;
import java.util.function.BiConsumer;

public class HashMap<K, V> implements Map<K, V> {
  private static final class Node<K, V> implements Map.Entry<K, V> {
    K key;
    V val;
    Node<K, V> next;

    Node(K k, V v) {
      key = k;
      val = v;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return val;
    }

    @Override
    public V setValue(V v) {
      V old = val;
      val = v;
      return old;
    }
  }

  private List<Node<K, V>> buckets;
  private int size;
  private float maxLoad = 0.75f;

  public HashMap() {
    buckets = new ArrayList<>();
  }

  private int idxFor(Object key, int mod) {
    return (key == null ? 0 : (key.hashCode() & 0x7fffffff)) % mod;
  }

  private void initIfEmpty() {
    if (buckets.isEmpty()) {
      for (int i = 0; i < 16; i++)
        buckets.add(null);
    }
  }

  private void rehashIfNeeded() {
    initIfEmpty();
    float lf = (float) size / (float) buckets.size();
    if (lf <= maxLoad)
      return;

    int newCap = buckets.size() * 2;
    List<Node<K, V>> nb = new ArrayList<>(newCap);
    for (int i = 0; i < newCap; i++)
      nb.add(null);

    for (Node<K, V> head : buckets) {
      for (Node<K, V> cur = head; cur != null;) {
        Node<K, V> nxt = cur.next;
        int idx = idxFor(cur.key, newCap);
        cur.next = nb.get(idx);
        nb.set(idx, cur);
        cur = nxt;
      }
    }
    buckets = nb;
  }

  private Node<K, V> findNode(Object key) {
    if (buckets.isEmpty())
      return null;
    int i = idxFor(key, buckets.size());
    for (Node<K, V> cur = buckets.get(i); cur != null; cur = cur.next) {
      if ((key == null && cur.key == null) || (key != null && key.equals(cur.key)))
        return cur;
    }
    return null;
  }

  // === Map Interface Implementations ===

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return findNode(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    for (Node<K, V> head : buckets) {
      for (Node<K, V> cur = head; cur != null; cur = cur.next) {
        if ((value == null && cur.val == null) ||
            (value != null && value.equals(cur.val)))
          return true;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    Node<K, V> n = findNode(key);
    return (n == null) ? null : n.val;
  }

  @Override
  public V put(K key, V value) {
    Node<K, V> n = findNode(key);
    if (n != null) {
      return n.setValue(value); // return old value!
    }
    rehashIfNeeded();
    int i = idxFor(key, buckets.size());
    Node<K, V> head = buckets.get(i);
    Node<K, V> ins = new Node<>(key, value);
    ins.next = head;
    buckets.set(i, ins);
    size++;
    return null; // no previous value
  }

  @Override
  public V remove(Object key) {
    if (buckets.isEmpty())
      return null;
    int i = idxFor(key, buckets.size());
    Node<K, V> cur = buckets.get(i), prev = null;
    while (cur != null) {
      if ((key == null && cur.key == null) || (key != null && key.equals(cur.key))) {
        if (prev == null)
          buckets.set(i, cur.next);
        else
          prev.next = cur.next;
        size--;
        return cur.val; // return removed value
      }
      prev = cur;
      cur = cur.next;
    }
    return null;
  }

  @Override
  public void clear() {
    buckets = new ArrayList<>();
    size = 0;
  }

  @Override
  public Set<K> keySet() {
    Set<K> ks = new HashSet<>();
    forEach((k, v) -> ks.add(k));
    return ks;
  }

  @Override
  public Collection<V> values() {
    List<V> vs = new ArrayList<>();
    forEach((k, v) -> vs.add(v));
    return vs;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    Set<Entry<K, V>> es = new HashSet<>();
    forEach((k, v) -> es.add(new AbstractMap.SimpleEntry<>(k, v)));
    return es;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    if (m == null)
      return; // or throw NullPointerException like java.util.HashMap
    for (Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Objects.requireNonNull(action);
    if (buckets.isEmpty())
      return;

    for (Node<K, V> head : buckets) {
      for (Node<K, V> cur = head; cur != null; cur = cur.next) {
        action.accept(cur.key, cur.val);
      }
    }
  }
}
