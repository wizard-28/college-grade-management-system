package gms.io;

import gms.core.Exam;
import gms.core.Institute;
import gms.core.Student;
import gms.utils.CSV;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PersistenceManager {
  public static boolean deleteCSV(String dir) {
    String[] files = { "state.csv", "students.csv", "subjects.csv", "marks.csv", "sgpa.csv" };
    boolean allDeleted = true;
    boolean anyFileExists = false;

    for (String fname : files) {
      File f = new File(dir, fname);
      if (f.exists()) {
        anyFileExists = true;
        if (!f.delete()) { // delete() returns false if it fails
          System.err.println("Failed to delete: " + f.getPath());
          allDeleted = false;
        }
      }
    }

    // If no files existed, just consider it successful (nothing to delete)
    return !anyFileExists || allDeleted;
  }

  public static boolean saveCSV(Institute inst, String dir) {
    try {
      new File(dir).mkdirs();

      try (CSV.Writer state = new CSV.Writer(dir + "/state.csv");
          CSV.Writer students = new CSV.Writer(dir + "/students.csv");
          CSV.Writer subjects = new CSV.Writer(dir + "/subjects.csv");
          CSV.Writer marks = new CSV.Writer(dir + "/marks.csv");
          CSV.Writer sgpa = new CSV.Writer(dir + "/sgpa.csv")) {

        state.header("current_semester", "cat1_done", "cat2_done", "fat_done");
        students.header("regid", "name", "semester", "cgpa");
        subjects.header("regid", "semester", "subject");
        marks.header("regid", "semester", "subject", "exam", "values");
        sgpa.header("regid", "semester", "sgpa");

        state.row(inst.currentSemester(), b(inst.isCat1Done()), b(inst.isCat2Done()), b(inst.isFatDone()));

        inst.forEachStudent(s -> {
          students.row(s.id(), s.name(), s.semester(), s.getCGPA());
          for (int sem = 1; sem <= s.semester(); sem++) {
            sgpa.row(s.id(), sem, s.getSGPA(sem));
            for (String sub : s.listSubjects(sem)) {
              subjects.row(s.id(), sem, sub);
              for (Exam ex : new Exam[] { Exam.CAT1, Exam.CAT2, Exam.FAT }) {
                StringBuilder values = new StringBuilder();
                var hist = s.history(sem, sub, ex);
                for (int i = 0; i < hist.size(); i++) {
                  values.append(hist.get(i));
                  if (i + 1 < hist.size())
                    values.append(';');
                }
                marks.row(s.id(), sem, sub, ex.display(), values.toString());
              }
            }
          }
        });
      }
      return true;
    } catch (IOException e) {
      System.err.println("Save failed: " + e.getMessage());
      return false;
    }
  }

  public static boolean loadCSV(Institute inst, String dir) {
    try {
      File stateF = new File(dir + "/state.csv");
      File studentsF = new File(dir + "/students.csv");
      File subjectsF = new File(dir + "/subjects.csv");
      File marksF = new File(dir + "/marks.csv");
      File sgpaF = new File(dir + "/sgpa.csv");

      boolean have = stateF.exists() || studentsF.exists() || subjectsF.exists() || marksF.exists();
      if (!have)
        return true;

      // state
      if (stateF.exists()) {
        try (CSV.Reader r = new CSV.Reader(stateF.getPath())) {
          r.readRow(); // header
          List<String> row = r.readRow();
          if (row != null && row.size() >= 4) {
            inst.setCurrentSemester(Integer.parseInt(row.get(0)));
            inst.setCat1Done("1".equals(row.get(1)) || "true".equalsIgnoreCase(row.get(1)));
            inst.setCat2Done("1".equals(row.get(2)) || "true".equalsIgnoreCase(row.get(2)));
            inst.setFatDone("1".equals(row.get(3)) || "true".equalsIgnoreCase(row.get(3)));
          }
        }
      }

      // students
      if (studentsF.exists()) {
        try (CSV.Reader r = new CSV.Reader(studentsF.getPath())) {
          r.readRow();
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 4)
              continue;
            String id = row.get(0);
            String name = row.get(1);
            int sem = Integer.parseInt(row.get(2));
            double cg = Double.parseDouble(row.get(3));
            if (inst.getStudent(id) == null)
              inst.addStudent(new Student(id, name));
            Student s = inst.getStudent(id);
            while (s.semester() < sem)
              s.promoteOneSemester();
            s.setCGPA(cg);
          }
        }
      }

      // subjects
      if (subjectsF.exists()) {
        try (CSV.Reader r = new CSV.Reader(subjectsF.getPath())) {
          r.readRow();
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 3)
              continue;
            String id = row.get(0);
            Student s = inst.getStudent(id);
            if (s != null) {
              int sem = Integer.parseInt(row.get(1));
              String sub = row.get(2);
              s.addSubject(sem, sub);
            }
          }
        }
      }

      // marks
      if (marksF.exists()) {
        try (CSV.Reader r = new CSV.Reader(marksF.getPath())) {
          r.readRow();
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 5)
              continue;
            String id = row.get(0);
            Student s = inst.getStudent(id);
            if (s == null)
              continue;
            int sem = Integer.parseInt(row.get(1));
            String sub = row.get(2);
            Exam ex = Exam.fromString(row.get(3));
            String vals = row.get(4);
            if (!vals.isEmpty()) {
              for (String tok : vals.split(";")) {
                if (!tok.isEmpty())
                  s.pushMark(sem, sub, ex, Double.parseDouble(tok));
              }
            }
          }
        }
      }

      // sgpa
      if (sgpaF.exists()) {
        try (CSV.Reader r = new CSV.Reader(sgpaF.getPath())) {
          r.readRow();
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 3)
              continue;
            String id = row.get(0);
            Student s = inst.getStudent(id);
            if (s == null)
              continue;
            int sem = Integer.parseInt(row.get(1));
            double val = Double.parseDouble(row.get(2));
            s.setSGPA(sem, val);
          }
        }
      }

      return true;
    } catch (IOException e) {
      System.err.println("Load failed: " + e.getMessage());
      return false;
    }
  }

  private static int b(boolean v) {
    return v ? 1 : 0;
  }
}
