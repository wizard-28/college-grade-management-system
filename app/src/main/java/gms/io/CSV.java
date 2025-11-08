package gms.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSV {
  public static final String SEP = ",";

  public static class Writer implements Closeable {
    private final PrintWriter out;

    public Writer(String path) throws IOException {
      this.out = new PrintWriter(new FileWriter(path));
    }

    public void header(String... cols) {
      row((Object[]) cols);
    }

    public void row(Object... cols) {
      for (int i = 0; i < cols.length; i++) {
        out.print(cols[i]);
        if (i + 1 < cols.length)
          out.print(SEP);
      }
      out.print("\n");
    }

    public void close() {
      out.close();
    }

    public boolean ok() {
      return !out.checkError();
    }
  }

  public static class Reader implements Closeable {
    private final BufferedReader in;

    public Reader(String path) throws IOException {
      this.in = new BufferedReader(new FileReader(path));
    }

    public List<String> readRow() throws IOException {
      String line = in.readLine();
      if (line == null)
        return null;
      if (line.isEmpty())
        return new ArrayList<>();
      List<String> cols = new ArrayList<>();
      String[] parts = line.split(",", -1);
      for (String p : parts)
        cols.add(p.trim());
      return cols;
    }

    public void close() throws IOException {
      in.close();
    }
  }
}
