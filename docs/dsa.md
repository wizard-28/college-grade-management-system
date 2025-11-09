# 📘 Data Structures & Algorithms Justification

This document explains **why each data structure and algorithm** was chosen in the Grade Management System (GMS), along with **time and space complexity trade-offs** and **why alternatives were not used**.

---

## `DoublyLinkedList<T>`

### **Used in**

- `DoublyLinkedList` stacks (`marks` history in `Subject`)
- `Student.semSubs` - list of semesters
- Sorting using MergeSort

### Why a Doubly Linked List?

| Requirement                               | Why DLL is suitable                  |
| ----------------------------------------- | ------------------------------------ |
| Fast `add()` at end                       | O(1) without resizing like ArrayList |
| Fast deletion given a node                | O(1) due to `prev` and `next` links  |
| Used in Stack for marks history           | Efficient push/pop from tail         |
| Supports stable MergeSort (pointer-based) | Avoids large memory copies           |

### **Time Complexity**

| Operation                          | Time                      |
| ---------------------------------- | ------------------------- |
| Add at tail (`add`)                | O(1)                      |
| Remove at tail                     | O(1)                      |
| Insert/remove in middle (via node) | O(1)                      |
| Access by index                    | O(n) (Traversal required) |

### **Space Usage**

- Extra `prev` pointer per node → ~2× memory of singly list.
- No pre-allocation like ArrayList → memory grows dynamically.

### **Why Not ArrayList?**

| Reason                       | ArrayList downside                             |
| ---------------------------- | ---------------------------------------------- |
| Frequent add/remove from end | Triggers resizing (O(n))                       |
| MergeSort                    | Requires copying arrays (extra memory)         |
| Stack operations             | Less optimal (ArrayList pop O(1), insert O(n)) |

### Why Doubly Linked instead of Singly Linked List?

| **Use Case in Project**                                                    | **Why Doubly Linked List Works Better**                                                                                        | **Why Singly Linked List Is Bad**                                                                                                                                                     |
| -------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **1. Sorting students by CGPA (MergeSort on DoublyLinkedList)**            | Merging two sorted lists is easy - can reattach nodes by modifying both `prev` and `next` pointers. No extra traversal needed. | During merge, we don't have `prev`, so reconnecting nodes requires scanning from head to find previous node -> O(n) extra work.                                                       |
| **2. Stack for marks history in Subject (push, pop, rollback)**            | `pop()` from tail is O(1) using `tail = tail.prev`.                                                                            | If marks are stored newest at tail: removing last requires traversing from head -> O(n). If marks are stored newest at head: works fine but then sorting & other list features break. |
| **3. Convert stack to list (`Stack.toList()` used in CSV save)**           | Underlying list supports iteration both ways and behaves like a normal List.                                                   | A pure stack with only `Node* top` cannot behave like a List -> no indexing, backward iteration, or direct traversal from oldest.                                                     |
| **4. Student list used in Institute (`getAllStudents() -> sort`)**         | Students stored in DoublyLinkedList can be sorted in-place using MergeSort, no copying required.                               | Singly linked list would need auxiliary arrays or full traversal for merges -> more memory or slower runtime.                                                                         |
| **5. Future flexibility (remove subject, modify list mid-sequence, etc.)** | Can remove any node in O(1) time if node reference is known (adjust `.prev` and `.next`).                                      | Removing a node requires finding the previous node first -> full traversal -> O(n).                                                                                                   |

---

## `Stack<T>` (based on DoublyLinkedList)

### Used for

- Storing each exam's full **marks history** per subject (supports rollback)

### Why?

- `push(mark)` → add new latest mark
- `rollback()` → simply `pop()`
- `latest()` → `peek()`

### Time Complexity

| Operation | Time |
| --------- | ---- |
| Push      | O(1) |
| Pop       | O(1) |
| Peek      | O(1) |

### Space

- Only stores mark values, no wasted capacity.

---

## `HashMap<K, V>` (Custom Implementation)

### Used for

- Institute’s student storage (`Map<regNo, Student>`)
- Student → subjects in a semester (`Map<String, Subject>`)
- Cache SGPA per semester
- Maintaining history CSV mapping

### Why HashMap?

| Requirement                        | Reason              |
| ---------------------------------- | ------------------- |
| Fast lookup of student by reg ID   | O(1) average        |
| Subjects must be retrieved quickly | O(1) search         |
| CSV loading stores unordered data  | Order is irrelevant |

### Complexity

| Operation      | Average | Worst Case                |
| -------------- | ------- | ------------------------- |
| get/put/remove | O(1)    | O(n) (if many collisions) |

### Memory

- Buckets array + linked nodes → O(n)

### Why Not TreeMap / ArrayList?

| Alternative   | Problem                                                |
| ------------- | ------------------------------------------------------ |
| TreeMap       | O(log n) for get/put, slower                           |
| ArrayList     | Searching regNo / subject takes O(n) linear time       |
| LinkedHashMap | Preserves order but uses more memory — not needed here |

---

## 4. Sorting with `MergeSort`

### Used in

- Sorting students by CGPA (`viewAllSorted()`)

### Why MergeSort Over QuickSort or InsertionSort?

| Reason                                  | MergeSort Advantage                  |
| --------------------------------------- | ------------------------------------ |
| Works well on Linked Lists              | QuickSort needs random access (O(n)) |
| Stable (preserves order with same CGPA) | Required for academic fairness       |
| Deterministic O(n log n) time always    | No worst-case O(n²) like QuickSort   |

### Time Complexity

| Case    | Time       |
| ------- | ---------- |
| Best    | O(n log n) |
| Average | O(n log n) |
| Worst   | O(n log n) |

### Space

- In-place on linked list nodes → no extra arrays
- Only recursion / node pointers used

---

## Marks Rollback via Stack instead of Storing Only Latest

| Approach          | Problem                                            |
| ----------------- | -------------------------------------------------- |
| Store only latest | Cannot undo incorrect entries                      |
| Store all in List | Rollback = remove last -> O(1), but wastes memory? |
| Store in Stack    | Natural LIFO marks history + rollback = `pop()`    |

---

## CSV-based Persistence Instead of Serialization

| Reason               | Why CSV is better                       |
| -------------------- | --------------------------------------- |
| Human readable       | Easy debugging in Excel/Notepad         |
| Language-independent | Later can add web UI / Python plots     |
| Fine-grained control | You manually load SGPA, subjects, marks |

---
