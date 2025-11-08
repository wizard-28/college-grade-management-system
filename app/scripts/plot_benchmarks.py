import csv
import math

import matplotlib.pyplot as plt

# Load CSV -> dict[op] = [(x, time_ns, mem_kb), ...]
data = {}
with open("benchmarks.csv", "r") as f:
    header = next(f)  # Operation,InputSize,TimeNanoseconds,MemoryKB
    for line in f:
        op, size, t_ns, mem_kb = line.strip().split(",")
        size = int(size)
        t_ns = float(t_ns)
        mem_kb = float(mem_kb)
        data.setdefault(op, []).append((size, t_ns, mem_kb))

# Sort by x for each op
for op in data:
    data[op].sort(key=lambda row: row[0])


# Helper to draw reference lines (scaled)
def plot_onelog(ax, x, y, label):
    # Build n log n line and scale to y-range
    ref = [(n * math.log2(n)) if n > 0 else 0 for n in x]
    if max(ref) > 0:
        scale = (max(y) / max(ref)) if max(ref) != 0 else 1.0
        ref = [v * scale for v in ref]
        ax.plot(x, ref, "--", label=label)


def plot_linear(ax, x, y, label):
    if not x:
        return
    max_x = max(x)
    if max_x <= 0:
        return
    ref = [val * (max(y) / max_x) for val in x]
    ax.plot(x, ref, "--", label=label)


def plot_flat(ax, x, y, label):
    if not y:
        return
    mean_val = sum(y) / len(y)
    xref = [min(x), max(x)] if x else [0, 1]
    yref = [mean_val, mean_val]
    ax.plot(xref, yref, "--", label=label)


# Prepare figure (2 rows × 3 cols): top = time; bottom = memory
fig, axes = plt.subplots(2, 4, figsize=(18, 8))  # EDITED
fig.suptitle(
    "Benchmark Results — Time & Memory (with Theoretical References)",
    fontsize=14,
    y=0.98,
)

# ---------------- TIME: O(1) vs HISTORY SIZE ----------------
ax = axes[0][0]
for op in ["PushMark", "RollbackMark", "LatestMark"]:
    if op in data:
        x = [row[0] for row in data[op]]
        y = [row[1] for row in data[op]]
        ax.plot(x, y, marker="o", label=op)
plot_flat(
    ax, x if "PushMark" in data else [], y if "PushMark" in data else [], "O(1) Ref"
)
ax.set_title("Time — O(1) ops vs History Size", fontsize=11)
ax.set_xlabel("History Size (marks)")
ax.set_ylabel("Time (ns)")
ax.grid(True)
ax.legend(fontsize=9)

# ---------------- TIME: O(n log n) vs STUDENTS ---------------
ax = axes[0][1]
if "MergeSort" in data:
    x = [row[0] for row in data["MergeSort"]]
    y = [row[1] for row in data["MergeSort"]]
    ax.plot(x, y, marker="o", label="MergeSort")
    plot_onelog(ax, x, y, "O(n log n) Ref")
ax.set_title("Time — Merge Sort vs Students", fontsize=11)
ax.set_xlabel("Number of Students")
ax.set_ylabel("Time (ns)")
ax.grid(True)
ax.legend(fontsize=9)

# ---------------- TIME: O(n) vs HISTORY SIZE -----------------
ax = axes[0][2]
if "HistoryDisplay" in data:
    x = [row[0] for row in data["HistoryDisplay"]]
    y = [row[1] for row in data["HistoryDisplay"]]
    ax.plot(x, y, marker="o", label="HistoryDisplay")
    plot_linear(ax, x, y, "O(n) Ref")
ax.set_title("Time — History Traversal vs History Size", fontsize=11)
ax.set_xlabel("History Size (marks)")
ax.set_ylabel("Time (ns)")
ax.grid(True)
ax.legend(fontsize=9)

# ---------------- MEMORY: O(1) ops vs HISTORY SIZE ----------
ax = axes[1][0]
for op in ["PushMark", "RollbackMark", "LatestMark"]:
    if op in data:
        x = [row[0] for row in data[op]]
        m = [row[2] for row in data[op]]
        ax.plot(x, m, marker="o", label=op)
# expected O(1) memory delta (near flat)
if "PushMark" in data:
    x = [row[0] for row in data["PushMark"]]
    m = [row[2] for row in data["PushMark"]]
    plot_flat(ax, x, m, "O(1) Ref")
ax.set_title("Memory — O(1) ops vs History Size", fontsize=11)
ax.set_xlabel("History Size (marks)")
ax.set_ylabel("Δ Memory (KB)")
ax.grid(True)
ax.legend(fontsize=9)

# ✅ ---------------- TIME: HashMap Ops (New Subplot) ----------------

HASHMAP_OPS = ["HashMapPut", "HashMapGet", "HashMapRemove"]
ax = axes[0][3]
for op in HASHMAP_OPS:
    if op in data:
        x = [row[0] for row in data[op]]  # Number of items in HashMap
        y = [row[1] for row in data[op]]  # Time in ns
        ax.plot(x, y, marker="o", label=op)
if "HashMapPut" in data:
    plot_flat(ax, x, y, "O(1) Ref")
ax.set_title("Time — HashMap Put/Get/Remove", fontsize=11)
ax.set_xlabel("Number of Items")
ax.set_ylabel("Time (ns)")
ax.grid(True)
ax.legend(fontsize=9)

# ---------------- MEMORY: MergeSort vs STUDENTS -------------
ax = axes[1][1]
if "MergeSort" in data:
    x = [row[0] for row in data["MergeSort"]]
    m = [row[2] for row in data["MergeSort"]]
    ax.plot(x, m, marker="o", label="MergeSort")
    plot_flat(ax, x, m, "O(1) Ref")
ax.set_title("Memory — Merge Sort vs Students", fontsize=11)
ax.set_xlabel("Number of Students")
ax.set_ylabel("Δ Memory (KB)")
ax.grid(True)
ax.legend(fontsize=9)

# ---------------- MEMORY: History Traversal vs HISTORY SIZE --
ax = axes[1][2]
if "HistoryDisplay" in data:
    x = [row[0] for row in data["HistoryDisplay"]]
    m = [row[2] for row in data["HistoryDisplay"]]
    ax.plot(x, m, marker="o", label="HistoryDisplay")
    # traversal should be ~O(1) memory (flat)
    plot_flat(ax, x, m, "O(1) Ref")
ax.set_title("Memory — History Traversal vs History Size", fontsize=11)
ax.set_xlabel("History Size (marks)")
ax.set_ylabel("Δ Memory (KB)")
ax.grid(True)
ax.legend(fontsize=9)

# ✅ ---------------- MEMORY: HashMap Ops (New Subplot) ----------------
ax = axes[1][3]  # EDITED
for op in HASHMAP_OPS:
    if op in data:
        x = [row[0] for row in data[op]]
        m = [row[2] for row in data[op]]
        ax.plot(x, m, marker="o", label=op)
if "HashMapPut" in data:
    plot_flat(ax, x, m, "O(1) Ref")
ax.set_title("Memory — HashMap Ops", fontsize=11)
ax.set_xlabel("Number of Items")
ax.set_ylabel("Δ Memory (KB)")
ax.grid(True)
ax.legend(fontsize=9)

plt.tight_layout(rect=[0, 0, 1, 0.95])
plt.subplots_adjust(hspace=0.5, wspace=0.3)
plt.show()
