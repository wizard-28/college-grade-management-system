# Grade Management System

## Dependencies

- Java 11+
- Python with `matplotlib` installed for benchmark plotting

## How to run?

```bash
javac -d out -sourcepath app/src/main/java app/src/main/java/gms/App.java
java -cp out gms.App
```

## Project Architecture Overview

```monospace

gms
│
├── App.java             -> Starts the application & loads CSV data
│
├── cli/                 -> Handles user interaction
│   └── CLI.java         -> Menu, input handling, calls core logic
│
├── core/                -> Main business logic
│   ├── Institute.java   -> Manages students, semesters & exam phases
│   ├── Student.java     -> Holds subjects, marks, SGPA/CGPA calculations
│   ├── Subject.java     -> Stack of marks per exam (CAT1, CAT2, FAT)
│   ├── Exam.java        -> Enum for exam types
│
├── dsa/                      -> Custom Data Structures + Algorithms
│   ├── DoublyLinkedList.java -> Custom List<T> implementation using a double ended linked list
│   ├── Stack.java            -> Stack using double ended linked list
│   ├── HashMap.java          -> Own Map implementation
│   └── MergeSort.java        -> Merge sort for linked list
│
├── io/                         -> File input/output & persistence
│   ├── CSV.java                -> Generic CSV reader/writer
│   └── PersistenceManager.java -> Save/load students, subjects, marks
│
└── utils/
    └── Benchmark.java  -> Analytical benchmarking for operations
```
