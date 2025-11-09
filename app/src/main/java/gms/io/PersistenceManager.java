package gms.io;

import gms.core.Exam;
import gms.core.Institute;
import gms.core.Student;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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

        state.row(inst.currentSemester(), booleanToInteger(inst.isCat1Done()), booleanToInteger(inst.isCat2Done()),
            booleanToInteger(inst.isFatDone()));

        inst.forEachStudent(s -> {
          students.row(s.id(), s.name(), s.semester(), s.getCGPA());
          for (int sem = 1; sem <= s.semester(); sem++) {
            sgpa.row(s.id(), sem, s.getSGPA(sem));
            for (String sub : s.listSubjects(sem)) {
              subjects.row(s.id(), sem, sub);
              for (Exam ex : new Exam[] { Exam.CAT1, Exam.CAT2, Exam.FAT }) {
                StringBuilder values = new StringBuilder();
                var hist = s.marksHistory(sem, sub, ex);
                boolean first = true;
                for (Double v : hist) {
                  if (!first)
                    values.append(';');
                  values.append(v);
                  first = false;
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
            Iterator<String> it = row.iterator();
            inst.setCurrentSemester(Integer.parseInt(it.next()));
            inst.setCat1Done(parseBool(it.next()));
            inst.setCat2Done(parseBool(it.next()));
            inst.setFatDone(parseBool(it.next()));
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

            Iterator<String> it = row.iterator();
            String id = it.next();
            String name = it.next();
            int sem = Integer.parseInt(it.next());
            double cg = Double.parseDouble(it.next());

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
          r.readRow(); // skip header
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 3)
              continue;

            Iterator<String> it = row.iterator();

            String id = it.next();
            Student s = inst.getStudent(id);
            if (s != null) {
              int sem = Integer.parseInt(it.next());
              String sub = it.next();
              s.addSubject(sem, sub);
            }
          }
        }
      }

      // marks
      if (marksF.exists()) {
        try (CSV.Reader r = new CSV.Reader(marksF.getPath())) {
          r.readRow(); // skip header
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 5)
              continue;

            Iterator<String> it = row.iterator();

            String id = it.next();
            Student s = inst.getStudent(id);
            if (s == null)
              continue;

            int sem = Integer.parseInt(it.next());
            String sub = it.next();
            Exam ex = Exam.fromString(it.next());
            String vals = it.next();

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
          r.readRow(); // skip header
          List<String> row;
          while ((row = r.readRow()) != null) {
            if (row.size() < 3)
              continue;

            Iterator<String> it = row.iterator();

            String id = it.next();
            Student s = inst.getStudent(id);
            if (s == null)
              continue;

            int sem = Integer.parseInt(it.next());
            double val = Double.parseDouble(it.next());

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

  private static int booleanToInteger(boolean v) {
    return v ? 1 : 0;
  }

  private static boolean parseBool(String s) {
    return "1".equals(s) || "true".equalsIgnoreCase(s);
  }
}
