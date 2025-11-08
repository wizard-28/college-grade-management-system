package gms.core;

public enum Exam {
  CAT1, CAT2, FAT;

  public String display() {
    switch (this) {
      case CAT1:
        return "CAT1";
      case CAT2:
        return "CAT2";
      case FAT:
        return "FAT";
    }
    return "";
  }

  public static Exam fromString(String s) {
    if ("CAT1".equalsIgnoreCase(s))
      return CAT1;
    if ("CAT2".equalsIgnoreCase(s))
      return CAT2;
    if ("FAT".equalsIgnoreCase(s))
      return FAT;
    throw new IllegalArgumentException("Unknown exam: " + s);
  }
}
