package gms.utils;

import gms.core.*;
import gms.dsa.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Benchmarks core operations using analytical memory model instead of JVM heap
 * tracking.
 * CSV Format:
 * Operation,InputSize,TimeNanoseconds,MemoryKB
 */
public class Benchmark {
  private static final int START = 500;
  private static final int END = 10000;
  private static final int STEP = 1000;
  private static final int REPEAT = 1000;

  public static void runAll() {
    System.out.println("Benchmarking...");

    try (FileWriter out = new FileWriter("benchmarks.csv")) {
      out.write("Operation,InputSize,TimeNanoseconds,MemoryKB\n");

      System.out.println("Benchmarking PushMark...");
      benchmarkPushMark(out);
      System.out.println("Benchmarking RollbackMark...");
      benchmarkRollbackMark(out);
      System.out.println("Benchmarking LatestMark...");
      benchmarkLatest(out);
      System.out.println("Benchmarking HashMap Put...");
      benchmarkHashMapPut(out);
      System.out.println("Benchmarking HashMap Get...");
      benchmarkHashMapGet(out);
      System.out.println("Benchmarking HashMap Remove...");
      benchmarkHashMapRemove(out);
      System.out.println("Benchmarking HistoryDisplay...");
      benchmarkHistoryDisplay(out);

      System.out.println("Benchmarking MergeSort...");
      for (int n = START; n <= END; n += STEP) {
        Institute inst = new Institute();
        populateStudentsForSorting(inst, n);
        benchmarkMergeSort(out, inst, n);
      }

      System.out.println("Benchmarks complete -> benchmarks.csv");
    } catch (IOException e) {
      System.err.println("Error writing benchmarks.csv: " + e.getMessage());
    }
  }

  // Helper to populate data
  private static void populateStudentsForSorting(Institute inst, int n) {
    for (int i = 0; i < n; i++) {
      String id = "STU" + i;
      Student s = new Student(id, "Student" + i);
      inst.addStudent(s);

      s.addSubject(1, "Math");
      s.addSubject(1, "Physics");
      s.addSubject(1, "Chemistry");

      s.pushMark(1, "Math", Exam.CAT1, 80 + (i % 20));
      s.pushMark(1, "Physics", Exam.CAT1, 75 + (i % 25));
      s.pushMark(1, "Chemistry", Exam.CAT1, 70 + (i % 15));

      s.pushMark(1, "Math", Exam.CAT2, 78 + (i % 18));
      s.pushMark(1, "Physics", Exam.CAT2, 77 + (i % 22));
      s.pushMark(1, "Chemistry", Exam.CAT2, 73 + (i % 12));

      s.pushMark(1, "Math", Exam.FAT, 90 + (i % 10));
      s.pushMark(1, "Physics", Exam.FAT, 88 + (i % 10));
      s.pushMark(1, "Chemistry", Exam.FAT, 85 + (i % 10));

      s.calculateSGPA(1);
    }
  }

  // ========== 1. PushMark (O(1)) ==========
  private static void benchmarkPushMark(FileWriter out) throws IOException {
    for (int h = START; h <= END; h += STEP) {
      Student s = new Student("S", "Bench");
      s.addSubject(1, "Math");

      // Fill with baseline history size h
      for (int i = 0; i < h; i++) {
        s.pushMark(1, "Math", Exam.CAT1, 75);
      }

      // Now measure memory only for new 1000 insertions, not total
      long memBefore = usedMemory(false);
      s.pushMark(1, "Math", Exam.CAT1, 98);
      long memAfter = usedMemory(false);
      long memPerPush = (memAfter - memBefore);

      // Time measurement (same as you wrote)
      long total = 0;
      for (int i = 0; i < REPEAT; i++) {
        long t0 = System.nanoTime();
        s.pushMark(1, "Math", Exam.CAT1, 98);
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avgTime = total / REPEAT;

      out.write("PushMark," + h + "," + avgTime + "," + memPerPush + "\n");
    }
  }

  // ========== 2. RollbackMark (O(1)) ==========
  private static void benchmarkRollbackMark(FileWriter out) throws IOException {
    for (int h = START; h <= END; h += STEP) {
      Student s = new Student("S", "Bench");
      s.addSubject(1, "Math");
      for (int i = 0; i < h; i++)
        s.pushMark(1, "Math", Exam.CAT1, 80);
      for (int i = 0; i < REPEAT; i++)
        s.pushMark(1, "Math", Exam.CAT1, 82);

      long total = 0;
      long memBefore = usedMemory(true);
      for (int i = 0; i < REPEAT; i++) {
        long t0 = System.nanoTime();
        s.rollbackMark(1, "Math", Exam.CAT1);
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avg = total / REPEAT;

      long memAfter = usedMemory(false);
      long memKB = memAfter - memBefore;
      out.write("RollbackMark," + h + "," + avg + "," + memKB + "\n");
    }
  }

  // ========== 3. LatestMark (O(1)) ==========
  private static void benchmarkLatest(FileWriter out) throws IOException {
    for (int h = START; h <= END; h += STEP) {
      Student s = new Student("S", "Bench");
      s.addSubject(1, "Math");
      for (int i = 0; i < h; i++)
        s.pushMark(1, "Math", Exam.CAT1, 70 + (i % 20));

      long total = 0;
      long memBefore = usedMemory(true);
      for (int i = 0; i < REPEAT; i++) {
        long t0 = System.nanoTime();
        s.latest(1, "Math", Exam.CAT1);
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avg = total / REPEAT;

      long memAfter = usedMemory(false);
      long memKB = memAfter - memBefore; // Linked List Merge doesn't allocate anything else
      out.write("LatestMark," + h + "," + avg + "," + memKB + "\n");
    }
  }

  // ========== 4. History traversal (O(n)) ==========
  private static void benchmarkHistoryDisplay(FileWriter out) throws IOException {
    for (int h = START; h <= END; h += STEP) {
      Stack<Double> stack = new Stack<>();
      for (int i = 0; i < h; i++)
        stack.push(80.0 + (i % 20));

      long total = 0;
      long memBefore = usedMemory(true);

      for (int r = 0; r < 1; r++) {
        long t0 = System.nanoTime();
        stack.emptyTraversal(); // Simulate printing to output stream
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avg = total / 1;

      long memAfter = usedMemory(false);
      System.gc();
      long memKB = memAfter - memBefore;
      out.write("HistoryDisplay," + h + "," + avg + "," + memKB + "\n");
    }
  }

  // ========== 5. MergeSort (O(n log n)) ==========
  private static void benchmarkMergeSort(FileWriter out, Institute inst, int n) throws IOException {
    DoublyLinkedList<Student> list = inst.getAllStudents();
    MergeSort.sort(list, (a, b) -> Double.compare(b.getCGPA(), a.getCGPA())); // warm-up

    DoublyLinkedList<DoublyLinkedList<Student>> copies = new DoublyLinkedList<>();
    for (int r = 0; r < REPEAT; r++) {
      copies.add(new DoublyLinkedList<>(list)); // pre-allocate outside memory measurement
    }

    long total = 0;
    for (DoublyLinkedList<Student> copy : copies) {
      long t0 = System.nanoTime();
      MergeSort.sort(copy, (a, b) -> Double.compare(b.getCGPA(), a.getCGPA()));
      long t1 = System.nanoTime();
      total += (t1 - t0);
    }

    long totalMem = 0;
    for (DoublyLinkedList<Student> copy : copies) {
      long before = usedMemory(true);
      MergeSort.sort(copy, (a, b) -> Double.compare(b.getCGPA(), a.getCGPA()));
      long after = usedMemory(false);
      totalMem += (after - before);
    }

    System.gc();
    long avg = total / copies.size();
    long memKB = totalMem / copies.size();

    out.write("MergeSort," + n + "," + avg + "," + memKB + "\n");
  }

  // ========== HashMap PUT (O(1)) ==========
  private static void benchmarkHashMapPut(FileWriter out) throws IOException {
    for (int n = START; n <= END; n += STEP) {

      // Create several independent HashMaps (avoids accumulated size)
      DoublyLinkedList<HashMap<String, Integer>> copies = new DoublyLinkedList<>();
      for (int r = 0; r < REPEAT / 10; r++) {
        // Pre-allocate larger bucket array to minimize rehash
        copies.add(new HashMap<String, Integer>(n * 2));
      }

      long total = 0;
      for (HashMap<String, Integer> map : copies) {
        System.gc();
        long t0 = System.nanoTime();
        map.put("K" + n, n); // insert one element
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }

      // Memory Measurement (per map)
      long totalMem = 0;
      for (HashMap<String, Integer> map : copies) {
        long before = usedMemory(true);
        map.put("K_mem" + n, n);
        long after = usedMemory(false);
        totalMem += (after - before);
      }

      long avgTime = total / (REPEAT / 10);
      long avgMem = totalMem / copies.size();
      System.gc();

      out.write("HashMapPut," + n + "," + avgTime + "," + avgMem + "\n");
    }
  }

  // ========== 6. HashMap GET (O(1)) ==========
  private static void benchmarkHashMapGet(FileWriter out) throws IOException {
    for (int n = START; n <= END; n += STEP) {
      HashMap<String, Integer> map = new HashMap<>();
      for (int i = 0; i < n; i++)
        map.put("K" + i, i);

      long total = 0;
      long memBefore = usedMemory(true);

      for (int r = 0; r < REPEAT; r++) {
        long t0 = System.nanoTime();
        map.get("K" + (n / 2));
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }

      long memAfter = usedMemory(false);
      long avg = total / REPEAT;
      long memKB = memAfter - memBefore;

      out.write("HashMapGet," + n + "," + avg + "," + memKB + "\n");
    }
  }

  // ========== 7. HashMap REMOVE (O(1)) ==========
  private static void benchmarkHashMapRemove(FileWriter out) throws IOException {
    for (int n = START; n <= END; n += STEP) {

      DoublyLinkedList<HashMap<String, Integer>> copies = new DoublyLinkedList<>();
      for (int r = 0; r < REPEAT / 10; r++) {
        copies.add(new HashMap<String, Integer>(n * 2));
      }

      long total = 0;
      for (HashMap<String, Integer> map : copies) {
        for (int i = 0; i < n; i++)
          map.put("K" + i, i);

        System.gc();
        long t0 = System.nanoTime();
        map.remove("K" + (n / 2));
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }

      long totalMem = 0;
      for (HashMap<String, Integer> map : copies) {
        for (int i = 0; i < n; i++)
          map.put("K" + i, i);

        long before = usedMemory(true);
        map.remove("K" + (n / 2));
        long after = usedMemory(false);
        totalMem += (after - before);
      }

      long avg = total / REPEAT / 10;
      long memKB = totalMem / copies.size();
      System.gc();

      out.write("HashMapRemove," + n + "," + avg + "," + memKB + "\n");
    }
  }

  private static long usedMemory(boolean runGC) {
    Runtime r = Runtime.getRuntime();
    if (runGC)
      System.gc();

    return r.totalMemory() - r.freeMemory();
  }
}
