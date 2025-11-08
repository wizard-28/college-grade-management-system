package gms.core;

import gms.dsa.MergeSort;
import gms.dsa.DoublyLinkedList;
import gms.dsa.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Student {
  private final String id;
  private final String name;
  private int semester = 1;

  private final List<HashMap<String, Subject>> semSubs = new DoublyLinkedList<>();
  private final Map<Integer, Double> sgpa = new HashMap<>();
  private double cgpa = 0.0;

  public Student(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public int semester() {
    return semester;
  }

  public void promoteOneSemester() {
    semester++;
  }

  public double getCGPA() {
    return cgpa;
  }

  public double getSGPA(int sem) {
    return sgpa.getOrDefault(sem, 0.0);
  }

  public void setSGPA(int sem, double v) {
    sgpa.put(sem, v);
  }

  public void setCGPA(double v) {
    cgpa = v;
  }

  // Ensures semester list exists
  private HashMap<String, Subject> ensureSem(int sem) {
    while (semSubs.size() < sem) {
      semSubs.add(new HashMap<>());
    }
    return semSubs.get(sem - 1);
  }

  private Subject ensureSubject(int sem, String sub) {
    HashMap<String, Subject> inner = ensureSem(sem);
    return inner.computeIfAbsent(sub, Subject::new);
  }

  private Subject findSubject(int sem, String sub) {
    if (sem <= 0 || sem > semSubs.size())
      return null;
    return semSubs.get(sem - 1).get(sub);
  }

  // Subject management
  public boolean addSubject(int sem, String sub) {
    if (hasSubject(sem, sub))
      return false;
    ensureSubject(sem, sub);
    return true;
  }

  public boolean removeSubject(int sem, String sub) {
    if (sem <= 0 || sem > semSubs.size())
      return false;
    return semSubs.get(sem - 1).remove(sub) != null;
  }

  public boolean hasSubject(int sem, String sub) {
    return findSubject(sem, sub) != null;
  }

  public List<String> listSubjects(int sem) {
    if (sem <= 0 || sem > semSubs.size())
      return Collections.emptyList();
    DoublyLinkedList<String> names = new DoublyLinkedList<>(semSubs.get(sem - 1).keySet());
    MergeSort.sort(names, String::compareTo);
    return names;
  }

  // Marks handling
  public void pushMark(int sem, String subject, Exam ex, double mark) {
    ensureSubject(sem, subject).addMark(ex, mark);
  }

  public List<Double> marksHistory(int sem, String subject, Exam ex) {
    Subject s = findSubject(sem, subject);
    return (s == null) ? Collections.emptyList() : s.marksHistory(ex);
  }

  public void printHistory(int sem, String subject, Exam ex) {
    Subject s = findSubject(sem, subject);
    if (s != null) {
      s.printHistory(ex);
    }
  }

  public double latest(int sem, String subject, Exam ex) {
    Subject s = findSubject(sem, subject);
    return (s == null) ? 0.0 : (s.latest(ex) == null) ? s.latest(ex) : 0.0;
  }

  public boolean rollbackMark(int sem, String subject, Exam ex) {
    Subject s = findSubject(sem, subject);
    return s != null && s.rollback(ex);
  }

  public void finalizeSemester() {
    calculateSGPA(semester);
  }

  public void printGrades(int sem) {
    if (sem <= 0 || sem > semSubs.size()) {
      System.out.println("(No subjects)");
      return;
    }

    HashMap<String, Subject> inner = semSubs.get(sem - 1);
    if (inner.isEmpty()) {
      System.out.println("(No subjects)");
      return;
    }

    System.out.printf("%-22s%-10s%-10s%-10s%-10s%s%n",
        "Subject", "CAT1", "CAT2", "FAT", "Total", "Grade/Status");

    StringBuilder line = new StringBuilder();
    for (int i = 0; i < 70; i++)
      line.append('-');
    System.out.println(line);

    inner.forEach((subName, subj) -> {
      Double c1 = subj.latest(Exam.CAT1);
      Double c2 = subj.latest(Exam.CAT2);
      Double fat = subj.latest(Exam.FAT);

      boolean missing = (c1 == null || c2 == null || fat == null);
      double total = missing ? 0.0 : 0.3 * c1 + 0.3 * c2 + 0.4 * fat;

      System.out.printf("%-22s", subName);
      System.out.printf("%-10s", c1 == null ? "-" : c1);
      System.out.printf("%-10s", c2 == null ? "-" : c2);
      System.out.printf("%-10s", fat == null ? "-" : fat);

      if (missing)
        System.out.printf("%-10s%s%n", "-", "-");
      else
        System.out.printf("%-10.2f%c%n", total, letterGrade(total));
    });
  }

  public double calculateSGPA(int sem) {
    if (sem <= 0 || sem > semSubs.size()) {
      sgpa.put(sem, 0.0);
      recomputeCGPA();
      return 0.0;
    }

    HashMap<String, Subject> inner = semSubs.get(sem - 1);
    if (inner.isEmpty()) {
      sgpa.put(sem, 0.0);
      recomputeCGPA();
      return 0.0;
    }

    int totalCredits = 0, sumGrades = 0;
    for (var subj : inner.values()) {
      Double cat1 = subj.latest(Exam.CAT1);
      Double cat2 = subj.latest(Exam.CAT2);
      Double fat = subj.latest(Exam.FAT);
      double total = 0.3 * (cat1 == null ? 0.0 : cat1) +
          0.3 * (cat2 == null ? 0.0 : cat2) +
          0.4 * (fat == null ? 0.0 : fat);

      sumGrades += gradePoints(total);
      totalCredits++;
    }

    double sg = totalCredits == 0 ? 0.0 : (double) sumGrades / totalCredits;
    sgpa.put(sem, sg);
    recomputeCGPA();
    return sg;
  }

  private void recomputeCGPA() {
    double sum = 0;
    for (double v : sgpa.values())
      sum += v;
    cgpa = sgpa.isEmpty() ? 0.0 : sum / sgpa.size();
  }

  private static int gradePoints(double total) {
    if (total >= 90)
      return 10;
    if (total >= 80)
      return 9;
    if (total >= 70)
      return 8;
    if (total >= 60)
      return 7;
    if (total >= 50)
      return 6;
    if (total >= 40)
      return 5;
    return 0;
  }

  private static char letterGrade(double total) {
    if (total >= 90)
      return 'S';
    if (total >= 80)
      return 'A';
    if (total >= 70)
      return 'B';
    if (total >= 60)
      return 'C';
    if (total >= 50)
      return 'D';
    if (total >= 40)
      return 'E';
    return 'F';
  }
}
