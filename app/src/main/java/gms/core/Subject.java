package gms.core;

import gms.dsa.Stack;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Subject {
  private final String name;
  private final Map<Exam, Stack<Double>> marks = new EnumMap<>(Exam.class);

  public Subject(String name) {
    this.name = name;
    marks.put(Exam.CAT1, new Stack<>());
    marks.put(Exam.CAT2, new Stack<>());
    marks.put(Exam.FAT, new Stack<>());
  }

  public String name() {
    return name;
  }

  public void addMark(Exam ex, double mark) {
    marks.get(ex).push(mark);
  }

  public boolean rollback(Exam ex) {
    Stack<Double> s = marks.get(ex);
    if (s.size() <= 1)
      return false;
    return s.pop();
  }

  public Double latest(Exam ex) {
    Stack<Double> s = marks.get(ex);
    if (s.isEmpty())
      return null;
    return s.peek();
  }

  public List<Double> history(Exam ex) {
    return marks.get(ex).toList(); // bottom..top
  }
}
