package gms.cli;

import gms.core.Exam;
import gms.core.Institute;
import gms.core.Student;
import gms.utils.Benchmark;
import gms.dsa.MergeSort;

import java.util.List;
import java.util.Scanner;

public class CLI {
  private final Institute inst;
  private final Scanner in = new Scanner(System.in);

  public CLI(Institute inst) {
    this.inst = inst;
  }

  public void run() {
    while (true) {
      System.out.println("\n╔══════════════════════════════════════════════════╗");
      System.out.println("║               Grade Management System            ║");
      System.out.println("╚══════════════════════════════════════════════════╝");
      System.out.println("Current Semester: " + inst.currentSemester());

      System.out.println("\n─────────────── STUDENT MANAGEMENT ─────────────────");
      if (inst.empty() || !inst.lastExam().isPresent()) {
        System.out.println(" 1. Add Student (Semester starts at 1)");
      }
      if (!inst.empty()) {
        if (inst.size() > 1)
          System.out.println(" 2. Remove Student");
        System.out.println(" 3. View All Students (Sorted by CGPA)");
        System.out.println(" 4. View Student Details");

        if (!inst.isCat1Done()) {
          System.out.println("\n──────────────── SUBJECT MANAGEMENT ────────────────");
          System.out.println(" 5. Add Subject to Student");
          System.out.println(" 6. Remove Subject from Student");
        }

        System.out.println("\n─────────────── EXAMINATION WORKFLOW ───────────────");
        if (!inst.isCat1Done()) {
          System.out.println(" 7. Enter CAT1 Marks (All Students)");
        } else if (inst.isCat1Done() && !inst.isCat2Done()) {
          System.out.println(" 7. Enter CAT2 Marks (All Students)");
          System.out.println(" 8. Update CAT1 Marks (Revaluation)");
        } else if (inst.isCat2Done() && !inst.isFatDone()) {
          System.out.println(" 7. Enter FAT Marks (All Students)");
          System.out.println(" 8. Update CAT2 Marks (Revaluation)");
        } else {
          System.out.println(" 7. Promote All Students to Next Semester");
          System.out.println(" 8. Update FAT Marks (Revaluation)");
        }

        if (inst.canShowRollback() && inst.lastExam().isPresent()) {
          Exam lastEx = inst.lastExam().get();
          System.out.println(" 9. Rollback Last " + lastEx + " Marks");
        } // Same logic as C++

        System.out.println("\n──────────────────── REPORTS ───────────────────────");
        System.out.println("10. Show Student Report");
        System.out.println("11. Show Subject Marks History");
      }

      System.out.println("\n──────────────────── TOOLS ─────────────────────────");
      System.out.println("12. Run Benchmarks");
      System.out.println("13. Save Data");
      System.out.println("14. Delete Data");
      System.out.println("15. Exit");
      System.out.println("────────────────────────────────────────────────────");

      int choice = readInt("Enter choice: ");
      switch (choice) {
        case 1:
          addStudent();
          break;
        case 2:
          removeStudent();
          break;
        case 3:
          viewAllSorted();
          break;
        case 4:
          viewStudent();
          break;
        case 5:
          addSubject();
          break;
        case 6:
          removeSubject();
          break;
        case 7:
          examFlow();
          break;
        case 8:
          revaluation();
          break;
        case 9:
          rollback();
          break;
        case 10:
          showReport();
          break;
        case 11:
          showHistory();
          break;
        case 12:
          System.out.println("Running benchmarks with synthetic data...");
          Benchmark.runAll();
          System.out.println("Generating plots with Python...");
          try {
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/plot_benchmarks.py");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
              System.out.println("Python script failed. Exit code: " + exitCode);
            }
          } catch (Exception e) {
            System.out.println("Could not run Python script: " + e.getMessage());
          }
          waitEnter();
          break;
        case 13:
          if (gms.io.PersistenceManager.saveCSV(inst, "data")) {
            System.out.println("Successfully saved data.");
          }
          waitEnter();
          break;
        case 14:
          if (gms.io.PersistenceManager.deleteCSV("data")) {
            System.out.println("Successfully deleted data");
          }
          waitEnter();
          break;
        case 15:
          System.out.println("Exiting...");
          return;
        default:
          System.out.println("Invalid choice.");
      }
    }
  }

  private void addStudent() {
    String name = readLine("Enter student's name: ");
    String id = readLine("Enter registration ID: ");
    inst.addStudent(new Student(id, name));
    System.out.println("Student added: " + id + " (" + name + ")");
    waitEnter();
  }

  private void removeStudent() {
    String id = readLine("Enter registration ID to remove: ");
    System.out.println(inst.removeStudent(id) ? "Removed." : "No such student.");
    waitEnter();
  }

  private void viewStudent() {
    String id = readLine("Enter registration ID: ");
    Student s = inst.getStudent(id);
    if (s == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    System.out.println("ID: " + s.id());
    System.out.println("Name: " + s.name());
    System.out.println("Semester (stored): " + s.semester());
    List<String> subs = s.listSubjects(s.semester());
    System.out.println("Subjects in Semester " + s.semester() + " (" + subs.size() + "):");
    for (String sub : subs)
      System.out.println("  - " + sub);
    waitEnter();
  }

  private void addSubject() {
    String id = readLine("Enter registration ID: ");
    Student s = inst.getStudent(id);
    if (s == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    String subject = readLine("Enter subject name to add: ");
    System.out.println(s.addSubject(s.semester(), subject) ? "Subject added." : "Subject already exists.");
    waitEnter();
  }

  private void removeSubject() {
    String id = readLine("Enter registration ID: ");
    Student s = inst.getStudent(id);
    if (s == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    String subject = readLine("Enter subject name to remove: ");
    System.out.println(s.removeSubject(s.semester(), subject) ? "Subject removed." : "Subject not found.");
    waitEnter();
  }

  private void examFlow() {
    if (!inst.isCat1Done()) {
      if (enterMarksAll(Exam.CAT1))
        inst.setCat1Done(true);
    } else if (!inst.isCat2Done()) {
      if (enterMarksAll(Exam.CAT2))
        inst.setCat2Done(true);
    } else if (!inst.isFatDone()) {
      if (enterMarksAll(Exam.FAT))
        inst.setFatDone(true);
    } else {
      System.out.println("Promoting all students to next semester...");
      inst.promoteAll();
      inst.nextSemester();
      waitEnter();
    }
  }

  private void revaluation() {
    if (inst.isCat1Done() && !inst.isCat2Done())
      updateMarksOne(Exam.CAT1);
    else if (inst.isCat2Done() && !inst.isFatDone())
      updateMarksOne(Exam.CAT2);
    else if (inst.isFatDone())
      updateMarksOne(Exam.FAT);
    else {
      System.out.println("Nothing to update yet.");
      waitEnter();
    }
  }

  private void rollback() {
    if (!inst.canShowRollback()) {
      System.out.println("No marks to rollback.");
      waitEnter();
      return;
    }
    String id = readLine("Enter registration ID: ");
    Student st = inst.getStudent(id);
    if (st == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    List<String> subs = st.listSubjects(st.semester());
    if (subs.isEmpty()) {
      System.out.println("No subjects.");
      waitEnter();
      return;
    }
    for (int i = 0; i < subs.size(); i++)
      System.out.println("  " + (i + 1) + ". " + subs.get(i));
    int idx = readInt("Choose subject (number): ", 1, subs.size());
    Exam last = inst.lastExam().get();
    boolean ok = st.rollbackMark(st.semester(), subs.get(idx - 1), last);
    System.out.println(ok ? "Rolled back." : "Cannot rollback — only one mark in history.");
    waitEnter();
  }

  private void viewAllSorted() {
    List<Student> all = inst.getAllStudents();
    MergeSort.sort(all, (a, b) -> Double.compare(b.getCGPA(), a.getCGPA()));
    System.out.println("=== Students Sorted by CGPA ===");
    for (Student s : all) {
      System.out.printf("ID: %s | Name: %s | CGPA: %.2f%n", s.id(), s.name(), s.getCGPA());
    }
    waitEnter();
  }

  private boolean enterMarksAll(Exam ex) {
    System.out.println("Entering " + ex.display() + " marks for ALL students");
    final int[] nStud = { 0 };
    final int[] nSub = { 0 };
    inst.forEachStudent(s -> {
      nStud[0]++;
      List<String> subs = s.listSubjects(s.semester());
      if (subs.isEmpty())
        return;
      System.out.println("\nStudent: " + s.id() + " - " + s.name());
      for (String sub : subs) {
        double mark = readDouble("  " + sub + " marks (0-100): ", 0, 100);
        s.pushMark(s.semester(), sub, ex, mark);
        nSub[0]++;
      }
    });
    if (nStud[0] == 0) {
      System.out.println("No students found.");
      waitEnter();
      return false;
    }
    if (nSub[0] == 0) {
      System.out.println("No subjects found.");
      waitEnter();
      return false;
    }
    System.out.println(
        "Recorded " + ex.display() + " marks for " + nSub[0] + " subject entries across " + nStud[0] + " students.");
    waitEnter();
    return true;
  }

  private void updateMarksOne(Exam ex) {
    String id = readLine("Enter registration ID: ");
    Student s = inst.getStudent(id);
    if (s == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    List<String> subs = s.listSubjects(s.semester());
    if (subs.isEmpty()) {
      System.out.println("No subjects.");
      waitEnter();
      return;
    }
    System.out.println("Subjects:");
    for (int i = 0; i < subs.size(); i++)
      System.out.println("  " + (i + 1) + ". " + subs.get(i));
    int idx = readInt("Choose subject (number): ", 1, subs.size());
    double mark = readDouble("Enter new " + ex.display() + " marks (0-100): ", 0, 100);
    s.pushMark(s.semester(), subs.get(idx - 1), ex, mark);
    inst.markUpdated(ex);
    System.out.println("Updated (revaluation).");
    waitEnter();
  }

  private void showReport() {
    String id = readLine("Enter registration ID: ");
    Student s = inst.getStudent(id);
    if (s == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    for (int sem = 1; sem <= s.semester(); sem++) {
      System.out.println("\n--- Grades for " + s.name() + " (" + s.id() + "), Semester " + sem + " ---");
      s.printGrades(sem);
      System.out.printf("SGPA: %.2f | CGPA: %.2f%n", s.getSGPA(sem), s.getCGPA());
    }
    waitEnter();
  }

  private void showHistory() {
    String id = readLine("Enter registration ID: ");
    Student s = inst.getStudent(id);
    if (s == null) {
      System.out.println("Student not found.");
      waitEnter();
      return;
    }
    List<String> subs = s.listSubjects(s.semester());
    if (subs.isEmpty()) {
      System.out.println("No subjects.");
      waitEnter();
      return;
    }
    for (int i = 0; i < subs.size(); i++)
      System.out.println("  " + (i + 1) + ". " + subs.get(i));
    int idx = readInt("Choose subject (number): ", 1, subs.size());
    String sub = subs.get(idx - 1);
    for (Exam ex : new Exam[] { Exam.CAT1, Exam.CAT2, Exam.FAT }) {
      List<Double> hist = s.history(s.semester(), sub, ex);
      System.out.print(ex.display() + " history (latest to earliest): ");
      if (hist.isEmpty())
        System.out.print("(none)");
      else {
        for (int i = 0; i < hist.size(); i++) {
          System.out.print(hist.get(i));
          if (i < hist.size() - 1) {
            System.out.print(", ");
          }
        }
      }
      System.out.println();
    }
    waitEnter();
  }

  // ---- io helpers ----
  private String readLine(String prompt) {
    System.out.print(prompt);
    return in.nextLine().trim();
  }

  private int readInt(String prompt) {
    while (true) {
      try {
        System.out.print(prompt);
        return Integer.parseInt(in.nextLine().trim());
      } catch (NumberFormatException e) {
        System.out.println("Invalid number.");
      }
    }
  }

  private int readInt(String prompt, int start, int end) {
    while (true) {
      try {
        System.out.print(prompt);
        int number = Integer.parseInt(in.nextLine().trim());
        if (number < start || number > end) {
          System.out.println("Invalid range.");
        } else {
          return number;
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid number.");
      }
    }
  }

  private double readDouble(String prompt, double start, double end) {
    while (true) {
      try {
        System.out.print(prompt);
        double number = Double.parseDouble(in.nextLine().trim());
        if (number < start || number > end) {
          System.out.println("Invalid range.");
        } else {
          return number;
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid number.");
      }
    }
  }

  private void waitEnter() {
    System.out.println("Press ENTER to continue...");
    in.nextLine();
  }
}
