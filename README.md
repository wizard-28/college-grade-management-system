# Grade Management System

A full-fledged (mostly) java grade management system that handles student records, subjects, exams, mark histories, grade computation, semester promotion, CSV-based persistence and performance benchmarking all using custom data structures and algorithms.

## Dependencies

- Java 11+
- Python with `matplotlib` installed for benchmark plotting

## How to run?

```bash
javac -d out -sourcepath app/src/main/java app/src/main/java/gms/App.java
java -cp out gms.App
```

## How to benchmark?

```bash
javac -d out -sourcepath app/src/main/java app/src/main/java/gms/App.java
java -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -XX:+AlwaysPreTouch -cp out gms.App # Disable GC for stable memory usage statistics
```

## Features

### Student & Subject Management

- Add / Remove Students
- Add / Remove subjects per semester
- View all students sorted by their CGPA using **iterative merge sort** for **a double ended linked list**
- View individual student details and subjects

### Examination Workflow

| Exam Order         | Action                                |
| ------------------ | ------------------------------------- |
| CAT1               | Enter marks for all students          |
| CAT2               | Enter marks after CAT1                |
| FAT                | Enter marks after CAT2                |
| Semester Promotion | Student gets prmoted to next semester |

After each exam you can revaluate the marks for a student, and you can rollback an invalid revaluation.

### Academic Progress

- Mark history stored using a **stack** (push & rollback) implemented using a **double ended linked list**
- Automatic SGPA and CGPA calculation
- Student promotion to next semester
- Report card generation per semester

### Data Persistence

- Saves and Loads:
  - Student data
  - Subjects
  - Marks history
  - SGPA & CGPA
  - Exam flow state
- Stored in CSV file format under `data/` folder

### Performance Benchmarking

- Benchmarks:
  - Revaluating (`PushMark`)
  - Rolling back a revalutaion (`RollbackMark`)
  - Getting the latest mark for report card generation (`LatestMark`)
  - Displaying revalutaion (marks) history (`HistoryTraversal`)
  - Iterative merge sort on students by CGPA
- Outputs to `benchmarks.csv` for easy analysis
- Plots using `matplotlib` and Python

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

## Menu Overview

The menu with all the options. Some of these options are hidden based on context/state.

```monospace
1. Add Student
2. Remove Student
3. View All Students (Sorted by CGPA)
4. View Student Details
5. Add Subject
6. Remove Subject
7. Enter Exam Marks (CAT1 → CAT2 → FAT → Promote)
8. Update Marks (Revaluation)
9. Rollback Last Marks
10. Show Student Report
11. Show Subject Marks History
12. Save Data
13. Delete Data
14. Exit
```

## Benchmarks

![Benchmarks Image](docs/benchmark.png)

| Functionality                                                  | Data Structure                              | Time Complexity | Space |
| -------------------------------------------------------------- | ------------------------------------------- | --------------- | ----- |
| Add Subject                                                    | HashMap                                     | O(1)            | O(1)  |
| Revaluation (`PushMark`)                                       | Custom Stack                                | O(1)            | O(1)  |
| Rolling back a revalutaion (`RollbackMark`)                    | Custom Stack.pop()                          | O(1)            | O(1)  |
| Latest mark retrival for report card generation (`LatestMark`) | Custom Stack.peek()                         | O(1)            | O(1)  |
| Revalutaion history display (`HistoryTraversal`)               | Stack Traversal                             | O(n)            | O(1)  |
| Sort Students by CGPA for display                              | Custom Iterative Merge Sort for Linked List | O(n log n)      | O(1)  |

## Future Enhancements

- GUI-based interface (JavaFX or Swing)
- Export reports to PDF
- Add authentication for admin access
- Transaction log for marks & revaluation history
- GPA trend graphs per student

## Authors & License

- Developed as a DSA + OOP + File Handling academic project
- Uses only custom-built data structures—no Java Collections
