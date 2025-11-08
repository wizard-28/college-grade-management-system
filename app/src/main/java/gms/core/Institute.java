package gms.core;

import gms.dsa.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Institute {
  private final Map<String, Student> students = new HashMap<>();
  private int currentSemester = 1;

  private boolean cat1Done, cat2Done, fatDone;
  private boolean cat1Updated, cat2Updated, fatUpdated;

  // students
  public void addStudent(Student s) {
    if (students.containsKey(s.id())) {
      System.out.println("Student already exists.");
      return;
    }
    students.put(s.id(), s);
  }

  public boolean removeStudent(String id) {
    return students.remove(id) != null;
  }

  public Student getStudent(String id) {
    return students.get(id);
  }

  public boolean empty() {
    return students.isEmpty();
  }

  public int size() {
    return students.size();
  }

  public void forEachStudent(java.util.function.Consumer<Student> fn) {
    students.forEach((id, s) -> fn.accept(s));
  }

  public void forEachStudentConst(java.util.function.Consumer<Student> fn) {
    students.forEach((id, s) -> fn.accept(s));
  }

  // semester + exam flow
  public int currentSemester() {
    return currentSemester;
  }

  public void setCurrentSemester(int s) {
    currentSemester = s;
  }

  public void promoteAll() {
    forEachStudent(s -> {
      s.finalizeSemester();
      s.promoteOneSemester();
    });
    System.out.println("All students promoted to next semester.");
  }

  public void nextSemester() {
    currentSemester++;
    resetExamFlow();
    resetUpdateFlags();
  }

  public boolean isCat1Done() {
    return cat1Done;
  }

  public boolean isCat2Done() {
    return cat2Done;
  }

  public boolean isFatDone() {
    return fatDone;
  }

  public void setCat1Done(boolean v) {
    cat1Done = v;
  }

  public void setCat2Done(boolean v) {
    cat2Done = v;
  }

  public void setFatDone(boolean v) {
    fatDone = v;
  }

  public void resetExamFlow() {
    cat1Done = cat2Done = fatDone = false;
  }

  public void markUpdated(Exam e) {
    switch (e) {
      case CAT1:
        cat1Updated = true;
        break;
      case CAT2:
        cat2Updated = true;
        break;
      case FAT:
        fatUpdated = true;
        break;
    }
  }

  public void resetUpdateFlags() {
    cat1Updated = cat2Updated = fatUpdated = false;
  }

  public Optional<Exam> lastExam() {
    if (isFatDone())
      return Optional.of(Exam.FAT);
    if (isCat2Done())
      return Optional.of(Exam.CAT2);
    if (isCat1Done())
      return Optional.of(Exam.CAT1);
    return Optional.empty();
  }

  public boolean canShowRollback() {
    Optional<Exam> last = lastExam();
    if (!last.isPresent())
      return false;
    switch (last.get()) {
      case CAT1:
        return cat1Updated;
      case CAT2:
        return cat2Updated;
      case FAT:
        return fatUpdated;
    }
    return false;
  }

  public HashMap<Integer, List<Student>> getAllStudentsBySemester() {
    HashMap<Integer, List<Student>> result = new HashMap<>();
    forEachStudent(s -> {
      int sem = s.semester();
      List<Student> list = result.get(sem);
      if (list == null) {
        list = new ArrayList<>();
        result.put(sem, list);
      }
      list.add(s);
    });
    return result;
  }

  public List<Student> getAllStudents() {
    List<Student> out = new ArrayList<>();
    forEachStudent(out::add);
    return out;
  }
}
