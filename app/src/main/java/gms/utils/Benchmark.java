package gms.utils;

import gms.core.*;
import gms.dsa.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Benchmarks core operations using analytical memory model instead of JVM heap
 * tracking.
 * CSV Format:
 * Operation,InputSize,TimeNanoseconds,MemoryKB
 */
public class Benchmark {
  private static final int START = 500;
  private static final int END = 10000;
  private static final int STEP = 500;
  private static final int REPEAT = 1000;

  // Analytical memory constants
  private static final int STACK_NODE_BYTES = 32; // Approx for one Stack.Node<Double>
  private static final int POINTER_BYTES = 8; // 64-bit reference

  public static void runAll() {
    System.out.println("Benchmarking (Analytical Memory Model)...");

    try (FileWriter out = new FileWriter("benchmarks.csv")) {
      out.write("Operation,InputSize,TimeNanoseconds,MemoryKB\n");

      benchmarkPushMark(out);
      benchmarkRollbackMark(out);
      benchmarkLatest(out);
      benchmarkHistoryDisplay(out);

      for (int n = START; n <= END; n += STEP) {
        Institute inst = new Institute();
        populateStudentsForSorting(inst, n);
        benchmarkMergeSort(out, inst, n);
      }

      System.out.println("✅ Benchmarks complete → benchmarks.csv");
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
      for (int i = 0; i < h; i++)
        s.pushMark(1, "Math", Exam.CAT1, 75);

      s.pushMark(1, "Math", Exam.CAT1, 95); // Warm-up

      long total = 0;
      for (int i = 0; i < REPEAT; i++) {
        long t0 = System.nanoTime();
        s.pushMark(1, "Math", Exam.CAT1, 98);
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avgTime = total / REPEAT;
      long memKB = STACK_NODE_BYTES / 1024; // Analytical

      out.write("PushMark," + h + "," + avgTime + "," + memKB + "\n");
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
      for (int i = 0; i < REPEAT; i++) {
        long t0 = System.nanoTime();
        s.rollbackMark(1, "Math", Exam.CAT1);
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avg = total / REPEAT;

      out.write("RollbackMark," + h + "," + avg + ",0\n");
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
      for (int i = 0; i < REPEAT; i++) {
        long t0 = System.nanoTime();
        s.latest(1, "Math", Exam.CAT1);
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avg = total / REPEAT;
      out.write("LatestMark," + h + "," + avg + ",0\n");
    }
  }

  // ========== 4. History traversal (O(n)) ==========
  private static void benchmarkHistoryDisplay(FileWriter out) throws IOException {
    for (int h = START; h <= END; h += STEP) {
      Stack<Double> stack = new Stack<>();
      for (int i = 0; i < h; i++)
        stack.push(80.0 + (i % 20));

      long total = 0;
      for (int r = 0; r < REPEAT; r++) {
        long t0 = System.nanoTime();
        stack.toList();
        long t1 = System.nanoTime();
        total += (t1 - t0);
      }
      long avg = total / REPEAT;

      long memKB = 0; // no extra allocation
      out.write("HistoryDisplay," + h + "," + avg + "," + memKB + "\n");
    }
  }

  // ========== 5. MergeSort (O(n log n)) ==========
  private static void benchmarkMergeSort(FileWriter out, Institute inst, int n) throws IOException {
    List<Student> list = inst.getAllStudents();
    MergeSort.sort(list, (a, b) -> Double.compare(b.getCGPA(), a.getCGPA())); // warm-up

    long total = 0;
    for (int r = 0; r < REPEAT; r++) {
      List<Student> copy = new ArrayList<>(list);
      long t0 = System.nanoTime();
      MergeSort.sort(copy, (a, b) -> Double.compare(b.getCGPA(), a.getCGPA()));
      long t1 = System.nanoTime();
      total += (t1 - t0);
    }
    long avg = total / REPEAT;
    long memKB = (long) ((n * POINTER_BYTES) / 1024); // merge buffer size

    out.write("MergeSort," + n + "," + avg + "," + memKB + "\n");
  }
}
