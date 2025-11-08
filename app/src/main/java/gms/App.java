package gms;

import gms.cli.CLI;
import gms.core.Institute;
import gms.io.PersistenceManager;

public class App {
  public static void main(String[] args) {
    Institute inst = new Institute();
    // load previous state (if any)
    PersistenceManager.loadCSV(inst, "data");
    new CLI(inst).run();
  }
}
